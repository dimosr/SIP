package gov.nist.sip.proxy.presenceserver;

import java.util.*;
import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;
import gov.nist.sip.proxy.*;
import gov.nist.sip.proxy.registrar.*;


/**
 * The PresenceServer stores presence info about those registered
 * user agents (presentities) that other user agents (watchers) are
 * subscribing to.<br>
 *
 * The PresenceServer processes Register, Publish, Subscribe and
 * Notify requests.
 *
 * The PresenceServer filters incoming requests and if no obvious
 * errors are found, it pushes them down to the PresentityManager
 * for further processing. The PresentityManager returns a
 * responseCode that is used to send a response to the request.
 *
 * <h3>ToDo</h3><ul>
 *   <li>If a subscription for xpidf-data is received for a user that
 *       is registered it is forwarded, but if this user, the notifier,
 *       is offline, the subscription response will ultimately be a 404.
 *       instead, buffer the subscription and send a 202.</li>
 *   <li>An empty Notify is sent after the initial one when subscribing
 *       an offline notifier. Remove it.</
 *  </ul>
 * @author deruelle
 * @author Henrik Leion
 * @version 1.1
 */

public class PresenceServer {
    
    protected int expiresTime;
    protected Proxy proxy;
    protected PresentityManager presentityManager;
    private   Vector notifysToBeSent;
    
    public PresenceServer(Proxy proxy) {
        expiresTime=proxy.getConfiguration().expiresTime;
        presentityManager=new PresentityManager(this);
        this.proxy=proxy;
    }
    
    
    
    
    
    //*****************************
    //   REQUEST PROCESSING
    //*****************************
    
    
    
    /** When a User Agent registers to the Registrar, the PresenceServer
     *  hands over all the contact Uri:s with expires times to the
     *  presentityManager.
     *  This method does not send a response. That is done by the registrar.<p>
     *
     *  TODO: <ul>
     *   <li>Howto add more contacts and updating subscriptions</li>
     *  </ul>
     */
    public void processRegisterRequest(SipProvider sipProvider,
				       Request request,
				       ServerTransaction serverTransaction) {
        
        if (ProxyDebug.debug) {
            ProxyDebug.println("PresenceServer:processRegisterRequest: received a request\n" + request);
        }
        
        try {
            //If the Accept header includes the Publish method
            // the PresenceServer will act as a ESC for the user
            boolean stateAgent = false;
            ListIterator it = (ListIterator)request.getHeaders(AllowHeader.NAME);
            while (it.hasNext()) {
                AllowHeader allowHeader = (AllowHeader)it.next();
                if (allowHeader.getMethod().equalsIgnoreCase("PUBLISH")) {
                    stateAgent = true;
                    break;
                }
            }
            
            //if (!stateAgent) return;
            
            //We are acting as the EventStateCompositor and/or PresenceAgent for
            // this User (EPA or PUA)
            
            //Extract a default expires value.
            //Should be retrieved from the registrar instead.
            // Note, the expires value for a subscription and a registration are
            //   quite different. The registration should last more than one hour
            //   and less than 136 years, while the subscriptions are typically
            //   a few hours or so.
            ExpiresHeader expiresHeader=
            (ExpiresHeader)request.getHeader(ExpiresHeader.NAME);
            int expires;
            if (expiresHeader == null) {
                expires = getExpiresTime(); //This is the PresenceServer expirestime
            } else {
                expires = expiresHeader.getExpires();
            }
            
            String notifierKey = getKey(request, "To");
            String contactURI;
            ListIterator cit = request.getHeaders(ContactHeader.NAME);
            //update the notifer one contact at a time
            //  (there is always at least one)
            while (cit.hasNext()) {
                ContactHeader contactH = (ContactHeader)cit.next();
                contactURI = contactH.getAddress().getURI().toString();
                /* Could do something about individual expires times.
                   Note, there must still be the general expires time
                   int localExpires = contactH.getExpires();
                   if(localExpires == -1) {
                   localExpires = expires;
                   }
                 */
                presentityManager.processRegister(notifierKey, expires, contactURI);
            }
            
            
            
            
            //No response, that's for the Registrar
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /** The registrar can (should) have a set of known users that are registered
     *  on startup. The registrar calls this method to let the presenceserver know of them
     *  so that other users may subscribe to them before they go online.
     */
    
    public void processUploadedRegistration(Registration registration) {
        /*	 java.util.Vector 	getContactsList() //list of contactHeaders
                 java.lang.String 	getDisplayName()
                 java.lang.String 	getKey() */
        ProxyDebug.println("PresenceServer.processUploadedRegistration: " +
        "\n   Key="+registration.getKey() +
        "\n   DisplayName="+registration.getDisplayName() +
        "\n   Contacts="+registration.getContactsList().toString());
        
	
        presentityManager.processRegister(registration.getKey(),Integer.MAX_VALUE);
    }
    
    
    
    /**
     * The PresenceServer can act as a Event State Compositor and processes
     * Publish requests according to draft-ietf-sip-publish-02
     */
    public void processPublishRequest(SipProvider sipProvider,
				      Request request,
				      ServerTransaction serverTransaction) {
        
        if (ProxyDebug.debug) {
            ProxyDebug.println("PresenceServer:processPublishRequest: received a request\n" + request);
        }
        
        try {
            int responseCode;
            
            
            //Authenticate user
            
            
            //Hand over to PresentityManager
            responseCode = presentityManager.processPublish(request);
            
            //Send response
            MessageFactory messageFactory = proxy.getMessageFactory();
            HeaderFactory headerFactory = proxy.getHeaderFactory();
            Response response = messageFactory.createResponse(responseCode, request);
            
            //add SIP-ETag header
            Header eTag = headerFactory.createHeader("Sip-ETag",
						     proxy.getProxyUtilities().generateTag());
            response.addHeader(eTag);
            
            //Add tag-parameter to To-header
            ToHeader toHeader = (ToHeader)response.getHeader(ToHeader.NAME);
            if(toHeader.getTag()==null) {
                toHeader.setTag(proxy.getProxyUtilities().generateTag());
                response.setHeader(toHeader);
            }
            if(serverTransaction!=null) {
                serverTransaction.sendResponse(response);
            } else {
                sipProvider.sendResponse(response);
                ProxyDebug.println("PresenceServer.processPublishRequest() - send using sipProvider()");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    
    
    /** Starts or ends subscriptions between registered user agents.<br>
     *  After verifyiong that the headers are understood, the PresenceServer
     *  tries to match the Event-, Supported- and AcceptHeaders between
     *  subscriber and notifier.<br>
     *
     *  The proxy has already verified that we are responsible for the
     *  domain and that we are tha ESC or PA for the notifier, so there should
     *  be no need for forwarding the request.<br>
     *
     *
     * <h3>Policy for forwarding subscriptions.</h3> <ol>
     *   <li>If the Notifier is registered and is publishing, the subscritption
     *       ends here. WatcherInfo is updated and PS sends notifies to subscriber
     *       according to authorization. Note, the default process should be that
     *       the notifiers are all registered from the registrations.xml file, so
     *       they don't have to be online.</li>
     *   <li>If the notifier is registered, but the subscriber requires some non-pidf
     *       notification format (such as Windows Messengers xpidf) or has other
     *       special requirements that the PS can't understand, the subscription is
     *       forwarded statelessly to notifyer.</li>
     *   <li>If user is not registered, but the domain indicates that we are
     *       responsible for him, a 404 is sent (by Proxy)</li>
     *  </ol>
     *
     *
     *  <h3>About authorization</h3>
     *  The subscriber must always know who is receiving his notiofications,
     *  and must be able to terminate all notifications individually. There
     *  are three authorization modes available:<ol>
     *   <li>WatcherInfo. Server authenticates subscriber and notifier trusts
     *       that this information is correct.</li>
     *   <li>Server knows subscriber authentication secret and is allowed to
     *       impersonate subscriber. No other subscriber may utilize the
     *       notifications received in this dialog, but the server may
     *       collect notifications from several sources and send to subscriber </li>
     *   <li>Forward request</li></ol>
     *
     *  If the request was not forwarded, a response is sent.<br>
     *  Hands over the Subscription to the presentityManager
     */
    public void processSubscribeRequest(SipProvider sipProvider,
					Request request,
					ServerTransaction serverTransaction) {
        
        if (ProxyDebug.debug) {
            ProxyDebug.println
            ("PresenceServer:processSubscribeRequest:received a request\n"
            + request);
        }
        
        try {
            int responseCode;
            
            //Authenticate user
            
            //Examine expires header
            //Note: no regard is taken to the registration time of the
            //  notifyer, a subscription can last longer than a registration
            //  because the registrar will not supply the expires time.
            ExpiresHeader expiresHeader=
            (ExpiresHeader)request.getHeader(ExpiresHeader.NAME);
            int expires;
            if (expiresHeader==null) {
                HeaderFactory headerFactory = proxy.getHeaderFactory();
                expiresHeader=headerFactory.createExpiresHeader(getExpiresTime());
                request.setHeader(expiresHeader);
            } else {
                expires = expiresHeader.getExpires();
                if (expires < 0) {
                    expiresHeader.setExpires(0);
                } else if (expires > getExpiresTime()) {
                    expiresHeader.setExpires(getExpiresTime());
                }
            }
            expires = expiresHeader.getExpires();
            ProxyDebug.println("   ExpiresHeader = " + expires);
            
            
            
            //Hand over the request to PresentityManager
            Dialog dialog = serverTransaction.getDialog();
            responseCode = presentityManager.processSubscribe(request, dialog, expires);
            
            
            //Send response, add tag if necessary
            MessageFactory messageFactory = proxy.getMessageFactory();
            Response response = messageFactory.createResponse(responseCode, request);
            ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
            if (to.getTag() == null) {
                to.setTag(ProxyUtilities.generateTag());
            }
            serverTransaction.sendResponse(response);
            
            //If a new subscription was created, it should get
            // a notify straight away, especially if it is a Fetcher
            // or if the subscription was terminated directly,
            // otherwise, the subscription is lost.
            //It must be sent after the response above, of course.
            presentityManager.sendInitialNotify();
            
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    /**
     * Processes Notify requests from virtual subscriptions, in other words
     *  Subscriptions that the PresenceServer has issued to external resources
     *   on behalf of a resource list.<br>
     *  Updates the internal presence information.<br>
     *  Checks that the attached pidf-document is valid, and replies with
     *  a 200 OK. Sets the subscribers dirty bit or terminates the
     *  serversubscription depending on the subscription state.
     */
    
    public void processNotifyRequest(SipProvider sipProvider,
    Request notify,
    ServerTransaction serverTransaction) {
        if (ProxyDebug.debug) {
            ProxyDebug.println("processNotifyRequest: PresenceServer receives a Notify request");
            
        }
        
        try {
            String presentity = getKey((Message)notify, "From");
            int responseCode = Response.OK;
            MessageFactory messageFactory = proxy.getMessageFactory();
            Response response;
            
            if(serverTransaction == null) {
                responseCode = Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST;
                response = messageFactory.createResponse(responseCode, notify);
                sipProvider.sendResponse(response);
                return;
            }
            
            Dialog dialog = serverTransaction.getDialog();
            if(dialog == null) {
                responseCode = Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST;
                response = messageFactory.createResponse(responseCode, notify);
                serverTransaction.sendResponse(response);
                return;
            }
            
            //Send to presentityManager
            responseCode = presentityManager.processNotify(notify, dialog);
            
            //Reply to notifier
            response = messageFactory.createResponse(responseCode,notify);
            serverTransaction.sendResponse(response);
            
            ProxyDebug.println("processNotifyRequest(), response sent:" +
            response.toString());
            
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    //*****************************
    //   RESPONSE PROCESSING
    //*****************************
    
    
    
    
    /** If the response is not a class 2xx response, we might want to try again or terminate
     *  all subscriptions to this UA. Otherwise hand over the recently created dialog to
     *  the presentityManager.
     */
    public void processSubscribeResponse(Response response, ClientTransaction clientTransaction) {
        
        if (ProxyDebug.debug) {
            ProxyDebug.println("PresenceServer:processSubscribeResponse: received a response\n" + response);
        }
        
        try {
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    /** Do nothing?
     *
     */
    public void processNotifyResponse(Response response, ClientTransaction clientTransaction) {
        
        if (ProxyDebug.debug) {
            ProxyDebug.println("PresenceServer:processNotifyResponse: received a response\n" + response);
        }
        
        try {
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    //*****************************
    //   SEND METHODS
    //*****************************
    
    
    /**
     *  Sends an options request to the UA
     */
    protected void sendOptionsRequest(String toURI) {
        
    }
    
    
    
    
    
    /**
     *  Sends an Subscribe request to an external ESC
     *
     */
    protected void sendSubscribeRequest(String toURI,
    int expires, Dialog dialog) {
        
    }
    
    
    protected void sendNotifyRequest(Subscriber subscriber) {
        
        if (ProxyDebug.debug) {
            ProxyDebug.println("PresenceServer:sendNotifyRequest to " +
            subscriber.getSubscriberURL());
        }
        try {
            
            HeaderFactory hf        = proxy.getHeaderFactory();
            MessageFactory mf       = proxy.getMessageFactory();
            Dialog dialog           = subscriber.getDialog();
            
            if(dialog == null) {
                ProxyDebug.println("PresenceServer:sendNotifyRequest: dialog doesn't exist, nothing sent");
                return;
            }
            
            //First perform some checks
            String subscriptionState = subscriber.getSubscriptionState();
            String notifyBody = subscriber.removeNotifyBody();
            
            if(notifyBody==null || notifyBody.equals("")) {
                if (!subscriptionState.equalsIgnoreCase("terminated")) {
                    ProxyDebug.println
                    ("PresenceServer:sendNotifyRequest: " +
                    "   NotifyBody is null \n" +
                    "   subscriptionState = " + subscriptionState +
                    "   Nothing sent");
                    return;
                }
            }
            
            
            
            //Create the Notify request and add som headers
            Request notify = dialog.createRequest(Request.NOTIFY);
            
            //Add Event Header
            notify.addHeader(hf.createEventHeader("presence"));
            
            //Create SubscriptionState Header (add later)
            SubscriptionStateHeader ssH =
            hf.createSubscriptionStateHeader(subscriptionState);
            
            if ((subscriptionState.equalsIgnoreCase(SubscriptionStateHeader.ACTIVE)) ||
            (subscriptionState.equalsIgnoreCase(SubscriptionStateHeader.PENDING))) {
                
                //Add expires parameter to Subscription state header
                ssH.setExpires(subscriber.getExpiresTime());
                
                //Add Presence-info and Content-Type Header.
                //The media type should be derived from the Registration request
                ContentTypeHeader ctH = hf.createContentTypeHeader("application", "pidf+xml");
                notify.setContent(notifyBody, ctH);
                
                //Add Authorization Header?
            } else {
                ProxyDebug.println("subscription state is " +
                subscriptionState);
            }
            
            //Add SubscriptionStateHeader
            notify.addHeader(ssH);
            
            ClientTransaction clientTransaction =
            proxy.getSipProvider().getNewClientTransaction(notify);
            
            //Send Notify
            dialog.sendRequest(clientTransaction);
            
            if(ProxyDebug.debug) {
                ProxyDebug.println("PresenceServer:sendNotifyRequest. Request sent: \n" + notify.toString());
            }
            
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    
    //*****************************
    //   GETTERS & SETTERS
    //*****************************
    
    
    public PresentityManager getPresentityManager() {
        return presentityManager;
    }
    
    
    public int getExpiresTime() {
        return expiresTime;
    }
    
    /**
     *  returns true if the notifier of this subscription
     *  is publishing it's state here and if the subscriber
     *  understands pidf+xml (modify if the server starts handling
     *  new formats)
     *  <h3ToDo</h3><ul>
     *    <li>Should not only check if the notifier is registered, but if he actually
     *        is publishing his state info, instead of assuming it. This means that the
     *        registrations in registrations.xml must indicate this.</li>
     *   </ul>
     **/
    public boolean isStateAgent(Request request) {
        String notifierKey = getKey(request, "To");
        
        if (presentityManager.hasNotifier(notifierKey)) {
            if(request.getMethod().equalsIgnoreCase("PUBLISH") ) {
                return true;
            } else if(request.getMethod().equals(Request.SUBSCRIBE) ) {
                //Check acceptheaders for "application/pidf+xml, else forward
                Iterator  acceptHeaders = request.getHeaders(AcceptHeader.NAME);
                while (acceptHeaders.hasNext()) {
                    AcceptHeader acceptHeader = (AcceptHeader)acceptHeaders.next();
                    if(acceptHeader.getContentType().equals("application") &&
                    acceptHeader.getContentSubType().equals("pidf+xml"))
                        return true;
                }
                return false;
            }
        }   else {
            ProxyDebug.println("could not find notifier record for "
            + notifierKey);
            return false;
        }
        return false;
    }
    
    /** Returns true if the registration records the from header as being a buddy. So OK
     * to report that buddy has not yet registered.
     */
    public boolean isBuddy( Request request ) {
        String notifierKey = getKey(request, "To");
        if (proxy.getRegistrar().hasRegistration(notifierKey)) {
            Registration registration = proxy.getRegistrar().getRegistration(notifierKey);
            String subscriberKey = getKey(request,"From");
            if (registration.isMyBuddy(subscriberKey)) return true;
            else return false;
        }  else return false;
    }
    
    
    
    //*****************************
    //   UTILITY METHODS
    //*****************************
    
    
    
    
    /**
     * Returns the value of a named header
     * @author deruelle
     */
    private  String getKey(Message message,String header) {
        try{
            Address address=null;
            if (header.equals("From") ) {
                FromHeader fromHeader=(FromHeader)
                message.getHeader(FromHeader.NAME);
                address=fromHeader.getAddress();
                
            }
            else
                if (header.equals("To") ) {
                    ToHeader toHeader=(ToHeader)message.getHeader(ToHeader.NAME);
                    address=toHeader.getAddress();
                    
                }
            
            javax.sip.address.URI cleanedUri=null;
            if (address==null) {
                cleanedUri= getCleanUri( ((Request)message).getRequestURI());
            }
            else {
                // We have to build the key, all
                // URI parameters MUST be removed:
                cleanedUri = getCleanUri(address.getURI());
            }
            
            if (cleanedUri==null) return null;
            
            String  keyresult=cleanedUri.toString();
            ProxyDebug.println("DEBUG, PresenceServer, getKey(), the key is: " +
            keyresult);
            return keyresult.toLowerCase();
            
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    /**
     * Helper for getKey. Removes parameters from URI
     * @author deruelle
     */
    private static URI getCleanUri(URI uri) {
        if (uri instanceof SipURI) {
            SipURI sipURI=(SipURI)uri.clone();
            
            Iterator iterator=sipURI.getParameterNames();
            while (iterator!=null && iterator.hasNext()) {
                String name=(String)iterator.next();
                sipURI.removeParameter(name);
            }
            return  sipURI;
        }
        else return  uri;
    }
    
    
    /** Stop the presentity manager.
     */
    public void stop() {
        this.presentityManager.stop();
    }
    
    //*****************************
    //   OLD METHODS
    //
    // Required by other parts of the proxy
    // Should deprecate.
    //*****************************
    
    
    /**
     *  This is only here because RequestValidation.java and the Registrar needs it.
     *  Should deprecate.
     *  @return True only if authorization gives 200 OK
     */
    public boolean hasAuthorization(Request request) {
        //everybody is accepted for now. Note that we can get into a
        // conflict with method authorize() if we're not careful
        return true;
    }
    
    
    
    
    
}

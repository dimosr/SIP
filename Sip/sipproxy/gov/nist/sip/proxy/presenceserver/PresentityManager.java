package gov.nist.sip.proxy.presenceserver;

import gov.nist.sip.proxy.*;

import java.util.*;

import javax.sip.Dialog;
import javax.sip.address.*;
import java.io.File;
import javax.sip.message.*;
import javax.sip.header.*;
import gov.nist.sip.proxy.presenceserver.pidfparser.*;




/** Implements a presetity manager.
 *
 * @author  Henrik Leion
 * @version 0.1
 *
 */
public class PresentityManager implements Runnable {

    protected HashMap subscribers;
    protected HashMap notifiers;
    protected HashMap resourcelists;
    protected HashMap virtualSubscriptions;
    protected PresenceServer presenceServer;
    protected boolean isRunning;
	/** A list of all registered notifiers. The objects are only referenced
	 * to from this hashtable, and removing them from it should delete them **/ 
    private   LinkedList initialNotifyQueue;

    private String resourceListDir; 

    public XMLcpimParser xmlCpimParser;
    public XMLResourceListParser xmlResourceListParser;    

    public PresentityManager(PresenceServer presenceServer) {
	subscribers   = new HashMap(); 
	initialNotifyQueue = new LinkedList();
	notifiers     = new HashMap();
	resourcelists = new HashMap();
	virtualSubscriptions = new HashMap();
	this.presenceServer = presenceServer;
        this.isRunning = true;
	
	new Thread(this).start();

	xmlCpimParser = new XMLcpimParser();
	xmlResourceListParser = new XMLResourceListParser(this);

	resourceListDir = "";
    }


    public void stop() {
            this.isRunning = false;
    }

    //**************************
    //  Processing methods
    //**************************


    /** Creates or updates a subscription.
     *  New subscriptions with expires=0 are called fetchers, they only require one notification
     *  @returns responseCode
     **/

  public synchronized int processSubscribe(Request request, Dialog dialog, int expires) {

      ProxyDebug.println("PresenceServer, processSubscribeRequest: \n" + request);
	
      if (isResourceListSubscriber(request)) {
	  //ResourceLists demand a little special handling
	  return processResourceListSubscribe(request, dialog, expires);
      }

      int responseCode;
      String    notifierKey = getKey((Message)request, "To");
      String  subscriberKey = getKey((Message)request, "From");
      String   subscriberId = dialog.getDialogId();
      Notifier     notifier = (Notifier)notifiers.get(notifierKey);
      Subscriber subscriber = (Subscriber)subscribers.get(subscriberId);


      if (subscriber!=null) {
	  ProxyDebug.println("   PM.processSubscribe - updating old subscription");
	  if (expires == 0) {
	      //remove old subscription, send notify
	      responseCode = notifier.removeSubscriber(subscriberKey);
	      subscriber.setSubscriptionState("terminated");
	  } else if (notifier.hasSubscriber(subscriberId)) {
	      //Update subscription, don't send Notify
	      //Authorize subscriber
	      responseCode = notifier.authorize(subscriberKey,Request.SUBSCRIBE);
	      if (responseCode==Response.OK) {
		  subscriber.setExpires(expires);
		  subscriber.setSubscriptionState("active");
	      } else if (responseCode==Response.ACCEPTED) {
		  subscriber.setExpires(expires);
		  subscriber.setSubscriptionState("pending");
	      } else {
		  subscriber.setSubscriptionState("terminated");
	      }
	      return responseCode;
	  } else {
	      //subscriber object exists, but is not a subscriber
	      //to this notifier. Possibly the subscriberId (dialogId) has been reused
	      //don't send notify
	      return Response.SERVER_INTERNAL_ERROR;
	  }
      } else { 
	  //subscriber object does not exist (The dialogId was unmatched)
	  //Authorize subscriber and create object
	  responseCode = notifier.authorize(subscriberKey,Request.SUBSCRIBE);
	  if (responseCode==Response.OK) {
	      subscriber = createSubscriber(request,subscriberKey,dialog,expires);
	      subscriber.setExpires(expires);
	      subscriber.setSubscriptionState("active");
	      subscriber.updateNotifyBody(notifier.getFullState());
	      initialNotifyQueue.add((Object)subscriber);
	      if (expires > 0) {
		  subscribers.put((Object)subscriberId, (Object)subscriber);
		  notifier.addSubscriber(subscriberKey,subscriberId, "active");
	      }
	  } else if (responseCode==Response.ACCEPTED) {
	      ProxyDebug.println("   PM.processSubscribe - new subscription, Accepted");
	      subscriber = (Subscriber)createSubscriber(request,subscriberKey,dialog,expires);
	      subscriber.setExpires(expires);
	      subscriber.setSubscriptionState("pending");
	      subscriber.updateNotifyBody(notifier.getOfflineState());
	      if (expires > 0) {
		  subscribers.put((Object)subscriberId, (Object)subscriber);
		  notifier.addSubscriber(subscriberKey,subscriberId, "pending");
	      }
	  } else {
	      ProxyDebug.println("   PM.processSubscribe - new subscription, Not accepted");
	      //this new subscription was declined.
	      return responseCode;
	  }
	  
	      
      }
      
      //New subscriptions should be notified immediately
      initialNotifyQueue.add((Object)subscriber);
      ProxyDebug.println(" PM.processSubscribe finished, added subscriber to NotifyQueue: " + initialNotifyQueue.toString());

      return responseCode;

  }


    /** Sets up a subscription to a resourcelist. A Resourcelist is a subclassed
     *  Subscriber.
     *
     *
     */
    private int processResourceListSubscribe(Request request, Dialog dialog, int expires) {

      ProxyDebug.println("PresenceServer, processResourceListSubscribe");

      String resourceListURI = getKey((Message)request, "To");
      String subscriberKey = getKey((Message)request, "From");
      String subscriberId = dialog.getDialogId();
      int    responseCode;

      //Check if ResourceListSubscriber already exists. In that case
      // just add the new subscriber
      ResourceList resourceList = (ResourceList)subscribers.get(resourceListURI);
      if (resourceList!=null) {
	  //authorize
	  responseCode = resourceList.authorize(subscriberKey,Request.SUBSCRIBE);
	  SubscriberMultipart subscriber = new SubscriberMultipart(subscriberKey,dialog,expires);
	  subscriber.setExpires(expires);
	  subscriber.setSubscriptionState("active");
	  subscriber.updateNotifyBody(resourceList.getNotifyBody());
	  if (expires > 0) {
	      subscribers.put((Object)subscriberId, (Object)subscriber);
	      resourceList.addSubscriber(subscriberKey,subscriberId, "active");
	  }
      } else {
	  //We have to parse the resourcelist and get all Notifiers and start all
	  // VirtualSubscriptions

	  File resourceListFile = new File(resourceListDir,resourceListURI);
	  if(resourceListFile.length()<1) {
	      return Response.NOT_FOUND;
	  }
	  
	  resourceList = 
	      new ResourceList(resourceListURI, null, expires, resourceListFile);
	  SubscriberMultipart subscriber =
	      new SubscriberMultipart(subscriberKey, dialog, expires);
	  subscriber.setRlmiFile(resourceListFile);
	  

	  //Parse file and retrieve a list of all entities
	  Vector entities = 
	      xmlResourceListParser.getNotifiers(resourceListFile);
	  
	  Iterator it = entities.iterator();
	  while (it.hasNext()) {
	      String uri = (String)it.next();
	    
	      //1. Is it a notifier? => notifier.addSubscriber(resourceList);
	      Notifier notifier = (Notifier)notifiers.get(uri);
	      synchronized (notifier) {
		  if (notifier!=null) {
		      subscriber.updateNotifyBody(notifier.getFullState());
		      if (expires>0)
			  //resourcelists have no dialog, URL is used instead 
			  notifier.addSubscriber(resourceList.getSubscriberURL(), 
						 resourceList.getSubscriberURL(), //resourcelists have no dialog, URI is used instead 
						 "active");
		      continue;
		  }
	      }
	      
	      //2. Is it another resourceList?
	      ResourceList parentResourceList = (ResourceList)subscribers.get(uri);
	      synchronized(parentResourceList) {
		  if (parentResourceList != null) {
		      continue;
		  }
	      }

	      //3. Is it external?
	      //4. Are we responsible for it, but it hasn't registered?
	  }
	  responseCode = Response.OK;
      }
	  
      return responseCode;
    }


    /** 
     *  Extra sendNotify for the special case
     *  when the initial notify of a new subscripion is
     *  to be send. This is required since the response
     *  to the subscription must be sent before the Notify
     *  and fetchers and terminated subscriptions are lost
     *  when the processSubscribe method finishes.
     **/
    protected synchronized void sendInitialNotify() {
	ListIterator it = initialNotifyQueue.listIterator();
	while (it.hasNext()) {
	    Subscriber subscriber = (Subscriber)it.next();
	    presenceServer.sendNotifyRequest(subscriber);
	}
    }




    /**
     * Responsible for updating internal Notifier state and updating
     * internal states of it's subscribers.
     * If expires!=0, creates a Notify object else all subscribers
     * are notified and the Notifier object is deleted.<p />
     * TODO <ul>
     *   <li>Add fields for contacts, accept-headers and allow-events in
     *       Notify object and constructor</li>
     *  </ul>
     **/
    public  void processRegister
	(String notifierKey, int expires, String contact) {
      synchronized(notifiers) {
	Notifier notifier = (Notifier)notifiers.get(notifierKey);
	
	if ((notifier != null) && (expires == 0)) {
	    //Remove Notifier contact
	    String tupleId = notifier.removeContact(contact);

	    //Update subscribers
	    //This should be done if the subscribers support Partial presence 
	    /*
	    Collection notifiersSubscriberKeys = notifier.getSubscribers();
	    Iterator it = notifiersSubscriberKeys.iterator();
	    String text = "<removed><t_id>" + tupleId + "</t_id></removed>";
	    while (it.hasNext()) {
		String dialogId = (String)it.next();
		Subscriber subscriber = (Subscriber)subscribers.get(dialogId);
		subscriber.updateNotifyBody(text);
	    }
	    */
	} else if ((notifier == null) && (expires != 0)) {
	    //Create Notifier
	    notifier = new Notifier(notifierKey,expires,contact);
	    notifiers.put(notifierKey, notifier);
	} else if ((notifier != null) && ((expires != 0) || (contact != null))) {
	    //Update Notifier
	    if (contact != null) {
		notifier.addContact(contact);
	    }
	    if (expires != 0) {
		notifier.setExpires(expires);
	    }
	} else {
	    //Trying to update a non-existing Notifier
	}
      }

    }

    /** This method is called for static (uploaded registrations).
    * 
    * No contact address is specified for uploaded registrations.
    */

    protected  void processRegister(String notifierKey, int expires) {
	 synchronized (notifiers) {
	    Notifier notifier = new Notifier(notifierKey,expires, null);
	    notifiers.put(notifierKey, notifier);
	}
     }



    
    /**
     *  Responsible for updating Notifier internal state and also
     *  updating all it's subscribers accordingly.<br>
     *  
     *  Todo: <ul>
     *  <li>Verification</li>
     *  </ul>
     *
     *  See draft-ietf-sip-publish-02, chapter 6.
     *
     * Valid responses are 412 (Precondition failed), 423 (Interval too brief),
     *   403 (Forbidden), 489 (Bad Event), 415 (Unsupported Media Type), 
     *   200 (OK)
     *   The 404 (Not found) response is taken care of by Proxy.
     */
    public synchronized int processPublish(Request request) {
	
	ProxyDebug.println("PresenceServer, processPublish");
      
	String  notifierKey = getKey((Message)request, "To");
	String publisherKey = getKey((Message)request, "From");
	Notifier   notifier = (Notifier)notifiers.get(notifierKey);
	

	int responseCode;

	//Step 1 is performed by Proxy
	//Steps 2-3 are performed by PresentityManager

	//4. Authorize, return 403 if Forbidden
	responseCode = notifier.authorize(publisherKey, "PUBLISH");
	if (responseCode == Response.FORBIDDEN)
	    return responseCode;
	 
	
	//5. Examine event header
	EventHeader eventHeader = (EventHeader)request.getHeader(EventHeader.NAME);
	if ((eventHeader == null) ||
	    !(notifier.hasEvent(eventHeader.getEventType() ))) {
	    return Response.BAD_EVENT;
	}

	//6. Examine If-Match header
	
	Header ifMatchHeader = (Header)request.getHeader("SIP-If-Match");
	String entityTag = new String();
	if(ifMatchHeader != null) {
	    String ifMatchString = ifMatchHeader.toString();
	    int colon = ifMatchString.indexOf(":");
	    entityTag = ifMatchString.substring(colon+1).trim();
	}
	//if(more than one entity-tag) return 400(Invalid request)

	//7. Expiration header
	//   This is ignored for now, published data is used until
	//   updated

	//8. Process body
	if (responseCode==Response.OK) {
	    Object content = request.getContent();
	    String    newBody = null;
	    if (content instanceof String) {
		newBody = (String)content;
	    } else  if (content instanceof byte[] ) {
		newBody = new String((byte[])content);
	    }


	    //get oldBody from notifier
	    String oldBody = notifier.getFullState();

	    if (oldBody.length()>0) {
		//Merge the old and new body.

		//parse old body
		xmlCpimParser.parseCPIMString(oldBody.trim());
		PresenceTag oldPresenceTag=xmlCpimParser.getPresenceTag();
		Vector oldTupleTagList=oldPresenceTag.getTupleTagList();

		//parse new body, generate a list of all id's
		xmlCpimParser.parseCPIMString(newBody.trim());
		PresenceTag newPresenceTag=xmlCpimParser.getPresenceTag();
		if (newPresenceTag==null) {
		    System.out.println
		    ("ERROR: The incoming presence Tag is null!!!");
		    return Response.BAD_REQUEST;
		}
		Vector newTupleTagList=newPresenceTag.getTupleTagList();  

		//Iterate through the old list and update old Tuples
		for(int i=0; i<oldTupleTagList.size(); i++) {
		    TupleTag oldTupleTag = (TupleTag)oldTupleTagList.get(i);
		    String tupleId = oldTupleTag.getId(); 
		    for(int j=0; j<newTupleTagList.size(); j++) {
			TupleTag newTag = (TupleTag)newTupleTagList.get(j);
			if (newTag.getId().equals(tupleId)) {
			    //replace the old tuple with the new
			    oldTupleTagList.setElementAt
			     ((Object)newTupleTagList.remove(j), i);
			    break;
			}
		    }
		}

		//add new tuples
		while(!newTupleTagList.isEmpty()) {
		    TupleTag newTT = (TupleTag)newTupleTagList.remove(0);
		    oldPresenceTag.addTupleTag(newTT);
		}

		newBody = oldPresenceTag.toString();
	    }

	    
	    //Update the notifier
	    notifier.setFullState(newBody);

	    //Update subscribers. Should be partial update someday
	    Collection notifiersSubscriberKeys = notifier.getSubscribers();
	    Iterator it = notifiersSubscriberKeys.iterator();
	    while (it.hasNext()) {
		String dialogId = (String)it.next();
	        if (ProxyDebug.debug)
		    ProxyDebug.println("getting subscriber for " + dialogId );
		Subscriber subscriber = (Subscriber)subscribers.get(dialogId);
		subscriber.updateNotifyBody(newBody);
	    }
	    
	    
	}

	return responseCode;
    }



    /** Processes incoming notify requests from virtual subscribers. 
     *
     */
    public synchronized int processNotify(Request request, Dialog dialog) {
	
	int responseCode;

	//Check event. If not recognized, return 489 BAD_EVENT	
	
	String dialogId = dialog.getDialogId();
	VirtualSubscription virtualSubscription = (VirtualSubscription)virtualSubscriptions.get(dialogId);
	if(virtualSubscription == null)
	    return Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST;
	
	SubscriptionStateHeader subscriptionStateHeader = 
	    (SubscriptionStateHeader)request.getHeader(SubscriptionStateHeader.NAME);
	String subscriptionState = subscriptionStateHeader.getState();

	
	//check if event is understood, else send 489 (Bad Event)
	EventHeader eventHeader = (EventHeader)request.getHeader(EventHeader.NAME);
	if ((eventHeader==null) || !(eventHeader.getEventType().equalsIgnoreCase("presence")))
	    return Response.BAD_EVENT;
	
	
	//update subscriptionState
	String oldState = virtualSubscription.getSubscriptionState();
	virtualSubscription.setSubscriptionState(subscriptionState);
	
	
	//if Subscription-State has expires-parameter, update expirestime
	if (subscriptionStateHeader.getParameter("expires") != null) {
	    int expires = subscriptionStateHeader.getExpires();
	    virtualSubscription.setExpiresTime(expires);
	}
	
	//if body exist, update body
	Object content = request.getContent();
	String    text = null;
	if (content instanceof String) {
	    text = (String)content;
	} else  if (content instanceof byte[] ) {
	    text = new String((byte[])content);
	}
       
	responseCode = virtualSubscription.processNotify(text);
	
	
	//if subscription-state was "terminated", check for retry-parameter and behave accordingly
	
	if (responseCode == Response.OK) {
	    //update vs's subscribers body (a single virtual subscription state cannot effect a whole subscriber)
	    Collection subSubscribers = virtualSubscription.getSubscribers();
	    Iterator it = subSubscribers.iterator();
	    while (it.hasNext()) {
		Subscriber s = (Subscriber)it.next();
		s.updateNotifyBody(text);
	    }
	    
	}
	 
	return responseCode;



    }



    //*****************************
    //   Other Methods
    //*****************************
    

 
    /**
     * @param notifierURI cleaned SipURI as found in a SUBSCRIBE To-header 
     */
    public boolean hasNotifier(String notifierURI) {
	return notifiers.containsKey(notifierURI);
    }
    

    /**
     * @param notifierURI cleaned SipURI as found in a SUBSCRIBE To-header 
     * @param subscriberURI cleaned SipURI as found in a SUBSCRIBE To-header 
     */
    protected synchronized boolean hasSubscriber(String notifierURI, String subscriberURI) {
	Notifier notifier=(Notifier)notifiers.get(notifierURI);
	return notifier.hasSubscriber(subscriberURI);
    }






    //*****************************
    //   UTILITY METHODS
    //*****************************
    

    /** Subscribers come in many flavours. This method determines which Subscriber subclass
     *  should be created and creates it.
     *  @return A type-casted Subscriber object or null if the headers in the subscription were
     *          ambigous or insufficient.
     */
    private Subscriber createSubscriber(Request request, String subscriberKey, Dialog dialog, int expires) {

	
	//Examine event header, this determines the type of subscriber
	EventHeader eventHeader = (EventHeader)request.getHeader(EventHeader.NAME);
	String event;
	if (eventHeader == null) {
	    return null;
	} else {
	    event = eventHeader.getEventType();
	}



	if(event.equalsIgnoreCase("presence")) {

	    boolean partialNotification = false;
	    ListIterator acceptHeadersIterator = (ListIterator)request.getHeaders(AcceptHeader.NAME);

	    while (acceptHeadersIterator.hasNext()) {
		String subtype = ((AcceptHeader)acceptHeadersIterator.next()).getContentSubType();
		
		if (subtype.equalsIgnoreCase("pidf-partial+xml")) {
		    partialNotification = true;
		    break;
		}
	    }
	    
	    return new SubscriberPresence(subscriberKey, dialog, expires, partialNotification);

	} else if (event.equalsIgnoreCase("watcher.presence")) {
	    //get notifier
	    //get winfo file
	    //SubscriberWinfo subscriberWinfo = new SubscriberWinfo(subscriberKey, dialog, expires, winfoFile);
	    return new Subscriber(subscriberKey, dialog, expires);
	}

	
	return null;

    }


    private boolean isResourceListSubscriber(Request request) {
	EventHeader eventHeader = (EventHeader)request.getHeader(EventHeader.NAME);
	String event;
	if ((eventHeader == null) || !(eventHeader.getEventType().equalsIgnoreCase("presence"))) {
	    return false;
	} else {
	    ListIterator supportedHeadersIterator = (ListIterator)request.getHeaders(SupportedHeader.NAME);
	    while (supportedHeadersIterator.hasNext()) {
		String header = ((SupportedHeader)supportedHeadersIterator.next()).toString();
		if (header.indexOf("resourceList")>0) {
		    return true;
		}
	    }
	}
	return false;
    }


    
    /**
     * Returns the value of a named header
     */
    protected  String getKey(Message message,String header) {
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
     */
    protected static URI getCleanUri(URI uri) {
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






    public void run() {
        while (this.isRunning) {
	    try {
		Thread.sleep(1000); //millisecs 
	    } catch (InterruptedException ex) {

	    }
            if (!this.isRunning) return;
	    
	    int expires = 0;
	    synchronized (subscribers) {
			
		Iterator sit = subscribers.values().iterator();
		while(sit.hasNext()) {
		    Subscriber s = (Subscriber)sit.next();

		    if (s.hasExpired()) {
			s.setSubscriptionState("terminated");
			//add reason=timeout parameter
			presenceServer.sendNotifyRequest(s);
			continue;
		    } 

		    if (s.isTerminated()) {
			sit.remove();
			continue;
		    }

		    if (s.sendNotify()) { 
			presenceServer.sendNotifyRequest(s);
			ProxyDebug.println("Thread::Sending notify");
		    }
			    
		    //collect max expiresTime for possible server resubscription
		    int thisExpires = s.getExpiresTime();
		    if (thisExpires > expires) { 
			expires = thisExpires;
		    }
		}
	    }//end subscribers

	    synchronized (notifiers) {
		Iterator nit = notifiers.values().iterator();
		while (nit.hasNext()) {
		    Notifier n = (Notifier)nit.next();
		    if (n.hasExpired()) {
			System.out.println("removing NOTIIFER");
			nit.remove();
		    }
		}
	    } //end notifiers

	    synchronized (virtualSubscriptions) {
		Iterator vit = virtualSubscriptions.values().iterator();
		while (vit.hasNext()) {
		    VirtualSubscription vs = (VirtualSubscription)vit.next();
		    if (vs.hasNoSubscribers()) {
			vit.remove();
		    }
		    if (vs.needToResubscribe()) {
			presenceServer.sendSubscribeRequest(vs.getURI(), 
							    vs.getRequiredExpiresTime(), 
							    vs.getDialog());
		    }
		}
	    } //end virtualSubscriptions


	}
    }



}

/*
 * RequestValidation.java
 *
 * Created on April 10, 2003, 6:31 PM
 */

package gov.nist.sip.proxy;

import java.util.*;
import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;
import java.io.IOException;
import gov.nist.sip.proxy.authentication.*;
import gov.nist.sip.proxy.presenceserver.*;
/**
 *  RFC 3261 16.3 Request Validation:
 *  Before an element can proxy a request, it MUST verify the message's
 *  validity.  A valid message must pass the following checks:
 *    1. Reasonable Syntax: Here, it is done by the stack, so this step is passed!
 *
 *    2. URI scheme: we just support the "sip", "sips", "tel" schemes, for simplicity!!!
 *
      3. Max-Forwards

      4. (Optional) Loop Detection

      5. Proxy-Require: We don't support any option tags!

      6. Proxy-Authorization
 *
 * @author  deruelle
 */
public class RequestValidation {
    
    protected Proxy proxy;
    private boolean VALIDATED;
    
    /** Creates a new instance of RequestValidation */
    public RequestValidation(Proxy proxy) {
        this.proxy=proxy;
        VALIDATED=false;
    }
    
    public boolean validateRequest(SipProvider sipProvider,Request request,ServerTransaction st) {
        try {
           MessageFactory messageFactory = proxy.getMessageFactory();
           PresenceServer presenceServer=proxy.getPresenceServer();
           // Important check: the server transaction can be null! So, in this case,
           // we have to reply an eventual error code statelessly
           ServerTransaction serverTransaction=st;
                
           if ( !checkURIScheme(request) ) {
               // Let's return a 416 Unsupported URI scheme
               Response response=messageFactory.createResponse
               (Response.UNSUPPORTED_URI_SCHEME,request);
               if (serverTransaction!=null)
                    serverTransaction.sendResponse(response);
               else sipProvider.sendResponse(response);
                   
                   
               if (ProxyDebug.debug)
                   ProxyDebug.println(
                   "RequestValidation: 416 UNSUPPORTED_URI_SCHEME replied:\n"+
                   response.toString());
               return false;
           }
           
           if ( !checkMaxForwards(request) ) {
               // Let's return a 483 too many hops
               Response response=messageFactory.createResponse
               (Response.TOO_MANY_HOPS,request);
               if (serverTransaction!=null)
                    serverTransaction.sendResponse(response);
               else sipProvider.sendResponse(response);
                   
               if (ProxyDebug.debug)
                   ProxyDebug.println(
                   "RequestValidation: 483 TOO_MANY_HOPS replied:\n"+
                   response.toString());
               return false;
           }
           
           if ( !checkLoopDetection(request) ) {
                // Let's return a 482 Loop detection
               Response response=messageFactory.createResponse
               (Response.LOOP_DETECTED,request);
               if (serverTransaction!=null)
                    serverTransaction.sendResponse(response);
               else sipProvider.sendResponse(response);
                   
               if (ProxyDebug.debug)
                   ProxyDebug.println(
                   "RequestValidation: 482 LOOP_DETECTED replied:\n"+
                   response.toString());
               return false;
           }
           
           if ( !checkProxyRequire(request) ) {
                // Let's return a 420 Bad Extension
               Response response=messageFactory.createResponse
               (Response.BAD_EXTENSION,request);
               
               // We have to add a Unsupported header listing the Option tags 
               // that we don't support:
               HeaderFactory headerFactory= proxy.getHeaderFactory();
               ProxyRequireHeader prh = (ProxyRequireHeader) request.getHeader
                (ProxyRequireHeader.NAME);
               if (prh != null) {
                    UnsupportedHeader unsupportedHeader=headerFactory.
                    createUnsupportedHeader(prh.getOptionTag());
                    response.setHeader(unsupportedHeader);
               }
               if (serverTransaction!=null)
                    serverTransaction.sendResponse(response);
               else sipProvider.sendResponse(response);
                   
               if (ProxyDebug.debug)
                   ProxyDebug.println(
                   "RequestValidation: 420 BAD_EXTENSION replied:\n"+
                   response.toString());
               return false;
           }
           
           if ( !checkProxyAuthorization(request) ) {
                Authentication authentication=proxy.getAuthentication();
                Response response=authentication.getResponse(request);
                if (response!=null) {
                    if (serverTransaction!=null)
                        serverTransaction.sendResponse(response);
                    else sipProvider.sendResponse(response);
                   
                    ProxyDebug.println(
                    "RequestValidation: 407 PROXY_AUTHENTICATION_REQUIRED replied:\n"+
                    response.toString());
                    
                }
                else {
                    // This is an internal error:
                    // Let's return a 500 SERVER_INTERNAL_ERROR
                    response=messageFactory.createResponse
                    (Response.SERVER_INTERNAL_ERROR,request);
                    if (serverTransaction!=null)
                        serverTransaction.sendResponse(response);
                    else sipProvider.sendResponse(response);
                   
                    if (ProxyDebug.debug)
                        ProxyDebug.println(
                        "RequestValidation: 500 SERVER_INTERNAL_ERROR replied:\n"+
                        response.toString());
                }
                return false;
           }
           
           // Let's add some more important basics checks:
           // - From tag presence.
           
           if ( !checkFromTag(request) ) {
                // Let's return a 400 BAD_REQUEST
               Response response=messageFactory.createResponse
               (Response.BAD_REQUEST,request);
               if (serverTransaction!=null)
                    serverTransaction.sendResponse(response);
               else sipProvider.sendResponse(response);
                   
               if (ProxyDebug.debug)
                   ProxyDebug.println(
                   "RequestValidation: 400 BAD_REQUEST replied:\n"+
                   response.toString());
               return false;
           }
           
           // For Event notifications:
           if (proxy.isPresenceServer()) {
               String method=request.getMethod();
               if (method.equals("SUBSCRIBE") ) {
                   
                   // RFC 3265 3.1.1:
            /*
            Subscribers MUST include exactly one "Event" header in SUBSCRIBE
            requests, indicating to which event or class of events they are
            subscribing.
             */
                   if (!checkEventHeader(request)) {
                       // Let's return a 400 BAD_REQUEST
                       Response response=messageFactory.createResponse
                       (Response.BAD_REQUEST,request);
                       if (serverTransaction!=null)
                           serverTransaction.sendResponse(response);
                       else sipProvider.sendResponse(response);
                       
                       if (ProxyDebug.debug)
                           ProxyDebug.println("RequestValidation: the event"+
                           " header is mandatory, 400 BAD_REQUEST replied:\n"+
                           response.toString());
                       return false;
                   }
                   
                   if ( !presenceServer.hasAuthorization(request) ) {
                       
            // We have to check what kind of 2xx response to send back!!!    
             /* RFC 3265 3.1.4.1:
             This SUBSCRIBE request will be confirmed with a final response.
             200-class responses indicate that the subscription has been accepted,
             and that a NOTIFY will be sent immediately.  A 200 response indicates
             that the subscription has been accepted and that the user is
             authorized to subscribe to the requested resource.  A 202 response
             merely indicates that the subscription has been understood, and that
             authorization may or may not have been granted.
             */
            
                       Response response=messageFactory.createResponse(Response.ACCEPTED,
                       request);
                       if (serverTransaction!=null)
                           serverTransaction.sendResponse(response);
                       else sipProvider.sendResponse(response);
                       if (ProxyDebug.debug)
                           ProxyDebug.println("RequestValidation: validateRequest(),"+
                           " 202 ACCEPTED replied:\n"+response.toString());
                       return false;
                   }
               }
           }
           
           // All the checks are passed:
           return true;
           
        }
        catch(Exception e) {
            if (ProxyDebug.debug) {
                   ProxyDebug.println(
                   "RequestValidation: validateRequest(), exception raised:");
                   ProxyDebug.logException(e);
            }
            return false;
        }
    }
    
    private boolean checkURIScheme(Request request) {
        try{
            URI requestURI=request.getRequestURI();
            String uriScheme=requestURI.getScheme();
            return uriScheme.equals("sip") ||
                   uriScheme.equals("sips") ||
                   uriScheme.equals("tel");        
        }
        catch(Exception e) {
             if (ProxyDebug.debug) {
                   ProxyDebug.println(
                   "RequestValidation: checkURIScheme(), exception raised:");
                   ProxyDebug.logException(e);
             }
             return false;
        }
    }
    
    private boolean checkMaxForwards(Request request) {
        try{
            HeaderFactory headerFactory= proxy.getHeaderFactory();
            MessageFactory messageFactory = proxy.getMessageFactory();
           
            MaxForwardsHeader mf = (MaxForwardsHeader) request.getHeader
                (MaxForwardsHeader.NAME);
            if (mf == null) {
                // We don't add one here!!! We will do it on the cloned request
                // that we are going to forward later.
                return true;
            }
             
            if ( mf.getMaxForwards() == 0) {
                return false;
            } 
            else {
               // We don't decrement here!!! We will do it on the cloned request
               // that we are going to forward later.
                return true;
            }   
        }
        catch(Exception e) {
             if (ProxyDebug.debug) {
                   ProxyDebug.println(
                   "RequestValidation: checkMaxForwards(), exception raised:");
                   ProxyDebug.logException(e);
             }
             return false;
        }
    }
    
     private boolean checkLoopDetection(Request request) {
        try{
            SipStack sipStack=proxy.getSipStack();
            ListIterator viaList=request.getHeaders(ViaHeader.NAME);
            if (viaList==null ) 
               return false;
        
            String stackIPAddress=sipStack.getIPAddress();
           
            while (viaList.hasNext() ) {
                ViaHeader viaHeader=(ViaHeader)viaList.next();
                String host=viaHeader.getHost();
                if (host.equals(stackIPAddress)) {
                //    ProxyDebug.println("RequestValidation, checkLoopDetection(),"+
                //    " the via address matches the proxy address");
                    Iterator lps=sipStack.getListeningPoints();
                    while (lps!=null && lps.hasNext() ) {
                        ListeningPoint lp = (ListeningPoint)lps.next();
                        int port = lp.getPort();
                        if (viaHeader.getPort()==port) {
                             ProxyDebug.println("RequestValidation, checkLoopDetection(),"+
                    " the via port matches the proxy port");
                             
                             // We have to check the branch-ids...
                             // TO DO.
                             
                            return false;
                        }
                    }
                }
            }
            return true;
            
        }
        catch(Exception e) {
             if (ProxyDebug.debug) {
                   ProxyDebug.println(
                   "RequestValidation: checkLoopDetection(), exception raised:");
                   ProxyDebug.logException(e);
             }
             return false;
        }
    }
     
    private boolean checkProxyRequire(Request request) {
        try{
           
            ProxyRequireHeader prh = (ProxyRequireHeader) request.getHeader
                (ProxyRequireHeader.NAME);
            if (prh == null) return true;
            else {
                // We don't support any option tags. So we reject the request:
                return false;
            }
        }
        catch(Exception e) {
             if (ProxyDebug.debug) {
                   ProxyDebug.println(
                   "RequestValidation: checkProxyRequire(), exception raised:");
                   ProxyDebug.logException(e);
             }
             return false;
        }
    }
    
    private boolean checkProxyAuthorization(Request request) {
        try{
            Configuration configuration=proxy.getConfiguration();
            Authentication authentication=proxy.getAuthentication();
            if (configuration.enableAuthentication) {
                 return authentication.isAuthorized(request);
            }
            else return true;
            
        }
        catch(Exception e) {
            if (ProxyDebug.debug) {
                ProxyDebug.println(
                "RequestValidation: checkProxyAuthorization(), exception raised:");
                ProxyDebug.logException(e);
            }
            return false;
        }
    }
      
    private boolean checkFromTag(Request request) {
        try{
            if (request.getMethod().equals("REGISTER")) return true; 
            FromHeader fh = (FromHeader) request.getHeader
                (FromHeader.NAME);
            return (fh.getTag() != null);
            
        }
        catch(Exception e) {
             if (ProxyDebug.debug) {
                   ProxyDebug.println(
                   "RequestValidation: checkFromTag(), exception raised:");
                   ProxyDebug.logException(e);
             }
             return false;
        }
    }
    
    private boolean checkEventHeader(Request request) {
        try{
            EventHeader eventHeader=(EventHeader)request.getHeader(
            EventHeader.NAME);
           // return eventHeader!=null;
            // For Microsoft interoperability:
            return true;
        }
        catch(Exception e) {
            if (ProxyDebug.debug) {
                ProxyDebug.println(
                "RequestValidation: checkEventHeader(), exception raised:");
                ProxyDebug.logException(e);
            }
            return false;
        }
    }

}

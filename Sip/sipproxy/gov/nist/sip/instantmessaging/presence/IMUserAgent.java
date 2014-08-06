/*
 * InstantMessagingUserAgent.java
 *
 * Created on July 28, 2002, 8:23 AM
 */

package gov.nist.sip.instantmessaging.presence;

import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import java.util.*;
import java.io.*;
import gov.nist.sip.instantmessaging.*;
import gov.nist.sip.instantmessaging.authentication.* ;
/**
 *
 * @author  olivier
 * @version 1.0
 */
public class IMUserAgent implements SipListener {

    private  SipStack sipStack;
    private  MessageFactory messageFactory;
    private  AddressFactory addressFactory;
    private  HeaderFactory headerFactory;
    private  SipProvider sipProvider;
    
    private InstantMessagingGUI imGUI;
  
    private IMAckProcessing imAckProcessing;
    private IMRegisterProcessing imRegisterProcessing;
    private IMByeProcessing imByeProcessing;
    private IMSubscribeProcessing imSubscribeProcessing;
    private IMNotifyProcessing imNotifyProcessing;
    private IMInfoProcessing imInfoProcessing;
    private IMMessageProcessing imMessageProcessing;
    
    private ProxyAuthorizationHeader proxyAuthHeader;
    private PresenceManager presenceManager;

    private IMPublishProcessing imPublishProcessing;
    
    /** Creates new InstantMessagingUserAgent */
    public IMUserAgent(InstantMessagingGUI imGUI) {
        this.imGUI=imGUI;
      
        imAckProcessing=new IMAckProcessing(this);
        imRegisterProcessing=new IMRegisterProcessing(this);
        imByeProcessing=new IMByeProcessing(this);
        imSubscribeProcessing=new IMSubscribeProcessing(this);
        imNotifyProcessing=new IMNotifyProcessing(this);
        imInfoProcessing=new IMInfoProcessing(this);
        imMessageProcessing=new IMMessageProcessing(this);

        imPublishProcessing=new IMPublishProcessing(this);

        presenceManager=new PresenceManager(this);
        proxyAuthHeader=null;
    }
    
 
    public PresenceManager getPresenceManager() {
        return presenceManager;
    }
    
    public IMAckProcessing getIMAckProcessing() {
        return imAckProcessing;
    }
    
    public IMRegisterProcessing getIMRegisterProcessing() {
        return imRegisterProcessing;
    }
    
    public IMByeProcessing getIMByeProcessing() {
        return imByeProcessing;
    }
    
    public IMSubscribeProcessing getIMSubscribeProcessing() {
        return imSubscribeProcessing;
    }
    
    public IMNotifyProcessing getIMNotifyProcessing() {
        return imNotifyProcessing;
    }
    
    public IMInfoProcessing getIMInfoProcessing() {
        return imInfoProcessing;
    }
    
    public IMMessageProcessing getIMMessageProcessing() {
        return imMessageProcessing;
    }
    
    public IMPublishProcessing getIMPublishProcessing() {
	return imPublishProcessing;
    }

    public SipStack getSipStack() {
        return sipStack;
    }
    
    public SipProvider getSipProvider() {
        return sipProvider;
    }
    
    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public HeaderFactory getHeaderFactory() {
        return headerFactory;
    }
    
    public AddressFactory getAddressFactory() {
        return addressFactory;
    }
    
    public ProxyAuthorizationHeader getProxyAuthorizationHeader() {
         return proxyAuthHeader;   
    }
    
    
    public String getRouterPath() {
        ListenerInstantMessaging listenerIM=
                        imGUI.getListenerInstantMessaging();
        ConfigurationFrame configurationFrame=listenerIM.getConfigurationFrame();
        String res=configurationFrame.getRouterPath();
        if (res==null || res.trim().equals("") ) {
            return null;
        }
        else return res.trim();
    }
    
     public String getOutputFile() {
        ListenerInstantMessaging listenerIM=
                        imGUI.getListenerInstantMessaging();
        ConfigurationFrame configurationFrame=listenerIM.getConfigurationFrame();
        String res=configurationFrame.getOutputFile();
        if (res==null || res.trim().equals("") ) {
            return null;
        }
        else return res.trim();
    }
    
    public String getProxyAddress() {
        ListenerInstantMessaging listenerIM=
                        imGUI.getListenerInstantMessaging();
        ConfigurationFrame configurationFrame=listenerIM.getConfigurationFrame();
        String res=configurationFrame.getOutboundProxyAddress();
         
        if (res==null || res.trim().equals("") ) {
         
            return null;
        }
        else return res.trim();
    }
    
    public int getProxyPort() {
         ListenerInstantMessaging listenerIM=
                        imGUI.getListenerInstantMessaging();
        ConfigurationFrame configurationFrame=listenerIM.getConfigurationFrame();
        String res=configurationFrame.getOutboundProxyPort();
        
        if (res==null || res.trim().equals("") ) {
            return -1;
        }
        else {
            try {
                int i=Integer.valueOf(res).intValue();
                return i;
            }
            catch(Exception e) {
                return -1;
            }
        }
    }
    
    public String getRegistrarAddress() {
         ListenerInstantMessaging listenerIM=
                        imGUI.getListenerInstantMessaging();
        ConfigurationFrame configurationFrame=listenerIM.getConfigurationFrame();
        String res=configurationFrame.getRegistrarAddress();
        if (res==null || res.trim().equals("") ) {
            return null;
        }
        else return res.trim();
    }
    
    public int getRegistrarPort() {
        ListenerInstantMessaging listenerIM=
                        imGUI.getListenerInstantMessaging();
        ConfigurationFrame configurationFrame=listenerIM.getConfigurationFrame();
         String res=configurationFrame.getRegistrarPort();
        if (res==null || res.trim().equals("") ) {
            return -1;
        }
        else {
            try {
                int i=Integer.valueOf(res).intValue();
                return i;
            }
            catch(Exception e) {
                return -1;
            }
        }
    }
    
    public String getIMAddress() {
        ListenerInstantMessaging listenerIM=
                        imGUI.getListenerInstantMessaging();
        ConfigurationFrame configurationFrame=listenerIM.getConfigurationFrame();
        String res=configurationFrame.getIMAddress();
       
        if (res==null || res.trim().equals("") ) {
            return null;
        }
        else return res.trim();
    }
    
     public int getIMPort() {
        ListenerInstantMessaging listenerIM=
                        imGUI.getListenerInstantMessaging();
        ConfigurationFrame configurationFrame=listenerIM.getConfigurationFrame();
        String res=configurationFrame.getIMPort();
      
        if (res==null || res.trim().equals("") ) {
            return -1;
        }
        else {
            try {
                int i=Integer.valueOf(res).intValue();
                return i;
            }
            catch(Exception e) {
                return -1;
            }
        }
    }
    
    public String getIMProtocol() {
       ListenerInstantMessaging listenerIM=
                        imGUI.getListenerInstantMessaging();
       ConfigurationFrame configurationFrame=listenerIM.getConfigurationFrame();
       String res=configurationFrame.getIMProtocol();
       
        if (res==null || res.trim().equals("") ) {
             return null;
        }
        else return res.trim();
    }
   
    public InstantMessagingGUI getInstantMessagingGUI() {
        return imGUI;
    }
    
/*******************************************************************************/
/************ The methods for implementing the listener            *************/    
/*******************************************************************************/        
    
    public void processRequest(RequestEvent requestEvent) {
        try {
	
            Request request = requestEvent.getRequest();
	 
            Request requestCloned=(Request)request.clone();
            ServerTransaction serverTransaction =
            requestEvent.getServerTransaction();
            sipProvider = (SipProvider) requestEvent.getSource();
            
            System.out.println
            ("\n\nRequest " + request.getMethod()+" received:\n" );
            
            
            if (serverTransaction == null)
                serverTransaction =
                sipProvider.getNewServerTransaction(request);
            
            
            if (request.getMethod().equals(Request.ACK)) {
                imAckProcessing.processAck(requestCloned, serverTransaction);
            }
            else if (request.getMethod().equals(Request.BYE)) {
                imByeProcessing.processBye(requestCloned,serverTransaction);
            }
            else if (request.getMethod().equals("MESSAGE")) {
                imMessageProcessing.processMessage(requestCloned,
			serverTransaction);
            }
            else if (request.getMethod().equals("INFO")) {
                imInfoProcessing.processInfo(requestCloned,serverTransaction);
            }
            else if (request.getMethod().equals("SUBSCRIBE")) {
		imSubscribeProcessing.processSubscribe
		    (requestCloned,serverTransaction);
            }
            else if (request.getMethod().equals("NOTIFY")) {
                imNotifyProcessing.processNotify(requestCloned,serverTransaction);
            }
	   
            else {
                DebugIM.println("processRequest: 405 Method Not Allowed replied");
                
                Response response=messageFactory.createResponse
				(Response.METHOD_NOT_ALLOWED,request);
                serverTransaction.sendResponse(response);
            }
        } catch (Exception ex) {
            DebugIM.println("Unable to process the request:");
            ex.printStackTrace();
        }
    }
    
    public void  processResponse(ResponseEvent responseEvent) {
	Response response = responseEvent.getResponse();
	System.out.println("@@@ IMua processing response: " + response.toString());
	ClientTransaction clientTransaction = 
	    responseEvent.getClientTransaction();
	
	try{
              DebugIM.println("\n\nResponse " + response.getStatusCode() + " "+
              response.getReasonPhrase()+" :\n" + response );
              
              Response responseCloned=(Response)response.clone();
              CSeqHeader cseqHeader=(CSeqHeader)responseCloned.getHeader(CSeqHeader.NAME);
              if ( response.getStatusCode()==Response.OK || 
                   response.getStatusCode()==202 ){
                     if (cseqHeader.getMethod().equals("REGISTER") ) {
                         imRegisterProcessing.processOK(responseCloned,clientTransaction);
                     }
                     if (cseqHeader.getMethod().equals("MESSAGE") ) {
                         imMessageProcessing.processOK(responseCloned,clientTransaction);
                     }
                     if (cseqHeader.getMethod().equals("BYE") ) {
                         imByeProcessing.processOK(responseCloned,clientTransaction);
                     }
                     if (cseqHeader.getMethod().equals("SUBSCRIBE") ) {
                         imSubscribeProcessing.processOK(responseCloned,clientTransaction);
                     }
		     //Henrik Leion added NOTIFY processing
		     if (cseqHeader.getMethod().equals("NOTIFY") ) {
                         imNotifyProcessing.processOk(responseCloned,clientTransaction);
		     }
              }
              else 
		  if ( response.getStatusCode()==Response.NOT_FOUND ||
		       response.getStatusCode()==Response.TEMPORARILY_UNAVAILABLE 
		       ){
		      if (cseqHeader.getMethod().equals("SUBSCRIBE") ) {
			  new AlertInstantMessaging("The presence server is not aware "+
						    "of the buddy you want to add.");
		      }
		      else {
			  ListenerInstantMessaging listenerInstantMessaging=
			      imGUI.getListenerInstantMessaging();
			  ChatSessionManager chatSessionManager=listenerInstantMessaging.getChatSessionManager();
			  ChatSession chatSession=null;
			  String toURL=IMUtilities.getKey(response,"To");
			  if (chatSessionManager.hasAlreadyChatSession(toURL)) {
			      chatSession=chatSessionManager.getChatSession(toURL);
			      chatSession.setExitedSession(true,"Contact not found");
			  }
			  new AlertInstantMessaging("Your instant message could not be delivered..."+
						    " The contact is not available!!!");
		      }
		  }
		  else 
		      if ( response.getStatusCode()==Response.DECLINE ||
			   response.getStatusCode()==Response.FORBIDDEN
			   ){
               
			  String fromURL=IMUtilities.getKey(response,"From");
			  new AlertInstantMessaging("The contact "+fromURL+
						    " has rejected your subscription!!!");
		      }
		      else {
			  if ( response.getStatusCode()==Response.PROXY_AUTHENTICATION_REQUIRED ||
			       response.getStatusCode()==Response.UNAUTHORIZED) {
                            DebugIM.println("IMUserAgent, processResponse(), Credentials to "+
                            " provide!");
                      // WE start the authentication process!!!
                      // Let's get the Request related to this response:
                      Request request=clientTransaction.getRequest();
                      if (request==null) {
                          DebugIM.println("IMUserAgent, processResponse(), the request "+
                          " that caused the 407 has not been retrieved!!! Return cancelled!");
                      }
                      else {
                           Request clonedRequest=(Request)request.clone();
                          // Let's increase the Cseq:
                          cseqHeader=(CSeqHeader)clonedRequest.getHeader(CSeqHeader.NAME);
                          cseqHeader.setSequenceNumber(cseqHeader.getSequenceNumber()+1);
                          
                          // Let's add a Proxy-Authorization header:
                          // We send the informations stored:
                          AuthenticationProcess authenticationProcess=
                          imGUI.getAuthenticationProcess();
                          Header header=authenticationProcess.
                          getHeader(response);
                          
                          if (header==null ) {
                              DebugIM.println("IMUserAgent, processResponse(), Proxy-Authorization "+
                              " header is null, the request is not resent");
                          }
                          else {
                              clonedRequest.setHeader(header);
                              
                              ClientTransaction newClientTransaction=
                              sipProvider.getNewClientTransaction(clonedRequest);
            
                              
                              newClientTransaction.sendRequest();
                              DebugIM.println("IMUserAgent, processResponse(), REGISTER "+
                              "with credentials sent:\n"+clonedRequest);
                              DebugIM.println();
                          }
                      }
                  }
              }
          }
          catch (Exception ex) {
		ex.printStackTrace();
	  }
    }
   
    public void processTimeout(TimeoutEvent timeOutEvent) {
    }
    
    
/************************* Utilities ******************************************/    
   
    
/*
    public static String getBuddyParsedPlusSIP(String buddy) {
        String result=null;
        try{
            if (buddy.startsWith("sip:") ) {
                return buddy;
            }
            else return "sip:"+buddy;
        }
        catch(Exception e )
        {
          return result;  
        }
    }
    
    public static String getBuddyParsedMinusSIP(String buddy) {
        String result=null;
        try{
            if (buddy.startsWith("sip:") ) {
                result=buddy.substring(4);
                return result;
            }
            else return buddy;
        }
        catch(Exception e )
        {
          return result;  
        }
    }
*/
    
/***********************  Methods for initiating,                  ***************
 *       starting and stopping the User agent (Useful to integrate the User agent in other 
 *       applications, like a GUI) 
 *********************************************************************************/
   
    
    /** Start the proxy, this method has to be called after the init method
     * throws Exception that which can be caught by the upper application 
     */
    public void start() throws Exception {
        sipStack = null;
        sipProvider = null;
        
        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();
        
        // Create SipStack object
        Properties properties=new Properties();
        
       
        if  ( getIMAddress()!=null ) {
            properties.setProperty("javax.sip.IP_ADDRESS",getIMAddress());
            DebugIM.println("DebugIM, the stack address is set to: "+getIMAddress());
        }
        else {
            throw new Exception("ERROR, Specify the stack IP Address.");
        }
        
        properties.setProperty("gov.nist.javax.sip.ACCESS_LOG_VIA_RMI","false");
        properties.setProperty("gov.nist.javax.sip.RMI_PORT","0");
        properties.setProperty("gov.nist.javax.sip.LOG_LIFETIME","3600");
        properties.setProperty("gov.nist.javax.sip.BAD_MESSAGE_LOG",
        "./debug/bad_im_message_log.txt");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
        "./debug/debug_im_log.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
        "./debug/server_im_log.txt");
        
        
        properties.setProperty("javax.sip.STACK_NAME","nist-sip-im-client");
       
        if ( getProxyAddress()!=null && getProxyPort()!=-1 &&
             getIMProtocol()!=null ) {
            properties.setProperty("javax.sip.OUTBOUND_PROXY",
            getProxyAddress()+":"+getProxyPort()+"/"+getIMProtocol());
            DebugIM.println("DebugIM, the outbound proxy is set to: "+
            getProxyAddress()+":"+getProxyPort()+"/"+getIMProtocol());
        }
        else DebugIM.println("WARNING, the outbound proxy is not set!");
        
        if (getRouterPath()!=null)
            properties.setProperty("javax.sip.ROUTER_PATH",
            getRouterPath());
        else DebugIM.println("WARNING, the router class is not set!");
        
	// tell the stack to create a dialog when message comes in.
	// this is a hack 
	properties.setProperty("javax.sip.EXTENSION_METHODS",Request.MESSAGE);
        sipStack = sipFactory.createSipStack(properties);
        
        // We create the Listening points:
        if (getIMPort()==-1) throw new Exception("ERROR, the stack port is not set");
        if (getIMProtocol()==null) throw new Exception("ERROR, the stack transport is not set");
        
        ListeningPoint lp=sipStack.createListeningPoint(getIMPort(),getIMProtocol());
        DebugIM.println("DebugIM, one listening point created: port:"+lp.getPort()+", "+
        " transport:"+lp.getTransport());
        sipProvider = sipStack.createSipProvider(lp);
        sipProvider.addSipListener( this );
        
        DebugIM.println("DebugIM, Instant Messaging user agent ready to work");
    }
    
    /** Stop the User agent, this method has to be called after the start method
     * throws Exception that which can be caught by the upper application 
     */
    public void stop() throws Exception{
         if (sipStack==null) {
             DebugIM.println("IM user agent has not been started, so nothing to stop!");
             return;
         }
         Iterator listeningPoints=sipStack.getListeningPoints();
         if (listeningPoints!=null) {
                while( listeningPoints.hasNext()) {
                    ListeningPoint lp=(ListeningPoint)listeningPoints.next();
                    sipStack.deleteListeningPoint(lp);
                    DebugIM.println("One listening point removed!");
                }
                 DebugIM.println("IM user agent stopped");
         }
         else  {
             DebugIM.println("IM user agent has not been started, so nothing to stop!");
         }
    }
    
    
    
    
    
    
/*******************************************************************************/
/************ The main method: to launch the proxy                  *************/    
/*******************************************************************************/        
    
    
    public static void main(String args[]) {
        try{
            // the InstantMessagingUserAgent:
            IMUserAgent instantMessagingUserAgent=
            new IMUserAgent(null);
            
            instantMessagingUserAgent.start();       
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}



/*
 * IMMessageProcessing.java
 *
 * Created on September 26, 2002, 12:04 AM
 */

package gov.nist.sip.instantmessaging.presence;

import gov.nist.javax.sip.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import java.util.*;
import gov.nist.sip.instantmessaging.*;
/**
 *
 * @author  olivier
 * @version 1.0
 */
public class IMMessageProcessing {
    
    private IMUserAgent imUA;
    private int callIdCounter;
   
    /** Creates new IMMessageProcessing */
    public IMMessageProcessing( IMUserAgent imUA) {
        this.imUA=imUA;
        callIdCounter=0;
    }
    
    public void processOK(Response responseCloned,ClientTransaction clientTransaction) {
         DebugIM.println("Processing OK for MESSAGE in progress...");
	
	if (clientTransaction == null) {
	     // This could occur if this is a retransmission of the OK.
             DebugIM.println("ERROR, IMProcessing, processOK(), the transaction is null");    
	     return;
	 }
		
            
         InstantMessagingGUI instantMessagingGUI=imUA.getInstantMessagingGUI();
         ListenerInstantMessaging listenerInstantMessaging=
         instantMessagingGUI.getListenerInstantMessaging();
         ChatSessionManager chatSessionManager=listenerInstantMessaging.getChatSessionManager();
         ChatSession chatSession=null;
         
         Dialog dialog=clientTransaction.getDialog();
         if (dialog==null) {
             DebugIM.println("ERROR, IMProcessing, processOK(), the dialog is null");    
             return;
         }
         
         String fromURL=IMUtilities.getKey(responseCloned,"To");
         if (chatSessionManager.hasAlreadyChatSession(fromURL)) {
             chatSession=chatSessionManager.getChatSession(fromURL);
             chatSession.displayLocalText();
             // WE remove the text typed:
             chatSession.removeSentText();
             
             
             if ( chatSession.isEstablishedSession() ) {
                }
             else {
                 DebugIM.println("DEBUG, IMMessageProcessing, we mark the "+
                 " session established");
                 chatSession.setDialog(dialog);
                 chatSession.setEstablishedSession(true);
             }
         }
         else {
             // This is a bug!!!
             DebugIM.println("Error: IMMessageProcessing, processOK(), the chatSession is null");
         }
         DebugIM.println("Processing OK for MESSAGE completed...");
            
    }
    


     public void processMessage(Request request,ServerTransaction serverTransaction) {
        try{
            SipProvider sipProvider=imUA.getSipProvider();
            MessageFactory messageFactory=imUA.getMessageFactory();
            HeaderFactory headerFactory=imUA.getHeaderFactory();
            AddressFactory addressFactory=imUA.getAddressFactory();
            
            InstantMessagingGUI instantMessagingGUI=imUA.getInstantMessagingGUI();
            ListenerInstantMessaging listenerInstantMessaging=
            instantMessagingGUI.getListenerInstantMessaging();
            ChatSessionManager chatSessionManager=listenerInstantMessaging.getChatSessionManager();
            ChatSession chatSession=null;
            String fromURL=IMUtilities.getKey(request,"From");
            if (chatSessionManager.hasAlreadyChatSession(fromURL))
                chatSession=chatSessionManager.getChatSession(fromURL);
            else chatSession=chatSessionManager.createChatSession(fromURL);
            
            DebugIM.println("IMMessageProcessing, processMEssage(), ChatSession:"+chatSession);
            DebugIM.println("Processing MESSAGE in progress...");
            
            // Send an OK
            Response response=messageFactory.createResponse
            (Response.OK,request);
             // Contact header:
            SipURI sipURI=addressFactory.createSipURI(null,imUA.getIMAddress());
            sipURI.setPort(imUA.getIMPort());
            sipURI.setTransportParam(imUA.getIMProtocol());
            Address contactAddress=addressFactory.createAddress(sipURI);
            ContactHeader contactHeader=headerFactory.createContactHeader(contactAddress);
            response.setHeader(contactHeader);
            ToHeader toHeader=(ToHeader)response.getHeader(ToHeader.NAME);
            if (toHeader.getTag()==null) {
                 // It is the first message without a TO tag
                toHeader.setTag(new Integer((int)(Math.random() * 10000)).toString());
            }
            
            if ( chatSession.isEstablishedSession() ) {
                DebugIM.println("The Session already exists");
                serverTransaction.sendResponse(response);
                DebugIM.println("OK replied to the MESSAGE:\n"+response.toString());
            }
            else {
                 DebugIM.println("The Session does not exists yet. ");
                serverTransaction.sendResponse(response);
                DebugIM.println("OK replied to the MESSAGE:\n"+response.toString());
                
               
           
                Dialog dialog=serverTransaction.getDialog();
                if (dialog==null) {
                    DebugIM.println("ERROR, IMProcessing, processMessage(), the dialog is null");    
                    return;
                }
                 // We need to store the dialog:
                chatSession.setDialog(dialog);
                chatSession.setEstablishedSession(true);
                DebugIM.println("The DIALOG object has been stored in the ChatSession");
            }
            
            Object content=request.getContent();
            String text=null;
            if (content instanceof String)
                text=(String)content;
            else
                if (content instanceof byte[] ) {
                    text=new String(  (byte[])content  );
                }
                else {
                }
            if (text!=null){
                chatSession.displayRemoteText(text);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    public void sendMessage(String localSipURL,String remoteSipURL,String text,
        ChatSession chatSession) {
        try {
            DebugIM.println("IMMessageProcessing, ChatSession:"+chatSession);
            DebugIM.println("Sending a MESSAGE in progress to "+remoteSipURL);
  
            SipProvider sipProvider=imUA.getSipProvider();
            MessageFactory messageFactory=imUA.getMessageFactory();
            HeaderFactory headerFactory=imUA.getHeaderFactory();
            AddressFactory addressFactory=imUA.getAddressFactory();
            
            
            String proxyAddress=imUA.getProxyAddress();
            SipURI requestURI=null;
            if (proxyAddress!=null) {
                requestURI=addressFactory.createSipURI(null,proxyAddress);
                requestURI.setPort(imUA.getProxyPort());
                requestURI.setTransportParam(imUA.getIMProtocol());
            }
            else {
                requestURI=(SipURI)addressFactory.createURI(remoteSipURL);
                requestURI.setTransportParam(imUA.getIMProtocol());
            }
            
            // Call-Id:
            CallIdHeader callIdHeader=null;
            
            // CSeq:
            CSeqHeader cseqHeader=null;
            
            // To header:
            ToHeader toHeader=null;
            
            // From Header:
            FromHeader fromHeader=null;
            
            //  Via header
            String branchId=Utils.generateBranchId();
            ViaHeader viaHeader=headerFactory.createViaHeader(
                imUA.getIMAddress(),imUA.getIMPort(),imUA.getIMProtocol(),branchId);
            Vector viaList=new Vector();
            viaList.addElement(viaHeader);
            
            // MaxForwards header:
            MaxForwardsHeader maxForwardsHeader=headerFactory.createMaxForwardsHeader(70);
            Dialog dialog=chatSession.getDialog();
            if ( chatSession.isEstablishedSession() ) {
                DebugIM.println("DEBUG, IMMessageProcessing, sendMessage(), we get"+
                " the DIALOG from the ChatSession");
              
                
                
                Address localAddress=dialog.getLocalParty();
                Address remoteAddress=dialog.getRemoteParty();  
              //  if (dialog.isServer()) {
                    // We received the first MESSAGE
                fromHeader=headerFactory.createFromHeader(localAddress,dialog.getLocalTag());
                toHeader=headerFactory.createToHeader(remoteAddress,dialog.getRemoteTag());
              //  }
              //  else {
                    
             //   }
                int cseq=dialog.getLocalSequenceNumber();
                DebugIM.println("the cseq number got from the dialog:"+cseq);
                cseqHeader=headerFactory.createCSeqHeader(cseq,"MESSAGE");
                
                callIdHeader=dialog.getCallId();
               
            }
            else {
                DebugIM.println("DEBUG, IMMessageProcessing, sendMessage(), the "+
                " session has not been established yet! We create the first message");
                
                // To header:
                Address toAddress=addressFactory.createAddress(remoteSipURL);
                
                // From Header:
                Address fromAddress=addressFactory.createAddress(localSipURL);
                
                // We have to initiate the dialog: means to create the From tag
                String localTag=Utils.generateTag();
                fromHeader=headerFactory.createFromHeader(fromAddress,localTag);
                toHeader=headerFactory.createToHeader(toAddress,null);
                  
                // CSeq:
                cseqHeader=headerFactory.createCSeqHeader(1,"MESSAGE");
            
                // Call-ID:
                callIdCounter++;
                callIdHeader=(CallIdHeader)headerFactory.createCallIdHeader(
                "nist-sip-im-message-callId"+callIdCounter);
            }
            
            // Content-Type:
            ContentTypeHeader contentTypeHeader=headerFactory.createContentTypeHeader(
            "text","plain");
            contentTypeHeader.setParameter("charset","UTF-8");
            
            Request request=messageFactory.createRequest(requestURI,"MESSAGE",
            callIdHeader,cseqHeader,fromHeader,toHeader,viaList,maxForwardsHeader
            ,contentTypeHeader,text);
           
            // Contact header:
            SipURI sipURI=addressFactory.createSipURI(null,imUA.getIMAddress());
            sipURI.setPort(imUA.getIMPort());
            sipURI.setTransportParam(imUA.getIMProtocol());
            Address contactAddress=addressFactory.createAddress(sipURI);
            ContactHeader contactHeader=headerFactory.createContactHeader(contactAddress);
            request.setHeader(contactHeader);
            
            // ProxyAuthorization header if not null:
            ProxyAuthorizationHeader proxyAuthHeader=imUA.getProxyAuthorizationHeader();
            if (proxyAuthHeader!=null) 
                request.setHeader(proxyAuthHeader);
            
            ClientTransaction clientTransaction=sipProvider.getNewClientTransaction(request);
           
            if ( chatSession.isEstablishedSession() ) {
                dialog.sendRequest(clientTransaction);
                  DebugIM.println("IMessageProcessing, sendMessage(), MESSAGE sent"+
                " using the dialog:\n"+request);
            }
            else{
                clientTransaction.sendRequest();
                DebugIM.println("IMessageProcessing, sendMessage(), MESSAGE sent"+
                " using a new client transaction:\n"+request);
            }
            
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

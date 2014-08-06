/*
 * IMNotifyProcessing.java
 *
 * Created on September 26, 2002, 12:14 AM
 */

package gov.nist.sip.instantmessaging.presence;

import gov.nist.javax.sip.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import java.util.*;
import gov.nist.sip.instantmessaging.*;
import gov.nist.sip.instantmessaging.presence.pidfparser.*;
import gov.nist.sip.instantmessaging.presence.cpimparser.*;
/**
 *
 * @author  olivier
 * @version 1.0
 */
public class IMNotifyProcessing {

     private IMUserAgent imUA;
     private int cseq;
     public XMLpidfParser xmlPidfParser;
     public XMLcpimParser xmlCpimParser;
    
    /** Creates new IMNotifyProcessing */
    public IMNotifyProcessing( IMUserAgent imUA) {
        this.imUA=imUA;
        xmlPidfParser=new XMLpidfParser();
        xmlCpimParser=new XMLcpimParser();
        cseq=0;
    }

   
    public void processOk(Response response,ClientTransaction clientTransaction) {
	DebugIM.println("Processing OK received for a NOTIFY ");
	
        // We have to particular processing to do with the OK..
        DebugIM.println("OK processed!!!");
    }
    
    
    public void processNotify(Request request,ServerTransaction serverTransaction) {
        try {
            InstantMessagingGUI imGUI=imUA.getInstantMessagingGUI();
            ListenerInstantMessaging listenerIM=
            imGUI.getListenerInstantMessaging();
            MessageFactory messageFactory=imUA.getMessageFactory();
            SipProvider sipProvider=imUA.getSipProvider();
            
            DebugIM.println("Process NOTIFY in progress...");
            String fromURL=IMUtilities.getKey(request,"From");
            ExpiresHeader expiresHeader=(ExpiresHeader)request.getHeader(ExpiresHeader.NAME);
            SubscriptionStateHeader subscriptionStateHeader = 
		(SubscriptionStateHeader)request.getHeader(SubscriptionStateHeader.NAME);
            PresenceManager presenceManager=imUA.getPresenceManager();
            /*
            Presentity presentity=presenceManager.getPresentity(fromURL);
            Dialog dialog=presentity.getDialog();
            if (dialog==null) {
                 DebugIM.println("ERROR, processNotify(), PB to retrieve the dialog");
                 return;
            }
            */
            

	    //According to RFC3265 the ExpiresHeader should not exist in a NOTIFY
	    // so it should go. Leaving it for backwards compability reasons for now
            if ((expiresHeader!=null && expiresHeader.getExpires()==0) ||
		(subscriptionStateHeader!=null && 
		 subscriptionStateHeader.getState().equalsIgnoreCase(SubscriptionStateHeader.TERMINATED)))
	    { 
	    
                DebugIM.println("DEBUG, IMNotifyProcessing, processNotify(), "+
                " this is the NOTIFY related to the \"unSUBSCRIBE\"...");
                // We have to update the buddy list!!!
                    BuddyList buddyList=imGUI.getBuddyList();
                    buddyList.changeBuddyStatus(fromURL,"offline");
                    
                    // We can update the information field for the ChatFrame:
                    ChatSessionManager chatSessionManager=listenerIM.getChatSessionManager();
                    ChatSession chatSession=chatSessionManager.getChatSession(fromURL);
                    if (chatSession==null) {
                             DebugIM.println("DEBUG, IMNotifyProcessing, processNotify(), "+
                    " the chat session does not exist, no need to update the chatFrame!!!");
                    }
                    else { 
                            DebugIM.println("DEBUG, IMNotifyProcessing, processNotify(), "+
                    " the chat session does exist,  need to update the chatFrame!!!");
                            
                            chatSession.setInfo("The contact is offline");
                    }
                    
                    // Send an OK
                    Response response=messageFactory.createResponse
				(Response.OK,request);
                    serverTransaction.sendResponse(response);
                    DebugIM.println("OK replied to the NOTIFY");
                                      
                    // WE have to update the presentity list: the status has changed!!!
                   
                    presenceManager.updatePresentity(fromURL,"offline");
                    
                    // Very important: we need to subscribe again!!!
                    // Henrik: don't understand... if the user has removed the presentity, 
		    //    why should it be resubscribed to? And without updating GUI?
		    //IMSubscribeProcessing imSubscribeProcessing=imUA.getIMSubscribeProcessing();
                    //imSubscribeProcessing.sendSubscribe(listenerIM.getLocalSipURL(),fromURL,false);
            }
            else {
                
		
                Object content=request.getContent();
                String text=null;
                if (content instanceof String) {
                    text=(String)content;
                } else  if (content instanceof byte[] ) {
		    text=new String(  (byte[])content  );
		} else {
		    DebugIM.println("DEBUG, IMNotifyProcessing, process(), "+
			       " Error, the body of the request is unknown!!");
		    DebugIM.println("ERROR, IMNotifyProcessing, process(): "+
			       " pb with the xml body, 488 Not Acceptable Here replied");
		    Response response=messageFactory.createResponse
			(Response.NOT_ACCEPTABLE,request);
		    serverTransaction.sendResponse(response);
		    return;
		}
                    
                if (text!=null && !text.trim().equals("") ) {
                    // we have to parse the XML body!!!!
                    try{
                        ContentTypeHeader contentTypeHeader=(ContentTypeHeader)
                        request.getHeader(ContentTypeHeader.NAME);
                        String xmlType=contentTypeHeader.getContentSubType(); 
                        
                        DebugIM.println("DEBUG, IMNotifyProcessing, process(), the XML body format"+
                        " is: "+xmlType);
                        String status=null;
                        if (xmlType.equals("xpidf+xml") ) {
                            xmlPidfParser.parsePidfString(text);
                             gov.nist.sip.instantmessaging.presence.pidfparser.PresenceTag
                            presenceTag=xmlPidfParser.getPresenceTag();
                            if (presenceTag==null)
                                 DebugIM.println("ERROR: The presence Tag is null!!!");
                            else
                                DebugIM.println("the parsed body:"+presenceTag.toString());
                            
                            Vector atomTagList=presenceTag.getAtomTagList();
                            AtomTag atomTag=(AtomTag)atomTagList.firstElement();
                            AddressTag addressTag=atomTag.getAddressTag();
                            MSNSubStatusTag msnSubStatusTag=addressTag.getMSNSubStatusTag();
                            status=msnSubStatusTag.getMSNSubStatus();
                        }
                        else 
                            if (xmlType.equals("pidf+xml") ) {
                               
                                xmlCpimParser.parseCPIMString(text.trim());
                                gov.nist.sip.instantmessaging.presence.cpimparser.PresenceTag 
				    presenceTag=xmlCpimParser.getPresenceTag();
                                if (presenceTag==null)
                                    DebugIM.println("ERROR: The presence Tag is null!!!");
                                else
                                    DebugIM.println("the parsed body:"+presenceTag.toString());
                                
                                Vector tupleTagList=presenceTag.getTupleTagList();
				NoteTag noteTag = null;
				if (tupleTagList.size() > 0)  {
                                	TupleTag tupleTag=(TupleTag)tupleTagList.firstElement();
                                 	noteTag=tupleTag.getNoteTag();
				} 
                                if (noteTag!=null)
                                    status=noteTag.getNote();
                                else status="offline";
                                DebugIM.println("status:"+status);
                            }
                        
                        
                        // Send an OK
                        Response response=messageFactory.createResponse
                        (Response.OK,request);
                        serverTransaction.sendResponse(response);
                        DebugIM.println("OK replied to the NOTIFY");
                        
                        
                        // We have to update the buddy list!!!
                        BuddyList buddyList=imGUI.getBuddyList();
                        buddyList.changeBuddyStatus(fromURL,status);
                        
                        // We can update the information field for the ChatFrame:
                        ChatSessionManager chatSessionManager=listenerIM.getChatSessionManager();
                        ChatSession chatSession=chatSessionManager.getChatSession(fromURL);
                        if (chatSession==null) {
                            DebugIM.println("DEBUG, IMNotifyProcessing, processNotify(), "+
                            " the chat session does not exist, no need to update the chatFrame!!!");
                        }
                        else {
                            DebugIM.println("DEBUG, IMNotifyProcessing, processNotify(), "+
                            " the chat session does exist,  need to update the chatFrame!!!");
                            chatSession.setInfo("The contact is "+status);
                            
                        }
                        
                        // WE have to update the presentity list: the status has changed!!!
                        presenceManager.updatePresentity(fromURL,status);
                        
                    }
                    catch(Exception e) {
                        //e.printStackTrace();
                        DebugIM.println("ERROR, IMNotifyProcessing, process(): "+
                        " pb with the xml body, 488 Not Acceptable Here replied");
                        Response response=messageFactory.createResponse
				(Response.NOT_ACCEPTABLE_HERE,request);
                        serverTransaction.sendResponse(response);
                   
                        e.printStackTrace();
                    }
                }
                else {
                    DebugIM.println("DEBUG, IMNotifyProcessing, processNotify(), "+
                    " PB to get the NOTIFY xml body, 488 Not Acceptable Here replied");
                   
                    Response response=messageFactory.createResponse
                        (Response.NOT_ACCEPTABLE_HERE,request);
                    serverTransaction.sendResponse(response);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    public void sendNotifyToAllSubscribers(String status,String subStatus) {
        try{
             // We have to get all our subscribers and send them a NOTIFY!
             PresenceManager presenceManager=imUA.getPresenceManager();
             Vector subscribersList=presenceManager.getAllSubscribers();
             DebugIM.println("DEBUG, IMNotifyProcessing, sendNotifyToAllSuscribers(),"+
             " we have to notify our SUBSCRIBERS: let's send a NOTIFY for each one "+
             "of them (subscribersList: "+subscribersList.size()+")!!!");
             for (int i=0;i<subscribersList.size();i++) {
                Subscriber subscriber=(Subscriber)subscribersList.elementAt(i); 
                
                Response okSent=subscriber.getOkSent();
                String subscriberName=subscriber.getSubscriberName();
                
                String contactAddress= imUA.getIMAddress()+":"+
                                       imUA.getIMPort();
                String xmlBody=null;
                //if (!status.equals("closed") )
                    xmlBody=xmlPidfParser.createXMLBody(status,subStatus,subscriberName,
                    contactAddress);
                
                Dialog dialog=subscriber.getDialog();
                if (dialog==null) {
                    DebugIM.println("ERROR, sendNotifyToAllSubscribers(), PB to "+
                    "retrieve the dialog, NOTIFY not sent!");
                }
                else
                    sendNotify(okSent,xmlBody,dialog);
             }
	     
	     //Send a PUBLISH request to our PA
	     IMRegisterProcessing imRegisterProcessing = imUA.getIMRegisterProcessing();
	     if (imRegisterProcessing.isRegistered()) {
		 //Fetching the sip-uri from gui. Isn't that a bit odd?
		 IMPublishProcessing imPublishProcessing = imUA.getIMPublishProcessing();
		 javax.swing.JTextField guiSipURI = imUA.getInstantMessagingGUI().getLocalSipURLTextField();
		 String localSipURI = guiSipURI.getText();
		 int  colonIndex = localSipURI.indexOf(':');
		 String localURI = localSipURI.substring(colonIndex+1); //strip off "sip:"
		 imPublishProcessing.sendPublish(localURI, subStatus); //"fosfor@nitrogen.epact.se"
	     }

        }
        catch (Exception ex) {
            ex.printStackTrace();
        } 
    }
    

   
    public void sendNotify(Response okSent,String body,Dialog dialog) {
        try{
            // We send the NOTIFY!!!
            
            // we create the Request-URI: the one of the proxy
            HeaderFactory headerFactory=imUA.getHeaderFactory();
            AddressFactory addressFactory=imUA.getAddressFactory();
            MessageFactory messageFactory=imUA.getMessageFactory();
            SipProvider sipProvider=imUA.getSipProvider();
            
            String imProtocol=imUA.getIMProtocol();
            String proxyAddress=imUA.getProxyAddress();
            int proxyPort=imUA.getProxyPort();
            
            SipURI requestURI=null;
            if (proxyAddress!=null) {
                 requestURI=addressFactory.createSipURI(null,proxyAddress);
                 requestURI.setPort(proxyPort);
                 requestURI.setTransportParam(imProtocol);
            }
            else {
                DebugIM.println("DEBUG, IMNotifyProcessing, sendNotify(), request-uri is null");
                return;
            }
            
           
            Address localAddress=dialog.getLocalParty();
            Address remoteAddress=dialog.getRemoteParty();  
                      
            FromHeader fromHeader=headerFactory.createFromHeader(localAddress,dialog.getLocalTag());
            ToHeader toHeader=headerFactory.createToHeader(remoteAddress,dialog.getRemoteTag());
            
            int cseq=dialog.getLocalSequenceNumber();
            CSeqHeader cseqHeader=headerFactory.createCSeqHeader(cseq,"NOTIFY");
                
            CallIdHeader callIdHeader=dialog.getCallId();
            
             //  Via header
            String branchId=Utils.generateBranchId();
            ViaHeader viaHeader=headerFactory.createViaHeader(
                imUA.getIMAddress(),imUA.getIMPort(),imProtocol,branchId);
            Vector viaList=new Vector();
            viaList.addElement(viaHeader);
            
            // MaxForwards header:
            MaxForwardsHeader maxForwardsHeader=headerFactory.createMaxForwardsHeader(70);
            
            Request request=null;
            ClientTransaction clientTransaction=null;
            if (body==null) {
                
                request=messageFactory.createRequest(requestURI,"NOTIFY",
                callIdHeader,cseqHeader,fromHeader,toHeader,viaList,maxForwardsHeader);
               
                
            }
            else {
                body=body+"\r\n";
                // Content-Type:
                ContentTypeHeader contentTypeHeader=headerFactory.createContentTypeHeader(
                "application","xpidf+xml");
                
                request=messageFactory.createRequest(requestURI,"NOTIFY",
                callIdHeader,cseqHeader,fromHeader,toHeader,viaList,maxForwardsHeader
                ,contentTypeHeader,body);
                   
            }
         
            // WE have to add a new Header: "Subscription-State"
	    // Modified by Henrik Leion
            DebugIM.println("DEBUG, IMNotifyProcessing, sendNotify(), We add the Subscription-State"+
			    " header to the request");
	    String subscriptionState;
	    if (body == null)
		subscriptionState = "terminated";
	    else
		subscriptionState = "active";
            Header header=headerFactory.createHeader("Subscription-State",subscriptionState);
            request.setHeader(header);
            

            // WE have to add a new Header: "Event"
            header=headerFactory.createHeader("Event","presence");
            request.setHeader(header);
            
            // ProxyAuthorization header if not null:
            ProxyAuthorizationHeader proxyAuthHeader=imUA.getProxyAuthorizationHeader();
            if (proxyAuthHeader!=null) 
                request.setHeader(proxyAuthHeader);
            
            clientTransaction=sipProvider.getNewClientTransaction(request);
            dialog.sendRequest(clientTransaction);
            
            DebugIM.println("DEBUG, IMNotifyProcessing, sendNotify(),"+
            " NOTIFY sent:\n" );
            DebugIM.println(request.toString());

	    //Added by Henrik Leion
	    //If the Notify ended the dialog (body was null and 
	    //  SubscriptionStateHeader=terminated) the dialog should be deleted
	    if (body==null) {
		dialog.delete();
	    }


        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}

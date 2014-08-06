/*
 * IMInfoProcessing.java
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
/**
 *
 * @author  olivier
 * @version 1.0
 */
public class IMInfoProcessing {

     private IMUserAgent imUA;
    
    /** Creates new IMInfoProcessing */
    public IMInfoProcessing( IMUserAgent imUA ) {
        this.imUA=imUA;
    }

    public void processInfo(Request request,ServerTransaction serverTransaction) {
        try {
            
            DebugIM.println("Process INFO in progress...");
            MessageFactory messageFactory=imUA.getMessageFactory();
            SipProvider sipProvider=imUA.getSipProvider();
            InstantMessagingGUI instantMessagingGUI=imUA.getInstantMessagingGUI();
            ListenerInstantMessaging listenerInstantMessaging=
            instantMessagingGUI.getListenerInstantMessaging();
            ChatSessionManager chatSessionManager=listenerInstantMessaging.getChatSessionManager();
            ChatSession chatSession=null;
            String fromURL=IMUtilities.getKey(request,"From");
            if (chatSessionManager.hasAlreadyChatSession(fromURL)) {
                chatSession=chatSessionManager.getChatSession(fromURL);
                // WE have to parse the XML info body and notify the
                // user by the chatSession and ChatFrame!!
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
                if (text!=null) {
                    //String infoParsed=infoParser.parseXMLInfoBody(text);
                    chatSession.setInfo("Your contact is typing...");
                    InfoTimer infoTimer=new InfoTimer(chatSession);
                    java.util.Timer timer=new java.util.Timer();
                    timer.schedule(infoTimer,2000);
                }  
            }
            else {
                 // Nothing to update!!!
            }
            
            // Send an OK
            Response response=messageFactory.createResponse
                       (Response.OK,request);
            serverTransaction.sendResponse(response);
            DebugIM.println("OK replied to INFO");
           
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}

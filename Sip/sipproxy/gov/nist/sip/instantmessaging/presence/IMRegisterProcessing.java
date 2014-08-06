/*
 * IMRegisterProcessing.java
 *
 * Created on September 26, 2002, 12:14 AM
 */

package gov.nist.sip.instantmessaging.presence;

import gov.nist.javax.sip.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import gov.nist.sip.instantmessaging.*;
/**
 *
 * @author  olivier
 * @version 1.0
 */
public class IMRegisterProcessing {
    
    private boolean signIn;
    protected long callIdCounter;
    protected int cseq;
   
    private IMUserAgent imUA;
    
    /** Creates new IMRegisterProcessing */
    public IMRegisterProcessing( IMUserAgent imUA ) {
        this.imUA=imUA;
        signIn=false;
        callIdCounter=0;
        cseq=0;
        
    }
    
    public boolean isRegistered() {
        return signIn;
    }
    
   
    
    public void processOK(Response responseCloned,ClientTransaction clientTransaction) {
        
        InstantMessagingGUI imGUI=imUA.getInstantMessagingGUI();
        ListenerInstantMessaging listenerIM=imGUI.getListenerInstantMessaging();
        JMenu signMenu=imGUI.getSignMenu();
        JRadioButtonMenuItem offlineJRadioButtonMenuItem=imGUI.getOfflineJRadioButtonMenuItem();
        JRadioButtonMenuItem onlineJRadioButtonMenuItem=imGUI.getOnlineJRadioButtonMenuItem();
        JLabel statusLabel=imGUI.getStatusLabel();
        
        ExpiresHeader expiresHeader=(ExpiresHeader)responseCloned.getHeader(ExpiresHeader.NAME);
        if ( expiresHeader==null || expiresHeader.getExpires()!=0 ) {
            if (!isRegistered()) {
                DebugIM.println("process OK for REGISTER (to signIn) in progress");
                // WE are registered!!!!
                imGUI.setTitle("NIST-SIP Instant Messaging: User registered");
                signMenu.setText("Sign out");
                signMenu.setToolTipText("Sign out to the server");
                
                signIn=true;
                DebugIM.println("processing done: signed In");
                
                // WE have to block all the configurations until we unregister
                imGUI.blockProperties();
                
                // We have to update our status and notice all the buddies!
                statusLabel.setText("You are: ONLINE");
                onlineJRadioButtonMenuItem.setSelected(true);
                listenerIM.setLocalStatus("online");
                
                BuddyList buddyList=imGUI.getBuddyList();
                Vector buddies=buddyList.getBuddies();
                
                // WE have to subscribe to all our buddies!!!
                IMSubscribeProcessing imSubscribeProcessing=imUA.getIMSubscribeProcessing();
                imSubscribeProcessing.sendSubscribeToAllPresentities(buddies,false);
                
                // WE have to notify all our subscribers!!!!
                IMNotifyProcessing imNotifyProcessing=imUA.getIMNotifyProcessing();
                imNotifyProcessing.sendNotifyToAllSubscribers("open","online");
                
                // We have to reopen all our Chat sessions!!!
                ChatSessionManager chatSessionManager=listenerIM.getChatSessionManager();
                //chatSessionManager.reOpenAllActiveSessions();
            }
            else {
                DebugIM.println("Dropping OK retransmission for REGISTER, we are already logged in.");
            }
        }
        else{

            DebugIM.println("process OK for REGISTER (to signOut) in progress");
            // WE are logged out:
            imGUI.setTitle("NIST-SIP Instant Messaging: User unregistered");
            
            
            signMenu.setText("Sign in");
            signMenu.setToolTipText("Sign in to the server");
            
            signIn=false;
            DebugIM.println("processing done: signed Out");
            
             // WE have to block all the configurations until we unregister
            imGUI.unblockProperties();
            
            // WE have to update our status and notice all the buddies!
            statusLabel.setText("You are: OFFLINE");
            
            offlineJRadioButtonMenuItem.setSelected(true);
            listenerIM.setLocalStatus("offline");
           
            // WE have to notify all our subscribers!!!!
            IMNotifyProcessing imNotifyProcessing=imUA.getIMNotifyProcessing();
            imNotifyProcessing.sendNotifyToAllSubscribers("closed","offline");
            
            // WE put all the contacts offline:
            BuddyList buddyList=imGUI.getBuddyList();
            buddyList.changeAllBuddiesStatus("offline");
            //Vector buddies=buddyList.getBuddies();
            // WE have to send unSUBSCRIBE to all our buddies!
            //IMSubscribeProcessing imSubscribeProcessing=imUA.getIMSubscribeProcessing();
            //imSubscribeProcessing.sendSubscribeToAllPresentities(buddies,true);
            
            // We have to close all our Chat sessions!!!
            ChatSessionManager chatSessionManager=listenerIM.getChatSessionManager();
            chatSessionManager.closeAllActiveSessions();
            
        }
    }

    public void signIn(String localSipURL) {
        try {
            DebugIM.println();
            DebugIM.println("process REGISTER (SignIn) in progress for : "+localSipURL);
            String imProtocol=imUA.getIMProtocol();
            SipStack sipStack=imUA.getSipStack();
            SipProvider sipProvider=imUA.getSipProvider();
            MessageFactory messageFactory=imUA.getMessageFactory();
            HeaderFactory headerFactory=imUA.getHeaderFactory();
            AddressFactory addressFactory=imUA.getAddressFactory();
            
            // Request-URI:
	    // Error: RFC 3261, 10.2 states that the Request-URI should 
	    //        be "sip:domain.com" the domain on which we register.
	    int atIndex = localSipURL.indexOf('@');
	    String sipDomain = localSipURL.substring(atIndex+1);
            SipURI requestURI = addressFactory.createSipURI(null,sipDomain);
            requestURI.setPort(imUA.getRegistrarPort());
            
            // Call-ID:
            callIdCounter++;
            CallIdHeader callIdHeader=headerFactory.createCallIdHeader(
            "nist-sip-im-register-callId"+callIdCounter);
            
            // CSeq:
            cseq++;
            CSeqHeader cseqHeader=headerFactory.createCSeqHeader(cseq,"REGISTER");
            
            // To header:
            Address toAddress=addressFactory.createAddress(localSipURL);
            ToHeader toHeader=headerFactory.createToHeader(toAddress,null);
            
            // From Header:
            Address fromAddress=addressFactory.createAddress(localSipURL);
            String fromTag=Utils.generateTag();
            FromHeader fromHeader=headerFactory.createFromHeader(fromAddress,fromTag);
            
            //  Via header
            String branchId=Utils.generateBranchId();
            ViaHeader viaHeader=headerFactory.createViaHeader(
                imUA.getIMAddress(),imUA.getIMPort(),imProtocol,branchId);
            Vector viaList=new Vector();
            viaList.addElement(viaHeader);
            
            // MaxForwards header:
            MaxForwardsHeader maxForwardsHeader=headerFactory.createMaxForwardsHeader(10);
            
            
            Request request=messageFactory.createRequest(requestURI,"REGISTER",
            callIdHeader,cseqHeader,fromHeader,toHeader,viaList,maxForwardsHeader);
            
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
            
	    // Allow header. With PUBLISH, to indicate that we'd like to have an server-sided PA
	    String methods = Request.INVITE+", "+Request.SUBSCRIBE+", "+Request.NOTIFY+", "+
		Request.MESSAGE+", "+Request.INFO+", "+"PUBLISH";
	    AllowHeader allowHeader = headerFactory.createAllowHeader(methods);
	    request.setHeader(allowHeader);

            ClientTransaction clientTransaction=sipProvider.getNewClientTransaction(request);
            
            clientTransaction.sendRequest();
            DebugIM.println("REGISTER sent:\n"+request);
            DebugIM.println();

       }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    public void signOut(String localSipURL) {
        try {
            DebugIM.println();
            DebugIM.println("process REGISTER (SignOut) in progress for "+localSipURL);
            
            String imProtocol=imUA.getIMProtocol();
            SipStack sipStack=imUA.getSipStack();
            SipProvider sipProvider=imUA.getSipProvider();
            MessageFactory messageFactory=imUA.getMessageFactory();
            HeaderFactory headerFactory=imUA.getHeaderFactory();
            AddressFactory addressFactory=imUA.getAddressFactory();
            
            // Request-URI:
            SipURI requestURI=addressFactory.createSipURI(null,imUA.getRegistrarAddress());
            requestURI.setPort(imUA.getRegistrarPort());
            
            // Call-ID:
            callIdCounter++;
            CallIdHeader callIdHeader=headerFactory.createCallIdHeader(
            "nist-sip-im-register-callId"+callIdCounter);
            
            // CSeq:
            cseq++;
            CSeqHeader cseqHeader=headerFactory.createCSeqHeader(cseq,"REGISTER");
            
            // To header:
            Address toAddress=addressFactory.createAddress(localSipURL);
            String toTag=Utils.generateTag();
            ToHeader toHeader=headerFactory.createToHeader(toAddress,toTag);
            
            // From Header:
            Address fromAddress=addressFactory.createAddress(localSipURL);
            String fromTag=Utils.generateTag();
            FromHeader fromHeader=headerFactory.createFromHeader(fromAddress,fromTag);
            
            //  Via header
            String branchId=Utils.generateBranchId();
            ViaHeader viaHeader=headerFactory.createViaHeader(
                imUA.getIMAddress(),imUA.getIMPort(),imProtocol,branchId);
            Vector viaList=new Vector();
            viaList.addElement(viaHeader);
            
            // MaxForwards header:
            MaxForwardsHeader maxForwardsHeader=headerFactory.createMaxForwardsHeader(10);
            
            
            Request request=messageFactory.createRequest(requestURI,"REGISTER",
            callIdHeader,cseqHeader,fromHeader,toHeader,viaList,maxForwardsHeader);
            
            // Contact header:
            SipURI sipURI=addressFactory.createSipURI(null,imUA.getIMAddress());
            sipURI.setPort(imUA.getIMPort());
            sipURI.setTransportParam(imUA.getIMProtocol());
            Address contactAddress=addressFactory.createAddress(sipURI);
            ContactHeader contactHeader=headerFactory.createContactHeader(contactAddress);
            request.setHeader(contactHeader);
           
            ExpiresHeader expiresHeader=headerFactory.createExpiresHeader(0);
            request.setHeader(expiresHeader);
            
            // ProxyAuthorization header if not null:
            ProxyAuthorizationHeader proxyAuthHeader=imUA.getProxyAuthorizationHeader();
            if (proxyAuthHeader!=null) 
                request.setHeader(proxyAuthHeader);
                  
            ClientTransaction clientTransaction=sipProvider.getNewClientTransaction(request);
            
            clientTransaction.sendRequest();
            DebugIM.println("REGISTER sent:\n"+request);
            DebugIM.println();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}

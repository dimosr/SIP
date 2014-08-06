/*
 * ChatSession.java
 *
 * Created on September 25, 2002, 4:30 PM
 */

package gov.nist.sip.instantmessaging;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import gov.nist.sip.instantmessaging.presence.*;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class ChatSession  {

    private ChatFrame chatFrame;
    
    private String textSent;
    
    private javax.sip.Dialog dialog;
    
    private boolean ESTABLISHED_SESSION;
    private boolean EXITED_SESSION;
    
    /** Creates new ChatSession */
    public ChatSession() {
        chatFrame=null;
        
        init();
    }

    public void init() {
        EXITED_SESSION=false;
        ESTABLISHED_SESSION=false;
        dialog=null;
    }
    
    public javax.sip.Dialog getDialog() {
        return dialog;
    }
    
    public void setDialog(javax.sip.Dialog dialog) {
        this.dialog=dialog;
    }
    
    public void setChatFrame(ChatFrame chatFrame) {
        this.chatFrame=chatFrame;
    }
    
    public void setExitedSession(boolean exited,String text) {
        EXITED_SESSION=exited;
        setInfo(text);
    }
    
    public boolean hasExited() {
        return EXITED_SESSION;
    }
    
    public ChatFrame getChatFrame() {
        return chatFrame;
    }
    
    public String getBuddy() {
        return chatFrame.getContact();
    }
    
    public boolean isEstablishedSession() {
        return ESTABLISHED_SESSION;
    }

    public void setInfo(String text) {
        chatFrame.setInfo(text);
    }
    
    public void setEstablishedSession(boolean est) {
         ESTABLISHED_SESSION=est;
    }
    
    public void displayRemoteText(String text) {
        chatFrame.displayRemoteText(text);   
    }
    
    public void displayLocalText() {
        chatFrame.displayLocalText(textSent);   
    }
    
    public void removeSentText() {
        chatFrame.removeSentText();   
    }
    
    public String getRemoteSipURL() {
       return chatFrame.getContact();
    }
    
    
    public void removeWindow() {
        chatFrame.remove();
    }
    
    public void sendIMActionPerformed(ActionEvent evt){
        try{
            //if ( EXITED_SESSION ) {
            //    new AlertInstantMessaging(
            //    "Unable to send Messages, close the window!!!",JOptionPane.ERROR_MESSAGE);
            //}
            //else {
                    // Send a MESSAGE to the proxy
                    // and wait for the OK.
                    InstantMessagingGUI imGUI=chatFrame.getInstantMessagingGUI();
                    ListenerInstantMessaging listenerIM=imGUI.getListenerInstantMessaging();
                    IMUserAgent imUserAgent=imGUI.getInstantMessagingUserAgent();
                    IMMessageProcessing imMessageProcessing=imUserAgent.getIMMessageProcessing();
                    
                    String remoteSipURL=getRemoteSipURL();
                    if (remoteSipURL!=null) {
                        String localSipURL=listenerIM.getLocalSipURL();
                        JTextArea messageTextArea=chatFrame.getMessageTextArea();
                        String text= messageTextArea.getText();
                        if (text==null|| text.trim().equals(""))
                            new AlertInstantMessaging(
                            "You must type something!!!",JOptionPane.ERROR_MESSAGE);
                        else{
                            textSent=text;
                            imMessageProcessing.sendMessage(localSipURL,remoteSipURL,text,this);
                        }
                    }
                    else
                        new AlertInstantMessaging(
                        "You must set a remote url!!!",JOptionPane.ERROR_MESSAGE);
               
          //  }
        }
        catch(Exception e) {
            e.printStackTrace();
        }   
    }
    
    public void sendBye() {
        if ( !EXITED_SESSION ) {
            // send a BYE if we didn't receive one!!
            // cleaning the session!!!
            InstantMessagingGUI imGUI=chatFrame.getInstantMessagingGUI();
            ListenerInstantMessaging listenerIM=imGUI.getListenerInstantMessaging();
            IMUserAgent imUserAgent=imGUI.getInstantMessagingUserAgent();
            IMByeProcessing imByeProcessing=imUserAgent.getIMByeProcessing();  
            
            String localSipURL=listenerIM.getLocalSipURL();
            String remoteSipURL=getRemoteSipURL();
            //imByeProcessing.sendBye(localSipURL,remoteSipURL,this);
        }
    }
   
    
}
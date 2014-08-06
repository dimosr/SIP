/*
 * ListenerIM.java
 *
 * Created on July 28, 2002, 8:14 AM
 */

package gov.nist.sip.instantmessaging;

import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import gov.nist.sip.instantmessaging.presence.*;
import tools.tracesviewer.*;
/**
 *
 * @author  olivier
 * @version 1.0
 */
public class ListenerInstantMessaging {

    protected InstantMessagingGUI imGUI;
    protected ConfigurationFrame configurationFrame;
    protected HelpBox helpBox;
    protected ChatSessionManager chatSessionManager;
    protected Process rmiregistryProcess;
    protected String localStatus;
    protected TracesViewer tracesViewer; 
    
    /** Creates new ListenerIM */
    public ListenerInstantMessaging(InstantMessagingGUI imGUI) {
        this.imGUI=imGUI;
        localStatus="offline";
        configurationFrame=new ConfigurationFrame(imGUI,"Configuration");
        
        helpBox=new HelpBox();
        
        chatSessionManager=new ChatSessionManager(imGUI);
    }
    
    public String getLocalStatus() {
        return localStatus;
    }
    
    public void setLocalStatus(String localStatus) {
        this.localStatus=localStatus;
    }

   
    public ChatSessionManager getChatSessionManager() {
        return chatSessionManager;
    }
    
    public InstantMessagingGUI getInstantMessagingGUI() {
        return imGUI;
    }
    
    public ConfigurationFrame getConfigurationFrame() {
        return configurationFrame;
    }
     
    public void helpMenuMouseEvent(MouseEvent mouseEvent) {
       try{
            helpBox.show();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void buddyListMouseClicked(MouseEvent e){
        if (e.getClickCount() == 2) {
            BuddyList buddyList=imGUI.getBuddyList();
            int index = buddyList.locationToIndex(e.getPoint());
            String buddy=buddyList.getBuddy(index);
            if ( buddy== null || buddy.equals("(empty)") ) {
                // we don't start anything!!!
            }
            else {
                IMUserAgent imUA=imGUI.getInstantMessagingUserAgent();
                IMRegisterProcessing imRegisterProcessing=imUA.getIMRegisterProcessing();
                if (imRegisterProcessing.isRegistered()) {
                    if (chatSessionManager.hasAlreadyChatSession(buddy) ) {
                        // This chat session already exists, we put the focus on it:
                        ChatSession chatSession=chatSessionManager.getChatSession(buddy);
                        ChatFrame chatFrame=chatSession.getChatFrame();
                        chatFrame.show();
                    }
                    else {
                        ChatFrame chatFrame=new ChatFrame(imGUI,buddy);
                        ChatSession chatSession=new ChatSession();
                        
                        chatSession.setChatFrame(chatFrame);
                        chatFrame.setChatSession(chatSession);
                        chatSessionManager.addChatSession(chatSession);
                    }
                }
                else  new AlertInstantMessaging(
                "You must sign in first to the server!!!",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    
    
    public void configurationActionPerformed(ActionEvent evt){
        try{
            configurationFrame.show();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
     public void startRMIregistry() {
         // Launches the rmiregistry:
         try{
             rmiregistryProcess=null;
             Runtime runtime=Runtime.getRuntime();
             
             // The root directory
             File file=new File("../../..");
             String localRootDirectory=file.getAbsolutePath();
             String javaHome= System.getProperty("java.home")+"/bin/";
             String localSeparator= System.getProperty("path.separator");
            
             String rmiregistryClasspath=
                localRootDirectory+"/classes"+localSeparator+
                localRootDirectory+"/lib/antlr/antlrall.jar"+localSeparator+
                localRootDirectory+"/lib/xerces/xerces.jar";
             
             // The command to execute
             String commandLine=javaHome+"rmiregistry -J-Denv.class.path="+rmiregistryClasspath;
            
             DebugIM.println("Starting the rmiregistry:");
             DebugIM.println(commandLine);
             rmiregistryProcess=runtime.exec(commandLine);
             
         }
         catch (Exception e) {
             DebugIM.println("ERROR, starting the rmiregistry, exception raised:");
             e.printStackTrace();
         }    
    }
    
    
    public void tracesViewerActionPerformed(ActionEvent evt){
        try{
           // String javaHome= System.getProperty("java.home")+"/bin/";
            if (tracesViewer!=null) {
                tracesViewer.show();
                return;
            }
                /*
            IMUserAgent imUA=imGUI.getInstantMessagingUserAgent();
            String imAddress=imUA.getIMAddress();
            int imPort=imUA.getIMPort();
            if (imAddress==null ||
                imAddress.trim().equals("") ||
                imPort==-1 )
                 {
                   DebugIM.println("ERROR, ListenerInstantMessaging, tracesViewer"+
                   "ActionPerformed(), you have to set the stack IP address/port"+
                   " in the configuration frame.");
                   return;
            }
            */
            String back="images/back.gif";
            String faces="images/faces.jpg";
            String actors="images/comp.gif";
            String logoNist="images/nistBanner.jpg";
            
            String serverLogFile="debug/server_im_log.txt";
             if (serverLogFile!=null) {
                String fileName =serverLogFile;
		LogFileParser parser = new LogFileParser();
		Hashtable traces = parser.parseLogsFromFile(fileName);
		tracesViewer=new TracesViewer
		      (serverLogFile,traces,parser.logName,parser.logDescription,
			parser.auxInfo,"IM Traces Viewer", 
                        back,faces,actors,logoNist);
                tracesViewer.show();
            } 
            else { 
                new AlertInstantMessaging("ERROR: Specify a server log file before viewing the traces!",
                       JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            
            
            
            
            
          //  tracesViewer=new TracesViewer("IM Traces Viewer", 
          //  imAddress,"0","nist-sip-im-client",back,faces,actors,logoNist); 
            
            
            
            /*
            String commandLine=javaHome+"java -classpath "+getViewerClasspath()+" "+
                 "tools.tracesviewer.TracesViewer -stackId "+
                 proxyIPAddress+":"+proxyPort+" -back "+back+" -faces "+faces+
                 " -actors "+actors+" -logoNist "+logoNist; 
                    
            System.out.println(commandLine);
            Runtime runtime=Runtime.getRuntime();
            viewerProcess=runtime.exec(commandLine);
                    
            // Thread for Debug:
            errorViewerThread=new ErrorStreamViewerThread(viewerProcess,viewerOutputFrame);
            errorViewerThread.start();
            inputViewerThread=new InputStreamViewerThread(viewerProcess,viewerOutputFrame);
            inputViewerThread.start(); 
             */
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
  
    public void sendIMActionPerformed(ActionEvent evt){
        try{
            IMUserAgent imUA=imGUI.getInstantMessagingUserAgent();
            IMRegisterProcessing imRegisterProcessing=imUA.getIMRegisterProcessing();
           // if (imRegisterProcessing.isRegistered()) {
                RemoteSipURLFrame remoteSipURLFrame=new RemoteSipURLFrame(imGUI);
          //  }
          //  else  new AlertInstantMessaging(
          //  "You must sign in first to the server!!!",JOptionPane.ERROR_MESSAGE);
        }
        catch(Exception e) {
            e.printStackTrace();
        }   
    }
    
    public void addContact(String contact){
        try{
            
            IMUserAgent imUA=imGUI.getInstantMessagingUserAgent();
            IMRegisterProcessing imRegisterProcessing=imUA.getIMRegisterProcessing();
            IMSubscribeProcessing imSubscribeProcessing=imUA.getIMSubscribeProcessing();
           // if (imRegisterProcessing.isRegistered()) {
                AlertInstantMessaging alert=new AlertInstantMessaging("Do you want to add "+
                contact+" to your buddy list?",
                AlertInstantMessaging.CONFIRMATION,null);
                if (alert.getConfirmationResult()==JOptionPane.OK_OPTION){
                    // WE have to send a SUBSCRIBE!!!
                    imSubscribeProcessing.sendSubscribe(getLocalSipURL(),contact,false);
                }
           // }
           // else  new AlertInstantMessaging(
           // "You must sign in first to the server!!!",JOptionPane.ERROR_MESSAGE);
        }
        catch(Exception e) {
            DebugIM.println("ERROR, ListenerInstantMessaging, addContact(), buddy is null:");
            e.printStackTrace();
        }
    }
    
    public void addContactActionPerformed(ActionEvent evt){
        IMUserAgent imUA=imGUI.getInstantMessagingUserAgent();
        IMRegisterProcessing imRegisterProcessing=imUA.getIMRegisterProcessing();
        IMSubscribeProcessing imSubscribeProcessing=imUA.getIMSubscribeProcessing();
        if (imRegisterProcessing.isRegistered()) {
            AlertInstantMessaging alert=new AlertInstantMessaging("Enter the URI"+
            " of the buddy to add:",
            JOptionPane.INFORMATION_MESSAGE,"sip:");
            if (alert.finalInputValue!=null){
                String buddy=alert.finalInputValue;
                if (buddy==null || !XMLBuddyParser.checkURI(buddy) ) {
                    new AlertInstantMessaging("The URI is not valid: enter sip:....",
                    JOptionPane.ERROR_MESSAGE);
                }
                else {
                    // WE have to check if the contact is not already in the buddy list:
                    // WE have to create a new Buddy in the GUI!!!
                    
                    BuddyList buddyList=imGUI.getBuddyList();
                    if ( !buddyList.hasBuddy(buddy) ) { 
                        // WE have to send a SUBSCRIBE!!! WE will add the buddy 
                        // when we will receive the OK... 
                          DebugIM.println("DEBUG, ListenerInstantMessaging, "+
                        " addContactActionPerformed(), WE are going to send a SUBSCRIBE...");
                        imSubscribeProcessing.sendSubscribe(getLocalSipURL(),buddy,false);
                    }
                    else {
                        DebugIM.println("DEBUG, ListenerInstantMessaging, "+
                        " addContactActionPerformed(), The buddy is already in the Buddy list...");
                    }
                }
            }
        }
        else  new AlertInstantMessaging(
        "You must sign in first to the server!!!",JOptionPane.ERROR_MESSAGE);
    }
    
    public void removeContactActionPerformed(ActionEvent evt){
        IMUserAgent imUA=imGUI.getInstantMessagingUserAgent();
        IMRegisterProcessing imRegisterProcessing=imUA.getIMRegisterProcessing();
        if (imRegisterProcessing.isRegistered()) {
            // Just to make sure u know :-)
            AlertInstantMessaging alertInstantMessaging=
            new AlertInstantMessaging("Are you sure to remove the selected contact?",
            AlertInstantMessaging.CONFIRMATION,
            null);
            int confirmationResult=alertInstantMessaging.getConfirmationResult();
            if (confirmationResult==JOptionPane.YES_OPTION) {
                BuddyList buddyList=imGUI.getBuddyList();
                
                String buddy=buddyList.getBuddy(buddyList.getSelectedIndex());
                if (buddy!=null) {
                    if (imRegisterProcessing.isRegistered()) {
                    IMSubscribeProcessing imSubscribeProcessing=imUA.getIMSubscribeProcessing();
                    // WE have to send a "unSUBSCRIBE"!!!
                    DebugIM.println("DEBUG, ListenerInstantMessaging, "+
                    " removeContactActionPerformed(), We have to unSUBSCRIBE to the buddy: "+buddy);
                    imSubscribeProcessing.sendSubscribe(getLocalSipURL(),buddy,true);
                    
                    }
                    buddyList.removeSelectedBuddy();
                }
                else {
                    new AlertInstantMessaging("You must select a contact to remove!",
                    JOptionPane.ERROR_MESSAGE);
                }
            }
        }
       else  new AlertInstantMessaging(
        "You must sign in first to the server!!!",JOptionPane.ERROR_MESSAGE);
    }
    
    public void onlineActionPerformed(ActionEvent evt){
        IMUserAgent imUA=imGUI.getInstantMessagingUserAgent();
        IMRegisterProcessing imRegisterProcessing=imUA.getIMRegisterProcessing();
        if (imRegisterProcessing.isRegistered()) {
            JLabel statusLabel=imGUI.getStatusLabel();
            statusLabel.setText("You are: ONLINE");
           
            localStatus="online";
            
            // we have to notify our SUBSCRIBERS: let's send a NOTIFY for each
            // of them:
            IMNotifyProcessing imNotifyProcessing=imUA.getIMNotifyProcessing();
            imNotifyProcessing.sendNotifyToAllSubscribers("open",localStatus);
        }
        else  new AlertInstantMessaging(
        "You must sign in first to the server!!!",JOptionPane.ERROR_MESSAGE);
    }
    
    public void offlineActionPerformed(ActionEvent evt){
        IMUserAgent imUA=imGUI.getInstantMessagingUserAgent();
        IMRegisterProcessing imRegisterProcessing=imUA.getIMRegisterProcessing();
        if (imRegisterProcessing.isRegistered()) {
            JLabel statusLabel=imGUI.getStatusLabel();
            statusLabel.setText("You are: OFFLINE");
            
            localStatus="offline";
            
            // we have to notify our SUBSCRIBERS: let's send a NOTIFY for each
            // of them:
            IMNotifyProcessing imNotifyProcessing=imUA.getIMNotifyProcessing();
            imNotifyProcessing.sendNotifyToAllSubscribers("closed",localStatus);
        }
        else  new AlertInstantMessaging(
        "You must sign in first to the server!!!",JOptionPane.ERROR_MESSAGE);
    }
    
    public void busyActionPerformed(ActionEvent evt){
        IMUserAgent imUA=imGUI.getInstantMessagingUserAgent();
        IMRegisterProcessing imRegisterProcessing=imUA.getIMRegisterProcessing();
        if (imRegisterProcessing.isRegistered()) {
            JLabel statusLabel=imGUI.getStatusLabel();
            statusLabel.setText("You are: BUSY");
            
            localStatus="busy";
            
            // we have to notify our SUBSCRIBERS: let's send a NOTIFY for each
            // of them:
            IMNotifyProcessing imNotifyProcessing=imUA.getIMNotifyProcessing();
            imNotifyProcessing.sendNotifyToAllSubscribers("open",localStatus);
        }
        else  new AlertInstantMessaging(
        "You must sign in first to the server!!!",JOptionPane.ERROR_MESSAGE);
    }
    
    public void awayActionPerformed(ActionEvent evt){
        IMUserAgent imUA=imGUI.getInstantMessagingUserAgent();
        IMRegisterProcessing imRegisterProcessing=imUA.getIMRegisterProcessing();
        if (imRegisterProcessing.isRegistered()) {
            JLabel statusLabel=imGUI.getStatusLabel();
            statusLabel.setText("You are: AWAY");
            
            localStatus="away";
            
            // we have to notify our SUBSCRIBERS: let's send a NOTIFY for each
            // of them:
            IMNotifyProcessing imNotifyProcessing=imUA.getIMNotifyProcessing();
            imNotifyProcessing.sendNotifyToAllSubscribers("open",localStatus);
        }
        else  new AlertInstantMessaging(
        "You must sign in first to the server!!!",JOptionPane.ERROR_MESSAGE);
    }
    
    
    
    public String getLocalSipURL() {
        try{
            JTextField localSipURLTextField=imGUI.getLocalSipURLTextField();
            String text=localSipURLTextField.getText(); 
            if (text==null || text.trim().equals("") || text.trim().equals("sip:") ) {
                new AlertInstantMessaging("You must enter your sip url before signing in!!!",
                JOptionPane.ERROR_MESSAGE);
                return null;
            }
            else {
               if (text.startsWith("sip:")) {
                     return text;
                } 
                else {
                    String res="sip:"+text;
                    return res;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
  
    public void signMenuMouseClicked(MouseEvent mouseEvent) {
        try{
           IMUserAgent instantMessagingUserAgent=imGUI.getInstantMessagingUserAgent();
            // Send a REGISTER to the proxy
            // and wait for the OK.
            String localSipURL=getLocalSipURL();
            if (localSipURL!=null && !localSipURL.trim().equals("") ) {
                // Build the message:
                IMUserAgent imUA=imGUI.getInstantMessagingUserAgent();
                IMRegisterProcessing imRegisterProcessing=imUA.getIMRegisterProcessing();
		IMPublishProcessing imPublishProcessing=imUA.getIMPublishProcessing();
                if (imRegisterProcessing.isRegistered()) {
                    //It means we will sign out!!!
		    imPublishProcessing.sendPublish(localSipURL.trim(),"offline");
                    imRegisterProcessing.signOut(localSipURL.trim());
                }
                else {
                    imRegisterProcessing.signIn(localSipURL.trim());
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}

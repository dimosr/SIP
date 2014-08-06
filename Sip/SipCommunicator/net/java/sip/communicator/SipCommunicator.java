/*
 * 
 * 	Raptis Dimos - Dimitrios (dimosrap@yahoo.gr) - 03109770
 *  Lazos Philippos (plazos@gmail.com) - 03109082
 * 	Omada 29
 * 
 */

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */
package net.java.sip.communicator;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.awt.*;

import net.java.sip.communicator.common.*;
import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.gui.*;
import net.java.sip.communicator.gui.event.*;
import net.java.sip.communicator.media.*;
import net.java.sip.communicator.media.event.*;
import net.java.sip.communicator.sip.*;
import net.java.sip.communicator.sip.event.*;
import net.java.sip.communicator.sip.security.*;

import java.io.IOException;

import javax.swing.JOptionPane;

import net.java.sip.communicator.plugin.setup.*;
import net.java.sip.communicator.sip.simple.*;


/**
 * <p>Title: SIP COMMUNICATOR</p>
 * <p>Description:JAIN-SIP Audio/Video phone application</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr) </p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
 * <p>Louis Pasteur University - Strasbourg - France</p>
 * @author Emil Ivov (http://www.emcho.com)
 *
 */
public class SipCommunicator
    implements MediaListener,
    UserActionListener,
    CommunicationsListener,
    CallListener,
    SecurityAuthority, SubscriptionAuthority
{
    protected static Console console = Console.getConsole(SipCommunicator.class);

    protected GuiManager               guiManager               = null;
    protected MediaManager             mediaManager             = null;
    protected SipManager               sipManager               = null;
    protected SimpleContactList        simpleContactList        = null;
    protected PresenceStatusController presenceStatusController = null;

    protected Integer unregistrationLock = new Integer(0);

    public SipCommunicator()
    {
        try {
            console.logEntry();

            loadProperties();

            guiManager = new GuiManager();
            mediaManager = new MediaManager();
            sipManager = new SipManager();
            simpleContactList = new SimpleContactList();

            guiManager.addUserActionListener(this);
        }
        finally {
            console.logExit();
        }
    }

    public void configure()
    {
        try {
            console.logEntry();
            guiManager.showConfigFrame();
        }
        finally {
            console.logExit();
        }
    }

    public void launch()
    {
        try {
            console.logEntry();

            //run setup wizard if launching for the first time
            if(   PropertiesDepot.getProperty("net.java.sip.communicator.FIRST_LAUNCH")==null
               || !PropertiesDepot.getProperty("net.java.sip.communicator.FIRST_LAUNCH").equalsIgnoreCase("false")
               )
         {
             SetupWizard.start();
             PropertiesDepot.setProperty("net.java.sip.communicator.FIRST_LAUNCH", "false");
             PropertiesDepot.storeProperties();
         }


             if(Utils.getProperty("net.java.sip.communicator.gui.GUI_MODE") != null
               && Utils.getProperty("net.java.sip.communicator.gui.GUI_MODE").equalsIgnoreCase(GuiManager.PHONE_UI_MODE))
                 guiManager.showPhoneFrame();
            else
                guiManager.showContactList();

            NetworkAddressManager.start();

            try {
                mediaManager.start();
            }
            catch (MediaException exc) {
                console.error("Failed to start mediaManager", exc);
                console.showException(
                    "The following exception occurred while initializing media!\n"
                    + exc.getMessage(),
                    exc);
            }
            mediaManager.addMediaListener(this);

            initDebugTool();

            sipManager.addCommunicationsListener(this);
            sipManager.setSecurityAuthority(this);
            sipManager.setSubscritpionAuthority(this);

            try {
                //put in a seperate thread
                sipManager.start();
                if (sipManager.isStarted()) {
                    console.trace(
                        "sipManager appears to be successfully started");
                    guiManager.setCommunicationActionsEnabled(true);
                }
            }
            catch (CommunicationsException exc) {
                console.showException(
                    "An exception occurred while initializing communication stack!\n"
                    + "You won't be able to send or receive calls",
                    exc);
                return;
            }
            try {
                sipManager.startRegisterProcess();
            }
            catch (CommunicationsException exc) {
                console.error(
                    "An exception occurred while trying to register, exc");
                console.showException(
                    "Failed to register!\n"
                    + exc.getMessage() + "\n"
                    + "This is a warning only. The phone would still function",
                    exc);
            }

            boolean startSimple = false;
            try {
                startSimple = Boolean.valueOf(Utils.getProperty(
                    "net.java.sip.communicator.ENABLE_SIMPLE")).booleanValue();
            }
            catch (Exception ex) {
                console.debug("Couldn't read net.java.sip.communicator.ENABLE_SIMPLE. Assuming false", ex);
            }
            if(startSimple)
            {
                //get the contact list only after registering in case its stored on
                //a remote server
                try {
                    ContactGroup cList =
                        sipManager.retrieveContactList(
                        Utils.getProperty("user.home")
                        + File.separator + ".sip-communicator" + File.separator
                        + Utils.getProperty(
                        "net.java.sip.communicator.sip.simple.CONTACT_LIST_FILE"));
                    simpleContactList.setRoot(cList);
                    simpleContactList.setContactListController(sipManager.
                        getContactListController());

                    presenceStatusController = new PresenceStatusController(
                        sipManager.getPresenceStatusManager());
                    guiManager.setContactListModel(simpleContactList);

                    guiManager.setStatusControllerUiModel(
                        presenceStatusController);

                    //it is important that the following call comes after registering and after
                    //initialization of the presenceStatusController
                    String initialStatus = Utils.getProperty(
                        "net.java.sip.communicator.sip.simple.LAST_SELECTED_OPEN_STATUS");
                    if (initialStatus == null)
                        initialStatus = PresenceTuple.EXTENDED_STATUS_ONLINE;
                    presenceStatusController.requestStatusChange(initialStatus);
                }
                catch (Exception ex) {
                    console.error(ex);
                    console.showException(ex);
                }
            }
        }
        finally {
            console.logExit();
        }
    }

    public static void main(String[] args)
    {
        try {
            console.logEntry();
            //timestamp the log
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            console.debug("Sip Communicator Session, "
                          + cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DATE) + " " //date
                          + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND)); //time

            //dump startup properties for easier debug
            Enumeration systemProperties = System.getProperties().keys();
            while (systemProperties.hasMoreElements()) {
                String pName = (String)systemProperties.nextElement();
                console.debug(pName + "=" + System.getProperty(pName));

            }

            SipCommunicator sipCommunicator = new SipCommunicator();
            if (args.length == 0) {
                sipCommunicator.launch();
            }
            else if (args.length == 1 && args[0].equals("--configure")) {
                sipCommunicator.configure();
            }
            else {
                System.out.println(
                    "Usage: java net.java.sip.communicator.SipCommunicator [--configure]");
            }
        }
        finally {
            console.logExit();
        }
    }

//========================= MEDIA LISTENER ===============================
    public void playerStarting(MediaEvent event)
    {
        try {
            console.logEntry();
            javax.media.Player player = (javax.media.Player) event.getSource();
//		java.awt.Component player = (java.awt.Component) event.getSource();
            guiManager.addVisualComponent(player.getVisualComponent());
            guiManager.addControlComponent(player.getControlPanelComponent());
        }
        finally {
            console.logExit();
        }
    }

    public void nonFatalMediaErrorOccurred(MediaErrorEvent evt)
    {
        console.showNonFatalException(
            "The following non fatal error was reported by MediaManager",
            evt.getCause());
    }

    public void playerStopped()
    {
        try {
            console.logEntry();
            guiManager.removePlayerComponents();
        }
        finally {
            console.logExit();
        }
    }

//========================= USER ACTION LISTENER =========================
    public void handleAnswerRequest(UserCallControlEvent evt)
    {
        try {
            console.logEntry();
            Interlocutor interlocutor =
                (Interlocutor) evt.getAssociatedInterlocutor();
            if (!interlocutor.getCallState().equals(Call.ALERTING)) {
                return;
            }
            String sdpData = null;
            try {
                sdpData = mediaManager.generateSdpDescription();
            }
            catch (MediaException ex) {
                console.showException("Failed to Generate an SDP description",
                                      ex);
                try {
                    sipManager.sendServerInternalError(interlocutor.getID());
                }
                catch (CommunicationsException ex1) {
                    console.error(ex1.getMessage(), ex1);
                }
                return;
            }
            try {
                sipManager.answerCall(interlocutor.getID(), sdpData);
            }
            catch (CommunicationsException exc) {
                console.showException("Could not answer call!\nError was: "
                                      + exc.getMessage(),
                                      exc);
            }
        }
        finally {
            console.logExit();
        }
    }

    public void handleDialRequest(UserCallInitiationEvent evt)
    {
        try {
            console.trace(
                "Entering handleDialRequest(UserCallInitiationEvent)");
            String callee = (String) evt.getSource();
            String sdpData = null;
            try {
                sdpData = mediaManager.generateSdpDescription();
            }
            catch (MediaException ex) {
                console.showException("Failed to Generate an SDP description",
                                      ex);
                return;
            }
            try {
                Call call = sipManager.establishCall(callee, sdpData);
                call.addStateChangeListener(this);
                Interlocutor interlocutor = new Interlocutor();
                interlocutor.setCall(call);
                guiManager.addInterlocutor(interlocutor);
            }
            catch (CommunicationsException exc) {
                console.showException("Could not establish call!\nError was: "
                                      + exc.getMessage(),
                                      exc);
            }
        }
        finally {
            console.logExit();
        }
    }

    public void handleHangupRequest(UserCallControlEvent evt)
    {
        try {
            console.logEntry();
            sipManager.endCall(evt.getAssociatedInterlocutor().getID());
            //no further action should be taken here. Close
            //(mediaManager.closeStream guiManager.removeInterlocutor)
            //should be perfomed by corresponding call listeners
        }
        catch (CommunicationsException exc) {
            console.showException("Could not properly terminate call!\n"
                                  + "(This is not a fatal error)",
                                  exc
                                  );
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Tries to launch the NIST traces viewer.
     * Changes made by M.Ranganathan to match new logging system.
     */
    public void handleDebugToolLaunch()
    {/*
        try {
            console.logEntry();
            if (Utils.getProperty("gov.nist.javax.sip.SERVER_LOG") == null) {
                console.showMsg(
                    "Debug not supported!",
                    "Messages are not logged. Specify SERVER_LOG property");
                return;
            }
            else
            if (Utils.getProperty("net.java.sip.communicator.sip.STACK_PATH") != null
                //means default == nist-sip
                &&
                !Utils.getProperty("net.java.sip.communicator.sip.STACK_PATH").
                equals(
                "gov.nist")) {
                console.showMsg(
                    "Debug not supported!",
                    "You seem to be using a stack other than nist-sip and\n"
                    + "debugging is only supported for the nist-sip sack."
                    );
                return;
            }
            if (tracesViewerWindow != null) {
                tracesViewerWindow.show();
            }
            Class tracesViewerClass;
            Constructor tracesViewerConstructor;
            try {
                tracesViewerClass = Class.forName(
                    "tools.tracesviewer.TracesViewer");
                tracesViewerConstructor =
                    tracesViewerClass.getConstructor(new Class[] {
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class
                });
                tracesViewerWindow = (Window) tracesViewerConstructor.
                    newInstance(
                    new Object[] {
                    "SipCommunicator Traces",
                    Utils.getProperty("gov.nist.javax.sip.SERVER_LOG"),
                    "net/java/sip/communicator/common/resource/back.gif",
                    "net/java/sip/communicator/common/resource/faces.jpg",
                    "net/java/sip/communicator/common/resource/comp.gif",
                    "net/java/sip/communicator/common/resource/nistBanner.jpg"
                }
                    );
                //Do not Center as it goes on top of the phone and there's no point in doing it.
//            int x = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()
//            				- tracesViewerWindow.getWidth())/2;
//			int y = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight()
//            				- tracesViewerWindow.getHeight())/2;
//            tracesViewerWindow.setLocation(x, y);
            }
            catch (Throwable ex) {
                console.showException(
                    "The following exception occurred while trying "
                    + "to launch the TracesViewerDebugTool\n" + ex.getMessage(),
                    ex);
                return;
            }
            tracesViewerWindow.show();
        }
        finally {
            console.logExit();
        }*/
    }

    public void handleExitRequest()
    {
        shutDown();
    }

    protected void shutDown()
    {
        try {
            console.logEntry();
            //close all media streams
            //close capure devices
            try {
                mediaManager.closeStreams();
            }
            catch (Exception exc) {
                console.showException(
                    "Could not properly close media streams!\n", exc);
            }
            catch (Throwable exc)
            {
                console.error("Failed to properly close media streams", exc);
            }

            try {
                mediaManager.stop();
            }
            catch (MediaException exc) {
                console.showException(
                    "Could not properly close media sources!\n", exc);
            }
            catch(Throwable ex)
            {
                console.error("Failed to properly stop Media Manager", ex);
            }

            //close all sip calls
            try {
                sipManager.endAllCalls();
            }
            catch (CommunicationsException exc) {
                console.showException(
                    "Could not properly terminate all calls!\n", exc);
            }
            catch (Throwable exc){
                console.error("Failed to properly end active callse", exc);

            }
            //unregister
            try {
                sipManager.unregister();
            }
            catch (CommunicationsException ex) {
                console.showException("Could not unregister!", ex);
            }
            catch(Throwable exc)
            {
                console.error("Failed to properly unregister", exc);
            }

            NetworkAddressManager.shutDown();
        }
        catch(Exception ex) {
            console.error("Failed to properly shut down.", ex);
            /*
            if (rmiRegistryProcess != null) {
                rmiRegistryProcess.destroy();
            }
            */
        }
        finally{
            console.logExit();
            System.exit(0);
        }
    }

    public void handleForwardRequest(UserCallControlEvent evt)
    {
        try {
        	console.logEntry();
        	sipManager.sendInfo("FORWARD\n" + (String)evt.getSource() + "\n");
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        finally {
            console.logExit();
        }
    }
    
    public void handleFriendRequest(UserCallControlEvent evt)
    {
        try {
        	console.logEntry();
        	sipManager.sendInfo((String)evt.getSource());
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        finally {
            console.logExit();
        }
    }
    
    public void handleBlockRequest(UserCallControlEvent evt)
    {
        try {
        	console.logEntry();
        	sipManager.sendInfo((String)evt.getSource());
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        finally {
            console.logExit();
        }
    }
    
    
    public void handleBlockedListRequest(UserCallControlEvent evt)
    {
        try {
        	console.logEntry();
        	sipManager.sendInfo("GET_BLOCKED");
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        finally {
            console.logExit();
        }
    }
    
    public void handleFriendsListRequest(UserCallControlEvent evt)
    {
        try {
        	console.logEntry();
        	sipManager.sendInfo("GET_FRIENDS");
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        finally {
            console.logExit();
        }
    }
    
    public void handlePriceRequest(UserCallControlEvent evt)
    {
        try {
        	console.logEntry();
        	sipManager.sendInfo("GET_COST");
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        finally {
            console.logExit();
        }
    }
    
    
//======================= COMMUNICATIONS LISTENER ==============================
    public void callReceived(CallEvent evt)
    {
        try {
            console.logEntry();
            Call call = evt.getSourceCall();
            Interlocutor interlocutor = new Interlocutor();
            interlocutor.setCall(call);
            guiManager.addInterlocutor(interlocutor);
            call.addStateChangeListener(this);
        }
        finally {
            console.logExit();
        }
    }

    public void messageReceived(MessageEvent evt)
    {
        try {
            console.logEntry();
            String fromAddress = evt.getFromAddress();
            String fromName = evt.getFromName();
            String messageBody = evt.getBody();
            console.showDetailedMsg(
                "Incoming MESSAGE",
                "You received a MESSAGE\n"
                + "From:    " + fromName + "\n"
                + "Address: " + fromAddress + "\n"
                + "Message: " + messageBody + "\n");
        }
        finally {
            console.logExit();
        }
    }

    public void callRejectedLocally(CallRejectedEvent evt)
    {
        try {
            console.logEntry();
            String reason = evt.getReason();
            String detailedReason = evt.getDetailedReason();
            console.showDetailedMsg(
                "An incoming call was rejected!\n"
                + evt.getReason(),
                evt.getDetailedReason());
        }
        finally {
            console.logExit();
        }
    }

    public void callRejectedRemotely(CallRejectedEvent evt)
    {
        try {
            console.trace(
                "Entering callRejectedRemotely(CallRejectedEvent evt)");
            String reason = evt.getReason();
            String detailedReason = evt.getDetailedReason();
            console.showDetailedMsg(
                "The calling party rejected your call!\n"
                + evt.getReason(),
                evt.getDetailedReason());
        }
        finally {
            console.logExit();
        }
    }

    public void registered(RegistrationEvent evt)
    {
        try {
            console.logEntry();
            guiManager.setGlobalStatus(GuiManager.REGISTERED,
                                       evt.getReason());
        }
        finally {
            console.logExit();
        }
    }
    
    public void receiveBlockedList(BlockedListEvent evt)
    {
        try {
            console.logEntry();
            String msg = evt.getBlockedList();
            if(msg.equals("_WRONGUSERNAME"))
            {
            	guiManager.showError("Wrong Username!");
            	System.exit(0);
            }else if(msg.equals("_WRONGPASSWORD"))
            {
            	guiManager.showError("Wrong Password!");
            	System.exit(0);
            }else
            	guiManager.showBlockedListFrame(evt.getBlockedList());
        }
        finally {
            console.logExit();
        }
    }
    
    public void receiveFriendsList(BlockedListEvent evt)
    {
        try {
            console.logEntry();
            /*String msg = evt.getBlockedList();
            if(msg.equals("_WRONGUSERNAME"))
            {
            	guiManager.showError("Wrong Username!");
            }else if(msg.equals("_WRONGPASSWORD"))
            {
            	guiManager.showError("Wrong Password!");
            }else*/
            	guiManager.showFriendsListFrame(evt.getBlockedList());
        }
        finally {
            console.logExit();
        }
    }
    
    public void receivePrice(BlockedListEvent evt)
    {
        try {
            console.logEntry();
            /*String msg = evt.getBlockedList();
            if(msg.equals("_WRONGUSERNAME"))
            {
            	guiManager.showError("Wrong Username!");
            }else if(msg.equals("_WRONGPASSWORD"))
            {
            	guiManager.showError("Wrong Password!");
            }else*/
            	guiManager.showPriceFrame(evt.getBlockedList());
        }
        finally {
            console.logExit();
        }
    }

    public void registering(RegistrationEvent evt)
    {
        try {
            console.logEntry();
            guiManager.setGlobalStatus(GuiManager.REGISTERING,
                                       evt.getReason());
        }
        finally {
            console.logExit();
        }
    }

    public void unregistered(RegistrationEvent evt)
    {
        try {
            console.logEntry();
            guiManager.setGlobalStatus(GuiManager.NOT_REGISTERED,
                                       evt.getReason());
            //we could now exit
            synchronized(unregistrationLock)
            {
                unregistrationLock.notifyAll();;
            }

        }
        finally {
            console.logExit();
        }
    }

    public void unregistering(RegistrationEvent evt)
    {
        try {
            console.logEntry();
            guiManager.setGlobalStatus(GuiManager.NOT_REGISTERED,
                                       evt.getReason());


            String waitUnreg = Utils.getProperty("net.java.sip.communicator.sip.WAIT_UNREGISTGRATION_FOR");
            if(waitUnreg != null)
            {
                try {
                    int delay = Integer.valueOf(waitUnreg).intValue();
                    //we get here through a _synchronous_ call from shutdown so let's try
                    //and wait for unregistrations confirmation in case the registrar has requested authorization
                    //before conriming unregistration
                    if(delay > 0)
                        synchronized(unregistrationLock)
                        {
                            unregistrationLock.wait(delay);
                        }
                }
                catch (InterruptedException ex) {
                    console.error("Failed to wait for sip-communicator to unregister", ex);
                }
                catch (NumberFormatException ex) {
                    console.error("Value specified for time interval to wait for unregistration was not valid.", ex);
                }

            }

        }
        finally {
            console.logExit();
        }
    }


    public void receivedUnknownMessage(UnknownMessageEvent evt)
    {
        try {
            console.logEntry();
            console.showDetailedMsg(
                "Unknown Communications Message",
                "SipCommunicator's SipManager didn't know how to handle the message " +
                evt.getMessageName() + "\n"
                + "in the current context!\n"
                + "(See Details) ",
                evt.getMessage()
                );
        }
        finally {
            console.logExit();
        }
    }

    public void communicationsErrorOccurred(CommunicationsErrorEvent evt)
    {
        try {
            console.trace(
                "Entering communicationsErrorOccurred(CommunicationsErrorEvent evt)");
            console.showException(
                "SipManager encountered the following error\n"
                + evt.getCause().getMessage() + "\n",
                evt.getCause()
                );
        }
        finally {
            console.logExit();
        }
    }

//======================= CALL LISTENER ==============================
    public void callStateChanged(CallStateEvent evt)
    {
        try {
            console.logEntry();
            Call call = evt.getSourceCall();
            if (evt.getNewState() == Call.CONNECTED) {
                try {
                    mediaManager.openMediaStreams(call.getRemoteSdpDescription());
                }
                catch (MediaException ex) {
                    console.showException(
                        "The following exception occurred while trying to open media connection:\n"
                        + ex.getMessage(),
                        ex);
//You better not send an error response. User would terminate call if they wish so.
//                try {
//                    sipManager.sendServerInternalError(call.getID());
//                }
//                catch (CommunicationsException ex1) {
//                    //Ignore
//                    console.println("Faile to send an error response. " + ex1.getMessage());
//                }
                }
            }
            else if (evt.getNewState() == Call.DISCONNECTED) {
                mediaManager.closeStreams();
            }
        }
        finally {
            console.logExit();
        }
    }
//========================== Security Authority ==============================
    /**
     * Implements obtainCredentials from SecurityAuthority.
     * @param realm the realm that credentials are needed for
     * @return the credentials for the specified realm or null if no credentials
     * could be obtained
     */
    public UserCredentials obtainCredentials(String realm, UserCredentials defaultValues)
    {
        try{
            console.logEntry();

            guiManager.requestAuthentication(sipManager,realm,
                                             defaultValues.getUserName(),
                                             defaultValues.getPassword());

            UserCredentials credentials = new UserCredentials();

            credentials.setUserName(guiManager.getAuthenticationUserName());
            credentials.setPassword(guiManager.getAuthenticationPassword());

            return credentials;
        }
        finally
        {
            console.logExit();
        }
    }

//============================== PROPERTIES ==================================
    protected File getPropertiesFile()
    {
        try {
            console.logEntry();
            String pFileName = Utils.getProperty(
                "net.java.sip.communicator.PROPERTIES");
            if (pFileName == null) {
                pFileName = "sip-communicator.xml";
            }
//			   we'll be using xml from now on (delete the following one day)
//            File propertiesFile;
//            URL url = null;
//            try {
//                url = new File(pFileName).toURL();
//            }
//            catch (MalformedURLException ex) {
//            }
//            //try default location or user specified dir
//            if (url != null) {
//                propertiesFile = new File(url.getPath());
//                //try user home
//            }
//            else {
//                propertiesFile = new File(Utils.getProperty("user.home") +
//                                          "/.sip-communicator/" + pFileName);
//            }
//            return propertiesFile;

            // check in working directory
            File configFileInWorkingDir = new File(pFileName);
            if(configFileInWorkingDir.exists())
            {
                console.trace("file found in working directory");
                return configFileInWorkingDir;
            }

            // check in user.home directory
            File configDir = new File(Utils.getProperty("user.home") +
                                      File.separator +
                                      ".sip-communicator");

            File configFileInUserHomeDir =
                new File(configDir, pFileName);

            if(configFileInUserHomeDir.exists())
            {
                console.trace("file exists in userhome");
                return configFileInUserHomeDir;
            }

            // if doesn't exist - create it
            configDir.mkdirs();
            console.trace("creating properties file");
            InputStream in = PropertiesDepot.class.getClassLoader().
                getResourceAsStream(pFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            PrintWriter writer = new PrintWriter(new FileWriter(configFileInUserHomeDir));

            String line = null;
            System.out.println("Copying properties file:");
            while( (line = reader.readLine()) != null)
            {
                writer.println(line);
                console.debug(line);
            }
            writer.flush();
            return configFileInUserHomeDir;
        }
        catch(IOException ex)
        {
            console.error("Error creating config file", ex);
            return null;
        }
        finally {
            console.logExit();
        }
    }

    public void loadProperties()
    {
        try {
                PropertiesDepot.loadProperties();

//            File pFile = getPropertiesFile();
//            FileInputStream pIS = new FileInputStream(pFile);
//            properties.load(pIS);
//            pIS.close();
//            System.getProperties().putAll(properties);
        }
        //Catch IO & FileNotFound & NullPointer exceptions
        catch (Throwable exc) {
            console.warn(
                "Warning:Failed to load properties!"
                + "\nThis is only a warning.SipCommunicator will use defaults.",
                exc);
        }
    }

    /**
     * Changes made by M.Ranganathan to match new logging system.
     */
    protected void initDebugTool()
    {
        try {
            console.logEntry();
            // If the trace level is already set then just bail out.
            if (Utils.getProperty
                ("gov.nist.javax.sip.TRACE_LEVEL") != null) {
                return;
            }
            // Location where the server log is collected.
            if (Utils.getProperty
                ("gov.nist.javax.sip.SERVER_LOG") == null) {
                Utils.setProperty("gov.nist.javax.sip.SERVER_LOG",
                                  "./log/serverlog.txt");
            }
            // 16 or above logs the messages only.
            if (Utils.getProperty
                ("gov.nist.javax.sip.TRACE_LEVEL") == null) {
                Utils.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
                                  "16");
            }
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Returns a SubscriptionAuthorizationResponse with a code indicating whether
     * or not the specified presence entity is authorised to subscribe for our
     * presence state notifications.
     *
     * @param displayName the presence entity requesting the subscription
     * @param address String
     * @param message String
     * @param acceptedResponses String[]
     * @return an instance of SubscriptionAuthorizationResponse indicating
     *   whether or not the specified presence entity is authorised to
     *   subscribed for events concerning our presence state.
     * @todo Implement this
     *   net.java.sip.communicator.sip.simple.SubscriptionAuthority method
     */
    public SubscriptionAuthorizationResponse requestSubscriptionAuthorization(
        String displayName, String address, String message,
        String[] acceptedResponses)
    {
        SimpleSubscriptionRequest request = new SimpleSubscriptionRequest(displayName, address, message, acceptedResponses);

        String response = guiManager.requestSubscriptionAuthorization(request);

        return SubscriptionAuthorizationResponse.createResponse(response);
    }
}

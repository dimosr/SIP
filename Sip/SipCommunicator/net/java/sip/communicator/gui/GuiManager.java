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
package net.java.sip.communicator.gui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.common.*;
import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.gui.event.*;

import java.awt.SystemColor;

import javax.swing.plaf.metal.MetalLookAndFeel;

import net.java.sip.communicator.gui.plaf.SipCommunicatorColorTheme;

import java.awt.event.KeyEvent;
import java.io.*;

import net.java.sip.communicator.media.JMFRegistry;
import net.java.sip.communicator.plugin.setup.*;
import net.java.sip.communicator.gui.imp.*;
import net.java.sip.communicator.sip.SipManager;
import net.java.sip.communicator.sip.simple.event.*;

/**
 * <p>Title: SIP COMMUNICATOR</p>
 * <p>Description:JAIN-SIP Audio/Video phone application</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr) </p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
 * <p>Louis Pasteur University - Strasbourg - France</p>
 * @author Emil Ivov (http://www.emcho.com)
 * @version 1.1
 *
 */
public class GuiManager
    implements GuiCallback
{
    private static Console console = Console.getConsole(GuiManager.class);
    //Global status codes
    public static final String NOT_REGISTERED = "Not Registered";
    public static final String UNREGISTERING = "Unregistering, please wait!";
    public static final String REGISTERING = "Trying to register as:";
    public static final String REGISTERED = "Registered as ";

    public static final String PHONE_UI_MODE = "PhoneUiMode";
    public static final String IM_UI_MODE    = "ImUiMode";

    static{
        initLookAndFeel();
    }

    private PhoneFrame       phoneFrame   = null;
    private ContactListFrame contactList  = null;
    private ConfigFrame      configFrame  = null;
    private ForwardFrame	 forwardFrame = null;
    private BlockFrame		 blockFrame	  = null;
    private FriendFrame	 	 friendFrame = null;
    private BlockedListFrame blockedListFrame = null;
    private FriendsListFrame friendsListFrame = null;
    private PriceFrame	 	 priceFrame   = null;
    private ArrayList        listeners    = null;
    private AlertManager     alertManager = null;

/** @todo remove after testing */
//    private Properties      properties;
    private JPanel          logoPanel    = null;
    private InterlocutorsTableModel interlocutors = null;
    //Set default colors
    static Color defaultBackground = new Color(255, 255, 255);
    //--

    VoiceMailAction voiceMailAction = null;
    MySipphoneAction mySipphoneAction = null;
    private AuthenticationSplash authenticationSplash = null;

    static boolean isThisSipphoneAnywhere = false;

    public GuiManager()
    {
        String isSipphone = Utils.getProperty("net.java.sip.communicator.sipphone.IS_RUNNING_SIPPHONE");
        if(isSipphone != null && isSipphone.equalsIgnoreCase("true"))
            isThisSipphoneAnywhere = true;

        //create actions first for they are used by others
        voiceMailAction = new VoiceMailAction();
        mySipphoneAction = new MySipphoneAction();

        phoneFrame    = new PhoneFrame(this);
        contactList   = new ContactListFrame();
        configFrame   = new ConfigFrame(phoneFrame);
        forwardFrame  = new ForwardFrame();
        blockFrame 	  = new BlockFrame();
        friendFrame   = new FriendFrame();
        blockedListFrame = new BlockedListFrame();
        friendsListFrame = new FriendsListFrame();
        priceFrame 	  = new PriceFrame();
        listeners     = new ArrayList();
        alertManager  = new AlertManager();
        logoPanel     = new JPanel(new FlowLayout(FlowLayout.CENTER));
        interlocutors = new InterlocutorsTableModel();

        initActionListeners();
        phoneFrame.contactBox.setModel(new ContactsComboBoxModel());

        ForwardAction forwardAction = new ForwardAction();
        ( (MenuBar) phoneFrame.jMenuBar1).addForwardAction(forwardAction);
        //contactList.menuBar.addForwardAction(forwardAction);
        
        BlockAction blockAction = new BlockAction();
        ( (MenuBar) phoneFrame.jMenuBar1).addBlockAction(blockAction);
        //contactList.menuBar.addConfigAction(blockAction);
        
        BlockedListAction blockedListAction = new BlockedListAction();
        ( (MenuBar) phoneFrame.jMenuBar1).addBlockedListAction(blockedListAction);
        //contactList.menuBar.addConfigAction(blockAction);
        
        FriendAction friendAction = new FriendAction();
        ( (MenuBar) phoneFrame.jMenuBar1).addFriendAction(friendAction);
        //contactList.menuBar.addConfigAction(blockAction);
        
        FriendsListAction friendsListAction = new FriendsListAction();
        ( (MenuBar) phoneFrame.jMenuBar1).addFriendsListAction(friendsListAction);
        //contactList.menuBar.addConfigAction(blockAction);
        
        PriceAction priceAction = new PriceAction();
        ( (MenuBar) phoneFrame.jMenuBar1).addPriceAction(priceAction);
        //contactList.menuBar.addConfigAction(blockAction);
        
        ConfigAction configAction = new ConfigAction();
        ( (MenuBar) phoneFrame.jMenuBar1).addConfigCallAction(configAction);
        contactList.menuBar.addConfigAction(configAction);
        
        ConfigMediaAction configMediaAction = new ConfigMediaAction();
        ( (MenuBar) phoneFrame.jMenuBar1).addConfigMediaAction(configMediaAction);
        contactList.menuBar.addConfigMediaAction(configMediaAction);

        SetupWizardAction setupWizardAction = new SetupWizardAction();
        ( (MenuBar) phoneFrame.jMenuBar1).addSetupWizardAction(new SetupWizardAction());
        contactList.menuBar.addSetupWizardAction(setupWizardAction);

        AboutAction aboutAction = new AboutAction();
        ( (MenuBar) phoneFrame.jMenuBar1).addAbout(aboutAction);
        contactList.menuBar.addAboutAction(aboutAction);

        ( (MenuBar) phoneFrame.jMenuBar1).addCallAction(voiceMailAction, KeyEvent.VK_F6);

        if(isThisSipphoneAnywhere)
        {
            console.debug("We are running the sipphone edition and will add the my.sipphone browse launchers");
            ( (MenuBar) phoneFrame.jMenuBar1).addCallAction(mySipphoneAction, KeyEvent.VK_F7);
        }

        ExitAction exitAction = new ExitAction();
        ( (MenuBar) phoneFrame.jMenuBar1).addExitCallAction(exitAction);
        contactList.menuBar.addExitAction(exitAction);
        configFrame.setLocationRelativeTo(phoneFrame);

        phoneFrame.participantsTable.setModel(interlocutors);
        phoneFrame.setIconImage(new ImageIcon(Utils.getResource("sip-communicator-16x16.jpg")).getImage());
        JLabel logoLabel = new JLabel();

//        logoLabel.setIcon(new ImageIcon(Utils.getResource("sip-communicator.jpg")));
        logoLabel.setIcon(new ImageIcon(Utils.getResource("sip-communicator.logo.thin.jpg")));
        logoPanel.add(logoLabel);
        logoPanel.setBackground(Color.white);

        phoneFrame.videoPane.setBackground(Color.white);
        phoneFrame.videoPane.add(logoPanel);
        setGlobalStatus(NOT_REGISTERED, "");
        JOptionPane.setRootFrame(phoneFrame);
    }

    private static void initLookAndFeel()
    {
        MetalLookAndFeel mlf = new MetalLookAndFeel();
        mlf.setCurrentTheme( new SipCommunicatorColorTheme());

        try {
            UIManager.setLookAndFeel(mlf);
        }
        catch (UnsupportedLookAndFeelException ex) {
            console.error("Failed to set custom look and feel", ex);
        }
    }

    public void showPhoneFrame()
    {
        phoneFrame.show();
    }

    public void showContactList()
    {
        contactList.show();
    }

    public void setContactListModel(ContactListModel model)
    {
        contactList.setModel(model);
    }

    /**
     * Sets the PresenceController instance that would fire corresponding events
     * to the user interface.
     * @param statusController the PresenceController instance that would fire corresponding events
     * to the user interface.
     */
    public void setStatusControllerUiModel(PresenceStatusControllerUIModel statusController)
    {
        contactList.setStatusControllerModel(statusController);
    }

    public void showConfigFrame()
    {
        configFrame.show();
    }
    
    public void showForwardFrame()
    {
        forwardFrame.show();
    }
    
    public void showBlockFrame()
    {
        blockFrame.show();
    }
    
    public void showFriendFrame()
    {
        friendFrame.show();
    }

    public void showBlockedListFrame(String b)
    {
    	blockedListFrame.setModal(true);
    	blockedListFrame.updateBlockedUsers(b);
    	blockedListFrame.show();
    }
    
    public void showFriendsListFrame(String b)
    {
    	friendsListFrame.setModal(true);
    	friendsListFrame.updateFriends(b);
    	friendsListFrame.show();
    }
    
    public void showPriceFrame(String b)
    {
    	priceFrame.setModal(true);
    	priceFrame.updatePrice(b);
    	priceFrame.show();
    }
    
    public void addVisualComponent(Component vComp)
    {
        if (vComp == null) {
            return;
        }
        else {
            phoneFrame.videoPane.remove(logoPanel);
            phoneFrame.videoPane.add(vComp);
        }
        phoneFrame.videoPane.updateUI();
    }

    public void addControlComponent(Component cComp)
    {
        if (cComp == null) {
            return;
        }
        else {
            phoneFrame.videoPane.remove(logoPanel);
            phoneFrame.videoPane.add(cComp);
        }
        phoneFrame.videoPane.updateUI();
    }

    public void removePlayerComponents()
    {
        phoneFrame.videoPane.removeAll();
//        phoneFrame.controlPanes.removeAll();
//        phoneFrame.videoPane.add(phoneFrame.controlPanes, BorderLayout.SOUTH);
        phoneFrame.videoPane.add(logoPanel);
        phoneFrame.videoPane.updateUI();
    }

    public void addInterlocutor(InterlocutorUI interlocutor)
    {
        interlocutor.setCallback(this);
        interlocutors.addInterlocutor(interlocutor);
        phoneFrame.participantsTable.
            setRowSelectionInterval(interlocutors.findIndex(interlocutor.getID()),
                                    interlocutors.findIndex(interlocutor.getID()));
    }

    public void setCommunicationActionsEnabled(boolean enabled)
    {
//        phoneFrame.contactBox.setEnabled(enabled);
        phoneFrame.dialButton.setEnabled(enabled);
        phoneFrame.hangupButton.setEnabled(enabled);
        phoneFrame.answerButton.setEnabled(enabled);
    }

    public void addUserActionListener(UserActionListener l)
    {
        listeners.add(l);
    }

    public void removeUserActionListener(UserActionListener l)
    {
        listeners.remove(l);
    }

//    public static void main(String[] args) //throws HeadlessException
//    {
//        GuiManager guiManager = new GuiManager();
//    }
//-------------------------- GuiCallback --------------------
    public void update(InterlocutorUI interlocutorUI)
    {
        interlocutors.update(interlocutorUI);
    }

    public void remove(InterlocutorUI interlocutorUI)
    {
        interlocutors.remove(interlocutorUI);
    }

    //----------- Alerts
    public void startAlert(String alertResourceName)
    {
        try {
            alertManager.startAlert(alertResourceName);
        }
        catch (Throwable ex) {
            //OK, no one cares really
            console.warn("Couldn't play alert", ex);
        }
    }

    public void stopAlert(String alertResourceName)
    {
        try {
            alertManager.stopAlert(alertResourceName);
        }
        catch (Throwable ex) {
            //OK, no one cares really
            console.warn("Couldn't sotp alert", ex);
        }
    }

//----------------- Event dispatching------------------------
    void dialButton_actionPerformed(EventObject evt)
    {
        //TODO temporarily close alerts from here.
        alertManager.stopAllAlerts();
        String callee = phoneFrame.contactBox.getSelectedItem().toString();
        if (callee == null || callee.trim().length() < 1) {
            return;
        }
        UserCallInitiationEvent commEvt = new UserCallInitiationEvent(callee);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            ( (UserActionListener) listeners.get(i)).handleDialRequest(commEvt);
        }
    }

    void hangupButton_actionPerformed(ActionEvent evt)
    {
        //TODO temporarily close alerts from here.
        alertManager.stopAllAlerts();
        if (interlocutors.getRowCount() < 1) {
            return;
        }
        int selectedRow = phoneFrame.participantsTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow > interlocutors.getRowCount() - 1) {
            return;
        }
        InterlocutorUI inter = interlocutors.getInterlocutorAt(selectedRow);
        UserCallControlEvent commEvt = new UserCallControlEvent(inter);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            ( (UserActionListener) listeners.get(i)).handleHangupRequest(
                commEvt);
        }
    }

    void answerButton_actionPerformed(ActionEvent evt)
    {
        //TODO temporarily close alerts from here.
        alertManager.stopAllAlerts();
        if (interlocutors.getRowCount() < 1) {
            return;
        }
        int selectedRow = phoneFrame.participantsTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow > interlocutors.getRowCount() - 1) {
            return;
        }
        InterlocutorUI inter = interlocutors.getInterlocutorAt(selectedRow);
        UserCallControlEvent commEvt = new UserCallControlEvent(inter);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            ( (UserActionListener) listeners.get(i)).handleAnswerRequest(
                commEvt);
        }
    }

    void fireExitRequest()
    {
        for (int i = listeners.size() - 1; i >= 0; i--) {
            ( (UserActionListener) listeners.get(i)).handleExitRequest();
        }
    }

    void fireDebugToolLaunchRequest()
    {
        for (int i = listeners.size() - 1; i >= 0; i--) {
            ( (UserActionListener) listeners.get(i)).handleDebugToolLaunch();
        }
    }

//============================== Configuration ==============================
/** @todo remove after testing */
//    public void setProperties(Properties properties)
//    {
//        configFrame.properties.setProperties(properties);
//        this.properties = properties;
//    }

//    private void configFrame_savePerformed(ActionEvent evt)
//    {
//        //check if properties are still being edited
//        if (configFrame.propertiesTable.isEditing()) {
//            configFrame.propertiesTable.getCellEditor().stopCellEditing();
//        }
//        for (int i = listeners.size() - 1; i >= 0; i--) {
//            ( (UserActionListener) listeners.get(i)).
//                handlePropertiesSaveRequest();
//        }
//        configFrame.dispose();
//    }

    public void setGlobalStatus(String statusCode, String reason)
    {
        if (statusCode == REGISTERED) {
            phoneFrame.registrationLabel.setForeground(SipCommunicatorColorTheme.REGISTERED);
            phoneFrame.registrationLabel.setText(statusCode);
            phoneFrame.registrationAddressLabel.setForeground(SipCommunicatorColorTheme.REGISTERED);
            phoneFrame.registrationAddressLabel.setText(reason);
        }
        else if (statusCode == REGISTERING) {
            phoneFrame.registrationLabel.setForeground(SipCommunicatorColorTheme.REGISTERING);
            phoneFrame.registrationLabel.setText(statusCode);
            phoneFrame.registrationAddressLabel.setForeground(SipCommunicatorColorTheme.REGISTERING);
            phoneFrame.registrationAddressLabel.setText(reason);
        }
        else if (statusCode == NOT_REGISTERED) {
            phoneFrame.registrationLabel.setForeground(SipCommunicatorColorTheme.NOT_REGISTERED);
            phoneFrame.registrationLabel.setText(statusCode + " ");
            phoneFrame.registrationAddressLabel.setForeground(SipCommunicatorColorTheme.NOT_REGISTERED);
            phoneFrame.registrationAddressLabel.setText(reason);
        }
        else if (statusCode == UNREGISTERING)
        {
            phoneFrame.registrationLabel.setForeground(SipCommunicatorColorTheme.NOT_REGISTERED);
            phoneFrame.registrationLabel.setText(statusCode + " ");
            phoneFrame.registrationAddressLabel.setForeground(SipCommunicatorColorTheme.NOT_REGISTERED);
            phoneFrame.registrationAddressLabel.setText(reason);
        }
        else {
            phoneFrame.registrationLabel.setForeground(Color.red);
            phoneFrame.registrationLabel.setText(statusCode);
        }
    }

//===================================== Action classes ===============================

    private class MySipphoneAction
        extends AbstractAction
    {
        String mySipphoneUrl = Utils.getProperty("net.java.sip.communicator.sipphone.MY_SIPPHONE_URL");
        String commandStr = null;
        public MySipphoneAction()
        {
            super("My.SIPphone");

            String os = Utils.getProperty("os.name");
            if(os.toLowerCase().indexOf("windows") != -1)
                commandStr = "rundll32 url.dll,FileProtocolHandler " + mySipphoneUrl;
            else
                commandStr = "mozilla " + mySipphoneUrl;

        }

        public void actionPerformed(ActionEvent evt)
        {

            try {
                Runtime.getRuntime().exec(commandStr);
            }
            catch (IOException ex) {
                console.error("Failed to open a browser to my.sipphone", ex);
            }
        }
    }

    private class VoiceMailAction
        extends AbstractAction
    {
        private String voiceMailNumber = null;

        public VoiceMailAction()
        {
            super("Voicemail");
            voiceMailNumber = Utils.getProperty("net.java.sip.communicator.VOICE_MAIL_ADDRESS");
            if(voiceMailNumber == null)
                setEnabled(false);

            //they don't support that just yet
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent evt)
        {
            phoneFrame.contactBox.setSelectedItem(voiceMailNumber);
            dialButton_actionPerformed(new EventObject(phoneFrame.dialButton));
        }

    }

    private class ExitAction
        extends AbstractAction
    {
        public ExitAction()
        {
            super("Exit");
        }

        public void actionPerformed(ActionEvent evt)
        {
            fireExitRequest();
        }
    }

    private class ConfigAction
        extends AbstractAction
    {
        public ConfigAction()
        {
            super("Configure");
        }

        public void actionPerformed(ActionEvent evt)
        {
            showConfigFrame();
        }
    }

    private class ForwardAction
    	extends AbstractAction
    {
    	public ForwardAction()
    	{
    		super("Forward");
    	}

    	public void actionPerformed(ActionEvent evt)
    	{
    		showForwardFrame();
    		if(!forwardFrame.canceled)
    		{
    			String forwardTo = forwardFrame.getForwardUser();
    	        UserCallControlEvent commEvt = new UserCallControlEvent(forwardTo);
    	        for (int i = listeners.size() - 1; i >= 0; i--) {
    	            ( (UserActionListener) listeners.get(i)).handleForwardRequest(
    	                commEvt);
    	        }
    		}
    	}
    }
    
    private class BlockAction
    extends AbstractAction
    {
    	public BlockAction()
    	{
    		super("Block");
    	}

    	public void actionPerformed(ActionEvent evt)
    	{
    		showBlockFrame();
    		if(!blockFrame.canceled())
    		{
    			String user = null; 
    			if(blockFrame.block())
    				user = "BLOCK\n" + blockFrame.getUser() + "\n";
    			else
    				user = "UNBLOCK\n" + blockFrame.getUser() + "\n";
    			
    	        UserCallControlEvent commEvt = new UserCallControlEvent(user);
    	        for (int i = listeners.size() - 1; i >= 0; i--) {
    	            ( (UserActionListener) listeners.get(i)).handleBlockRequest(
    	                commEvt);
    	        }
    		}
    	}
    }
    
    private class BlockedListAction
    extends AbstractAction
    {
    	public BlockedListAction()
    	{
    		super("View Blocked Users");
    	}

    	public void actionPerformed(ActionEvent evt)
    	{

    	        UserCallControlEvent commEvt = new UserCallControlEvent("");
    	        for (int i = listeners.size() - 1; i >= 0; i--) {
    	            ( (UserActionListener) listeners.get(i)).handleBlockedListRequest(
    	                commEvt);
    	        }
    	}
    }
    
    private class FriendsListAction
    extends AbstractAction
    {
    	public FriendsListAction()
    	{
    		super("View Friends");
    	}

    	public void actionPerformed(ActionEvent evt)
    	{

    	        UserCallControlEvent commEvt = new UserCallControlEvent("");
    	        for (int i = listeners.size() - 1; i >= 0; i--) {
    	            ( (UserActionListener) listeners.get(i)).handleFriendsListRequest(
    	                commEvt);
    	        }
    	}
    }
    
    private class PriceAction
    extends AbstractAction
    {
    	public PriceAction()
    	{
    		super("View Total Cost");
    	}

    	public void actionPerformed(ActionEvent evt)
    	{

    	        UserCallControlEvent commEvt = new UserCallControlEvent("");
    	        for (int i = listeners.size() - 1; i >= 0; i--) {
    	            ( (UserActionListener) listeners.get(i)).handlePriceRequest(
    	                commEvt);
    	        }
    	}
    }
    
    private class FriendAction
    extends AbstractAction
    {
    	public FriendAction()
    	{
    		super("Add Friends");
    	}

    	public void actionPerformed(ActionEvent evt)
    	{
    		showFriendFrame();
    		if(!friendFrame.canceled())
    		{
    			String user = null; 
    			if(friendFrame.block())
    				user = "FRIEND\n" + friendFrame.getUser() + "\n";
    			else
    				user = "UNFRIEND\n" + friendFrame.getUser() + "\n";
    			
    	        UserCallControlEvent commEvt = new UserCallControlEvent(user);
    	        for (int i = listeners.size() - 1; i >= 0; i--) {
    	            ( (UserActionListener) listeners.get(i)).handleFriendRequest(
    	                commEvt);
    	        }
    		}
    	}
    }
    
    
    private class ConfigMediaAction
        extends AbstractAction
    {
        public ConfigMediaAction()
        {
            super("Media Preferences (JMF Registry)");
        }

        public void actionPerformed(ActionEvent evt)
        {
            net.java.sip.communicator.media.JMFRegistry.main(null);
//            net.java.sip.communicator.media.JMFInit.main(null);
//            try {
//              Runtime.getRuntime().exec(System.getProperty("java.home") + java.io.File.separator + "bin"+java.io.File.separator+"java -classpath "+System.getProperty("java.class.path")+" JMFRegistry" );
//              System.out.println(System.getProperty("java.home") + java.io.File.separator + "bin"+java.io.File.separator+"java -classpath "+System.getProperty("java.class.path")+" JMFRegistry" );
//            }
//            catch (IOException ex) {
//              console.error(ex);
//            }
            //configFrame.show();
        }
    }


    /*
        private class MediaChooserAction
            extends AbstractAction
        {
            public MediaChooserAction()
            {
                super("...");
            }
            public void actionPerformed(ActionEvent evt)
            {
                JFileChooser chooser = new JFileChooser();
                chooser.showOpenDialog(configFrame);
                configFrame.mediaSource.setText(
                    "file:/" +
                    chooser.getSelectedFile().getAbsolutePath());
            }
        }
     */
    private class SetupWizardAction
        extends AbstractAction
    {
        public SetupWizardAction()
        {
            super("Run Setup Wizard");
        }

        public void actionPerformed(ActionEvent action)
        {
            SetupWizard.start();
        }
    }


    private class ShowTracesAction
        extends AbstractAction
    {
        public ShowTracesAction()
        {
            super("View Traces");
        }

        public void actionPerformed(ActionEvent action)
        {
            fireDebugToolLaunchRequest();
        }
    }

    private void initActionListeners()
    {
        ActionListener dialListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                dialButton_actionPerformed(evt);
            }
        };
        phoneFrame.dialButton.addActionListener(dialListener);
        phoneFrame.contactBox.addItemListener(new ContactBoxListener());
        phoneFrame.answerButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                answerButton_actionPerformed(evt);
            }
        });
        phoneFrame.hangupButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                hangupButton_actionPerformed(evt);
            }
        });
        phoneFrame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                fireExitRequest();
            }
        });
        contactList.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                fireExitRequest();
            }
        });

    }

    private class ContactBoxListener
        implements ItemListener
    {
        public void itemStateChanged(ItemEvent evt)
        {
            if ( ( (DefaultComboBoxModel) phoneFrame.contactBox.getModel()).
                getIndexOf(evt.getItem()) == -1) {
                ( (DefaultComboBoxModel) phoneFrame.contactBox.getModel()).
                    addElement(evt.getItem().toString().trim());
            }
        }
    }

    public void requestAuthentication(SipManager sM,String realm,
                                      String userName,
                                      char[] password)
    {
        if (authenticationSplash != null)
            authenticationSplash.dispose();
        authenticationSplash = new AuthenticationSplash(phoneFrame, true, sM);
        if(userName != null)
            authenticationSplash.userNameTextField.setText(userName);
        if(password != null)
            authenticationSplash.passwordTextField.setText(new String(password));
        //Set a relevant realm value
        //Bug report by Steven Lass (sltemp at comcast.net)
        if(realm != null)
            authenticationSplash.realmValueLabel.setText(new String(realm));
        authenticationSplash.show();
    }

    public String getAuthenticationUserName()
    {
        return authenticationSplash.userName;
    }

    public char[] getAuthenticationPassword()
    {
        return authenticationSplash.password;
    }

    /**
     * Displays a SubscriptionAuthorizationDialog that demands the user to
     * authorize the specified subscription request.
     * @param request the request that is to be authorized
     * @return a String (a member of the array returned by
     * SubscriptionRequestUIModel.getAcceptedResponses()) that indacates the
     * response of the user.
     */
    public String requestSubscriptionAuthorization(SubscriptionRequestUIModel request)
    {
        return SubscriptionAuthorizationDialog.obtainAuthorisationResponse(phoneFrame, request);
    }

    /**
     * The class is used to display an About Dialog describing the SIP Communicator
     * project
     */
    private class AboutAction
        extends AbstractAction
    {
        public AboutAction()
        {
            super("About ...");
        }

        public void actionPerformed(ActionEvent evt)
        {
             JOptionPane.showMessageDialog(null,
                                           new JLabel(new ImageIcon(Utils.getResource("sip-communicator.about.jpg"))),
                                           "SIPphone Anywhere (powered by SIP Communicator)",
                                           JOptionPane.PLAIN_MESSAGE);
        }
    }

    
    public void showError(String error)
    {
    	JOptionPane.showMessageDialog(phoneFrame, error);
    }

}

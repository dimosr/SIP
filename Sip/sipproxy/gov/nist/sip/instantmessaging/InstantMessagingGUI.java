/*
 * IMGUI.java
 *
 * Created on July 27, 2002, 10:57 PM
 */

package gov.nist.sip.instantmessaging;

import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.io.*;
import gov.nist.sip.instantmessaging.presence.*;
import gov.nist.sip.instantmessaging.authentication.* ;
/**
 *
 * @author  olivier
 * @version 1.0
 */
public class InstantMessagingGUI extends JFrame {

    // Menus
    protected JMenuBar menuBar;
    protected JMenu menuMenu;
    protected JMenu statusMenu;
    protected JMenu optionsMenu;
    protected JMenu signMenu;
    protected JMenu helpMenu;
    protected JMenu quit;

    protected JMenuItem configurationMenuItem;
    protected JMenuItem connectionMenuItem;
    protected JMenuItem debugMenuItem;
    
    protected JMenuItem sendIMMenuItem;
    protected JMenuItem addContactMenuItem;
    protected JMenuItem removeContactMenuItem;
    protected JRadioButtonMenuItem onlineJRadioButtonMenuItem;
    protected JRadioButtonMenuItem awayJRadioButtonMenuItem;
    protected JRadioButtonMenuItem offlineJRadioButtonMenuItem;
    protected JRadioButtonMenuItem busyJRadioButtonMenuItem;
    protected JMenuItem tracesViewerMenuItem;
    
    protected JPanel firstPanel;
    protected JPanel secondPanel;

    protected JLabel localSipURLLabel;
    protected JLabel statusLabel;
    protected JTextField localSipURLTextField;
    protected JLabel buddyLabel;

    protected BuddyList buddyList;
    
    private AuthenticationProcess authenticationProcess;
    protected ListenerInstantMessaging listenerInstantMessaging;
    protected IMUserAgent instantMessagingUserAgent;
   
    // All for the container:
    protected static Color containerBackGroundColor=new Color(204,204,204);
    protected static String logo="logoNist-gray.jpg";
    
    // All for the labels:
    protected static Border labelBorder=new EtchedBorder(EtchedBorder.RAISED);
    protected static Color labelBackGroundColor=new Color(217,221,221);
    
    // All for the TextField
    protected static Color textBackGroundColor=Color.white;
    
    // All for the Button
    protected static Border buttonBorder=new BevelBorder(BevelBorder.RAISED);
    protected static Color  buttonBackGroundColor=new Color(186,175,175);
    
    protected String propertiesFile;
    protected Properties properties;
    protected String xmlBuddiesFile;

/******************************************************************************/   
/*********************************  Some useful methods ***********************/    
/******************************************************************************/
    
    protected String replaceSlashByBackSlash(String line) {
        if (line==null) return null;
        else {
            try{
                StringBuffer res=new StringBuffer();
                for (int i=0;i<line.length();i++) {
                    char c=line.charAt(i);
                    if ( c=='\\' )
                        res.append("/");
                    else res.append(c);
                }
                //DebugIM.println("Utils:"+res);
                return res.toString();
            }
            catch(Exception e) {
                return null;
            }
        }
    }
    
    
    public AuthenticationProcess getAuthenticationProcess() {
        return authenticationProcess;
    }
    
    
    public boolean getAuthorizationForBuddy(String buddy) {
        // WE should read in a file for the old subscriptions
        // if it's accepted and rejected
        
        // Let's check if we have an already defined authorization for this buddy:
        
        if (buddyList.hasAuthorization(buddy)) {
            DebugIM.println("DEBUG, InstantMessagingGUI, getAuthorizationForBuddy()"+
            ", we got a positive authorization from the XML file for: "+buddy);
            return true;
        }
        else {
         
            DebugIM.println("DEBUG, InstantMessagingGUI, getAuthorizationForBuddy()"+
            ", the user has to authorize the buddy to subscribe: "+buddy);
            AlertInstantMessaging alertInstantMessaging= new
            AlertInstantMessaging("The buddy "+
            buddy+" wants to add you in his buddy list, do you authorize him?",
            AlertInstantMessaging.CONFIRMATION,null);
            
            boolean author=(alertInstantMessaging.getConfirmationResult()==JOptionPane.OK_OPTION);
            // WE have to update the buddies vector!!!
           // buddyList.setAuthorization(buddy,author);
             DebugIM.println("DEBUG, InstantMessagingGUI, getAuthorizationForBuddy()"+
            ", the user authorization is: "+author);
            return author;
        }
    }
    
    public BuddyList getBuddyList() {
        return buddyList;
    }
    
    public JMenu getSignMenu() {
        return signMenu;
    }
    
    public JRadioButtonMenuItem getOfflineJRadioButtonMenuItem() {
        return offlineJRadioButtonMenuItem;
    }
    
    public JRadioButtonMenuItem getOnlineJRadioButtonMenuItem() {
        return onlineJRadioButtonMenuItem;
    }
    
    public JLabel getStatusLabel() {
        return statusLabel;
    }
    
   
    
    public String getPropertiesFile() {
        return propertiesFile;
    }
     
    public JTextField getLocalSipURLTextField() {
        return localSipURLTextField;
    }
    
    public IMUserAgent getInstantMessagingUserAgent() {
        return instantMessagingUserAgent;
    }
    
    public ListenerInstantMessaging getListenerInstantMessaging() {
        return listenerInstantMessaging;
    }

    public String getXMLBuddiesFile() {
        return xmlBuddiesFile;
    }
    
    public void start() {
        try{

            instantMessagingUserAgent.start();
           
        }
        catch(Exception e) {
            DebugIM.println("DebugIM, InstantMessagingGUI, start(), Unable to start the UA:");
            e.printStackTrace();
        }
    }
    
    public void stop() {
        try{

            instantMessagingUserAgent.stop();
           
        }
        catch(Exception e) {
            DebugIM.println("DebugIM, InstantMessagingGUI, stop(), Unable to stop the UA:");
            e.printStackTrace();
        }
    }
    
    public void restart() {
        try{
            DebugIM.println("\n***************** RESTARTING ****************************\n");
            stop();
            uploadProperties(propertiesFile);
            initFields();
            uploadBuddies();
            uploadAuthentication();
            start();
            if (listenerInstantMessaging.tracesViewer!=null) {
                try{
                 listenerInstantMessaging.tracesViewer.close();
                 listenerInstantMessaging.tracesViewer=null;
                 //listenerInstantMessaging.tracesViewerActionPerformed(null);
                 DebugIM.println("DebugIM, restart(), traces viewer closed.");
                }
                catch(Exception e) {
                    DebugIM.println("DebugIM, InstantMessagingGUI, restart(), traces viewer"+
                    " not closed, exception raised:");
                    e.printStackTrace();
                }
            }       
            DebugIM.println("\n*************************************************\n");
        }
        catch(Exception e) {
            DebugIM.println("DebugIM, InstantMessagingGUI, restart(), Unable to restart the UA:");
            e.printStackTrace();
        }
    }
    
    public void blockProperties() {
        localSipURLTextField.setEditable(false);
        localSipURLTextField.setBackground(Color.lightGray);
        ConfigurationFrame configurationFrame=listenerInstantMessaging.getConfigurationFrame();
        configurationFrame.blockProperties();
    }
    
    public void unblockProperties() {
        localSipURLTextField.setEditable(true);
        localSipURLTextField.setBackground(Color.white);
        ConfigurationFrame configurationFrame=listenerInstantMessaging.getConfigurationFrame();
        configurationFrame.unblockProperties();
    }
    
    public void uploadProperties(String propertiesFile) {
        properties=new Properties();
        
        if (propertiesFile==null) {
            DebugIM.println("DebugIM, Unable to upload the properties: properties file missing");
            return;
        }
        

        
        DebugIM.println("DebugIM, InstantMessagingGUI, we upload the properties from the "+
        "file:"+propertiesFile);
        try{
            properties.load( new FileInputStream(propertiesFile)) ;
            DebugIM.println("DebugIM, InstantMessagingGUI, properties uploaded.");
        }
        catch(Exception e) {
            DebugIM.println("DebugIM, InstantMessagingGUI, properties file not found");
        }
    }

    public void initFields() {
        String localSipURL=properties.getProperty("examples.im.localSipURL");
        if (localSipURL!=null) {
            if (localSipURL.startsWith("sip:"))
                   localSipURLTextField.setText(localSipURL);
            else localSipURLTextField.setText("sip:"+localSipURL);
        }
        else   DebugIM.println("DebugIM, InstantMessagingGUI, initFields(),"+
             " Local Sip URL not set!!");
            
    
        ConfigurationFrame configurationFrame=listenerInstantMessaging.getConfigurationFrame();
        
        
        // Init the components input:
        String outboundProxyAddress=properties.getProperty("examples.im.outboundProxyAddress");
        if (outboundProxyAddress!=null && !outboundProxyAddress.trim().equals("")) 
            configurationFrame.outboundProxyAddressTextField.setText(outboundProxyAddress);
        String outboundProxyPort=properties.getProperty("examples.im.outboundProxyPort");
        if (outboundProxyPort!=null && !outboundProxyPort.trim().equals("")) 
            configurationFrame.outboundProxyPortTextField.setText(outboundProxyPort);
        
        String registrarAddress=properties.getProperty("examples.im.registrarAddress");
        if ( registrarAddress!=null && !registrarAddress.trim().equals("")) 
            configurationFrame.registrarAddressTextField.setText(registrarAddress);
        String registrarPort=properties.getProperty("examples.im.registrarPort");
        if (registrarPort!=null && !registrarPort.trim().equals("") )
            configurationFrame.registrarPortTextField.setText(registrarPort);
        
        String imAddress=properties.getProperty("examples.im.imAddress");
        if ( imAddress!=null && !imAddress.trim().equals(""))
            configurationFrame.imAddressTextField.setText(imAddress);
        String imPort=properties.getProperty("examples.im.imPort");
        if (imPort!=null && !imPort.trim().equals(""))
            configurationFrame.imPortTextField.setText(imPort);
        String imProtocol=properties.getProperty("examples.im.imProtocol");
        if (imProtocol!=null && !imProtocol.trim().equals(""))
            configurationFrame.imProtocolTextField.setText(imProtocol);
        
        String outputFile=properties.getProperty("examples.im.outputFile");
        if (outputFile!=null && !outputFile.trim().equals("")) {
            configurationFrame.outputFileTextField.setText(outputFile);
            DebugIM.setDebugFile(outputFile);
        }
        
        
        
        String buddiesFile=properties.getProperty("examples.im.buddiesFile");
        if (  buddiesFile!=null && !buddiesFile.trim().equals("")) 
            configurationFrame.buddiesFileTextField.setText(buddiesFile);
        String authenticationFile=properties.getProperty("examples.im.authenticationFile");
        if (  authenticationFile!=null && !authenticationFile.trim().equals("")) 
            configurationFrame.authenticationFileTextField.setText(authenticationFile);
        
        String router=properties.getProperty("examples.im.defaultRouter");
        if (router!=null && !router.trim().equals(""))
            configurationFrame.defaultRouterTextField.setText(router);
        
    }
    
    public void uploadBuddies() {
            xmlBuddiesFile=properties.getProperty("examples.im.buddiesFile");
            if (xmlBuddiesFile!=null) {
                
                XMLBuddyParser xmlBuddyParser=new XMLBuddyParser(xmlBuddiesFile);
                Vector buddies=xmlBuddyParser.getBuddies();
                if (buddies!=null)
                    buddyList.init(buddies);
                else {
                    DebugIM.println("DebugIM, InstantMessagingGUI, uploadBuddies(),"+
                    " No Buddies to upload...");
                    buddyList.init(new Vector());
                }
            }
            else  {
                DebugIM.println("DebugIM, InstantMessagingGUI, uploadBuddies(),"+
                " Property not set for uploading the buddies...");
                buddyList.init(new Vector());
            }
    }
    
    public void uploadAuthentication() {
        try{
            
            String authenticationFile=properties.getProperty("examples.im.authenticationFile");
            if (authenticationFile!=null && !authenticationFile.trim().equals("")) {
                
                XMLAuthenticationParser xmlAuthenticationParser=
                new XMLAuthenticationParser(authenticationFile);
                Vector usersTagList=xmlAuthenticationParser.getUsersTagList();
                
                authenticationProcess=new AuthenticationProcess(instantMessagingUserAgent,usersTagList);
            }
            else {
                DebugIM.println("DebugIM, InstantMessagingGUI, uploadAuthentication(),"+
                " the file for authentication is missing");
            }
        }
        catch(Exception e) {
            DebugIM.println("DebugIM, InstantMessagingGUI, uploadAuthentication(),"+
                " Error trying to upload the authentication file ");
            e.printStackTrace();
        }
    }
    
/*******************************************************************************/    
/*******************************************************************************/    
    
    /** Creates new IMGUI */
    public InstantMessagingGUI(String confFile) throws Exception{
        super("NIST-SIP Instant Messaging");
       
        listenerInstantMessaging=new ListenerInstantMessaging(this);
        instantMessagingUserAgent=new IMUserAgent(this);
        
       // listenerInstantMessaging.startRMIregistry();
        
        propertiesFile=confFile;
        DebugIM.println("\n***************** STARTING ******************************\n");
        uploadProperties(propertiesFile);
        initComponents();
        initFields();
        uploadBuddies();
        uploadAuthentication();
        start();
        DebugIM.println("\n*************************************************\n");
        show();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    public void initComponents() {
        /***************** The main frame ***************************************/
        // width, height
        this.setSize(360,400);
        Container container=this.getContentPane();
        container.setLayout(new BorderLayout());
        container.setBackground(containerBackGroundColor);
        this.setLocation(0,0);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              clean();
            }
        });
        
        /********************** Menu bar **************************************/
        menuBar=new JMenuBar();
        setJMenuBar(menuBar);
        // create a menu and add it to the menubar
        menuMenu=new JMenu("  Menu  ");
        menuMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
        menuMenu.setToolTipText("Available features!!!");

        configurationMenuItem=new JMenuItem("Configuration");
        configurationMenuItem.setToolTipText("Edit the properties");
        configurationMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
                 listenerInstantMessaging.configurationActionPerformed(evt);
              }
          }
        );
        menuMenu.add(configurationMenuItem);
        
        sendIMMenuItem=new JMenuItem("Send an IM ");
        sendIMMenuItem.setToolTipText("Send an instant message");
        sendIMMenuItem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                 listenerInstantMessaging.sendIMActionPerformed(evt);
              }
          }
        );
       
        addContactMenuItem=new JMenuItem("Add contact");
        addContactMenuItem.setToolTipText("Add a contact to the buddy list");
        addContactMenuItem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                 listenerInstantMessaging.addContactActionPerformed(evt);
              }
          }
        );
        
        removeContactMenuItem=new JMenuItem("Remove a contact");
        removeContactMenuItem.setToolTipText("Remove a contact from the buddy list");
        removeContactMenuItem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                 listenerInstantMessaging.removeContactActionPerformed(evt);
              }
          }
        );
        
        tracesViewerMenuItem=new JMenuItem("Traces viewer");
        tracesViewerMenuItem.setToolTipText("Visualize the traces");
        tracesViewerMenuItem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                 listenerInstantMessaging.tracesViewerActionPerformed(evt);
              }
          }
        );
        
        menuMenu.add(sendIMMenuItem);
        menuMenu.add(addContactMenuItem);
        menuMenu.add(removeContactMenuItem);
        menuMenu.add(tracesViewerMenuItem);
        
        statusMenu=new JMenu("  Status ");
       // statusMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
        statusMenu.setToolTipText("your status!!!");
        
        onlineJRadioButtonMenuItem=new JRadioButtonMenuItem(" Online ");
        onlineJRadioButtonMenuItem.setToolTipText("Set your status to online");
        onlineJRadioButtonMenuItem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                 listenerInstantMessaging.onlineActionPerformed(evt);
              }
          }
        );
        
        awayJRadioButtonMenuItem=new JRadioButtonMenuItem(" Away ");
        awayJRadioButtonMenuItem.setToolTipText("Set your status to away");
        awayJRadioButtonMenuItem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                 listenerInstantMessaging.awayActionPerformed(evt);
              }
          }
        );
        
        offlineJRadioButtonMenuItem=new JRadioButtonMenuItem(" Offline ");
        offlineJRadioButtonMenuItem.setToolTipText("Set your status to offline");
        offlineJRadioButtonMenuItem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                 listenerInstantMessaging.offlineActionPerformed(evt);
              }
          }
        );
        
        busyJRadioButtonMenuItem=new JRadioButtonMenuItem(" busy ");
        busyJRadioButtonMenuItem.setToolTipText("Set your status to busy");
        busyJRadioButtonMenuItem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                 listenerInstantMessaging.busyActionPerformed(evt);
              }
          }
        );
        
        ButtonGroup group = new ButtonGroup();
        offlineJRadioButtonMenuItem.setSelected(true);
        group.add(onlineJRadioButtonMenuItem);
        group.add(offlineJRadioButtonMenuItem);
        group.add(busyJRadioButtonMenuItem);
        group.add(awayJRadioButtonMenuItem);
        
        statusMenu.add(awayJRadioButtonMenuItem);
        statusMenu.add(onlineJRadioButtonMenuItem);
        statusMenu.add(offlineJRadioButtonMenuItem);
        statusMenu.add(busyJRadioButtonMenuItem);
        menuMenu.add(statusMenu);
        
        // add the menu to the menu bar
        menuBar.add(menuMenu);
        

        signMenu=new JMenu("  Sign in  ");
        signMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
        signMenu.setToolTipText("Sign in to the server");
        signMenu.addMouseListener( new MouseAdapter() {
            public void mouseClicked(MouseEvent evt){
                  listenerInstantMessaging.signMenuMouseClicked(evt);
              }
          }
        );
        menuBar.add(signMenu);
       
        //...create and add some menus...
        menuBar.add(Box.createHorizontalGlue());
        
        helpMenu=new JMenu("  Help  ");
        helpMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
        helpMenu.setToolTipText("Some useful notes about this tool");
        helpMenu.addMouseListener( new MouseAdapter() {
            public void mouseClicked(MouseEvent evt){
                   listenerInstantMessaging.helpMenuMouseEvent(evt);
              }
          }
        );
        menuBar.add(helpMenu);
          
        quit=new JMenu("  Quit  ");
        quit.setBorder(new BevelBorder(BevelBorder.RAISED));
        quit.setToolTipText("Quit the application");
        quit.addMouseListener( new MouseAdapter() {
            public void mouseClicked(MouseEvent evt){
              clean();
            }
        }
        );
        menuBar.add(quit);
        
      
      
        
        
        /****************** The components    **********************************/
        firstPanel=new JPanel();
        //Top,,bottom,
        //firstPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
        // If put to False: we see the container's background
        firstPanel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        firstPanel.setLayout( new GridLayout(3,1,0,0) );

        JPanel subPanel1=new JPanel();
        //Top,,bottom,
        subPanel1.setBorder(BorderFactory.createEmptyBorder(20,10,7,10));
        // If put to False: we see the container's background
        subPanel1.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        subPanel1.setLayout( new GridLayout(1,1,5,10) );
        
        JPanel subPanel2=new JPanel();
        //Top,,bottom,
        subPanel2.setBorder(BorderFactory.createEmptyBorder(4,10,15,10));
        // If put to False: we see the container's background
        subPanel2.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        subPanel2.setLayout( new GridLayout(1,2,5,10) );
        
        statusLabel=new JLabel(" You are: OFFLINE");
        statusLabel.setToolTipText("your status");
        // Alignment of the text
        statusLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        statusLabel.setForeground(Color.black);
        // Size of the text
        statusLabel.setFont(new Font ("Dialog", 1, 14));
        // If put to true: we see the label's background
        statusLabel.setOpaque(true);
        statusLabel.setBackground(labelBackGroundColor);
        statusLabel.setBorder(labelBorder);
        subPanel1.add(statusLabel);
       
        firstPanel.add(subPanel1);
        firstPanel.add(subPanel2);

        localSipURLLabel=new JLabel("Contact SIP URL:");
        localSipURLLabel.setToolTipText("Your identifier");
        // Alignment of the text
        localSipURLLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        localSipURLLabel.setForeground(Color.black);
        // Size of the text
        localSipURLLabel.setFont(new Font ("Dialog", 1, 14));
        // If put to true: we see the label's background
        localSipURLLabel.setOpaque(true);
        localSipURLLabel.setBackground(labelBackGroundColor);
        localSipURLLabel.setBorder(labelBorder);
        localSipURLTextField = new JTextField(20);
        localSipURLTextField.setHorizontalAlignment(JTextField.LEFT);
        localSipURLTextField.setFont(new Font ("Dialog", 1, 14));
        localSipURLTextField.setBackground(textBackGroundColor);
        localSipURLTextField.setForeground(Color.black);
        localSipURLTextField.setText("sip:");
        localSipURLTextField.setSelectionStart(4);
        localSipURLTextField.setBorder(BorderFactory.createLoweredBevelBorder() );
        subPanel2.add(localSipURLLabel);
        subPanel2.add(localSipURLTextField);
       
        // The buddy list:
        JPanel subPanel3=new JPanel();
        //Top,,bottom,
        subPanel3.setBorder(BorderFactory.createEmptyBorder(30,10,0,10));
        // If put to False: we see the container's background
        subPanel3.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        subPanel3.setLayout( new GridLayout(1,1,5,10) );
        firstPanel.add(subPanel3);
        
        buddyLabel=new JLabel("Buddy list:");
        buddyLabel.setToolTipText("Double click on the contact to send an IM");
        // Alignment of the text
        buddyLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        buddyLabel.setForeground(Color.black);
        // Size of the text
        buddyLabel.setFont(new Font ("Dialog", 1, 14));
        // If put to true: we see the label's background
        buddyLabel.setOpaque(true);
        buddyLabel.setBackground(labelBackGroundColor);
        buddyLabel.setBorder(labelBorder);
        subPanel3.add(buddyLabel);
        
        JPanel subPanel4=new JPanel();
        //Top,,bottom,
        subPanel4.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
        // If put to False: we see the container's background
        subPanel4.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        subPanel4.setLayout( new GridLayout(1,1,5,10) );
        buddyList=new BuddyList(this);
        buddyList.setToolTipText("Double click on the contact to send an IM");
        MouseListener mouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            listenerInstantMessaging.buddyListMouseClicked(e);
        }
        };
        buddyList.addMouseListener(mouseListener);
        
        JScrollPane scrollPane = new JScrollPane(buddyList,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        subPanel4.add(scrollPane);
        
        // The logo
        JPanel imagesPanel=new JPanel();
        imagesPanel.setOpaque(false);
        // top, left, bottom, right
        imagesPanel.setBorder(BorderFactory.createEmptyBorder(5,5,0,0));
        container.add(imagesPanel);
        imagesPanel.setLayout( new FlowLayout(FlowLayout.CENTER,0,0) );
        
        ImageIcon icon;
        icon = new ImageIcon("./images/"+logo);
        JLabel label=new JLabel(icon);
        label.setVisible(true);
        label.setToolTipText("The NIST logo!!!");
        label.setHorizontalAlignment(AbstractButton.CENTER);
        label.setForeground(Color.black);
        label.setFont(new Font ("Dialog", 1, 14));
        label.setOpaque(true);
        label.setBackground(Color.lightGray);
        imagesPanel.add(label);
        
        
        container.add("North",firstPanel);
        container.add("Center",subPanel4);
        container.add("South",imagesPanel);
        
        
    }
    
    
    public void clean() {
        try {
            instantMessagingUserAgent.stop();
            buddyList.writeToXMLFile();
            
            if (listenerInstantMessaging.rmiregistryProcess!=null) {
                 listenerInstantMessaging.rmiregistryProcess.destroy();
                 DebugIM.println("DebugIM, rmiregistry process destroyed.");
            }       
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        System.exit(0);
    }
    
    public static void main(String args[]) {
       try{
           
           String confFile= (String) args[1];
           InstantMessagingGUI imGUI=new InstantMessagingGUI(confFile);
         
       }
       catch(Exception e){
            System.out.println
            ("ERROR: Set the configuration file flag: " +
            "USE: -cf configuration_file_location"  );
            System.out.println("ERROR, the IM client can not be started, " +
            " exception raised:\n");
            e.printStackTrace();
       }
   }
    
    
}
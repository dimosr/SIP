/*
 *
 *
 * Created on April 1, 2002, 3:08 PM
 */
package gov.nist.sip.instantmessaging;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import gov.nist.sip.instantmessaging.presence.*;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class ConfigurationFrame extends JFrame {
    
    protected JLabel outboundProxyAddressLabel;
    protected JLabel outboundProxyPortLabel;
    protected JLabel registrarAddressLabel;
    protected JLabel registrarPortLabel;
    protected JLabel imAddressLabel;
    protected JLabel imPortLabel;
    protected JLabel imProtocolLabel;
    protected JLabel outputFileLabel;
    protected JLabel buddiesFileLabel;
    protected JLabel authenticationFileLabel;
    protected JLabel defaultRouterLabel;
   
    protected JTextField outboundProxyAddressTextField;
    protected JTextField outboundProxyPortTextField ;
    protected JTextField registrarAddressTextField;
    protected JTextField registrarPortTextField;
    protected JTextField imAddressTextField ;
    protected JTextField imPortTextField;
    protected JTextField imProtocolTextField;
    protected JTextField outputFileTextField;
    protected JTextField buddiesFileTextField;  
    protected JTextField authenticationFileTextField; 
    protected JTextField defaultRouterTextField;
   
    protected JPanel firstPanel;
    protected JPanel thirdPanel;
 
    protected JButton submitButton;   
    protected InstantMessagingGUI imGUI;
    
    
    // All for the container:
    public Color containerBackGroundColor=new Color(204,204,204);
   
    
    // All for the labels:
    public Border labelBorder=new EtchedBorder(EtchedBorder.RAISED);
    public Color labelBackGroundColor=new Color(217,221,221);
    
    // All for the TextField
    public Color textBackGroundColor=Color.white;
    
    // All for the Button
    public Border buttonBorder=new BevelBorder(BevelBorder.RAISED);
    public Color  buttonBackGroundColor=new Color(186,175,175);
   
    
    /** Creates new form SIPHeadersParametersFrame */
    public ConfigurationFrame(InstantMessagingGUI imGUI, String title) {
        super(title);
        this.imGUI=imGUI;
        
      
        initComponents();
     
      
    }
    
    public String getRouterPath() {
        return defaultRouterTextField.getText();
    }
    
    public String getOutboundProxyAddress() {
        return outboundProxyAddressTextField.getText();
    }
    
    public String getOutboundProxyPort() {
        return outboundProxyPortTextField.getText();
    }
    
    public String getRegistrarAddress() {
        return registrarAddressTextField.getText();
    }
    
    public String getRegistrarPort() {
        return registrarPortTextField.getText();
    }
    
    public String getIMAddress() {
        return imAddressTextField.getText();
    }
    
    public String getIMPort() {
        return imPortTextField.getText();
    }
    
    public String getIMProtocol() {
        return imProtocolTextField.getText();
    }
    
    public String getOutputFile() {
        return outputFileTextField.getText();
    }
    
    public void hideFrame() {
        this.hide();
    }
    
    public void blockProperties() {
        outboundProxyAddressTextField.setEditable(false);
        outboundProxyAddressTextField.setBackground(Color.lightGray);
        outboundProxyPortTextField.setEditable(false);
        outboundProxyPortTextField.setBackground(Color.lightGray); 
        registrarAddressTextField.setEditable(false);
        registrarAddressTextField.setBackground(Color.lightGray); 
        registrarPortTextField.setEditable(false);
        registrarPortTextField.setBackground(Color.lightGray);
        imAddressTextField.setEditable(false);
        imAddressTextField.setBackground(Color.lightGray);
        imPortTextField.setEditable(false);
        imPortTextField.setBackground(Color.lightGray);
        imProtocolTextField.setEditable(false);
        imProtocolTextField.setBackground(Color.lightGray);
        outputFileTextField.setEditable(false);
        outputFileTextField.setBackground(Color.lightGray);
        buddiesFileTextField.setEditable(false);
        buddiesFileTextField.setBackground(Color.lightGray);
        authenticationFileTextField.setEditable(false);
        authenticationFileTextField.setBackground(Color.lightGray);
        defaultRouterTextField.setEditable(false);
        defaultRouterTextField.setBackground(Color.lightGray);
    }
    
    public void unblockProperties() {
        outboundProxyAddressTextField.setEditable(true);
        outboundProxyAddressTextField.setBackground(Color.white);
        outboundProxyPortTextField.setEditable(true);
        outboundProxyPortTextField.setBackground(Color.white);
        registrarAddressTextField.setEditable(true);
        registrarAddressTextField.setBackground(Color.white); 
        registrarPortTextField.setEditable(true);
        registrarPortTextField.setBackground(Color.white);
        imAddressTextField.setEditable(true);
        imAddressTextField.setBackground(Color.white);
        imPortTextField.setEditable(true);
        imPortTextField.setBackground(Color.white);
        imProtocolTextField.setEditable(true);
        imProtocolTextField.setBackground(Color.white);
        outputFileTextField.setEditable(true);
        outputFileTextField.setBackground(Color.white);
        buddiesFileTextField.setEditable(true);
        buddiesFileTextField.setBackground(Color.white);
        authenticationFileTextField.setEditable(true);
        authenticationFileTextField.setBackground(Color.white);
        defaultRouterTextField.setEditable(true);
        defaultRouterTextField.setBackground(Color.white);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    public void initComponents() {
        /***************** The main frame ***************************************/
        // width, height
        this.setSize(560,370);
        Container container=this.getContentPane();
        container.setLayout(new BoxLayout(getContentPane(), 1));
        container.setBackground(containerBackGroundColor);
        this.setLocation(0,0);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new AlertInstantMessaging("Your changes will not be checked: use the Submit button!!!",
                JOptionPane.WARNING_MESSAGE);
                hideFrame();
            }
        });
        
        /****************** The components    **********************************/
        firstPanel=new JPanel();
        firstPanel.setBorder(BorderFactory.createEmptyBorder(15,4,15,4));
        // If put to False: we see the container's background
        firstPanel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        firstPanel.setLayout( new GridLayout(11,2,2,2) );
        container.add(firstPanel);
        
        outboundProxyAddressLabel=new JLabel("Outbound proxy IP address:");
        outboundProxyAddressLabel.setForeground(Color.black);
        outboundProxyAddressTextField = new JTextField(20);
        outboundProxyAddressLabel.setBorder(labelBorder);
        outboundProxyAddressLabel.setOpaque(true);
        outboundProxyAddressLabel.setBackground(labelBackGroundColor);
        firstPanel.add(outboundProxyAddressLabel);
        firstPanel.add(outboundProxyAddressTextField);
        
        outboundProxyPortLabel=new JLabel("Outbound proxy port:");
        outboundProxyPortLabel.setForeground(Color.black);
        outboundProxyPortTextField = new JTextField(20);
        outboundProxyPortLabel.setBorder(labelBorder);
        outboundProxyPortLabel.setOpaque(true);
        outboundProxyPortLabel.setBackground(labelBackGroundColor);
        firstPanel.add(outboundProxyPortLabel);
        firstPanel.add(outboundProxyPortTextField);
        
        registrarAddressLabel=new JLabel("Registrar IP address:");
        registrarAddressLabel.setForeground(Color.black);
        registrarAddressTextField = new JTextField(20);
        registrarAddressLabel.setBorder(labelBorder);
        registrarAddressLabel.setOpaque(true);
        registrarAddressLabel.setBackground(labelBackGroundColor);
        firstPanel.add(registrarAddressLabel);
        firstPanel.add(registrarAddressTextField);
        
        
        registrarPortLabel=new JLabel("Registrar port:");
        registrarPortLabel.setForeground(Color.black);
        registrarPortTextField = new JTextField(20);
        registrarPortLabel.setBorder(labelBorder);
        registrarPortLabel.setOpaque(true);
        registrarPortLabel.setBackground(labelBackGroundColor);
        firstPanel.add(registrarPortLabel);
        firstPanel.add(registrarPortTextField);
        
        imAddressLabel=new JLabel("Contact IP address:");
        imAddressLabel.setForeground(Color.black);
        imAddressTextField = new JTextField(20);
        imAddressLabel.setBorder(labelBorder);
        imAddressLabel.setOpaque(true);
        imAddressLabel.setBackground(labelBackGroundColor);
        firstPanel.add(imAddressLabel);
        firstPanel.add(imAddressTextField);
        
        imPortLabel=new JLabel("Contact port:");
        imPortLabel.setForeground(Color.black);
        imPortTextField = new JTextField(20);
        imPortLabel.setBorder(labelBorder);
        imPortLabel.setOpaque(true);
        imPortLabel.setBackground(labelBackGroundColor);
        firstPanel.add(imPortLabel);
        firstPanel.add(imPortTextField);
        
        imProtocolLabel=new JLabel("Contact transport:");
        imProtocolLabel.setForeground(Color.black);
        imProtocolTextField = new JTextField(20);
        imProtocolLabel.setBorder(labelBorder);
        imProtocolLabel.setOpaque(true);
        imProtocolLabel.setBackground(labelBackGroundColor);
        firstPanel.add(imProtocolLabel);
        firstPanel.add(imProtocolTextField);
        
        outputFileLabel=new JLabel("Output file:");
        outputFileLabel.setForeground(Color.black);
        outputFileTextField = new JTextField(20);
        outputFileLabel.setBorder(labelBorder);
        outputFileLabel.setOpaque(true);
        outputFileLabel.setBackground(labelBackGroundColor);
        firstPanel.add(outputFileLabel);
        firstPanel.add(outputFileTextField);
        
            
        buddiesFileLabel=new JLabel("Buddies file:");
        buddiesFileLabel.setForeground(Color.black);
        buddiesFileTextField = new JTextField(20);
        buddiesFileLabel.setBorder(labelBorder);
        buddiesFileLabel.setOpaque(true);
        buddiesFileLabel.setBackground(labelBackGroundColor);
        firstPanel.add(buddiesFileLabel);
        firstPanel.add(buddiesFileTextField);
        
        authenticationFileLabel=new JLabel("Authentication file:");
        authenticationFileLabel.setForeground(Color.black);
        authenticationFileTextField = new JTextField(20);
        authenticationFileLabel.setBorder(labelBorder);
        authenticationFileLabel.setOpaque(true);
        authenticationFileLabel.setBackground(labelBackGroundColor);
        firstPanel.add(authenticationFileLabel);
        firstPanel.add(authenticationFileTextField);
   
        defaultRouterLabel=new JLabel("Default router class name:");
        defaultRouterLabel.setForeground(Color.black);
        defaultRouterTextField = new JTextField(20);
        defaultRouterLabel.setBorder(labelBorder);
        defaultRouterLabel.setOpaque(true);
        defaultRouterLabel.setBackground(labelBackGroundColor);
        firstPanel.add(defaultRouterLabel);
        firstPanel.add(defaultRouterTextField);
        
        
        thirdPanel = new JPanel();
        thirdPanel.setOpaque(false);
        // top, left, bottom, right
        thirdPanel.setLayout(new FlowLayout(FlowLayout.CENTER) );
       
        submitButton = new JButton(" Submit ");
        submitButton.setToolTipText("Submit your changes!");
        submitButton.setFocusPainted(false);
        submitButton.setFont(new Font ("Dialog", 1, 16));
        submitButton.setBackground(buttonBackGroundColor);
        submitButton.setBorder(buttonBorder);
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                 submitButtonActionPerformed(evt);
            }
          }
        );
        thirdPanel.add(submitButton);
        container.add(thirdPanel);
      
    }
    
    public boolean check(String text) {
        if (text==null || text.trim().equals("") ) {
             return false;
        }
        else return true;
    }
    
    
    
    public void submitButtonActionPerformed(ActionEvent evt) {
        String text="";
        
        IMUserAgent imUA=imGUI.getInstantMessagingUserAgent();
        IMRegisterProcessing imRegisterProcessing=imUA.getIMRegisterProcessing();
        if ( !imRegisterProcessing.isRegistered()) {
            
            String temp=outboundProxyAddressTextField.getText();
            if (temp !=null && !temp.trim().equals("") )
                text+="examples.im.outboundProxyAddress="+temp+"\n";
            
            temp=outboundProxyPortTextField.getText();
            if (temp !=null && !temp.trim().equals("") )
                text+="examples.im.outboundProxyPort="+temp+"\n";
            
            temp=registrarAddressTextField.getText();
            if (temp !=null && !temp.trim().equals("") )
                text+="examples.im.registrarAddress="+temp+"\n";
            
            temp=registrarPortTextField.getText();
            if (temp !=null && !temp.trim().equals("") )
                text+="examples.im.registrarPort="+temp+"\n";
            
            temp=imAddressTextField.getText();
            if (temp !=null && !temp.trim().equals("") )
                text+="examples.im.imAddress="+temp+"\n";
            
            temp=imPortTextField.getText();
            if (temp !=null && !temp.trim().equals("") ) 
                text+="examples.im.imPort="+temp+"\n";  
            
            temp=imProtocolTextField.getText();
            if (temp !=null && !temp.trim().equals("") ) 
                text+="examples.im.imProtocol="+temp+"\n";
            
            temp=outputFileTextField.getText();
            if (temp !=null && !temp.trim().equals("") ) {
                text+="examples.im.outputFile="+temp+"\n";
                
                DebugIM.setDebugFile(temp);
            }
            else DebugIM.setDebugFile(null);
            
            temp=buddiesFileTextField.getText();
            if (temp !=null && !temp.trim().equals("") )
                text+="examples.im.buddiesFile="+temp+"\n";
            
            temp=authenticationFileTextField.getText();
            if (temp !=null && !temp.trim().equals("") )
                text+="examples.im.authenticationFile="+temp+"\n";
            
            temp=defaultRouterTextField.getText();
            if (temp !=null && !temp.trim().equals("") )
                text+="examples.im.defaultRouter="+temp+"\n";
            
            temp=imGUI.getLocalSipURLTextField().getText();
            if (temp !=null && !temp.trim().equals("") )
                text+="examples.im.localSipURL="+temp+"\n";
            
            String propertiesFile=imGUI.getPropertiesFile();
            if (propertiesFile==null) DebugIM.println("DebugIM, unable to write the "+
            "properties, specify a properties file when you start the client" );
            else {
                IMUtilities.writeFile(propertiesFile,text);
                imGUI.restart();   
            }
            this.setVisible(false);
            
        }
        else {
             new AlertInstantMessaging(
                "You must sign out from the registrar before changing something!!!",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
}


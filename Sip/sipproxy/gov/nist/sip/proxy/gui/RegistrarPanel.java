/*
 * RegistrationAuthenticationFrame.java
 *
 * Created on April 15, 2002, 1:55 PM
 */

package gov.nist.sip.proxy.gui;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import gov.nist.sip.proxy.*;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class RegistrarPanel extends JPanel {
   
    protected JPanel firstPanel;
    protected JPanel secondPanel;
    protected JPanel thirdPanel;
  
    protected JCheckBox enableRegistrationsCheckBox;
    protected JCheckBox enablePresenceServerCheckBox;
    
    protected JLabel registrationTimeOutLabel;
    protected JLabel xmlRegistrationFileLabel;
   
    protected JTextField registrationTimeOutTextField;
    protected JTextField xmlRegistrationFileTextField;
   
    protected JButton xmlRegistrationFileButton;
  
    protected ProxyLauncher proxyLauncher;
    protected DomainList domainList;
    protected ConfigurationFrame parent;
    
    
    /** Creates new RegistrationAuthenticationFrame */
    public RegistrarPanel(ConfigurationFrame configurationFrame,ProxyLauncher proxyLauncher) {
        super();
        this.parent=configurationFrame;
        this.proxyLauncher=proxyLauncher;
        
        domainList=new DomainList(proxyLauncher);
        
        initComponents();
        
        // Init the components input:
        try{
            Configuration configuration=proxyLauncher.getConfiguration();
            if (configuration==null) return;
            enableRegistrationsCheckBox.setSelected(configuration.enableRegistrations);
            enablePresenceServerCheckBox.setSelected(configuration.enablePresenceServer);
            if (configuration.registrationsFile!=null)
                xmlRegistrationFileTextField.setText(configuration.registrationsFile);
            registrationTimeOutTextField.setText(String.valueOf(configuration.expiresTime));
            
            if (configuration==null)
                domainList.displayList(new Vector() );
            else
                domainList.displayList(configuration.domainList);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    public void initComponents() {
        
        /****************** The components    **********************************/
        firstPanel=new JPanel();
        //firstPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        // If put to False: we see the container's background
        firstPanel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        firstPanel.setLayout( new GridLayout(4,2,4,4) );
        this.setBorder(BorderFactory.createEmptyBorder(3,5,5,3));
        this.setLayout( new GridLayout(2,1,3,3) );
        this.add(firstPanel);
        
        enableRegistrationsCheckBox = new JCheckBox("Registrations uploading");
        enableRegistrationsCheckBox.setBorderPainted(true);
        enableRegistrationsCheckBox.setBorder(ProxyLauncher.labelBorder);
        enableRegistrationsCheckBox.setSelected(true);
        enableRegistrationsCheckBox.setFont(new Font("Dialog", 1, 12));
        enableRegistrationsCheckBox.setBackground(ProxyLauncher.labelBackGroundColor);
        firstPanel.add(enableRegistrationsCheckBox);
        
       
        enablePresenceServerCheckBox = new JCheckBox("Enable presence server");
        enablePresenceServerCheckBox.setBorderPainted(true);
        enablePresenceServerCheckBox.setBorder(ProxyLauncher.labelBorder);
        enablePresenceServerCheckBox.setSelected(true);
        enablePresenceServerCheckBox.setFont(new Font("Dialog", 1, 12));
        enablePresenceServerCheckBox.setBackground(ProxyLauncher.labelBackGroundColor);
        firstPanel.add(enablePresenceServerCheckBox);
        
        registrationTimeOutLabel=new JLabel("Registrations expires time:");
        registrationTimeOutLabel.setToolTipText("registration lifetime before expiring (in sec.)");
        // Alignment of the text
        registrationTimeOutLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        registrationTimeOutLabel.setForeground(Color.black);
        // Size of the text
        registrationTimeOutLabel.setFont(new Font ("Dialog", 1, 12));
        // If put to true: we see the label's background
        registrationTimeOutLabel.setOpaque(true);
        registrationTimeOutLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        registrationTimeOutLabel.setBorder(ProxyLauncher.labelBorder);
        registrationTimeOutTextField = new JTextField(15);
        registrationTimeOutTextField.setHorizontalAlignment(AbstractButton.CENTER);
        registrationTimeOutTextField.setFont(new Font ("Dialog", 0, 14));
        registrationTimeOutTextField.setBackground(ProxyLauncher.textBackGroundColor);
        registrationTimeOutTextField.setForeground(Color.black);
        registrationTimeOutTextField.setBorder(BorderFactory.createLoweredBevelBorder() );
        firstPanel.add(registrationTimeOutLabel);
        firstPanel.add(registrationTimeOutTextField);
      
        
        xmlRegistrationFileLabel=new JLabel("Registrations XML file:");
        xmlRegistrationFileLabel.setToolTipText("Location of the XML file for uploading registrations");
        // Alignment of the text
        xmlRegistrationFileLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        xmlRegistrationFileLabel.setForeground(Color.black);
        // Size of the text
        xmlRegistrationFileLabel.setFont(new Font ("Dialog", 1, 12));
        // If put to true: we see the label's background
        xmlRegistrationFileLabel.setOpaque(true);
        xmlRegistrationFileLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        xmlRegistrationFileLabel.setBorder(ProxyLauncher.labelBorder);
        xmlRegistrationFileTextField = new JTextField(30);
        xmlRegistrationFileTextField.setEditable(false);
        xmlRegistrationFileTextField.setVisible(true);
        xmlRegistrationFileTextField.setHorizontalAlignment(AbstractButton.CENTER);
        xmlRegistrationFileTextField.setFont(new Font ("Dialog", 0, 14));
        xmlRegistrationFileTextField.setForeground(Color.black);
        xmlRegistrationFileTextField.setBorder(BorderFactory.createLoweredBevelBorder() );
        xmlRegistrationFileButton=new JButton("Choose");
        xmlRegistrationFileButton.setToolTipText("Choose the passwords file for the authentication!!!");
        xmlRegistrationFileButton.setFont(new Font ("Dialog", 0, 14));
        xmlRegistrationFileButton.setFocusPainted(false);
        xmlRegistrationFileButton.setVisible(true);
        xmlRegistrationFileButton.setBackground(ProxyLauncher.buttonBackGroundColor);
        xmlRegistrationFileButton.setBorder(ProxyLauncher.buttonBorder);
        xmlRegistrationFileButton.setVerticalAlignment(AbstractButton.CENTER);
        xmlRegistrationFileButton.setHorizontalAlignment(AbstractButton.CENTER);
        xmlRegistrationFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                  xmlRegistrationFileActionPerformed(evt);
            }
          }
        );
        firstPanel.add(xmlRegistrationFileLabel);
        firstPanel.add(xmlRegistrationFileButton);
        
        /*
        secondPanel=new JPanel();
        secondPanel.setBorder(BorderFactory.createEmptyBorder(0,20,0,20));
        // If put to False: we see the container's background
        secondPanel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        secondPanel.setLayout( new GridLayout(1,1,0,0) );
    
        this.add(secondPanel);
        */
        JLabel ghostLabel=new JLabel("");
      
        firstPanel.add(xmlRegistrationFileTextField);
          firstPanel.add(ghostLabel);
         JPanel panel=new JPanel();
        // top, left, bottom, right
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,2));
        // If put to False: we see the container's background
        panel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        panel.setLayout( new BorderLayout() );
        this.add(panel);
        
        JLabel lpLabel=new JLabel("Domains list:");
        lpLabel.setVisible(true);
        lpLabel.setToolTipText("The domains the proxy is responsible for");
        lpLabel.setHorizontalAlignment(AbstractButton.CENTER);
        lpLabel.setForeground(Color.black);
        lpLabel.setFont(new Font ("Dialog", 1, 12));
        lpLabel.setOpaque(true);
        lpLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        lpLabel.setBorder(ProxyLauncher.labelBorder);
        panel.add(lpLabel,BorderLayout.NORTH);
   
        //this.add(listeningPointsList);
        JScrollPane scrollPane = new JScrollPane(domainList,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane,BorderLayout.CENTER);
        
        thirdPanel = new JPanel();
        thirdPanel.setOpaque(false);
        // top, left, bottom, right
        thirdPanel.setBorder(BorderFactory.createEmptyBorder(3,0,5,0));
        thirdPanel.setLayout(new GridLayout(1,2,3,3) );
        
        JButton addLPButton = new JButton(" Add ");
        addLPButton.setToolTipText("Add a domain");
        addLPButton.setFocusPainted(false);
        addLPButton.setFont(new Font ("Dialog", 1, 16));
        addLPButton.setBackground(ProxyLauncher.buttonBackGroundColor);
        addLPButton.setBorder(ProxyLauncher.buttonBorder);
        addLPButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                 addLPButtonActionPerformed(evt);
            }
          }
        );
        thirdPanel.add(addLPButton);
        
        JButton removeLPButton = new JButton(" Remove ");
        removeLPButton.setToolTipText("Remove a domain");
        removeLPButton.setFocusPainted(false);
        removeLPButton.setFont(new Font ("Dialog", 1, 16));
        removeLPButton.setBackground(ProxyLauncher.buttonBackGroundColor);
        removeLPButton.setBorder(ProxyLauncher.buttonBorder);
        removeLPButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                 removeLPButtonActionPerformed(evt);
            }
          }
        );
        thirdPanel.add(removeLPButton);
        
        panel.add(thirdPanel,BorderLayout.SOUTH);
        
       
    }
    
    public boolean check(String text) {
        if (text==null || text.trim().equals("") ) {
             return false;
        }
        else return true;
    }
    
    public void addLPButtonActionPerformed(ActionEvent evt) {
       Point point=parent.getLocation();
       DomainDialog dd=new DomainDialog(parent,point );
       if ( dd.domainTextField.getText() ==null ||
              dd.domainTextField.getText().trim().equals("") )
           return;
      
       domainList.addDomain(dd.domainTextField.getText());
    }
    
    public void removeLPButtonActionPerformed(ActionEvent evt) {
       domainList.removeSelectedDomain();
    }
   
     public void xmlRegistrationFileActionPerformed(ActionEvent ev) {
        try{
            JFileChooser fileChooser = new JFileChooser("./configuration/");
            fileChooser.addChoosableFileFilter(new ScriptFilter("xml"));  
            int returnVal = fileChooser.showOpenDialog(proxyLauncher);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file!=null) {
                   
                    xmlRegistrationFileTextField.setText(file.getAbsolutePath());
                }
            } 
            this.requestFocus();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
      
}

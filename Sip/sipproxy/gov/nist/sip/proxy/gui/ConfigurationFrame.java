/*
 * Configuration.java
 *
 * Created on March 17, 2003, 2:01 PM
 */

package gov.nist.sip.proxy.gui;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import gov.nist.sip.proxy.*;
/**
 *
 * @author  deruelle
 */
public class ConfigurationFrame extends JFrame {
    
    protected JPanel panel;
    protected JTabbedPane tabbedPane;
    
    protected ProxyLauncher proxyLauncher;
    
    protected StackPanel stackPanel;
    protected ConnectionPanel connectionPanel;
    protected DebugPanel debugPanel;
    protected RegistrarPanel registrarPanel;
    protected AuthenticationPanel authenticationPanel;
   //protected ExtensionHandlerPanel extensionHandlerPanel;
    
    /** Creates a new instance of Configuration */
    public ConfigurationFrame(ProxyLauncher proxyLauncher, String title) {
        super(title);
        
        this.proxyLauncher=proxyLauncher;
        
        stackPanel=new StackPanel(this,proxyLauncher);
        connectionPanel=new ConnectionPanel(proxyLauncher);
        debugPanel=new DebugPanel(proxyLauncher);
        registrarPanel=new RegistrarPanel(this,proxyLauncher);
        authenticationPanel=new AuthenticationPanel(proxyLauncher);
       // extensionHandlerPanel=new ExtenstionHandlerPanel();
        
        initComponents();
        
    }
    
      
    public void hideFrame() {
        this.hide();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    public void initComponents() {
        /***************** The main frame ***************************************/
        // width, height
        this.setSize(450,400);
        Container container=this.getContentPane();
        container.setLayout(new BorderLayout() );
        container.setBackground(ProxyLauncher.containerBackGroundColor);
        this.setLocation(0,200);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new AlertFrame("Your changes will not be checked: use the Apply button!!!",
                JOptionPane.WARNING_MESSAGE);
                hideFrame();
            }
        });
        
        /****************** The components    **********************************/
        
        tabbedPane = new JTabbedPane();
        
        container.add(tabbedPane,BorderLayout.CENTER);
        tabbedPane.add("SIP Stack",stackPanel);
        tabbedPane.add("Connections",connectionPanel);
        tabbedPane.add("Debug",debugPanel);
        tabbedPane.add("Registrar",registrarPanel);
        tabbedPane.add("Authentication",authenticationPanel);
        
        
        JButton applyButton = new JButton(" Apply ");
        applyButton.setToolTipText("Apply the changes");
        applyButton.setFocusPainted(false);
        applyButton.setFont(new Font ("Dialog", 1, 16));
        applyButton.setBackground(ProxyLauncher.buttonBackGroundColor);
        applyButton.setBorder(ProxyLauncher.buttonBorder);
        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                 applyButtonActionPerformed(evt);
            }
          }
        );
        
        container.add(applyButton,BorderLayout.SOUTH);
    }
    
  
    public void applyButtonActionPerformed(ActionEvent evt) {
        // We have to build a new Configuration object:
        Configuration configuration=new Configuration();
        
        // Stack
        String proxyStackName=stackPanel.proxyStackNameTextField.getText();
        if (proxyStackName==null || proxyStackName.trim().equals("") ) {
            new AlertFrame("Specify the stack name");
            return;
        }
        else configuration.stackName=proxyStackName;
        String proxyIPAddress=stackPanel.proxyIPAddressTextField.getText();
        if (proxyIPAddress==null || proxyIPAddress.trim().equals("") ) {
            new AlertFrame("Specify the stack IP address");
            return;
        }
        else configuration.stackIPAddress=proxyIPAddress;
        String outboundProxy=stackPanel.outboundProxyTextField.getText();
        if (outboundProxy!=null && !outboundProxy.trim().equals("") ) 
            configuration.outboundProxy=outboundProxy;
        
        Vector domainList=registrarPanel.domainList.domains;
        if (domainList!=null ) 
            configuration.domainList=domainList;
        
        String routerPath=stackPanel.routerClassTextField.getText();
        if (routerPath!=null && !routerPath.trim().equals("") )
            configuration.routerPath=routerPath;
        configuration.registryPort="1099";
        configuration.exportRegistry=true;
        
        // the listening points:
        Hashtable lps=stackPanel.listeningPointsList.lps;
        if (lps==null || lps.isEmpty())  {
            new AlertFrame("Specify at least one listening point");
            return;
        }
        configuration.listeningPoints=lps;
        
        // The connections:
        String maxConnections=connectionPanel.getMaxConnections();
        //System.out.println(maxConnections);
        if (maxConnections!=null && !maxConnections.trim().equals("") )
            configuration.maxConnections=maxConnections;
        String maxServerTransactions=connectionPanel.getMaxServerTransactions();
        if (maxServerTransactions!=null && !maxServerTransactions.trim().equals("") )
            configuration.maxServerTransactions=maxServerTransactions;
        String threadPoolSize=connectionPanel.getThreadPoolSize();
        if (threadPoolSize!=null && !threadPoolSize.trim().equals("") )
            configuration.threadPoolSize=threadPoolSize;
        
        
        // Logging
        configuration.accessLogViaRMI=false;
        configuration.logRMIPort="0";
        configuration.logLifetime="3600";
        
        // Debug
        boolean b=debugPanel.enableDebugCheckBox.isSelected();
        configuration.enableDebug=b;
        String badMessageLogFile=debugPanel.badMessageLogFileTextField.getText();
        if (badMessageLogFile!=null && !badMessageLogFile.trim().equals("") )
            configuration.badMessageLogFile=badMessageLogFile;
        String debugLogFile=debugPanel.debugFileTextField.getText();
        if (debugLogFile!=null && !debugLogFile.trim().equals("") )
            configuration.debugLogFile=debugLogFile;
        String serverLogFile=debugPanel.serverLogFileTextField.getText();
        if (serverLogFile!=null && !serverLogFile.trim().equals("") )
            configuration.serverLogFile=serverLogFile;
        String outputProxyFile=debugPanel.outputProxyFileTextField.getText();
        if (outputProxyFile!=null && !outputProxyFile.trim().equals("") )
            configuration.outputProxy=outputProxyFile;
        
    
        // Authentication
        b=authenticationPanel.authenticationCheckBox.isSelected();
        configuration.enableAuthentication=b;
        configuration.method="digest";
        String className=authenticationPanel.classNameTextField.getText();
        if (className!=null && !className.trim().equals("") )
            configuration.classFile=className;
        else {
            new AlertFrame("Specify the method class name");
            return;
        }
        String passwordsFile=authenticationPanel.passwordsFileTextField.getText();
        if (passwordsFile!=null && !passwordsFile.trim().equals("") )
            configuration.passwordsFile=passwordsFile;
    
        
        // Registrations:
        b=registrarPanel.enableRegistrationsCheckBox.isSelected();
        // Presence server
        configuration.enablePresenceServer=registrarPanel.enablePresenceServerCheckBox.isSelected();
        
        configuration.enableRegistrations=b;
        String registrationsFile=registrarPanel.xmlRegistrationFileTextField.getText();
        if (registrationsFile!=null && !registrationsFile.trim().equals("") )
            configuration.registrationsFile=registrationsFile;
        else {
            new AlertFrame("Specify the registrations File");
            return;
        }
        
        String expiresTime=registrarPanel.registrationTimeOutTextField.getText();
        if (expiresTime!=null && !expiresTime.trim().equals("") ) {
            try{
                configuration.expiresTime=Integer.valueOf
			(expiresTime.trim()).intValue();
            }
            catch(Exception e) {
            }
       }
        
       writeXMLConfiguration(proxyLauncher.getConfigurationFile(),configuration);
       
       this.setVisible(false);
       
    }
    
    
    public void writeXMLConfiguration(String confFile,Configuration conf){
        try{
            String text=ProxyConfigurationHandler.createTags(conf);
            ProxyConfigurationHandler.writeFile(confFile,text);
            ProxyDebug.println("DEBUG, the changes have been written to the XML file.");
        }
        catch(Exception e) {
            new AlertFrame("ERROR, unable to save the properties!");
        }
    }
    
    
}

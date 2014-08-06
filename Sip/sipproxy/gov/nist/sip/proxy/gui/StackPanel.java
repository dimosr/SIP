/*
 * StackPanel.java
 *
 * Created on March 18, 2003, 11:41 AM
 */

package gov.nist.sip.proxy.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import gov.nist.sip.proxy.*;
/**
 *
 * @author  deruelle
 */
public class StackPanel extends JPanel {
    
    protected JLabel proxyStackNameLabel;
    protected JLabel proxyIPAddressLabel;
    protected JLabel outboundProxyLabel;
    protected JLabel routerClassLabel;
  
    protected JTextField proxyStackNameTextField;
    protected JTextField proxyIPAddressTextField ;
    protected JTextField outboundProxyTextField;  
    protected JTextField routerClassTextField;
   
    protected JPanel firstPanel;
    protected JPanel thirdPanel;
    
    protected ListeningPointsList listeningPointsList;
 
    protected JButton submitButton;   
    protected ProxyLauncher proxyLauncher;
    protected ConfigurationFrame parent;
    
    /** Creates new form SIPHeadersParametersFrame */
    public StackPanel(ConfigurationFrame configurationFrame,ProxyLauncher proxyLauncher) {
        super();
        this.parent=configurationFrame;
        this.proxyLauncher=proxyLauncher;
        
    
        listeningPointsList=new ListeningPointsList(proxyLauncher);
        
        
        initComponents();
     
        // Init the components input:
        try{
            Configuration configuration=proxyLauncher.getConfiguration();
            if (configuration==null) return;
            if (configuration.stackName!=null)
                proxyStackNameTextField.setText(configuration.stackName);
            if (configuration.stackIPAddress!=null)
                proxyIPAddressTextField.setText(configuration.stackIPAddress);
            
            if (configuration.outboundProxy!=null)
                outboundProxyTextField.setText(configuration.outboundProxy);
            if (configuration.routerPath!=null )
                routerClassTextField.setText(configuration.routerPath);
            if (configuration==null)
                listeningPointsList.displayList(new Hashtable());
            else
                listeningPointsList.displayList(configuration.listeningPoints);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getProxyStackNameProperty() {
        return proxyStackNameTextField.getText();
    }
    
    public String getProxyIPAddressProperty() {
        return proxyIPAddressTextField.getText();
    }
    
    public String getOutboundProxyProperty() {
        return outboundProxyTextField.getText();
    }
   
    /*
    public String getRouterProperty() {
        return routerTextField.getText();
    }
    */
    public boolean hasProperties() {
         //String routerText=routerTextField.getText();
         String proxyStackNameText=proxyStackNameTextField.getText();
         String proxyIPAddressText=proxyIPAddressTextField.getText();
        
         if (// check(routerText) &&
              check(proxyStackNameText) &&
              check(proxyIPAddressText) 
          
            ) {
                    return true;
         }
         else   return false;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    public void initComponents() {
       
        /****************** The components    **********************************/
        firstPanel=new JPanel();
        firstPanel.setBorder(BorderFactory.createEmptyBorder(10,5,5,2));
        // If put to False: we see the container's background
        firstPanel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        firstPanel.setLayout( new GridLayout(4,2,3,3) );
        this.setLayout( new GridLayout(2,1,3,3) );
        this.add(firstPanel);
        
        proxyStackNameLabel=new JLabel("Proxy stack name:");
        proxyStackNameLabel.setToolTipText("The name of the stack to set");
        // Alignment of the text
        proxyStackNameLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        proxyStackNameLabel.setForeground(Color.black);
        // Size of the text
        proxyStackNameLabel.setFont(new Font ("Dialog", 1, 12));
        // If put to true: we see the label's background
        proxyStackNameLabel.setOpaque(true);
        proxyStackNameLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        proxyStackNameLabel.setBorder(ProxyLauncher.labelBorder);
        proxyStackNameTextField = new JTextField(20);
        proxyStackNameTextField.setHorizontalAlignment(AbstractButton.CENTER);
        proxyStackNameTextField.setFont(new Font ("Dialog", 0, 14));
        proxyStackNameTextField.setBackground(ProxyLauncher.textBackGroundColor);
        proxyStackNameTextField.setForeground(Color.black);
        proxyStackNameTextField.setBorder(BorderFactory.createLoweredBevelBorder() );
        firstPanel.add(proxyStackNameLabel);
        firstPanel.add(proxyStackNameTextField);
        
        proxyIPAddressLabel=new JLabel("Proxy IP address:");
        proxyIPAddressLabel.setToolTipText("The address of the proxy to set");
        // Alignment of the text
        proxyIPAddressLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        proxyIPAddressLabel.setForeground(Color.black);
        // Size of the text
        proxyIPAddressLabel.setFont(new Font ("Dialog", 1, 12));
        // If put to true: we see the label's background
        proxyIPAddressLabel.setOpaque(true);
        proxyIPAddressLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        proxyIPAddressLabel.setBorder(ProxyLauncher.labelBorder);
        proxyIPAddressTextField = new JTextField(20);
        proxyIPAddressTextField.setHorizontalAlignment(AbstractButton.CENTER);
        proxyIPAddressTextField.setFont(new Font ("Dialog", 0, 14));
        proxyIPAddressTextField.setBackground(ProxyLauncher.textBackGroundColor);
        proxyIPAddressTextField.setForeground(Color.black);
        proxyIPAddressTextField.setBorder(BorderFactory.createLoweredBevelBorder() );
        firstPanel.add(proxyIPAddressLabel);
        firstPanel.add(proxyIPAddressTextField);
        
        outboundProxyLabel=new JLabel("Next hop (IP:port/protocol):");
        outboundProxyLabel.setToolTipText("Location where the message will be sent "+
        "if all the resolutions (DNS, router,...) fail. If not set: 404 will be replied");
        // Alignment of the text
        outboundProxyLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        outboundProxyLabel.setForeground(Color.black);
        // Size of the text
        outboundProxyLabel.setFont(new Font ("Dialog", 1, 12));
        // If put to true: we see the label's background
        outboundProxyLabel.setOpaque(true);
        outboundProxyLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        outboundProxyLabel.setBorder(ProxyLauncher.labelBorder);
        outboundProxyTextField = new JTextField(20);
        outboundProxyTextField.setHorizontalAlignment(AbstractButton.CENTER);
        outboundProxyTextField.setFont(new Font ("Dialog", 0, 14));
        outboundProxyTextField.setBackground(ProxyLauncher.textBackGroundColor);
        outboundProxyTextField.setForeground(Color.black);
        outboundProxyTextField.setBorder(BorderFactory.createLoweredBevelBorder() );
        firstPanel.add(outboundProxyLabel);
        firstPanel.add(outboundProxyTextField);
        
        routerClassLabel=new JLabel("The Router class name:");
        routerClassLabel.setToolTipText("The class name (full java package name) of the router"+
        " used to forward the messages");
        // Alignment of the text
        routerClassLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        routerClassLabel.setForeground(Color.black);
        // Size of the text
        routerClassLabel.setFont(new Font ("Dialog", 1, 12));
        // If put to true: we see the label's background
        routerClassLabel.setOpaque(true);
        routerClassLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        routerClassLabel.setBorder(ProxyLauncher.labelBorder);
        routerClassTextField = new JTextField(20);
        routerClassTextField.setHorizontalAlignment(AbstractButton.CENTER);
        routerClassTextField.setFont(new Font ("Dialog", 0, 12));
        routerClassTextField.setBackground(ProxyLauncher.textBackGroundColor);
        routerClassTextField.setForeground(Color.black);
        routerClassTextField.setBorder(BorderFactory.createLoweredBevelBorder() );
        firstPanel.add(routerClassLabel);
        firstPanel.add(routerClassTextField);
        
       
       
        JPanel panel=new JPanel();
        // top, left, bottom, right
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,2));
        // If put to False: we see the container's background
        panel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        panel.setLayout( new BorderLayout() );
        this.add(panel);
        
        JLabel lpLabel=new JLabel("Listening points list:");
        lpLabel.setVisible(true);
        lpLabel.setToolTipText("The listening points of the proxy");
        lpLabel.setHorizontalAlignment(AbstractButton.CENTER);
        lpLabel.setForeground(Color.black);
        lpLabel.setFont(new Font ("Dialog", 1, 12));
        lpLabel.setOpaque(true);
        lpLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        lpLabel.setBorder(ProxyLauncher.labelBorder);
        panel.add(lpLabel,BorderLayout.NORTH);
   
        //this.add(listeningPointsList);
        JScrollPane scrollPane = new JScrollPane(listeningPointsList,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane,BorderLayout.CENTER);
        
        thirdPanel = new JPanel();
        thirdPanel.setOpaque(false);
        // top, left, bottom, right
        thirdPanel.setBorder(BorderFactory.createEmptyBorder(3,0,5,0));
        thirdPanel.setLayout(new GridLayout(1,2,3,3) );
        
        JButton addLPButton = new JButton(" Add ");
        addLPButton.setToolTipText("Add a listening point");
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
        removeLPButton.setToolTipText("Remove a listening point");
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
       ListeningPointDialog lpd=new ListeningPointDialog(parent,point );
       if ( lpd.transportTextField.getText() ==null ||
              lpd.transportTextField.getText().trim().equals("") )
           return;
       else {
             if ( lpd.portTextField.getText() ==null ||
                  lpd.portTextField.getText().trim().equals("") )
               return;
       }
       listeningPointsList.addListeningPoint(lpd.portTextField.getText(),
       lpd.transportTextField.getText());
    }
    
    public void removeLPButtonActionPerformed(ActionEvent evt) {
       listeningPointsList.removeSelectedListeningPoint();
    }
       
}


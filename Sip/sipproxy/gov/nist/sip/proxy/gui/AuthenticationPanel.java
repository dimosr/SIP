/*
 * LoggingFeaturesFrame.java
 *
 * Created on April 15, 2002, 1:54 PM
 */

package gov.nist.sip.proxy.gui;


import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import gov.nist.sip.proxy.*;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class AuthenticationPanel extends JPanel {
    
    protected JPanel firstPanel;
    protected JPanel secondPanel;
    protected JPanel thirdPanel;
    
    protected JLabel authenticationLabel;
    protected JLabel passwordsFileLabel;
    protected JLabel classNameLabel;
    
    protected JButton passwordsFileButton;
    protected JTextField passwordsFileTextField;
    protected JTextField classNameTextField;
    protected JCheckBox authenticationCheckBox;
    protected JCheckBox digestCheckBox;
    
    protected JButton passwordFileButton;
    protected JButton submitButton;
    
    protected ProxyLauncher proxyLauncher;
    
    /** Creates new RegistrationAuthenticationFrame */
    public AuthenticationPanel(ProxyLauncher proxyLauncher) {
       
        this.proxyLauncher=proxyLauncher;
        
       
        initComponents();
        
        // Init the components input:
        try{
            Configuration configuration=proxyLauncher.getConfiguration();
            //System.out.println(configuration.classFile);
            if (configuration==null) return;
            if (configuration.enableAuthentication)
               authenticationCheckBox.setSelected(true);
            else authenticationCheckBox.setSelected(false);
            if (configuration.classFile!=null)
                classNameTextField.setText(configuration.classFile);
            if (configuration.passwordsFile!=null )
                passwordsFileTextField.setText(configuration.passwordsFile);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
 
    
    public String getPasswordsFileProperty() {
        return passwordsFileTextField.getText();
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
        Container container=this.getParent();
        firstPanel.setLayout(  new GridLayout(5,2,2,4) );
       
        authenticationCheckBox = new JCheckBox("Enable Authentication");
        authenticationCheckBox.setBorderPainted(true);
        authenticationCheckBox.setBorder(ProxyLauncher.labelBorder);
        authenticationCheckBox.setSelected(true);
        authenticationCheckBox.setFont(new Font("Dialog", 1, 12));
        authenticationCheckBox.setBackground(ProxyLauncher.labelBackGroundColor);
        firstPanel.add(authenticationCheckBox);
        
        JCheckBox ghostCheckBox = new JCheckBox("");
        ghostCheckBox.setVisible(false);
        firstPanel.add(ghostCheckBox);
 
        authenticationLabel=new JLabel("Authentication scheme:");
        authenticationLabel.setToolTipText("The type of authentication method");
        authenticationLabel.setForeground(Color.black);
        authenticationLabel.setFont(new Font("Dialog", 1, 12));
        authenticationLabel.setOpaque(true);
        authenticationLabel.setHorizontalAlignment(AbstractButton.CENTER);
        authenticationLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        authenticationLabel.setBorder(ProxyLauncher.labelBorder);
        firstPanel.add(authenticationLabel);
        
               
          
        digestCheckBox = new JCheckBox("Digest");
        digestCheckBox.setBorderPainted(true);
        digestCheckBox.setBorder(ProxyLauncher.labelBorder);
        digestCheckBox.setSelected(true);
        digestCheckBox.setFont(new Font("Dialog", 1, 14));
        digestCheckBox.setBackground(ProxyLauncher.labelBackGroundColor);
        firstPanel.add(digestCheckBox);
        
        
        
        classNameLabel=new JLabel("Method client class:");
        classNameLabel.setToolTipText("The class to use");
   
        classNameLabel.setForeground(Color.black);
        classNameLabel.setFont(new Font("Dialog", 1, 12));
        classNameLabel.setOpaque(true);
        classNameLabel.setHorizontalAlignment(AbstractButton.CENTER);
        classNameLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        classNameLabel.setBorder(ProxyLauncher.labelBorder);
        firstPanel.add(classNameLabel);
        
        classNameTextField = new JTextField(18);
        classNameTextField.setEditable(true);
        classNameTextField.setFont(new Font("Dialog", 0, 12));
        classNameTextField.setForeground(Color.black);
        classNameTextField.setBorder(BorderFactory.createLoweredBevelBorder() );
        firstPanel.add(classNameTextField);
        
       
      
        passwordsFileLabel=new JLabel("Authentication file:");
        passwordsFileLabel.setToolTipText("The XML authentication file:");
        passwordsFileLabel.setHorizontalAlignment(AbstractButton.CENTER);
        passwordsFileLabel.setForeground(Color.black);
        passwordsFileLabel.setFont(new Font("Dialog", 1, 12));
        passwordsFileLabel.setOpaque(true);
        passwordsFileLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        passwordsFileLabel.setBorder(ProxyLauncher.labelBorder);
        passwordsFileTextField = new JTextField(30);
        passwordsFileTextField.setEditable(false);
        passwordsFileTextField.setHorizontalAlignment(AbstractButton.CENTER);
        passwordsFileTextField.setFont(new Font("Dialog", 0, 14));
        passwordsFileTextField.setForeground(Color.black);
        passwordsFileTextField.setBorder(BorderFactory.createLoweredBevelBorder() );
        passwordsFileButton=new JButton("Choose");
        passwordsFileButton.setToolTipText("Choose the authentication file");
        passwordsFileButton.setFont(new Font("Dialog", 0, 12));
        passwordsFileButton.setFocusPainted(false);
        passwordsFileButton.setBackground(ProxyLauncher.buttonBackGroundColor);
        passwordsFileButton.setBorder(ProxyLauncher.buttonBorder);
        passwordsFileButton.setVerticalAlignment(AbstractButton.CENTER);
        passwordsFileButton.setHorizontalAlignment(AbstractButton.CENTER);
        passwordsFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                passwordsFileActionPerformed(evt);
            }
        }
        );
        firstPanel.add(passwordsFileLabel);
        firstPanel.add(passwordsFileButton);
        this.add(firstPanel);
        
        secondPanel=new JPanel();
        secondPanel.setBorder(BorderFactory.createEmptyBorder(0,20,0,20));
        // If put to False: we see the container's background
        secondPanel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        secondPanel.setLayout( new GridLayout(1,1,0,0) );
        this.add(secondPanel);
        secondPanel.add(passwordsFileTextField);
        
       
        
        
    }
    
    public boolean checkDigestCheckBox(){
        if ( digestCheckBox.isSelected() ) return true;
        else {
            new AlertFrame("The digest scheme has to be checked!!!",JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean check(String text) {
        if (text==null || text.trim().equals("") ) {
             return false;
        }
        else return true;
    }
    
    
    public void passwordsFileActionPerformed(ActionEvent ev) {
        try{
            JFileChooser fileChooser = new JFileChooser("./configuration/");
            fileChooser.addChoosableFileFilter(new ScriptFilter("xml"));
            int returnVal = fileChooser.showOpenDialog(proxyLauncher);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file!=null) {
                   
                    passwordsFileTextField.setText(file.getAbsolutePath());
                }
               
            }
             this.requestFocus();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
}

/*
 * AuthenticationDialog.java
 *
 * Created on January 7, 2003, 6:08 PM
 */

package gov.nist.sip.instantmessaging;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
/**
 *
 * @author  olivier deruelle
 */
public class AuthenticationDialog {
    
    private JLabel realmLabel;
    private JLabel realmLabelContent;
    private JLabel userNameLabel;
    private JLabel passwordLabel;
    private JButton submitButton;
    private JDialog dialog;
    private JTextField userNameTextField;
    private JPasswordField passwordTextField;
    private boolean STOP=false;
   
    /** Creates a new instance of AuthenticationDialog */
    public AuthenticationDialog(Frame parent,String realm) {
        
        if (parent==null) parent=new Frame();
            dialog= new JDialog(parent,"Authentication",true);
        
        // width, height
        dialog.setSize(200,150) ;	
        //rows, columns, horizontalGap, verticalGap
        dialog.getContentPane().setLayout( new BoxLayout(dialog.getContentPane(), 1));
        dialog.setBackground(InstantMessagingGUI.containerBackGroundColor);
        
        JPanel firstPanel=new JPanel();
        firstPanel.setBorder(BorderFactory.createEmptyBorder(10,4,10,4));
        // If put to False: we see the container's background
        firstPanel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        firstPanel.setLayout( new GridLayout(3,2,0,2) );
        dialog.getContentPane().add(firstPanel);
        
        realmLabel = new JLabel("realm:");
        realmLabelContent = new JLabel(realm);
        firstPanel.add(realmLabel);
        firstPanel.add(realmLabelContent);
        
        userNameLabel = new JLabel("username:");
        userNameTextField = new JTextField(20);
        firstPanel.add(userNameLabel);
        firstPanel.add(userNameTextField);
        
        passwordLabel = new JLabel("password:");
        passwordTextField = new JPasswordField(20);
        passwordTextField.setEchoChar('*');
        firstPanel.add(passwordLabel);
        firstPanel.add(passwordTextField);
        
        JPanel thirdPanel = new JPanel();
        thirdPanel.setOpaque(false);
        thirdPanel.setLayout(new FlowLayout(FlowLayout.CENTER) );
       
        submitButton = new JButton(" OK ");
        submitButton.setToolTipText("Submit your changes!");
        submitButton.setFocusPainted(false);
        submitButton.setFont(new Font ("Dialog", 1, 14));
        submitButton.setBackground(InstantMessagingGUI.buttonBackGroundColor);
        submitButton.setBorder(InstantMessagingGUI.buttonBorder);
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                 okButtonActionPerformed(evt);
            }
        }
        );
        thirdPanel.add(submitButton);
        dialog.getContentPane().add(thirdPanel);
         
        dialog.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                clean();
            }
        }
        );
         
        
         dialog.show();
    }
    
    public void  clean() {
         dialog.setVisible(false) ;	
         STOP=true;
         dialog.dispose();
    }
    
    public boolean isStop() {
        return STOP;
    }   
    
    public void okButtonActionPerformed(ActionEvent evt) {
         if (userNameTextField.getText() ==null || userNameTextField.getText().trim().equals("") )
             new AlertInstantMessaging("You must enter an user name!!!",
             JOptionPane.ERROR_MESSAGE);
         else {
             char[] pass= passwordTextField.getPassword();
             
             if ( pass ==null )
                 new AlertInstantMessaging("You must enter a password!!!",
                 JOptionPane.ERROR_MESSAGE);
             else {
                 String s=new String(pass);
                 if ( s.trim().equals("") ) 
                      new AlertInstantMessaging("You must enter a password!!!",
                      JOptionPane.ERROR_MESSAGE);
                 else 
                    dialog.setVisible(false) ;
             }
         }
    }

    
    public String getUserName() {
        return userNameTextField.getText().trim();
    }
    
    public String getPassword() {
        char[] pass= passwordTextField.getPassword();
        String s=new String(pass);
        return s.trim();
    }
    
}
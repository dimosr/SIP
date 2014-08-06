/*
 * DomainDialog.java
 *
 * Created on April 11, 2003, 12:48 PM
 */

package gov.nist.sip.proxy.gui;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
/**
 *
 * @author  deruelle
 */
public class DomainDialog {
    
    protected JLabel domainLabel;
    protected JButton submitButton;
    protected JDialog dialog;
    protected JTextField domainTextField;
    
    /** Creates a new instance of AuthenticationDialog */
    public DomainDialog(Frame parent,Point point) {
        
        if (parent==null) parent=new Frame();
            dialog= new JDialog(parent,"Domain",true);
        
        // width, height
        dialog.setSize(180,100) ;	
        //rows, columns, horizontalGap, verticalGap
        dialog.getContentPane().setLayout( new BoxLayout(dialog.getContentPane(), 1));
        dialog.setBackground(ProxyLauncher.containerBackGroundColor);
        
        JPanel firstPanel=new JPanel();
        firstPanel.setBorder(BorderFactory.createEmptyBorder(10,4,2,4));
        // If put to False: we see the container's background
        firstPanel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        firstPanel.setLayout( new GridLayout(1,2,0,2) );
        dialog.getContentPane().add(firstPanel);
        
        domainLabel = new JLabel("domain:");
        domainLabel.setToolTipText("Specify a domain");
        domainTextField = new JTextField(10);
        firstPanel.add(domainLabel);
        firstPanel.add(domainTextField);
        
       
        JPanel thirdPanel = new JPanel();
        thirdPanel.setOpaque(false);
        thirdPanel.setLayout(new FlowLayout(FlowLayout.CENTER) );
       
        submitButton = new JButton(" OK ");
        submitButton.setToolTipText("Submit your changes!");
        submitButton.setFocusPainted(false);
        submitButton.setFont(new Font ("Dialog", 1, 14));
        submitButton.setBackground(ProxyLauncher.buttonBackGroundColor);
        submitButton.setBorder(ProxyLauncher.buttonBorder);
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
            }
        }
        );
         
        dialog.setLocation(point.x,point.y);
        dialog.show();
    }
    
    public boolean checkDomain(String domain) {
        try {
            if (domain ==null || domain.trim().equals("")) {
                new AlertFrame("Specify a domain!");
                return false;
            }
            return true;
        }
        catch(Exception e) {
            new AlertFrame("The domain is required!");
            return false;
        }
    }
    
   

    public void okButtonActionPerformed(ActionEvent evt) {
         if ( checkDomain(domainTextField.getText() ) )
         
                    dialog.setVisible(false) ;
         
    }

}
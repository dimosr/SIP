/*
 * AuthenticationDialog.java
 *
 * Created on January 7, 2003, 6:08 PM
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
 * @author  olivier deruelle
 */
public class ListeningPointDialog {
    
    protected JLabel portLabel;
    protected JLabel transportLabel;
    protected JButton submitButton;
    protected JDialog dialog;
    protected JTextField portTextField;
    protected JTextField transportTextField;
   
    /** Creates a new instance of AuthenticationDialog */
    public ListeningPointDialog(Frame parent,Point point) {
        
        if (parent==null) parent=new Frame();
            dialog= new JDialog(parent,"Listening point",true);
        
        // width, height
        dialog.setSize(150,150) ;	
        //rows, columns, horizontalGap, verticalGap
        dialog.getContentPane().setLayout( new BoxLayout(dialog.getContentPane(), 1));
        dialog.setBackground(ProxyLauncher.containerBackGroundColor);
        
        JPanel firstPanel=new JPanel();
        firstPanel.setBorder(BorderFactory.createEmptyBorder(10,4,2,4));
        // If put to False: we see the container's background
        firstPanel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        firstPanel.setLayout( new GridLayout(3,2,0,2) );
        dialog.getContentPane().add(firstPanel);
        
        portLabel = new JLabel("port:");
        portLabel.setToolTipText("Specify a port number");
        portTextField = new JTextField(10);
        firstPanel.add(portLabel);
        firstPanel.add(portTextField);
        
        transportLabel = new JLabel("transport:");
        transportLabel.setToolTipText("Specify UDP or TCP");
        transportTextField = new JTextField(10);
        firstPanel.add(transportLabel);
        firstPanel.add(transportTextField);
        
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
    
    public boolean checkLPPort(String port) {
        try {
            if (port ==null || port.trim().equals("")) {
                new AlertFrame("Specify a port number!");
                return false;
            }
            Integer.valueOf(port);
            return true;
        }
        catch(Exception e) {
            new AlertFrame("The port is a number!");
            return false;
        }
    }
    
     public boolean checkLPTransport(String transport) {
        try {
            if (transport ==null || 
                transport.trim().equals("") ){
                    new AlertFrame("Specify a transport parameter!");
                    return false;
            }
            if (transport.trim().compareToIgnoreCase("UDP")==0 ||
                transport.trim().compareToIgnoreCase("TCP") ==0
                )  return true;
            else {
                new AlertFrame("Specify UDP or TCP!");
                return false;
            }
          
        }
        catch(Exception e) {
            return false;
        }
    }

    public void okButtonActionPerformed(ActionEvent evt) {
         if ( checkLPPort(portTextField.getText()) &&
             checkLPTransport(transportTextField.getText())
         )
                             dialog.setVisible(false) ;
         
    }

}
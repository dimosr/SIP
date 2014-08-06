/*
 * RemoteSipURLFrame.java
 *
 * Created on September 25, 2002, 10:27 AM
 */

package gov.nist.sip.instantmessaging;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.* ;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class RemoteSipURLFrame extends JFrame {
    
    private InstantMessagingGUI imGUI;
    
    private JPanel firstPanel;
    private JPanel secondPanel;
    
    private JLabel remoteSipURLLabel;
    private JTextField remoteSipURLTextField;
    
    private JButton submitButton;
    
    
    /** Creates new RemoteSipURLFrame */
    public RemoteSipURLFrame(InstantMessagingGUI imGUI) {
        super("Buddy contact sip URL");
        this.imGUI=imGUI;
        initComponents();
        show();
    }

      /** This method is called from within the constructor to
     * initialize the form.
     */
    public void initComponents() {
        /***************** The main frame ***************************************/
        // width, height
        this.setSize(440,130);
        Container container=this.getContentPane();
        container.setLayout(new BorderLayout());
        container.setBackground(InstantMessagingGUI.containerBackGroundColor);
        Point point=imGUI.getLocation();
        this.setLocation(point.x,point.y);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            }
        });
        
        /****************** The components    **********************************/
        firstPanel=new JPanel();
        //Top,,bottom,
        firstPanel.setBorder(BorderFactory.createEmptyBorder(20,10,10,10));
        // If put to False: we see the container's background
        firstPanel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        firstPanel.setLayout( new GridLayout(1,2,5,10) );

      
        remoteSipURLLabel=new JLabel("Enter the buddy sip URL:");
        remoteSipURLLabel.setToolTipText("Your contact: (format: sip:user@nist.gov)");
        // Alignment of the text
        remoteSipURLLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        remoteSipURLLabel.setForeground(Color.black);
        // Size of the text
        remoteSipURLLabel.setFont(new Font ("Dialog", 1, 14));
        // If put to true: we see the label's background
        remoteSipURLLabel.setOpaque(true);
        remoteSipURLLabel.setBackground(InstantMessagingGUI.labelBackGroundColor);
        remoteSipURLLabel.setBorder(InstantMessagingGUI.labelBorder);
       
        remoteSipURLTextField = new JTextField(20);
        remoteSipURLTextField.setHorizontalAlignment(JTextField.LEFT);
        remoteSipURLTextField.setFont(new Font ("Dialog", 1, 16));
        remoteSipURLTextField.setBackground(InstantMessagingGUI.textBackGroundColor);
        remoteSipURLTextField.setForeground(Color.black);
        remoteSipURLTextField.setText("sip:");
        remoteSipURLTextField.setSelectionStart(4);
        remoteSipURLTextField.setBorder(BorderFactory.createLoweredBevelBorder() );
        firstPanel.add(remoteSipURLLabel);
        firstPanel.add(remoteSipURLTextField);
     
        secondPanel = new JPanel();
        secondPanel.setOpaque(false);
        secondPanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,10) );
        secondPanel.add(Box.createHorizontalGlue());
        
        submitButton = new JButton("  OK  ");
        submitButton.setToolTipText("Start the chat session!");
        submitButton.setFocusPainted(false);
        submitButton.setFont(new Font ("Dialog", 1, 16));
        submitButton.setBackground(InstantMessagingGUI.buttonBackGroundColor);
        submitButton.setBorder(InstantMessagingGUI.buttonBorder);
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                 submitButtonActionPerformed(evt);
            }
          }
        );
        

        secondPanel.add(submitButton);
        container.add("Center",firstPanel);
        container.add("South",secondPanel);
    }

    public String getRemoteSipURL() {
        try{
            String text=remoteSipURLTextField.getText(); 
            if (text==null || text.trim().equals("") || text.trim().equals("sip:")  ) {
                new AlertInstantMessaging("You must set the contact sip URL!!!",JOptionPane.ERROR_MESSAGE);
                return null;
            }
            else {
                text=text.trim();
                if (text.startsWith("sip:")) {
                       return text;
                }
                else {
                    return ("sip:"+text);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
    
    
    public void submitButtonActionPerformed(ActionEvent evt) {
        // We have to check if the fields are correct:
        String remoteSipURL=getRemoteSipURL();
        if (remoteSipURL!=null) {
            this.hide();
            ListenerInstantMessaging listenerInstantMessaging=imGUI.getListenerInstantMessaging();
            ChatSessionManager chatSessionManager=listenerInstantMessaging.getChatSessionManager();
            if (chatSessionManager.hasAlreadyChatSession(remoteSipURL) ) { 
                // This chat session already exists, we put the focus on it:
                ChatSession chatSession=chatSessionManager.getChatSession(remoteSipURL);
                ChatFrame chatFrame=chatSession.getChatFrame();
                chatFrame.show();
            }
            else {
                ChatFrame chatFrame=new ChatFrame(imGUI,remoteSipURL);
                ChatSession chatSession=new ChatSession();
                chatSession.setChatFrame(chatFrame);
                chatFrame.setChatSession(chatSession);
                chatSessionManager.addChatSession(chatSession);
            }
        }
    }

}

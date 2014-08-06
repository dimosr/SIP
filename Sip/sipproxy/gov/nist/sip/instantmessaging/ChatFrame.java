/*
 * ChatFrame.java
 *
 * Created on September 25, 2002, 9:47 AM
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
public class ChatFrame extends JFrame {

    private InstantMessagingGUI imGUI;
    
    private JPanel firstPanel;
    private JPanel secondPanel;
    private JPanel thirdPanel;
    
    private JLabel remoteUserLabel;
    private JLabel textToSendLabel;
    private JLabel infoLabel;

    private JTextArea chatTextArea;  
    private JTextArea messageTextArea;
    private String contact;
    private JButton sendButton;
    
    private ChatSession chatSession;
    
    /** Creates new ChatFrame */
    public ChatFrame(InstantMessagingGUI imGUI,String contact) {
        super("Chat window");
        this.imGUI=imGUI;
        this.contact=contact;
        chatSession=null;
        initComponents();
        show();
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setChatSession(ChatSession chatSession) {
        this.chatSession=chatSession;
    }
    
    public JTextArea getMessageTextArea() {
        return messageTextArea;
    }
    
    public InstantMessagingGUI getInstantMessagingGUI() {
        return imGUI;
    }
    
    public void removeSentText() {
        messageTextArea.setText("");   
    }
    
    public void setInfo(String text) {
        infoLabel.setText(text);
    }
    
    public void displayRemoteText(String text) {
        String oldText=chatTextArea.getText();
        String newText;
        chatTextArea.setFont(new Font("Helvetica", Font.BOLD, 14));
        
        if (oldText==null || oldText.trim().equals("") )
            newText=contact+" says:\n"+text+"\n";
        else  newText=contact+" says:\n"+text+"\n\n"+oldText;
        
        chatTextArea.setSelectionColor(Color.black);
        chatTextArea.setText(newText);
    }
    
        
    public void displayLocalText(String text) {
        String oldText=chatTextArea.getText();
        String newText;
        chatTextArea.setFont(new Font("Helvetica", Font.BOLD, 14) );
        
        if (oldText==null || oldText.trim().equals("") )
            newText="You say:\n"+text+"\n";
        else  newText="You say:\n"+text+"\n\n"+oldText;
        
        chatTextArea.setSelectionColor(Color.black);
        chatTextArea.setText(newText);
        
        messageTextArea.setText("");
    }
    
    
    
    

      /** This method is called from within the constructor to
     * initialize the form.
     */
    public void initComponents() {
        /***************** The main frame ***************************************/
        // width, height
        this.setSize(550,400);
        Container container=this.getContentPane();
        container.setLayout(new BorderLayout());
        container.setBackground(InstantMessagingGUI.containerBackGroundColor);
        Point point=imGUI.getLocation();
        this.setLocation(point.x,point.y);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              clean();
            }
        });
        
        /****************** The components    **********************************/
        firstPanel=new JPanel();
        //Top,,bottom,
        firstPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        // If put to False: we see the container's background
        firstPanel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        firstPanel.setLayout( new GridLayout(1,1,5,10) );
        
        remoteUserLabel=new JLabel("Chatting session with "+contact);
        remoteUserLabel.setToolTipText("Your contact!");
        // Alignment of the text
        remoteUserLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        remoteUserLabel.setForeground(Color.black);
        // Size of the text
        remoteUserLabel.setFont(new Font ("Dialog", 1, 14));
        // If put to true: we see the label's background
        remoteUserLabel.setOpaque(true);
        remoteUserLabel.setBackground(InstantMessagingGUI.labelBackGroundColor);
        remoteUserLabel.setBorder(InstantMessagingGUI.labelBorder);
        firstPanel.add(remoteUserLabel);
        
        chatTextArea = new JTextArea(25, 45);
	chatTextArea.setToolTipText("Messages Received");
	chatTextArea.setEditable(false);
        chatTextArea.setBackground(Color.lightGray);

	// create scroll bars for text areas
	JScrollPane incomingScroller = new JScrollPane(chatTextArea);
	incomingScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	incomingScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        
        infoLabel=new JLabel("");
        // Alignment of the text
        infoLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        infoLabel.setForeground(Color.black);
        // Size of the text
        infoLabel.setFont(new Font ("Dialog", 1, 10));
        // If put to true: we see the label's background
        infoLabel.setOpaque(false);
        //infoLabel.setBackground(InstantMessagingGUI.labelBackGroundColor);
        //infoLabel.setBorder(InstantMessagingGUI.labelBorder);
        
        textToSendLabel=new JLabel("Text to send:");
        // Alignment of the text
        textToSendLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        textToSendLabel.setForeground(Color.black);
        // Size of the text
        textToSendLabel.setFont(new Font ("Dialog", 1, 14));
        // If put to true: we see the label's background
        textToSendLabel.setOpaque(true);
        textToSendLabel.setBackground(InstantMessagingGUI.labelBackGroundColor);
        textToSendLabel.setBorder(InstantMessagingGUI.labelBorder);
        
        messageTextArea = new JTextArea(3,2);
	messageTextArea.setEditable(true);
	messageTextArea.setFont(new Font("Helvetica", Font.PLAIN, 14));
	messageTextArea.setSelectedTextColor(Color.blue);
	messageTextArea.setSelectionColor(Color.lightGray);

	// create scroll bars for text areas
	JScrollPane outgoingScroller = new JScrollPane(messageTextArea);
	outgoingScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	outgoingScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        
        secondPanel = new JPanel();
        secondPanel.setOpaque(false);
        // top, left, bottom, right
        secondPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        secondPanel.setLayout( new GridLayout(2,1,0,0) );
       
        sendButton = new JButton("  Send  ");
        sendButton.setToolTipText("Send the message!");
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font ("Dialog", 1, 16));
        sendButton.setBackground(InstantMessagingGUI.buttonBackGroundColor);
        sendButton.setBorder(InstantMessagingGUI.buttonBorder);
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
               chatSession.sendIMActionPerformed(evt);
            }
          }
        );
        
        firstPanel.add(remoteUserLabel);
        
        //secondPanel.add(infoLabel);
        //secondPanel.add(textToSendLabel);
        //secondPanel.add(outgoingScroller);
        //secondPanel.add(sendButton);
        JPanel subPanel2=new JPanel();
        //subPanel2.setBorder(BorderFactory.createEmptyBorder(30,10,0,10));
        // If put to False: we see the container's background
        subPanel2.setOpaque(false);
        subPanel2.setLayout( new GridLayout(2,1,0,0) );
        subPanel2.add(infoLabel);
        subPanel2.add(textToSendLabel);
        
        JPanel subPanel3=new JPanel();
        //subPanel3.setBorder(BorderFactory.createEmptyBorder(30,10,0,10));
        // If put to False: we see the container's background
        subPanel3.setOpaque(false);
        subPanel3.setLayout( new BorderLayout() );
        subPanel3.add("Center",outgoingScroller);
        subPanel3.add("East",sendButton);
        secondPanel.add(subPanel2);
        secondPanel.add(subPanel3);
        
        
        container.add("North",firstPanel);
        container.add("Center",incomingScroller);
        container.add("South",secondPanel);
    }
    
    public void remove() {
        this.dispose();
    }
    
    public void clean() {
        try {
            DebugIM.println("Closing ChatSession....");
            if (chatSession!=null) {
                if (!chatSession.hasExited()) {
                    ListenerInstantMessaging listenerInstantMessaging=imGUI.getListenerInstantMessaging();
                    ChatSessionManager chatSessionManager=listenerInstantMessaging.getChatSessionManager();
                    chatSessionManager.removeChatSession(contact);
                    DebugIM.println("Closed ChatSession....");
                }
            }
            this.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}

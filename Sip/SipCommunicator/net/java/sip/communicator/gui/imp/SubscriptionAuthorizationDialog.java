/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */
package net.java.sip.communicator.gui.imp;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.*;
import net.java.sip.communicator.gui.plaf.*;
import net.java.sip.communicator.common.*;
import javax.swing.text.html.*;
import java.util.*;
import java.awt.event.*;

/**
 * The class is used to request user SubscriptionAuthorisations. When an
 * incoming subscription is received the user must approve or refuse it for
 * notifications to be sent.
 *
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class SubscriptionAuthorizationDialog
    extends JDialog
    implements ActionListener
{
    private static final Console console = Console.getConsole(SubscriptionAuthorizationDialog.class);
    private JPanel contentPane = new JPanel();
    private Border border1;
    private JPanel controlsPane = new JPanel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private GridLayout gridLayout1 = new GridLayout();
    private JComboBox responsesComboBox = new JComboBox();
    private JPanel buttonsPane = new JPanel();
    private JButton okButton = new JButton();
    private FlowLayout flowLayout1 = new FlowLayout();
    private JEditorPane messagePane = new JEditorPane();

    private String messageString1   = "The following user has requested to subscribe for your presence status.";
    private String namePrefix       = "Name: <b>";
    private String nameSuffix       = "</b>";
    private String addressPrefix    = "Address: <b>";
    private String addressSuffix    = "</b>";
    private String messagePrefix    = "Message: ";
    private String messageSuffix    = "";
    private String messageString2   = "What would you like to do?";

    private String name    = null;
    private String address = null;
    JScrollPane messageScrollPane = new JScrollPane();


    public SubscriptionAuthorizationDialog(JFrame owner) throws HeadlessException
    {
        super(owner, true);
        try
        {
            jbInit();
            initComponents();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initComponents()
    {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        messagePane.setBackground(contentPane.getBackground());
        messagePane.setEditorKit(new HTMLEditorKit());
        messagePane.setForeground(contentPane.getForeground());

        setSize(450, 360);

        //center the window
        int x = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() -
                       getWidth()) / 2;
        int y = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() -
                       getHeight()) / 2;
        setLocation(x, y);

        okButton.addActionListener(this);
    }
    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createEmptyBorder(20,20,20,20);
        contentPane.setBorder(border1);
        contentPane.setLayout(borderLayout1);
        controlsPane.setLayout(gridLayout1);
        gridLayout1.setColumns(1);
        gridLayout1.setHgap(10);
        gridLayout1.setRows(0);
        gridLayout1.setVgap(10);
        okButton.setVerifyInputWhenFocusTarget(true);
        okButton.setMnemonic('O');
        okButton.setText("OK");
        buttonsPane.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.CENTER);
        messagePane.setEditable(false);
        messagePane.setText("");
        borderLayout1.setHgap(10);
        borderLayout1.setVgap(10);
        this.setTitle("Subscription Authorization");
        this.getContentPane().add(contentPane, BorderLayout.CENTER);
        contentPane.add(controlsPane,  BorderLayout.SOUTH);
        controlsPane.add(responsesComboBox, null);
        controlsPane.add(buttonsPane, null);
        buttonsPane.add(okButton, null);
        contentPane.add(messageScrollPane,  BorderLayout.CENTER);
        messageScrollPane.getViewport().add(messagePane, null);
    }

    private void setSubscriberDetails(String subscriberName,
                                      String subscriberAddress,
                                      String message)
    {
        this.name     = subscriberName;
        this.address  = subscriberAddress;

        Color color = messagePane.getForeground();

        messagePane.setText(  "<FONT COLOR=\"#"+Integer.toHexString(color.getRGB()).substring(2)+"\">"
                            + messageString1 + "<p>"
                            + namePrefix + name + nameSuffix + "<br>"
                            + addressPrefix + address + addressSuffix + "<p>"
                            + ((message!= null && message.trim().length() >0)
                                    ?messagePrefix + message + messageSuffix + "<p>"
                                    :"")
                            + messageString2
                            + "</FONT>");
    }

    public static String obtainAuthorisationResponse(JFrame owner, SubscriptionRequestUIModel request)
    {
        SubscriptionAuthorizationDialog sad = new SubscriptionAuthorizationDialog(owner);
        sad.setSubscriberDetails(request.getRequestingPartyDisplayName(), request.getRequestingPartyAddress(), request.getReasonPhrase());

        String[] responses = request.getAcceptedResponses();
        Vector resVector = new Vector();

        for(int i = 0; i < responses.length; i++)
            resVector.add(responses[i]);

        sad.responsesComboBox.setModel(new DefaultComboBoxModel(resVector));
        sad.responsesComboBox.setSelectedIndex(0);

        sad.show();
        return sad.responsesComboBox.getSelectedItem().toString();
    }

    public static void main(String[] args)
    {
        initLookAndFeel();
        String response = obtainAuthorisationResponse(null, new SubscriptionRequestUIModel()
        {
            public String[] getAcceptedResponses()
            {
                String[] arr = new String[3];
                arr[0] = "response 1";
                arr[1] = "ouaba daba";
                arr[2] = "ouaba dabaasdfaf";

                return arr;
            }

            public String getRequestingPartyDisplayName()
            {
                return "Emil Ivov";
            }

            public String getRequestingPartyAddress()
            {
                return "emcho@iptel.org";
            }

            public String getReasonPhrase()
            {
                return "Hello, could I please add you to my contact list?";
            }
        });

        System.out.println("Returned response = " + response);
    }

    static void initLookAndFeel()
    {
        MetalLookAndFeel mlf = new MetalLookAndFeel();
        mlf.setCurrentTheme( new SipCommunicatorColorTheme());

        try {
            UIManager.setLookAndFeel(mlf);
        }
        catch (UnsupportedLookAndFeelException ex) {
            console.error("Failed to set custom look and feel", ex);
        }
    }

    public void actionPerformed(ActionEvent evt)
    {
        dispose();
    }
}

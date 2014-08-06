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
package net.java.sip.communicator.common;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/** <p>Title: SIP COMMUNICATOR</p>
 * <p>Description:JAIN-SIP Audio/Video phone application</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr) </p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
 * <p>Louis Pasteur University - Strasbourg - France</p>
 * @author Emil Ivov (http://www.emcho.com)
 * @version 1.1
 *
 */
class ConsoleUI
    extends JDialog
{
    static final String ERROR_ICON = "Error.gif";
    static final String MESSAGE_ICON = "Inform.gif";
    private static Icon errorIcon = null;
    private BorderLayout borderLayout1 = new BorderLayout();
    private JScrollPane detailsScroll = new JScrollPane();
    private JPanel messagePane = new JPanel();
    private BorderLayout borderLayout2 = new BorderLayout();
    private JPanel buttonPane = new JPanel();
    private BorderLayout borderLayout3 = new BorderLayout();
    private Border border1;
    private Border border2;
    private Border border3;
    private JTextArea detailsTextArea = new JTextArea();
    private JPanel buttonsEastPane = new JPanel();
    private JButton closeButton = new JButton();
    private JButton messageButton = new JButton();
    private Border border4;
    private Border border5;
    private JLabel iconLabel = new JLabel();
    private JTextArea message = new JTextArea();
    public ConsoleUI(Frame owner) throws HeadlessException
    {
        super(owner);
        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        detailsScroll.setVisible(false);

        message.setBackground(iconLabel.getBackground());
        super.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createEmptyBorder(20, 20, 20, 0);
        border3 = BorderFactory.createEmptyBorder(20, 20, 10, 20);
        border4 = BorderFactory.createEmptyBorder(0, 0, 0, 20);
        border5 = BorderFactory.createEmptyBorder(0, 0, 20, 0);
        this.getContentPane().setLayout(borderLayout1);
        messagePane.setLayout(borderLayout2);
        buttonPane.setLayout(borderLayout3);
        detailsScroll.setPreferredSize(new Dimension(3, 3));
        this.setResizable(false);
        this.setTitle("");
        messagePane.setBorder(border3);
        detailsTextArea.setEditable(false);
        closeButton.setText("Close");
        closeButton.addActionListener(new ConsoleUI_closeButton_actionAdapter(this));
        messageButton.setText("Details");
        messageButton.addActionListener(new
                                        ConsoleUI_messageButton_actionAdapter(this));
        buttonPane.setBorder(null);
        iconLabel.setBorder(border4);
        iconLabel.setIconTextGap(4);
        iconLabel.setText("");
        //Colors are now handled by SipCommunicatorColorTheme
        //message.setBackground(new Color(224, 223, 227));
        message.setEnabled(true);
        message.setCaretColor(Color.black);
        message.setEditable(false);
        message.setText("");
        this.getContentPane().add(detailsScroll, BorderLayout.SOUTH);
        detailsScroll.getViewport().add(detailsTextArea, null);
        this.getContentPane().add(messagePane, BorderLayout.CENTER);
        messagePane.add(buttonPane, BorderLayout.SOUTH);
        buttonPane.add(buttonsEastPane, BorderLayout.EAST);
        buttonsEastPane.add(messageButton, null);
        buttonsEastPane.add(closeButton, null);
        getRootPane().setDefaultButton(closeButton);
        messagePane.add(iconLabel, BorderLayout.WEST);
        messagePane.add(message, BorderLayout.CENTER);
    }

    void messageButton_actionPerformed(ActionEvent e)
    {
        if (detailsScroll.isVisible()) {
            detailsScroll.setVisible(false);
        }
        else {
            detailsScroll.setPreferredSize(
                new Dimension( (int) detailsTextArea.getPreferredSize().
                              getWidth(),
                              220));
            detailsScroll.setVisible(true);
        }
        pack();
    }

    static void showMsg(String title, String message, String details,
                        String icon)
    {
        ConsoleUI consUI = new ConsoleUI(JOptionPane.getRootFrame());
        try {
            consUI.iconLabel.setIcon(new ImageIcon(Utils.getResource(icon)));
        }
        catch (Throwable thr) {
            //don't let a nullpointerexception spoil our party
        }
        consUI.setTitle(title);
        consUI.message.setText(message);
        consUI.detailsTextArea.setText(details);
        consUI.pack();
        consUI.setModal(true);
        consUI.setLocationRelativeTo(JOptionPane.getRootFrame());
        consUI.show();
    }

    void closeButton_actionPerformed(ActionEvent e)
    {
        dispose();
    }
}

class ConsoleUI_closeButton_actionAdapter
    implements java.awt.event.ActionListener
{
    ConsoleUI adaptee;
    ConsoleUI_closeButton_actionAdapter(ConsoleUI adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.closeButton_actionPerformed(e);
    }
}

class ConsoleUI_messageButton_actionAdapter
    implements java.awt.event.ActionListener
{
    ConsoleUI adaptee;
    ConsoleUI_messageButton_actionAdapter(ConsoleUI adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.messageButton_actionPerformed(e);
    }
}

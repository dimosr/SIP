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

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;
import net.java.sip.communicator.common.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */
class NewContactDialog extends JDialog implements ActionListener
{
    private static final Console console = Console.getConsole(NewContactDialog.class);
    private String actionCommand = CANCEL_COMMAND;

    static final String CANCEL_COMMAND = "Cancel";
    static final String OK_COMMAND     = "OK";

    JPanel contentPane = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel buttonsPane = new JPanel();
    JButton cancelButton = new JButton();
    JButton okButton = new JButton();
    FlowLayout flowLayout1 = new FlowLayout();
    Border border1;
    JPanel labelsPane = new JPanel();
    JPanel fieldsPane = new JPanel();
    JLabel descriptionLabel = new JLabel();
    JLabel addressLabel = new JLabel();
    JLabel aliasLabel = new JLabel();
    GridLayout gridLayout1 = new GridLayout();
    Border border2;
    GridLayout gridLayout2 = new GridLayout();
    JTextField aliasField = new JTextField();
    JTextField descriptionField = new JTextField();
    JTextField addressField = new JTextField();
    Border border3;

    private NewContactDialog(Frame frame, String title, boolean modal)
    {
        super(frame, title, modal);
        try
        {
            jbInit();
            initComponents();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void initComponents()
    {
        setSize(400, 200);

        getRootPane().setDefaultButton(okButton);

        //center the window
        int x = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() -
                       getWidth()) / 2;
        int y = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() -
                       getHeight()) / 2;
        setLocation(x, y);

        okButton.addActionListener(this);
        cancelButton.addActionListener(this);


    }

    private NewContactDialog(JFrame owner)
    {
        this(owner, "New Contact", false);
    }

    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createEmptyBorder(10,20,10,20);
        border2 = BorderFactory.createEmptyBorder(0,30,0,0);
        border3 = BorderFactory.createEmptyBorder(20,0,0,20);
        contentPane.setLayout(borderLayout1);
        cancelButton.setMnemonic('C');
        cancelButton.setText(CANCEL_COMMAND);
        okButton.setActionCommand(OK_COMMAND);
        okButton.setMnemonic('O');
        okButton.setText("OK");
        buttonsPane.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        buttonsPane.setBorder(border1);
        descriptionLabel.setVerifyInputWhenFocusTarget(true);
        descriptionLabel.setDisplayedMnemonic('D');
        descriptionLabel.setLabelFor(descriptionField);
        descriptionLabel.setText("Description: ");
        addressLabel.setDisplayedMnemonic('E');
        addressLabel.setLabelFor(addressField);
        addressLabel.setText("Address: ");
        aliasLabel.setOpaque(false);
        aliasLabel.setDisplayedMnemonic('A');
        aliasLabel.setLabelFor(aliasField);
        aliasLabel.setText("Alias: ");
        labelsPane.setLayout(gridLayout1);
        gridLayout1.setColumns(1);
        gridLayout1.setHgap(10);
        gridLayout1.setRows(0);
        labelsPane.setBorder(border2);
        fieldsPane.setLayout(gridLayout2);
        gridLayout2.setColumns(1);
        gridLayout2.setHgap(5);
        gridLayout2.setRows(0);
        gridLayout2.setVgap(10);
        addressField.setSelectionStart(11);
        addressField.setText("");
        borderLayout1.setHgap(10);
        borderLayout1.setVgap(0);
        contentPane.setBorder(border3);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setModal(true);
        this.setResizable(false);
        descriptionField.setText("");
        aliasField.setText("");
        getContentPane().add(contentPane);
        contentPane.add(buttonsPane,  BorderLayout.SOUTH);
        buttonsPane.add(okButton, null);
        buttonsPane.add(cancelButton, null);
        contentPane.add(labelsPane, BorderLayout.WEST);
        contentPane.add(fieldsPane, BorderLayout.CENTER);
        labelsPane.add(aliasLabel, null);
        labelsPane.add(addressLabel, null);
        labelsPane.add(descriptionLabel, null);
        fieldsPane.add(aliasField, null);
        fieldsPane.add(addressField, null);
        fieldsPane.add(descriptionField, null);
    }

    /**
     * Shows a dialog requesting the user to enter details about the new contact
     * to add returns a NewContactDescriptor object describing the new contact.
     * @param owner the currently valid instance of the sip-communicator frame.
     * @return int
     */
    public static DialogResult collectUserDetails(JFrame owner)
    {
       NewContactDialog ncd = new NewContactDialog(owner);


       ncd.show();
       DialogResult ncdesc = new DialogResult();

       ncdesc.selectedAction = ncd.actionCommand;

       ncdesc.alias      = ncd.aliasField.getText();
       ncdesc.presentity = ncd.addressField.getText();
       ncdesc.notes      = ncd.descriptionField.getText();

       if(console.isDebugEnabled())
       {
           console.debug("alias=" + ncdesc.alias);
           console.debug("presentity=" + ncdesc.presentity);
           console.debug("notes=" + ncdesc.notes);

           console.debug("selected action = " + ncdesc.selectedAction);
       }
       return ncdesc;
    }

    /**
     * A class used to return contact description entered by the user in the
     * new contact dialog.
     */
    static class DialogResult
    {
        String selectedAction = CANCEL_COMMAND;

        String alias      = null;
        String presentity = null;
        String notes      = null;
    }

    public void actionPerformed(ActionEvent evt)
    {
        this.actionCommand = evt.getActionCommand();
        dispose();
    }

    public static void main(String[] args)
    {
        net.java.sip.communicator.gui.plaf.SipCommunicatorColorTheme.initLookAndFeel();
        collectUserDetails(null);
    }

}

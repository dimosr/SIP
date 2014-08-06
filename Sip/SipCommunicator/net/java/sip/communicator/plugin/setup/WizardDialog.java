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

package net.java.sip.communicator.plugin.setup;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import net.java.sip.communicator.common.*;
import java.awt.event.*;
import javax.swing.plaf.metal.*;
import net.java.sip.communicator.gui.plaf.*;

/**
 * @author Emil Ivov
 */

public class WizardDialog extends JDialog
{
    private static final Console console = Console.getConsole(WizardDialog.class);

    JPanel contentPane = new JPanel();
    JPanel buttonsPane = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton cancelButton = new JButton();
    JButton nextButton = new JButton();
    JButton backButton = new JButton();
    FlowLayout flowLayout1 = new FlowLayout();
    Border border1;
    JPanel logoPane = new JPanel();
    JScrollPane wizardContentPane = new JScrollPane();
    Border border2;
    Border border3;
    JLabel logoLabel = new JLabel();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel buttonsAlignmentPane = new JPanel();
    GridLayout gridLayout1 = new GridLayout();
    Border border4;
    JPanel pageTitleFrame = new JPanel();
    JLabel pageTitle = new JLabel();
    FlowLayout flowLayout2 = new FlowLayout();
    Border border5;

    static final String NEXT_COMMAND   = "Next";
    static final String FINISH_COMMAND = "Finish";
    static final String BACK_COMMAND   = "Back";
    static final String CANCEL_COMMAND = "Cancel";
    static final String DIALOG_TITLE   = "SIP Communicator Configuration Wizard";

    public WizardDialog(JFrame owner) throws HeadlessException
    {
        super(owner, true);
        try
        {
            setTitle(DIALOG_TITLE);

            nextButton.setActionCommand(NEXT_COMMAND);
            backButton.setActionCommand(BACK_COMMAND);
            cancelButton.setActionCommand(CANCEL_COMMAND);

            jbInit();

            logoLabel.setIcon(new ImageIcon(Utils.getResource("sip-communicator.logo.jpg")));
            pack();
            //center the dialog
            int x = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth() - getWidth())/2;
            int y = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight() - getHeight())/2;
            setLocation(x, y);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            console.error("Error constructing Wizard", e);
        }
    }
    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createEmptyBorder(10,0,0,-4);
        border2 = BorderFactory.createEtchedBorder(Color.white,new Color(142, 142, 142));
        border3 = BorderFactory.createEmptyBorder(10,20,20,20);
        border4 = BorderFactory.createEmptyBorder(10,10,10,10);
        border5 = BorderFactory.createEmptyBorder(0,0,5,0);
        contentPane.setLayout(borderLayout1);
        cancelButton.setMnemonic('C');
        cancelButton.setText("Cancel");
        nextButton.setMnemonic('N');
        nextButton.setText("Next >");
        backButton.setMnemonic('B');
        backButton.setText("< Back");
        buttonsPane.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        buttonsPane.setBorder(border1);
        logoPane.setBorder(border2);
        logoPane.setMinimumSize(new Dimension(100, 10));
//        logoPane.setPreferredSize(new Dimension(180, 480));
        logoPane.setRequestFocusEnabled(true);
        logoPane.setLayout(borderLayout2);
        wizardContentPane.setBorder(border2);
        wizardContentPane.setMinimumSize(new Dimension(380, 300));
        wizardContentPane.setPreferredSize(new Dimension(380, 300));
        contentPane.setBorder(border3);
        contentPane.setDebugGraphicsOptions(0);
        logoLabel.setIcon(null);
        buttonsAlignmentPane.setLayout(gridLayout1);
        gridLayout1.setHgap(5);
        gridLayout1.setVgap(5);
        pageTitleFrame.setBorder(border5);
        pageTitleFrame.setRequestFocusEnabled(true);
        pageTitleFrame.setToolTipText("");
        pageTitleFrame.setLayout(flowLayout2);
        pageTitle.setFont(new java.awt.Font("Dialog", 1, 18));
        pageTitle.setText("Page Title");
        flowLayout2.setAlignment(FlowLayout.RIGHT);
        this.getContentPane().add(contentPane, BorderLayout.CENTER);
        contentPane.add(buttonsPane,  BorderLayout.SOUTH);
        buttonsPane.add(buttonsAlignmentPane, null);
        buttonsAlignmentPane.add(backButton, null);
        buttonsAlignmentPane.add(nextButton, null);
        buttonsAlignmentPane.add(cancelButton, null);
        contentPane.add(logoPane,  BorderLayout.WEST);
        logoPane.add(logoLabel,  BorderLayout.CENTER);
        contentPane.add(wizardContentPane,  BorderLayout.CENTER);
        contentPane.add(pageTitleFrame, BorderLayout.NORTH);
        pageTitleFrame.add(pageTitle, null);
    }

    protected void processWindowEvent(WindowEvent evt)
    {
        super.processWindowEvent(evt);
        if(evt.getID() == WindowEvent.WINDOW_CLOSING)
        {
            ActionListener[] listeners = cancelButton.getActionListeners();
            for (int i = 0; i < listeners.length; i++) {
                listeners[0].actionPerformed(new ActionEvent(cancelButton, ActionEvent.ACTION_PERFORMED, CANCEL_COMMAND));
            }
        }
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


}

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
import net.java.sip.communicator.common.*;
import javax.swing.tree.*;
import java.awt.event.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class ContactListFrame
    extends JFrame
    implements MouseListener
{
    private static final Console console = Console.getConsole(ContactListFrame.class);
    JPanel contentPane = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();

    ContactsTree contactsTree  = null;
    JScrollPane treeScrollPane = new JScrollPane();
    BorderLayout borderLayout2 = new BorderLayout();
    JComboBox statusComboBox   = new JComboBox();

    // The following two are filled in by the
    public MenuBar              menuBar          = new MenuBar();
    public ContactListPopupMenu popupMenu        = new ContactListPopupMenu();

    private static final int DEFAULT_WIDTH = 150;
    private static final int DEFAULT_HEIGHT = 500;

    private ContactListActions contactListActions = new ContactListActions();


    public ContactListFrame()
    {
        super("SIP Communicator");
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
        addWindowListener(new FrameSizeSaver());
        addMouseListener(this);
        treeScrollPane.addMouseListener(this);

        popupMenu.setContactListActions(contactListActions);
        contactListActions.setApplicationFrame(this);

        int x = -1;
        int y = -1;

        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;

        try {
            x = Integer.parseInt(Utils.getProperty(
                "net.java.sip.communicator.gui.imp.CONTACT_LIST_X"));
        }
        catch (NumberFormatException ex) {
            console.debug("Failed to parse CONTACT_LIST_X default value (" + x + ") will be used");
        }
        try {
            y = Integer.parseInt(Utils.getProperty(
                "net.java.sip.communicator.gui.imp.CONTACT_LIST_Y"));
        }catch(NumberFormatException ex){
            console.debug("Failed to parse CONTACT_LIST_Y default value (" + y +
                          ") will be used");
        }
        try {
            width = Integer.parseInt(Utils.getProperty(
                "net.java.sip.communicator.gui.imp.CONTACT_LIST_WIDTH"));
        }
        catch (NumberFormatException ex) {
            console.debug("Failed to parse CONTACT_LIST_WIDTH default value (" + width +
                          ") will be used");
        }
        try {
            height = Integer.parseInt(Utils.getProperty(
                "net.java.sip.communicator.gui.imp.CONTACT_LIST_HEIGHT"));

        }
        catch (NumberFormatException ex) {
            console.debug("Failed to parse CONTACT_LIST_HEIGHT default value (" + height +
                          ") will be used");
        }

        setSize(width, height);

        //center frame if no frame location was defined in the config file
        if(x == -1)
            x = ((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() -
                getWidth())/2;
           if(y == -1)
            y = ((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() -
                getHeight())/2;


        setLocation(x, y);

        setIconImage(new ImageIcon(Utils.getResource("sip-communicator-16x16.jpg")).getImage());

        setDefaultLookAndFeelDecorated(true);

    }

    private void expandContactsTree()
    {
        for(int i = 0; i < contactsTree.getRowCount(); i++)
            contactsTree.expandRow(i);
    }



    private void jbInit() throws Exception
    {
        this.getContentPane().setLayout(borderLayout1);
        contentPane.setLayout(borderLayout2);
        this.setJMenuBar(menuBar);
        this.getContentPane().add(contentPane, BorderLayout.CENTER);
        contentPane.add(treeScrollPane,  BorderLayout.CENTER);
        this.getContentPane().add(statusComboBox, BorderLayout.SOUTH);
    }


    public void setModel(ContactListModel model)
    {
        contactsTree = new ContactsTree();
        contactsTree.setModel(model);
        contactsTree.setCellRenderer(model);
        contactsTree.addMouseListener(this);
        treeScrollPane.setViewportView(contactsTree);
        treeScrollPane.updateUI();
        expandContactsTree();
    }

    /**
     * Set a set of strings representing a supported status set.
     * @param iter an iterator over strings representing a status set.
     */
    public void setStatusControllerModel(PresenceStatusControllerUIModel model)
    {
            statusComboBox.setModel(model);
            statusComboBox.setRenderer(model);
    }

    private class FrameSizeSaver extends WindowAdapter
    {

        public void windowClosing(WindowEvent evt)
        {
//            System.out.println( "width = " + evt.getComponent().getSize().width );
//            System.out.println( "height = " + evt.getComponent().getSize().height );
            Utils.setProperty("net.java.sip.communicator.gui.imp.CONTACT_LIST_X", String.valueOf(getX()));
            Utils.setProperty("net.java.sip.communicator.gui.imp.CONTACT_LIST_Y", String.valueOf(getY()));
            Utils.setProperty("net.java.sip.communicator.gui.imp.CONTACT_LIST_WIDTH", String.valueOf(getWidth()));
            Utils.setProperty("net.java.sip.communicator.gui.imp.CONTACT_LIST_HEIGHT", String.valueOf(getHeight()));
            PropertiesDepot.storeProperties();
        }


    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on a
     * component.
     *
     * @param e MouseEvent
     */
    public void mouseClicked(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e MouseEvent
     */
    public void mousePressed(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e MouseEvent
     */
    public void mouseReleased(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if(e.isPopupTrigger())
        {
            //select the node the user clicked upon
            Component invoker = (Component)e.getSource();
            if (invoker instanceof ContactsTree) {
                ContactsTree tree = (ContactsTree) invoker;
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path != null
                    && path.getPathCount() > 0) {
                    tree.setSelectionPath(path);
                }
            }

            //tell all contact list actions about the newly selected node


            popupMenu.show(invoker, e.getX(), e.getY());
        }
    }

    /**
     * Returns the path to the first currently selected node.
     * @return an array of objects representing the path to the first currently
     * selected node.
     */
    Object[] getSelectedPath()
    {
        return contactsTree.getSelectionPath().getPath();
    }

    /**
     * Returns a path containing all parents of the currently selected node
     * except the node itself.
     * @return an array of objects containing all parents of the currently
     * selected node except the node itself.
     */
    Object[] getSelectedParentPath()
    {
        return contactsTree.getSelectionPath().getParentPath().getPath();
    }

    /**
     * Transmits the given request to the contacts tree module.
     * @param request the request describing the contact that the user requested
     * to add.
     */
    void requestContactAddition(ContactAdditionRequest request)
    {
        ((ContactListModel)this.contactsTree.getModel()).requestContactAddition(request);
    }






    /**
     * Transmits the given request to the contacts tree module.
     * @param request the request describing the contact that the user requested
     * to remove.
     */
    void requestContactRemoval(ContactRemovalRequest request)
    {
        ( (ContactListModel)this.contactsTree.getModel()).
            requestContactRemoval(request);
    }

}


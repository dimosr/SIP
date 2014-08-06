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

import java.util.*;

import java.awt.event.*;
import javax.swing.*;

/**
 * All contact list manipulations such az managing contact groups, adding and
 * removing contacts and etc. are implemented as swing Actions. These actions
 * are then used to create a pop up menu and to populate bar menus. All actions
 * here extend the AbstractContactListAction class.
 * @author Emil Ivov
 */

public class ContactListActions
{

    private ArrayList contactSpecificActions   = new ArrayList ();
    private ArrayList groupSpecificActions     = new ArrayList ();
    private ArrayList selectionAgnosticActions = new ArrayList ();

    private ContactListFrame applicationFrame = null;

    public ContactListActions()
    {
        // init contact specific actions
        contactSpecificActions.add(new SendMessageAction());
        contactSpecificActions.add(new CallAction());
        contactSpecificActions.add(new MoveContactAction());
        contactSpecificActions.add(new RemoveContactAction());

        // init group specific actions
        groupSpecificActions.add(new AddContactAction());
        groupSpecificActions.add(new NewGroupAction());
        groupSpecificActions.add(new MoveGroupAction());
        groupSpecificActions.add(new RemoveGroupAction());

        //init selection agnostic actions
        //nothing to add here for the moment.
    }

    /**
     * All Contact List actions extend from this one. It provides standard
     * AbstractAction methods and some extra stuff such as the getMnemonic()
     * method implemented by all descendents.
     */
    public abstract class AbstractContactListAction
        extends AbstractAction
    {
        public AbstractContactListAction(String name)
        {
            super(name);
        }

        public abstract char getMnemonic();
    }

    /**
     * Defines an action that provides the user with a means of editing and
     * sending Text Messages.
     */
    public class SendMessageAction
        extends AbstractContactListAction
    {

        public SendMessageAction()
        {
            super("Send Message");
        }

        public void actionPerformed(ActionEvent evt)
        {

        }

        public char getMnemonic()
        {
            return 'S';
        }

    }

    /**
     * Defines an action that provides the user with a means of starting an
     * audio/video conversation.
     */
    public class CallAction
        extends AbstractContactListAction
    {

        public CallAction()
        {
            super("Call");
        }

        public void actionPerformed(ActionEvent evt)
        {

        }

        public char getMnemonic()
        {
            return 'C';
        }

    }

    /**
     * Defines an action that provides the user with a means of moving an existing
     * contact to a different contact group.
     */
    public class MoveContactAction
        extends AbstractContactListAction
    {

        public MoveContactAction()
        {
            super("Move to");
        }

        public void actionPerformed(ActionEvent evt)
        {

        }

        public char getMnemonic()
        {
            return 'M';
        }

    }

    /**
     * Defines an action that provides the user with a means of moving an existing
     * contact group into another contact group.
     */
    public class MoveGroupAction
        extends AbstractContactListAction
    {

        public MoveGroupAction()
        {
            super("Move to");
        }

        public void actionPerformed(ActionEvent evt)
        {

        }

        public char getMnemonic()
        {
            return 'M';
        }

    }


    /**
     * Defines an action that provides the user with a means of removing a contact
     * from the contact list.
     */
    public class RemoveContactAction
        extends AbstractContactListAction
    {

        public RemoveContactAction()
        {
            super("Remove Contact");
        }

        public void actionPerformed(ActionEvent evt)
        {
            if(JOptionPane.showConfirmDialog(
                 applicationFrame,
                 "Do you really want to delete the selected contact?",
                 "Confirm - Delete",
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.QUESTION_MESSAGE)
               == JOptionPane.NO_OPTION)
                return;

            ContactRemovalRequest request = new ContactRemovalRequest();
            Object contact = applicationFrame.getSelectedPath()[applicationFrame.getSelectedPath().length - 1 ];
            request.setContact(contact);
            request.setLocation(applicationFrame.getSelectedParentPath());

            applicationFrame.requestContactRemoval(request);
        }

        public char getMnemonic()
        {
            return 'R';
        }
    }

    /**
     * Defines an action that provides the user with a means of creating new
     * Contact Groups
     */
    public class NewGroupAction
        extends AbstractContactListAction
    {

        public NewGroupAction()
        {
            super("New Contact Group");
        }

        public void actionPerformed(ActionEvent evt)
        {

        }

        public char getMnemonic()
        {
            return 'N';
        }
    }

    /**
    * Defines an action that provides the user with a means of removing
    * Contact Groups
    */
   public class RemoveGroupAction
       extends AbstractContactListAction
   {

       public RemoveGroupAction()
       {
           super("Remove Contact Group");
       }

       public void actionPerformed(ActionEvent evt)
       {

       }

       public char getMnemonic()
       {
           return 'R';
       }
   }


    /**
     * Defines an action that provides the user with a means of creating new
     * Contacts
     */
    public class AddContactAction
        extends AbstractContactListAction
    {

        public AddContactAction()
        {
            super("Add Contact");
        }

        public void actionPerformed(ActionEvent evt)
        {
            NewContactDialog.DialogResult result =
                NewContactDialog.collectUserDetails(applicationFrame);

            if(result.selectedAction == NewContactDialog.CANCEL_COMMAND)
                return;

            ContactAdditionRequest request = new ContactAdditionRequest();
            request.setAlias(result.alias);
            request.setContactIdentifier(result.presentity);
            request.setNotes(result.notes);

            /** @todo we should have a location selection component in the
             * new contact dialog and it is the result of that component
             * that must be set in the ContactAdditionRequest. For the time
             * being however we are going to use the path that is currently
             * selected in the contact tree.*/
            request.setLocation(applicationFrame.getSelectedPath());

            applicationFrame.requestContactAddition(request);


//            ((ContactListModel)applicationFrame.contactsTree.getModel());
        }

        public char getMnemonic()
        {
            return 'A';
        }

    }

    /**
     * Returns an ArrayList containing all actions applicable on a specific
     * Contact.
     * @return ArrayList an ArrayList containing all actions applicable on a
     * specific Contact.
     */
    public ArrayList getContactSpecificActions()
    {
        return contactSpecificActions;
    }

    /**
     * Returns an ArrayList containing all actions applicable on a specific
     * ContactGroup.
     * @return ArrayList an ArrayList containing all actions applicable on a
     * specific ContactGroup.
     */
    public ArrayList getGroupSpecificActions()
    {
        return groupSpecificActions;
    }

    /**
     * Returns an ArrayList containing that are independent of the object
     * currently selected in the contact tree.
     * @return an ArrayList containing that are independent of the object
     * currently selected in the contact tree.
     */
    public ArrayList getSelectionAgnosticActions()
    {
        return selectionAgnosticActions;
    }

    /**
     * Sets the currently running instance of ContactListFrame.
     *
     * @param contactListFrame the ContactListFrame instance that is currently
     * visible.
     */
    void setApplicationFrame(ContactListFrame contactListFrame)
    {
        this.applicationFrame = contactListFrame;
    }

}

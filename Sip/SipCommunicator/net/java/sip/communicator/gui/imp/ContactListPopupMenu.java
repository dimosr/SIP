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

import javax.swing.JPopupMenu;
import javax.swing.*;
import java.awt.Component;
import javax.swing.event.*;
import javax.swing.tree.*;
import net.java.sip.communicator.common.*;
import java.awt.event.*;
import java.util.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class ContactListPopupMenu
    extends JPopupMenu

    implements PopupMenuListener
{
    private static final Console console = Console.getConsole(ContactListPopupMenu.class);
    private ContactListActions contactListActions = null;


    public ContactListPopupMenu()
    {
        addPopupMenuListener(this);
    }

    /**
     * Displays the popup menu at the position x,y in the coordinate space of the
     * component invoker.
     *
     * @param invoker the component in whose space the popup menu is to appear
     * @param x the x coordinate in invoker's coordinate space at which the
     *   popup menu is to be displayed
     * @param y the y coordinate in invoker's coordinate space at which the
     *   popup menu is to be displayed
     */
    public void show(Component invoker, int x, int y)
    {
       try{
            console.logEntry();

            if(contactListActions == null)
            {
                console.debug("contactListActions is null, ignoring popup menu request");
                return;
            }
            if (invoker instanceof ContactsTree) {
                ContactsTree tree = (ContactsTree)invoker;
                TreePath path = tree.getPathForLocation(x, y);
                if(path != null
                   && path.getPathCount() > 0)
                {

                    ContactListModel model = (ContactListModel)tree.getModel();

                    //First add actions that depend on the selected node
                    ArrayList actionSet = null;

                    //get menu actions depending on whether the menu is invoked
                    //for a contact or a group
                    if( model.isGroup( path.getLastPathComponent() ) )
                    {
                        actionSet =
                            contactListActions.getGroupSpecificActions();
                    }
                    else
                    {
                        actionSet =
                            contactListActions.getContactSpecificActions();
                    }

                    if(actionSet.size() > 0)
                    {
                        for (int i = 0 ; i < actionSet.size() ; i++) {
                            add(new JMenuItem(  ((AbstractAction)actionSet.get(i))  ) );
                        }
                        addSeparator();
                    }

                    //Add actions that do not depend on the selected node
                    actionSet = contactListActions.getSelectionAgnosticActions();
                    for (int i = 0; i < actionSet.size(); i++) {
                        add(new JMenuItem( ( (AbstractAction) actionSet.get(i))));
                    }

                }
            }

            super.show(invoker, x, y);
        }
        finally
        {
            console.logExit();
        }

    }

    /**
     *
     * @param e PopupMenuEvent
     */
    public void popupMenuWillBecomeVisible(PopupMenuEvent e)
    {
    }

    /**
     *
     * @param e PopupMenuEvent
     */
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
    {
        this.removeAll();
    }

    /**
     *
     * @param e PopupMenuEvent
     */
    public void popupMenuCanceled(PopupMenuEvent e)
    {
//        this.removeAll();
    }

    /**
     * Sets a valid instance of the contact list actions that the popup menu will
     * include as available options.
     * @param contactListActions a set of available contact list options.
     */
    public void setContactListActions(ContactListActions contactListActions)
    {
        this.contactListActions = contactListActions;
    }

    /**
     * Initializes submenus
     */
    private void initMenus()
    {

    }
}

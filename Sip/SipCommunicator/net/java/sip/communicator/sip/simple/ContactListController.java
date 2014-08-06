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
package net.java.sip.communicator.sip.simple;

import java.io.*;
import java.util.*;

import javax.swing.*;

import net.java.sip.communicator.common.*;
import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.sip.*;
import net.java.sip.communicator.sip.simple.event.*;
import net.java.sip.communicator.sip.simple.storage.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class ContactListController
    implements StatusListener, WatcherEventsDispatcher
{
    private static final Console console = Console.getConsole(ContactListController.class);

    private SipManager sipManCallback = null;
    private ContactGroup  contactList  = null;
    protected ArrayList listenerList = new ArrayList();
    private SubscriberThread subThread = new SubscriberThread();

    public ContactListController(SipManager sipManCallback)
    {
        this.sipManCallback = sipManCallback;
//        new DummyEventGenerationDialog().show();
    }

    /**
    * Return the contactList corresponding to (loaded from) the specified url.
    * Currently the url will be consulted only if the contact list had not been
    * previosly loaded.
    * @param url the url where the contact list should be loaded.
    * @throws CommunicationsException if we fail to load the contact list.
    * @return the contact list corresponding to the specified url.
    */
   public ContactGroup loadContactList(String url)
       throws CommunicationsException
   {
       try{
           if(contactList == null)
               contactList = ContactListSerializer.loadList(url);

           //save the location we loaded the list from so that we know where to
           //store it incase it gets modified.
           contactList.setSourceLocation(url);

           return contactList;
       }
       catch(IOException exc)
       {
           throw new CommunicationsException("Failed to load contact local list.", exc);
       }
       finally
       {
           console.logExit();
       }

   }

   /**
    * Returns a list containing all the parents of the contact specified by the
    * given uri or null if the contact could not be located.
    * @param uri the identity of the contact to locate
    * @param root the Contact group where the search should begin
    * @return a list containing all the parents of the contact specified by the
    * given uri or null if the contact could not be located.
    */
   public LinkedList findContactLocation(GenericURI uri, ContactGroup root)
   {
       if(root == null)
           return null;

       for(int i = 0; i < root.getChildCount(); i++)
       {
           Contact child = root.getChild(i);
           if(child instanceof ContactGroup)
           {
               LinkedList path = findContactLocation(uri, (ContactGroup) child);
               if(path != null)
               {
                   path.addFirst(root);
                   return path;
               }
               else
                   continue;
           }

           if( child.getPresenceUri() != null //handle erroneous contact list file
               && child.getPresenceUri().matches(uri))
           {
               LinkedList path = new LinkedList();
               path.addFirst(root);
               return path;
           }
       }

       return null;
   }

    /**
    * This class is only used for debugging and testing purposes
    *
    * @author Emil Ivov
    */
   private class DummyEventGenerationDialog
       extends javax.swing.JDialog
       implements java.awt.event.ActionListener
   {
       public DummyEventGenerationDialog()
       {
           super( (javax.swing.JFrame)null, false);
           getContentPane().setLayout(new java.awt.BorderLayout());

           setSize(250, 60);
           javax.swing.JButton button = new javax.swing.JButton(
               "Simulate Notification Events");
            button.addActionListener(this);
            getContentPane().add(button, java.awt.BorderLayout.CENTER);
       }

       public void actionPerformed(java.awt.event.ActionEvent evt)
       {
           //this code should eventually be moved to a method called by the
           //simple.Watcher class upon reception of a notification
           LinkedList pathList = findContactLocation(GenericURI.parseURI("pres:pichaga@proxenet.u-strasbg.fr"), contactList);
            if( pathList == null
                  || pathList.size() < 1)
                return;
            ContactGroup path[] = new ContactGroup[pathList.size()];

            for(int i = 0; i < path.length; i++)
            {
                path[i] = (ContactGroup)pathList.get(i);
            }

            Contact source = path[path.length -1].getChildContact(GenericURI.parseURI("pres:pichaga@proxenet.u-strasbg.fr"));
            if(source == null)
                return;
            PresenceTuple status = source.getStatusTuple();
            PresenceTuple oldStatus = status == null ? null : (PresenceTuple)status.clone();

            status.setExtendedStatus(PresenceTuple.EXTENDED_STATUS_ONLINE);
            status.setBasicStatus(PresenceTuple.BASIC_STATUS_OPEN);

            fireNotificationReceived(source , path, status, oldStatus);
       }
   }

   /**
     * Adds a listener for the StatusEvents posted after status changes.
     *
     * @param l the listener to add
     */
    public void addNotificationListener(ContactListChangeListener l)
    {
        if(l == null)
            throw new NullPointerException("PresenceStatusListener cannot be null");

        listenerList.add(l);
    }

    /**
     * Removes a listener previously added with
     * <code>addStatusListener</code>.
     *
     * @param l the listener to remove
     */
    public void removeNotificationListener(ContactListChangeListener l)
    {
        if(l == null)
            return;
        for(int i = 0; i < listenerList.size(); i++)
            if(listenerList.get(i).equals(l))
            {
                listenerList.remove(i);
                return;
            }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source The contact that changed status.
     * @param sourcePath the location of the contact that changed status. This
     * argument contains an array of ContactGroup-s starting from the root
     *             ContactGroup going to the parent of the event source contact.
     * @param newStatus the new status of the specified source contact.
     * @param oldStatus the previos status of the specified soruce contact.
     */
    protected void fireNotificationReceived(Contact        source,
                                        ContactGroup[] sourcePath,
                                        PresenceTuple  newStatus,
                                        PresenceTuple  oldStatus)
    {
        NotificationReceivedEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listenerList.size()-1; i>=0; i--) {
            // Lazily create the event:
            if (e == null)
                e = new NotificationReceivedEvent(source, sourcePath, newStatus, oldStatus);
            ((ContactListChangeListener)listenerList.get(i)).notificationReceived(e);
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source The contact that has been added.
     * @param sourcePath the location of the contact that has been added.
     * @param index the index where the child was added
     */
    protected void fireContactAdded(Contact        source,
                                    ContactGroup[] sourcePath,
                                    int            index)
    {
        ContactAddedEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listenerList.size()-1; i>=0; i--) {
            // Lazily create the event:
            if (e == null)
                e = new ContactAddedEvent(source, sourcePath, index);
            ((ContactListChangeListener)listenerList.get(i)).contactAdded(e);
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source The contact that has been removed.
     * @param sourcePath the location where the contact got removed from.
     * @param index the index where the child was before being removed.
     */
    protected void fireContactRemoved(Contact        source,
                                    ContactGroup[] sourcePath,
                                    int            index)
    {
        ContactRemovedEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listenerList.size()-1; i>=0; i--) {
            // Lazily create the event:
            if (e == null)
                e = new ContactRemovedEvent(source, sourcePath, index);
            ((ContactListChangeListener)listenerList.get(i)).contactRemoved(e);
        }
    }



    /**
     * Starts the subscriber thread which sends SUBSCRIBE requests to all
     * contact-list members.
     * @todo better thread support - avoid race conditiions and make
     * sure subscription thread gets executed each time
     */
    public void startSubscribeThread()
    {
        if(subThread.isRunning)
        {
            //kill the thread
        }

        subThread.action = SubscriberThread.SUBSCRIBE;
        subThread.start();

    }

    /**
     * Starts the subscriber thread which sends SUBSCRIBE requests to all
     * contact-list members.
     */
    public void startUnSubscribeThread()
    {
        if(subThread.isRunning)
        {
            //kill the thread
        }

        subThread.action = SubscriberThread.UNSUBSCRIBE;
        subThread.start();
    }


    /**
     * Called by the PresenceStatusManager upon status change. The
     * ContactListController uses the method to trigger the subscriptions thread
     * if the status change is an offline->online one and to send unSUBSCRIBE
     * requests in the case of an online->offline event
     * @param evt A StatusChangeEvent instance.
     */
    public void statusChanged(StatusChangeEvent evt)
    {
        PresenceTuple oldStatus = evt.getOldStatus();
        PresenceTuple newStatus = evt.getNewStatus();

        if(   oldStatus.getBasicStatus() != null
           && oldStatus.getBasicStatus().equals(PresenceTuple.BASIC_STATUS_CLOSED)
           && newStatus.getBasicStatus() != null
           && newStatus.getBasicStatus().equals(PresenceTuple.BASIC_STATUS_OPEN))
             this.startSubscribeThread();
         else if(
              oldStatus.getBasicStatus() != null
           && oldStatus.getBasicStatus().equals(PresenceTuple.BASIC_STATUS_OPEN)
           && newStatus.getBasicStatus() != null
           && newStatus.getBasicStatus().equals(PresenceTuple.BASIC_STATUS_CLOSED))
             this.startUnSubscribeThread();

    }

    /**
     * Sending a subscribe request to all contact list members might take quite a while and should be run in a separate
     * Thread. The Subscriber thread iterates through the contact list and sends SUBSCRIBE requests to  we'll be using
     */
    private class SubscriberThread extends Thread
    {
        boolean isRunning = false;

        static final int SUBSCRIBE   = 0;
        static final int UNSUBSCRIBE = 1;

        int action = SUBSCRIBE;

        public void run()
        {
            isRunning = true;
            if(action == SUBSCRIBE)
                sendSubscriptionsToGroupMembers(contactList);
            else
                removeSubscriptionsForGroupMembers(contactList);

            /** @todo do the same thing for removing subscriptions */
            isRunning = false;
        }

        /**
         * Make the Watcher send SUBSCRIBE requests to all <code>group</code>
         * members.
         * @param group the group whose recipients we should SUBSCRIBE for
         */
        private void sendSubscriptionsToGroupMembers(ContactGroup group)
        {
            for(int i = 0; i < group.getChildCount() && isRunning; i++)
            {
                Contact child = group.getChild(i);

                if (child instanceof ContactGroup)
                    sendSubscriptionsToGroupMembers((ContactGroup)child);
                else
                    try {
                        sipManCallback.watcher.sendSubscription(child.
                            getPresenceUri().toString());
                    }
                    catch (CommunicationsException ex) {
                        sipManCallback.fireCommunicationsError(
                            new CommunicationsException("Failed to SUBSCRIBE for status notifications from "
                                                    + child.getPresenceUri(),
                                                    ex));
                    }
            }
        }

        /**
         * Make the Watcher send unSUBSCRIBE requests to all <code>group</code>
         * members.
         * @param group the group whose recipients we should unSUBSCRIBE from
         */
        private void removeSubscriptionsForGroupMembers(ContactGroup group)
        {
            for(int i = 0; i < group.getChildCount() && !isRunning; i++)
            {
                Contact child = group.getChild(i);

                if (child instanceof ContactGroup)
                    removeSubscriptionsForGroupMembers(group);
                else
                    try {
                        /** @todo  call remove subscription for child*/
                        throw new CommunicationsException("Method not implemented");
                    }
                    catch (CommunicationsException ex) {
                        sipManCallback.fireCommunicationsError(
                            new CommunicationsException("Failed to SUBSCRIBE for status notifications from "
                                                    + child.getPresenceUri(),
                                                    ex));
                    }
            }
        }
    }

    /**
     * Decodes <code>presenceData</code> and dispatches a corresponding
     * NotificationEvent to the user interface module.
     * @param presentity the presendity that sent the presenceData
     * @param contentType the content subtype used to encode <code>presenceData</code>
     * @param presenceData the Presence Information Data descibing presentity's
     * presence status.
     * @param senderURI, the uri of the sending party.
     */
    public void dispatchNotification(GenericURI senderURI, String contentType, byte[] presenceData)
    {
        try {
            PresenceTuple tuple = XmlPresenceInformationFactory.
                decodePresenceInformationData(contentType, presenceData);

            if(contentType.equals(XmlPresenceInformationFactory.XPIDF_XML))
                tuple.setPresenceUri(senderURI);

            LinkedList pathList = findContactLocation(tuple.getPresenceUri(),
                contactList);
            if (pathList == null
                || pathList.size() < 1)
                return;
            ContactGroup path[] = new ContactGroup[pathList.size()];

            for (int i = 0; i < path.length; i++) {
                path[i] = (ContactGroup) pathList.get(i);
            }

            Contact source = path[path.length -1].getChildContact(tuple.getPresenceUri());
            if (source == null)
                return;
            PresenceTuple oldStatus = source.getStatusTuple();

            source.setStatusTuple(tuple);

            fireNotificationReceived(source, path, tuple, oldStatus);

        }
        catch (CommunicationsException ex) {
            console.error("Failed to decode incoming presence data.", ex);
            sipManCallback.fireCommunicationsError(ex);
        }
    }

    /**
     * Adds <code>contact</code> to the ContactList managed by this
     * ContactListController.
     *
     * @param location the location where the new contact should be created.
     * @param presenceUriStr the presence URI for the new contact
     * @param displayName a display alias to be saved for the new contact.
     * @param notes a string containing any user notes for the new contact.
     * @throws CommunicationsException if we faile to create the new contact.
     */
    public void requestContactAddition(ContactGroup[] location,
                                       String         presenceUriStr,
                                       String         displayName,
                                       String         notes)
        throws CommunicationsException
    {
        // create the contact and add it to the specified location
        GenericURI presenceURI = GenericURI.parseURI(presenceUriStr);
        Contact newContact = new Contact(presenceURI);
        newContact.setDisplayName(displayName);
        newContact.setNotes(notes);
        newContact.setPresenceUri(presenceURI);

        if(location == null
           || location.length < 1)
            throw new CommunicationsException("Invalid location parameter!");

        ContactGroup parent = location[location.length - 1];
        int index = parent.addContact(newContact);

        //store the list to a file
        if(contactList.getSourceLocation() == null
           ||contactList.getSourceLocation().length() == 0)
          throw new CommunicationsException("No store location was given for the specified contact list!");
        ContactListSerializer.storeContactList(contactList.getSourceLocation(),
                                               contactList);

        // trigger an event that would cause the ui refresh the user interface
        fireContactAdded(newContact, location, index);

    }

    /**
     * Adds <code>contact</code> to the ContactList managed by this
     * ContactListController.
     *
     * @param location the location where the new contact should be created.
     * @param presenceUriStr the presence URI for the new contact
     * @param displayName a display alias to be saved for the new contact.
     * @param notes a string containing any user notes for the new contact.
     * @throws CommunicationsException if we faile to create the new contact.
     */
    public void requestContactRemoval( ContactGroup[] location,
                                       Contact        contact)
        throws CommunicationsException
    {
        if (location == null
            || location.length < 1)
            throw new CommunicationsException("Invalid location parameter!");

        int index = location[location.length - 1].indexOf(contact);

        location[location.length - 1].removeContact(contact);


        //store the list to a file
        if (contactList.getSourceLocation() == null
            || contactList.getSourceLocation().length() == 0)
            throw new CommunicationsException(
                "No store location was given for the specified contact list!");
        ContactListSerializer.storeContactList(contactList.getSourceLocation(),
                                               contactList);

        // trigger an event that would cause the ui refresh the user interface
        fireContactRemoved(contact, location, index);

    }


}

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

import java.util.*;

import net.java.sip.communicator.common.*;
import net.java.sip.communicator.sip.*;
import net.java.sip.communicator.sip.simple.event.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class PresenceStatusManager
    implements LocalPresenceUserAgent, SubscriptionAuthority
{
    private static final Console console = Console.getConsole(PresenceStatusManager.class);

    private SipManager sipManCallback = null;
    protected ArrayList listenerList = new ArrayList();
    private static LinkedList  supportedStatusSet = new LinkedList();

    private PresenceTuple currentStatus = new PresenceTuple();
    private PresenceTuple bogusOfflineStatus = new PresenceTuple();
    public static final float DEFAULT_CONTACT_PRIORITY = 0.8f;

    private Vector supportedPidFormats = new Vector();

    private SubscriptionAuthority subscriptionAuthority = null;

    public PresenceStatusManager(SipManager callback)
    {
        this.sipManCallback = callback;
        currentStatus.setBasicStatus(PresenceTuple.BASIC_STATUS_CLOSED);
        currentStatus.setExtendedStatus(PresenceTuple.EXTENDED_STATUS_OFFLINE);

        currentStatus.setBasicStatus(PresenceTuple.BASIC_STATUS_CLOSED);
        currentStatus.setExtendedStatus(PresenceTuple.EXTENDED_STATUS_OFFLINE);

        supportedPidFormats.add(XmlPresenceInformationFactory.PIDF_XML);
        supportedPidFormats.add(XmlPresenceInformationFactory.XPIDF_XML);

    }


    /**
     * Sets the current presence status of the client.
     * @param basicStatusString a string representing the new status of the client
     * @throws CommunicationsException if setting the status fails for some
     * reason
     */
    public void requestStatusChange(String extendedStatus)
        throws CommunicationsException
    {
        if(extendedStatus == null )
            throw new CommunicationsException("null is not a valid presence status");

        PresenceTuple oldStatus = (PresenceTuple)currentStatus.clone();

        if(  extendedStatus.equals(PresenceTuple.EXTENDED_STATUS_OFFLINE))
             this.currentStatus.setBasicStatus(PresenceTuple.BASIC_STATUS_CLOSED);
        else
             this.currentStatus.setBasicStatus(PresenceTuple.BASIC_STATUS_OPEN);

        this.currentStatus.setExtendedStatus( extendedStatus );

        fireStatusChanged(currentStatus, oldStatus);

        //if this is not a closed status - save it as the last chosen status
        // so that it gets automatically selected next time the client is started
        if(!currentStatus.getBasicStatus().equals(PresenceTuple.BASIC_STATUS_CLOSED))
            Utils.setProperty("net.java.sip.communicator.sip.simple.LAST_SELECTED_OPEN_STATUS",
                              extendedStatus);
        new ListNotificationThread().start();
    }

    /**
     * Returns a string describing the current basic status of the client.
     * @return a string describing the current basic status of the client.
     */
    public String getBasicStatus()
    {
        return currentStatus.getBasicStatus();
    }

    /**
     * Returns a string describing the current basic status of the client.
     * @return a string describing the current basic status of the client.
     */
    public PresenceTuple getCurrentStatus()
    {
        return currentStatus;
    }


    /**
     * Returns an iterator over strings representing supported
     * @return an iterator over strings representing supported
     */
    public Iterator getSupportedStatusSet()
    {
        if(supportedStatusSet.size() == 0)
        {
            supportedStatusSet.add(PresenceTuple.EXTENDED_STATUS_OFFLINE);
            supportedStatusSet.add(PresenceTuple.EXTENDED_STATUS_ONLINE);
            supportedStatusSet.add(PresenceTuple.EXTENDED_STATUS_BUSY);
            supportedStatusSet.add(PresenceTuple.EXTENDED_STATUS_AWAY);
        }

        return supportedStatusSet.iterator();
    }

    /**
     * Adds a listener for the StatusEvents posted after status changes.
     *
     * @param l the listener to add
     */
    public void addStatusListener(StatusListener l)
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
    public void removeStatusListener(StatusListener l)
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
    protected void fireStatusChanged(PresenceTuple  newStatus,
                                     PresenceTuple  oldStatus)
    {
        StatusChangeEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listenerList.size()-1; i>=0; i--) {
            // Lazily create the event:
            if (e == null)
                e = new StatusChangeEvent(newStatus, oldStatus);
            ((StatusListener)listenerList.get(i)).statusChanged(e);
        }
    }

    /**
     * Sets the presence uri this presence agent is handling
     *
     * @param presenceUri the presence entity uri this presence agent is
     * handling
     */
    public void setPresenceEntityUri(GenericURI presenceUri)
    {
        currentStatus.setPresenceUri(presenceUri);
        bogusOfflineStatus.setPresenceUri(presenceUri);
    }

    /**
     * Sets the presence uri this presence agent is handling
     *
     * @param presenceUri the presence entity uri this presence agent is
     * handling
     */
    public void setPresenceEntityUriString(String presenceUri)
    {
        GenericURI uri = GenericURI.parseURI(presenceUri);
        currentStatus.setPresenceUri(uri);
        bogusOfflineStatus.setPresenceUri(uri);
    }


    /**
     * Adds the specified contact uri to the list of local contact URIs,
     * assigning it the specified priority.
     */
    public void addContactUri(String contactUri, float priority)
    {
        ContactUri uri = new ContactUri();
        uri.setContactValue(contactUri);
        uri.setPriority(priority);

        currentStatus.addContact(uri);
        bogusOfflineStatus.addContact(uri);
    }


    /**
     * Returns a set of MIME strings describing supported presence information
     * data formats.
     *
     * @return a java.util.Enumeration of MIME strings describing supprted
     *   NOTIFY content types
     */
    public Enumeration getSupportedPresenceInformationDataFormats()
    {
        return supportedPidFormats.elements();
    }

    /**
     * Returns a byte array describing the PUA's current state in the data format
     * specified bye <code>contentType</code>.
     *
     * @param authorization SubscriptionAuthorizationResponse
     * @param contentType a MIME String specifying the format to be used when
     *   encoding the presence information
     * @return a binary array containing a description of the PUA's presence
     *   state.
     * @throws CommunicationsException if the specified content-type is not
     * supported or if an error occurrs while generating xml data.
     */
    public byte[] getPresenceInformationData(
                        SubscriptionAuthorizationResponse authorization,
                        String contentType)
        throws CommunicationsException
    {
        if (   authorization == null
            || authorization.getResponseCode() == SubscriptionAuthorizationResponse.AUTHORISATION_REFUSED)
                return XmlPresenceInformationFactory.
                    serializePresenceData(contentType,
                                          getBogusOfflineStatus());
        else
               return XmlPresenceInformationFactory.serializePresenceData(
                                               contentType, getCurrentStatus());
    }

    /**
     * Returns a status with basic and extended properties set to CLOSED and
     * OFFLINE respectively. The purpose of the method is to provide presence
     * information data for NOTIFY respone requests to non authorized
     * subscription requests
     *
     * @return A PresenceTuple indicating a status with basic and extended
     * properties set to CLOSED and OFFLINE respectively.
     */
    private PresenceTuple getBogusOfflineStatus()
    {
        return bogusOfflineStatus;
    }

    /**
     * Sets an instance of SubscriptionAutority that woul convey subscription
     * authorisation requests to the user (or another subscription authority)
     * @param authority a valid SubscriptionAuthority instance.
     */
    public void setSubscritpionAuthority(SubscriptionAuthority authority)
    {
        this.subscriptionAuthority = authority;
    }

    /**
     * Returns a SubscriptionAuthorizationResponse with a code indicating whether
     * or not the specified presence entity is authorised to subscribe for our
     * presence state notifications.
     *
     * @param displayName the presence entity requesting the subscription
     * @param address String
     * @param message String
     * @param acceptedResponses String[]
     * @return an instance of SubscriptionAuthorizationResponse indicating
     *   whether or not the specified presence entity is authorised to
     *   subscribed for events concerning our presence state.
     */
    public SubscriptionAuthorizationResponse requestSubscriptionAuthorization(
        String displayName, String address, String message,
        String[] acceptedResponses)
    {
        /** @todo query list of already authorized subscriptions */

        return this.subscriptionAuthority.
                                requestSubscriptionAuthorization( displayName,
                                                                  address,
                                                                  message,
                                                                  acceptedResponses);
    }

    /**
     * The class is used to delegate a notifyAllSubscribers call to the presence
     * agent. Status changes are most often triggerred from the user interface.
     *
     */
    private class ListNotificationThread extends Thread
    {
        public void run()
        {
            if(   sipManCallback               != null
               && sipManCallback.presenceAgent != null )
                try {
                    sipManCallback.presenceAgent.notifyAllSubscribers();
                }
                catch (CommunicationsException ex) {
                    sipManCallback.fireCommunicationsError(
                        new CommunicationsException(
                            "Failed to notify all subscribers for your last "
                            +"status change."
                            ,ex));
                }
        }
    }


}

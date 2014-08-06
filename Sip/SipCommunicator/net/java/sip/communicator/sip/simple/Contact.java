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

/**
 * The class represents an entry in a communications contact list.
 * @author Emil Ivov
 * @version 1.0
 */

public class Contact
{
    protected Vector        contactUriList = new Vector();
    protected String        displayName    = null;
    private   PresenceTuple statusTuple    = null;
    protected GenericURI    presenceUri    = null;
    private   String        notes          = null;

    public Contact()
    {
        statusTuple = new PresenceTuple();
        statusTuple.setExtendedStatus(PresenceTuple.EXTENDED_STATUS_OFFLINE);
    }


    public Contact(GenericURI presenceUri)
    {
        setPresenceUri(presenceUri);
    }

    /**
     * Returns a name representing the contact.
     * @return a name representing this contact
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Returns the PresenceTuple describing the presence status of this contact.
     * @return the PresenceTuple describing the presence status of this contact.
     */
    public PresenceTuple getStatusTuple()
    {
        return statusTuple;
    }

    /**
     * Sets a name representing the contact
     * @param displayName a name representing this contact
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    /**
     * Sets the PresenceTuple describing the presence status of this contact.
     * @param statusTuple the PresenceTuple describing the presence status of
     * this contact.
     */
    public void setStatusTuple(PresenceTuple statusTuple)
    {
        this.statusTuple = statusTuple;
    }

    public ContactUri getContactUri(int index)
    {
        return (ContactUri)contactUriList.get(index);
    }

    public void addContactUri(ContactUri uri)
    {
        contactUriList.add(uri);
    }

    /**
     * Returns the display name of the contact.
     * @return the display name of the contact.
     */
    public String toString()
    {
        return getDisplayName();
    }

    /**
     * Determines whether the Contact has an open or closed basic presence status
     *
     * @return true if the Contact has an open presence status and false otherwrise.
     */
    public boolean isOnline()
    {
        return statusTuple != null
            && statusTuple.getBasicStatus() != null
            && statusTuple.getBasicStatus().equalsIgnoreCase(PresenceTuple.BASIC_STATUS_OPEN);
    }

    /**
     * Returns the presence URI that uniquely identifies this contact in the
     * contact list.
     * @return the presence URI that uniquely identifies this contact in the
     * contact list.
     */
    public GenericURI getPresenceUri()
    {
        return presenceUri;
    }

    /**
     * Sets a String containing any user comments on this contact.
     * @param notes a String containing any user comments on this contact.
     */
    public String getNotes()
    {
        return notes;
    }

    /**
     * Sets the presence URI that uniquely identifies this contact in the
     * contact list.
     * @param presenceUri the presence URI that uniquely identifies this contact in the
     * contact list.
     */
    public void setPresenceUri(GenericURI presenceUri)
    {
        this.presenceUri = presenceUri;
    }


    /**
     * Sets a String containing any user comments on this contact.
     * @param notes a String containing any user comments on this contact.
     */
    public void setNotes(String notes)
    {
        this.notes = notes;
    }
}

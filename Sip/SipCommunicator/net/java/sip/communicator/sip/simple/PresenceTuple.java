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
 * Instances of this class contain information about the presence status of the
 * client
 * @author Emil Ivov
 * @version 1.0
 */
public class PresenceTuple
{
    public static String BASIC_STATUS_OPEN = "open";
    public static String BASIC_STATUS_CLOSED = "closed";

    /**
     * The basic statuc contains one of the following strings: "open" or
     * "closed".
     *
     * The values "open" and "closed" indicate availability to receive INSTANT
     * MESSAGES if the <tuple> is for an instant messaging address. They also
     * indicate general availability for other communication means.
     *
     * open: In the context of INSTANT MESSAGES, this value means that the
     * associated <contact> element, if any, corresponds to an INSTANT INBOX
     * that is ready to accept an INSTANT MESSAGE.
     *
     * closed: In the context of INSTANT MESSAGES, this value means that the
     * associated <contact> element, if any, corresponds to an INSTANT INBOX
     * that is unable to accept an INSTANT MESSAGE.
     */
    protected String    basicStatus = BASIC_STATUS_CLOSED;

    public static String EXTENDED_STATUS_OFFLINE = "offline";
    public static String EXTENDED_STATUS_ONLINE  = "online";
    public static String EXTENDED_STATUS_BUSY    = "busy";
    public static String EXTENDED_STATUS_AWAY    = "away";

    protected String    extendedStatus = EXTENDED_STATUS_OFFLINE;

    private   TreeSet   contacts = new TreeSet();

    private   Hashtable notes    = new Hashtable();

    /**
     * The presence uri this PresenceTuple descibes.
     */
    private GenericURI presenceUri = null;

    public PresenceTuple()
    {
    }

    /**
     * Sets the basic status for this Presence instance.
     *
     * @param status the basic status for the current presence isntance.
     */
    void setBasicStatus(String status)
    {
        basicStatus = status;
    }

    /**
     * Returns the basic status for this Presence instance.
     *  The basic statuc contains one of the following strings: "open" or
     * "closed".
     *
     * @return the basic status for the current presence isntance.
     */
    public String getBasicStatus()
    {
        return basicStatus;
    }

    /**
     * Adds a contact to the presence instance. If a contact with the same URI
     * already exists in this tuple's list of contacts, it is removed.
     * @param contact the contact to add
     */
    public void addContact(ContactUri contact)
    {
        removeContact(contact.getContactValue());
        this.contacts.add(contact);
    }

    /**
     * Returns an iterator for the list of contacts contained by this
     * presence instance.
     * @return Iterator
     */
    public Iterator getContacts()
    {
        return contacts.iterator();
    }

    /**
     * Adds the specified note to the list of notes of this presence instance.
     * @param language the language used in note
     * @param note the message
     */
    public void addNote(Locale language, String note)
    {
        if(language == null)
           language = Locale.ENGLISH;

        notes.put(language, note);
    }

    /**
     * Returns a note in the specified language or null if there's no note for
     * that language
     * @param language Locale
     * @return a note in the specified language or null if there's no note for
     * that language
     */
    public String getNote(Locale language)
    {
        if (language == null)
            return( (String)notes.get(Locale.ENGLISH) );
        return (String)notes.get(language) ;
    }

    /**
     * Returns a set of languages in which we have notes in.
     * @return Enumeration
     */
    public Enumeration getNoteLanguages()
    {
        return notes.keys();
    }

    /**
     * Returns the presence uri this PresenceTuple describes.
     * @return the presence uri this PresenceTuple describes.
     */
    public GenericURI getPresenceUri()
    {
        return presenceUri;
    }

    /**
     * Sets the presence uri this PresenceTuple describes.
     *
     * @param presenceUri the presence uri this PresenceTuple describes.
     */
    public void setPresenceUri(GenericURI presenceUri)
    {
        this.presenceUri = presenceUri;
    }

    /**
     * Sets an extended, descriptive status string.
     * @param extendedStatus an extended, descriptive status string.
     */
    public void setExtendedStatus(String extendedStatus)
    {
        this.extendedStatus = extendedStatus;
    }

    /**
     * Returns an extended status String.
     * @return an extended status String.
     */
    public String getExtendedStatus()
    {
        return extendedStatus;
    }

    public Object clone()
    {
        PresenceTuple clone = new PresenceTuple();

        clone.setBasicStatus(new String(getBasicStatus()));
        clone.setExtendedStatus(new String(getExtendedStatus()));
        clone.setPresenceUri(getPresenceUri() == null? null : (GenericURI)getPresenceUri().clone());

        Iterator contacts = getContacts();
        while (contacts.hasNext()) {
            ContactUri contact = (ContactUri)contacts.next();
            clone.addContact(((ContactUri)contact.clone()));
        }

        Enumeration noteLangs = getNoteLanguages();
        while(noteLangs.hasMoreElements())
        {
            Locale lang = (Locale)noteLangs.nextElement();
            clone.addNote((Locale)lang, getNote(lang));
        }

        return clone;
    }

    /**
     * Removes the ContactUri object with the specified presence uri from the
     * set of contacts.
     * @param uri the uri of the contact to remove.
     */
    public void removeContact(String uri)
    {
        Iterator contactsIt = contacts.iterator();
        while (contactsIt.hasNext()) {
            ContactUri item = (ContactUri) contactsIt.next();
            if (   item.getContactValue() != null
                && item.getContactValue().equals(uri))
             {
                 contactsIt.remove();
                 return;
            }
        }
    }

}

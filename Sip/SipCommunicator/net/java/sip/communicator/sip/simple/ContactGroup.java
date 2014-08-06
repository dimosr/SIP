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
 * The class is used to store a set of contacts (buddies).
 *
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class ContactGroup extends Contact
{
    private String sourceLocation = null;

    private Vector contacts = new Vector();

    public ContactGroup()
    {
    }

    /**
     * Adds a (existing) contact to this group.
     * The method should not be accessed outside the sip package and adding new
     * contacts to the group must be handled by the <code>ContactListController</code>.
     * @param contact the contact to add to the group.
     * @return the index where the contact was added
     */
    public int addContact(Contact contact)
    {
        String displayName = contact.getDisplayName();
        int index = 0;
        for( int i = 0; (index = i) < contacts.size(); i++)
        {
            if( contact.getClass().equals(contacts.get(i).getClass())
                && displayName.compareToIgnoreCase( ((Contact)contacts.get(i)).getDisplayName()) < 0)
                break;
            else if (!(contact instanceof ContactGroup)
                     &&(contacts.get(i) instanceof ContactGroup ))
                continue;
            else
                break;
        }
        contacts.add(index, contact);
        return index;
    }

    /**
     * Removes the specified contact from this group
     * @param contact the contact to remove
     */
    public void removeContact(Contact contact)
    {
        contacts.remove(contact);
    }

    /**
     * Removes the contact that is on the specified position
     * @param index the index of the contact to remove.
     */
    public void removeContact(int index)
    {
        contacts.remove(index);
    }

    /**
     * Returns the node at the specified position.
     * @param index the index of the node to return
     * @return the contact (or ContactGroup) that is at the specified position
     */
    public Contact getChild(int index)
    {
        return (Contact)contacts.get(index);
    }

    /**
     * Returns the number of objects (contacts or contact groups) contained
     * by this group.
     * @return int the number of objects (contacts or contact groups) contained
     * by this group.
     */
    public int getChildCount()
    {
        return contacts.size();
    }

    /**
     * Returns the number of contacts (and not contact groups) contained by the
     * group. If recurseMemberGroups is set to true the method will return the
     * number of contacts contained by this group and those in its subgroups.
     *
     * @param recurseMemberGroups indicates whether the result should include
     * contacts contained in sub groups.
     * @return the number of contacts (and not contact groups) contained by the
     * group.
     */
    public int getContactCount(boolean recurseMemberGroups)
    {
        int contactCount = 0;
        for (int i = 0; i < getChildCount(); i++)
        {
            Contact contact = (Contact)getChild(i);
            if(contact instanceof ContactGroup)
            {
                if (recurseMemberGroups)
                    contactCount += ( (ContactGroup) contact).
                        getContactCount(recurseMemberGroups);
            }
            else
                contactCount++;

        }
        return contactCount;
    }

    /**
     * Returns the number of contacts in this group that are currently on-line
     * If recurseMemberGroups is set to true the method will return the
     * number of on-line contacts in this group and in its subgroups.
     *
     * @param recurseMemberGroups indicates whether the result should include
     * contacts contained in sub groups.
     * @return the number of contacts in the group that are currently "on-line"
     */
    public int getOnlineContactsCount(boolean recurseMemberGroups)
    {
        int contactCount = 0;
        for (int i = 0; i < getChildCount(); i++)
        {
            Contact contact = (Contact)getChild(i);
            if(contact instanceof ContactGroup)
            {
                if (recurseMemberGroups)
                    contactCount += ( (ContactGroup) contact).
                        getContactCount(recurseMemberGroups);
            }
            else
            {
                if (contact.isOnline())
                    contactCount++;
            }

        }
        return contactCount;
    }


    /**
     * Returns a string representation of the group and its current state - number
     * of "on-line" members and total number of contacts.
     * @return a string representation of the group and its current state - number
     * of "on-line" members and total number of contacts.
     */
    public String toString()
    {
        return getDisplayName()
            + " (" + getOnlineContactsCount(false) + "/" + getContactCount(false) + ")";
    }

    /**
     * Returns the first (direct) child contact with the specified presentity or
     * null if no such contact exists. The method won't recurse subgroups.
     * @param presentity the presence uri identifying the contact we're looking for.
     * @return the first (direct) child contact with the specified presentity or
     * null if no such contact exists.
     */
    public Contact getChildContact(GenericURI presentity)
    {
        int childIndex = getChildIndex(presentity);

        return childIndex == -1? null : getChild(childIndex);
    }

   /**
    * Returns the index of the first (direct) child contact with the specified
    * presentity or -1 if no such contact exists. The method won't recurse subgroups.
    * @param presentity the presence uri identifying the contact we're looking for.
    * @return the index of first (direct) child contact with the specified
    * presentity or -1 if no such contact exists.
    */
   public int getChildIndex(GenericURI presentity)
   {
       for(int i = 0; i < getChildCount(); i ++)
       {
           Contact childI = getChild(i);
           if(!(childI instanceof ContactGroup)
              &&childI.getPresenceUri().matches(presentity))
               return i;
       }
       return -1;
   }

   /**
    * Sets a String pointing to the location where this group got loaded from.
    * The field should only be used in the case of a root ContactGroup
    * (the contact list itself)
    * @param sourceLocation a String pointing to the location where this group
    * got loaded from.
    */
   void setSourceLocation(String sourceLocation)
   {
       this.sourceLocation = sourceLocation;
   }

   /**
   * Returns a String pointing to the location where this group got loaded from.
   * The field should only be used in the case of a root ContactGroup
   * (the contact list itself)
   * @return a String pointing to the location where this group
   * got loaded from.
   */
   String getSourceLocation()
   {
       return sourceLocation;
   }

   /**
    * Searches for the first occurence of the given contact, testing for equality
    * using the equals method.
    * @param contact the contact to look for.
    * @return he index of the first occurrence of the argument in this vector,
    * that is, the smallest value k such that elem.equals(elementData[k]) is
    * true; returns -1 if the object is not found.
    */
   public int indexOf(Contact contact)
   {
           return contacts.indexOf(contact);
   }

}

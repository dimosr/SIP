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
package net.java.sip.communicator.sip.simple.event;

import java.util.EventObject;
import net.java.sip.communicator.sip.simple.*;

/**
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */
public class NotificationReceivedEvent extends EventObject
{

    private ContactGroup[] sourceLocation = null;
    private PresenceTuple  newStatus      = null;
    private PresenceTuple  oldStatus      = null;

    /**
     * Creates a StatusChangeEvent indicating that the specified source contact
     * located at the given location (path) has changed status from oldStatus to
     * newStatus.
     * @param source The contact that changed status.
     * @param sourcePath the location of the contact that changed status. This
     * argument contains an array of ContactGroup-s starting from the root
     * ContactGroup going to the parent of the event source contact.
     * @param newStatus the new status of the specified source contact.
     * @param oldStatus the previos status of the specified soruce contact.
     */
    public NotificationReceivedEvent(Contact        source,
                             ContactGroup[] sourcePath,
                             PresenceTuple  newStatus,
                             PresenceTuple  oldStatus)
    {
        super(source);
        this.sourceLocation = sourcePath;
        this.newStatus      = newStatus;
        this.oldStatus      = oldStatus;
    }

    /**
     * Returns the contact that changed status.
     * @return the Contact that changed its status.
     */
    public Contact getSourceContact()
    {
        return (Contact)super.getSource();
    }

    /**
     * Returns the current status of the source contact.
     * @return the current status of the source contact.
     */
    public PresenceTuple getNewStatus()
    {
        return newStatus;
    }

    /**
     * Returns the status the source contact had before changing to newStatus.
     * @return the status the source contact had before changing to newStatus.
     */
    public PresenceTuple getOldStatus()
    {
        return oldStatus;
    }

    /**
     * Returns the location of the event source.
     * @return An array of contact groups representing the pat (all parents) from
     * the contact list root to source's direct parent.
     */
    public ContactGroup[] getSourceLocation()
    {
        return sourceLocation;
    }
}

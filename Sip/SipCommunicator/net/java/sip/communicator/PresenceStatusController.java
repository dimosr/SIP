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
package net.java.sip.communicator;

import net.java.sip.communicator.gui.imp.PresenceStatusControllerUIModel;
import net.java.sip.communicator.sip.simple.*;
import java.util.*;
import net.java.sip.communicator.sip.*;
import net.java.sip.communicator.common.*;
import net.java.sip.communicator.sip.simple.event.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class PresenceStatusController
    extends PresenceStatusControllerUIModel
    implements StatusListener
{
    private static final Console console = Console.getConsole(PresenceStatusController.class);

    private ArrayList             supportedStatusSet = new ArrayList();
    private PresenceStatusManager statusManager      = null;

    public PresenceStatusController(PresenceStatusManager statusManager)
    {
        this.statusManager = statusManager;
        Iterator supportedStatusSet = statusManager.getSupportedStatusSet();
        statusManager.addStatusListener(this);

        while (supportedStatusSet.hasNext()) {
            this.supportedStatusSet.add( supportedStatusSet.next());
        }
    }

    /**
     *
     * @return the currently active Presence Status of SipCommunicator
     */
    public Object getCurrentPresenceStatus()
    {
        return statusManager.getCurrentStatus().getExtendedStatus();
    }

    /**
     * Returns the value of the presence status corresponding to the specified
     * index.
     *
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    public Object getStatusAt(int index)
    {
        return supportedStatusSet.get(index);
    }

    /**
     * Returns the size of the supported stuatus set.
     *
     * @return the size of the supported stuatus set
     */
    public int getStatusCount()
    {
        return supportedStatusSet.size();
    }

    /**
     * Requests the underlying presence stack to change the current status to
     * <code>newStatus</code>.
     *
     * @param newStatus the list object to select or <code>null</code> to clear
     *   the selection
     */
    public void requestStatusChange(String newStatusDescriptorStr)
    {
        try {
            statusManager.requestStatusChange(newStatusDescriptorStr);
        }
        catch (CommunicationsException ex) {
            console.error(ex);
            Console.showException(ex);
        }
    }

    /**
     * statusChanged
     *
     * @param evt StatusChangeEvent
     */
    public void statusChanged(StatusChangeEvent evt)
    {
        this.fireContentsChanged(this, 0, this.getStatusCount());
    }
}

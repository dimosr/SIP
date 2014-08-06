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
package net.java.sip.communicator.sip;

import java.util.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import net.java.sip.communicator.sip.event.*;
import net.java.sip.communicator.common.*;

/**
 * <p>Title: SIP COMMUNICATOR</p>
 * <p>Description:JAIN-SIP Audio/Video phone application</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr) </p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
 * <p>Louis Pasteur University - Strasbourg - France</p>
 * @author Emil Ivov (http://www.emcho.com)
 * @version 1.1
 *
 */
public class Call
{
    private static final Console console = Console.getConsole(Call.class);

    public static final String DIALING         = "Dialing";
    public static final String RINGING         = "Ringing";
    public static final String ALERTING        = "Alerting";
    public static final String CONNECTED       = "Connected";
    public static final String DISCONNECTED    = "Disconnected";
    public static final String BUSY            = "Busy";
    public static final String FAILED          = "Failed";

    //Used for mobility - TEST CODE.
    public static final String MOVING_LOCALLY  = "Moving Locally";
    public static final String MOVING_REMOTELY = "Moving Remotely";
    public static final String RECONNECTED     = "reConnected";

    //Call Properties
    private Dialog dialog = null;
    private String remoteSdpDescription = null;
    /**
     * While in its early state the dialog cannot provide us with its
     * corresponding transaction as it is not yet created
     * That's where the initialRequest field comes in.
     */
    private Request initialRequest = null;
    private String callState = "";
    //Event Management
    ArrayList listeners = new ArrayList();
    public String getState()
    {
        return callState;
    }

    public boolean isIncoming()
    {
        //Let it throw a null pointer exception if necessary
        return dialog.isServer();
    }

    void setDialog(Dialog dialog)
    {
        this.dialog = dialog;
    }

    Dialog getDialog()
    {
        return dialog;
    }

    //SDP Data
    void setRemoteSdpDescription(String data)
    {
        if( console.isDebugEnabled() )
            console.debug("setting remote description to [" + data + "]");
        this.remoteSdpDescription = data;
    }

    public String getRemoteSdpDescription()
    {
        return remoteSdpDescription;
    }

    void setState(String newStatus)
    {
        try
        {
            console.logEntry();

            if(newStatus.equals(getState()))
                return;

            if( console.isDebugEnabled() )
                console.debug("setting call status to "+newStatus);

            String oldStatus = callState;
            this.callState = newStatus;
            fireCallStatusChangedEvent(oldStatus);
        }
        finally
        {
            console.logExit();
        }

    }

    public String getAddress()
    {
        if (dialog.getState() != null) {
            return dialog.getRemoteParty().getURI().toString();
        }
        else {
            if (dialog.isServer()) {
                FromHeader fromHeader = (FromHeader) initialRequest.getHeader(
                    FromHeader.NAME);
                return fromHeader.getAddress().getURI().toString();
            }
            else {
                ToHeader toHeader = (ToHeader) initialRequest.getHeader(
                    ToHeader.NAME);
                return toHeader.getAddress().getURI().toString();
            }
        }
    }

    public String getRemoteName()
    {
        Address address;
        if (dialog.getState() != null) {
            address = dialog.getRemoteParty();
        }
        else {
            if (dialog.isServer()) {
                FromHeader fromHeader = (FromHeader) initialRequest.getHeader(
                    FromHeader.NAME);
                address = fromHeader.getAddress();
            }
            else {
                ToHeader toHeader = (ToHeader) initialRequest.getHeader(
                    ToHeader.NAME);
                address = toHeader.getAddress();
            }
        }
        String retVal = null;
        if (address.getDisplayName() != null
            && address.getDisplayName().trim().length() > 0) {
            retVal = address.getDisplayName();
        }
        else {
            URI uri = address.getURI();
            if (uri.isSipURI()) {
                retVal = ( (SipURI) uri).getUser();
            }
        }
        return retVal == null ? "" : retVal;
    }

    public int getID()
    {
        return hashCode();
    }

    void setInitialRequest(Request request)
    {
        this.initialRequest = request;
    }

    String getDialogID()
    {
        return dialog.getDialogId();
    }

    public String toString()
    {
        return "[ Call " + getID()
            + "\nfrom " + getRemoteName()+ "@" + getAddress()
            + "\nSDP:" + getRemoteSdpDescription()
            + "]";
    }

//====================== EVENTS ===========================
    public void addStateChangeListener(CallListener listener)
    {
        listeners.add(listener);
    }

    public void fireCallStatusChangedEvent(String oldStatus)
    {
        CallStateEvent evt = new CallStateEvent(this);
        evt.setOldState(oldStatus);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            ( (CallListener) listeners.get(i)).callStateChanged(evt);
        }
    }
}
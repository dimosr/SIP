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
package net.java.sip.communicator.media;

/**
 * <p>Title: SIP COMMUNICATOR</p>
 * <p>Description:JAIN-SIP Audio/Video phone application</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr) </p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
 * <p>Louis Pasteur University - Strasbourg - France</p>
 * @author Emil Ivov (http://www.emcho.com)
 * @author Paulo Pizzarro ( added support for media level connection parameter)
 * @version 1.1
 *
 */
import java.util.*;
import java.net.*;
import javax.media.*;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.media.rtp.*;
import javax.media.rtp.event.*;
import net.java.sip.communicator.common.*;

/**
 * AVReceiver to receive RTP transmission using the new RTP API.
 */
class AVReceiver
    implements ReceiveStreamListener, SessionListener,
    ControllerListener, SendStreamListener
{
    private static Console console = Console.getConsole(AVReceiver.class);
    private DataSource dataSource = null;
    net.java.sip.communicator.media.MediaManager mediaManager;
    ArrayList sessions = null;
    RTPManager mgrs[] = null;
    boolean dataReceived = false;
    Object dataSync = new Object();

    private int bindRetries = 1;
    public static final int DEFAULT_BUFFER_LENGTH = 350;

    public AVReceiver(ArrayList sessions)
    {
        this.sessions = sessions;
        String retries = null;
        if((retries = Utils.getProperty("net.java.sip.communicator.media.RECEIVER_BIND_RETRIES")) != null)
            try {
                bindRetries = Integer.valueOf(retries).intValue();
            }
            catch (NumberFormatException ex) {
                console.error(retries + " is not a valid number. ignoring property", ex);
            }
    }

    void setMediaManager(MediaManager mManager)
    {
        this.mediaManager = mManager;
    }

    protected boolean initialize()
    {
        try {
            console.logEntry();
            InetAddress ipAddr;
            SessionAddress localAddr = new SessionAddress();
            SessionAddress destAddr;
            mgrs = new RTPManager[sessions.size()];
            SessionLabel session;
            // Open the RTP sessions.
            for (int i = 0; i < sessions.size(); i++) {
                // Parse the session addresses.
                try {
                    session = new SessionLabel((String)sessions.get(i));
                }
                catch (IllegalArgumentException e) {
                    console.error(
                        "Failed to parse the session address given: "
                        + (String)sessions.get(i));
                    console.logExit();
                    return false;
                }
                if (console.isDebugEnabled()) {
                    console.debug(
                        " Start listening for RTP @ addr: "
                        + session.addr + " port: " + session.port
                        + " ttl: " + session.ttl);
                }
                mgrs[i] = mediaManager.getRtpManager(new SessionAddress(mediaManager.
                            getLocalHost(),
                            session.port));
                if(mgrs[i] == null)
                {
                    mgrs[i] = (RTPManager) RTPManager.newInstance();
                    mediaManager.putRtpManager(new SessionAddress(mediaManager.
                        getLocalHost(),
                        session.port), mgrs[i]);
                }
                mgrs[i].addSessionListener(this);
                mgrs[i].addReceiveStreamListener(this);
                mgrs[i].addSendStreamListener(this);
                ipAddr = InetAddress.getByName(session.addr);
                int tries = 0;
                while (tries++ < bindRetries)
                {
                    if (ipAddr.isMulticastAddress()) {
                        // local and remote address pairs are identical:
                        localAddr = new SessionAddress(ipAddr,
                            session.port,
                            session.ttl);
                        destAddr = new SessionAddress(ipAddr,
                            session.port,
                            session.ttl);
                    }
                    else {
                        localAddr = new SessionAddress(mediaManager.
                            getLocalHost(),
                            session.port);
                        destAddr = new SessionAddress(ipAddr, session.port);
                    }
                    try {
                        mgrs[i].initialize(localAddr);
                    }
                    catch (Exception exc) {
                        if (tries < bindRetries) {
                            continue;
                        }
                        console.error(
                            "Could not initialize rtp manager!",exc);
                        return false;
                    }
                    // You can try out some other buffer size to see
                    // if you can get better smoothness.
                    BufferControl bc = (BufferControl) mgrs[i].getControl(
                        "javax.media.control.BufferControl");
                    if (bc != null) {
                        int bl = DEFAULT_BUFFER_LENGTH;
                        try {
                            bl = Integer.parseInt(Utils.getProperty(
                                "net.java.sip.communicator.media.MEDIA_BUFFER_LENGTH"));
                            console.debug("MEDIA_BUFFER_LENGTH length is set to " + DEFAULT_BUFFER_LENGTH);
                        }
                        catch (NumberFormatException ex) {
                            console.debug("MEDIA_BUFFER_LENGTH length not specified using default " + DEFAULT_BUFFER_LENGTH, ex);
                        }

                        bc.setBufferLength(bl);
                    }
                    mgrs[i].addTarget(destAddr);
                    break; //port retries
                } //port retries
            }
        }
        catch (Exception e) {
            console.error("Cannot create the RTP Session: ", e);
            console.logExit();
            return false;
        }
        console.logExit();
        return true;
    }

    public boolean isDone()
    {
        return false;
    }

    /**
     * Close the players and the session managers.
     */
    protected void close()
    {
        try {
            console.logEntry();
            // close the RTP session.
            for (int i = 0; i < mgrs.length; i++) {
                if (mgrs[i] != null) {
                    if (console.isDebugEnabled()) {
                        console.debug("Stopped mgr " + (int) (i + 1));
                    }
                    mgrs[i].removeTargets("Closing session from AVReceiver");
                    mgrs[i].dispose();
                    mgrs[i] = null;
                }
            }
        }
        finally {
            console.logExit();
        }
    }

    /**
     * SessionListener.
     */
    public synchronized void update(SessionEvent evt)
    {
        try {
            console.logEntry();
            if (evt instanceof NewParticipantEvent) {
                Participant p = ( (NewParticipantEvent) evt).getParticipant();
                if (console.isDebugEnabled()) {
                    console.debug("A new participant had just joined: "
                                  + p.getCNAME());
                }
            }
            else {
                if (console.isDebugEnabled()) {
                    console.debug(
                        "Received a the following JMF Session event - "
                        + "evt.getClass().getName()");
                }
            }
        }
        finally {
            console.logExit();
        }
    }

    /**
     * ReceiveStreamListener
     */
    public synchronized void update(ReceiveStreamEvent evt)
    {
        try {
            console.logEntry();
            RTPManager mgr = (RTPManager) evt.getSource();
            Participant participant = evt.getParticipant(); // could be null.
            ReceiveStream stream = evt.getReceiveStream(); // could be null.
            if (evt instanceof NewReceiveStreamEvent) {
                try {
                    stream = ( (NewReceiveStreamEvent) evt).getReceiveStream();
                    DataSource ds = stream.getDataSource();
                    // Find out the formats.
                    RTPControl ctl = (RTPControl) ds.getControl(
                        "javax.media.rtp.RTPControl");
                    if (console.isDebugEnabled()) {
                        if (ctl != null) {
                            console.debug("Recevied new RTP stream: "
                                          + ctl.getFormat());
                        }
                        else {
                            console.debug("Recevied new RTP stream");
                        }
                    }
                    Player p = Manager.createPlayer(ds);
                    p.addControllerListener(this);
                    p.realize();
                }
                catch (Exception e) {
                    console.error("NewReceiveStreamEvent exception ", e);
                    return;
                }
            }
            else if (evt instanceof StreamMappedEvent) {
                if (stream != null && stream.getDataSource() != null) {
                    DataSource ds = stream.getDataSource();
                    // Find out the formats.
                    RTPControl ctl = (RTPControl) ds.getControl(
                        "javax.media.rtp.RTPControl");
                    if (console.isDebugEnabled()) {
                        String msg = "The previously unidentified stream ";
                        if (ctl != null) {
                            msg += ctl.getFormat();
                        }
                        msg += " had now been identified as sent by: "
                            + participant.getCNAME();
                        console.debug(msg);
                    }
                }
            }
            else if (evt instanceof ByeEvent) {
                console.debug("Got \"bye\" from: " + participant.getCNAME());
            }
        }
        finally {
            console.logExit();
        }
    }

    /**
     * ControllerListener for the Players.
     */
    public synchronized void controllerUpdate(ControllerEvent ce)
    {
        try {
            console.logEntry();
            Player p = (Player) ce.getSourceController();
            if (p == null) {
                return;
            }
            // Get this when the internal players are realized.
            if (ce instanceof RealizeCompleteEvent) {
                console.debug("A player was realized and will be started.");
                p.start();
            }
            if (ce instanceof StartEvent) {
                console.debug("Received a StartEvent");
                mediaManager.firePlayerStarting(p);
            }
            if (ce instanceof ControllerErrorEvent) {
                console.error(
                    "The following error was reported while starting a player"
                    + ce);
            }
            if (ce instanceof ControllerClosedEvent) {
                console.debug("Received a ControllerClosedEvent");
                mediaManager.firePlayerStopped();
            }
        }
        finally {
            console.logExit();
        }
    }

    /**
     * A utility class to parse the session addresses.
     */
    class SessionLabel
    {
        public String addr = null;
        public int port;
        public int ttl = 1;
        private Console console = Console.getConsole(SessionLabel.class);
        SessionLabel(String session) throws IllegalArgumentException
        {
            try {
                console.logEntry();
                int off;
                String portStr = null, ttlStr = null;
                if (session != null && session.length() > 0) {
                    while (session.length() > 1 && session.charAt(0) == '/') {
                        session = session.substring(1);
                        // Now see if there's a addr specified.
                    }
                    off = session.indexOf('/');
                    if (off == -1) {
                        if (!session.equals("")) {
                            addr = session;
                        }
                    }
                    else {
                        addr = session.substring(0, off);
                        session = session.substring(off + 1);
                        // Now see if there's a port specified
                        off = session.indexOf('/');
                        if (off == -1) {
                            if (!session.equals("")) {
                                portStr = session;
                            }
                        }
                        else {
                            portStr = session.substring(0, off);
                            session = session.substring(off + 1);
                            // Now see if there's a ttl specified
                            off = session.indexOf('/');
                            if (off == -1) {
                                if (!session.equals("")) {
                                    ttlStr = session;
                                }
                            }
                            else {
                                ttlStr = session.substring(0, off);
                            }
                        }
                    }
                }
                if (addr == null) {
                    throw new IllegalArgumentException();
                }
                if (portStr != null) {
                    try {
                        Integer integer = Integer.valueOf(portStr);
                        if (integer != null) {
                            port = integer.intValue();
                        }
                    }
                    catch (Throwable t) {
                        throw new IllegalArgumentException();
                    }
                }
                else {
                    throw new IllegalArgumentException();
                }
                if (ttlStr != null) {
                    try {
                        Integer integer = Integer.valueOf(ttlStr);
                        if (integer != null) {
                            ttl = integer.intValue();
                        }
                    }
                    catch (Throwable t) {
                        throw new IllegalArgumentException();
                    }
                }
            }
            finally {
                console.logExit();
            }
        }
    }

    public void update(SendStreamEvent event)
    {
        console.debug(
            "received the following JMF Session event - "
            + event.getClass().getName());
    }

    public static void main(String argv[])
    {
        if (argv.length == 0) {
            prUsage();
        }
        ArrayList sessions = new ArrayList();
        for(int i = 0; i<argv.length; i++) {
            sessions.add(argv[i]);
        }
        AVReceiver avReceiver = new AVReceiver(sessions);
        if (!avReceiver.initialize()) {
            System.out.println(
                "[AVReceiver]"
                + "Failed to initialize the sessions.");
            System.exit( -1);
        }
        // Check to see if AVReceive2 is done.
        try {
            while (!avReceiver.isDone()) {
                Thread.sleep(1000);
            }
        }
        catch (Exception e) {}
        System.out.println(
            "[AVReceiver]"
            + "Exiting AVReceive2");
    }

    static void prUsage()
    {
        System.err.println("Usage: AVReceive2 <session> <session> ...");
        System.err.println("     <session>: <address>/<port>/<ttl>");
        System.exit(0);
    }
    /*
        private DataSource addDataSource(DataSource newDataSource)
        {
            if (dataSource == null) {
                dataSource = newDataSource;
            }
            else {
                try {
         dataSource = Manager.createMergingDataSource(new DataSource[] {
                        dataSource,
                        newDataSource
                    });
                }
                catch (IncompatibleSourceException ex) {
                    Console.println(
         "[AVReceiver]Failed to create a merging datasource. Old one is ignored!");
                    dataSource = newDataSource;
                }
            }
            return dataSource;
        }
     */
} // end of AVReceive2

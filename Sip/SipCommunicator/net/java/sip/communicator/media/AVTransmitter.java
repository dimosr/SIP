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
import java.io.*;
import java.net.*;
import java.util.*;
import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.rtp.*;
import javax.media.rtp.rtcp.*;
import java.awt.*;
import net.java.sip.communicator.common.*;
import net.java.sip.communicator.common.Console;

class AVTransmitter
{
    protected static Console console = Console.getConsole(AVTransmitter.class);
    // Input MediaLocator
    // Can be a file or http or capture source
    protected MediaLocator locator;
    protected ArrayList ipAddresses = null;
    protected Processor processor = null;
    protected RTPManager rtpMgrs[];
    //Used by mobility - keeps rtpMgrs[] corresponding addresses
    protected SessionAddress sessionAddresses[] = null;

    protected DataSource dataOutput = null;
    protected ArrayList ports = null;
    protected ArrayList formatSets = null;
    protected MediaManager mediaManCallback = null;

    public AVTransmitter(Processor processor,
                         ArrayList ipAddresses,
                         ArrayList ports,
                         ArrayList formatSets)
    {
        try {
            console.logEntry();
            this.processor = processor;
            this.ipAddresses = ipAddresses;
            this.ports = ports;
            this.formatSets = formatSets;
            if (console.isDebugEnabled()) {
                console.debug(
                    "Created transmitter for: "
                    + ipAddresses.toString()
                    + " at ports: "
                    + ports.toString()
                    + " encoded as: "
                    + formatSets.toString());
            }
        }
        finally {
            console.logExit();
        }
    }

    void setMediaManagerCallback(MediaManager mediaManager)
    {
        this.mediaManCallback = mediaManager;
    }

    /**
     * Starts the transmission. Returns null if transmission started ok.
     * Otherwise it returns a string with the reason why the setup failed.
     */
    synchronized String start() throws MediaException
    {
        try {
            console.logEntry();
            String result;
            configureProcessor();
            // Create an RTP session to transmit the output of the
            // processor to the specified IP address and port no.
            try {
                createTransmitter();
            }
            catch (MediaException ex) {
                console.error("createTransmitter() failed", ex);
                processor.close();
                processor = null;
                throw ex;
            }
            // Start the transmission
            processor.start();
            return null;
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Stops the transmission if already started
     */
    void stop()
    {
        try {
            console.logEntry();
            synchronized (this) {
                if (processor != null) {
                    processor.stop();
                    if (rtpMgrs != null) {
                        for (int i = 0; i < rtpMgrs.length; i++) {
                            if (rtpMgrs[i] == null) {
                                continue;
                            }
                            rtpMgrs[i].removeTargets("Session ended.");
                            rtpMgrs[i].dispose();
                        }
                    }
                }
            }
        }
        finally {
            console.logExit();
        }
    }

    protected void configureProcessor() throws MediaException
    {
        try {
            console.logEntry();
            if (processor == null) {
                console.error("Processor is null.");
                throw new MediaException("Processor is null.");
            }
            // Wait for the processor to configure
            boolean result = true;
            if (processor.getState() < Processor.Configured) {
                result = waitForState(processor, Processor.Configured);
            }
            if (result == false) {
                console.error("Couldn't configure processor");
                throw new MediaException("Couldn't configure processor");
            }
            // Get the tracks from the processor
            TrackControl[] tracks = processor.getTrackControls();
            // Do we have atleast one track?
            if (tracks == null || tracks.length < 1) {
                console.error("Couldn't find tracks in processor");
                throw new MediaException("Couldn't find tracks in processor");
            }
            // Set the output content descriptor to RAW_RTP
            // This will limit the supported formats reported from
            // Track.getSupportedFormats to only valid RTP formats.
            ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.
                RAW_RTP);
            processor.setContentDescriptor(cd);
            Format supported[];
            Format chosenFormat;
            boolean atLeastOneTrack = false;
            // Program the tracks.
            for (int i = 0; i < tracks.length; i++) {
                Format format = tracks[i].getFormat();
                if (tracks[i].isEnabled()) {
                    supported = tracks[i].getSupportedFormats();
                    if (console.isDebugEnabled()) {
                        console.debug("Available encodings are:");
                        for (int j = 0; j < supported.length; j++) {
                            console.debug("track[" + (i + 1) + "] format[" +
                                          (j + 1) + "]="
                                          + supported[j].getEncoding());
                        }
                    }
                    // We've set the output content to the RAW_RTP.
                    // So all the supported formats should work with RTP.
                    // We'll pick one that matches those specified by the constructor.
                    if (supported.length > 0) {
                        if (supported[0] instanceof VideoFormat) {
                            // For video formats, we should double check the
                            // sizes since not all formats work in all sizes.
                            int index = findFirstMatchingFormat(supported,
                                formatSets);
                            if (index != -1) {
                                chosenFormat = checkForVideoSizes(tracks[i].
                                    getFormat(),
                                    supported[index]);
                                tracks[i].setFormat(chosenFormat);
                                if (console.isDebugEnabled()) {
                                    console.debug("Track " + i
                                                  + " is set to transmit as: " +
                                                  chosenFormat);
                                }
                                atLeastOneTrack = true;
                            }
                            else {
                                tracks[i].setEnabled(false);
                            }
                        }
                        else {
                            int index = findFirstMatchingFormat(supported,
                                formatSets);
                            if (index != -1) {
                                tracks[i].setFormat(supported[index]);
                                if (console.isDebugEnabled()) {
                                    console.debug("Track " + i +
                                                  " is set to transmit as: "
                                                  + supported[index]);
                                }
                                atLeastOneTrack = true;
                            }
                            else {
                                tracks[i].setEnabled(false);
                            }
                        }
                    }
                    else {
                        tracks[i].setEnabled(false);
                    }
                }
                else {
                    tracks[i].setEnabled(false);
                }
            }
            if (!atLeastOneTrack) {
                console.error(
                    "Couldn't set any of the tracks to a valid RTP format");
                throw new MediaException(
                    "Couldn't set any of the tracks to a valid RTP format");
            }
            // Realize the processor. This will internally create a flow
            // graph and attempt to create an output datasource
            result = waitForState(processor, Controller.Realized);
            if (result == false) {
                console.error("Couldn't realize processor");
                throw new MediaException("Couldn't realize processor");
            }
            // Set the JPEG quality to .5.
            //TODO set the jpeg quality through a property
            setJPEGQuality(processor, 1f);
            // Get the output data source of the processor
            dataOutput = processor.getDataOutput();
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Use the RTPManager API to create sessions for each media
     * track of the processor.
     */
    protected void createTransmitter() throws MediaException
    {
        try {
            console.logEntry();
            PushBufferDataSource pbds = (PushBufferDataSource) dataOutput;
            PushBufferStream pbss[] = pbds.getStreams();
            rtpMgrs = new RTPManager[pbss.length];
            //used by mobility
            sessionAddresses = new SessionAddress[pbss.length];
            SessionAddress localAddr, destAddr;
            InetAddress remoteAddress;
            SendStream sendStream;
            SourceDescription srcDesList[];
            console.debug("data sources - " + pbss.length);
            int port = 0;
            String format = null;
            String ipAddress = null;
            for_loop:
            for (int i = 0; i < pbss.length; i++) {
                try {
                    format = pbss[i].getFormat().getEncoding();
                    ipAddress = findIPAddressForFormat(format);
                    if(ipAddress == null) {
                        console.error("failed to find a format's ipAddress");
                        throw new MediaException(
                            "Internal error! AVTransmitter failed to find a"
                            + " format's corresponding ipAddress");
                    }
                    remoteAddress = InetAddress.getByName(ipAddress);
                }
                catch (UnknownHostException ex) {
                    console.error("Failed to resolve remote address", ex);
                    throw new MediaException("Failed to resolve remote address",
                                             ex);
                }
                port = findPortForFormat(format);
                if (port == -1) {
                    console.error("failed to find a format's port");
                    throw new MediaException(
                        "Internal error! AVTransmitter failed to find a"
                        + " format's corresponding port");
                }
                // first try to bind to same port we're talking to for firewall
                // support if that fails go for a random one
                // which will be randomly changed and retried retryCount times.
                // (erroneous comment reported by Joe.Provino at Sun.COM)
                boolean success = true;
                boolean createdRtpManager = false;
                int retries = 4; // 4 retries
                do {
                    success = true;
                    int localPort = 0;

                    if(retries ==4)
                        localPort = port;
                    else
                        localPort = (int) (63976 * Math.random()) + 1024;

                    localAddr = new SessionAddress(mediaManCallback.
                        getLocalHost(), localPort);
                    destAddr = new SessionAddress(remoteAddress, port);
                    rtpMgrs[i] =  mediaManCallback.getRtpManager(localAddr);
                    if(rtpMgrs[i] == null)
                    {
                        rtpMgrs[i] = RTPManager.newInstance();
                        createdRtpManager = true;
                    }
                    else
                    {
                        success = true;
                        break;
                    }
                    try {
                        rtpMgrs[i].initialize(localAddr);
                        console.debug("Just bond to port" + localAddr.getDataPort());
                        rtpMgrs[i].addTarget(destAddr);
                        sessionAddresses[i] = destAddr;
                    }
                    catch (InvalidSessionAddressException ex) {
                        //port was occupied
                        if (console.isDebugEnabled()) {
                            console.debug("Couldn't bind to local ports "
                                          + localAddr.getDataPort() + ", " +
                                          localAddr.getControlPort()
                                          + " @ " +
                                          localAddr.getControlHostAddress()
                                          + ".\n Exception message was: " +
                                          ex.getMessage()
                                          + " Will try another pair!");
                        }
                        success = false;
                    }
                    catch (IOException ex) {
                        //we should just try to notify user and continue with other tracks
                        console.error(
                            "Failed to initialize an RTPManager for address pair:\n"
                            + "Local address:" + localAddr.toString()
                            + " data port:" + localAddr.getDataPort()
                            + " control port:" + localAddr.getControlPort() +
                            "\n"
                            + "Dest  address:" + destAddr
                            + " data port:" + destAddr.getDataPort()
                            + " control port:" + destAddr.getControlPort(),
                            ex);
                        mediaManCallback.fireNonFatalMediaError(new
                            MediaException(
                            "Failed to initialize an RTPManager for address pair:\n"
                            + "Local address:" + localAddr.toString()
                            + " data port:" + localAddr.getDataPort()
                            + " control port:" + localAddr.getControlPort() +
                            "\n"
                            + "Dest  address:" + destAddr
                            + " data port:" + destAddr.getDataPort()
                            + " control port:" + destAddr.getControlPort(),
                            ex));
                        success = false;
                        retries = 0;
                    }
                }
                while (!success && --retries > 0);
                //notify user if we could bind at all
                if (!success) {
                    if (console.isDebugEnabled()) {
                        console.error(
                            "Failed to initialise rtp manager for track " + i
                            + " encoded as " + pbss[i].getFormat().getEncoding()
                            + " @ [" + ipAddress + "]:" + port + "!");
                    }
                    mediaManCallback.fireNonFatalMediaError(
                        new MediaException(
                        "Failed to initialise rtp manager for track " + i
                        + " encoded as " + pbss[i].getFormat().getEncoding()
                        + " @ [" + ipAddress + "]:" + port + "!"));
                    continue;
                }
                if(createdRtpManager)
                    mediaManCallback.putRtpManager(localAddr, rtpMgrs[i]);
                try {
                    sendStream = rtpMgrs[i].createSendStream(dataOutput, i);
                    sendStream.start();
                    if (console.isDebugEnabled()) {
                        console.debug("Started transmitting track " + i
                                      + " encoded as " +
                                      pbss[i].getFormat().getEncoding()
                                      + " @ [" + ipAddress + "]:" + port + "!");
                    }
                }
                catch (Exception ex) {
                    console.error("Session " + i +
                                  " failed to start transmitting.");
                    throw new MediaException(
                        "Session " + i + " failed to start transmitting.");
                }
            }
        }
        finally {
            console.logExit();
        }
    }

    /**
     * For JPEG and H263, we know that they only work for particular
     * sizes.  So we'll perform extra checking here to make sure they
     * are of the right sizes.
     */
    Format checkForVideoSizes(Format original, Format supported)
    {
        try {
            console.logEntry();
            int width, height;
            Dimension size = ( (VideoFormat) original).getSize();
            Format jpegFmt = new Format(VideoFormat.JPEG_RTP);
            Format h263Fmt = new Format(VideoFormat.H263_RTP);
            if (supported.matches(jpegFmt)) {
                // For JPEG, make sure width and height are divisible by 8.
                width = (size.width % 8 == 0 ? size.width :
                         (int) (size.width / 8) * 8);
                height = (size.height % 8 == 0 ? size.height :
                          (int) (size.height / 8) * 8);
            }
            else if (supported.matches(h263Fmt)) {
                // For H.263, we only support some specific sizes.
                if (size.width < 128) {
                    width = 128;
                    height = 96;
                }
                else if (size.width < 176) {
                    width = 176;
                    height = 144;
                }
                else {
                    width = 352;
                    height = 288;
                }
            }
            else {
                // We don't know this particular format.  We'll just
                // leave it alone then.
                return supported;
            }
            return (new VideoFormat(null,
                                    new Dimension(width, height),
                                    Format.NOT_SPECIFIED,
                                    null,
                                    Format.NOT_SPECIFIED)).intersects(supported);
        }
        finally {
            console.logExit();
        }
    }

    protected String findIPAddressForFormat(String format)
    {
        try {
            console.logEntry();
            for (int i = 0; i < formatSets.size(); i++) {
                ArrayList currentSet = (ArrayList) formatSets.get(i);
                for (int j = 0; j < currentSet.size(); j++) {
                    if ( ( (String) currentSet.get(j)).equals(format)) {
                        return (String) ipAddresses.get(i);
                    }
                }
            }
            return null;
        }
        finally {
            console.logExit();
        }
    }

    protected int findPortForFormat(String format)
    {
        try {
            console.logEntry();
            for (int i = 0; i < formatSets.size(); i++) {
                ArrayList currentSet = (ArrayList) formatSets.get(i);
                for (int j = 0; j < currentSet.size(); j++) {
                    if ( ( (String) currentSet.get(j)).equals(format)) {
                        return ( (Integer) ports.get(i)).intValue();
                    }
                }
            }
            return -1;
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Setting the encoding quality to the specified value on the JPEG encoder.
     * 0.5 is a good default.
     */
    void setJPEGQuality(Player p, float val)
    {
        try {
            console.logEntry();
            Control cs[] = p.getControls();
            QualityControl qc = null;
            VideoFormat jpegFmt = new VideoFormat(VideoFormat.JPEG);
            // Loop through the controls to find the Quality control for
            // the JPEG encoder.
            for (int i = 0; i < cs.length; i++) {
                if (cs[i] instanceof QualityControl &&
                    cs[i] instanceof Owned) {
                    Object owner = ( (Owned) cs[i]).getOwner();
                    // Check to see if the owner is a Codec.
                    // Then check for the output format.
                    if (owner instanceof Codec) {
                        Format fmts[] = ( (Codec) owner).
                            getSupportedOutputFormats(null);
                        for (int j = 0; j < fmts.length; j++) {
                            if (fmts[j].matches(jpegFmt)) {
                                qc = (QualityControl) cs[i];
                                qc.setQuality(val);
                                if(console.isDebugEnabled())
                                console.debug("Setting quality to "
                                              + val + " on " + qc);
                                break;
                            }
                        }
                    }
                    if (qc != null) {
                        break;
                    }
                }
            }
        }
        finally {
            console.logExit();
        }
    }

    protected int findFirstMatchingFormat(Format[] hayStack, ArrayList needles)
    {
        try {
            console.logEntry();
            if (hayStack == null || needles == null) {
                return -1;
            }
            for (int j = 0; j < needles.size(); j++) {
                ArrayList currentSet = (ArrayList) needles.get(j);
                for (int k = 0; k < currentSet.size(); k++) {
                    for (int i = 0; i < hayStack.length; i++) {
                        if (hayStack[i].getEncoding().equals( (String)
                            currentSet.get(k))) {
                            return i;
                        }
                    }
                }
            }
            return -1;
        }
        finally {
            console.logExit();
        }
    }

    /****************************************************************
     * Convenience methods to handle processor's state changes.
     ****************************************************************/
    protected Integer stateLock = new Integer(0);
    protected boolean failed = false;
    Integer getStateLock()
    {
        return stateLock;
    }

    void setFailed()
    {
        failed = true;
    }

    protected synchronized boolean waitForState(Processor p, int state)
    {
        p.addControllerListener(new StateListener());
        failed = false;
        // Call the required method on the processor
        if (state == Processor.Configured) {
            p.configure();
        }
        else if (state == Processor.Realized) {
            p.realize();
        }
        // Wait until we get an event that confirms the
        // success of the method, or a failure event.
        // See StateListener inner class
        while (p.getState() < state && !failed) {
            synchronized (getStateLock()) {
                try {
                    getStateLock().wait();
                }
                catch (InterruptedException ie) {
                    return false;
                }
            }
        }
        if (failed) {
            return false;
        }
        else {
            return true;
        }
    }

    /****************************************************************
     * Inner Classes
     ****************************************************************/
    class StateListener
        implements ControllerListener
    {
        public void controllerUpdate(ControllerEvent ce)
        {
            try {
                console.logEntry();
                // If there was an error during configure or
                // realize, the processor will be closed
                if (ce instanceof ControllerClosedEvent) {
                    setFailed();
                    // All controller events, send a notification
                    // to the waiting thread in waitForState method.
                }
                if (ce instanceof ControllerEvent) {
                    synchronized (getStateLock()) {
                        getStateLock().notifyAll();
                    }
                }
                //Loop media files
                if (ce instanceof EndOfMediaEvent) {
                    processor.setMediaTime(new Time(0));
                    processor.start();
                }
            }
            finally {
                console.logExit();
            }
        }
    }
}

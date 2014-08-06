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

import java.io.*;
import java.net.*;
import java.util.*;
import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.sdp.*;
import net.java.sip.communicator.common.*;
import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.media.event.*;
import net.java.sip.communicator.media.event.MediaEvent;
import net.java.sip.communicator.common.NetworkAddressManager;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SessionAddress;
import java.io.InputStreamReader;

/**
 * <p>Title: SIP COMMUNICATOR</p>
 * <p>Description:JAIN-SIP Audio/Video phone application</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr) </p>
 * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
 * <p>Louis Pasteur University - Strasbourg - France</p>
 * <p>Division Chief: Thomas Noel </p>
 * @author Emil Ivov (http://www.emcho.com)
 * @author Paulo Pizzarro ( added support for media level connection parameter)
 * @version 1.1
 *
 */
public class MediaManager
    implements Serializable
{
    protected static Console console = Console.getConsole(MediaManager.class);
    protected ArrayList listeners = new ArrayList();
    protected Vector avTransmitters = new Vector();
    protected AVReceiver avReceiver;
    protected SdpFactory sdpFactory;
    protected ProcessorUtility procUtility = new ProcessorUtility();
    //media devices
    protected CaptureDeviceInfo audioDevice = null;
    protected CaptureDeviceInfo videoDevice = null;
    //Sdp Codes of all formats supported for
    //transmission by the selected datasource
    protected ArrayList transmittableVideoFormats = new ArrayList();
    protected ArrayList transmittableAudioFormats = new ArrayList();
    //Sdp Codes of all formats that we can receive
    //i.e.  all formats supported by JMF
    protected String[] receivableVideoFormats = new String[] {
        //sdp format 							   		// corresponding JMF Format
        Integer.toString(SdpConstants.H263), // javax.media.format.VideoFormat.H263_RTP
        Integer.toString(SdpConstants.JPEG), // javax.media.format.VideoFormat.JPEG_RTP
        Integer.toString(SdpConstants.H261) // javax.media.format.VideoFormat.H261_RTP
    };
    protected String[] receivableAudioFormats = new String[] {
        //sdp format 							   		// corresponding JMF Format
        Integer.toString(SdpConstants.G723), // javax.media.format.AudioFormat.G723_RTP
        Integer.toString(SdpConstants.GSM), // javax.media.format.AudioFormat.GSM_RTP;
        Integer.toString(SdpConstants.PCMU), // javax.media.format.AudioFormat.ULAW_RTP;
        Integer.toString(SdpConstants.DVI4_8000), // javax.media.format.AudioFormat.DVI_RTP;
        Integer.toString(SdpConstants.DVI4_16000), // javax.media.format.AudioFormat.DVI_RTP;
        Integer.toString(SdpConstants.PCMA), // javax.media.format.AudioFormat.ALAW;
        Integer.toString(SdpConstants.G728), // javax.media.format.AudioFormat.G728_RTP;
        //g729 is not suppported by JMF
        Integer.toString(SdpConstants.G729) // javax.media.format.AudioFormat.G729_RTP
    };

    /**
     * A list of currently active RTPManagers mapped against Local session addresses.
     * The list is used by transmitters and receivers so that receiving and transmitting
     * from the same port simultaneousl is possible
     */
    Hashtable activeRtpManagers = new Hashtable();

    protected String mediaSource = null;
    //only use these for initialisation and for
//    protected DataSource audioDataSource = null;
//    protected DataSource videoDataSource = null;
    protected DataSource avDataSource = null;
    protected Processor processor = null;
    protected boolean isStarted = false;
    public MediaManager()
    {
    }

    public void start() throws MediaException
    {
        try {
            console.logEntry();
            try {
                sdpFactory = SdpFactory.getInstance();
            }
            catch (SdpException exc) {
                console.error("Failed to create sdpFactory", exc);
                throw new MediaException("Failed to create sdpFactory", exc);
            }

            //init jmf capture devices
            setupJMF();

            mediaSource = Utils.getProperty(
                "net.java.sip.communicator.media.MEDIA_SOURCE");
            //Init Capture devices
            DataSource audioDataSource = null;
            DataSource videoDataSource = null;
            if (mediaSource == null) {
                console.debug(
                    "Scanning available capture devices.");
                //audio device
                Vector audioDevices = CaptureDeviceManager.getDeviceList(new
                    AudioFormat(AudioFormat.LINEAR, 44100, 16, 2));
                if (audioDevices.size() > 0) {
                    audioDevice = (CaptureDeviceInfo) audioDevices.get(0);
                    audioDataSource = createDataSource(audioDevice.getLocator());
                    console.debug("An Audio Device was found.");
                }
                else
                {
                    console.debug("No Audio Device was found.");
                }
                //video device
                Vector videoDevices = CaptureDeviceManager.getDeviceList(new
                    VideoFormat(VideoFormat.RGB));
                if (videoDevices.size() > 0) {
                    videoDevice = (CaptureDeviceInfo) videoDevices.get(0);
                    videoDataSource = createDataSource(videoDevice.getLocator());
                    console.debug("An RGB Video Device was found.");
                }
                // no RGB camera found. And what about YUV ?
                else
                {
                    videoDevices = CaptureDeviceManager.getDeviceList(new
                                    VideoFormat(VideoFormat.YUV));
            if (videoDevices.size() > 0) {
                       videoDevice = (CaptureDeviceInfo) videoDevices.get(0);
                       videoDataSource = createDataSource(videoDevice.getLocator());
                       console.debug("An YUV Video Device was found.");
                    }
                    else
                      console.debug("No Video Device was found.");

                }
                //Create the av data source
                if (audioDataSource != null && videoDataSource != null) {
                    DataSource[] allDS = new DataSource[] {
                        audioDataSource,
                        videoDataSource
                    };
                    try {
                        avDataSource = Manager.createMergingDataSource(allDS);
                    }
                    catch (IncompatibleSourceException exc) {
                        console.error(
                            "Failed to create a media data source!"
                            + "Media transmission won't be enabled!", exc);
                        //Shouldn't happen
                        throw new MediaException(
                            "Failed to create a media data source!"
                            + "Media transmission won't be enabled!", exc);
                    }
                }
                else {
                    if (audioDataSource != null) {
                        avDataSource = audioDataSource;
                    }
                    if (videoDataSource != null) {
                        avDataSource = videoDataSource;
                    }
                }
                //avDataSource may be null (Bug report Vince Fourcade)
                if (avDataSource != null) {
                    initProcessor(avDataSource);
                }
            }
            //A custom media source
            else {
                if (console.isDebugEnabled()) {
                    console.debug("Specified Media Source is: " + mediaSource);
                }
                MediaLocator locator = new MediaLocator(mediaSource);
                avDataSource = createDataSource(locator);
                if (avDataSource != null) {
                    initProcessor(avDataSource);
                }
            }
            isStarted = true;
        }
        finally {
            console.logExit();
        }
    }

    protected DataSource createDataSource(MediaLocator locator)
    {
        try {
            console.logEntry();
            try {
                if (console.isDebugEnabled())
                    console.debug("Creating datasource for:"
                                      + locator != null
                                      ? locator.toExternalForm()
                                      : "null");
                return Manager.createDataSource(locator);
            }
            catch (NoDataSourceException ex) {
                //The failure only concens us
                if (console.isDebugEnabled()) {
                    console.debug("Coud not create data source for " +
                                  locator.toExternalForm(), ex);
                }
                return null;
            }
            catch (IOException ex) {
                //The failure only concens us
                if (console.isDebugEnabled()) {
                    console.debug("Coud not create data source for " +
                                  locator.toExternalForm(), ex);
                }
                return null;
            }
        }
        finally {
            console.logExit();
        }
    }

    public void openMediaStreams(String sdpData) throws MediaException
    {
        try {
            console.logEntry();

            if(console.isDebugEnabled())
                console.debug("sdpData arg - " + sdpData);
            checkIfStarted();
            SessionDescription sessionDescription = null;
            if (sdpData == null) {
                console.error("The SDP data was null! Cannot open " +
                              "a stream withour an SDP Description!");
                throw new MediaException(
                    "The SDP data was null! Cannot open " +
                    "a stream withour an SDP Description!");
            }
            try {
                sessionDescription =
                    sdpFactory.createSessionDescription(sdpData);
            }
            catch (SdpParseException ex) {
                console.error("Incorrect SDP data!", ex);
                throw new MediaException("Incorrect SDP data!", ex);
            }
            Vector mediaDescriptions;
            try {
                mediaDescriptions = sessionDescription.
                    getMediaDescriptions(true);
            }
            catch (SdpException ex) {
                console.error(
                    "Failed to extract media descriptions from provided session description!",
                    ex);
                throw new MediaException(
                    "Failed to extract media descriptions from provided session description!",
                    ex);
            }
            Connection sessionConnection = sessionDescription.getConnection();
            String sessionRemoteAddress = null;
            if (sessionConnection != null) {
                try {
                    sessionRemoteAddress = sessionConnection.getAddress();
                }
                catch (SdpParseException ex) {
                    console.error(
                        "Failed to extract the connection address parameter"
                        + "from privided session description", ex);
                    throw new MediaException(
                        "Failed to extract the connection address parameter"
                        + "from privided session description", ex);
                }
            }
            int mediaPort = -1;
            boolean atLeastOneTransmitterStarted = false;
            ArrayList mediaTypes = new ArrayList();
            ArrayList remoteAddresses = new ArrayList();
            ArrayList ports = new ArrayList();
            ArrayList formatSets = new ArrayList();
            for (int i = 0; i < mediaDescriptions.size(); i++) {
                MediaDescription mediaDescription = (MediaDescription) mediaDescriptions.get(i);
                Media media = mediaDescription.getMedia();
                //Media Type
                String mediaType = null;
                try {
                    mediaType = media.getMediaType();
                }
                catch (SdpParseException ex) {
                    console.error(
                        "Failed to extract the media type for one of the provided media descriptions!\n"
                        + "Ignoring description!",
                        ex);
                    fireNonFatalMediaError(new MediaException(
                        "Failed to extract the media type for one of the provided media descriptions!\n"
                        + "Ignoring description!",
                        ex
                        ));
                    continue;
                }
                //Find ports
                try {
                    mediaPort = media.getMediaPort();
                }
                catch (SdpParseException ex) {
                    console.error("Failed to extract port for media type ["
                                  + mediaType + "]. Ignoring description!",
                                  ex);
                    fireNonFatalMediaError(new MediaException(
                        "Failed to extract port for media type ["
                        + mediaType + "]. Ignoring description!",
                        ex
                        ));
                    continue;
                }
                //Find  formats
                Vector sdpFormats = null;
                try {
                    sdpFormats = media.getMediaFormats(true);
                }
                catch (SdpParseException ex) {
                    console.error(
                        "Failed to extract media formats for media type ["
                        + mediaType + "]. Ignoring description!",
                        ex);
                    fireNonFatalMediaError(new MediaException(
                        "Failed to extract media formats for media type ["
                        + mediaType + "]. Ignoring description!",
                        ex
                        ));
                    continue;
                }

                Connection mediaConnection = mediaDescription.getConnection();
                String mediaRemoteAddress = null;
                if (mediaConnection == null) {
                    if(sessionConnection == null) {
                        console.error(
                            "A connection parameter was not present in provided session/media description");
                        throw new MediaException(
                            "A connection parameter was not present in provided session/media description");
                    } else {
                        mediaRemoteAddress = sessionRemoteAddress;
                    }
                } else {
                    try {
                        mediaRemoteAddress = mediaConnection.getAddress();
                    }
                    catch (SdpParseException ex) {
                        console.error(
                            "Failed to extract the connection address parameter"
                            + "from privided media description", ex);
                        throw new MediaException(
                            "Failed to extract the connection address parameter"
                            + "from privided media description", ex);
                    }
                }

                //START TRANSMISSION
                try {
                    if (isMediaTransmittable(mediaType)) {
                        mediaTypes.add(mediaType);
                        remoteAddresses.add(mediaRemoteAddress);
                        ports.add(new Integer(mediaPort));
                        formatSets.add(extractTransmittableJmfFormats(
                            sdpFormats));
                    }
                    else{
                        //nothing to transmit here so skip setting the flag
                        //bug report and fix - Gary M. Levin - Telecordia
                        continue;
                    }
                }
                catch (MediaException ex) {
                    console.error(
                        "Could not start a transmitter for media type ["
                        + mediaType + "]\nIgnoring media [" + mediaType + "]!",
                        ex
                        );
                    fireNonFatalMediaError(new MediaException(
                        "Could not start a transmitter for media type ["
                        + mediaType + "]\nIgnoring media [" + mediaType + "]!",
                        ex
                        ));
                    continue;
                }
                atLeastOneTransmitterStarted = true;
            }
            if (!atLeastOneTransmitterStarted) {
                console.error(
                    "Apparently all media descriptions failed to initialise!\n" +
                    "SIP COMMUNICATOR won't be able to open a media stream!");
                throw new MediaException(
                    "Apparently all media descriptions failed to initialise!\n" +
                    "SIP COMMUNICATOR won't be able to open a media stream!");
            }
            else {
                startReceiver(mediaTypes, remoteAddresses);
                startTransmitter(
                    remoteAddresses,
                    ports,
                    formatSets);
            }
        }
        finally {
            console.logExit();
        }
    }

    protected void closeProcessor() throws MediaException
    {
        try {
            console.logEntry();
            if(processor != null)
            {
                processor.stop();
                processor.close();
            }
            if(avDataSource!=null)
                avDataSource.disconnect();
        }
        finally {
            console.logExit();
        }
    }

    public void stop() throws MediaException
    {
        try {
            console.logEntry();
            closeStreams();
            closeProcessor();
        }
        finally {
            console.logExit();
        }
    }

    public void closeStreams()
    {
        try {
            removeAllRtpManagers();
            console.logEntry();
            stopTransmitters();
            stopReceiver();
            firePlayerStopped();
        }
        finally {
            console.logExit();
        }
    }

    protected void startTransmitter(ArrayList destHosts,
                                  ArrayList ports,
                                  ArrayList formatSets) throws MediaException
    {
        try {
            console.logEntry();
            if (avDataSource != null) {
                AVTransmitter transmitter =
                    new AVTransmitter(processor,
                                      destHosts,
                                      ports,
                                      formatSets
                                      );
                transmitter.setMediaManagerCallback(this);
                avTransmitters.add(transmitter);
                console.debug("Starting transmission.");
                transmitter.start();
            }
        }
        finally {
            console.logExit();
        }
    }

    protected void stopTransmitters()
    {
        try {
            console.logEntry();
            for (int i = avTransmitters.size() - 1; i >= 0; i--) {
                try {
                    ( (AVTransmitter) avTransmitters.elementAt(i)).stop();
                } //Catch everything that comes out as we wouldn't want
                //Some null pointer prevent us from closing a device and thus
                //render it unusable
                catch (Exception exc) {
                    console.error("Could not close transmitter " + i, exc);
                }
                avTransmitters.removeElementAt(i);
            }
        }
        finally {
            console.logExit();
        }
    }

    protected void startReceiver(ArrayList mediaTypes, ArrayList remoteAddresses)
    {
        try {
            console.logEntry();

            ArrayList sessions = new ArrayList();
            String mediaType = null;
            for(int i = 0; i< mediaTypes.size(); i++) {
                mediaType = (String)mediaTypes.get(i);
                if("audio".equals(mediaType)) {
                    sessions.add((String)remoteAddresses.get(i) + "/" + getAudioPort() + "/1");
                } else if("video".equals(mediaType)) {
                    sessions.add((String)remoteAddresses.get(i) + "/" + getVideoPort() + "/1");
                }
            }

            avReceiver = new AVReceiver(sessions);
            avReceiver.setMediaManager(this);
            avReceiver.initialize();
        }
        finally {
            console.logExit();
        }
    }

    protected void stopReceiver()
    {
        try {
            console.logEntry();
            if (avReceiver != null) {
                avReceiver.close();
                avReceiver = null;
            }
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Only stops the receiver without deleting it. After calling this method
     * one can call softStartReceiver to relauch reception.
     */
    public void softStopReceiver()
    {
        try {
            console.logEntry();
            if (avReceiver != null) {
                avReceiver.close();
                this.firePlayerStopped();
            }
            else
                console.debug(
                    "Attempt to soft stop reception for a null avReceiver");
        }
        finally
        {
            console.logExit();
        }
    }


    /**
     * Starts a receiver that has been stopped using softStopReceiver().
     */
    public void softStartReceiver()
    {
        try
        {
            console.logEntry();
            if (avReceiver != null) {
                avReceiver.initialize();
            }
            else
                console.error(
                    "acReceiver is null. Use softStartReceiver only for receivers "
                    +"that had been stopped using softStopReceiver()");
        }finally
        {
            console.logExit();
        }
    }

    void firePlayerStarting(Player player)
    {
        try {
            console.logEntry();
            MediaEvent evt = new MediaEvent(player);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (MediaListener) listeners.get(i)).playerStarting(evt);
            }
        }
        finally {
            console.logExit();
        }
    }

    void firePlayerStopped()
    {
        try {
            console.logEntry();
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (MediaListener) listeners.get(i)).playerStopped();
            }
        }
        finally {
            console.logExit();
        }
    }

    void fireNonFatalMediaError(Throwable cause)
    {
        try {
            console.logEntry();
            MediaErrorEvent evt = new MediaErrorEvent(cause);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (MediaListener) listeners.get(i)).nonFatalMediaErrorOccurred(
                    evt);
            }
        }
        finally {
            console.logExit();
        }
    }

    public void addMediaListener(MediaListener listener)
    {
        try {
            console.logEntry();
            listeners.add(listener);
        }
        finally {
            console.logExit();
        }
    }

    InetAddress getLocalHost() throws MediaException
    {
        try {
            console.logEntry();
            String hostAddress = Utils.getProperty(
                "net.java.sip.communicator.media.IP_ADDRESS");
            InetAddress localhost = null;
            if (hostAddress == null) {
                localhost = NetworkAddressManager.getLocalHost(false);
            }
            else {
                try {
                    localhost = InetAddress.getByName(hostAddress);
                }
                catch (UnknownHostException ex) {
                    throw new MediaException("Failed to create localhost!", ex);
                }
            }

            if (console.isDebugEnabled()) {
                console.debug("returning - " + localhost.getHostAddress());
            }
            return localhost;
        }
        finally {
            console.logExit();
        }
    }

    public String generateSdpDescription() throws MediaException
    {
        try {
            console.logEntry();
            checkIfStarted();
            try {
                SessionDescription sessDescr = sdpFactory.
                    createSessionDescription();
                //"v=0"
                Version v = sdpFactory.createVersion(0);
                InetSocketAddress publicVideoAddress = NetworkAddressManager.getPublicAddressFor(Integer.parseInt(getVideoPort()));
                InetSocketAddress publicAudioAddress = NetworkAddressManager.getPublicAddressFor(Integer.parseInt(getAudioPort()));
                InetAddress publicIpAddress = publicAudioAddress.getAddress();
                String addrType = publicIpAddress instanceof Inet6Address ?
                    "IP6" : "IP4";

                //spaces in the user name mess everything up.
                //bug report - Alessandro Melzi
                Origin o = sdpFactory.createOrigin(
                    Utils.getProperty("user.name").replace(' ', '_'), 0, 0, "IN",
                    addrType, publicIpAddress.getHostAddress());
                //"s=-"
                SessionName s = sdpFactory.createSessionName("-");
                //c=
                Connection c = sdpFactory.createConnection(
                    "IN",
                    addrType,
                    publicIpAddress.getHostAddress());
                //"t=0 0"
                TimeDescription t = sdpFactory.createTimeDescription();
                Vector timeDescs = new Vector();
                timeDescs.add(t);
                //--------Audio media description
                //make sure preferred formats come first
                surfacePreferredEncodings(getReceivableAudioFormats());
                String[] formats = getReceivableAudioFormats();
                MediaDescription am = sdpFactory.createMediaDescription(
                    "audio", publicAudioAddress.getPort(), 1, "RTP/AVP",
                    formats);
                if (!isAudioTransmissionSupported()) {
                    am.setAttribute("recvonly", null);
                    //--------Video media description
                }
                surfacePreferredEncodings(getReceivableVideoFormats());
                //"m=video 22222 RTP/AVP 34";
                String[] vformats = getReceivableVideoFormats();
                MediaDescription vm = sdpFactory.createMediaDescription(
                    "video", publicVideoAddress.getPort(), 1, "RTP/AVP",
                    vformats);
                if (!isVideoTransmissionSupported()) {
                    vm.setAttribute("recvonly", null);
                }
                Vector mediaDescs = new Vector();

                //Add Video and media descriptions if the user has not requested
                //otherwise (feature request by Pradeep Cheetal)
                if(    Utils.getProperty("net.java.sip.communicator.media.NO_AUDIO_DESCRIPTION_IN_SDP")== null
                   || !Utils.getProperty("net.java.sip.communicator.media.NO_AUDIO_DESCRIPTION_IN_SDP").equalsIgnoreCase("true"))
                    mediaDescs.add(am);
                if(    Utils.getProperty("net.java.sip.communicator.media.NO_VIDEO_DESCRIPTION_IN_SDP")== null
                   || !Utils.getProperty("net.java.sip.communicator.media.NO_VIDEO_DESCRIPTION_IN_SDP").equalsIgnoreCase("true"))
                       mediaDescs.add(vm);


                sessDescr.setVersion(v);
                sessDescr.setOrigin(o);
                sessDescr.setConnection(c);
                sessDescr.setSessionName(s);
                sessDescr.setTimeDescriptions(timeDescs);
                if(mediaDescs.size() > 0)
                    sessDescr.setMediaDescriptions(mediaDescs);
                if (console.isDebugEnabled()) {
                    console.debug("Generated SDP - " + sessDescr.toString());
                }
                return sessDescr.toString();
            }
            catch (SdpException exc) {
                console.error(
                    "An SDP exception occurred while generating local sdp description",
                    exc);
                throw new MediaException(
                    "An SDP exception occurred while generating local sdp description",
                    exc);
            }
        }
        finally {
            console.logExit();
        }
    }

    String getAudioPort()
    {
        try {
            console.logEntry();
            String audioPort = Utils.getProperty(
                "net.java.sip.communicator.media.AUDIO_PORT");
            return audioPort == null ? "22224" : audioPort;
        }
        finally {
            console.logExit();
        }
    }

    String getVideoPort()
    {
        try {
            console.logEntry();
            String videoPort = Utils.getProperty(
                "net.java.sip.communicator.media.VIDEO_PORT");
            return videoPort == null ? "22222" : videoPort;
        }
        finally {
            console.logExit();
        }
    }

    protected void finalize()
    {
        try {
            console.logEntry();
            try {
                if (avDataSource != null) {
                    avDataSource.disconnect();
                }
            }
            catch (Exception exc) {
                console.error("Failed to disconnect data source:" +
                              exc.getMessage());
            }
        }
        finally {
            console.logExit();
        }
    }

    public boolean isStarted()
    {
        return isStarted;
    }

    protected void checkIfStarted() throws MediaException
    {
        if (!isStarted()) {
            console.error("The MediaManager had not been properly started! "
                          + "Impossible to continue");
            throw new MediaException(
                "The MediaManager had not been properly started! "
                + "Impossible to continue");
        }
    }

    protected boolean isAudioTransmissionSupported()
    {
        return transmittableAudioFormats.size() > 0;
    }

    protected boolean isVideoTransmissionSupported()
    {
        return transmittableVideoFormats.size() > 0;
    }

    protected boolean isMediaTransmittable(String media)
    {
        if (media.equalsIgnoreCase("video")
            && isVideoTransmissionSupported()) {
            return true;
        }
        else if (media.equalsIgnoreCase("audio")
                 && isAudioTransmissionSupported()) {
            return true;
        }
        else {
            return false;
        }
    }

    protected String[] getReceivableAudioFormats()
    {
        return receivableAudioFormats;
    }

    protected String[] getReceivableVideoFormats()
    {
        return receivableVideoFormats;
    }

    protected String findCorrespondingJmfFormat(String sdpFormatStr)
    {
        int sdpFormat = -1;
        try {
            sdpFormat = Integer.parseInt(sdpFormatStr);
        }
        catch (NumberFormatException ex) {
            return null;
        }
        switch (sdpFormat) {
            case SdpConstants.PCMU:
                return AudioFormat.ULAW_RTP;
            case SdpConstants.GSM:
                return AudioFormat.GSM_RTP;
            case SdpConstants.G723:
                return AudioFormat.G723_RTP;
            case SdpConstants.DVI4_8000:
                return AudioFormat.DVI_RTP;
            case SdpConstants.DVI4_16000:
                return AudioFormat.DVI_RTP;
            case SdpConstants.PCMA:
                return AudioFormat.ALAW;
            case SdpConstants.G728:
                return AudioFormat.G728_RTP;
            case SdpConstants.G729:
                return AudioFormat.G729_RTP;
            case SdpConstants.H263:
                return VideoFormat.H263_RTP;
            case SdpConstants.JPEG:
                return VideoFormat.JPEG_RTP;
            case SdpConstants.H261:
                return VideoFormat.H261_RTP;
            default:
                return null;
        }
    }

    protected String findCorrespondingSdpFormat(String jmfFormat)
    {
        if (jmfFormat == null) {
            return null;
        }
        else if (jmfFormat.equals(AudioFormat.ULAW_RTP)) {
            return Integer.toString(SdpConstants.PCMU);
        }
        else if (jmfFormat.equals(AudioFormat.GSM_RTP)) {
            return Integer.toString(SdpConstants.GSM);
        }
        else if (jmfFormat.equals(AudioFormat.G723_RTP)) {
            return Integer.toString(SdpConstants.G723);
        }
        else if (jmfFormat.equals(AudioFormat.DVI_RTP)) {
            return Integer.toString(SdpConstants.DVI4_8000);
        }
        else if (jmfFormat.equals(AudioFormat.DVI_RTP)) {
            return Integer.toString(SdpConstants.DVI4_16000);
        }
        else if (jmfFormat.equals(AudioFormat.ALAW)) {
            return Integer.toString(SdpConstants.PCMA);
        }
        else if (jmfFormat.equals(AudioFormat.G728_RTP)) {
            return Integer.toString(SdpConstants.G728);
        }
        else if (jmfFormat.equals(AudioFormat.G729_RTP)) {
            return Integer.toString(SdpConstants.G729);
        }
        else if (jmfFormat.equals(VideoFormat.H263_RTP)) {
            return Integer.toString(SdpConstants.H263);
        }
        else if (jmfFormat.equals(VideoFormat.JPEG_RTP)) {
            return Integer.toString(SdpConstants.JPEG);
        }
        else if (jmfFormat.equals(VideoFormat.H261_RTP)) {
            return Integer.toString(SdpConstants.H261);
        }
        else {
            return null;
        }
    }

    /**
     * @param sdpFormats
     * @return
     * @throws MediaException
     */
    protected ArrayList extractTransmittableJmfFormats(Vector sdpFormats) throws
        MediaException
    {
        try {
            console.logEntry();
            ArrayList jmfFormats = new ArrayList();
            for (int i = 0; i < sdpFormats.size(); i++) {
                int sdpFormat = -1;
                String jmfFormat =
                    findCorrespondingJmfFormat(sdpFormats.elementAt(i).toString());
                if (jmfFormat != null) {
                    jmfFormats.add(jmfFormat);
                }
            }
            if (jmfFormats.size() == 0) {
                throw new MediaException(
                    "None of the supplied sdp formats for is supported by SIP COMMUNICATOR");
            }
            return jmfFormats;
        }
        finally {
            console.logExit();
        }
    }

    //This is the data source that we'll be using to transmit
    //let's see what can it do
    protected void initProcessor(DataSource dataSource) throws MediaException
    {
        try {
            console.logEntry();
            try {
                try {
                    dataSource.connect();
                }
                //Thrown when operation is not supported by the OS
                catch (NullPointerException ex) {
                    console.error(
                        "An internal error occurred while"
                        + " trying to connec to to datasource!", ex);
                    throw new MediaException(
                        "An internal error occurred while"
                        + " trying to connec to to datasource!", ex);
                }
                processor = Manager.createProcessor(dataSource);
                procUtility.waitForState(processor, Processor.Configured);
            }
            catch (NoProcessorException ex) {
                console.error(
                    "Media manager could not create a processor\n"
                    + "for the specified data source",
                    ex
                    );
                throw new MediaException(
                    "Media manager could not create a processor\n"
                    + "for the specified data source", ex);
            }
            catch (IOException ex) {
                console.error(
                    "Media manager could not connect "
                    + "to the specified data source",
                    ex);
                throw new MediaException("Media manager could not connect "
                                         + "to the specified data source", ex);
            }
            processor.setContentDescriptor(new ContentDescriptor(
                ContentDescriptor.RAW_RTP));
            TrackControl[] trackControls = processor.getTrackControls();
            console.debug("We will be able to transmit in:");
            for (int i = 0; i < trackControls.length; i++) {
                Format[] formats = trackControls[i].getSupportedFormats();
                for (int j = 0; j < formats.length; j++) {
                    Format format = formats[j];
                    String encoding = format.getEncoding();
                    if (format instanceof AudioFormat) {
                        String sdp = findCorrespondingSdpFormat(encoding);
                        if (sdp != null
                            && !transmittableAudioFormats.contains(sdp)) {
                            if (console.isDebugEnabled()) {
                                console.debug("Audio=[" + (j + 1) + "]=" +
                                              encoding + "; sdp=" + sdp);
                            }
                            transmittableAudioFormats.add(sdp);
                        }
                    }
                    if (format instanceof VideoFormat) {
                        String sdp = findCorrespondingSdpFormat(encoding);
                        if (sdp != null
                            && !transmittableVideoFormats.contains(sdp)) {
                            if (console.isDebugEnabled()) {
                                console.debug("Video=[" + (j + 1) + "]=" +
                                              encoding + "; sdp=" + sdp);
                            }
                            transmittableVideoFormats.add(sdp);
                        }
                    }
                }
            }
        }
        finally {
            console.logExit();
        }
    }

    /*
     protected static MediaManager mman;
        public static void main(String[] args)
            throws Throwable
        {
            mman = new MediaManager();
            System.setProperty("net.java.sip.communicator.media.MEDIA_SOURCE","file://home/emcho/lostinspace.mov");
         System.setProperty("net.java.sip.communicator.media.VIDEO_PORT","44444");
         System.setProperty("net.java.sip.communicator.media.AUDIO_PORT","44446");
            System.setProperty("net.java.sip.communicator.media.IP_ADDRESS","2001:660:220:102:230:5ff:fe1a:805f");
            mman.start();
            String sdp =
                      "v=0" + "\r\n" +
                      "o=Emcho 0 0 IN IP4 130.79.90.142" + "\r\n" +
                      "s=-" + "\r\n" +
                      "c=IN IP4 2001:660:220:102:230:5ff:fe2a:77c1" + "\r\n" +
                      "t=2208988800 2208988800" + "\r\n" +
                      "m=video 22222 RTP/AVP 20 26 31" + "\r\n" +
                      "m=audio 22224 RTP/AVP 3 0 4 5 6 8 15 18" + "\r\n"
                      ;
            mman.openMediaStreams(sdp);
            javax.swing.JFrame frame = new javax.swing.JFrame("Close & Exit");
            frame.addWindowListener(new java.awt.event.WindowAdapter(){
       public void windowClosing(java.awt.event.WindowEvent evt)
                {
                    try {
                        mman.stop();
                    }
                    catch (MediaException ex) {
                        ex.printStackTrace();
                    }
                 System.exit(0);
                }
            });
            frame.show();
        }
        protected void writeObject(ObjectOutputStream oos) throws IOException
        {
            oos.defaultWriteObject();
        }
        protected void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
        {
            ois.defaultReadObject();
        }
     */



    /**
     * Returns a cached instance of an RtpManager bound on the specified local
     * address. If no such instance exists null is returned.
     * @param localAddress the address where the rtp manager must be bound locally.
     * @return an rtp manager bound on the specified address or null if such an
     * instance was not found.
     */
     synchronized RTPManager getRtpManager(SessionAddress localAddress)
     {
         return (RTPManager)activeRtpManagers.get(localAddress);
     }

     /**
      * Maps the specified rtp manager against the specified local address so
      * that it may be later retrieved in case someone wants to operate
      * (transmit/receive) on the same port.
      * @param localAddress the address where the rtp manager is bound
      * @param rtpManager the rtp manager itself
      */
     synchronized void putRtpManager(SessionAddress localAddress, RTPManager rtpManager)
     {
         activeRtpManagers.put(localAddress, rtpManager);
     }

     /**
      * Removes all rtp managers from the rtp manager cache.
      */
     synchronized void removeAllRtpManagers()
     {
        Enumeration rtpManages = activeRtpManagers.keys();
        while (rtpManages.hasMoreElements()) {
            SessionAddress item = (SessionAddress)rtpManages.nextElement();
            activeRtpManagers.remove(item);
        }
     }



    /**
     * Moves formats with the specified encoding to the top of the array list
     * so that they are the ones chosen for transmission (if supported by the
     * remote party) (feature request by Vince Fourcade)
     */
    protected void surfacePreferredEncodings(String[] formats)
    {
        try {
            console.logEntry();
            String preferredAudioEncoding =
                Utils.getProperty(
                "net.java.sip.communicator.media.PREFERRED_AUDIO_ENCODING");
            String preferredVideoEncoding =
                Utils.getProperty(
                "net.java.sip.communicator.media.PREFERRED_VIDEO_ENCODING");
            if (preferredAudioEncoding == null
                && preferredVideoEncoding == null) {
                return;
            }
            for (int i = 0; i < formats.length; i++) {
                String encoding = formats[i];
                if ( (preferredAudioEncoding != null
                      && encoding.equalsIgnoreCase(preferredAudioEncoding))
                    || (preferredVideoEncoding != null
                        && encoding.equalsIgnoreCase(preferredVideoEncoding))) {
                    formats[i] = formats[0];
                    formats[0] = encoding;
                    if (console.isDebugEnabled()) {
                        console.debug("Encoding  [" +
                                      findCorrespondingJmfFormat(encoding) +
                                      "] is set as preferred.");
                    }
                    break;
                }
            }
        }
        finally {
            console.logExit();
        }
    }


    /**
     * Runs JMFInit the first time the application is started so that capture
     * devices are properly detected and initialized by JMF.
     * @throws MediaException if an exception occurs during the detection.
     */
    public static void setupJMF()
        throws MediaException
    {
        try
        {
            console.logExit();

            //.jmf is the place where we store the jmf.properties file used
            //by JMF. if the directory does not exist or it does not contain
            //a jmf.properties file. or if the jmf.properties file has 0 length
            //then this is the first time we're running and should continue to
            //with JMFInit
            String homeDir = System.getProperty("user.home");
            File jmfDir = new File(homeDir, ".jmf");
            String classpath = System.getProperty("java.class.path");
            classpath += System.getProperty("path.separator") +
                jmfDir.getAbsolutePath();
            System.setProperty("java.class.path", classpath);

            if (!jmfDir.exists())
                jmfDir.mkdir();

            File jmfProperties = new File(jmfDir, "jmf.properties");

            if (!jmfProperties.exists()) {
                try {
                    jmfProperties.createNewFile();
                }
                catch (IOException ex) {
                    throw new MediaException(
                        "Failed to create jmf.properties - " +
                        jmfProperties.getAbsolutePath());
                }
            }

            //if we're running on linux checkout that libjmutil.so is where it
            //should be and put it there.
            runLinuxPreInstall();

            if (jmfProperties.length() == 0)
                JMFInit.start();
        }
        finally
        {
            console.logExit();
        }

    }


    private static void runLinuxPreInstall()
    {
        try {
            console.logEntry();

            if(   Utils.getProperty("os.name") == null
               || !Utils.getProperty("os.name").equalsIgnoreCase("Linux"))
                 return;

            try {
                System.loadLibrary("jmv4l");
                console.debug("Successfully loaded libjmv4l.so");
            }
            catch (UnsatisfiedLinkError err) {
                console.debug("Failed to load libjmv4l.so. Will try and copy libjmutil.so", err);

                String destinationPathStr = Utils.getProperty("java.home")
                                              + File.separator + "lib"
                                              + File.separator + "i386";
                String libjmutilFileStr   = "libjmutil.so";

                try {
                    InputStream libIS =
                        MediaManager.class.getClassLoader().
                                          getResourceAsStream(libjmutilFileStr);
                     File outFile = new File(destinationPathStr
                                             +File.separator + libjmutilFileStr);

                     //Check if file is already there - Ben Asselstine
                     if (outFile.exists()) {
                         //if we're here then libjmutil is already where it should be
                         // but yet we failed to load libjmv4l.
                         //so notify log and bail out
                         console.error(
                             "An error occurred while trying to load JMF. This "
                             +"error is probably due to a JMF installation problem. "
                             +"Please copy libjmutil.so to a location contained by "
                             + "$LD_LIBRARY_PATH and try again!",
                             err);
                         return;

                     }

                     outFile.createNewFile();

                     console.debug("jmutil");

                    FileOutputStream fileOS = new FileOutputStream(outFile);
                    int available = libIS.available();
                    byte[] bytes = new byte[libIS.available()];
                    int read = 0;
                    int i = 0;
                    for (i = 0; i<available ; i++)
                    {
                        bytes[i] = (byte)libIS.read();
                    }

                    console.debug("Read " + i + " bytes out of " + available );

                    fileOS.write(bytes, 0, bytes.length);
                    console.debug("Wrote " + available + " bytes.");
                    bytes = null;
                    libIS.close();
                    fileOS.close();
                }
                catch (IOException exc) {
                    if(   exc.getMessage() != null
                       && exc.getMessage().toLowerCase().indexOf("permission denied") != -1)
                         console.showError("Permission denied!",
                                         "Because of insufficient permissions SIP Communicator has failed "
                                         + "to copy a required library to\n\n\t"
                                         + destinationPathStr + "!\n\nPlease run the application as root or "
                                         + "manually copy the " +libjmutilFileStr
                                         + " file to the above location!\n");
                    exc.printStackTrace();
                }
            }
                /** @todo check whether we have a permissions problem and alert the
             * user that they should be running as root */
            catch(Throwable t)
            {
                console.debug("Error while loading");
            }
        }
        finally {
            console.logExit();
        }
    }
}
/*
   v=0
   o=Emcho 0 0 IN IP4 130.79.90.142
   s=-
   c=IN IP4 130.79.90.142
   t=2208988800 2208988800
   m=video 22222 RTP/AVP 20 26 31
   m=audio 22224 RTP/AVP 0 3 4 5 6 8 15 18
 */
/*
    PCMU 		javax.media.format.AudioFormat.ULAW_RTP;
    1016
    G721
    GSM 		javax.media.format.AudioFormat.GSM_RTP;
    G723		javax.media.format.AudioFormat.G723_RTP
    DVI4_8000	javax.media.format.AudioFormat.DVI_RTP;
    DVI4_16000 	javax.media.format.AudioFormat.DVI_RTP;
    LPC
    PCMA		javax.media.format.AudioFormat.ALAW;
    G722		javax.media.format.AudioFormat.ALAW;
    L16_2CH
    L16_1CH
    QCELP
    CN
    MPA
    G728		javax.media.format.AudioFormat.G728_RTP;
    DVI4_11025
    DVI4_22050
    G729		javax.media.format.AudioFormat.G729_RTP
    CN_DEPRECATED
    H263		javax.media.format.VideoFormat.H263_RTP
    CelB
    JPEG		javax.media.format.VideoFormat.JPEG_RTP
    nv
    H261		javax.media.format.VideoFormat.H261_RTP
       MPV*/

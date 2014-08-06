/*
 * 
 * 	Raptis Dimos - Dimitrios (dimosrap@yahoo.gr) - 03109770
 *  Lazos Philippos (plazos@gmail.com) - 03109082
 * 	Omada 29
 * 
 */


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

import java.awt.Event;
import java.net.*;
import java.text.*;
import java.util.*;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.*;

import net.java.sip.communicator.common.*;
import net.java.sip.communicator.sip.event.*;
import net.java.sip.communicator.sip.security.*;
import net.java.sip.communicator.sip.simple.*;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;


//import net.java.sip.communicator.sip.simple.storage.*;
//import java.io.*;
import net.java.sip.communicator.sip.simple.event.*;
import net.java.sip.communicator.sip.simple.SubscriptionAuthority;

/**
 * The SipManager provides wrapping of the underlying stack's functionalities.
 * It also implements the SipListener interface and handles incoming
 * SIP messages.
 *
 * @author Emil Ivov
 * @version 1.0
 */
public class SipManager
    implements SipListener
{
    /**
     * Specifies the number of retries that should be attempted when deleting
     * a sipProvider
     */
	private int newHeader = 1;
    protected static final int  RETRY_OBJECT_DELETES       = 10;
    /**
     * Specifies the time to wait before retrying delete of a sipProvider.
     */
    protected static final long RETRY_OBJECT_DELETES_AFTER = 500;


    protected static final Console console = Console.getConsole(SipManager.class);
    protected static final String DEFAULT_TRANSPORT = "udp";
    //jain-sip objects - package accessibility as they should be
    //available for XxxProcessing classes
    /**
     * The SipFactory instance used to create the SipStack and the Address
     * Message and Header Factories.
     */
    public SipFactory sipFactory;

    /**
     * The AddressFactory used to create URLs ans Address objects.
     */
    public AddressFactory addressFactory;

    /**
     * The HeaderFactory used to create SIP message headers.
     */
    public HeaderFactory headerFactory;

    /**
     * The Message Factory used to create SIP messages.
     */
    public MessageFactory messageFactory;

    /**
     * The sipStack instance that handles SIP communications.
     */
    SipStack sipStack;

    /**
     * The default (and currently the only) SIP listening point of the
     * application.
     */
    ListeningPoint listeningPoint;

    /**
     * The JAIN SIP SipProvider instance.
     */
    public SipProvider sipProvider;

    /**
     * An instance used to provide user credentials
     */
    private SecurityAuthority securityAuthority = null;

    /**
     * The field is queried when user authorization is needed.
     */
    private SubscriptionAuthority subscriptionAuthority = null;


    /**
     * Used for the contact header to provide firewall support.
     */
    private InetSocketAddress publicIpAddress = null;

    //properties
    protected String sipStackPath = null;
    protected String currentlyUsedURI = null;
    protected String displayName = null;
    protected String transport = null;
    protected String registrarAddress = null;
    protected int localPort = -1;
    protected int registrarPort = -1;
    protected int registrationsExpiration = -1;
    protected String registrarTransport = null;

    //mandatory stack properties
    protected String stackAddress = null;
    protected String stackName = "sip-communicator";

    //Prebuilt Message headers
    protected FromHeader fromHeader = null;
    protected ContactHeader contactHeader = null;
    protected ArrayList viaHeaders = null;
    protected static final int  MAX_FORWARDS = 70;
    protected MaxForwardsHeader maxForwardsHeader = null;
    protected long registrationTransaction = -1;
    protected ArrayList listeners = new ArrayList();

    //XxxProcessing managers
    /**
     * The instance that handles all registration associated activity such as
     * registering, unregistering and keeping track of expirations.
     */
    RegisterProcessing registerProcessing = null;

    /**
     * The instance that handles all call associated activity such as
     * establishing, managing, and terminating calls.
     */
    CallProcessing callProcessing = null;

    /**
     * The instance that handles subscriptions.
     */
    public Watcher watcher = null;

    /**
     * The instance that informs others of our avaibility.
     */
    public PresenceAgent presenceAgent = null;

    /**
     * The instance that handles status management and notifications.
     */
    public PresenceStatusManager presenceStatusManager = null;

    /**
     * The instance that handles status management and notifications.
     */
    public ContactListController contactListController = null;

    /**
     * The instance that handles incoming/outgoing MESSAGE requests.
     */
    public MessageProcessing messageProcessing = null;

    /**
     * The instance that handles incoming/outgoing REFER requests.
     */
    TransferProcessing transferProcessing = null;

    /**
     * Authentication manager.
     */
    public SipSecurityManager sipSecurityManager = null;

    protected boolean isStarted = false;

    /**
     * Constructor. It only creates a SipManager instance without initializing
     * the stack itself.
     */
    public SipManager()
    {
    	newHeader = 1;
        registerProcessing    = new RegisterProcessing(this);
        callProcessing        = new CallProcessing(this);
        watcher               = new Watcher(this);
        presenceAgent         = new PresenceAgent(this);
        presenceStatusManager = new PresenceStatusManager(this);
        messageProcessing              = new MessageProcessing(this);

        presenceAgent.setLocalPUA(presenceStatusManager);

        sipSecurityManager    = new SipSecurityManager();
        contactListController = new ContactListController(this);

        //contactListController needs to know when we go on and offline so that
        //it could start SUBSCRIBE UNSUBSCRIBE threads.
        presenceStatusManager.addStatusListener(contactListController);

        //Set a WatcherEventsDispatcher reference to the Watcher
        watcher.setWatcherEventsDispatcher(contactListController);

    }

    /**
     * Creates and initializes JAIN SIP objects (factories, stack, listening
     * point and provider). Once this method is called the application is ready
     * to handle (incoming and outgoing) sip messages.
     *
     * @throws CommunicationsException if an axception should occur during the
     * initialization process
     */
    public void start() throws CommunicationsException
    {
        try {
            console.logEntry();
            initProperties();
            this.sipFactory = SipFactory.getInstance();
            sipFactory.setPathName(sipStackPath);
            try {
                addressFactory = sipFactory.createAddressFactory();
                headerFactory = sipFactory.createHeaderFactory();
                messageFactory = sipFactory.createMessageFactory();
            }
            catch (PeerUnavailableException ex) {
                console.error("Could not create factories!", ex);
                throw new CommunicationsException(
                    "Could not create factories!",
                    ex
                    );
            }

            try {
                sipStack = sipFactory.createSipStack(System.getProperties());
            }
            catch (PeerUnavailableException ex) {
                console.error("Could not create SipStack!", ex);
                throw new CommunicationsException(
                    "Could not create SipStack!\n"
                    +
                    "A possible reason is an incorrect OUTBOUND_PROXY property\n"
                    + "(Syntax:<proxy_address:port/transport>)",
                    ex
                    );
            }
            try {
                boolean successfullyBound = false;
                while (!successfullyBound) {
                    try {
                        //try and capture the firewall mapping for this address
                        //just befre it gets occuppied by the stack
                        publicIpAddress = NetworkAddressManager.
                                    getPublicAddressFor(localPort);

                        listeningPoint = sipStack.createListeningPoint(localPort,
                            transport);
                    }
                    catch (InvalidArgumentException ex) {
                        //choose another port between 1024 and 65000
                        console.error("error binding stack to port " + localPort +
                                      ". Will try another port", ex);

                        localPort = (int) ( (65000 - 1024) * Math.random()) +
                            1024;
                        continue;
                    }
                    successfullyBound = true;
                }
            }
            catch (TransportNotSupportedException ex) {
                console.error(
                    "Transport " + transport
                    +
                    " is not suppported by the stack!\n Try specifying another"
                    + " transport in SipCommunicator property files.\n",
                    ex);
                throw new CommunicationsException(
                    "Transport " + transport
                    +
                    " is not suppported by the stack!\n Try specifying another"
                    + " transport in SipCommunicator property files.\n",
                    ex);
            }
            try {
                sipProvider = sipStack.createSipProvider(listeningPoint);
            }
            catch (ObjectInUseException ex) {
                console.error("Could not create factories!\n", ex);
                throw new CommunicationsException(
                    "Could not create factories!\n", ex);
            }
            try {
                sipProvider.addSipListener(this);
            }
            catch (TooManyListenersException exc) {
                console.error(
                    "Could not register SipManager as a sip listener!", exc);
                throw new CommunicationsException(
                    "Could not register SipManager as a sip listener!", exc);
            }

            // we should have a security authority to be able to handle
            // authentication
            if(sipSecurityManager.getSecurityAuthority() == null)
            {
                throw new CommunicationsException(
                    "No SecurityAuthority was provided to SipManager!");
            }

            // we should have also have a SubsciptionAuthority to be able to handle
            // incoming Subscription requests.
            if(sipSecurityManager.getSecurityAuthority() == null)
            {
                throw new CommunicationsException(
                    "No SubscriptionAuthority was provided to SipManager!");
            }

            sipSecurityManager.setHeaderFactory(headerFactory);
            sipSecurityManager.setTransactionCreator(sipProvider);
            sipSecurityManager.setSipManCallback(this);


            //Make sure prebuilt headers are nulled so that they get reinited
            //if this is a restart
            contactHeader = null;
            fromHeader = null;
            viaHeaders = null;
            maxForwardsHeader = null;
            isStarted = true;
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Unregisters listening points, deletes sip providers, and generally
     * prepares the stack for a re-start(). This method is meant to be used
     * when properties are changed and should be reread by the stack.
     * @throws CommunicationsException
     */
    public void stop() throws CommunicationsException
    {
        try
        {
            console.logEntry();

            //Delete SipProvider
            int tries = 0;
            for (tries = 0; tries < RETRY_OBJECT_DELETES; tries++) {
                try {
                    sipStack.deleteSipProvider(sipProvider);
                }
                catch (ObjectInUseException ex) {
                    // System.err.println("Retrying delete of riSipProvider!");
                    sleep(RETRY_OBJECT_DELETES_AFTER);
                    continue;
                }
                break;
            }
            if (tries >= RETRY_OBJECT_DELETES)
                throw new CommunicationsException(
                    "Failed to delete the sipProvider!");

            //Delete RI ListeningPoint
            for (tries = 0; tries < RETRY_OBJECT_DELETES; tries++) {
                try {
                    sipStack.deleteListeningPoint(listeningPoint);
                }
                catch (ObjectInUseException ex) {
                    //System.err.println("Retrying delete of riListeningPoint!");
                    sleep(RETRY_OBJECT_DELETES_AFTER);
                    continue;
                }
                break;
            }
            if (tries >= RETRY_OBJECT_DELETES)
                throw new CommunicationsException(
                    "Failed to delete a listeningPoint!");

            sipProvider = null;
            listeningPoint = null;
            addressFactory = null;
            messageFactory = null;
            headerFactory = null;
            sipStack = null;

            viaHeaders = null;
            contactHeader = null;
            fromHeader = null;
        }finally
        {
            console.logExit();
        }
    }

    /**
     * Waits during _no_less_ than sleepFor milliseconds.
     * Had to implement it on top of Thread.sleep() to guarantee minimum
     * sleep time.
     *
     * @param sleepFor the number of miliseconds to wait
     */
    protected static void sleep(long sleepFor)
    {
        try
        {
            console.logEntry();

            long startTime = System.currentTimeMillis();
            long haveBeenSleeping = 0;
            while (haveBeenSleeping < sleepFor) {
                try {
                    Thread.sleep(sleepFor - haveBeenSleeping);
                }
                catch (InterruptedException ex) {
                    //we-ll have to wait again!
                }
                haveBeenSleeping = (System.currentTimeMillis() - startTime);
            }
        }finally
        {
            console.logExit();
        }

    }

    public void setCurrentlyUsedURI(String uri)
    {
        this.currentlyUsedURI = uri;
    }

    /**
     * Causes the RegisterProcessing object to send a registration request
     * to the registrar defined in
     * net.java.sip.communicator.sip.REGISTRAR_ADDRESS and to register with
     * the address defined in the net.java.sip.communicator.sip.PUBLIC_ADDRESS
     * property
     *
     * @throws CommunicationsException if an exception is thrown by the
     * underlying stack. The exception that caused this CommunicationsException
     * may be extracted with CommunicationsException.getCause()
     */
    public void register() throws CommunicationsException
    {
        register(currentlyUsedURI,null);
    }

    /**
     * Registers using the specified public address. If public add
     * @param publicAddress
     * @throws CommunicationsException
     */
    public void register(String publicAddress, char [] password) throws CommunicationsException
    {
    	String username = publicAddress;
        try {
            console.logEntry();



            if(publicAddress == null || publicAddress.trim().length() == 0)
                return; //maybe throw an exception?


            //Handle default domain name (i.e. transform 1234 -> 1234@sip.com
            String defaultDomainName =
                Utils.getProperty("net.java.sip.communicator.sip.DEFAULT_DOMAIN_NAME");

            //feature request, Michael Robertson (sipphone.com)
            //strip the following chars of their user names: ( - ) <space>
            if(publicAddress.toLowerCase().indexOf("sipphone.com") != -1
               || defaultDomainName.indexOf("sipphone.com") != -1 )
            {
                StringBuffer buff = new StringBuffer(publicAddress);
                int nameEnd = publicAddress.indexOf('@');
                nameEnd = nameEnd==-1?Integer.MAX_VALUE:nameEnd;
                nameEnd = Math.min(nameEnd, buff.length())-1;

                int nameStart = publicAddress.indexOf("sip:");
                nameStart = nameStart == -1 ? 0 : nameStart + "sip:".length();

                for(int i = nameEnd; i >= nameStart; i--)
                    if(!Character.isLetter( buff.charAt(i) )
                       && !Character.isDigit( buff.charAt(i)))
                        buff.deleteCharAt(i);
                publicAddress = buff.toString();
            }


            // if user didn't provide a domain name in the URL and someone
            // has defined the DEFAULT_DOMAIN_NAME property - let's fill in the blank.
            if (defaultDomainName != null
                && publicAddress.indexOf('@') == -1 //most probably a sip uri
                ) {
                publicAddress = publicAddress + "@" + defaultDomainName;
            }

            if (!publicAddress.trim().toLowerCase().startsWith("sip:")) {
                publicAddress = "sip:" + publicAddress;
            }

            this.currentlyUsedURI = publicAddress;
            System.out.println("publicAddress =" + publicAddress);
            registerProcessing.register( registrarAddress,
            							 registrarPort,
            							 registrarTransport,
            							 registrationsExpiration, username, password);

             //at this point we are sure we have a sip: prefix in the uri
            // we construct our pres: uri by replacing that prefix.
            String presenceUri = "pres"
                + publicAddress.substring(publicAddress.indexOf(':'));

            presenceStatusManager.setPresenceEntityUriString(presenceUri);
            presenceStatusManager.addContactUri(publicAddress, PresenceStatusManager.DEFAULT_CONTACT_PRIORITY);
        }
        finally {
            console.logExit();
        }
    }

    public void startRegisterProcess() throws CommunicationsException
    {
        try {
            console.logEntry();
            checkIfStarted();
            //Obtain initial credentials

            UserCredentials defaultCredentials = new UserCredentials();

            //avoid nullpointer exceptions
            String uName = Utils.getProperty(
                "net.java.sip.communicator.sip.USER_NAME");
            defaultCredentials.setUserName(uName == null? "" : uName);
            defaultCredentials.setPassword(new char[0]);

            String realm = Utils.getProperty(
                "net.java.sip.communicator.sip.DEFAULT_AUTHENTICATION_REALM");
            realm = realm == null ? "" : realm;

            UserCredentials initialCredentials = securityAuthority.obtainCredentials(realm,
                defaultCredentials);
            //put the returned user name in the properties file
            //so that it appears as a default one next time user is prompted for pass
            PropertiesDepot.setProperty("net.java.sip.communicator.sip.USER_NAME",
                                        initialCredentials.getUserName()) ;
            PropertiesDepot.storeProperties();

            register(initialCredentials.getUserName(), initialCredentials.getPassword() );

            //at this point a simple register request has been sent and the global
            //from  header in SipManager has been set to a valid value by the RegisterProcesing
            //class. Use it to extract the valid user name that needs to be cached by
            //the security manager together with the user provided password.
            initialCredentials.setUserName(((SipURI)getFromHeader().getAddress().getURI()).getUser());

            cacheCredentials(realm, initialCredentials);
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Causes the PresenceAgent object to notify all subscribers of our brand new
     * offline status and the RegisterProcessing object to send a registration
     *  request with a 0 "expires" interval to the registrar defined in
     * net.java.sip.communicator.sip.REGISTRAR_ADDRESS.
     *
     * @throws CommunicationsException if an exception is thrown by the
     * underlying stack. The exception that caused this CommunicationsException
     * may be extracted with CommunicationsException.getCause()
     */
    public void unregister() throws CommunicationsException
    {
        try {
            console.logEntry();
            if (!isRegistered()) {
                return;
            }
            checkIfStarted();
            presenceAgent.removeAllSubscriptions(SubscriptionStateHeader.NO_RESOURCE);
            watcher.removeAllSubscriptions();
            registerProcessing.unregister();
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Queries the RegisterProcessing object whether the application is registered
     * with a registrar.
     * @return true if the application is registered with a registrar.
     */
    public boolean isRegistered()
    {
        return (registerProcessing != null && registerProcessing.isRegistered());
    }

    /**
     * Determines whether the SipManager was start()ed.
     * @return true if the SipManager was start()ed.
     */
    public boolean isStarted()
    {
        return isStarted;
    }

//============================ COMMUNICATION FUNCTIONALITIES =========================
    /**
     * Causes the CallProcessing object to send  an INVITE request to the
     * URI specified by <code>callee</code>
     * setting sdpContent as message body. The method generates a Call object
     * that will represent the resulting call and will be used for later
     * references to the same call.
     *
     * @param callee the URI to send the INVITE to.
     * @param sdpContent the sdp session offer.
     * @return the Call object that will represent the call resulting
     *                  from invoking this method.
     * @throws CommunicationsException if an exception occurs while sending and
     * parsing.
     */
    public Call establishCall(String callee, String sdpContent) throws
        CommunicationsException
    {
        try {
            console.logEntry();
            checkIfStarted();
            return callProcessing.invite(callee, sdpContent);
        }
        finally {
            console.logExit();
        }
    } //CALL

    //------------------ hang up on
    /**
     * Causes the CallProcessing object to send a terminating request (CANCEL,
     * BUSY_HERE or BYE) and thus terminate that call with id <code>callID</code>.
     * @param callID the id of the call to terminate.
     * @throws CommunicationsException if an exception occurs while invoking this
     * method.
     */
    public void endCall(int callID) throws CommunicationsException
    {
        try {
            console.logEntry();
            checkIfStarted();
            callProcessing.endCall(callID);
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Calls endCall for all currently active calls.
     * @throws CommunicationsException if an exception occurs while
     */
    public void endAllCalls() throws CommunicationsException
    {
        try {
            console.logEntry();
            if (callProcessing == null) {
                return;
            }
            Object[] keys = callProcessing.getCallDispatcher().getAllCalls();
            for (int i = 0; i < keys.length; i++) {
                endCall( ( (Integer) keys[i]).intValue());
            }
        }
        finally {
            console.logExit();
        }
    }


    /**
     * Causes CallProcessing to send a 200 OK response, with the specified
     * sdp description, to the specified call's remote party.
     * @param callID the id of the call that is to be answered.
     * @param sdpContent this party's media description (as defined by SDP).
     * @throws CommunicationsException if an axeption occurs while invoking this
     * method.
     */
    public void answerCall(int callID, String sdpContent) throws
        CommunicationsException
    {
        try {
            console.logEntry();
            checkIfStarted();
            callProcessing.sayOK(callID, sdpContent);
        }
        finally {
            console.logExit();
        }
    } //answer to

    /**
     * Sends a NOT_IMPLEMENTED response through the specified transaction.
     * @param serverTransaction the transaction to send the response through.
     * @param request the request that is being answered.
     */
    void sendNotImplemented(ServerTransaction serverTransaction,
                            Request request)
    {
        try {
            console.logEntry();
            Response notImplemented = null;
            try {
                notImplemented =
                    messageFactory.createResponse(Response.NOT_IMPLEMENTED,
                                                  request);
                attachToTag(notImplemented, serverTransaction.getDialog());
            }
            catch (ParseException ex) {
                fireCommunicationsError(
                    new CommunicationsException(
                    "Failed to create a NOT_IMPLEMENTED response to a "
                    + request.getMethod()
                    + " request!",
                    ex)
                    );
                return;
            }
            try {
                serverTransaction.sendResponse(notImplemented);
            }
            catch (SipException ex) {
                fireCommunicationsError(
                    new CommunicationsException(
                    "Failed to create a NOT_IMPLEMENTED response to a "
                    + request.getMethod()
                    + " request!",
                    ex)
                    );
            }
        }
        finally {
            console.logExit();
        }
    }

//============================= Utility Methods ==================================
    /**
     * Initialises SipManager's fromHeader field in accordance with
     * net.java.sip.communicator.sip.PUBLIC_ADDRESS
     * net.java.sip.communicator.sip.DISPLAY_NAME
     * net.java.sip.communicator.sip.TRANSPORT
     * net.java.sip.communicator.sip.PREFERRED_LOCAL_PORT and returns a
     * reference to it.
     * @return a reference to SipManager's fromHeader field.
     * @throws CommunicationsException if a ParseException occurs while
     * initially composing the FromHeader.
     */
    
    public void resetHeader()
    {
    	newHeader = 1;
    }
    
    public FromHeader getFromHeader() throws CommunicationsException
    {
        try {
            console.logEntry();
            if (newHeader == 0 && fromHeader != null) {
                return fromHeader;
            }
            try {
            	newHeader = 0;
                SipURI fromURI = (SipURI) addressFactory.createURI(
                    currentlyUsedURI);
                //Unnecessary test (report by Willem Romijn)
                //if (console.isDebugEnabled())
                fromURI.setTransportParam(listeningPoint.getTransport());
                // ECE355  - Commented out line fromURI.setPort(listeningPoint.getPort());
                // This chnage is needed so that we can run  two Sip Communicator instances in the same machine
                // Eachb instance uses a diferent SIP port.
                // The example below shows how to set the SIP port.
                // In sip-communicator.xml
                // <PREFERRED_LOCAL_PORT value="5061"/>
                //
                //fromURI.setPort(listeningPoint.getPort());

                
                Address fromAddress = addressFactory.createAddress(fromURI);
                if (displayName != null && displayName.trim().length() > 0) {
                    fromAddress.setDisplayName(displayName);
                }
                fromHeader = headerFactory.createFromHeader(fromAddress,
                    Integer.toString(hashCode()));
                console.debug("Generated from header: " + fromHeader);
            }
            catch (ParseException ex) {
                console.error(
                    "A ParseException occurred while creating From Header!", ex);
                throw new CommunicationsException(
                    "A ParseException occurred while creating From Header!", ex);
            }
            return fromHeader;
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Same as calling getContactHeader(true)
     *
     * @return the result of getContactHeader(true)
     * @throws CommunicationsException if an exception is thrown while calling
     * getContactHeader(false)
     */
    public ContactHeader getContactHeader() throws CommunicationsException
    {
        return getContactHeader(true);
    }

    /**
     * Same as calling getContactHeader(true).
     * @return the result of calling getContactHeader(true).
     * @throws CommunicationsException if an exception occurs while executing
     * getContactHeader(true).
     */
    ContactHeader getRegistrationContactHeader() throws CommunicationsException
    {
        return getContactHeader(true);
    }

    /**
     * Initialises SipManager's contactHeader field in accordance with
     * javax.sip.IP_ADDRESS
     * net.java.sip.communicator.sip.DISPLAY_NAME
     * net.java.sip.communicator.sip.TRANSPORT
     * net.java.sip.communicator.sip.PREFERRED_LOCAL_PORT and returns a
     * reference to it.
     * @param useLocalHostAddress specifies whether the SipURI in the contact
     * header should contain the value of javax.sip.IP_ADDRESS (true) or that of
     * net.java.sip.communicator.sip.PUBLIC_ADDRESS (false).
     * @return a reference to SipManager's contactHeader field.
     * @throws CommunicationsException if a ParseException occurs while
     * initially composing the FromHeader.
     */
    public ContactHeader getContactHeader(boolean useLocalHostAddress) throws
        CommunicationsException
    {
        try {
            console.logEntry();
            if (contactHeader != null) {
                return contactHeader;
            }
            try {

                SipURI contactURI;
                if (useLocalHostAddress) {

                    contactURI = (SipURI) addressFactory.createSipURI(null,
                        publicIpAddress.getAddress().getHostAddress());
                }
                else {
                    contactURI = (SipURI) addressFactory.createURI(
                        currentlyUsedURI);
                }
                contactURI.setTransportParam(listeningPoint.getTransport());
                contactURI.setPort(publicIpAddress.getPort());
                Address contactAddress = addressFactory.createAddress(
                    contactURI);
                if (displayName != null && displayName.trim().length() > 0) {
                    contactAddress.setDisplayName(displayName);
                }
                contactHeader = headerFactory.createContactHeader(
                    contactAddress);
                if (console.isDebugEnabled()) {
                    console.debug("generated contactHeader:" + contactHeader);
                }
            }
            catch (ParseException ex) {
                console.error(
                    "A ParseException occurred while creating From Header!", ex);
                throw new CommunicationsException(
                    "A ParseException occurred while creating From Header!", ex);
            }
            return contactHeader;
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Initializes (if null) and returns an ArrayList with a single ViaHeader
     * containing localhost's address. This ArrayList may be used when sending
     * requests.
     * @return ViaHeader-s list to be used when sending requests.
     * @throws CommunicationsException if a ParseException is to occur while
     * initializing the array list.
     */
    public ArrayList getLocalViaHeaders() throws CommunicationsException
    {
        try {
            console.logEntry();
            if (viaHeaders != null) {
                return viaHeaders;
            }
            ListeningPoint lp = sipProvider.getListeningPoint();
            viaHeaders = new ArrayList();
            try {
                ViaHeader viaHeader = headerFactory.createViaHeader(
                    sipStack.getIPAddress(),
                    lp.getPort(),
                    lp.getTransport(),
                    null
                    );
                viaHeaders.add(viaHeader);
                if (console.isDebugEnabled()) {
                    console.debug("generated via headers:" + viaHeader);
                }
                return viaHeaders;
            }
            catch (ParseException ex) {
                console.error(
                    "A ParseException occurred while creating Via Headers!");
                throw new CommunicationsException(
                    "A ParseException occurred while creating Via Headers!");
            }
            catch (InvalidArgumentException ex) {
                console.error(
                    "Unable to create a via header for port " + lp.getPort(),
                    ex);
                throw new CommunicationsException(
                    "Unable to create a via header for port " + lp.getPort(),
                    ex);
            }
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Initializes and returns SipManager's maxForwardsHeader field using the
     * value specified by MAX_FORWARDS.
     * @return an instance of a MaxForwardsHeader that can be used when
     * sending requests
     * @throws CommunicationsException if MAX_FORWARDS has an invalid value.
     */
    public MaxForwardsHeader getMaxForwardsHeader() throws CommunicationsException
    {
        try {
            console.logEntry();
            if (maxForwardsHeader != null) {
                return maxForwardsHeader;
            }
            try {
                maxForwardsHeader = headerFactory.createMaxForwardsHeader(MAX_FORWARDS);
                if (console.isDebugEnabled()) {
                    console.debug("generate max forwards: "
                                  + maxForwardsHeader.toString());
                }
                return maxForwardsHeader;
            }
            catch (InvalidArgumentException ex) {
                throw new CommunicationsException(
                    "A problem occurred while creating MaxForwardsHeader", ex);
            }
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Returns the user used to create the From Header URI.
     * @return the user used to create the From Header URI.
     */
    public String getLocalUser()
    {
        try {
            console.logEntry();
            return ( (SipURI) getFromHeader().getAddress().getURI()).getUser();
        }
        catch (CommunicationsException ex) {
            return "";
        }
        finally {
            console.logExit();
        }
    }

    /**
     * Generates a ToTag (the containingDialog's hashCode())and attaches it to
     * response's ToHeader.
     * @param response the response that is to get the ToTag.
     * @param containingDialog the Dialog instance that is to extract a unique
     * Tag value (containingDialog.hashCode())
     */
    public void attachToTag(Response response, Dialog containingDialog)
    {
        try {
            console.logEntry();
            ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
            if (to == null) {
                fireCommunicationsError(
                    new CommunicationsException(
                    "No TO header found in, attaching a to tag is therefore impossible"));
            }
            try {
                if (to.getTag() == null || to.getTag().trim().length() == 0) {

                    //the containing dialog may be null (e.g. when called by
                    //sendNotImplemented). Attach sth else in that case.
                    //Bug Report - Joe Provino - SUN Microsystems
                    int toTag = containingDialog != null? containingDialog.hashCode():(int)System.currentTimeMillis();

                    if (console.isDebugEnabled()) {
                        console.debug("generated to tag: " +
                                      toTag);
                    }
                    to.setTag(Integer.toString(toTag));
                }
            }
            catch (ParseException ex) {
                fireCommunicationsError(
                    new CommunicationsException(
                    "Failed to attach a TO tag to an outgoing response"));
            }
        }
        finally {
            console.logExit();
        }
    }

//================================ PROPERTIES ================================
    protected void initProperties()
    {
        try {
            console.logEntry();
            // ------------------ stack properties --------------

            //network address management is handled by common.NetworkAddressManager
            //stackAddress = Utils.getProperty("javax.sip.IP_ADDRESS");
            //if (stackAddress == null) {

            stackAddress = getLocalHostAddress();
            //Add the host address to the properties that will pass the stack
            Utils.setProperty("javax.sip.IP_ADDRESS", stackAddress);

            //ensure IPv6 address compliance
            if (stackAddress.indexOf(':') != stackAddress.lastIndexOf(':')
                && stackAddress.charAt(0) != '['
                ) {
                stackAddress = '[' + stackAddress.trim() + ']';
            }
            if (console.isDebugEnabled()) {
                console.debug("stack address=" + stackAddress);
            }
            stackName = Utils.getProperty("javax.sip.STACK_NAME");
            if (stackName == null) {
                stackName = "SipCommunicator@" + Integer.toString(hashCode());
                //Add the stack name to the properties that will pass the stack
                Utils.setProperty("javax.sip.STACK_NAME", stackName);
            }
            if (console.isDebugEnabled()) {
                console.debug("stack name is:" + stackName);
            }

            String retransmissionFilter = Utils.getProperty("javax.sip.RETRANSMISSION_FILTER");
            if (retransmissionFilter == null) {
                retransmissionFilter = "true";
                //Add the retransmission filter param to the properties that will pass the stack
                Utils.setProperty("javax.sip.RETRANSMISSION_FILTER", retransmissionFilter);
            }
            if (console.isDebugEnabled()) {
                console.debug("retransmission filter is:" + stackName);
            }
            //------------ application properties --------------
            currentlyUsedURI = Utils.getProperty(
                "net.java.sip.communicator.sip.PUBLIC_ADDRESS");
            if (currentlyUsedURI == null) {
                currentlyUsedURI = Utils.getProperty("user.name") + "@" +
                    stackAddress;
            }
            if (!currentlyUsedURI.trim().toLowerCase().startsWith("sip:")) {
                currentlyUsedURI = "sip:" + currentlyUsedURI.trim();
            }

            //at this point we are sure we have a sip: prefix in the uri
            // we construct our pres: uri by replacing that prefix.
            String presenceUri = "pres"
                + currentlyUsedURI.substring(currentlyUsedURI.indexOf(':'));

            presenceStatusManager.setPresenceEntityUriString(presenceUri);
            presenceStatusManager.addContactUri(currentlyUsedURI, PresenceStatusManager.DEFAULT_CONTACT_PRIORITY);


            if (console.isDebugEnabled()) {
                console.debug("public address=" + currentlyUsedURI);
            }
            registrarAddress = Utils.getProperty(
                "net.java.sip.communicator.sip.REGISTRAR_ADDRESS");
            if (console.isDebugEnabled()) {
                console.debug("registrar address=" + registrarAddress);
            }
            try {
                registrarPort = Integer.parseInt(Utils.getProperty(
                    "net.java.sip.communicator.sip.REGISTRAR_PORT"));
            }
            catch (NumberFormatException ex) {
                registrarPort = 5060;
            }
            if (console.isDebugEnabled()) {
                console.debug("registrar port=" + registrarPort);
            }
            registrarTransport = Utils.getProperty(
                "net.java.sip.communicator.sip.REGISTRAR_TRANSPORT");
            if (registrarTransport == null) {
                registrarTransport = DEFAULT_TRANSPORT;
            }
            try {
                registrationsExpiration = Integer.parseInt(Utils.getProperty(
                    "net.java.sip.communicator.sip.REGISTRATIONS_EXPIRATION"));
            }
            catch (NumberFormatException ex) {
                registrationsExpiration = 3600;
            }
            if (console.isDebugEnabled()) {
                console.debug("registrar transport=" + registrarTransport);
                // Added by mranga
            }
            String serverLog = Utils.getProperty
                ("gov.nist.javax.sip.SERVER_LOG");
            if (serverLog != null) {
                Utils.setProperty
                    ("gov.nist.javax.sip.TRACE_LEVEL", "16");
            }
            if (console.isDebugEnabled()) {
                console.debug("server log=" + serverLog);
            }
            sipStackPath = Utils.getProperty(
                "net.java.sip.communicator.sip.STACK_PATH");
            if (sipStackPath == null) {
                sipStackPath = "gov.nist";
            }
            if (console.isDebugEnabled()) {
                console.debug("stack path=" + sipStackPath);
            }
            String routerPath = Utils.getProperty("javax.sip.ROUTER_PATH");
            if (routerPath == null) {
                Utils.setProperty("javax.sip.ROUTER_PATH",
                                  "net.java.sip.communicator.sip.SipCommRouter");
            }
            if (console.isDebugEnabled()) {
                console.debug("router path=" + routerPath);
            }
            transport =
                Utils.getProperty("net.java.sip.communicator.sip.TRANSPORT");
            if (transport == null) {
                transport = DEFAULT_TRANSPORT;
            }
            if (console.isDebugEnabled()) {
                console.debug("transport=" + transport);
            }
            String localPortStr = Utils.getProperty(
                "net.java.sip.communicator.sip.PREFERRED_LOCAL_PORT");
            try {
                localPort = Integer.parseInt(localPortStr);
            }
            catch (NumberFormatException exc) {
                localPort = 5060;
            }
            if (console.isDebugEnabled()) {
                console.debug("preferred local port=" + localPort);
            }
            displayName = Utils.getProperty(
                "net.java.sip.communicator.sip.DISPLAY_NAME");
            if (console.isDebugEnabled()) {
                console.debug("display name=" + displayName);
            }
        }
        finally {
            console.logExit();
        }
    }

//============================     SECURITY     ================================
    /**
     * Sets the SecurityAuthority instance that should be consulted later on for
     * user credentials.
     *
     * @param authority the SecurityAuthority instance that should be consulted
     * later on for user credentials.
     */
    public void setSecurityAuthority(SecurityAuthority authority)
    {
        //keep a copty
        this.securityAuthority = authority;
        sipSecurityManager.setSecurityAuthority(authority);
    }

    /**
     * Adds the specified credentials to the security manager's credentials cache
     * so that they get tried next time they're needed.
     *
     * @param realm the realm these credentials should apply for.
     * @param credentials a set of credentials (username and pass)
     */
    public void cacheCredentials(String realm, UserCredentials credentials )
    {
        sipSecurityManager.cacheCredentials(realm, credentials);
    }
//============================ EVENT DISPATHING ================================
    /**
     * Adds a CommunicationsListener to SipManager.
     * @param listener The CommunicationsListener to be added.
     */
    public void addCommunicationsListener(CommunicationsListener listener)
    {
        try {
            console.logEntry();
            listeners.add(listener);
        }
        finally {
            console.logExit();
        }
    }

    //------------ call received dispatch
    void fireCallReceived(Call call)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("received call" + call);
            }
            CallEvent evt = new CallEvent(call);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).callReceived(evt);
            }
        }
        finally {
            console.logExit();
        }
    } //call received

    //------------ call received dispatch
    void fireMessageReceived(Request message)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("received instant message=" + message);
            }
            MessageEvent evt = new MessageEvent(message);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).messageReceived(
                    evt);
            }
        }
        finally {
            console.logExit();
        }
    } //call received

    //------------ registerred
    void fireRegistered(String address)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("registered with address = " + address);
            }
            RegistrationEvent evt = new RegistrationEvent(address);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).registered(evt);
            }
        }
        finally {
            console.logExit();
        }
    } //call received

    //------------ registering
    void fireRegistering(String address)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("registering with address=" + address);
            }
            RegistrationEvent evt = new RegistrationEvent(address);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).registering(evt);
            }
        }
        finally {
            console.logExit();
        }
    } //call received

    //------------ unregistered
    public void fireUnregistered(String address)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("unregistered, address is " + address);
            }
            RegistrationEvent evt = new RegistrationEvent(address);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).unregistered(evt);
            }
        }
        finally {
            console.logExit();
        }
    } //call received

    void fireUnregistering(String address)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("unregistering, address is " + address);
            }
            RegistrationEvent evt = new RegistrationEvent(address);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).unregistering(evt);
            }
        }
        finally {
            console.logExit();
        }
    } //call received


    //---------------- received unknown message
    void fireUnknownMessageReceived(Message message)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("unknown message=" + message);
            }
            UnknownMessageEvent evt = new UnknownMessageEvent(message);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).
                    receivedUnknownMessage(
                    evt);
            }
        }
        finally {
            console.logExit();
        }
    } //unknown message

    //---------------- rejected a call
    public void fireCallRejectedLocally(String reason, Message invite)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("locally rejected call. reason="
                              + reason
                              + "\ninvite message=" + invite);
            }
            CallRejectedEvent evt = new CallRejectedEvent(reason, invite);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).
                    callRejectedLocally(
                    evt);
            }
        }
        finally {
            console.logExit();
        }
    }

    void fireCallRejectedRemotely(String reason, Message invite)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("call rejected remotely. reason="
                              + reason
                              + "\ninvite message=" + invite);
            }
            CallRejectedEvent evt = new CallRejectedEvent(reason, invite);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).
                    callRejectedRemotely(
                    evt);
            }
        }
        finally {
            console.logExit();
        }
    }

    //call rejected
    //---------------- error occurred
    public void fireCommunicationsError(Throwable throwable)
    {
        try {
            console.logEntry();
            console.error(throwable);
            CommunicationsErrorEvent evt = new CommunicationsErrorEvent(
                throwable);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).
                    communicationsErrorOccurred(evt);
            }
        }
        finally {
            console.logExit();
        }
    } //error occurred
    
    public void fireReceivedBlockedList(String blocked)
    {
        try {
            console.logEntry();
            BlockedListEvent evt = new BlockedListEvent((String)blocked);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).
                    receiveBlockedList(evt);
            }
        }
        finally {
            console.logExit();
        }
    } //error occurred
    
    public void fireReceivedFriendsList(String blocked)
    {
        try {
            console.logEntry();
            BlockedListEvent evt = new BlockedListEvent((String)blocked);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).
                    receiveFriendsList(evt);
            }
        }
        finally {
            console.logExit();
        }
    } //error occurred
    
    public void fireReceivedPrice(String blocked)
    {
        try {
            console.logEntry();
            BlockedListEvent evt = new BlockedListEvent((String)blocked);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                ( (CommunicationsListener) listeners.get(i)).
                    receivePrice(evt);
            }
        }
        finally {
            console.logExit();
        }
    } //error occurred

//============================= SIP LISTENER METHODS ==============================
    public void processRequest(RequestEvent requestReceivedEvent)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("received request=" + requestReceivedEvent);
            }
            ServerTransaction serverTransaction = requestReceivedEvent.
                getServerTransaction();
            Request request = requestReceivedEvent.getRequest();
            String method = ( (CSeqHeader) request.getHeader(CSeqHeader.NAME)).
                getMethod();
            if (serverTransaction == null) {
                try {
                    serverTransaction = sipProvider.getNewServerTransaction(
                        request);
                }
                catch (TransactionAlreadyExistsException ex) {
                    /*fireCommunicationsError(
                        new CommunicationsException(
                        "Failed to create a new server"
                        + "transaction for an incoming request\n"
                        + "(Next message contains the request)",
                        ex));
                    fireUnknownMessageReceived(request);*/
                    //let's not scare the user
                    console.error("Failed to create a new server"
                        + "transaction for an incoming request\n"
                        + "(Next message contains the request)",
                        ex
                    );

                    return;
                }
                catch (TransactionUnavailableException ex) {
                    /**
                    fireCommunicationsError(
                        new CommunicationsException(
                        "Failed to create a new server"
                        + "transaction for an incoming request\n"
                        + "(Next message contains the request)",
                        ex));
                    fireUnknownMessageReceived(request);*/
                    //let's not scare the user
                    console.error("Failed to create a new server"
                        + "transaction for an incoming request\n"
                        + "(Next message contains the request)",
                        ex
                    );
                    return;
                }
            }
            Dialog dialog = serverTransaction.getDialog();
            Request requestClone = (Request) request.clone();
            //INVITE
            if (request.getMethod().equals(Request.INVITE)) {
                console.debug("received INVITE");
                if(serverTransaction.getDialog().getState() == null)
                {
                    if(console.isDebugEnabled())
                        console.debug("request is an INVITE. Dialog state="
                                      +serverTransaction.getDialog().getState());
                    callProcessing.processInvite(serverTransaction, request);
                }
                else
                {
                    console.debug("request is a reINVITE. Dialog state="
                                      +serverTransaction.getDialog().getState());
                    callProcessing.processReInvite(serverTransaction, request);
                }
            }
            //ACK
            else if (request.getMethod().equals(Request.ACK)) {
                if (serverTransaction != null
                    && serverTransaction.getDialog().getFirstTransaction().
                    getRequest().getMethod().equals(Request.INVITE)) {
                    callProcessing.processAck(serverTransaction, request);
                }
                else {
                    // just ignore
                    console.debug("ignoring ack");
                }
            }
            //BYE
            else if (request.getMethod().equals(Request.BYE)) {
                if (dialog.getFirstTransaction().getRequest().getMethod().
                    equals(
                    Request.INVITE)) {
                    callProcessing.processBye(serverTransaction, request);
                }
            }
            //CANCEL
            else if (request.getMethod().equals(Request.CANCEL)) {
                if (dialog.getFirstTransaction().getRequest().getMethod().
                    equals(
                    Request.INVITE)) {
                    callProcessing.processCancel(serverTransaction, request);
                }
                else {
                    sendNotImplemented(serverTransaction, request);
                    fireUnknownMessageReceived(requestReceivedEvent.getRequest());
                }
            }
            //REFER
            else if (request.getMethod().equals(Request.REFER)) {
                console.debug("Received REFER request");
                transferProcessing.processRefer(serverTransaction, request);
            }
            else if (request.getMethod().equals(Request.INFO)) {
                /** @todo add proper request handling */
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
            else if (request.getMethod().equals(Request.MESSAGE)) {
                messageProcessing.processMessageRequest(serverTransaction, request);
                fireMessageReceived(request);
            }
            else if (request.getMethod().equals(Request.NOTIFY)) {
                watcher.processNotification(request, serverTransaction);
            }
            else if (request.getMethod().equals(Request.OPTIONS)) {
                /** @todo add proper request handling */
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
            else if (request.getMethod().equals(Request.PRACK)) {
                /** @todo add proper request handling */
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
            else if (request.getMethod().equals(Request.REGISTER)) {
                /** @todo add proper request handling */
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
            else if (request.getMethod().equals(Request.SUBSCRIBE)) {
                presenceAgent.processSubscription(request, serverTransaction);
            }
            else if (request.getMethod().equals(Request.UPDATE)) {
                /** @todo add proper request handling */
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
            else {
                //We couldn't recognise the message
                sendNotImplemented(serverTransaction, request);
                fireUnknownMessageReceived(requestReceivedEvent.getRequest());
            }
        }
        finally {
            console.logExit();
        }
    }

    public void processTimeout(TimeoutEvent transactionTimeOutEvent)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("received time out event: "
                              + transactionTimeOutEvent);
            }
            Transaction transaction;
            if (transactionTimeOutEvent.isServerTransaction()) {
                transaction = transactionTimeOutEvent.getServerTransaction();
            }
            else {
                transaction = transactionTimeOutEvent.getClientTransaction();
            }
            Request request =
                transaction.getRequest();
            if (request.getMethod().equals(Request.REGISTER)) {
                registerProcessing.processTimeout(transaction, request);
            }
            else if (request.getMethod().equals(Request.INVITE)) {
                callProcessing.processTimeout(transaction, request);
            }
            else {
                //Just show an error for now
                Console.showError("TimeOut Error!",
                    "Received a TimeoutEvent while waiting on a message"
                    + "\n(Check Details to see the message that caused it)",
                    request.toString()
                    );
            }
        }
        finally {
            console.logExit();
        }
    }

    //-------------------- PROCESS RESPONSE
    public void processResponse(ResponseEvent responseReceivedEvent)
    {
        try {
            console.logEntry();
            if (console.isDebugEnabled()) {
                console.debug("received response=" + responseReceivedEvent);
            }
            ClientTransaction clientTransaction = responseReceivedEvent.
                getClientTransaction();
            if (clientTransaction == null) {
                console.debug("ignoring a transactionless response");
                return;
            }
            Response response = responseReceivedEvent.getResponse();
            Dialog dialog = clientTransaction.getDialog();
            String method = ( (CSeqHeader) response.getHeader(CSeqHeader.NAME)).
                getMethod();
            Response responseClone = (Response) response.clone();
            
            if (response.getStatusCode() == 201) {
            	String response_phrase = response.getReasonPhrase();
            	String message = "";
            	if( response_phrase.equals("none")){
            		message = "There is no blocked user";
            	}
            	else{
            		String blocked_users[] = response_phrase.split(",");
            		for( int i = 0; i< blocked_users.length; i++){
            			if( i != blocked_users.length - 1){
            				message = message + blocked_users[i] + "\n";
            			}
            			else{
            				message = message + blocked_users[i] ;
            			}
            			
            		}
            	}
            	fireReceivedBlockedList(message);
            	return;
            }
            
            if (response.getStatusCode() == 202) {
            	String response_phrase = response.getReasonPhrase();
            	String message = "";
            	if( response_phrase.equals("none")){
            		message = "You have no friends!";
            	}
            	else{
            		String blocked_users[] = response_phrase.split(",");
            		for( int i = 0; i< blocked_users.length; i++){
            			if( i != blocked_users.length - 1){
            				message = message + blocked_users[i] + "\n";
            			}
            			else{
            				message = message + blocked_users[i] ;
            			}
            			
            		}
            	}
            	fireReceivedFriendsList(message);
            	return;
            }
            
            if (response.getStatusCode() == 203) {
            	String response_phrase = response.getReasonPhrase();
            	fireReceivedPrice(response_phrase);
            	return;
            }
            
            //OK
            if (response.getStatusCode() == Response.OK) {
                //REGISTER
                if (method.equals(Request.REGISTER)) {
                    registerProcessing.processOK(clientTransaction, response);
                }//INVITE
                else if (method.equals(Request.INVITE)) {
                    callProcessing.processInviteOK(clientTransaction, response);
                }//BYE
                else if (method.equals(Request.BYE)) {
                    callProcessing.processByeOK(clientTransaction, response);
                }//CANCEL
                else if (method.equals(Request.CANCEL)) {
                    callProcessing.processCancelOK(clientTransaction, response);
                }
                else if (method.equals(Request.SUBSCRIBE)) {
                    watcher.processSubscribeOK(clientTransaction, response);
                }

            }
            //ACCEPTED
            else if (response.getStatusCode() == Response.ACCEPTED) {
                //SUBSCRIBE
                if (method.equals(Request.SUBSCRIBE)) {
                    watcher.processSubscribeOK(clientTransaction, response);
                }
            }
            //TRYING
            else if (response.getStatusCode() == Response.TRYING
                     //process all provisional responses here
                     //reported by Les Roger Davis
                     || response.getStatusCode() / 100 == 1) {
                if (method.equals(Request.INVITE)) {
                    callProcessing.processTrying(clientTransaction, response);
                }
                //We could also receive a TRYING response to a REGISTER req
                //bug reports by
                //Steven Lass <sltemp at comcast.net>
                //Luis Vazquez <luis at teledata.com.uy>
                else if(method.equals(Request.REGISTER))
                {
                    //do nothing
                }
                else {
                    fireUnknownMessageReceived(response);
                }
            }
            //RINGING
            else if (response.getStatusCode() == Response.RINGING) {
                if (method.equals(Request.INVITE)) {
                    callProcessing.processRinging(clientTransaction, response);
                }
                else {
                    fireUnknownMessageReceived(response);
                }
            }
            //NOT_FOUND
            else if (response.getStatusCode() == Response.NOT_FOUND) {
                if (method.equals(Request.INVITE)) {
                    callProcessing.processNotFound(clientTransaction, response);
                }
                if (method.equals(Request.SUBSCRIBE)) {
                    watcher.processNotFound(clientTransaction, response);
                }
                else {
                    fireUnknownMessageReceived(response);
                }
            }
            //NOT_IMPLEMENTED
            else if (response.getStatusCode() == Response.NOT_IMPLEMENTED) {
                if (method.equals(Request.REGISTER)) {
                    //Fixed typo issues - Reported by pizarro
                    registerProcessing.processNotImplemented(clientTransaction,
                        response);
                }
                else if (method.equals(Request.INVITE)) {
                    callProcessing.processNotImplemented(clientTransaction,
                        response);
                }
                else {
                    fireUnknownMessageReceived(response);
                }
            }
            //REQUEST_TERMINATED
            else if (response.getStatusCode() == Response.REQUEST_TERMINATED) {
                callProcessing.processRequestTerminated(clientTransaction,
                    response);
            }
            //BUSY_HERE
            else if (response.getStatusCode() == Response.BUSY_HERE) {
                if (method.equals(Request.INVITE)) {
                    callProcessing.processBusyHere(clientTransaction, response);
                }
                else {
                    fireUnknownMessageReceived(response);
                }
            }
            //401 UNAUTHORIZED
            else if (response.getStatusCode() == Response.UNAUTHORIZED
                     || response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED) {
                if(method.equals(Request.INVITE))
                    callProcessing.processAuthenticationChallenge(clientTransaction, response);
                else if(method.equals(Request.REGISTER))
                    registerProcessing.processAuthenticationChallenge(clientTransaction, response);
                else if(method.equals(Request.SUBSCRIBE))
                    watcher.processAuthenticationChallenge(clientTransaction, response);
                else
                    fireUnknownMessageReceived(response);
            }
            //Other Errors
            else if ( //We'll handle all errors the same way so no individual handling
                     //is needed
                     //response.getStatusCode() == Response.NOT_ACCEPTABLE
                     //|| response.getStatusCode() == Response.SESSION_NOT_ACCEPTABLE
                     response.getStatusCode() / 100 == 4
                     )
            {
               if (method.equals(Request.INVITE)) {
                   callProcessing.processCallError(clientTransaction, response);
               }
               else {
            	   if(response.getStatusCode() == Response.FORBIDDEN)
                       fireReceivedBlockedList("_WRONGUSERNAME");
            	   else if(response.getStatusCode() == Response.NOT_ACCEPTABLE)
                       fireReceivedBlockedList("_WRONGPASSWORD");
            	   else fireUnknownMessageReceived(response);
               }

            }
            else if (response.getStatusCode() == Response.ACCEPTED) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.ADDRESS_INCOMPLETE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.ALTERNATIVE_SERVICE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.AMBIGUOUS) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.BAD_EVENT) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.BAD_EXTENSION) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.BAD_GATEWAY) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.BAD_REQUEST) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.BUSY_EVERYWHERE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
                     Response.CALL_IS_BEING_FORWARDED) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
                     Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.DECLINE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
                     Response.DOES_NOT_EXIST_ANYWHERE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.EXTENSION_REQUIRED) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.FORBIDDEN) {
                /** @todo add proper request handling */
                fireReceivedBlockedList("_WRONGUSERNAME");
            }
            else if (response.getStatusCode() == Response.GONE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.INTERVAL_TOO_BRIEF) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.LOOP_DETECTED) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.MESSAGE_TOO_LARGE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.METHOD_NOT_ALLOWED) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.MOVED_PERMANENTLY) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.MOVED_TEMPORARILY) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.MULTIPLE_CHOICES) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.NOT_ACCEPTABLE_HERE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.PAYMENT_REQUIRED) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.QUEUED) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
                     Response.REQUEST_ENTITY_TOO_LARGE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.REQUEST_PENDING) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.REQUEST_TIMEOUT) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.REQUEST_URI_TOO_LONG) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.SERVER_INTERNAL_ERROR) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.SERVER_TIMEOUT) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.SERVICE_UNAVAILABLE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
                     Response.SESSION_NOT_ACCEPTABLE) {
                /** @todo add proper request handling */
                fireReceivedBlockedList("_WRONGPASSWORD");
            }
            else if (response.getStatusCode() == Response.SESSION_PROGRESS) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
                     Response.TEMPORARILY_UNAVAILABLE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.TOO_MANY_HOPS) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.UNDECIPHERABLE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
                     Response.UNSUPPORTED_MEDIA_TYPE) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() ==
                     Response.UNSUPPORTED_URI_SCHEME) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.USE_PROXY) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else if (response.getStatusCode() == Response.VERSION_NOT_SUPPORTED) {
                /** @todo add proper request handling */
                fireUnknownMessageReceived(response);
            }
            else { //We couldn't recognise the message
                fireUnknownMessageReceived(response);
            }
        }
        finally {
            console.logExit();
        }
    } //process response

    //--------
    String getLocalHostAddress()
    {
        try {
            console.logEntry();
            //network address management is handled by common.NetworkAddressManager
            //String hostAddress = Utils.getProperty("javax.sip.IP_ADDRESS");
//            if (hostAddress == null) {
            InetAddress localhost = NetworkAddressManager.getLocalHost();
            String hostAddress = localhost.getHostAddress();

            if (console.isDebugEnabled()) {
                console.debug("returning addres=" + hostAddress);
            }
            return hostAddress;
        }
        finally {
            console.logExit();
        }
    }

    protected void checkIfStarted() throws CommunicationsException
    {
        if (!isStarted) {
            console.error("attempt to use the stack while not started");
            throw new CommunicationsException(
                "The underlying SIP Stack had not been"
                + "properly initialised! Impossible to continue");
        }
    }

    public void sendServerInternalError(int callID) throws
        CommunicationsException
    {
        try {
            console.logEntry();
            checkIfStarted();
            callProcessing.sayInternalError(callID);
        }
        finally {
            console.logExit();
        }
    }

//======================================= SIMPLE ==========================================

    /**
     * Retrieves a Contact List from the specified URL.
     * @param url the location where the list is to be retrieved from.
     * @throws CommunicationsException if we fail to retrieve the list.
     * @return ContactGroup the contact list retrieved from the specified URL
     */
    public ContactGroup retrieveContactList(String url)
        throws CommunicationsException
    {
        try{
            console.logEntry();
            checkIfStarted();

            return contactListController.loadContactList(url);
        }
        finally
        {
            console.logExit();
        }

    }

    /**
     * Returns an iterator over strings representing supported
     * @return an iterator over strings representing supported
     */
    public Iterator getSupportedStatusSet()
    {
        return presenceStatusManager.getSupportedStatusSet();
    }

    /**
     * Adds a listener for the StatusEvents posted after status changes.
     *
     * @param listener the listener to add
     * @param listener PresenceStatusListener
     */
    public void addPresenceStatusListener(StatusListener listener)
    {
        presenceStatusManager.addStatusListener(listener);
    }

    /**
     * Removes a listener previously added with
     * <code>addStatusListener</code>.
     *
     * @param listener the listener to remove
     */
    public void removePresenceStatusListener(StatusListener listener)
    {
        presenceStatusManager.removeStatusListener(listener);
    }

    /**
     * Causes the presence user agent to shift to the status represented by the
     * specified string.
     * @param newStatusString A string representing the status that the PUA is
     * to shift to. Allowed values could be found in PresenceTuple.XXX_STATUS
     * vars
     * @throws CommunicationsException if changing the status fails
     */
    public void requestPresenceStatusChange(String newStatusDescriptorStr)
        throws CommunicationsException
    {
        presenceStatusManager.requestStatusChange(newStatusDescriptorStr);
    }

    /**
     * Returns the current presence status of the presence user agent.
     *
     * @return String the current presence status of the presence user agent.
     */
    public PresenceTuple getPresenceStatus()
    {
        return presenceStatusManager.getCurrentStatus();
    }

    /**
     * Returns the currently active PresenceStatusManager instance.
     * @return PresenceStatusManager the currently active presenceStatusManager
     * instance.
     */
    public PresenceStatusManager getPresenceStatusManager()
    {
        return presenceStatusManager;
    }

    /**
     * Returns the ContactListController instance that currently manages contacts
     * status notification messages.
     * @return the ContactListController instance that currently manages contacts
     * status notification messages.
     */
    public ContactListController getContactListController()
    {
        return contactListController;
    }

    /**
     * Sets the SubscritpionAuthority instance that should be consulted later on
     * for incoming Subscription approbation.
     *
     * @param authority a valid SubscriptionAuthority instance.
     */
    public void setSubscritpionAuthority(SubscriptionAuthority authority)
    {
        //keep a copy
        this.subscriptionAuthority = authority;
        this.presenceStatusManager.setSubscritpionAuthority(authority);

    }
    
    /**
     * Registers using the specified public address. If public add
     * @param publicAddress
     * @throws CommunicationsException
     */
    public void firstTimeRegister(String publicAddress, String password, String email,
    		String address) throws CommunicationsException
    {
    	String tempUri = this.currentlyUsedURI;
    	String username = publicAddress;
        try {
            console.logEntry();

            if(publicAddress == null || publicAddress.trim().length() == 0)
                return; //maybe throw an exception?


            //Handle default domain name (i.e. transform 1234 -> 1234@sip.com
            String defaultDomainName =
                Utils.getProperty("net.java.sip.communicator.sip.DEFAULT_DOMAIN_NAME");

            //feature request, Michael Robertson (sipphone.com)
            //strip the following chars of their user names: ( - ) <space>
            if(publicAddress.toLowerCase().indexOf("sipphone.com") != -1
               || defaultDomainName.indexOf("sipphone.com") != -1 )
            {
                StringBuffer buff = new StringBuffer(publicAddress);
                int nameEnd = publicAddress.indexOf('@');
                nameEnd = nameEnd==-1?Integer.MAX_VALUE:nameEnd;
                nameEnd = Math.min(nameEnd, buff.length())-1;

                int nameStart = publicAddress.indexOf("sip:");
                nameStart = nameStart == -1 ? 0 : nameStart + "sip:".length();

                for(int i = nameEnd; i >= nameStart; i--)
                    if(!Character.isLetter( buff.charAt(i) )
                       && !Character.isDigit( buff.charAt(i)))
                        buff.deleteCharAt(i);
                publicAddress = buff.toString();
            }


            // if user didn't provide a domain name in the URL and someone
            // has defined the DEFAULT_DOMAIN_NAME property - let's fill in the blank.
            if (defaultDomainName != null
                && publicAddress.indexOf('@') == -1 //most probably a sip uri
                ) {
                publicAddress = publicAddress + "@" + defaultDomainName;
            }

            if (!publicAddress.trim().toLowerCase().startsWith("sip:")) {
                publicAddress = "sip:" + publicAddress;
            }
            
            
            this.currentlyUsedURI = publicAddress;
            System.out.println("publicAddress =" + publicAddress);

            registerProcessing.firstTimeRegister( registrarAddress,
					 username,
					 password,
					 email, address);
          
        }
        finally {
        	this.currentlyUsedURI = tempUri;
            console.logExit();
        }
    }

    public synchronized void sendInfo(String forwardUser) throws
    CommunicationsException
    {
    	try
    	{
    		console.logEntry();


    		//Handle default domain name (i.e. transform 1234 -> 1234@sip.com
    		String defaultDomainName =
    				Utils.getProperty("net.java.sip.communicator.sip.DEFAULT_DOMAIN_NAME");

    		//Request URI
    		URI requestURI;
    		try {
    			requestURI = addressFactory.createSipURI(null,registrarAddress);
    		}
    		catch (ParseException ex) {
    			console.error(registrarAddress + " is not a legal SIP uri!", ex);
    			throw new CommunicationsException(registrarAddress +
    					" is not a legal SIP uri!", ex);
    		}
    		
    		//Call ID
    		CallIdHeader callIdHeader = sipProvider.getNewCallId();
    		//CSeq
    		CSeqHeader cSeqHeader;
    		try {
    			cSeqHeader = headerFactory.createCSeqHeader(1,
    					Request.INFO);
    		}
    		catch (ParseException ex) {
    			//Shouldn't happen
    			console.error(ex, ex);
    			throw new CommunicationsException(
    					"An unexpected erro occurred while"
    							+ "constructing the CSeqHeadder", ex);
    		}
    		catch (InvalidArgumentException ex) {
    			//Shouldn't happen
    			console.error(
    					"An unexpected erro occurred while"
    							+ "constructing the CSeqHeadder", ex);
    			throw new CommunicationsException(
    					"An unexpected erro occurred while"
    							+ "constructing the CSeqHeadder", ex);
    		}
    		//FromHeader
    		FromHeader fromHeader = getFromHeader();
    		//ToHeader
    		Address toAddress = addressFactory.createAddress(
    				requestURI);
    		ToHeader toHeader;
    		try {
    			toHeader = headerFactory.createToHeader(
    					toAddress, null);
    		}
    		catch (ParseException ex) {
    			//Shouldn't happen
    			console.error(
    					"Null is not an allowed tag for the to header!", ex);
    			throw new CommunicationsException(
    					"Null is not an allowed tag for the to header!", ex);
    		}
    		//ViaHeaders
    		ArrayList viaHeaders = getLocalViaHeaders();
    		//MaxForwards
    		MaxForwardsHeader maxForwards = getMaxForwardsHeader();
    		//Contact
    		ContactHeader contactHeader = getContactHeader();
    		Request info = null;
    		try {
    			info =  messageFactory.createRequest(requestURI,
    					Request.INFO,
    					callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders,
    					maxForwards);
    		}
    		catch (ParseException ex) {
    			console.error(
    					"Failed to create info Request!", ex);
    			throw new CommunicationsException(
    					"Failed to create info Request!", ex);
    		}
    		//
    		info.addHeader(contactHeader);
    		
    		//Content
    		ContentTypeHeader contentTypeHeader = null;
    		try {
    			contentTypeHeader =
    					headerFactory.createContentTypeHeader(
    							"text", "plain");
    		}
    		catch (ParseException ex) {
    			//Shouldn't happen
    			console.error(
    					"Failed to create a content type header for the INFO request",
    					ex);
    			throw new CommunicationsException(
    					"Failed to create a content type header for the INFO request",
    					ex);
    		}
    		//Creating Message Body
    		try {
    			String body = forwardUser;
    			byte[] messageBody = body.getBytes(Charset.forName("UTF-8"));
    			info.setContent(messageBody, contentTypeHeader);
    		}
    		catch (ParseException ex) {
    			console.error(
    					"Failed to parse registration data while creating info request!", ex);
    			throw new CommunicationsException(
    					"Failed to parse registration data while creating info request!", ex);
    		};
    		//Transaction
    		ClientTransaction infoTransaction;
    		try {
    			infoTransaction = sipProvider.
    					getNewClientTransaction(info);
    		}
    		catch (TransactionUnavailableException ex) {
    			console.error(
    					"Failed to create infoTransaction.\n" +
    							"This is most probably a network connection error.", ex);
    			throw new CommunicationsException(
    					"Failed to create infoTransaction.\n" +
    							"This is most probably a network connection error.", ex);
    		}
    		try {
    			infoTransaction.sendRequest();
    			if( console.isDebugEnabled() )
    				console.debug("sent request: " + info);
    		}
    		catch (SipException ex) {
    			console.error(
    					"An error occurred while sending info request", ex);
    			throw new CommunicationsException(
    					"An error occurred while sending info request", ex);
    		}
    	}
    	finally
    	{
    		console.logExit();
    	}

    }
    
    
    
}

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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.TransactionUnavailableException;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;

import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.common.Scheduler;
import net.java.sip.communicator.common.Utils;
import net.java.sip.communicator.sip.CommunicationsException;
import net.java.sip.communicator.sip.SipManager;
import net.java.sip.communicator.sip.security.SipSecurityException;


/**
 * @author Martin Andre
 */
public class Watcher
{
    protected static final Console console = Console.getConsole(Watcher.class);
    protected SipManager   sipManCallback = null;
    private   Vector       outgSubscriptions  = new Vector();
    private   int          expiresSubscription;

    private   WatcherEventsDispatcher watcherEventsDispatcher = null;

    public Watcher(SipManager sipManCallback)
    {
        this.sipManCallback = sipManCallback;
        try {
            this.expiresSubscription = Integer.parseInt(Utils.getProperty("net.java.sip.communicator.sip.simple.SUBSCRIPTION_EXP_TIME"));
        }
        catch (NumberFormatException ex) {
            ex.printStackTrace();
            expiresSubscription = 600;	/* Default value */
        }
    }

    void setSipManagerCallBack(SipManager sipManCallback)
    {
        this.sipManCallback = sipManCallback;
    }


    /**
     * Attempts to re-generate the corresponding request with the proper
     * credentials and terminates the call if it fails.
     *
     * @param clientTransaction the corresponding transaction
     * @param response the challenge
     */
    public void processAuthenticationChallenge(ClientTransaction clientTransaction,
                                        Response response)
    {
        try {
            console.logEntry();

            Request register = clientTransaction.getRequest();

            Request reoriginatedRequest = null;

            ClientTransaction retryTran = sipManCallback.sipSecurityManager.
                handleChallenge(response,
                                clientTransaction.getBranchId(),
                                register);

            //Dialog dialog = clientTransaction.getDialog();


            retryTran.sendRequest();

        }
        catch (SipSecurityException exc) {
            sipManCallback.fireCommunicationsError(
                new CommunicationsException("Authorization failed!", exc));
        }
        catch (Exception exc) {
            sipManCallback.fireCommunicationsError(
                new CommunicationsException("Failed to resend a request "
                                            + "after a security challenge!",
                                            exc)
                );
        }
    }


    /**
     * Subscribes to the presence information of a presentity. A SUBSCRIBE message is constructed
     * and sent using the presentity and the subscriber addresses. If a subscription already exists for
     * the presentity and the subscriber, invoking this method will renew the subscription explicitly and
     * the application will receive a NOTIFY message with the current presence information for the
     * presentity.
     *
     * @param presentity the address of the presentity to whose presence information the subscriber wishes to subscribe.
     * @return transaction the ID associated with the SUBSCRIBE message sent by this method.
     * @throws CommunicationsException
     */
    public java.lang.String sendSubscription(String presentity)
        throws CommunicationsException
    {
        try
        {
            console.logEntry();

            presentity = presentity.trim();
            //Handle default domain name (i.e. transform 1234 -> 1234@sip.com
            String defaultDomainName =
                Utils.getProperty("net.java.sip.communicator.sip.DEFAULT_DOMAIN_NAME");
            if (defaultDomainName != null //no sip scheme
                && !presentity.trim().startsWith("tel:")
                && presentity.indexOf('@') == -1 //most probably a sip uri
                ) {
                    presentity = presentity + "@" + defaultDomainName;
            }

            //Let's be uri fault tolerant
            if (presentity.toLowerCase().indexOf("sip:") == -1 //no sip scheme
                && presentity.indexOf('@') != -1 //most probably a sip uri
                ) {
                    presentity = "sip:" + presentity;
            }

            //Request URI
            URI requestURI;
            try {
                requestURI = sipManCallback.addressFactory.createURI(presentity);
            }
            catch (ParseException ex) {
                console.error(presentity + " is not a legal SIP uri!", ex);
                throw new CommunicationsException(presentity +
                                                  " is not a legal SIP uri!", ex);
            }
            //Call ID
            CallIdHeader callIdHeader = sipManCallback.sipProvider.getNewCallId();
            //CSeq
            CSeqHeader cSeqHeader;
            try {
                cSeqHeader = sipManCallback.headerFactory.createCSeqHeader(1,
                    Request.SUBSCRIBE);
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
            FromHeader fromHeader = sipManCallback.getFromHeader();
            //ToHeader
            Address toAddress = sipManCallback.addressFactory.createAddress(
                requestURI);
            ToHeader toHeader;
            try {
                toHeader = sipManCallback.headerFactory.createToHeader(
                    toAddress, null);
            }
            catch (ParseException ex) {
                //Shouldn't happen
                console.error(
                    "Null is not an allowed tag for the to header!", ex);
                throw new CommunicationsException(
                    "Null is not an allowed tag for the to header!", ex);
            }

            EventHeader eventHeader = null;
            try {
                eventHeader = sipManCallback.headerFactory.createEventHeader("presence");
            }
            catch (ParseException ex) {
                //Shouldn't happen
                console.error(
                    "Unable to create event header!", ex);
                throw new CommunicationsException(
                    "Unable to create event header!", ex);
            }

            ExpiresHeader expiresHeader = null;
            try {
                expiresHeader = sipManCallback.headerFactory.createExpiresHeader(expiresSubscription);
            }
            catch (InvalidArgumentException ex) {
                console.error(
                    "Expires Header must be an integer!", ex);
                throw new CommunicationsException(
                    "Expires Header must be an integer!", ex);
            }

            //ViaHeaders
            ArrayList viaHeaders = sipManCallback.getLocalViaHeaders();
            //MaxForwards
            MaxForwardsHeader maxForwards = sipManCallback.getMaxForwardsHeader();
            //Contact
            ContactHeader contactHeader = sipManCallback.getContactHeader();
            Request subscribe = null;
            try {
                subscribe = sipManCallback.messageFactory.createRequest(requestURI,
                    Request.SUBSCRIBE,
                    callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders,
                    maxForwards);
            }
            catch (ParseException ex) {
                console.error(
                    "Failed to create subscribe Request!", ex);
                throw new CommunicationsException(
                    "Failed to create subscribe Request!", ex);
            }

            subscribe.addHeader(contactHeader);
            subscribe.addHeader(eventHeader);
            subscribe.addHeader(expiresHeader);

            return sendSubscription(subscribe);

        } finally {
            console.logExit();
        }
    }


    /**
    * Sends a previously formed SUBSCRIBE message to a notifier. If a subscription already exists for
    * the presentity and the subscriber present in the subscribe request, invoking this method will 
    * renew the subscription explicitly and the application will receive a NOTIFY message with the current
    * presence information for the presentity.
    * 
    * @param subscribe SUBSCRIBE message to be sent to the notifier.
    * @return transaction ID associated with the SUBSCRIBE message sent by this method.
    * @throws CommunicationsException
    */
    public java.lang.String sendSubscription(Request subscribe)
        throws CommunicationsException
    {
        try
        {
            console.logEntry();

            ClientTransaction subscribeTransaction = null;
            String presentity = subscribe.getRequestURI().toString();
            String subscriber = sipManCallback.getFromHeader().getAddress().getURI().toString();
            
            try {
                subscribeTransaction = sipManCallback.sipProvider.
                    getNewClientTransaction(subscribe);
            }
            catch (TransactionUnavailableException ex) {
                console.error(
                    "Failed to create subscribeTransaction.", ex);
                throw new CommunicationsException(
                    "Failed to create subscribeTransaction.", ex);
            }

            try {
                subscribeTransaction.sendRequest();

                if( console.isDebugEnabled() )
                    console.debug("sent request: " + subscribe);

                Scheduler scheduler = Scheduler.getInstance();
                // We keep a margin of 10% for subscription to be resend
                // for exemple if expires is 10 min, a the subscription will be refreshed in 9 min
                long expires = expiresSubscription * 900;

                //is this a new subscription or have we already send subscriptions
                //to the same subscriber
                int sIndex = getIndexInSubscriptionCache(subscriber, presentity);
                if (sIndex == -1)
                {
                    /* First subscription for the couple subscriber/presentity */
                    Subscription sub = new Subscription(
                        subscriber,
                        presentity,
                        subscribeTransaction.getDialog(),
                        subscribe,
                        Subscription.OUTGOING_SUBSCRIPTION,
                        Subscription.PENDING,
                        null,
                        "pidf+xml",
                        this);
                    outgSubscriptions.add(sub);
                    scheduler.scheduleAtFixedRate(sub, new Date(System.currentTimeMillis() + expires), expires);
                }
            }
            catch (SipException ex) {
                console.error(
                    "An error occurred while sending subscribe request", ex);
                throw new CommunicationsException(
                    "An error occurred while sending subscribe request", ex);
            }
                
            return subscribeTransaction.toString();
        } finally {
            console.logExit();
        }
    }
    
    
    /**
    * Sends a SUBSCRIBE message to a notifier using a specific dialog. This method is mainly used to
    * refresh subscribtion to a notifier. Invoking this method will renew the subscription explicitly and 
    * the application will receive a NOTIFY message with the current presence information for the 
    * presentity.
    * This method should be used to handle subscription refresh and unsubscribe.
    * 
    * @param dialog the dialog to use
    * @return transaction ID associated with the SUBSCRIBE message sent by this method.
    * @throws CommunicationsException
    */
    public java.lang.String sendSubscription(Dialog dialog)
        throws CommunicationsException
    {
        try
        {
            console.logEntry();

            ClientTransaction subscribeTransaction = null;
            Request subscribe = null;
            
            try {
                subscribe = dialog.createRequest(Request.SUBSCRIBE);
            }
            catch (SipException ex) {
                console.error(
                    "Failed to create subscribe request.", ex);
                throw new CommunicationsException(
                    "Failed to create subscribe request.", ex);
            }
            
            try {
                subscribeTransaction = sipManCallback.sipProvider.
                    getNewClientTransaction(subscribe);
            }
            catch (TransactionUnavailableException ex) {
                console.error(
                    "Failed to create subscribeTransaction.", ex);
                throw new CommunicationsException(
                    "Failed to create subscribeTransaction.", ex);
            }

            try {
                dialog.sendRequest(subscribeTransaction);

                if( console.isDebugEnabled() )
                    console.debug("sent request: " + subscribe);
            }
            catch (SipException ex) {
                console.error(
                    "An error occurred while sending notify request", ex);
                throw new CommunicationsException(
                    "An error occurred while sending notify request", ex);
            }
                
            return subscribeTransaction.toString();
        } finally {
            console.logExit();
        }
    }


    /**
     * Responds to a received notification (NOTIFY message). This method responds to the NOTIFY requests as follows:
     * 1) Determines whether there is a subscription corresponding to this NOTIFY request or not.
     * 2) If such a subscription exists, sends a "200 OK" response.
     * 3) If such a subscription does not exist, sends a "481 Call/Transaction Does Not Exist" response.
     *
     * @param notification the received NOTIFY message.
     * @param transaction the transaction associated with the received NOTIFY request.
     * @return the status code (200 or 481) of the response sent by this method.
     */
    public int processNotification(Request notification, ServerTransaction transaction)
    {
        try
        {
            console.logEntry();

            int returnedResponse = 0;
            String subscriber =  ((ToHeader) notification.getHeader(ToHeader.NAME)).getAddress().getURI().toString();
            String presentity =  ((FromHeader) notification.getHeader(FromHeader.NAME)).getAddress().getURI().toString();

            Response responseMsg = null;
            int rank = getIndexInSubscriptionCache(subscriber, presentity);
            if (rank != -1) {
                //Suscription exists
                try {
                    responseMsg = sipManCallback.messageFactory.createResponse(
                        Response.OK, notification);
                }
                catch (ParseException ex) {
                    console.error("Failed to create an 200 response to a NOTIFY request.", ex);
                    sipManCallback.fireCommunicationsError(
                                new CommunicationsException("Failed to create an 200 response to a NOTIFY request.", ex));
                    return -1;
                }

                returnedResponse = Response.OK;

                //dispatch event
                ContentTypeHeader  ctHeader =
                    (ContentTypeHeader)notification.getHeader(ContentTypeHeader.NAME);
                FromHeader fromHeader =
                    (FromHeader)notification.getHeader(FromHeader.NAME);
                GenericURI senderURI = new GenericURI();
                senderURI.setScheme(fromHeader.getAddress().getURI().getScheme());
                String address = ((SipURI)fromHeader.getAddress().getURI()).getHost();
                String user    = ((SipURI)fromHeader.getAddress().getURI()).getUser();
                if(user != null && user.length() > 0)
                    address = user + '@' + address;
                senderURI.setAddressPart( address );
                int port = ((SipURI)fromHeader.getAddress().getURI()).getPort();
                if(port != -1 && port != 5060)
                    senderURI.setPort( port );
                String contentSubtype = "pdif+xml";
                if(ctHeader != null)
                    contentSubtype = ctHeader.getContentSubType();
                watcherEventsDispatcher.dispatchNotification(
                    senderURI, contentSubtype, notification.getRawContent());
            }
            else {
                //Suscribtion does not exist
                try {
                    responseMsg = sipManCallback.messageFactory.createResponse(
                                        Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, notification);
                }
                catch (ParseException ex) {
                    console.error("Failed to create a 481 response to a NOTIFY request.", ex);
                    sipManCallback.fireCommunicationsError(
                                new CommunicationsException("Failed to create an 481 response to a NOTIFY request.", ex));
                    return -1;
                }
                returnedResponse = Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST;
            }
            sipManCallback.attachToTag(responseMsg, transaction.getDialog());
            try {
                transaction.sendResponse(responseMsg);
            }
            catch (SipException ex) {
                console.error("Failed to send a response to a notify request!", ex);
                    sipManCallback.fireCommunicationsError(
                                new CommunicationsException("Failed to send a response to a notify request!", ex));
                    return -1;
            }

            return returnedResponse;

        } finally {
            console.logExit();
        }
    }

    /**
     * Search for the position of the subscription identified by the couple subscriber/presentity
     * @param subscriber the subscriber's address including sip scheme
     * @param presentity the presentity's address including sip scheme
     * @return the position of the subscription in the Vector, and -1 if the couple is not found
     */
    public int getIndexInSubscriptionCache(String subscriber, String presentity)
    {
        for (int i=0; i<outgSubscriptions.size(); i++)
        {
            if (((Subscription) outgSubscriptions.elementAt(i)).equals(subscriber, presentity))
                return i;
        }
        return -1;
    }
    
    
    /**
     * Unsubscribe from a notifier.
     * @param subscriber the user whose subscription to the presentity is to be removed.
     * @param presentity the user from whom the subscriber's subscription is to be removed.
     * @throws CommunicationsException
     */
    public void removeSubscription(String subscriber, String presentity)
        throws CommunicationsException
    {
        try {
            console.logEntry();

            int rank = getIndexInSubscriptionCache(subscriber, presentity);
            if (rank != -1)
            {
                //Subscription is in outgoing subscription list
                Subscription sub = (Subscription) outgSubscriptions.elementAt(rank);

                Request subscribe = (Request)sub.getRequest().clone();
                try {
                    //Increment CSeq
                    CSeqHeader cseqHeader = (CSeqHeader)subscribe.getHeader(CSeqHeader.NAME);
                    int cseq =  cseqHeader.getSequenceNumber() + 1;
                    cseqHeader.setSequenceNumber(cseq);
                }
                catch (InvalidArgumentException ex) {
                    ex.printStackTrace();
                    console.error("Cseq Header must contain a integer value!",
                                ex);
                    throw new CommunicationsException(
                            "Cseq Header must contain a integer value!");
                }

                try {
                    //Set Expires Header to 0
                    ExpiresHeader expiresHeader = (ExpiresHeader)subscribe.getHeader(ExpiresHeader.NAME);
                    expiresHeader.setExpires(0);
                }
                catch (InvalidArgumentException ex) {
                    //Shouldn't happen
                    console.error(
                        "The expires header must be an integer!", ex);
                    throw new CommunicationsException(
                        "The expires header must be an integer!", ex);
                }
                
                sendSubscription(subscribe);
                
                //At least, remove the subscription
                outgSubscriptions.remove(rank);
                if(console.isDebugEnabled())
                            console.debug("Removing " + subscriber + "/"
                                          + presentity + " from subscription list");
            }
        } finally {
            console.logExit();
        }

    }
    
    /**
     * Removes all outgoing subscriptions.
     * @throws CommunicationsException
     */
    public void removeAllSubscriptions()
        throws CommunicationsException
    {
        try
        {
            console.logEntry();
            for (int i=0; i<outgSubscriptions.size(); i++) {
                Subscription sub = (Subscription) outgSubscriptions.firstElement();
                removeSubscription(sub.getSubscriber(), sub.getPresentity());
            }

        } finally {
            console.logExit();
        }
    }

    /**
     * @todo handle properly OK messages
     *
     * @param clientTransaction ClientTransaction
     * @param ok Response
     */
    public void processSubscribeOK(ClientTransaction clientTransaction, Response ok)
    {
        if(console.isDebugEnabled())
            console.debug("Received an OK response for a previous subscribe");
    }

    /**
    * @todo handle properly NOT_FOUND messages
    *
    * @param clientTransaction ClientTransaction
    * @param notFound Response
    */
   public void processNotFound(ClientTransaction clientTransaction, Response notFound)
   {
       if(console.isDebugEnabled())
           console.debug("Received a NOT_FOUND response for a previous subscribe");
   }


    /**
     * Registers a WatcherEventsDispatcher instance with this watcher. The WatcherEventsDispatcher is
     * used for event dispatching purposes.
     * @param dispatcher a WatcherEventsDispatcher instance to use when dispatching
     * notifications.
     */
    public void setWatcherEventsDispatcher(WatcherEventsDispatcher dispatcher)
    {
        this.watcherEventsDispatcher = dispatcher;
    }

}

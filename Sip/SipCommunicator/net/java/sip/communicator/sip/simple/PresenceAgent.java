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
import java.util.Enumeration;
import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.common.Scheduler;
import net.java.sip.communicator.common.Utils;
import net.java.sip.communicator.sip.CommunicationsException;
import net.java.sip.communicator.sip.SipManager;
import java.util.*;

/**
 * @author Martin Andre
 */
public class PresenceAgent
{
    private static final Console console    = Console.getConsole(PresenceAgent.class);

    protected SipManager sipManCallback       = null;
    protected ArrayList listenerList          = new ArrayList();
    private   int minExpires;
    private   Vector incSubscriptions         = new Vector();
    private   LocalPresenceUserAgent localPUA = null;
    private   Vector suppotedPidFormats       = new Vector();


    public PresenceAgent(SipManager sipManCallback)
    {
        this.sipManCallback = sipManCallback;
        try {
            this.minExpires = Integer.parseInt(Utils.getProperty("net.java.sip.communicator.sip.simple.MIN_EXP_TIME"));
        }
        catch (NumberFormatException ex) {
            minExpires = 120;	/* Default value */
            console.error(
                "Unable to read minimum expire time from configuration file!\n" +
                "Using 120 as a default value."
                , ex);
            sipManCallback.fireCommunicationsError(new CommunicationsException(
                "Unable to read minimum expire time from configuration file!\n" +
                "Using 120 as a default value."
                , ex));
        }
    }

    void setSipManagerCallBack(SipManager sipManCallback)
    {
        this.sipManCallback = sipManCallback;
    }

    /**
     * Processes received SUBSCRIBE requests. The method is called by the SipManager
     * when an incoming subscription is received. It would first verify whether
     * the request is a refresh subscription and if so resend the response that it
     * had previosly received followed by a NOTIFY request containing date corresponding
     * to its user AuthorizationStatus. If this is the first SUBSCRIBE request received from the subscriber,
     * a  subscription authorization is request from the LocalPresenceUserAgent instance that would
     * check for predefined authorizations and eventually delegate the request
     * to the gui module for user intervention.
     * @param subscription the received SUBSCRIBE message.
     * @param transactionID the transaction ID associated with the received SUBSCRIBE request.
     * @return the status code of the response sent by this method.
     */
    public int processSubscription(Request subscribeRequest, ServerTransaction transaction)
    {
        try
        {
            console.logEntry();

            int subscriptionResponse = 0;
            URI requestUri = ( (ToHeader) subscribeRequest.getHeader(ToHeader.NAME)).
                            getAddress().getURI();

            if (!requestUri.isSipURI())
            {
                console.error(
                    "Received a Subscription Request for destined to a non SIP URI!"
                    + subscribeRequest.toString());
                return subscriptionResponse;
            }

            String subscriber =  ((FromHeader) subscribeRequest.getHeader(FromHeader.NAME)).getAddress().getURI().toString();
            String presentity =  ((ToHeader) subscribeRequest.getHeader(ToHeader.NAME)).getAddress().getURI().toString();

            // Verify that "Event" header is understood
            EventHeader eventHeader = (EventHeader) subscribeRequest.getHeader("Event");
            if (!eventHeader.getEventType().equals("presence")) {
                try {
                    Response bad_event = sipManCallback.messageFactory.
                        createResponse(Response.BAD_EVENT, subscribeRequest);
                    sipManCallback.attachToTag(bad_event, transaction.getDialog());
                    transaction.sendResponse(bad_event);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    console.error("Failed to send bad event message!", ex);
                }
                return Response.BAD_EVENT;
            }

            // Verify that subscription lifetime is not too short
            ExpiresHeader expiresHeader = subscribeRequest.getExpires();
            if (expiresHeader.getExpires() > 0
                && expiresHeader.getExpires() < 3600
                && expiresHeader.getExpires() < minExpires) {
                try {
                    Response tooShort = sipManCallback.messageFactory.createResponse(Response.INTERVAL_TOO_BRIEF, subscribeRequest);
                    MinExpiresHeader minExpiresHeader = sipManCallback.headerFactory.createMinExpiresHeader(minExpires);
                    tooShort.addHeader(minExpiresHeader);
                    sipManCallback.attachToTag(tooShort, transaction.getDialog());
                    transaction.sendResponse(tooShort);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    console.error("Failed to send interval too small message!", ex);
                }
                return Response.INTERVAL_TOO_BRIEF;
            }

            //Are we the one they are looking for?
            String calleeUser = ((SipURI) requestUri).getUser();
            String localUser = sipManCallback.getLocalUser();

            //user info is case sensitive according to rfc3261
            if (!calleeUser.equals(localUser)) {
                sipManCallback.fireCallRejectedLocally(
                    "The user specified by the caller did not match the local user!",
                    subscribeRequest);
                try {
                    Response forbidden = sipManCallback.messageFactory.
                        createResponse(Response.FORBIDDEN, subscribeRequest);
                    sipManCallback.attachToTag(forbidden, transaction.getDialog());
                    transaction.sendResponse(forbidden);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    console.error("Failed to send bad event message!", ex);
                }
                return Response.FORBIDDEN;
            }

            //is this a new subscription or have we already received subscriptions
            //from the same subscriber
            int          sIndex           = getIndexInSubscriptionCache(subscriber, presentity);
            Response     responseMsg      = null;
            byte[]       presenceDocument = null;
            Subscription subscription     = null;

            SubscriptionStateHeader subscriptionStateHeader = null;

            try {
                Scheduler scheduler = Scheduler.getInstance();
                long expires = subscribeRequest.getExpires().getExpires() * 1000;

                if (sIndex != -1) {
                    //Subscription exists, get subscription state
                    subscription = (Subscription) incSubscriptions.elementAt(sIndex);
                    //APPROVED
                    if (subscription.getStatus().equals(subscription.APPROVED)) {
                        responseMsg = sipManCallback.messageFactory.
                            createResponse(Response.OK, subscribeRequest);
                        subscriptionStateHeader = sipManCallback.headerFactory.
                            createSubscriptionStateHeader(
                            SubscriptionStateHeader.ACTIVE);
                        subscriptionResponse = Response.OK;
                    }
                    //PENDING
                    else if (subscription.getStatus().equals(subscription.PENDING)) {
                        responseMsg = sipManCallback.messageFactory.
                            createResponse(Response.ACCEPTED, subscribeRequest);
                        subscriptionStateHeader = sipManCallback.headerFactory.
                            createSubscriptionStateHeader(
                            SubscriptionStateHeader.PENDING);
                        subscriptionResponse = Response.ACCEPTED;
                    }
                    //REJECTED
                    else if (subscription.getStatus().equals(subscription.REJECTED)) {
                        responseMsg = sipManCallback.messageFactory.
                            createResponse(Response.DECLINE, subscribeRequest);
                        subscriptionResponse = Response.DECLINE;
                    }
                    else {
                        //Should not happen
                        subscriptionResponse = Response.ACCEPTED;
                    }

                    //Update subscriptions cache.
                    if (expiresHeader.getExpires() == 0) {
                        //This is a unsubscription
                        removeFromList(subscription);
                        if(console.isDebugEnabled())
                            console.debug("Removing " + subscriber + "/"
                                          + presentity + " from subscription list");
                        subscriptionStateHeader = sipManCallback.headerFactory.
                            createSubscriptionStateHeader(SubscriptionStateHeader.TERMINATED);
                        subscriptionStateHeader.setReasonCode(SubscriptionStateHeader.TIMEOUT);
                    }
                    else {
                        //This is a refresh subscription
                        scheduler.reschedule( (Subscription) incSubscriptions.
                                             elementAt(sIndex),
                                             new Date(System.currentTimeMillis() + expires));
                    }
                }
                else {
                    //Subscription was not found in subsciption cache.
                    responseMsg = sipManCallback.messageFactory.createResponse(
                        Response.ACCEPTED, subscribeRequest);
                    subscriptionStateHeader = sipManCallback.headerFactory.
                        createSubscriptionStateHeader(SubscriptionStateHeader.PENDING);
                    subscriptionResponse = Response.ACCEPTED;
                    ListIterator acceptedCTypes =
                        ((ListIterator)subscribeRequest.getHeaders(AcceptHeader.NAME));

                    //Determine the content type the remote party would prefer to see
                    //in NOTIFY requests.
                    String preferredContentType = suppotedPidFormats.get(0).toString();
                    while(acceptedCTypes.hasNext())
                    {
                        AcceptHeader acceptHeader = (AcceptHeader)acceptedCTypes.next();
                         String contentSubtype = acceptHeader.getContentSubType();

                        if(suppotedPidFormats.contains(contentSubtype))
                        {
                            preferredContentType = contentSubtype;
                            break;
                        }
                    }

                    //Deal with subscription list
                    if (expiresHeader.getExpires() > 0) {

                        // Create a Subscription instance and add it to the subscriptions cache.
                        subscription = new Subscription(
                            subscriber,
                            presentity,
                            transaction.getDialog(),
                            null,
                            Subscription.INCOMING_SUBSCRIPTION,
                            Subscription.PENDING,
                            null, //a null SubscriptionAuthorizationResponse would
                                  //gives bogus presence information later on
                                  //(necessary to send the NOTIFY before obtaining user authorization).
                            preferredContentType,
                            this);
                        incSubscriptions.add(subscription);
                        scheduler.schedule(subscription, new Date(System.currentTimeMillis() + expires));
                        if(console.isDebugEnabled())
                            console.debug("Adding " + subscriber + "/" + presentity
                                          + " to the subscription list");
                    }
                }

                try
                {
                    //the presence document we retrieve here would contain false (concealing)
                    //presence information to send before quering for user authorization.
                    presenceDocument = localPUA.getPresenceInformationData(
                        subscription.
                        getAuthorizationResponse(),
                        subscription.getPresenceDocFormat());
                }
                catch(CommunicationsException exc)
                {
                    sipManCallback.fireCommunicationsError(exc);
                }
            }
            catch (ParseException ex) {
                ex.printStackTrace();
                console.error("Could not create subscription state header!", ex);
                return subscriptionResponse;
            }

            // Send response message
            try {
                sipManCallback.attachToTag(responseMsg, transaction.getDialog());
                transaction.sendResponse(responseMsg);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                console.error("Failed to acknowledge subscribe!", ex);
                return subscriptionResponse;
            }

            if (subscriptionResponse == Response.OK || subscriptionResponse == Response.ACCEPTED) {
                // Send NOTIFY
                try {
                    Request notify = (Request) transaction.getDialog().
                        createRequest(Request.NOTIFY);
                    notify.setHeader(subscriptionStateHeader);
                    sendNotification(notify, transaction.getDialog(),
                                     presenceDocument, subscription.getPresenceDocFormat());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    console.error("Failed to send bogus notify message!", ex);
                    return subscriptionResponse;
                }

                if(subscription.getAuthorizationResponse() == null)
                {
                    //no authorization response means that we got someone new on
                    //the other side and what we just sent was a bogus notify. Let's
                    //ask the user for further details.
                    //request user authorization
                    String address     = extractSubscriberUri(subscribeRequest);
                    String displayName = extractSubscriberUri(subscribeRequest);

                    SubscriptionAuthorizationResponse authResponse =
                        localPUA.requestSubscriptionAuthorization(
                            displayName,
                            address,
                            null,
                            SubscriptionAuthorizationResponse.ACCEPTED_RESPONSES);

                    updateSubscriptionStatus(subscription.getSubscriber(), subscription.getPresentity(), authResponse);

                }
            }

            return subscriptionResponse;
        } finally {
            console.logExit();
        }
    }


    /**
     * Removes a subscription to a presentity from a specified subscriber.
     * @param subscriber the user whose subscription to the presentity is to be removed.
     * @param presentity the user from whom the subscriber's subscription is to be removed.
     * @param reasonCode the reason code for the removal of the subscriber's subscription. Its value may be a null.
     * @throws CommunicationsException
     */
    public void removeSubscription(String subscriber, String presentity, java.lang.String reasonCode)
        throws CommunicationsException
    {
//	   1) Sends a NOTIFY message to the subscriber whose subscription was removed.
//	       The NOTIFY message has a "Subscription-State" header with value "terminated".
//	       An appropriate reason code explaining the termination may also be supplied.
//	       Refer to Section 3.2.4 of RFC3265 for defined reason codes.
//		2) Removes the subscription from the "subscription list".

        try {
            console.logEntry();

            byte[] presenceDocument = null;
            int rank = getIndexInSubscriptionCache(subscriber, presentity);
            if (rank != -1)
            {
                //Subscription is in incoming subscription list
                Subscription sub = (Subscription) incSubscriptions.elementAt(rank);
                //Send an offline presence document
                presenceDocument =  localPUA.getPresenceInformationData(null, sub.getPresenceDocFormat());

                Request notify = null;
                try {
                    notify = (Request)sub.getDialog().createRequest(Request.NOTIFY);
                }
                catch (SipException ex) {
                    console.error(
                        "Could not create notify message!", ex);
                    throw new CommunicationsException(
                        "Could not create notify message!", ex);
                }
                SubscriptionStateHeader subscriptionStateHeader = null;
                try {
                    subscriptionStateHeader = sipManCallback.headerFactory.createSubscriptionStateHeader(SubscriptionStateHeader.TERMINATED);
                    subscriptionStateHeader.setReasonCode(reasonCode);
                }
                catch (ParseException ex) {
                    //Shouldn't happen
                    console.error(
                        "Null is not an allowed tag for the subscription state header!", ex);
                    throw new CommunicationsException(
                        "Null is not an allowed tag for the subscription state header!", ex);
                }
                notify.addHeader(subscriptionStateHeader);
                sendNotification(notify, ((Subscription) incSubscriptions.elementAt(rank)).getDialog(), presenceDocument, sub.getPresenceDocFormat());

                //At least, remove the subscription
                incSubscriptions.remove(rank);
                if(console.isDebugEnabled())
                            console.debug("Removing "+subscriber+"/"+presentity
                                          + " from subscription list");
            }
        } finally {
            console.logExit();
        }

    }

    /**
     * Removes all subscriptions.
     * @param reasonCode the reason code for the removal of the subscriber's subscription. Its value may be a null.
     * @throws CommunicationsException
     * @throws java.lang.IllegalArgumentException
     */
    public void removeAllSubscriptions(java.lang.String reasonCode)
        throws CommunicationsException, java.lang.IllegalArgumentException
    {
        try
        {
            console.logEntry();
            for (int i=0; i<incSubscriptions.size(); i++) {
                Subscription sub = (Subscription) incSubscriptions.firstElement();
                removeSubscription(sub.getSubscriber(), sub.getPresentity(), reasonCode);
            }

        } finally {
            console.logExit();
        }
    }


    /**
     * Sends a NOTIFY message to a subscriber.
     * @param notification NOTIFY message to be sent to the subscribers.
     * @param dialog the dialog used to send this message
     * @param presenceDocument the presence document
     * @param presenceFormat the format in which the presence document is encoded
     * @return transaction ID associated with the NOTIFY message sent by this method.
     * @throws CommunicationsException
     * @throws java.lang.IllegalArgumentException
     */
    public java.lang.String sendNotification(Request notification, Dialog dialog, byte[] presenceDocument, String presenceFormat)
        throws CommunicationsException, java.lang.IllegalArgumentException
    {
       try
       {
            console.logEntry();

            String response = null;
//			Transaction
            ClientTransaction notTrans = null;
            try {
                ContentTypeHeader contentTypeHeader = sipManCallback.headerFactory.createContentTypeHeader("application", presenceFormat);
                ContentLengthHeader contentLengthHeader = sipManCallback.headerFactory.createContentLengthHeader(presenceDocument.length);

                //Set event header to presence
                EventHeader eventHeader = sipManCallback.headerFactory.createEventHeader("presence");

                //Increment CSeq
                //CSeqHeader cseqHeader = ((CSeqHeader)notification.getHeader(CSeqHeader.NAME));
                //int cseq =  dialog.getLocalSequenceNumber() + 1;
                //cseqHeader.setSequenceNumber(cseq);

                //UserAgentHeader userAgentHeader = sipManCallback.headerFactory.createUserAgentHeader("SIP-Communicator");
                notification.setContent(presenceDocument, contentTypeHeader);
                notification.setContentLength(contentLengthHeader);
                notification.setHeader(eventHeader);
                //notification.setHeader(cseqHeader);

                notTrans = sipManCallback.sipProvider.getNewClientTransaction(notification);
            }
            catch (ParseException ex) {
                ex.printStackTrace();
                console.error("Header must not contain a null value!",
                            ex);
                throw new CommunicationsException(
                        "Header must not contain a null value!");
            }
            catch (InvalidArgumentException ex) {
                ex.printStackTrace();
                console.error("Content Length Header must contain an integer value!",
                            ex);
                throw new CommunicationsException(
                        "Content Length Header must contain an integer value!");
            }
            catch (TransactionUnavailableException ex) {
                ex.printStackTrace();
                console.error("Could not create notify message!",
                            ex);
                //throw was missing - reported by Eero Vaarnas
                throw new CommunicationsException(
                        "Could not create notify message!");
            }
                
            try {
                dialog.sendRequest(notTrans);
                //notTrans.sendRequest();
                if( console.isDebugEnabled() )
                    console.debug("sent request= " + notification);
            }
            //we sometimes get a null pointer exception here so catch them all
            catch (Exception ex) {
                console.error("Could not send out the notify request!", ex);
                //throw was missing - reported by Eero Vaarnas
                throw new CommunicationsException(
                    "Could not send out the notify request!", ex);
            }
            return response;
        } finally {
            console.logExit();
        }
    }

    /**
     * Notify all approved subscriptions of our presence status
     * @throws CommunicationsException
     */
    public void notifyAllSubscribers()
        throws CommunicationsException
    {
        try
        {
            console.logEntry();
            Subscription sub = null;
            Request notify = null;
            byte[] presenceDocument = null;

            for (int i=0; i<incSubscriptions.size(); i++) {
                sub = (Subscription) incSubscriptions.elementAt(i);
                //We send a Notify message only when the subscription is approved
                if (sub.getStatus().equals(sub.APPROVED)) {
                    presenceDocument =  localPUA.getPresenceInformationData(sub.getAuthorizationResponse(), sub.getPresenceDocFormat());
                    try {
                        notify = (Request)sub.getDialog().createRequest(Request.NOTIFY);
                    }
                    catch (SipException ex) {
                        console.error(
                            "Could not create notify message!", ex);
                        throw new CommunicationsException(
                            "Could not create notify message!", ex);
                    }
                    sendNotification(notify, sub.getDialog(), presenceDocument, sub.getPresenceDocFormat());
                }
            }

        } finally {
            console.logExit();
        }
    }

    public void removeFromList (Subscription subscription)
    {
        incSubscriptions.remove(subscription);
    }

    /**
     * Search for the position of the subscription identified by the couple subscriber/presentity
     * @param subscriber
     * @param presentity
     * @return the position of the subscription in the Vector, and -1 if the couple is not found
     */
    public int getIndexInSubscriptionCache(String subscriber, String presentity)
    {
        for (int i=0; i<incSubscriptions.size(); i++)
        {
            if (((Subscription) incSubscriptions.elementAt(i)).equals(subscriber, presentity))
                return i;
        }
        return -1;
    }

    /**
     * Is there a subscription identified by the couple subscriber/presentity ?
     * @param subscriber
     * @param presentity
     * @return boolean
     */
    public boolean existSubscription(String subscriber, String presentity)
    {
        return (getIndexInSubscriptionCache(subscriber, presentity) != -1);
    }

    /**
     * Update a subscription state and send a Notify message according to the new state
     * @param subscriber
     * @param presentity
     * @param newSubscriptionStatus
     * @throws CommunicationsException
     */
    public void updateSubscriptionStatus(String subscriber, String presentity, SubscriptionAuthorizationResponse subscriptionAuthorization)
    {
        try {
            console.logEntry();

            byte[] presenceDocument = null;
            SubscriptionStateHeader subscriptionStateHeader = null;
            int rank = getIndexInSubscriptionCache(subscriber, presentity);

            if (rank != -1) {
                //Subscription is in incoming subscription list
                Subscription sub = (Subscription) incSubscriptions.elementAt(rank);
                //Change subscription state
                try {
                    if (subscriptionAuthorization.getResponseCode().equals(SubscriptionAuthorizationResponse.AUTHORISATION_GRANTED)) {
                        sub.setStatus(Subscription.APPROVED);
                        subscriptionStateHeader = sipManCallback.headerFactory.createSubscriptionStateHeader(SubscriptionStateHeader.ACTIVE);
                    }
                    else if (subscriptionAuthorization.getResponseCode().equals(SubscriptionAuthorizationResponse.AUTHORISATION_REFUSED)) {
                        sub.setStatus(Subscription.REJECTED);
                        subscriptionStateHeader = sipManCallback.headerFactory.createSubscriptionStateHeader(SubscriptionStateHeader.TERMINATED);
                    }
                    else {
                        //Don't know how to handle this case so keep it pending
                        sub.setStatus(Subscription.PENDING);
                        subscriptionStateHeader = sipManCallback.headerFactory.createSubscriptionStateHeader(SubscriptionStateHeader.PENDING);
                    }
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    console.error(
                        "Null is not an allowed tag for the subscription state header!", ex);
                    sipManCallback.fireCommunicationsError( new CommunicationsException(
                        "Null is not an allowed tag for the subscription state header!", ex));
                }
                sub.setAuthorizationResponse(subscriptionAuthorization);
                try {
                    presenceDocument = localPUA.getPresenceInformationData(
                        subscriptionAuthorization, sub.getPresenceDocFormat());
                }
                catch (CommunicationsException ex) {
                    sipManCallback.fireCommunicationsError(ex);
                }
                //Send a notify to the subscriber whose subscription state changed
                try {
                    Request notify = (Request) sub.getDialog().createRequest(Request.NOTIFY);
                    notify.setHeader(subscriptionStateHeader);
                    sendNotification(notify, sub.getDialog(), presenceDocument, sub.getPresenceDocFormat());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    console.error("Failed to send notify message!", ex);
                }
            }
        } finally {
            console.logExit();
        }
    }

    public void setLocalPUA(LocalPresenceUserAgent lpua)
    {
        this.localPUA = lpua;

        //initialize supported PID formats;
        Enumeration supportedPidf = lpua.getSupportedPresenceInformationDataFormats();
        while (supportedPidf.hasMoreElements())
        {
            suppotedPidFormats.add(supportedPidf.nextElement());
        }

    }

    private String extractSubscriberUri(Request subscribeRequest)
    {
        FromHeader fromHeader = (FromHeader) subscribeRequest.getHeader(
                    FromHeader.NAME);
        return fromHeader.getAddress().getURI().toString();
    }

    private String extractSubscriberName(Request subscribeRequest)
    {
        FromHeader fromHeader = (FromHeader)subscribeRequest.getHeader(FromHeader.NAME);
        Address address       = fromHeader.getAddress();
        String retVal         = null;

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
}

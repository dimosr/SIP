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

import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.header.CSeqHeader;
import javax.sip.message.Request;

import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.common.SchedulerTask;
import net.java.sip.communicator.sip.CommunicationsException;

/**
 * @author Martin Andre
 */
class Subscription
    extends SchedulerTask
{
    //Date expires;
    private long delay;
    private Dialog dialog;
    private Request request;
    private String subscriptionType;	/* Incoming or Outgoing */
    private String subscriber;
    private String presentity;
    private String status;
    private SubscriptionAuthorizationResponse authorizationResponse;
    private String presenceDocFormat;
    protected static final Console console = Console.getConsole(Subscription.class);
    protected Object callback = null;

    static final String INCOMING_SUBSCRIPTION = "Incoming";
    static final String OUTGOING_SUBSCRIPTION = "Outgoing";

    static final String APPROVED = "approved";
    static final String PENDING = "pending";
    static final String REJECTED = "rejected";


    Subscription(
        String subscriber,
        String presentity,
        Dialog dialog,
        Request request,
        String subscriptionType,
        String status,
        SubscriptionAuthorizationResponse authorizationResponse,
        String presenceDocFormat,
        Object callback)
    {
        this.subscriber = subscriber;
        this.presentity = presentity;
        this.dialog = dialog;
        this.request = request;
        this.subscriptionType = subscriptionType;
        this.status = status;
        this.authorizationResponse = authorizationResponse;
        this.presenceDocFormat = presenceDocFormat;
        this.callback = callback;
    }

    /**
     * Action preformed by subscription when triggered
     * 2 cases here :
     * - Incoming Subscription : remove this subscription from incoming subscriptions list
     * - Outgoing Subscription : refresh the subscription sending a new SUBSCRIBE message
     */
    public void run()
    {
        try
        {
            console.logEntry();

            //PA, remove from subscriptionList
            if (subscriptionType.equalsIgnoreCase(INCOMING_SUBSCRIPTION)) {
                ((PresenceAgent) callback).removeFromList(this);
                if(console.isDebugEnabled())
                        console.debug("Processing INCOMING_SUBSCRIPTION trigger");
            }

            //Watcher, resend a subscribe msg
            else if  (subscriptionType.equalsIgnoreCase(OUTGOING_SUBSCRIPTION)) {
                try {
                    request = (Request)request.clone();
                    
                    try {
                        //Increment CSeq
                        CSeqHeader cseqHeader = ((CSeqHeader)request.getHeader(CSeqHeader.NAME));
                        int cseq =  cseqHeader.getSequenceNumber() + 1;
                        cseqHeader.setSequenceNumber(cseq);
                    }
                    catch (InvalidArgumentException ex) {
                        ex.printStackTrace();
                        console.error("Cseq Header must contain a integerl value!",
                                    ex);
                        throw new CommunicationsException(
                                "Cseq Header must contain a integerl value!");
                    }
                    
                    ((Watcher) callback).sendSubscription(request);
                    if(console.isDebugEnabled())
                            console.debug("Processing OUTGOING_SUBSCRIPTION trigger");
                }
                catch (CommunicationsException ex) {
                    ex.printStackTrace();
                    console.error("Unable to send SUBSCRIBE request !", 
                                ex);
                }
            }
            else {
                if(console.isDebugEnabled())
                            console.debug("Tried to process an unknown subscription type trigger : " 
                                        + subscriptionType);
            }
        } finally {
            console.logExit();
        }
    }

    /**
     * Test if the couple subscriber/presentity is the same as the one in
     * the current object. This couple can be considered as the subscription
     * unique identifier.
     * 
     * @param subscriber the subscriber's address
     * @param presentity the presentity's address
     * @return true if equal, false else
     */
    public boolean equals(String subscriber, String presentity)
    {
        return (this.subscriber.equals(subscriber) && this.presentity.equals(presentity));
    }


    /**
     * @return
     */
    public SubscriptionAuthorizationResponse getAuthorizationResponse()
    {
        return authorizationResponse;
    }

    /**
     * @return
     */
    public Dialog getDialog()
    {
        return dialog;
    }

    /**
     * @return
     */
    public String getPresenceDocFormat()
    {
        return presenceDocFormat;
    }

    /**
     * @return
     */
    public String getPresentity()
    {
        return presentity;
    }

    /**
     * @return
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * @return
     */
    public String getSubscriber()
    {
        return subscriber;
    }

    /**
     * @param response
     */
    public void setAuthorizationResponse(SubscriptionAuthorizationResponse response)
    {
        authorizationResponse = response;
    }

    /**
     * @param string
     */
    public void setStatus(String string)
    {
        status = string;
    }
    
    /**
     * @return
     */
    public Request getRequest()
    {
        return request;
    }

    public String toString()
    {
        String sub = "subscriber = " + subscriber +"\n" +
                            "presentity = " + presentity +"\n" +
                            "dialog = " + dialog +"\n" +
                            "subscriptionType = " + subscriptionType +"\n" +
                            "status = " + status +"\n" +
                            "authorizationResponse = " + authorizationResponse +"\n" +
                            "presenceDocFormat = " + presenceDocFormat ;
        
        return sub;
    }



}

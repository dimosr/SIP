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

import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.*;

import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.common.Utils;
import net.java.sip.communicator.sip.CommunicationsException;
import net.java.sip.communicator.sip.SipManager;
import net.java.sip.communicator.sip.security.SipSecurityException;

/**
 * <p>Title: SIP COMMUNICATOR-1.1</p>
 * <p>Description: JAIN-SIP-1.1 Audio/Video Phone Application</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Organisation: LSIIT Laboratory (http://lsiit.u-strasbg.fr)<p>
 * </p>Network Research Team (http://www-r2.u-strasbg.fr))<p>
 * </p>Louis Pasteur University - Strasbourg - France</p>
 * @author Emil Ivov
 * @version 1.1
 */
public class MessageProcessing
{
    protected static final Console console = Console.getConsole(MessageProcessing.class);
    protected SipManager sipManCallback = null;

    public MessageProcessing()
    {
        try {
            console.logEntry();
        }
        finally {
            console.logExit();
        }
    }

    public MessageProcessing(SipManager sipManCallback)
    {
        this.sipManCallback = sipManCallback;
    }

    public void setSipManagerCallBack(SipManager sipManCallback)
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
    void processAuthenticationChallenge(ClientTransaction clientTransaction,
                                        Response response)
    {
        try {
            console.logEntry();

            Request challengedRequest = clientTransaction.getRequest();
            Request reoriginatedRequest = null;

            ClientTransaction retryTran = sipManCallback.sipSecurityManager.
                    handleChallenge(response, clientTransaction.getBranchId(),
                                challengedRequest);

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
        finally {
            console.logExit();
        }
    }
    
    
    /**
     * Process MESSAGE requests and send OK response.
     * 
     * @param serverTransaction
     * @param request
     */
    public void processMessageRequest(ServerTransaction serverTransaction, Request request)
    {
        try {
            console.logEntry();

            //Send OK
            Response ok = null;
            try {
                ok = sipManCallback.messageFactory.
                    createResponse(Response.OK, request);
                //sipManCallback.attachToTag(ok, serverTransaction.getDialog());
            }
            catch (ParseException ex) {
                sipManCallback.fireCommunicationsError(
                    new CommunicationsException(
                    "Failed to construct an OK response to a MESSAGE request!",
                    ex)
                    );
                return;
            }
            try {
                serverTransaction.sendResponse(ok);
                if( console.isDebugEnabled() )
                    console.debug("sent response " + ok);
            }
            catch (SipException ex) {
                //This is not really a problem according to the RFC
                //so just dump to stdout should someone be interested
                console.error("Failed to send an OK response to MESSAGE request,"
                              + "exception was:\n",
                              ex);
            }
        }
        finally {
            console.logExit();
        }
    }
 
 
    /**
     * Sends an instant message in pager-mode using a SIMPLE/SIP MESSAGE request. In
     * pager-mode, each message is independent of any other messages.
     * An instant message will be the body of the MESSAGE request to be sent, therefore,
     * its format must conform to the values in the "Content-Type" and "Content-Encoding"
     * header fields. Refer to Message for details.
     * 
     * @param to the address of receiver.
     * @param messageBody the message to be sent. The messageBody will be the body of 
     * the MESSAGE request to be sent and its format must conform to the values in the parameters
     * contentType and contentEncoding. Please refer to the setBody method for details.
     * @param contentType the Internet media type of the messageBody. Please refer to the
     * Message.setBody method for details.
     * @param contentEncoding the encodings that have been applied to the messageBody in
     * addition to those specified by contentType. Please refer to the Message.setBody method
     * for details. 
     * @return the transaction ID associated with the MESSAGE request sent by this method. 
     * @throws CommunicationsException
     */
    public java.lang.String sendMessage(java.lang.String to,
                                        byte[] messageBody,
                                        java.lang.String contentType,
                                        java.lang.String contentEncoding)
                                 throws CommunicationsException
    {
        try
        {
            console.logEntry();

            to = to.trim();
            //Handle default domain name (i.e. transform 1234 -> 1234@sip.com
            String defaultDomainName =
                Utils.getProperty("net.java.sip.communicator.sip.DEFAULT_DOMAIN_NAME");
            if (defaultDomainName != null //no sip scheme
                && !to.trim().startsWith("tel:")
                && to.indexOf('@') == -1 //most probably a sip uri
                ) {
                    to = to + "@" + defaultDomainName;
            }

            //Let's be uri fault tolerant
            if (to.toLowerCase().indexOf("sip:") == -1 //no sip scheme
                && to.indexOf('@') != -1 //most probably a sip uri
                ) {
                    to = "sip:" + to;
            }

            //Request URI
            URI requestURI;
            try {
                requestURI = sipManCallback.addressFactory.createURI(to);
            }
            catch (ParseException ex) {
                console.error(to + " is not a legal SIP uri!", ex);
                throw new CommunicationsException(to +
                                                  " is not a legal SIP uri!", ex);
            }
            //Call ID
            CallIdHeader callIdHeader = sipManCallback.sipProvider.getNewCallId();
            //CSeq
            CSeqHeader cSeqHeader;
            try {
                cSeqHeader = sipManCallback.headerFactory.createCSeqHeader(1,
                    Request.MESSAGE);
            }
            catch (Exception ex) {
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
            
            ContentTypeHeader contentTypeHeader = null;
            try {
                String[] contentTypeTab = contentType.split("/");
                contentTypeHeader = sipManCallback.headerFactory.createContentTypeHeader(contentTypeTab[0], contentTypeTab[1]);
            }
            catch (ParseException ex) {
                console.error(
                    "ContentType Header must look like type/subtype!", ex);
                throw new CommunicationsException(
                    "ContentType Header must look like type/subtype!", ex);
            }
            
            ContentLengthHeader contentLengthHeader = null;
            try {
                contentLengthHeader = sipManCallback.headerFactory.createContentLengthHeader(messageBody.length);
            }
            catch (InvalidArgumentException ex) {
                console.error(
                    "Cseq Header must contain a integer value!", ex);
                throw new CommunicationsException(
                    "Cseq Header must contain a integer value!", ex);
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

//            ExpiresHeader expiresHeader = null;
//            try {
//                expiresHeader = sipManCallback.headerFactory.createExpiresHeader(expiresSubscription);
//            }
//            catch (InvalidArgumentException ex) {
//                console.error(
//                    "Expires Header must be an integer!", ex);
//                throw new CommunicationsException(
//                    "Expires Header must be an integer!", ex);
//            }

            //ViaHeaders
            ArrayList viaHeaders = sipManCallback.getLocalViaHeaders();
            //MaxForwards
            MaxForwardsHeader maxForwards = sipManCallback.getMaxForwardsHeader();
            Request message = null;
            try {
                message = sipManCallback.messageFactory.createRequest(requestURI,
                    Request.MESSAGE,
                    callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders,
                    maxForwards);
                message.setContent(messageBody, contentTypeHeader);
                message.setContentLength(contentLengthHeader);
                message.addHeader(eventHeader);
            }
            catch (ParseException ex) {
                console.error(
                    "Failed to create message Request!", ex);
                throw new CommunicationsException(
                    "Failed to create message Request!", ex);
            }

            ClientTransaction messageTransaction = null;
            String subscriber = sipManCallback.getFromHeader().getAddress().getURI().toString();
            
            try {
                messageTransaction = sipManCallback.sipProvider.
                    getNewClientTransaction(message);
            }
            catch (TransactionUnavailableException ex) {
                console.error(
                    "Failed to create messageTransaction.", ex);
                throw new CommunicationsException(
                    "Failed to create messageTransaction.", ex);
            }

            try {
                messageTransaction.sendRequest();

                if( console.isDebugEnabled() )
                    console.debug("sent request: " + message); 
            }
            catch (SipException ex) {
                console.error(
                    "An error occurred while sending message request", ex);
                throw new CommunicationsException(
                    "An error occurred while sending message request", ex);
            }
                
            return messageTransaction.toString();

        } finally {
            console.logExit();
        }
    }

}

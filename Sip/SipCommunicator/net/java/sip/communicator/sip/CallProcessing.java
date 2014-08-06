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

import java.text.*;
import java.util.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import net.java.sip.communicator.common.*;
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
public class CallProcessing
{
    protected static final Console console = Console.getConsole(CallProcessing.class);
    protected SipManager sipManCallback = null;
    protected CallDispatcher callDispatcher = new CallDispatcher();
    CallProcessing()
    {
        try {
            console.logEntry();
        }
        finally {
            console.logExit();
        }
    }

    CallProcessing(SipManager sipManCallback)
    {
        this.sipManCallback = sipManCallback;
    }

    void setSipManagerCallBack(SipManager sipManCallback)
    {
        this.sipManCallback = sipManCallback;
    }

//============================= Remotely Initiated Processing ===================================
    //----------------------------- Responses
    void processTrying(ClientTransaction clientTransaction, Response response)
    {
        try {
            console.logEntry();

            //find the call
            Call call = callDispatcher.findCall(clientTransaction.
                                                getDialog());
            if (call == null) {
                sipManCallback.fireUnknownMessageReceived(response);
                return;
            }
            //change status
            if (!call.getState().equals(Call.MOVING_LOCALLY))
                call.setState(Call.DIALING);
        }
        finally {
            console.logExit();
        }
    }

    void processRinging(ClientTransaction clientTransaction, Response response)
    {
        try
        {
            console.logEntry();

            //find the call
            Call call = callDispatcher.findCall(clientTransaction.
                                                getDialog());
            if (call == null) {
                //?maybe we should just ignore it?
                sipManCallback.fireUnknownMessageReceived(response);
                return;
            }
            //change status
            call.setState(Call.RINGING);
        }
        finally
        {
            console.logExit();
        }
    }

    /**
     * According to the RFC  a
     * UAC canceling a request cannot rely on receiving a 487 (Request
     * Terminated) response for the original request, as an RFC 2543-
     * compliant UAS will not generate such a response. So we are closing the
     * call when sending the cancel request and here we don't do anything.
     * @param clientTransaction
     * @param response
     */
    void processRequestTerminated(ClientTransaction clientTransaction,
                                  Response response)
    {
        try
        {
            console.logEntry();

            //add any additional code here
        }
        finally
        {
            console.logExit();
        }
    }

    void processByeOK(ClientTransaction clientTransaction, Response response)
    {
        try {
            console.logEntry();

            //add any additional code here
        }
        finally {
            console.logExit();
        }
    }

    void processCancelOK(ClientTransaction clientTransaction, Response response)
    {
        try
        {
            console.logEntry();

            //add any additional code here
        }
        finally
        {
            console.logExit();
        }
    }

    void processInviteOK(ClientTransaction clientTransaction, Response ok)
    {
        try
        {
            console.logEntry();

            //find the call
            Call call = callDispatcher.findCall(clientTransaction.
                                                getDialog());
            if (call == null) {
                sipManCallback.fireUnknownMessageReceived(ok);
                return;
            }
            //Send ACK
            try {
                //Need to use dialog generated ACKs so that the remote UA core
                //sees them - Fixed by M.Ranganathan
                Request ack = (Request) clientTransaction.getDialog().
                    createRequest(Request.ACK);
                clientTransaction.getDialog().sendAck(ack);
            }
            catch (SipException ex) {
                console.error("Failed to acknowledge call!", ex);
                call.setState(Call.DISCONNECTED);
                sipManCallback.fireCommunicationsError(
                    new CommunicationsException(
                    "Failed to acknowledge call!"
                    , ex)
                    );
                return;
            }
            // !!! set sdp content before setting call state as that is where
            //listeners get alerted and they need the sdp
            call.setRemoteSdpDescription(new String(ok.getRawContent()));
            //change status
            if (!call.getState().equals(Call.CONNECTED)) {
                call.setState(Call.CONNECTED);
            }
        }
        finally
        {
            console.logExit();
        }

    }

    void processBusyHere(ClientTransaction clientTransaction, Response busyHere)
    {
        try
        {
            console.logEntry();

            //find the call
            Call call = callDispatcher.findCall(clientTransaction.
                                                getDialog());
            if (call == null) {
                sipManCallback.fireUnknownMessageReceived(busyHere);
                return;
            }
            //change status
            call.setState(Call.BUSY);
            //it is the stack that should be sending the ACK so don't do it here
        }
        finally
        {
            console.logExit();
        }


    }

    void processCallError(ClientTransaction clientTransaction, Response notAcceptable)
    {
        try
        {
            console.logEntry();

            if(console.isDebugEnabled())
            {
                console.debug("Processing CALL ERROR response:\n" + notAcceptable);
            }
            //find the call
            Call call = callDispatcher.findCall(clientTransaction.
                                                getDialog());
            if (call == null) {
                sipManCallback.fireUnknownMessageReceived(notAcceptable);
                return;
            }
            //change status
            call.setState(Call.FAILED);
                sipManCallback.fireCommunicationsError(
                            new CommunicationsException(
                            "Remote party returned error response: " + notAcceptable.getStatusCode()
                            +" - " + notAcceptable.getReasonPhrase()
                        )
                    );
                return;

            //it is the stack that should be sending the ACK so don't do it here

        }
        finally
        {
            console.logExit();
        }


    }


    /**
     * Attempts to re-ogenerate the corresponding request with the proper
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

//            Dialog dialog = clientTransaction.getDialog();


            //dialog.sendRequest(retryTran);
            retryTran.sendRequest();
        }
        catch (SipSecurityException exc) {
            sipManCallback.fireCommunicationsError(
                new CommunicationsException("Authorization failed!", exc));
        }
//        catch(){}
        catch (Exception exc) {
            sipManCallback.fireCommunicationsError(
                new CommunicationsException("Failed to resend a request "
                                            + "after a security challenge!",
                                            exc)
                );
        }
        finally {
            callDispatcher.findCall(clientTransaction.getDialog()).
                setState(Call.FAILED);
            console.logExit();
        }
    }

    //-------------------------- Requests ---------------------------------
    void processInvite(ServerTransaction serverTransaction, Request invite)
    {
        try {
            console.logEntry();

            Dialog dialog = serverTransaction.getDialog();
            Call call = callDispatcher.createCall(dialog, invite);
            sipManCallback.fireCallReceived(call);
            //change status
            call.setState(Call.ALERTING);
            //sdp description may be in acks - bug report Laurent Michel
            ContentLengthHeader cl = invite.getContentLength();
            if (cl != null
                && cl.getContentLength() > 0) {
                call.setRemoteSdpDescription(new String(invite.getRawContent()));
            }
            //Are we the one they are looking for?
            URI calleeURI = ( (ToHeader) invite.getHeader(ToHeader.NAME)).
                getAddress().getURI();
            /** @todo We shoud rather ask the user what to do here as some
               would add prefixes or change user URIs*/
            if (calleeURI.isSipURI()) {
                String calleeUser = ( (SipURI) calleeURI).getUser();
                String localUser = sipManCallback.getLocalUser();
                boolean assertUserMatch = Boolean.valueOf(Utils.getProperty("net.java.sip.communicator.sip.FAIL_CALLS_ON_DEST_USER_MISMATCH")).booleanValue();
                //user info is case sensitive according to rfc3261
                if (!calleeUser.equals(localUser) && assertUserMatch)
                {
                    sipManCallback.fireCallRejectedLocally(
                        "The user specified by the caller did not match the local user!",
                        invite
                        );
                    call.setState(Call.DISCONNECTED);
                    Response notFound = null;
                    try {
                        notFound = sipManCallback.messageFactory.createResponse(
                            Response.NOT_FOUND,
                            invite
                            );
                        sipManCallback.attachToTag(notFound, dialog);
                    }
                    catch (ParseException ex) {
                        call.setState(Call.DISCONNECTED);
                        sipManCallback.fireCommunicationsError(
                            new CommunicationsException(
                            "Failed to create a NOT_FOUND response to an INVITE request!"
                            , ex)
                            );
                        return;
                    }
                    try {
                        serverTransaction.sendResponse(notFound);
                        if( console.isDebugEnabled() )
                            console.debug("sent a not found response: " + notFound);
                    }
                    catch (SipException ex) {
                        call.setState(Call.DISCONNECTED);
                        sipManCallback.fireCommunicationsError(
                            new CommunicationsException(
                            "Failed to send a NOT_FOUND response to an INVITE request!"
                            , ex)
                            );
                        return;
                    }
                    return;
                }
            }

            //Send RINGING
            Response ringing = null;
            try {
                ringing = sipManCallback.messageFactory.createResponse(
                    Response.RINGING,
                    invite
                    );
                sipManCallback.attachToTag(ringing, dialog);
            }
            catch (ParseException ex) {
                call.setState(Call.DISCONNECTED);
                sipManCallback.fireCommunicationsError(
                    new CommunicationsException(
                    "Failed to create a RINGING response to an INVITE request!"
                    , ex)
                    );
                return;
            }
            try {
                serverTransaction.sendResponse(ringing);
                if( console.isDebugEnabled() )
                    console.debug("sent a ringing response: " + ringing);
            }
            catch (SipException ex) {
                call.setState(Call.DISCONNECTED);
                sipManCallback.fireCommunicationsError(
                    new CommunicationsException(
                    "Failed to send a RINGING response to an INVITE request!"
                    , ex)
                    );
                return;
            }
        }
        finally
        {
            console.logExit();
        }

    }

    void processTimeout(Transaction transaction, Request request)
    {
        try
        {
            console.logEntry();

            Call call = callDispatcher.findCall(transaction.getDialog());
            if (call == null) {
                return;
            }
            sipManCallback.fireCommunicationsError(
                new CommunicationsException("The remote party has not replied!"
                                            + "The call will be disconnected")
                );
            //change status
            call.setState(Call.DISCONNECTED);
        }
        finally
        {
            console.logExit();
        }
    }

    void processBye(ServerTransaction serverTransaction, Request byeRequest)
    {
        try
        {
            console.logEntry();

            //find the call
            Call call = callDispatcher.findCall(serverTransaction.
                                                getDialog());
            if (call == null) {
                sipManCallback.fireUnknownMessageReceived(byeRequest);
                return;
            }
            //change status
            call.setState(Call.DISCONNECTED);
            //Send OK
            Response ok = null;
            try {
                ok = sipManCallback.messageFactory.
                    createResponse(Response.OK, byeRequest);
                sipManCallback.attachToTag(ok, call.getDialog());
            }
            catch (ParseException ex) {
                sipManCallback.fireCommunicationsError(
                    new CommunicationsException(
                    "Failed to construct an OK response to a BYE request!",
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
                console.error("Failed to send an OK response to BYE request,"
                              + "exception was:\n",
                              ex);
            }
        }
        finally
        {
            console.logExit();
        }

    }

    void processAck(ServerTransaction serverTransaction, Request ackRequest)
    {
        try
        {
            console.logEntry();

            if (!serverTransaction.getDialog().getFirstTransaction().getRequest().
                getMethod().equals(Request.INVITE)) {
                console.debug("ignored ack");
                return;
            }
            //find the call
            Call call = callDispatcher.findCall(serverTransaction.
                                                getDialog());
            if (call == null) {
                //this is most probably the ack for a killed call - don't signal it
                //sipManCallback.fireUnknownMessageReceived(ackRequest);
                console.debug("didn't find an ack's call, returning");
                return;
            }
            ContentLengthHeader cl = ackRequest.getContentLength();
            if (cl != null
                && cl.getContentLength() > 0)
            {
                call.setRemoteSdpDescription(new String(ackRequest.getRawContent()));
            }
            //change status
            call.setState(Call.CONNECTED);
        }
        finally
        {
            console.logExit();
        }

    }

    void processCancel(ServerTransaction serverTransaction,
                       Request cancelRequest)
    {
        try
        {
            console.logEntry();

            if (!serverTransaction.getDialog().getFirstTransaction().getRequest().
                getMethod().equals(Request.INVITE)) {
                //For someone else
                console.debug("ignoring request");
                return;
            }
            //find the call
            Call call = callDispatcher.findCall(serverTransaction.
                                                getDialog());
            if (call == null) {
                sipManCallback.fireUnknownMessageReceived(cancelRequest);
                return;
            }
            //change status
            call.setState(Call.DISCONNECTED);
            // Cancels should be OK-ed and the initial transaction - terminated
            // (report and fix by Ranga)
            try {
                Response ok = sipManCallback.messageFactory.createResponse(Response.
                    OK, cancelRequest);
                sipManCallback.attachToTag(ok, call.getDialog());
                serverTransaction.sendResponse(ok);
                if( console.isDebugEnabled() )
                    console.debug("sent ok response: " + ok);
            }
            catch (ParseException ex) {
                sipManCallback.fireCommunicationsError(
                    new CommunicationsException(
                    "Failed to create an OK Response to an CANCEL request.", ex));
            }
            catch (SipException ex) {
                sipManCallback.fireCommunicationsError(
                    new CommunicationsException(
                    "Failed to send an OK Response to an CANCEL request.", ex));
            }
            try {
                //stop the invite transaction as well
                Transaction tran = call.getDialog().getFirstTransaction();
                //should be server transaction and misplaced cancels should be
                //filtered by the stack but it doesn't hurt checking anyway
                if (! (tran instanceof ServerTransaction)) {
                    sipManCallback.fireCommunicationsError(
                        new CommunicationsException(
                        "Received a misplaced CANCEL request!"));
                    return;
                }
                ServerTransaction inviteTran = (ServerTransaction) tran;
                Request invite = call.getDialog().getFirstTransaction().getRequest();
                Response requestTerminated =
                    sipManCallback.
                    messageFactory.
                    createResponse(Response.REQUEST_TERMINATED, invite);
                sipManCallback.attachToTag(requestTerminated, call.getDialog());
                inviteTran.sendResponse(requestTerminated);
                if( console.isDebugEnabled() )
                    console.debug("sent request terminated response: "
                                  + requestTerminated);
            }
            catch (ParseException ex) {
                sipManCallback.fireCommunicationsError(
                    new CommunicationsException(
                    "Failed to create a REQUEST_TERMINATED Response to an INVITE request.",
                    ex));
            }
            catch (SipException ex) {
                sipManCallback.fireCommunicationsError(
                    new CommunicationsException(
                    "Failed to send an REQUEST_TERMINATED Response to an INVITE request.",
                    ex));
            }
        }
        finally
        {
            console.logExit();
        }

    }

    // ----------------- Responses --------------------------
    //NOT FOUND
    void processNotFound(ClientTransaction clientTransaction, Response response)
    {
        try
        {
            console.logEntry();

            if (!clientTransaction.getDialog().getFirstTransaction().getRequest().
                getMethod().equals(Request.INVITE)) {
                //Not for us
                console.debug("ignoring not found response");
                return;
            }
            //find the call
            Call call = callDispatcher.findCall(clientTransaction.
                                                getDialog());
            call.setState(Call.DISCONNECTED);
            sipManCallback.fireCallRejectedRemotely(
                "Server returned a NOT FOUND Response",
                response
                );
        }
        finally
        {
            console.logExit();
        }

    }

    void processNotImplemented(ClientTransaction clientTransaction,
                               Response response)
    {
        try
        {
            console.logEntry();

            if (!clientTransaction.getDialog().getFirstTransaction().getRequest().
                getMethod().equals(Request.INVITE)) {
                //Not for us
                console.debug("ignoring not implemented response");
                return;
            }
            //find the call
            Call call = callDispatcher.findCall(clientTransaction.
                                                getDialog());
            call.setState(Call.DISCONNECTED);
            sipManCallback.fireCallRejectedRemotely(
                "Server returned a NOT IMPLEMENTED Response",
                response
                );
        }
        finally
        {
            console.logExit();
        }

    }

//-------------------------------- User Initiated processing ---------------------------------
    Call invite(String callee, String sdpContent) throws
        CommunicationsException
    {
        try
        {
            console.logEntry();

            callee = callee.trim();
            //Remove excessive characters from phone numbers such as ' ','(',')','-'
            String excessiveChars = Utils.getProperty("net.java.sip.communicator.sip.EXCESSIVE_URI_CHARACTERS");


            //---------------------------------------------------------------------------
            //un ugly hack to override old xml configurations (todo: remove at some point)
            //define excessive chars for sipphone.com users
            String isSipphone = Utils.getProperty("net.java.sip.communicator.sipphone.IS_RUNNING_SIPPHONE");
            if(excessiveChars == null && isSipphone != null && isSipphone.equalsIgnoreCase("true"))
            {
                excessiveChars = "( )-";
                PropertiesDepot.setProperty("net.java.sip.communicator.sip.EXCESSIVE_URI_CHARACTERS", excessiveChars);
                PropertiesDepot.storeProperties();
            }
            //---------------------------------------------------------------------------


            if(excessiveChars != null )
            {
                StringBuffer calleeBuff = new StringBuffer(callee);
                for(int i = 0; i < excessiveChars.length(); i++)
                {
                    String charToDeleteStr = excessiveChars.substring(i, i+1);

                    int charIndex = -1;
                    while( (charIndex = calleeBuff.indexOf(charToDeleteStr)) != -1)
                        calleeBuff.delete(charIndex, charIndex + 1);
                }
                callee = calleeBuff.toString();
            }

            //Handle default domain name (i.e. transform 1234 -> 1234@sip.com
            String defaultDomainName =
                Utils.getProperty("net.java.sip.communicator.sip.DEFAULT_DOMAIN_NAME");
            if (defaultDomainName != null //no sip scheme
                && !callee.trim().startsWith("tel:")
                && callee.indexOf('@') == -1 //most probably a sip uri
                ) {
                callee = callee + "@" + defaultDomainName;
            }

            //Let's be uri fault tolerant
            if (callee.toLowerCase().indexOf("sip:") == -1 //no sip scheme
                && callee.indexOf('@') != -1 //most probably a sip uri
                ) {
                callee = "sip:" + callee;

            }
            //Request URI
            URI requestURI;
            try {
                requestURI = sipManCallback.addressFactory.createURI(callee);
            }
            catch (ParseException ex) {
                console.error(callee + " is not a legal SIP uri!", ex);
                throw new CommunicationsException(callee +
                                                  " is not a legal SIP uri!", ex);
            }
            //Call ID
            CallIdHeader callIdHeader = sipManCallback.sipProvider.getNewCallId();
            //CSeq
            CSeqHeader cSeqHeader;
            try {
                cSeqHeader = sipManCallback.headerFactory.createCSeqHeader(1,
                    Request.INVITE);
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
            //ViaHeaders
            ArrayList viaHeaders = sipManCallback.getLocalViaHeaders();
            //MaxForwards
            MaxForwardsHeader maxForwards = sipManCallback.getMaxForwardsHeader();
            //Contact
            ContactHeader contactHeader = sipManCallback.getContactHeader();
            Request invite = null;
            try {
                invite = sipManCallback.messageFactory.createRequest(requestURI,
                    Request.INVITE,
                    callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders,
                    maxForwards);
            }
            catch (ParseException ex) {
                console.error(
                    "Failed to create invite Request!", ex);
                throw new CommunicationsException(
                    "Failed to create invite Request!", ex);
            }
            //
            invite.addHeader(contactHeader);
            //Content
            ContentTypeHeader contentTypeHeader = null;
            try {
                //content type should be application/sdp (not applications)
                //reported by Oleg Shevchenko (Miratech)
                contentTypeHeader =
                    sipManCallback.headerFactory.createContentTypeHeader(
                    "application", "sdp");
            }
            catch (ParseException ex) {
                //Shouldn't happen
                console.error(
                    "Failed to create a content type header for the INVITE request",
                    ex);
                throw new CommunicationsException(
                    "Failed to create a content type header for the INVITE request",
                    ex);
            }
            try {
                invite.setContent(sdpContent, contentTypeHeader);
            }
            catch (ParseException ex) {
                console.error(
                    "Failed to parse sdp data while creating invite request!", ex);
                throw new CommunicationsException(
                    "Failed to parse sdp data while creating invite request!", ex);
            }
            //Transaction
            ClientTransaction inviteTransaction;
            try {
                inviteTransaction = sipManCallback.sipProvider.
                    getNewClientTransaction(invite);
            }
            catch (TransactionUnavailableException ex) {
                console.error(
                    "Failed to create inviteTransaction.\n" +
                    "This is most probably a network connection error.", ex);
                throw new CommunicationsException(
                    "Failed to create inviteTransaction.\n" +
                    "This is most probably a network connection error.", ex);
            }
            try {
                inviteTransaction.sendRequest();
                if( console.isDebugEnabled() )
                    console.debug("sent request: " + invite);
            }
            catch (SipException ex) {
                console.error(
                    "An error occurred while sending invite request", ex);
                throw new CommunicationsException(
                    "An error occurred while sending invite request", ex);
            }
            Call call = callDispatcher.createCall(inviteTransaction.
                                                  getDialog(), invite);
            call.setState(Call.DIALING);
            return call;
        }
        finally
        {
            console.logExit();
        }

    }

    //end call
    void endCall(int callID) throws CommunicationsException
    {
        try
        {
            console.logEntry();

            Call call = callDispatcher.getCall(callID);
            if (call == null) {
                console.error(
                    "Could not find call with id=" +
                    callID);
                throw new CommunicationsException(
                    "Could not find call with id=" +
                    callID);
            }
            Dialog dialog = call.getDialog();
            if (call.getState().equals(Call.CONNECTED)
                || call.getState().equals(Call.RECONNECTED)) {
                call.setState(Call.DISCONNECTED);
                sayBye(dialog);
            }
            else if (call.getState().equals(Call.DIALING)
                     || call.getState().equals(Call.RINGING)) {
                if (dialog.getFirstTransaction() != null) {
                    try {
                        //Someone knows about us. Let's be polite and say we are leaving
                        sayCancel(dialog);
                    }
                    catch (CommunicationsException ex) {
                        //something went wrong let's just tell the others
                        console.error(
                            "Could not send the CANCEL request! "
                            + "Remote party won't know we're leaving!",
                            ex);
                        sipManCallback.fireCommunicationsError(
                            new CommunicationsException(
                            "Could not send the CANCEL request! "
                            + "Remote party won't know we're leaving!",
                            ex));
                    }
                }
                call.setState(Call.DISCONNECTED);
            }
            else if (call.getState().equals(Call.ALERTING)) {
                call.setState(Call.DISCONNECTED);
                sayBusyHere(dialog);
            }
            //For FAILE and BUSY we only need to update CALL_STATUS
            else if (call.getState().equals(Call.BUSY)) {
                call.setState(Call.DISCONNECTED);
            }
            else if (call.getState().equals(Call.FAILED)) {
                call.setState(Call.DISCONNECTED);
            }

            else {
                call.setState(Call.DISCONNECTED);
                console.error(
                    "Could not determine call state!");
                throw new CommunicationsException
                    ("Could not determine call state!");
            }
        }
        finally
        {
            console.logExit();
        }

    } //end call

    //Bye
    private void sayBye(Dialog dialog) throws CommunicationsException
    {
        try
        {
            console.logEntry();

            Request request = dialog.getFirstTransaction().getRequest();
            Request bye = null;
            try {
                bye = dialog.createRequest(Request.BYE);
            }
            catch (SipException ex) {
                console.error(
                    "Failed to create bye request!",
                    ex);
                throw new CommunicationsException(
                    "Failed to create bye request!",
                    ex);
            }
            ClientTransaction clientTransaction = null;
            try {
                clientTransaction =
                    sipManCallback.sipProvider.getNewClientTransaction(bye);
            }
            catch (TransactionUnavailableException ex) {
                console.error(
                    "Failed to construct a client transaction from the BYE request",
                    ex);
                throw new CommunicationsException(
                    "Failed to construct a client transaction from the BYE request",
                    ex);
            }
            try {
                dialog.sendRequest(clientTransaction);
                if( console.isDebugEnabled() )
                    console.debug("sent request: " + bye);
            }
            catch (SipException ex1) {
                throw new CommunicationsException("Failed to send the BYE request");
            }
        }
        finally
        {
            console.logExit();
        }

    } //bye

    //cancel
    private void sayCancel(Dialog dialog) throws CommunicationsException
    {
        try
        {
            console.logEntry();

            Request request = dialog.getFirstTransaction().getRequest();
            if (dialog.isServer()) {
                console.error("Cannot cancel a server transaction");
                throw new CommunicationsException(
                    "Cannot cancel a server transaction");
            }
            ClientTransaction clientTransaction =
                (ClientTransaction) dialog.getFirstTransaction();
            try {
                Request cancel = clientTransaction.createCancel();
                ClientTransaction cancelTransaction =
                    sipManCallback.sipProvider.getNewClientTransaction(cancel);
                cancelTransaction.sendRequest();
                if( console.isDebugEnabled() )
                    console.debug("sent request: " + cancel);
            }
            catch (SipException ex) {
                console.error("Failed to send the CANCEL request", ex);
                throw new CommunicationsException(
                    "Failed to send the CANCEL request", ex);
            }
        }
        finally
        {
            console.logExit();
        }

    } //cancel

    //busy here
    private void sayBusyHere(Dialog dialog) throws CommunicationsException
    {
        try
        {
            console.logEntry();

            Request request = dialog.getFirstTransaction().getRequest();
            Response busyHere = null;
            try {
                busyHere = sipManCallback.
                    messageFactory.createResponse(Response.BUSY_HERE, request);
                sipManCallback.attachToTag(busyHere, dialog);
            }
            catch (ParseException ex) {
                console.error("Failed to create the BUSY_HERE response!", ex);
                throw new CommunicationsException(
                    "Failed to create the BUSY_HERE response!", ex);
            }
            if (!dialog.isServer()) {
                console.error("Cannot send BUSY_HERE in a client transaction");
                throw new CommunicationsException(
                    "Cannot send BUSY_HERE in a client transaction");
            }
            ServerTransaction serverTransaction =
                (ServerTransaction) dialog.getFirstTransaction();
            try {
                serverTransaction.sendResponse(busyHere);
                if( console.isDebugEnabled() )
                    console.debug("sent response: " + busyHere);
            }
            catch (SipException ex) {
                console.error("Failed to send the BUSY_HERE response", ex);
                throw new CommunicationsException(
                    "Failed to send the BUSY_HERE response", ex);
            }
        }
        finally
        {
            console.logExit();
        }

    } //busy here

    //------------------ say ok
    public void sayOK(int callID, String sdpContent) throws
        CommunicationsException
    {
        try
        {
            console.logEntry();

            Call call = callDispatcher.getCall(callID);
            if (call == null) {
                console.error("Failed to find call with id=" + callID);
                throw new CommunicationsException(
                    "Failed to find call with id=" + callID);
            }
            Dialog dialog = call.getDialog();
            if (dialog == null) {
                call.setState(Call.DISCONNECTED);
                console.error(
                    "Failed to extract call's associated dialog! Ending Call!");
                throw new CommunicationsException(
                    "Failed to extract call's associated dialog! Ending Call!");
            }
            Transaction transaction = dialog.getFirstTransaction();
            if (transaction == null  || !dialog.isServer()) {
                call.setState(Call.DISCONNECTED);
                throw new CommunicationsException(
                    "Failed to extract a ServerTransaction "
                    +"from the call's associated dialog!");
            }
            ServerTransaction serverTransaction = (ServerTransaction) transaction;
            Response ok = null;
            try {
                ok = sipManCallback.messageFactory.createResponse(
                    Response.OK,
                    dialog.getFirstTransaction().getRequest());
                sipManCallback.attachToTag(ok, dialog);
            }
            catch (ParseException ex) {
                call.setState(Call.DISCONNECTED);
                console.error(
                    "Failed to construct an OK response to an INVITE request",
                    ex);
                throw new CommunicationsException(
                    "Failed to construct an OK response to an INVITE request",
                    ex);
            }
            //Content
            ContentTypeHeader contentTypeHeader = null;
            try {
                //content type should be application/sdp (not applications)
                //reported by Oleg Shevchenko (Miratech)
                contentTypeHeader =
                    sipManCallback.headerFactory.createContentTypeHeader(
                    "application", "sdp");
            }
            catch (ParseException ex) {
                //Shouldn't happen
                call.setState(Call.DISCONNECTED);
                console.error(
                    "Failed to create a content type header for the OK request",
                    ex);
                throw new CommunicationsException(
                    "Failed to create a content type header for the OK request",
                    ex);
            }
            try {
                ok.setContent(sdpContent, contentTypeHeader);
            }
            catch (NullPointerException ex) {
                call.setState(Call.DISCONNECTED);
                console.error(
                    "No sdp data was provided for the ok response to an INVITE request!",
                    ex);
                throw new CommunicationsException(
                    "No sdp data was provided for the ok response to an INVITE request!",
                    ex);
            }
            catch (ParseException ex) {
                call.setState(Call.DISCONNECTED);
                console.error(
                    "Failed to parse sdp data while creating invite request!", ex);
                throw new CommunicationsException(
                    "Failed to parse sdp data while creating invite request!", ex);
            }
            //TODO This is here provisionally as my remote user agent that I am using for
            //testing is not doing it. It is not correct from the protocol point of view
            //and should probably be removed
            if ( ( (ToHeader) ok.getHeader(ToHeader.NAME)).getTag() == null) {
                try {
                    ( (ToHeader) ok.getHeader(ToHeader.NAME)).setTag(Integer.
                        toString(dialog.hashCode()));
                }
                catch (ParseException ex) {
                    call.setState(Call.DISCONNECTED);
                    throw new CommunicationsException(
                        "Unable to set to tag",
                        ex
                        );
                }
            }
            ContactHeader contactHeader = sipManCallback.getContactHeader();
            ok.addHeader(contactHeader);
            try {
                serverTransaction.sendResponse(ok);
                if( console.isDebugEnabled() )
                    console.debug("sent response " + ok);
            }
            catch (SipException ex) {
                call.setState(Call.DISCONNECTED);
                console.error(
                    "Failed to send an OK response to an INVITE request",
                    ex
                    );
                throw new CommunicationsException(
                    "Failed to send an OK response to an INVITE request",
                    ex
                    );
            }
        }
        finally
        {
            console.logExit();
        }

    } //answer call

    //------------------ Internal Error
    void sayInternalError(int callID) throws CommunicationsException
    {
        try
        {
            console.logEntry();

            Call call = callDispatcher.getCall(callID);
            if (call == null) {
                console.error("Failed to find call with id=" + callID);
                throw new CommunicationsException(
                    "Failed to find call with id=" + callID
                    );
            }
            Dialog dialog = call.getDialog();
            if (dialog == null) {
                call.setState(Call.DISCONNECTED);
                console.error(
                    "Failed to extract call's associated dialog! Ending Call!"
                    );
                throw new CommunicationsException(
                    "Failed to extract call's associated dialog! Ending Call!"
                    );
            }
            Transaction transaction = dialog.getFirstTransaction();
            if (transaction == null || !dialog.isServer()) {
                call.setState(Call.DISCONNECTED);
                console.error(
                    "Failed to extract a transaction"
                    +" from the call's associated dialog!");
                throw new CommunicationsException(
                    "Failed to extract a transaction from the call's associated dialog!"
                    );
            }
            ServerTransaction serverTransaction = (ServerTransaction) transaction;
            Response internalError = null;
            try {
                internalError = sipManCallback.messageFactory.createResponse(
                    Response.SERVER_INTERNAL_ERROR,
                    dialog.getFirstTransaction().getRequest());
                sipManCallback.attachToTag(internalError, dialog);
            }
            catch (ParseException ex) {
                call.setState(Call.DISCONNECTED);
                console.error(
                    "Failed to construct an OK response to an INVITE request",
                    ex);
                throw new CommunicationsException(
                    "Failed to construct an OK response to an INVITE request",
                    ex);
            }
            ContactHeader contactHeader = sipManCallback.getContactHeader();
            internalError.addHeader(contactHeader);
            try {
                serverTransaction.sendResponse(internalError);
                if( console.isDebugEnabled() )
                    console.debug("sent response: " + internalError);
            }
            catch (SipException ex) {
                call.setState(Call.DISCONNECTED);
                console.error(
                    "Failed to send an OK response to an INVITE request",
                    ex);
                throw new CommunicationsException(
                    "Failed to send an OK response to an INVITE request",
                    ex
                    );
            }
        }
        finally
        {
            console.logExit();
        }

    } //internal error

    CallDispatcher getCallDispatcher()
    {
        return callDispatcher;
    }

    //The following method is currently being implemented and tested
    protected void processReInvite(ServerTransaction serverTransaction, Request request)
    {
        try
        {
            console.logEntry();

            console.error("processReInvite is not yet implemented");
        }finally
        {
            console.logExit();
        }

    }
}

/*
 * ResponseForwarding.java
 *
 * Created on April 16, 2003, 11:09 AM
 */

package gov.nist.sip.proxy;
import java.util.*;
import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;
import gov.nist.sip.proxy.presenceserver.*;
/**
 *
 * @author  deruelle
 */
public class ResponseForwarding {
    
    protected Proxy proxy;
    
    /** Creates a new instance of ResponseForwarding */
    public ResponseForwarding(Proxy proxy) {
        this.proxy=proxy;
    }
    
    protected void forwardResponse(SipProvider sipProvider,
    Response response,ClientTransaction clientTransaction) {
        MessageFactory messageFactory=proxy.getMessageFactory();
        HeaderFactory headerFactory=proxy.getHeaderFactory();
        SipStack sipStack=proxy.getSipStack();
        AddressFactory addressFactory=proxy.getAddressFactory();
	TransactionsMapping transactionsMapping = null;
        
        try{
            
/***************************************************************************/
/*******************  PROXY BEHAVIOR    ************************************/
/***************************************************************************/
            
       /* RFC 3261: 16.7. Response Processing:
          When a response is received by an element, it first tries 
          to locate a client transaction (Section 17.1.3) matching 
	  the response.  If none is found, the element MUST process 
	  the response (even if it is an informational response) 
	  as a stateless proxy (described below).  If a match is found, 
	  the response is handed to the client transaction.  
         1. Find the appropriate response context
         2. Update timer C for provisional responses
         3. Remove the topmost Via
         4. Add the response to the response context
         5. Check to see if this response should be forwarded immediately
         6. When necessary, choose the best final response from the
             response context
         7.  Aggregate authorization header field values if necessary
         8.  Optionally rewrite Record-Route header field values
         9.  Forward the response
         10. Generate any necessary CANCEL requests
            
       16.11: Response processing as described in Section 16.7 does not
         apply to a proxy behaving statelessly.  
         When a response arrives at a stateless proxy, the proxy 
	 MUST inspect the sent-by value in the first (topmost) 
	 Via header field value.  If that address matches the proxy,
         (it equals a value this proxy has inserted into previous requests)
         the proxy MUST remove that header field value from the response and
         forward the result to the location indicated in the next Via header
         field value.  The proxy MUST NOT add to, modify, or remove the
         message body.  Unless specified otherwise, the proxy MUST NOT remove
         any other header field values.  If the address does not match the
         proxy, the message MUST be silently discarded.
     */
            
	     
            if (clientTransaction == null || 
		clientTransaction.getDialog() == null )  {
                if (ProxyDebug.debug)
                    ProxyDebug.println
                 ("ResponseForwarding, forwardResponse(), " +
		   " the client transaction does not exist, " +
                    " will forward the response statelessly!");
                
                if (ProxyUtilities.hasTopViaHeaderProxy(sipStack,response) ) {
                    ListIterator viaList=response.getHeaders(ViaHeader.NAME);
                    response.removeHeader(ViaHeader.NAME);
                    Vector v=new Vector();
                    viaList.next();
                    while (viaList!=null && viaList.hasNext()) {
                        ViaHeader viaHeader=(ViaHeader) viaList.next();
                        v.addElement(viaHeader);
                    }
                    for (int j=0;j<v.size();j++) {
                        ViaHeader viaHeader=(ViaHeader) v.elementAt(j);
                        response.addHeader(viaHeader);
                    }
                    
                    viaList=response.getHeaders(ViaHeader.NAME);
                    if (viaList==null || !viaList.hasNext()) {
                        if (ProxyDebug.debug)
                            ProxyDebug.println
                            ("ResponseForwarding, forwardResponse(), "	+
			    "the response does "+
                            " not have any Via header, the response is "+
			    "silently discarded");
                    } else {
                        // let's hope the sipProvider will take the Viaheader to
                        // forward the response...
                        sipProvider.sendResponse(response);
                        if (ProxyDebug.debug)
                            ProxyDebug.println
                            ("ResponseForwarding, forwardResponse(), "+
			    "the response statelessly forwarded:\n"+
                            response.toString());
                    }
                    return;
                } else {
                    //If the address does not match the
                    //proxy, the message MUST be silently discarded.
                    if (ProxyDebug.debug)
                        ProxyDebug.println 
                        ("ResponseForwarding, forwardResponse(), " +
		       "the address (Via header)does not"+ 
                        " match the proxy, the message is silently discarded");
                    return;
                }
                
            }

	    if (clientTransaction.getDialog() == null ) return;
	    else 
	      transactionsMapping = 
		(TransactionsMapping) 
		clientTransaction.getDialog().getApplicationData();
            
            
/***************************************************************************/
/************************ 1. Find the appropriate response context ********/
/***************************************************************************/
            
           /* The proxy locates the "response context" it created before
              forwarding the original request using the key described in
              Section 16.6.  The remaining processing steps take place in
              this context.
            */
            
            // I guess it is done by the stack....
            
/****************************************************************************/
/************** 2.  Update timer C for provisional responses        ********/
/****************************************************************************/
            
            // I guess it is done by the stack....
            
/****************************************************************************/
/*************** 3.  Via                                             ********/
/****************************************************************************/
            
          /*  The proxy removes the topmost Via header field value from the
              response.
         If no Via header field values remain in the response, the
         response was meant for this element and MUST NOT be forwarded.
         The remainder of the processing described in this section is
         not performed on this message, the UAC processing rules
         described in Section 8.1.3 are followed instead (transport
         layer processing has already occurred).
           */
            
            ListIterator viaList=response.getHeaders(ViaHeader.NAME);
            viaList.next();
            viaList.remove();
            if ( !viaList.hasNext()) {
                ProxyDebug.println
		("ResponseForwarding, forwardResponse() (STEP 3),"+
                " the response does not "+
                " contain any Via headers, the response was meant for " +
		" the proxy and MUST NOT be forwarded");
                return;
            }
            
/*****************************************************************************/
/*************** 4.  Add response to context                         ********/
/****************************************************************************/
            
            // The stack takes care of that...
            
/***************************************************************************/
/************** 5.  Check response for forwarding                   ********/
/***************************************************************************/
        /* Until a final response has been sent on the server transaction,
           the following responses MUST be forwarded immediately:
         -  Any provisional response other than 100 (Trying)
         -  Any 2xx response
         */
            
            if ( response.getStatusCode() == 100) {
		if (ProxyDebug.debug)
                ProxyDebug.println
		("ResponseForwarding, forwardResponse() (STEP 5)"+
                " don't forward the 100 Trying.");
                return;
            }
            if ( response.getStatusCode() == 487) {
		if (ProxyDebug.debug)
                ProxyDebug.println
		("ResponseForwarding, forwardResponse() (STEP 5),"+
                " we don't forward the"+
                " 487 Request terminated, in statefull behavior " +
		" (it's for the proxy).");
                return;
            }
            CSeqHeader cseqHeader=
		(CSeqHeader)response.getHeader(CSeqHeader.NAME);
            // No special processing for OK related to SUBSCIRBE and NOTIFY
            if ( proxy.isPresenceServer() 
		 && response.getStatusCode() == 200 &&
                cseqHeader.getMethod().equals("SUBSCRIBE")) {
		if (ProxyDebug.debug)
                ProxyDebug.println
		("ResponseForwarding, forwardResponse() (STEP 5),"+
                " the OK for SUBSCRIBE is discarded (Hop by hop)");
               
                return;
            }
            if ( proxy.isPresenceServer() && response.getStatusCode() == 200 &&
                 cseqHeader.getMethod().equals("NOTIFY")) {
		if (ProxyDebug.debug)
                ProxyDebug.println
		("ResponseForwarding, forwardResponse() (STEP 5),"+
                " the OK for NOTIFY is discarded (Hop by hop)");
                return;
            }
            
/****************************************************************************/
/*************** 6.  Choosing the best response                      ********/
/****************************************************************************/
            
          /*  A proxy MUST NOT
         insert a tag into the To header field of a 1xx or 2xx response
         if the request did not contain one.  A proxy MUST NOT modify
         the tag in the To header field of a 1xx or 2xx response.
            
          An element SHOULD preserve the To tag
         when simply forwarding a 3-6xx response to a request that did
         not contain a To tag.
            
         A proxy MUST NOT modify the To tag in any forwarded response to
         a request that contains a To tag.
         */
            
            
/*****************************************************************************/
/**************** 7.  Aggregate Authorization Header Field Values     ********/
/****************************************************************************/
            
/****************************************************************************/
/**************** 8.  Record-Route                                    ********/
/****************************************************************************/
            
            // we don't modify the record-route...
            
/*****************************************************************************/
/**************** 9.  Forward response                                ********/
/*****************************************************************************/
           
         /* 16.7. 9.  Forward response:  The
         proxy MUST pass the response to the server transaction
         associated with the response context.  This will result in the
         response being sent to the location now indicated in the
         topmost Via header field value.  If the server transaction is
         no longer available to handle the transmission, the element
         MUST forward the response statelessly by sending it to the
         server transport.  The server transaction might indicate
         failure to send the response or signal a timeout in its state
         machine.  These errors would be logged for diagnostic purposes
         as appropriate, but the protocol requires no remedial action
         from the proxy.  
          */
              ServerTransaction serverTransaction=transactionsMapping.
              getServerTransaction(clientTransaction);
            
           
            // For forking: 
            if ( (response.getStatusCode() == 401 
		||response.getStatusCode() == 603  )
                  && serverTransaction!=null ) {
                // check the busy or 
                Vector clientsTransactionList=transactionsMapping.
                	getClientTransactions(serverTransaction); 
                 if (clientsTransactionList!=null && 
		      clientsTransactionList.size()>1){
                       if (ProxyDebug.debug)
                        ProxyDebug.println
                        ("ResponseForwarding, forwardResponse() (STEP 9), " +
		         "the BUSY or DECLINE corresponds to a forked call, "+
			" so we drop it and wait for"+
                        " the others responses.");
                       
                       transactionsMapping.
                        removeMapping(clientTransaction);
                 }
            }
           
              // For forking:
            if (response.getStatusCode() == 200 && serverTransaction!=null ) {
                 Dialog peerDialog=serverTransaction.getDialog();
                 Vector clientsTransactionList=transactionsMapping.
                 getClientTransactions(serverTransaction); 
                
                 if (peerDialog !=null && peerDialog.getState()!=null &&
                     peerDialog.getState().equals(DialogState.CONFIRMED) &&
                     clientsTransactionList!=null && 
			clientsTransactionList.size()>1 ) {
                      if (ProxyDebug.debug)
                        ProxyDebug.println
                        ("ResponseForwarding, forwardResponse() (STEP 9), " +
			"the 200 OK corresponds to a forked call, " +
			"so we drop it and send a BYE");
                      
                       Dialog dialog=clientTransaction.getDialog(); 
                       Request byeRequest
                            = dialog.createRequest("BYE");
                       
                       ClientTransaction ct=
                            sipProvider.getNewClientTransaction
                            (byeRequest);
                       dialog.sendRequest(clientTransaction);
			if (ProxyDebug.debug)
                            ProxyDebug.println
                            ("ResponseForwarding, forwardResponse() (STEP 9), "+
                            "BYE created and sent for 200 OK (forking):\n" +
                            byeRequest.toString());
                            
                       // we have to remove the transaction from the table:
                        transactionsMapping.
                        removeMapping(clientTransaction);
                        return;
                 } else {
		    if (serverTransaction != null) 
		    	transactionsMapping.addMapping
				(serverTransaction,clientTransaction);
		}
            }
            
                if (serverTransaction == null) {
                    sipProvider.sendResponse(response);
                    if (ProxyDebug.debug)
                        ProxyDebug.println
                        ("ResponseForwarding, forwardResponse() (STEP 9), the "+
                        " response is statelessly"+
                        " forwarded:\n"+ response.toString());
		    return;
                } else {
                    // we can try to modify the tags:
                    Dialog dialog=serverTransaction.getDialog();
                    if (dialog!=null) {
                        String localTag= dialog.getLocalTag();
                        String remoteTag= dialog.getRemoteTag();
                        ToHeader toHeader=
				(ToHeader)response.getHeader(ToHeader.NAME);
                        FromHeader fromHeader=
			 	(FromHeader)response.getHeader(FromHeader.NAME);
                        
                        if (localTag!=null && remoteTag!=null) {
                            if (dialog.isServer() ) {
                                fromHeader.setTag(remoteTag);
			    if (ProxyDebug.debug)
                               ProxyDebug.println
                               ("ResponseForwarding, forwardResponse() " +
		                " (STEP 9), we changed the " +
                                "toTag:"+localTag);   
                            } else { 
			      toHeader.setTag(localTag);
			      if (ProxyDebug.debug)
                                ProxyDebug.println
                                 ("ResponseForwarding, forwardResponse() "+
				 " (STEP 9),  we changed the fromTag:"
				+remoteTag);
                            }
                        }
                    }

                    serverTransaction.sendResponse(response);
                    if (ProxyDebug.debug)
                        ProxyDebug.println
                        ("ResponseForwarding, forwardResponse() (STEP 9), the "+
                        " response is statefully forwarded: \n"
		         +response.toString());
                    
             
 	    }          
            
            
/***************************************************************************/
/************** 10. Generate CANCELs                                ********/
/****************************************************************************/    
           /*
           If the forwarded response was a final response, the proxy MUST
         generate a CANCEL request for all pending client transactions
         associated with this response context.  A proxy SHOULD also
         generate a CANCEL request for all pending client transactions
         associated with this response context when it receives a 6xx
         response.  A pending client transaction is one that has
         received a provisional response, but no final response (it is
         in the proceeding state) and has not had an associated CANCEL
         generated for it.  Generating CANCEL requests is described in
         Section 9.1.
           */
            
            if (response.getStatusCode() == 200 || 
                (response.getStatusCode() >= 600 
		&& response.getStatusCode()<=606) ){
               Vector clientsTransactionList=transactionsMapping.
               getClientTransactions(serverTransaction);
	     
               for ( Enumeration e = clientsTransactionList.elements();
		     e.hasMoreElements(); )  {
                    ClientTransaction 
			ctr=(ClientTransaction) e.nextElement();
                    if ( ctr != clientTransaction ) {
                        TransactionState transactionState=ctr.getState();
                        if (transactionState==null ||
                        transactionState.getValue()==
			TransactionState.PROCEEDING.getValue()) {
                            
      /* 9.1: The following procedures are used to construct a CANCEL request.  
       The Request-URI, Call-ID, To, the numeric part of CSeq, and From header
       fields in the CANCEL request MUST be identical to those in the
       request being cancelled, including tags.  A CANCEL constructed by a
       client MUST have only a single Via header field value matching the
       top Via value in the request being cancelled.  Using the same values
       for these header fields allows the CANCEL to be matched with the
       request it cancels (Section 9.2 indicates how such matching occurs).
       However, the method part of the CSeq header field MUST have a value
       of CANCEL.  This allows it to be identified and processed as a
       transaction in its own right (See Section 17).
                             
        If the request being cancelled contains a Route header field, the
        CANCEL request MUST include that Route header field's values.
        */
                            Request cancelRequest
                            =ctr.createCancel();   
                    
                          
                            // Let's keep only the top most via header:
                            ListIterator cancelViaList= 
				cancelRequest.getHeaders(ViaHeader.NAME);
                            cancelRequest.removeHeader(ViaHeader.NAME);
                            cancelRequest.addHeader
				((ViaHeader)cancelViaList.next());
                            
                            ClientTransaction ct=
                            sipProvider.getNewClientTransaction
                            (cancelRequest);
                            ct.sendRequest();
			    if (ProxyDebug.debug)
                            ProxyDebug.println
                            ("ResponseForwarding, forwardResponse()" +
			     " (STEP 10), CANCEL created"+
                            " and sent for cancel the pending " +
			     "transactions:\n" + cancelRequest.toString());
                        }
                    }
               }
            }
            
        } catch (Exception ex) {
            if (ProxyDebug.debug) {
                ProxyDebug.println
		("ResponseForwarding, forwardResponse(), internal error, "+
                "exception raised:");
                ProxyDebug.logException(ex);
            }
        } finally {
	     if (clientTransaction != null &&
		clientTransaction.getState().equals
		(TransactionState.COMPLETED)) {
		if (clientTransaction.getDialog() != null) {
		    transactionsMapping  = 
	        	(TransactionsMapping) 
			clientTransaction.getDialog().getApplicationData();
		    if (transactionsMapping != null)
		    transactionsMapping.removeMapping(clientTransaction);
		}

	     }
	}
    }

}

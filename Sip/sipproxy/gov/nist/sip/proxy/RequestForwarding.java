/*
 * RequestForwarding.java
 *
 * Created on April 16, 2003, 11:08 AM
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
 * @author Deruelle
 */
public class RequestForwarding {
    
    protected Proxy proxy;
    
    
    /** Creates a new instance of RequestForwarding */
    public RequestForwarding(Proxy proxy) {
       this.proxy=proxy;
    }
    
    public void forwardRequest
	(Vector targetsURIList,
	SipProvider sipProvider,
        Request request,
	ServerTransaction serverTransaction,
	boolean statefullForwarding) {
        /* RFC 3261: 16.6. Request Forwarding
         * For each target, the proxy forwards the request following these steps:
         *
         * 1.  Make a copy of the received request
         *
         * 2.  Update the Request-URI
         *
         * 3.  Update the Max-Forwards header field
         *
         * 4.  Optionally add a Record-route header field value
         *
         * 5.  Optionally add additional header fields
         *
         * 6.  Postprocess routing information
         *
         * 7.  Determine the next-hop address, port, and transport
         *
         * 8.  Add a Via header field value
         *
         * 9.  Add a Content-Length header field if necessary
         *
         * 10. Forward the new request
         *
         * 11. Set timer C
         */
        MessageFactory messageFactory=proxy.getMessageFactory();
        HeaderFactory headerFactory=proxy.getHeaderFactory();
        SipStack sipStack=proxy.getSipStack();
        AddressFactory addressFactory=proxy.getAddressFactory();
        //Get the parameters and the transport of the request URI
        URI requestURI=request.getRequestURI();
        Iterator parametersNames=null;
        String transport=null;
        System.out.println(requestURI.toString());
        if(requestURI.isSipURI()){
        	parametersNames=((SipURI)requestURI).getParameterNames();
        	transport=((SipURI)requestURI).getTransportParam();
        }        
        
        try{
            
            for (int i=0;i<targetsURIList.size();i++) {
                
                URI targetURI=(URI)targetsURIList.elementAt(i);
                //Copy the parameters and the transport in the new Request URI 
                //of the cloned Request
/**************************************************************************/
/************** 1.  Make a of received request                   **********/
/**************************************************************************/				
                Request clonedRequest = (Request) request.clone();
                if (ProxyDebug.debug)
                   ProxyDebug.println
		   ("RequestForwarding, forwardRequest() (STEP 1),"+
                   " the request is cloned");
/***************************************************************************/
/************** 2.  Update the Request-URI                        **********/
/***************************************************************************/
                
                /*
                The Request-URI in the copy's start line MUST be replaced with
                the URI for this target.  If the URI contains any parameters
                not allowed in a Request-URI, they MUST be removed.

		This is the essence of a proxy's role.  This is the mechanism
         	through which a proxy routes a request toward its destination.

         	In some circumstances, the received Request-URI is placed into
         	the target set without being modified.  For that target, the
         	replacement above is effectively a no-op.
		
		Note -- this should only be done if the target domain is
		managed by the proxy server.
		Bug fix by Daniel Martinez.
                 */
                // All the targets URI are already canonicalized

		if (requestURI.isSipURI()) {
			if ( proxy.managesDomain(((SipURI)requestURI).getHost())) {
				clonedRequest.setRequestURI(targetURI);
                		if (ProxyDebug.debug) {
                   			ProxyDebug.println
		    			("RequestForwarding, forwardRequest() (STEP 2),"+
                   			" The Request-URI in the copy's start line is replaced with"+
                   			" the URI for this target");
				}
			} else {
                		if (ProxyDebug.debug) {
                   			ProxyDebug.println
		    			("RequestForwarding, forwardRequest() (STEP 2),"+
					"the proxy does not manage the domain " + 
					((SipURI)targetURI).getHost());
				}
			}
		}
                
/**************************************************************************/
/************** 3. Max-Forwards                                  **********/
/**************************************************************************/
                /*
                If the copy contains a Max-Forwards header field, the proxy
                MUST decrement its value by one (1).
                If the copy does not contain a Max-Forwards header field, the
                proxy MUST add one with a field value, which SHOULD be 70.
                 */
                
                // RequestValidation took already care 
	        // to check if the header has
                // reached 0.
                MaxForwardsHeader mf = 
			(MaxForwardsHeader) clonedRequest.getHeader
                (MaxForwardsHeader.NAME);
                if (mf == null) {
                    if (ProxyDebug.debug)
                        ProxyDebug.println
			("RequestForwarding, forwardRequest() "+
                        " (STEP 3), MaxForwardHeader "+
                        " created and added to the cloned request");
                    mf=headerFactory.createMaxForwardsHeader(70);
                    clonedRequest.addHeader(mf);
                }
                else  {
                    if (ProxyDebug.debug)
                        ProxyDebug.println
			("RequestForwarding, forwardRequest(), "+
                        " (STEP 3) MaxForwardHeader "+
                        " decremented by one.");
                    mf.setMaxForwards(mf.getMaxForwards() - 1);
                }
                
/***************************************************************************/
/************** 4. Record-Route                                   **********/
/***************************************************************************/
                
                /*
                The URI placed in the Record-Route header field value MUST be a
                SIP or SIPS URI.  This URI MUST contain an lr parameter (see
                Section 19.1.1).  This URI MAY be different for each
                destination the request is forwarded to.
                The URI SHOULD NOT contain the transport parameter.
                 */
                
                // Only in statefull forwarding
                // We add our proxy RecordRoute header to the top of the list
                // We take the first listening point.
               
                String stackIPAddress=sipStack.getIPAddress();
                /* Iterator lps=sipStack.getListeningPoints();
                ListeningPoint lp=(ListeningPoint)lps.next();
		*/
	     	ListeningPoint lp=sipProvider.getListeningPoint();
                 if (statefullForwarding) {
               	SipURI sipURI=addressFactory.createSipURI(null,stackIPAddress);
                sipURI.setPort(lp.getPort());
                
                Address address=addressFactory.createAddress(null,sipURI);
                RecordRouteHeader recordRouteHeader=
                headerFactory.createRecordRouteHeader(address);
                
                // lr parameter to add:
                
                ListIterator recordRouteHeaders=clonedRequest.getHeaders
                (RecordRouteHeader.NAME);
                clonedRequest.removeHeader(RecordRouteHeader.NAME);
                Vector v=new Vector();
                v.addElement(recordRouteHeader);
                // add the other record route headers.
                while( recordRouteHeaders!=null 
			&& recordRouteHeaders.hasNext()) {
                    recordRouteHeader=
			(RecordRouteHeader)recordRouteHeaders.next();
                    v.addElement(recordRouteHeader);
                }
                for (int j=0;j<v.size();j++) {
                    recordRouteHeader=(RecordRouteHeader)v.elementAt(j);
                    clonedRequest.addHeader(recordRouteHeader);
                }
                
                if (ProxyDebug.debug)
                    ProxyDebug.println
		    ("RequestForwarding, forwardRequest(), (STEP 4)"+
                    " record-route header created and added to the " +
		    " cloned request");
                }
                else {
                    if (ProxyDebug.debug)
                    ProxyDebug.println
		    ("RequestForwarding, forwardRequest(), (STEP 4)"+
                    " record-route header not added to the cloned request " +
		    " (stateless)");
                }
                
/****************************************************************************/
/************** 5. Add Additional Header Fields                     **********/
/*****************************************************************************/
                
                // No Additional headers to add...
                if (ProxyDebug.debug)
                    ProxyDebug.println
		    ("RequestForwarding, forwardRequest(), (STEP 5)"+
                    " No Additional headers to add...");
                
/***************************************************************************/
/************** 6. Postprocess routing information                **********/
/***************************************************************************/
                
                /* If the copy contains a Route header field, 
		   the proxy MUST inspect the URI in its first value.  
		   If that URI does not
                   contain an lr parameter, the proxy MUST modify the copy as
                   follows:
                 
                   -  The proxy MUST place the Request-URI into the 
			Route header field as the last value.
                 
                   -  The proxy MUST then place the first Route header 
		      field value into the Request-URI and remove that 
		      value from the Route header field.
                 */
              
                // Strip first route if it is the proxy UIR adn lr parameter
                
                ListIterator routes = 
		    clonedRequest.getHeaders(RouteHeader.NAME);
                if (routes!=null && routes.hasNext() ) {
                
                    RouteHeader  routeHeader = (RouteHeader) routes.next();
                    Address routeAddress=routeHeader.getAddress();
                    URI routeURI=routeAddress.getURI();
                
                    if (  routeURI.isSipURI() && 
			((SipURI)routeURI).hasLrParam() ) {
                        
                        String host=((SipURI)routeURI).getHost();
                        
                        if ( stackIPAddress.equals(host)) {
                            routes.remove();
                            
                            if (ProxyDebug.debug)
                                ProxyDebug.println
				("Proxy, forwardRequest(), (STEP 6) we removed"+
                                " the first Route header field value: it contained"+
                                " the proxy address and a lr parameter");
                        }
                        else {
             
                            if (ProxyDebug.debug)
                                ProxyDebug.println
			       ("RequestForwarding, forwardRequest(), (STEP 6)"+
                                "lr parameter detected but no match...");
                        }
                    }
                    else
                          if (ProxyDebug.debug)
                    	ProxyDebug.println
			("RequestForwarding, forwardRequest(), (STEP 6)"+
                    	" no Postprocess routing information to do " + 
			"(the route has no lr parameter)...");
                }
                else {
             
                 if (ProxyDebug.debug)
                    ProxyDebug.println("RequestForwarding, forwardRequest(), (STEP 6)"+
                    " no Postprocess routing information to do (No routes detected)...");
                }
/*******************************************************************************/
/************** 7. Determine Next-Hop Address, Port, and Transport    **********/
/*******************************************************************************/
                
         /* the proxy
         applies the procedures listed in [4] as follows to determine
         where to send the request.  If the proxy has reformatted the
         request to send to a strict-routing element as described in
         step 6 above, the proxy MUST apply those procedures to the
         Request-URI of the request.  Otherwise, the proxy MUST apply
         the procedures to the first value in the Route header field, if
         present, else the Request-URI.  The procedures will produce an
         ordered set of (address, port, transport) tuples.
         Independently of which URI is being used as input to the
         procedures of [4], if the Request-URI specifies a SIPS
         resource, the proxy MUST follow the procedures of [4] as if the
         input URI were a SIPS URI.
          */
               if (ProxyDebug.debug)
                ProxyDebug.println("RequestForwarding, forwardRequest(), (STEP 7)"+
                " Determine Next-Hop Address, Port, and Transport will be done by the stack...");
                
/*******************************************************************************/
/**************  8. Add a Via header field value                      **********/
/*******************************************************************************/
                
         /* The proxy MUST insert a Via header field value into the copy
            before the existing Via header field values.
          */
                
                stackIPAddress=sipStack.getIPAddress();
		/*
                lps=sipStack.getListeningPoints();
                lp=(ListeningPoint)lps.next();
		*/
		lp = sipProvider.getListeningPoint();
                
                ViaHeader viaHeader=null;
                
                if ( clonedRequest.getMethod().equals(Request.CANCEL) ) {
                    // Branch Id will be assigned by the stack.
                    viaHeader=headerFactory.createViaHeader
                    (stackIPAddress,lp.getPort(),lp.getTransport(),null);
                    
                    if (clonedRequest.getMethod().equals(Request.CANCEL) ) {
                        // Cancel is hop by hop so remove all other via headers.
                        clonedRequest.removeHeader(ViaHeader.NAME);
                    }
                }
                else  {
                  
                    viaHeader=headerFactory.createViaHeader
                    (stackIPAddress,lp.getPort(),
                    lp.getTransport(),ProxyUtilities.generateBranchId());
                }
                
                if (viaHeader!=null)
                    clonedRequest.addHeader(viaHeader);
                
                 
                 if (ProxyDebug.debug)
                    ProxyDebug.println
		    ("RequestForwarding, forwardRequest(), (STEP 8)"+
                    " the proxy inserts a Via header field value into the copy"+
                    " before the existing Via header field values");
                
         /* Proxies choosing to detect loops have an additional constraint
         in the value they use for construction of the branch parameter.
         A proxy choosing to detect loops SHOULD create a branch
         parameter separable into two parts by the implementation.  The
         first part MUST satisfy the constraints of Section 8.1.1.7 as
         described above.  The second is used to perform loop detection
         and distinguish loops from spirals.
          */
                // Not yet implemented
                if (ProxyDebug.debug)
                    ProxyDebug.println
		    ("RequestForwarding, forwardRequest(), (STEP 8)"+
                    " Loop detection not implemented");

/****************************************************************************/
/*********** 9. Add a Content-Length header field if necessary     **********/
/****************************************************************************/
                
                try {
                    ContentTypeHeader contentTypeHeader=(ContentTypeHeader)
                    clonedRequest.getHeader(ContentTypeHeader.NAME);
                    if (contentTypeHeader==null) {
                        if (ProxyDebug.debug)
                        ProxyDebug.println
			("RequestForwarding, forwardRequest(),"+
                        " no Content-Type header,"+
                        " we don't stripe any parameters!!!");
                    }
                    else contentTypeHeader.removeParameter("msgr");
                    
                }
                catch (Exception e) {
                    ProxyDebug.println
		     ("RequestForwarding, forwardRequest(), Stripe"+
                    " Parameter failed!!!!");
                }
                
/****************************************************************************/
/*********** 10. Forward Request                                   **********/
/****************************************************************************/
            
                if (statefullForwarding) {
                    forwardRequestStatefully(sipProvider,
                    clonedRequest,request,serverTransaction);
                }
                else {
                    forwardRequestStatelessly(sipProvider,
                    clonedRequest,request,serverTransaction);
                }                
/***************************************************************************/
/********** 11. Set timer C                                       **********/
/***************************************************************************/
                
                // Not Implemented....
            }
        }
        catch (Exception ex){
            try{
                if (ProxyDebug.debug) {
                    ProxyDebug.println
		    ("RequestForwarding, forwardRequest(), "+
                    " internal error, "+
                    "exception raised:");
                    ProxyDebug.logException(ex);
                }
                // This is an internal error:
                // Let's return a 500 SERVER_INTERNAL_ERROR
                Response response=messageFactory.createResponse
                (Response.SERVER_INTERNAL_ERROR,request);
                if (serverTransaction!=null)
                    serverTransaction.sendResponse(response);
                else sipProvider.sendResponse(response);
                
                if (ProxyDebug.debug)
                    ProxyDebug.println
		    ( "RequestForwarding, forwardRequest(), " +
		    " 500 SERVER_INTERNAL_ERROR replied:\n"+
                     response.toString());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }


   /** Forward the request statefully.
    *@param sipProvider -- sip provider to forward request.
    *@param clonedRequest -- cloned request to forward.
    *@param originalRequest -- incoming request 
    *@param serverTransaction -- server transaction used to fwd the request.
    */
    
    
    private void forwardRequestStatefully
	(SipProvider sipProvider,
        Request clonedRequest,
	Request originalRequest,
	ServerTransaction serverTransaction) {
        MessageFactory messageFactory=proxy.getMessageFactory();
        HeaderFactory headerFactory=proxy.getHeaderFactory();
        AddressFactory addressFactory=proxy.getAddressFactory();
        SipStack sipStack=proxy.getSipStack();
	if (ProxyDebug.debug) {
		ProxyDebug.println("serverTransaction =  " +
				serverTransaction);
		if (serverTransaction != null) 
		    ProxyDebug.println("dialog =  " +
				serverTransaction.getDialog());
	}
        
        try{
           /* A stateful proxy MUST create a new client transaction for this
             request as described in Section 17.1 and instructs the
             transaction to send the request using the address, port and
             transport determined in step 7
            */
/************************************************************************/ 
/************************* SERVER TRANSACTION CHECK *********************/ 
/************************************************************************/ 
            if (serverTransaction==null 
		&& ! clonedRequest.getMethod().equals(Request.MESSAGE) ) {
		// dont create a server transaction for MESSAGE -
		// just forward the request statefully through a new
		// client transaction.
                if (ProxyDebug.debug)
                  ProxyDebug.println
                    ("RequestForwarding, forwardRequestStatefully(),"+
                   " the cloned request does not have a server transaction,"+
                   " so we drop the request!");
                 return;
            }
            
             if (originalRequest.getMethod().equals(Request.CANCEL) ) {
                // 487 Request Terminated to reply:
                Response response=messageFactory.createResponse
                (Response.REQUEST_TERMINATED,originalRequest);
                CSeqHeader cSeqHeader=(CSeqHeader)response.getHeader(CSeqHeader.NAME);
                cSeqHeader.setMethod("INVITE");
                serverTransaction.sendResponse(response);
             
                if (ProxyDebug.debug)
                ProxyDebug.println
		("Proxy, processRequest(), 487 Request Terminated"+
                " replied to the CANCEL:\n" +
                response.toString());
            }
            
                
/***************************************************************************/ 
/******* FROM TAG UPDATE FOR METHODS DIALOG CREATOR   **********************/ 
/***************************************************************************/
            
      // Important step: update the From tag ONLY for statefull forwarding
      FromHeader 
	fromHeader=(FromHeader)clonedRequest.getHeader(FromHeader.NAME);
     ToHeader toHeader=(ToHeader)clonedRequest.getHeader(ToHeader.NAME);
     CSeqHeader cseqHeader=(CSeqHeader)clonedRequest.getHeader(CSeqHeader.NAME);
     String method=clonedRequest.getMethod();       
     
/**************************************************************************/ 
/******               DIALOG CHECK                   **********************/ 
/**************************************************************************/            

	// Note that the proxy server is actually implemented as a back
	// to back User Agent.
     
           Dialog dialog= null;
	   if (serverTransaction != null) 
		dialog = serverTransaction.getDialog();
	   DialogState dialogState = null;
	   if (dialog != null ) 
	      dialogState=dialog.getState();
           if ( dialogState==null )  {
                 if (ProxyDebug.debug)
                    ProxyDebug.println
		    ("RequestForwarding, forwardRequestStatefully(),"+
                    " the dialog state is null, so we have to"+
                    " forward the request using"+
                    " a new clientTransaction");
                 ClientTransaction clientTransaction =
                    sipProvider.getNewClientTransaction
                    (clonedRequest);
                 clientTransaction.sendRequest();
                    
                 if (ProxyDebug.debug)
                        ProxyDebug.println
                        ("RequestForwarding, forwardRequestStatefully(),"+
                        ", cloned request forwarded statefully:\n "
                        +clonedRequest);
                 
		 if (dialog != null)  {
                   TransactionsMapping transactionsMapping=
		      (TransactionsMapping)
		    serverTransaction.getDialog().
				getApplicationData();
                    transactionsMapping.addMapping
			(serverTransaction,clientTransaction);
		}
                 return;
           }

	   // Dialog has already been assigned so transactions map
	   // should also be there.
           ProxyDebug.println("Dialog checked.");                
           
           // Special processing because the SUBSCRIBE is 
	   // hop by hop for a presence server


	   // Henrik: No special processing needed for SUBSCRIBE:
	   //   either this presenceserver is the PA of the PUA
	   //   and the subscribe ends here, or the subscribe is
	   //   forwarded like any other message.

	   /*
	     // Special processing because the SUBSCRIBE is 
	     // hop by hop for a presence server
	     if ( proxy.isPresenceServer() )    {
		this.forwardIMPresenceRequest ( 
				sipProvider, 
				serverTransaction,
				clonedRequest, 
			        originalRequest,
				dialog);
		return;
		} else {
	   */

		this.forwardRequestThroughDialog(
			sipProvider,
			serverTransaction,
			clonedRequest,
			dialog);
		return;

           
        } catch (Exception ex){
            try{
                if (ProxyDebug.debug) {
                    ProxyDebug.println
		     ("RequestForwarding, forwardRequestStatefully(), "+
                    " internal error, "+
                    "exception raised:");
                    ProxyDebug.logException(ex);
		    ex.printStackTrace();
		    System.exit(0);
                }
                // This is an internal error:
                // Let's return a 500 SERVER_INTERNAL_ERROR
                Response response=messageFactory.createResponse
                (Response.SERVER_INTERNAL_ERROR,originalRequest);
                if (serverTransaction!=null)
                    serverTransaction.sendResponse(response);
                else sipProvider.sendResponse(response);
                
                if (ProxyDebug.debug)
                    ProxyDebug.println(
                    "RequestForwarding, forwardRequestStatefully(), " +
		    " 500 SERVER_INTERNAL_ERROR replied:\n"+
                    response.toString());
            }
            catch (Exception e){
                e.printStackTrace();
            }
       }
    }

    /** Special processing for Instant Messaging and Presence
     * messages.
     *
     * @deprecated No special processing needed for SUBSCRIBE,
	      either this presenceserver is the PA of the PUA
	      and the subscribe ends here, or the subscribe is
	      forwarded like any other message.
     */
    
     private void forwardIMPresenceRequest(
		SipProvider sipProvider,
		ServerTransaction serverTransaction,
		Request clonedRequest, 
		Request originalRequest,
	 	Dialog	dialog) throws Exception { /*
                TransactionsMapping transactionsMapping= (TransactionsMapping) 
				serverTransaction.getDialog().
				getApplicationData();
	       CSeqHeader cseqHeader = (CSeqHeader) 
			clonedRequest.getHeader(CSeqHeader.NAME);
               if (  cseqHeader.getSequenceNumber()==1 &&
                     clonedRequest.getMethod().equals(Request.SUBSCRIBE) ){
                   if (ProxyDebug.debug)
                       ProxyDebug.println
			("RequestForwarding, forwardRequestStatefully(),"+
                       " It is a first SUBSCRIBE, so we have to"+
                       " forward the request using"+
                       " a new clientTransaction");
                   ClientTransaction clientTransaction =
                   sipProvider.getNewClientTransaction
                   (clonedRequest);
                   clientTransaction.sendRequest();
                   
                   if (ProxyDebug.debug)
                       ProxyDebug.println
                       ("RequestForwarding, forwardRequestStatefully(),"+
                       ", cloned request forwarded statefully:\n "
                       +clonedRequest);
                   
		   if (serverTransaction.getDialog() != null) {
                      transactionsMapping.addMapping
			(serverTransaction,clientTransaction);
		   }
                   return;
               } else if (clonedRequest.getMethod().equals(Request.NOTIFY)) {
				  // Forward the notify to all the watchers.
				  String url = 
				       ( (FromHeader) clonedRequest.getHeader(FromHeader.NAME)).
					getAddress().getURI().toString();
					if(url.indexOf(';')!=-1)
						url=url.substring(0,url.indexOf(';'));
				  PresenceServer ps  = proxy.getPresenceServer();
				  PresentityManager pm = ps.getPresentityManager();
				  Vector presentities = pm.getSubscriberList(url);
				  //System.out.println("GOT A NOTIFY...."  + presentities.size());                  
				  // get all the dialogs for the watchers.
				  for (int i = 0; i < presentities.size() ; i++) {
						Subscriber s = (Subscriber) presentities.elementAt(i);
						if (ProxyDebug.debug) {
			                             ProxyDebug.println
							("Forwarding request to Subscriber nb "+
								i+ " : "+s.getSubscriberURL());
						}
						Dialog d = s.getDialog();
						Request r = d.createRequest(Request.NOTIFY);
						// set the content of the outgoing request.
		                Object content=clonedRequest.getContent();
		                if(content!=null)
		                    r.setContent(content,
			       				(ContentTypeHeader) clonedRequest.getHeader
									(ContentTypeHeader.NAME));
						ClientTransaction ct = 
		                	sipProvider.getNewClientTransaction (r);
						d.sendRequest(ct);
				  }
                  return;
	       }  else if (clonedRequest.getMethod().equals(Request.MESSAGE)) {
			if (dialog != null) {
			    this.forwardRequestThroughDialog(sipProvider,
			     	serverTransaction, 
				clonedRequest,
				dialog);
			} else {

				forwardRequestStatelessly(sipProvider,
    					clonedRequest, originalRequest,
					serverTransaction);

                   		// ClientTransaction clientTransaction =
                   		// sipProvider.getNewClientTransaction
                   	        //		(clonedRequest);
                   		// clientTransaction.sendRequest();
			}
		}
               //Means that the message is not a presnece message
               else{
                    this.forwardRequestThroughDialog(
			sipProvider,
			serverTransaction,
			clonedRequest,
			dialog);	
               }
						   */     
         } 
    

 	/** Forward a request statefully through a dialog.
	*/
	private void forwardRequestThroughDialog 
	    (SipProvider sipProvider,
	    ServerTransaction  serverTransaction,
	    Request clonedRequest,
	    Dialog dialog) throws Exception {
           TransactionsMapping transactionsMapping= (TransactionsMapping) 
				serverTransaction.getDialog().
				getApplicationData();
		
           if (ProxyDebug.debug) {
              ProxyDebug.println("Printing TransactionsMappingTable...");                
            }
           
           if (ProxyDebug.debug)
                transactionsMapping.printTransactionsMapping();
           
            if ( clonedRequest.getMethod().equals("CANCEL") ) {
                 Transaction firstTransaction=dialog.getFirstTransaction();
                  if (firstTransaction==null) {
                     throw new Exception
			("ERROR, RequestForwarding, the first transaction"+
                  	" for the  dialog is null"); 
           	   }
		   if ( firstTransaction instanceof ClientTransaction ) {
			if (ProxyDebug.debug)
			   ProxyDebug.println("CANCEL IGNORED");
			return;
		   }
                   ServerTransaction firstServerTransaction=
			(ServerTransaction)firstTransaction;
                   Vector clientTransactions =
                            transactionsMapping.getClientTransactions
			    (firstServerTransaction);
                   if (clientTransactions==null || 
			clientTransactions.isEmpty()) {
                       throw new Exception
			("RequestForwarding, The peer first client " +
			" transaction(s) for the first server transaction " +
			 " is null (CANCEL)"); 
                   }
                   
                   for (Enumeration e = clientTransactions.elements(); 
			e.hasMoreElements();) {
                        ClientTransaction ct = 
				(ClientTransaction)e.nextElement();
                    
                    // check if the client transaction can be canceled.
                    if (ct.getState().equals(TransactionState.COMPLETED) ||
                    ct.getState().equals(TransactionState.TERMINATED)){
                        continue;
                    }
                    Request cancelRequest
                    =ct.createCancel();   
                    
                    ClientTransaction clientTransaction=
                    sipProvider.getNewClientTransaction
                    (cancelRequest);
                    clientTransaction.sendRequest();
                  
                    if (ProxyDebug.debug)
                        ProxyDebug.println
			("RequestForwarding, forwardRequestStatefully()"+
                        ", created CANCEL sent statefully (using a new client"+
                        " transaction):\n "+cancelRequest);
                   }
                   return;
           }
           
           if (ProxyDebug.debug) {
              ProxyDebug.println("Getting peer dialog...");                
            }

	   Dialog thisDialog = serverTransaction.getDialog();
	   Dialog peerDialog = transactionsMapping.getPeerDialog
				(serverTransaction);

	   if (peerDialog == null) {
		ClientTransaction 
			ct = sipProvider.getNewClientTransaction
				(clonedRequest);
		ct.sendRequest();
		transactionsMapping.addMapping(serverTransaction,ct);
	   } else if ( clonedRequest.getMethod().equals("ACK") ) {
                 peerDialog.sendAck(clonedRequest);
                        
                   if (ProxyDebug.debug)
                        ProxyDebug.println
		       ("RequestForwarding, forwardRequestStatefully()"+
                        ", cloned ACK forwarded statefully " +
			" (using the dialog from the"+
                        " first client transaction):\n "+clonedRequest);
                    
            } else {
                    
                    Request dialogRequest=
			peerDialog.createRequest(clonedRequest.getMethod());
                    Object content=clonedRequest.getContent();
                    if (content!=null) {
                        ContentTypeHeader contentTypeHeader=
			(ContentTypeHeader)clonedRequest.getHeader(
                        	ContentTypeHeader.NAME);
                        if (contentTypeHeader!=null)
                            dialogRequest.setContent(content,contentTypeHeader);
                    }
                     
                    // Copy all the headers from the original request to the 
                    // dialog created request:
                    try{
                        ListIterator l=clonedRequest.getHeaderNames();
                        while (l.hasNext() ) {
                             String name=(String)l.next();
                             Header header=dialogRequest.getHeader(name);
                             if (header==null  ) {
                                ListIterator li=clonedRequest.getHeaders(name);
                                if (li!=null) {
                                    while (li.hasNext() ) {
                                        Header  h=(Header)li.next();
                                        dialogRequest.addHeader(h);
                                    }
                                }
                             }
                             else {
                                 if ( header instanceof ViaHeader) {
                                     ListIterator li=
					clonedRequest.getHeaders(name);
                                     if (li!=null) {
                                         dialogRequest.removeHeader(name);
                                         Vector v=new Vector();
                                         while (li.hasNext() ) {
                                             Header  h=(Header)li.next();
                                             v.addElement(h);
                                         }
                                         for (int k=(v.size()-1);k>=0;k--) {
                                             Header  h=(Header)v.elementAt(k);
                                             dialogRequest.addHeader(h);
                                         }
                                     }
                                 }
                             }
                        }       
                    }
                    catch(Exception e) {
                        if (ProxyDebug.debug)
                        ProxyDebug.println
			("RequestForwarding, forwardRequestStatefully()"+
                        ", error trying to copy the headers from " +
			 " the original request");
                        e.printStackTrace();
                    }
                    if (ProxyDebug.debug) {
                      ProxyDebug.println("Sending request...");                
                    }
                    ClientTransaction clientTransaction =
			sipProvider.getNewClientTransaction(dialogRequest);
                    peerDialog.sendRequest(clientTransaction);
                    if (ProxyDebug.debug)
                        ProxyDebug.println
			("RequestForwarding, forwardRequestStatefully()"+
                        ", cloned request forwarded statefully " +
			"(using the dialog from the"+
                        " first client transaction):\n "+dialogRequest);
                        transactionsMapping.addMapping
				(serverTransaction,clientTransaction);
                }
	}


      private void 
	forwardRequestStatelessly(SipProvider sipProvider,
    	Request clonedRequest, Request originalRequest,
	ServerTransaction serverTransaction) {

        MessageFactory messageFactory=proxy.getMessageFactory();
        HeaderFactory headerFactory=proxy.getHeaderFactory();
        AddressFactory addressFactory=proxy.getAddressFactory();
        SipStack sipStack=proxy.getSipStack();
        
        try{
            
            // We forward statelessly:
            // It means the Request does not create dialogs...
            sipProvider.sendRequest(clonedRequest);
            if (ProxyDebug.debug)
                ProxyDebug.println
                ("RequestForwarding, forwardRequestStatelessly(), "+
                " cloned request forwarded statelessly:\n"
		+clonedRequest.toString());
        } catch (Exception ex){
            try{
                if (ProxyDebug.debug) {
                    ProxyDebug.println
		    ("RequestForwarding, forwardRequestStatelessly(), "+
                    " internal error, "+
                    "exception raised:");
                    ProxyDebug.logException(ex);
                }
                // This is an internal error:
                // Let's return a 500 SERVER_INTERNAL_ERROR
                Response response=messageFactory.createResponse
                (Response.SERVER_INTERNAL_ERROR,originalRequest);
                if (serverTransaction!=null)
                    serverTransaction.sendResponse(response);
                else sipProvider.sendResponse(response);
                
                if (ProxyDebug.debug)
                    ProxyDebug.println(
                    "RequestForwarding, forwardRequestStatelessly(), " +
		    " 500 SERVER_INTERNAL_ERROR replied:\n"+
                    response.toString());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    
}

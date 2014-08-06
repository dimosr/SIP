/*
 * IMPublishProcessing.java
 */

package gov.nist.sip.instantmessaging.presence;

import gov.nist.javax.sip.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import java.util.*;
import gov.nist.sip.instantmessaging.*;


/**
 * The Publish is NOT supported by the Jain SIP 1.1.
 * This is here for experimental purposes.
 * @author Henrik Leion
 * @version 0.1
 */

public class IMPublishProcessing {

    private IMUserAgent imUA;
    private int callIdCounter;
    /** A unique id used to identify this entity in a pidf-document  **/
    private String entity;
  
    public IMPublishProcessing(IMUserAgent imUA) {
	this.imUA = imUA;
	this.callIdCounter = 0;
	this.entity = "NistSipIM_" + Utils.generateTag();
    }


  
    public void sendPublish(String localURI,String status) {
        try {
            DebugIM.println();
            DebugIM.println("Sending PUBLISH in progress");
            int proxyPort=imUA.getProxyPort();
            String proxyAddress=imUA.getProxyAddress();
            String imProtocol=imUA.getIMProtocol();
            SipStack sipStack=imUA.getSipStack();
            SipProvider sipProvider=imUA.getSipProvider();
            MessageFactory messageFactory=imUA.getMessageFactory();
            HeaderFactory headerFactory=imUA.getHeaderFactory();
            AddressFactory addressFactory=imUA.getAddressFactory();
            

	    // Request-URI:
	    if(localURI.startsWith("sip:"))
		localURI=localURI.substring(4,localURI.length());
            SipURI requestURI=addressFactory.createSipURI(null,localURI);
            requestURI.setPort(proxyPort);
            requestURI.setTransportParam(imProtocol);
          
	    //  Via header
            String branchId=Utils.generateBranchId();
            ViaHeader viaHeader=headerFactory.createViaHeader(
                imUA.getIMAddress(),imUA.getIMPort(),imProtocol,branchId);
            Vector viaList=new Vector();
            viaList.addElement(viaHeader);
        

	     // To header:
	    System.out.println("XXX localURI=" + localURI);
	    Address localAddress = addressFactory.createAddress("sip:"+localURI);
            ToHeader toHeader =
		headerFactory.createToHeader(localAddress,null);

	    // From header:  
	    String localTag = Utils.generateTag();
	    FromHeader fromHeader =  
		headerFactory.createFromHeader(localAddress,localTag);

	    
	    // Call-ID:
	    CallIdHeader callIdHeader =
		headerFactory.createCallIdHeader(callIdCounter+localURI);

	    // CSeq:
	    CSeqHeader cseqHeader =
		headerFactory.createCSeqHeader(1,"PUBLISH");
               
	    
	    // MaxForwards header:
            MaxForwardsHeader maxForwardsHeader =
		headerFactory.createMaxForwardsHeader(70);
           
	    //Create Request
	    Request request=messageFactory.createRequest(requestURI, "PUBLISH",
		    callIdHeader,cseqHeader,fromHeader,toHeader,viaList,maxForwardsHeader);
          
	    

	    // Expires header: (none, let server chose)
          
	    
	    // Event header:
	    Header header=headerFactory.createHeader("Event","presence");
            request.setHeader(header);

	 
	    // Content and Content-Type header:
	    String basic;
	    if (status.equals("offline"))
		basic="closed";
	    else
		basic="open";

	    String content = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"" +
		" entity=\"" + localURI  + "\">\n" +
		" <tuple id=\"" + entity + "\">\n" +
		"  <status>\n" +
		"   <basic>" + basic + "</basic>\n" +
		"  </status>\n" + 
		"  <note>" + status + "</note>\n" +
		" </tuple>\n" +
		"</presence>";
	    
	    ContentTypeHeader contentTypeHeader = 
		headerFactory.createContentTypeHeader("application", "pidf+xml");
	    request.setContent(content, contentTypeHeader);
                

	    // Content-Length header: 
	    ContentLengthHeader contentLengthHeader =
		headerFactory.createContentLengthHeader(content.length());
	    request.setContentLength(contentLengthHeader);
	        

	    // Send request
	    ClientTransaction clientTransaction=sipProvider.getNewClientTransaction(request);
	    clientTransaction.sendRequest();
             
	}
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }



}
	

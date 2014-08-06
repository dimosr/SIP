/*
 * Binding.java
 *
 * Created on June 27, 2002, 2:04 PM
 */

package gov.nist.sip.proxy.registrar;

import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*; 

/**
 *
 * @author  deruelle
 * @version 1.0
 */
class Binding {
    protected boolean toExport;
    protected javax.sip.address.URI requestURI;
    protected ContactHeader contactHeader;
    protected FromHeader    fromHeader;
    protected ToHeader    toHeader;
    protected String callId;
    protected long cseq;
    protected boolean toDelete;
    protected long expiresTime;
    protected String userName;
    protected String displayName;
    protected String key;
    

    protected ExportedBinding exportBinding() {
	if (! this.toExport) return null;
	ExportedBinding retval = new ExportedBinding();
	retval.requestURI = this.requestURI.toString();
	if (this.fromHeader != null) 
	   retval.fromAddress = this.fromHeader.getAddress().toString();
	if (this.toHeader != null)
	   retval.toAddress = this.toHeader.getAddress().toString();
	if (this.contactHeader != null) 
	   retval.contactAddress = 
			this.contactHeader.getAddress().toString();
	return retval;

    }
    
    /** Creates new Binding */
    protected Binding(javax.sip.address.URI requestURI,
	ContactHeader contactHeader,long cseq,long expiresTime, 
	FromHeader fromHeader, ToHeader toHeader) {
	if (contactHeader == null) new Exception().printStackTrace();
	this.requestURI = requestURI;
        this.contactHeader=contactHeader;
        this.cseq=cseq;
        this.toDelete=false;   
        this.expiresTime=expiresTime;
	this.toHeader  = toHeader;
	this.fromHeader = fromHeader;
	// TODO -- need some access control on individual entries.
	this.toExport = true;
    }
    
    protected void print() {
        System.out.println("- requestURI:"+requestURI.toString());
        System.out.print("- contactHeader:"+contactHeader.toString());
        System.out.println("- callId:"+callId );
        System.out.println("- cseq:"+cseq );
        System.out.println("- expiresTime:"+expiresTime );
        try {
            Address address=fromHeader.getAddress();
            javax.sip.address.URI  uri=address.getURI(); 
            String  result=uri.toString();
            System.out.println("- from URL:"+result);
        }
        catch(Exception e) {
            System.out.println("DEBUG, Binding, print(), exception raised:");
            e.printStackTrace();
            
        }
    }

    protected String getXMLRepresentation() {
	StringBuffer retval = new StringBuffer();
	retval.append("<AGENT>\n");
	retval.append("callId="+callId+"\n");
	retval.append("requestURI="+requestURI.toString()+"\n");
	retval.append("contact="+contactHeader.toString()+"\n");
	retval.append("from="+fromHeader.toString()+"\n");
	retval.append("expires="+expiresTime+"\n");
	retval.append("</AGENT>\n");
	return retval.toString();
    }
    
}

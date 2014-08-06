/*
 * RegistrationDomainThread.java
 *
 * Created on March 25, 2003, 11:05 AM
 */

package gov.nist.sip.proxy.registrar;

import gov.nist.sip.proxy.*;
import java.io.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import java.util.*;
//ifdef SIMULATION
/*
import sim.java.net.*;
//endif
*/

/**
 *
 * @author  deruelle
 */
public class RegistrationDomainThread implements Runnable{
    
    protected Domain domain;
    protected Proxy proxy;
    protected long callIdCounter;
    protected int cseq;
    protected boolean STOP;
    
    /** Creates a new instance of RegistrationDomainThread */
    public RegistrationDomainThread(Proxy proxy, Domain domain) {
        this.domain=domain;
        this.proxy=proxy;
        callIdCounter=0;
        cseq=0;
        STOP=false;
    }
    
    public void sendDomainRegistration() {
        try {
            ProxyDebug.println();
            if (domain.hostPort!=null)
                ProxyDebug.println
		("RegistrationDomainThread, sendDomainRegistration(), sending REGISTER"+
                " (SignIn) in progress to the proxy: "+domain.hostName+":"+domain.hostPort);
            else
                ProxyDebug.println
		("RegistrationDomainThread, sendDomainRegistration(), sending REGISTER"+
                " (SignIn) in progress to the proxy: "+domain.hostName);
            
            SipStack sipStack=proxy.getSipStack();
            String stackIPAddress=sipStack.getIPAddress();
	    // Get a default SIP provider for the domain
            SipProvider sipProvider=proxy.getSipProvider();
            MessageFactory messageFactory=proxy.getMessageFactory();
            HeaderFactory headerFactory=proxy.getHeaderFactory();
            AddressFactory addressFactory=proxy.getAddressFactory();
            ProxyUtilities proxyUtilities=proxy.getProxyUtilities();
            
           
            // Request-URI:
            SipURI requestURI=addressFactory.createSipURI(null,domain.hostName);
            requestURI.setMAddrParam(domain.hostName);
            if (domain.hostPort!=null)
                requestURI.setPort(Integer.valueOf(domain.hostPort).intValue());
            
            // Call-ID:
            callIdCounter++;
            CallIdHeader callIdHeader=headerFactory.createCallIdHeader(
            "nist-sip-proxy-register-callId"+callIdCounter);
            
            // CSeq:
            cseq++;
            CSeqHeader cseqHeader=headerFactory.createCSeqHeader(cseq,"REGISTER");
            
            Iterator it  =sipStack.getListeningPoints();
            if (it==null) return;
            ListeningPoint lp = (ListeningPoint) it.next();
            if (lp==null) return;
            
            // To header:
            SipURI toURI=addressFactory.createSipURI(null,domain.from);
           // toURI.setPort(lp.getPort());
            Address toAddress=addressFactory.createAddress(toURI);
            //String toTag=Utils.generateTag();
            ToHeader toHeader=headerFactory.createToHeader(toAddress,null);
            
            // From Header:
            SipURI fromURI=addressFactory.createSipURI(null,domain.from);
          //  fromURI.setPort(lp.getPort());
            Address fromAddress=addressFactory.createAddress(fromURI);
            String fromTag=proxyUtilities.generateTag();
            FromHeader fromHeader=headerFactory.createFromHeader(fromAddress,fromTag);
            
            
            //  Via header
            ViaHeader viaHeader=proxy.getStackViaHeader(); 
            Vector viaList=new Vector();
            viaList.addElement(viaHeader);
            
            // MaxForwards header:
            MaxForwardsHeader maxForwardsHeader=headerFactory.createMaxForwardsHeader(70);
            
            
            Request request=messageFactory.createRequest(requestURI,"REGISTER",
            callIdHeader,cseqHeader,fromHeader,toHeader,viaList,maxForwardsHeader);
            
            ClientTransaction clientTransaction=sipProvider.getNewClientTransaction(request);
            if (clientTransaction==null) {
                ProxyDebug.println("RegistrationDomainThread, sendDomainRegistration(), "+
                " ERROR, the client transaction is null for the"+
                " request"+request);    
                return;
            }
            
            // Contact header:
            ContactHeader contactHeader=proxy.getStackContactHeader(); 
            request.setHeader(contactHeader);
           
            clientTransaction.sendRequest();
            ProxyDebug.println("RegistrationDomainThread, sendDomainRegistration(),"+
            " REGISTER sent:\n"+request);
            ProxyDebug.println();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
     /** 
     */    
    public void  run() {  
        ProxyDebug.println("Proxy sending REGISTER............. ");
        try {
           while (!STOP) {
                sendDomainRegistration();
//ifdef  SIMULATION
/*
		SimThread.sleep(36*60000);
//else
*/
                Thread.sleep(36*60000);
//endif
//
           }
          
        }
        catch(Exception e) {
            
        }
    }
    
    
}

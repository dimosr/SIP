package gov.nist.sip.proxy;

import java.util.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.sip.address.*;
/** Utilities for the proxy.
*
*@version  JAIN-SIP-1.1
*
*@author Olvier Deruelle <deruelle@nist.gov>  <br/>
*@author M. Ranganathan <mranga@nist.gov> <br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ProxyUtilities {
 
    protected Proxy proxy;
    public static final String BRANCH_MAGIC_COOKIE = "z9hG4bK";
    
    protected ProxyUtilities(Proxy proxy) {
        this.proxy=proxy;
    }
    
    	/** Generate a cryptographically random identifier that can be used
	* to generate a branch identifier.
	*
	*@return a cryptographically random gloablly unique string that
	*	can be used as a branch identifier.
	*/
	public static String generateBranchId() {
          String b =  new Integer((int)(Math.random() * 10000)).toString() + 
		System.currentTimeMillis();
          try {
              MessageDigest messageDigest = MessageDigest.getInstance("MD5");
              byte bid[] = messageDigest.digest(b.getBytes());
		// cryptographically random string.
		// prepend with a magic cookie to indicate we
		// are bis09 compatible.
              return 	BRANCH_MAGIC_COOKIE +
			toHexString(bid);
           } catch ( NoSuchAlgorithmException ex ) {
	      
	      return null;
           }
	}
    
    
     /**
     * to hex converter
     */
    private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6',
                                          '7', '8', '9', 'a', 'b', 'c', 'd',
                                          'e', 'f' };
 
    /**
     * convert an array of bytes to an hexadecimal string
     * @return a string
     * @param b bytes array to convert to a hexadecimal
     * string
     */
 
    public static String toHexString(byte b[]) {
        int pos = 0;
        char[] c = new char[b.length*2];
        for (int i=0; i< b.length; i++) {
            c[pos++] = toHex[(b[i] >> 4) & 0x0F];
            c[pos++] = toHex[b[i] & 0x0f];
        }
        return new String(c);
    }  
    
    
    
    public static String generateTag() {
            return new Integer((int)(Math.random() * 10000)).toString();
    }
    
    protected boolean matchProxyAddress(Request request) throws Exception{
        javax.sip.address.URI uri=request.getRequestURI();
        SipStack sipStack=proxy.getSipStack();
        if (uri==null) return false;
        else {
            SipURI sipURI=((SipURI)uri);
            
            String stackIPAddress=sipStack.getIPAddress();
            String host=sipURI.getHost();
            
            return stackIPAddress.equals(host);
           
        }
    }
    
    public static  int length(Message message) {
        int cpt=0;
        try{
            ListIterator l=message.getHeaders(ViaHeader.NAME);
            while (l.hasNext() ) {
                    l.next();
                    cpt++;
            }
            return cpt;
        }
        catch(Exception e) { return cpt;}
    }
    
    public static boolean hasTopViaHeaderProxy(SipStack sipStack,Message message) {
        ListIterator viaList=message.getHeaders(ViaHeader.NAME);
        if (viaList==null ||  length(message)==0) 
               return false;
        
        String stackIPAddress=sipStack.getIPAddress();
        ViaHeader viaHeader=(ViaHeader) viaList.next();
        
        if (  viaHeader.getHost().equals(stackIPAddress) ) {
            Iterator lps=sipStack.getListeningPoints();
            while (lps!=null && lps.hasNext() ) {
                ListeningPoint lp = (ListeningPoint)lps.next();
                int port = lp.getPort();
                if (viaHeader.getPort()==port)
                    return true;
            }
        }
        return false;
    }
    
    public static void printTransaction(Transaction transaction) {
        if (transaction==null) {
            ProxyDebug.println
		("DEBUG TRANSACTION INFO: the transaction is null ");
                return;
        }
        
        if ( transaction instanceof ServerTransaction) {
            ServerTransaction serverTransaction=(ServerTransaction)transaction;
            ProxyDebug.println
		("DEBUG TRANSACTION INFO: here is the "+
                 " server transaction: "+serverTransaction);
               
            ProxyDebug.println
                    ("DEBUG INFO: Its dialog is: "+serverTransaction.getDialog());
        }
        else 
        if ( transaction instanceof ClientTransaction) {
            ClientTransaction clientTransaction=(ClientTransaction)transaction;
            ProxyDebug.println
		("DEBUG TRANSACTION INFO: here is the "+
                 " client transaction: "+clientTransaction);
               
            ProxyDebug.println
                    ("DEBUG TRANSACTION INFO: Its dialog is: "+clientTransaction.getDialog());
        }
    }
    
   
    
}

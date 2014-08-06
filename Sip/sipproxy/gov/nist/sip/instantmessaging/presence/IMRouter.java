/*
 * IMRouter.java
 *
 * Created on July 28, 2002, 12:47 PM
 */

package gov.nist.sip.instantmessaging.presence;


import javax.sip.message.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.*;
import java.util.*;

import gov.nist.sip.instantmessaging.*;
/**
 *
 * @author  olivier
 * @version 1.0
 */
public class IMRouter implements Router {
    
    protected IMHop defaultRoute;
    protected SipStack stack;
    
        /**
         * Creates new IMRouter
         */
    public IMRouter(SipStack sipStack, String defaultRoute) {
        if (defaultRoute!=null)
            this.defaultRoute = new IMHop(defaultRoute);
        this.stack = sipStack;
    }
    
    
    public IMHop getNextHop(ListIterator routes) throws IllegalArgumentException{
        try{
            while (routes.hasNext() ) {
                RouteHeader  routeHeader = (RouteHeader) routes.next();
                Address routeAddress=routeHeader.getAddress();
                SipURI sipURI=(SipURI)routeAddress.getURI();
               
                String host = sipURI.getHost();
                String transport = sipURI.getTransportParam();
                int port = sipURI.getPort();
                if (port == -1) {
                    port = 5060;
                }
                if (transport==null) transport="UDP";
                
                // Dont want to route to myself.
                if (stack.getIPAddress().equals(host) && checkPort(port) ) {
                    DebugIM.println("DEBUG, IMRouter, getNextHop(), " +
                    "The RouteHeader address matches the proxy, we remove it!");
                    // Let'take the next one:
                    routes.remove();
                }
                else {
                    IMHop hop = new IMHop(host,port,transport);
                    return hop;
                }
            }
            return null;
        }
        catch(Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    
    public boolean checkPort(int port) {
        Iterator lps=stack.getListeningPoints();
        if (lps==null) return false;
        while(lps.hasNext()) {
            ListeningPoint lp=(ListeningPoint)lps.next();
            if (lp.getPort()==port) return true;
        }
        return false;
    }
    
    
    /**
     * Return  addresses for default proxy to forward the request to.
     * Thelist is organized in the following priority.
	 * If the request contains a Route Header it is used to construct
	 * the first element of the list.
         * If the requestURI refers directly to a host, the host and port
         * information are extracted from it and made the next hop on the
         * list. 
	 * If the default route has been specified, then it is used
	 * to construct the next element of the list.
         *@param method is the method of the request.
         *@param requestURI is the request URI of the request.
         */
    public ListIterator getNextHops(Request request)
    throws IllegalArgumentException {
        LinkedList nextHops=new LinkedList();
        ListIterator routes = request.getHeaders(RouteHeader.NAME);
        
        if (routes!=null) {
            Hop nextHop=getNextHop(routes);
            if (nextHop!=null) {
                nextHops.add(nextHop);
            }
        }
        
      
        URI requestURI=request.getRequestURI();
        if (requestURI instanceof SipURI) {
            String mAddr=((SipURI)requestURI).getMAddrParam();
            if ( mAddr!=null){
                try {
                    String mAddrTransport=((SipURI)requestURI).getTransportParam();
                    if (mAddrTransport==null) mAddrTransport="UDP";
                    int mAddrPort=((SipURI)requestURI).getPort();
                    if (mAddrPort==-1) mAddrPort=5060;
                    IMHop mAddrHop=new IMHop(mAddr,mAddrPort,mAddrTransport);
                    if (mAddrHop!=null) nextHops.add(mAddrHop);
                    DebugIM.println
                    ("DEBUG, IMRouter, getNextHops(), One hop added: Request URI maddr parameter!");
                }
                catch(Exception e) {
                    throw new IllegalArgumentException("ERROR, IMRouter, pb to add the maddr hop");
                }
            }
/** The RequestUri should be the Address of Record(AOR) (bug reported by
* Gaurav Khandpur
            else {
                SipURI sipURI=(SipURI)requestURI;
                String host = sipURI.getHost();
                int port = sipURI.getPort();
                if (port == -1) {
                    port = 5060;
                }
                String transport = sipURI.getTransportParam();
                if (transport==null) transport="UDP";
                IMHop requestURIHop = new IMHop(host,port,transport);
                nextHops.add(requestURIHop);
            }
**/
        }
        else {
              DebugIM.println
                    ("DEBUG, IMRouter, getNextHops(), " +
		    " the request URI is not a SipURI:"+
                    " unable to build a hop.");
        }
        
        if (defaultRoute != null ) {
            nextHops.add(defaultRoute);
        }
        
        return nextHops.listIterator();
    }
    
        /** Get the default hop.
         *@return defaultRoute is the default route.
         */
    public javax.sip.address.Hop getOutboundProxy() {
         return (javax.sip.address.Hop) this.defaultRoute;
    }
    
}


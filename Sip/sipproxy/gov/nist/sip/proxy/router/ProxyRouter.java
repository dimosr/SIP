/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.sip.proxy.router;

import javax.sip.message.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.*;
import java.util.*;
import gov.nist.sip.proxy.*;

/** This is a proxy default router. When the implementation wants to forward
* a request and  had run out of othe options, then it calls this method
* to figure out where to send the request. 
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olvier Deruelle <deruelle@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public class ProxyRouter implements Router {
    
    protected ProxyHop defaultRoute;
    protected SipStack stack;
	
    
        /**
         * Constructor.
         */
    public ProxyRouter(SipStack sipStack, String def) {
        ProxyDebug.println
	 ("DEBUG, ProxyRouter, the default route is: "+def);
        if (def !=null)
            this.defaultRoute = new ProxyHop(def);
	this.stack = sipStack;
    }


    
    public ProxyHop getNextHop(ListIterator routes) 
	throws IllegalArgumentException{
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
                
                
                ProxyHop hop = new ProxyHop(host,port,transport);
                ProxyDebug.println("DEBUG, ProxyRouter, getNextHop(), " +
                "The request has at least one route, we use the first one"+
                ": "+host+":"+port+"/"+transport);
                return hop;
                
            }
            return null;
        }
        catch(Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    
    
    private boolean checkPort(int port) {
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
         * The list is organized in the following priority.
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
    
         ProxyDebug.println("ProxyRouter: request:\n"+request.toString());   
         
        if (routes!=null) {
            Hop nextHop=getNextHop(routes);
            if (nextHop!=null) {
                nextHops.add(nextHop);
                return nextHops.listIterator();
            }
        }
        
      
        URI requestURI=request.getRequestURI();
        if (requestURI instanceof SipURI) {
            String mAddr=((SipURI)requestURI).getMAddrParam();
            if ( mAddr!=null){
                try {
                    String mAddrTransport=((SipURI)requestURI).
                    getTransportParam();
					if (mAddrTransport==null) {
						ViaHeader viaHeader=(ViaHeader)request.getHeader(ViaHeader.NAME);
						mAddrTransport=viaHeader.getTransport();
					}
                    int mAddrPort=((SipURI)requestURI).getPort();
                    if (mAddrPort==-1) mAddrPort=5060;
                    
                    ProxyHop mAddrHop=new ProxyHop
                    (mAddr,mAddrPort,mAddrTransport);
                    if (mAddrHop!=null) {
                        nextHops.add(mAddrHop);
                        ProxyDebug.println
                        ("DEBUG, ProxyRouter, getNextHops(), " +
                        " the hop added: Request URI maddr parameter:"+
                        " host:"+mAddr+
                        " port:"+mAddrPort+
                        " transport:"+mAddrTransport);
                        return nextHops.listIterator();
                    }
                    
                }
                catch(Exception e) {
                    throw new IllegalArgumentException
                    ("ERROR, ProxyRouter, pb to add the maddr hop");
                }
            }
            else {
                
                SipURI sipURI=(SipURI)requestURI;
                String host = sipURI.getHost();
                int port = sipURI.getPort();
                if (port == -1) {
                    port = 5060;
                }
                String transport = sipURI.getTransportParam();
                if (transport==null) {
                	ViaHeader viaHeader=(ViaHeader)request.getHeader(ViaHeader.NAME);
                	transport=viaHeader.getTransport();
                }
                
                ProxyHop requestURIHop = new ProxyHop(host,port,transport);
                nextHops.add(requestURIHop);
                ProxyDebug.println
                ("DEBUG, ProxyRouter, getNextHops(), " +
                " the hop added: host:"+requestURIHop.getHost()+
                " port:"+requestURIHop.getPort()+
                " transport:"+requestURIHop.getTransport());
                return nextHops.listIterator();
                
            }
        }
        else {
              ProxyDebug.println
                    ("DEBUG, ProxyRouter, getNextHops(), the request URI is not a SipURI:"+
                    " unable to build a hop.");
        }
        
        if (defaultRoute != null ) {
            nextHops.add(defaultRoute);
            ProxyDebug.println
                    ("DEBUG, ProxyRouter, getNextHops(), we added the hop (default route): "+
                    "host:"+defaultRoute.getHost()+
                    " port:"+defaultRoute.getPort()+
                    " transport:"+defaultRoute.getTransport());
        }
        
        return nextHops.listIterator();
    }
    
        /** Get the default hop.
         *@return defaultRoute is the default route.
         */
    public Hop getOutboundProxy() 
		{ return  this.defaultRoute; }

   
    
}


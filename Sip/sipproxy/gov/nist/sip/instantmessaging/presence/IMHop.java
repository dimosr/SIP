/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.sip.instantmessaging.presence;

import javax.sip.address.*;
import java.util.*;
import gov.nist.sip.instantmessaging.*;
/** Routing algorithms return a list of hops to which the request is
 * routed.
 *
 *@version  JAIN-SIP-1.1
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class IMHop implements Hop {
    
    protected String host;
    protected int port;
    protected String transport;
    
    /**
     * Debugging println.
     */
    public String toString() {  
        return  host + ":" + port + "/" + transport;  
    }
    
    /** Creates new Hop
     *@param hop is a hop string in the form of host:port/Transport
     *@throws IllegalArgument exception if string is not properly formatted or
     * null.
     */
    public IMHop(String hop) throws IllegalArgumentException {
        if (hop == null) throw new IllegalArgumentException("Null arg!");
        StringTokenizer stringTokenizer = new StringTokenizer(hop + "/");
        String hostPort = stringTokenizer.nextToken("/");
        transport = stringTokenizer.nextToken().trim();
        // System.out.println("Hop: transport = " + transport);
        if (transport == null) transport = "UDP";
        else if (transport == "") transport = "UDP";
        if (transport.compareToIgnoreCase("UDP") != 0 &&
        transport.compareToIgnoreCase("TCP") != 0)  {
            DebugIM.println("HopImpl, Bad transport string " + transport);
            throw new IllegalArgumentException(hop);
        }
        
        stringTokenizer = new StringTokenizer(hostPort+":");
        host = stringTokenizer.nextToken(":");
        if (host == null || host.equals( "") )
            throw new IllegalArgumentException("no host!");
        String portString = null;
        try {
            portString = stringTokenizer.nextToken(":");
        } catch (NoSuchElementException ex) {
            // Ignore.
        }
        if (portString == null || portString.equals("")) {
            port = 5060;
        } else {
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Bad port spec");
            }
        }
       
    }
    
    
    /** Create new hop given host, port and transport.
     *@param hostName hostname
     *@param portNumber port
     *@param trans transport
     */
    public IMHop(String hostName, int portNumber, String trans) {
        host = hostName;
        port = portNumber;
        transport = trans;
    }
    
    /**
     *Retruns the host string.
     *@return host String
     */
    public String getHost() {
        return host;
    }
    
    /**
     *Returns the port.
     *@return port integer.
     */
    public int getPort() {
        return port;
    }
    
    /** returns the transport string.
     */
    public String getTransport() {
        return transport;
    }
    
}

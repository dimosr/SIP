/*
 * Configuration.java
 *
 * Created on February 3, 2003, 3:21 PM
 */

package gov.nist.sip.proxy;
import java.util.*;
/**
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public class Configuration {
    
    // Stack:
    public String stackName;
    public String stackIPAddress;
    public String outboundProxy;
    public String routerPath;
    public String extensionMethods;
    public String retransmissionFilter;
    public String stopTime;
    public String maxConnections;
    public String maxServerTransactions;
    public String threadPoolSize;
    public Vector domainList;
    public Vector proxyToRegisterWithList;
    public String pstnGateway;
    
    // Registrar
    public String  registryPort;
    public boolean exportRegistry;
    
    // Logging
    public boolean accessLogViaRMI;
    public String logRMIPort;
    public String logLifetime;
    public boolean enableDebug;
    public String serverLogFile;
    public String badMessageLogFile;
    public String debugLogFile;
    public String outputProxy;
    
    // Presence server
    public boolean enablePresenceServer;
    
    // Authentication
    public boolean enableAuthentication;
    public String method;
    public String classFile;
    public String passwordsFile;
    
    // Registrations
    public boolean enableRegistrations;
    public int expiresTime=3600;
    public String registrationsFile;
    

    // Backwards compatibility
    public boolean rfc2543Compatible;

    // Listening points:
    public Hashtable listeningPoints;
    

    private int counter;
    
    /** Creates a new instance of Configuration */
    public Configuration() {
        counter=0;
        listeningPoints=new Hashtable();
        domainList=new Vector();
        proxyToRegisterWithList=new Vector();
    }
    
    public boolean hasDomain(String domainParam) {
        if (domainList!=null)
        for (int j=0;j<domainList.size();j++) {
            String domain=(String)domainList.elementAt(j);
            if ( domain.equalsIgnoreCase(domainParam) ) return true; 
        }
        return false;
    }
    
    public void addListeningPoint(String port,String transport) {
        if (check(port) && check(transport) ) {
            counter++;
            listeningPoints.put
		("listeningPoint"+counter,new Association(port,transport) );
        }
    }
    
  
    
    public Vector getListeningPoints() {
        if (listeningPoints!=null) {
            Collection c = listeningPoints.values();
           return new Vector(c);
        }
        return null;
    }
    
  
    
    protected boolean check(String s) {
        return  (s!=null && !s.trim().equals(""));
    }
    
    
    public boolean isValidConfiguration() {
        // Just a warning check for the basics methods
       
       if (enableAuthentication) {
            if ( check(method) &&
                 check(classFile) && 
                 check(passwordsFile) ) {
                 // OK
            }
            else {
                System.out.println
		("ERROR, the configuration is not valid: Problem with"+
                " the authentication tag and authentication parameters");
                return false;
            }
       }
        
       if  ( check(stackName) &&
             check(stackIPAddress) &&
             !listeningPoints.isEmpty()
             ) {
             return true;
       }
       else {
           System.out.println
		("ERROR, the configuration is not valid: Problem with"+
                " the stack tag and stack parameters");
            return false;
       }
       
       
    }
    
}

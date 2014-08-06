/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
* Creator: O. Deruelle (deruelle@nist.gov)                                     *
* Questions/Comments: nist-sip-dev@antd.nist.gov                               *
*******************************************************************************/
package gov.nist.sip.proxy;

import java.util.*;
import java.io.*;


/** Class for stopping the proxy.
 */
public class StopProxy extends TimerTask {
    
    private Proxy proxy;
    
    /** Constructor : 
     * @param proxy
     */    
    public StopProxy(Proxy proxy) {
       this.proxy=proxy;
    }
    
    /** 
     */    
    public void  run() {  
        ProxyDebug.println("Proxy trying to exit............. ");
        try {
            proxy.exit();
        }
        catch(Exception e) {
            ProxyDebug.println("Proxy failed to exit.........................");
            e.printStackTrace();
        }
        ProxyDebug.println();
    }
    
}

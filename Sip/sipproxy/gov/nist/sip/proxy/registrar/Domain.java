/*
 * Domain.java
 *
 * Created on March 25, 2003, 10:50 AM
 */

package gov.nist.sip.proxy.registrar;

/**
 *
 * @author  deruelle@nist.gov
 */
public class Domain {
    
    public String hostName;
    public String hostPort;
    public String from;
    
    /** Creates a new instance of Domain */
    public Domain() {
    }
    
    public void setHostName(String hostName) {
        this.hostName=hostName;
    }
    
    public void setHostPort(String hostPort) {
        this.hostPort=hostPort;
    }
    
     public void setFrom(String from) {
        this.from=from;
    }
    
}

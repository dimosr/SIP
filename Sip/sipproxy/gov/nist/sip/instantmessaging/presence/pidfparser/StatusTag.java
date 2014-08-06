/*
 * StatusTag.java
 *
 * Created on September 24, 2002, 11:03 AM
 */

package gov.nist.sip.instantmessaging.presence.pidfparser;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class StatusTag  {

    private String status;
    
    /** Creates new StatusTag */
    public StatusTag() {
        status=null;
    }

    public void setStatus(String status) {
        this.status=status;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String toString() {
        String result="<status status=\""+status+"\" />\n";
        return result;
    }
    
}

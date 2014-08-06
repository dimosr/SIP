/*
 * StatusTag.java
 *
 * Created on September 24, 2002, 11:03 AM
 */

package gov.nist.sip.proxy.presenceserver.pidfparser;

/**
 *
 * @author  deruelle
 * @author  Henrik Leion
 * @version 1.1
 */
public class StatusTag  {

    private BasicTag basicTag;
    
    /** Creates new StatusTag */
    public StatusTag() {
        basicTag=null;
    }

    public void setBasicTag(BasicTag basicTag) {
        this.basicTag=basicTag;
    }
    
    public BasicTag getBasicTag() {
        return basicTag;
    }
    
    public String toString() {
        String result="<status>\n"+basicTag.toString()+"</status>\n";
        return result;
    }
    
}

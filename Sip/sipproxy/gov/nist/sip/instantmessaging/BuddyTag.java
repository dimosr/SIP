/*
 * PresentityTag.java
 *
 * Created on September 24, 2002, 11:01 AM
 */

package gov.nist.sip.instantmessaging;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class BuddyTag  {

    private String uri;
 
    /** Creates new PresentityTag */
    public BuddyTag() {
    }

    public void setURI(String uri) {
        this.uri=uri;
    }
    
    public String getURI() {
        return uri;
    }
   
    public String toString() {
        String result="<buddy uri=\""+uri+"\" />\n";
        return result;
    }
    
}

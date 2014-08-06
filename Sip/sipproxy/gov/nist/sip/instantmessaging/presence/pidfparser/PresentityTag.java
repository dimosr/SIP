/*
 * PresentityTag.java
 *
 * Created on September 24, 2002, 11:01 AM
 */

package gov.nist.sip.instantmessaging.presence.pidfparser;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class PresentityTag  {

    private String uri;
    
    /** Creates new PresentityTag */
    public PresentityTag() {
        uri=null;
    }

    public void setURI(String uri) {
        this.uri=uri;
    }
    
    public String getURI() {
        return uri;
    }

    public String toString() {
        String result="<presentity uri=\""+uri+"\" />\n";
        return result;
    }
    
}

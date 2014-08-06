/*
 * PresentityTag.java
 *
 * Created on September 24, 2002, 11:01 AM
 */

package gov.nist.sip.proxy.presenceserver.pidfparser;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class PresentityTag  {

    private String id;
    
    /** Creates new PresentityTag */
    public PresentityTag() {
        id=null;
    }

    public void setId(String id) {
        this.id=id;
    }
    
    public String getId() {
        return id;
    }

    public String toString() {
        String result="<presentity id=\""+id+"\" />\n";
        return result;
    }
    
}

/*
 * AtomTag.java
 *
 * Created on September 24, 2002, 11:02 AM
 */

package gov.nist.sip.instantmessaging.presence.pidfparser;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class AtomTag {

    private AddressTag addressTag;
    private String id;
    
    /** Creates new AtomTag */
    public AtomTag() {
        addressTag=null;
    }

    public void setAddressTag(AddressTag addressTag) {
        this.addressTag=addressTag;
    }
    
    public AddressTag getAddressTag() {
        return addressTag;
    }
    
    public void setId(String id) {
        this.id=id;
    }
    
    public String getId() {
        return id;
    }
    
    public String toString() {
        String result="<atom id=\""+id+"\" >\n";
        result+=addressTag.toString();
        result+="</atom>\n";
        return result;
    }
    
}

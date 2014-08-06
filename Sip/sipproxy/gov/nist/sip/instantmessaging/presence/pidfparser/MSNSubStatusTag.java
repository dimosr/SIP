/*
 * MSNSubStatusTag.java
 *
 * Created on September 24, 2002, 11:03 AM
 */

package gov.nist.sip.instantmessaging.presence.pidfparser;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class MSNSubStatusTag  {

    private String msnSubStatus;
    
    /** Creates new MSNSubStatusTag */
    public MSNSubStatusTag() {
      msnSubStatus=null;
    }

    public void setMSNSubStatus(String msnSubStatus) {
        this.msnSubStatus=msnSubStatus;
    }
    
    public String getMSNSubStatus() {
        return msnSubStatus;
    }
    
    public String toString() {
        String result="<msnsubstatus substatus=\""+msnSubStatus+"\" />\n";
        return result;
    }
    
}

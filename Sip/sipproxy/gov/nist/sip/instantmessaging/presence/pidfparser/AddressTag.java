/*
 * AddressTag.java
 *
 * Created on September 24, 2002, 11:02 AM
 */

package gov.nist.sip.instantmessaging.presence.pidfparser;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class AddressTag {

    private StatusTag statusTag;
    private MSNSubStatusTag msnSubStatusTag;
    private String uri;
    private float priority;
    
    /** Creates new AddressTag */
    public AddressTag() {
        uri=null;
        statusTag=null;
        priority=-1;
        msnSubStatusTag=null;
    }

    public void setStatusTag(StatusTag statusTag) {
        this.statusTag=statusTag;
    }
    
    public void setMSNSubStatusTag(MSNSubStatusTag msnSubStatusTag) {
        this.msnSubStatusTag=msnSubStatusTag;
    }
    
    public StatusTag getStatusTag() {
        return statusTag;
    }
 
    public MSNSubStatusTag getMSNSubStatusTag() {
        return msnSubStatusTag;
    }
    
    public void setURI(String uri) {
        this.uri=uri;
    }
    
    public String getURI() {
        return uri;
    }
    
    public void setPriority(float priority) {
        this.priority=priority;
    }
    
    public float getPriority() {
        return priority;
    }
    
    public String toString() {
        String result="<address uri=\""+uri+"\" ";
        if (priority!=-1)
            result+="priority=\""+priority+"\"";
        result+=" >\n";
        if (statusTag!=null)
            result+=statusTag.toString();
        if (msnSubStatusTag!=null)
            result+=msnSubStatusTag.toString();
        result+="</address>\n";
        return result;
    }
    
}

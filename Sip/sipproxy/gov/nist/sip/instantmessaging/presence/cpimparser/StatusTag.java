/*
 * StatusTag.java
 *
 * Created on September 24, 2002, 11:03 AM
 */

package gov.nist.sip.instantmessaging.presence.cpimparser;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class StatusTag  {

    private ValueTag valueTag;
    
    /** Creates new StatusTag */
    public StatusTag() {
        valueTag=null;
    }

    public void setValueTag(ValueTag valueTag) {
        this.valueTag=valueTag;
    }
    
    public ValueTag getValueTag() {
        return valueTag;
    }
    
    public String toString() {
        String result="<status>\n"+valueTag.toString()+"</status>\n";
        return result;
    }
    
}

/*
 * ValueTag.java
 *
 * Created on October 4, 2002, 7:18 PM
 */

package gov.nist.sip.instantmessaging.presence.cpimparser;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class ValueTag  {

    private String value;
    
    /** Creates new ValueTag */
    public ValueTag() {
        value=null;
    }

    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value=value;
    }
    
    public String toString() {
        return "<value>"+value+"</value>\n";
    }
    
}

/*
 * ValueTag.java
 *
 * Created on October 4, 2002, 7:18 PM
 */

package gov.nist.sip.proxy.presenceserver.pidfparser;

/**
 *
 * @author  deruelle
 * @author  Henrik Leion
 * @version 1.1
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

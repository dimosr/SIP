/*
 * ContactTag.java
 *
 * Created on October 4, 2002, 7:17 PM
 */

package gov.nist.sip.proxy.presenceserver.pidfparser;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class ContactTag  {

    private float priority;
    private String contact;
    
    /** Creates new ContactTag */
    public ContactTag() {
         priority=-1;
         contact=null;
    }
    
    public void setPriority(float priority) {
        this.priority=priority;
    }
    
    public float getPriority() {
        return priority;
    }

    public void setContact(String contact) {
        this.contact=contact;
    }
    
    public String getContact() {
        return contact;
    }
    
    
    public String toString() {
        String result="<contact ";
        if (priority!=-1)
            result+="priority=\""+priority+"\"";
        result+=" >";
        if (contact!=null)
            result+=contact;
        result+="</contact>\n";
        return result;
    }
    
}

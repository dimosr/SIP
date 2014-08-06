/*
 * NoteTag.java
 *
 * Created on October 4, 2002, 7:17 PM
 */

package gov.nist.sip.instantmessaging.presence.cpimparser;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class NoteTag  {

    private String note;
    
    /** Creates new NoteTag */
    public NoteTag() {
        note=null;
    }

    public void setNote(String note) {
        this.note=note;
    }
    
    public String getNote() {
        return note;
    }
      
    public String toString() {
        String result="<note>";
        if (note!=null)
            result+=note;
        result+="</note>\n";
        return result;
    }
    
}

/*
 * TupleTag.java
 *
 * Created on October 4, 2002, 7:16 PM
 */

package gov.nist.sip.proxy.presenceserver.pidfparser;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class TupleTag  {

    private String id;
    private StatusTag statusTag;
    private ContactTag contactTag;
    private NoteTag noteTag;
    
    /** Creates new TupleTag */
    public TupleTag() {
        id=null;
        statusTag=null;
        contactTag=null;
        noteTag=null;
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id=id;
    }
 
    public void setStatusTag(StatusTag statusTag) {
        this.statusTag=statusTag;
    }
    
    public StatusTag getStatusTag() {
        return statusTag;
    }
    
    public void setContactTag(ContactTag contactTag) {
        this.contactTag=contactTag;
    }
    
    public ContactTag getContactTag() {
        return contactTag;
    }
    
     public void setNoteTag(NoteTag noteTag) {
        this.noteTag=noteTag;
    }
    
    public NoteTag getNoteTag() {
        return noteTag;
    }
    
    public String toString() {
        String result="<tuple ";
        result+=" id=\""+id+"\" >\n"; 
        if (statusTag!=null)
            result+=statusTag.toString();
        if (contactTag!=null) 
            result+=contactTag.toString();
        if (noteTag!=null) 
            result+=noteTag.toString();
        result+="</tuple>\n";
        return result;
    }
    
}

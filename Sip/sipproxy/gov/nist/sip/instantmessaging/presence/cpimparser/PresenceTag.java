/*
 * PresenceTag.java
 *
 * Created on September 24, 2002, 11:00 AM
 */

package gov.nist.sip.instantmessaging.presence.cpimparser;

import java.util.*;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class PresenceTag  {

    private Vector tupleTagList;
    private PresentityTag presentityTag;
    private String entity;
    
    /** Creates new PresenceTag */
    public PresenceTag() {
        tupleTagList=new Vector();
        entity=null;
        presentityTag=null;
    }

    public void addTupleTag(TupleTag tupleTag) {
        if (tupleTag !=null) 
            tupleTagList.addElement(tupleTag);
    }
    
    public void setPresentityTag(PresentityTag presentityTag) {
        this.presentityTag=presentityTag;
    }
    
    public Vector getTupleTagList() {
        return tupleTagList;
    }
    
    public PresentityTag getPresentityTag() {
        return presentityTag;
    }
    
    public void setEntity(String entity) {
        this.entity=entity;
    }    
    
    public String getEntity() {
        return entity;
    }
    
    public String toString() {
        String result="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                      "<presence xmlns=\"urn:ietf:params:cpim-presence:\" ";
        result+=" entity=\""+entity+"\" >\n";
        if (presentityTag!=null)
            result+= presentityTag.toString();
        // The tuple list:
        for (int i=0;i<tupleTagList.size();i++) {
            TupleTag tupleTag=(TupleTag)tupleTagList.elementAt(i);
            result+=tupleTag.toString();
        }
        result+="</presence>";
        return result;
    }
    
}

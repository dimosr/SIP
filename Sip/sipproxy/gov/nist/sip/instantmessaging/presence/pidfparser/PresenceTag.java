/*
 * PresenceTag.java
 *
 * Created on September 24, 2002, 11:00 AM
 */

package gov.nist.sip.instantmessaging.presence.pidfparser;

import java.util.*;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class PresenceTag  {

    private Vector atomTagList;
    private PresentityTag presentityTag;
    
    /** Creates new PresenceTag */
    public PresenceTag() {
        atomTagList=new Vector();
    }

    public void addAtomTag(AtomTag atomTag) {
        if (atomTag !=null) 
            atomTagList.addElement(atomTag);
    }
    
    public void setPresentityTag(PresentityTag presentityTag) {
        this.presentityTag=presentityTag;
    }
    
    public Vector getAtomTagList() {
        return atomTagList;
    }
    
    public PresentityTag getPresentityTag() {
        return presentityTag;
    }
    
    public String toString() {
        String result="<?xml version=\"1.0\"?>\n"+
                      "<!DOCTYPE presence\n"+
                      "PUBLIC \"-//IETF//DTD RFCxxxx XPIDF 1.0//EN\" \"xpidf.dtd\">\n"+
                      "<presence>\n";
        // The presentity:
        result+=presentityTag.toString();
        // The atom list:
        for (int i=0;i<atomTagList.size();i++) {
            AtomTag atomTag=(AtomTag)atomTagList.elementAt(i);
            result+=atomTag.toString();
        }
        result+="</presence>";
        return result;
    }
    
}

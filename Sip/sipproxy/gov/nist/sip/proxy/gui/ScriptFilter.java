package gov.nist.sip.proxy.gui;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

public class ScriptFilter extends FileFilter {
    
    public String extensionFile;
    
    public ScriptFilter(String extensionFile) {
        this.extensionFile=extensionFile;
    }
    
    // Accept all directories and all xml files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension =getExtension(f);
        //System.out.println("extension:"+extension);
        if (extension != null) {
            if (extension.equals(extensionFile) ) {
                    return true;
            } else {
                return false;
            }
        }
        return false;
    }
    
    // The description of this filter
    public String getDescription() {
        if (extensionFile.equals("xml") )
            return "Just .xml files";
        if (extensionFile.equals("passwords") )
            return "Just .passwords files";
        else return null;
    }
    
    /*
     * Get the extension of a file.
     */
    public String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}

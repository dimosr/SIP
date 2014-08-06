/*
 * Presentity.java
 *
 * Created on October 3, 2002, 6:47 PM
 */

package gov.nist.sip.instantmessaging.presence;

import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import gov.nist.sip.instantmessaging.*;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class Presentity  {

    private String presentityName;
    private String status;
    private Response okReceived;
    private Dialog dialog;
    
    /** Creates new Presentity */
    public Presentity(String presentityName,Response okReceived) {
        this.presentityName=presentityName;
        this.okReceived=okReceived;
        status="offline";
    }
    
    public Response getOkReceived() {
         return okReceived;
    }
    
    public Dialog getDialog() {
         return dialog;
    }
    
    public void setDialog(Dialog dialog) {
         this.dialog=dialog;
    }
    
    public String getPresentityName() {
        return presentityName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status=status;
    }
    
}

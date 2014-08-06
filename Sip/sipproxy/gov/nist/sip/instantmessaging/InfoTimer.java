/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
* Creator: O. Deruelle (deruelle@nist.gov)                                     *
* Questions/Comments: nist-sip-dev@antd.nist.gov                               *
*******************************************************************************/
package gov.nist.sip.instantmessaging;

import java.util.*;
import java.io.*;
import java.net.*;
import gov.nist.sip.instantmessaging.*;

/** Class for killing the processes and restoring the ports.
 */
public class InfoTimer extends TimerTask {
    
    private ChatSession chatSession; 
    
    /** 
     */    
    public InfoTimer(ChatSession chatSession) {
        this.chatSession=chatSession;
    }

    /**  
     */    
    public void  run() {  
        // WE reinitialize the text
        chatSession.setInfo("");
    }
    
}

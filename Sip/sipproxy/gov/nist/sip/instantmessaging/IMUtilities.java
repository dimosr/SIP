package gov.nist.sip.instantmessaging;

import java.io.*;
import java.util.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
/** Utilities for the IM client.
*
*@version  JAIN-SIP-1.1
*
*@author Olvier Deruelle <deruelle@nist.gov>  <br/>
*@author M. Ranganathan <mranga@nist.gov> <br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class IMUtilities {
 
      /** Utility method for writing in a file
       * @param outFile: file to write
       * @param text String to set
       */
    public static void writeFile(String outFile, String text) {
        try {
            
            File file=new File(outFile);
            
            if (file.exists()) {
                FileWriter fileWriter = new FileWriter(outFile,false);
                PrintWriter pw = new PrintWriter(fileWriter,true);
                
                pw.write(text);
                
                pw.close();
                fileWriter.close();
            }
            else DebugIM.println("DebugIM, writeFile(),  unable to write, the file:"+
                    outFile +" is missing.");
        }
        catch (Exception e) {
            DebugIM.println("DebugIM, writeFile(),  unable to write, the file:"+
                    outFile +" is missing.");
            e.printStackTrace();
        }
    }
    
    public static URI getCleanUri(URI uri) {
        if (uri instanceof SipURI) {
            SipURI sipURI=(SipURI)uri.clone();
            
            Iterator iterator=sipURI.getParameterNames();
            while (iterator!=null && iterator.hasNext()) {
                String name=(String)iterator.next();
                sipURI.removeParameter(name);
            }
            return  sipURI;
        }
        else return  uri;
    }
    
    
    public static  String getKey(Message message,String header) {
        try{
            URI uri=null;
            if (header.equals("From") ) {
                FromHeader fromHeader=(FromHeader)message.getHeader(FromHeader.NAME);
                Address fromAddress=fromHeader.getAddress();
                uri=fromAddress.getURI();
            }
            else
                if (header.equals("To") ) {
                    ToHeader toHeader=(ToHeader)message.getHeader(ToHeader.NAME);
                    Address toAddress=toHeader.getAddress();
                    uri=toAddress.getURI();
                }
                else
                    if (header.equals("Request-URI") ) {
                        uri=
                        ((Request)message).getRequestURI();
                    }
                    else {
                        return null;
                    }
            
            // URI parameters MUST be removed:
            URI cleanedUri = getCleanUri(uri);
            
            String  keyresult=cleanedUri.toString();
            keyresult=keyresult.toLowerCase();
            DebugIM.println("DEBUG, IMUtilities, getKey(), the key is: " +keyresult);
            return keyresult;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}

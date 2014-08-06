package gov.nist.sip.proxy;

/**
 *
*@version  JAIN-SIP-1.1
*
*@author  Olivier Deruelle <deruelle@nist.gov> <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
 */
public class Association {
    
     public String port;
        public String transport;
        
        public Association(String s1,String s2) {
            port=s1;
            transport=s2;
        }
    
}

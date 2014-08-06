/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
* See ../../../../doc/uncopyright.html for conditions of use.                  *
* Author: M. Ranganathan (mranga@nist.gov)                                     *
* Modified by: Marc Bednarek (bednarek@nist.gov)                               *
* Questions/Comments: nist-sip-dev@antd.nist.gov                               *
*******************************************************************************/
/******************************************************
 * File: DigestAuthenticationcMethod.java
 * created 26-Sep-00 2:17:37 PM by mranga
 */

package gov.nist.sip.proxy.authentication;

import java.security.*;
import java.util.*;

import gov.nist.sip.proxy.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;

/**
*  Implements the HTTP digest authentication method.
*/

public class DigestServerAuthenticationMethod implements AuthenticationMethod
{
	
	public static final String DEFAULT_SCHEME = "Digest";
        public static final String DEFAULT_DOMAIN = "129.6.55.78";
	public static final String DEFAULT_ALGORITHM = "MD5";
	public static String DEFAULT_REALM = "nist.gov";
	public static final String NULL_PASSWORD = "";
        
	private Hashtable passwordTable;
	private MessageDigest messageDigest;
	
	/**
	*  Default constructor.
	*/
	public DigestServerAuthenticationMethod() {
	   try {
		messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
	   }
           catch ( NoSuchAlgorithmException ex ) {
	   	ProxyDebug.println("Algorithm not found " + ex);
		ex.printStackTrace();
	   }
	   passwordTable = new Hashtable();
	}
	

	/**
	* Initialize -- load password files etc.
	* Password file format is name:authentication domain:password
	*@param pwFileName is the password file name.
        *@param Exception is thrown when the password file is bad.
	*/
	public void initialize (String pwFileName ) {
            try{
		XMLAuthenticationParser parser=new XMLAuthenticationParser(pwFileName);
                
                String def=parser.getRealm();
                if (def!=null) DEFAULT_REALM=def;
                ProxyDebug.println("DEBUG, DigestAuthenticationMethod, initialize(),"+
                             " the realm is:"+DEFAULT_REALM);
                Vector usersTagList=parser.getUsersTagList();
                if (usersTagList!=null)
                for (int i=0;i<usersTagList.size();i++) {
                    UserTag userTag=(UserTag)usersTagList.elementAt(i);
                    String userName=userTag.getUserName();
                    //String userRealm=userTag.getUserRealm();
                    String userPassword=userTag.getUserPassword();
                    if ( userName!=null ) {
                         
                         if ( userPassword==null ) {
                              ProxyDebug.println("DEBUG, DigestAuthenticationMethod, initialize(),"+
                             " the userPassword parameter does not exist for user: "+userName+
                             ", we use the default: \""+NULL_PASSWORD+"\"");
                             userPassword=NULL_PASSWORD;
                         }
                         passwordTable.put(userName+"@"+DEFAULT_REALM,userPassword);
                    }
                    else {
                            ProxyDebug.println("DEBUG, DigestAuthenticationMethod, initialize(),"+
                             " the userName parameter does not exist, we skip this entry!!");
                    }
                }
                else    ProxyDebug.println("DEBUG, DigestAuthenticationMethod, initialize(),"+
                             "Error during parsing the passwords file!");
               
            }
            catch(Exception e) {
                ProxyDebug.println("ERROR, DigestAuthenticationMethod, initialize(),"+
                "exception raised:");
                e.printStackTrace();
            }   
	}
	
	
	    	
	/**
	*  Get the authentication scheme
	*@return the scheme name
	*/
	public String getScheme() {
		return DEFAULT_SCHEME;
	}
	/**
	*  get the authentication realm
	*@return the realm name
	*/
	public String getRealm(String resource) {
		return  DEFAULT_REALM;
	}
	/**
	*  get the authentication domain.
	*@return the domain name
	*/
	public String getDomain() {
		return DEFAULT_DOMAIN;
	}
	/**
	*  Get the authentication Algorithm
	*@return the alogirithm name (i.e. Digest).
	*/
	public String getAlgorithm() {
		return DEFAULT_ALGORITHM;
	}
	/**
	*  Generate the challenge string.
	*@return a generated nonce.
	*/
	public String generateNonce() {
		// Get the time of day and run MD5 over it.
		Date date = new Date();   
		long time = date.getTime();
		Random rand = new Random();
		long pad = rand.nextLong();
		String nonceString = (new Long(time)).toString() + 
				(new Long(pad)).toString();
		byte mdbytes[] = messageDigest.digest(nonceString.getBytes());
		// Convert the mdbytes array into a hex string.
		return ProxyUtilities.toHexString(mdbytes);
	}
	
	/**
	*  Check the response and answer true if authentication succeeds.
	*  We are making simplifying assumptions here and assuming that 
	*  the password is available  to us for computation of the MD5 hash. 
	*  We also dont cache authentications so that the
	*  user has to authenticate on each registration.
	* @param user is the username 
	* @param authHeader is the Authroization header from the SIP request.
	* @param requestLine is the SIP Request line from the SIP request.
	* @exception SIPAuthenticationException 
        *     is thrown when authentication fails or message is bad
	*/
	public boolean doAuthenticate( String user,
		    AuthorizationHeader authHeader,
		    Request  request) 
	{
		String realm =  authHeader.getRealm();
		String username = authHeader.getUsername();
                URI requestURI=request.getRequestURI();
      
                
		if (username == null) {
                    ProxyDebug.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): "+
                    "WARNING: userName parameter not set in the header received!!!");
                    username = user;
		}
		if (realm == null) {
                    ProxyDebug.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): "+
                    "WARNING: realm parameter not set in the header received!!! WE use the default one");
                    realm = DEFAULT_REALM; 
                }
               
                ProxyDebug.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): "+
                "Trying to authenticate user: "+username+" for "+
                " the realm: "+ realm);
                
		String password = (String) passwordTable.get(username+"@"+ realm);
		if (password == null)  {
                     ProxyDebug.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): "+
                    "ERROR: password not found for the user: "+username+"@"+ realm);
			return false;
		}
                
		String nonce = authHeader.getNonce();
		// If there is a URI parameter in the Authorization header, 
		// then use it.
		URI uri= authHeader.getURI();
		// There must be a URI parameter in the authorization header.
                if (uri == null){
                    ProxyDebug.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): "+
                    "ERROR: uri paramater not set in the header received!");
                   return false;
                }
                
                
                ProxyDebug.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), username:"+username+"!");
                ProxyDebug.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), realm:"+realm+"!");
                ProxyDebug.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), password:"+password+"!");
                ProxyDebug.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), uri:"+uri+"!");
                ProxyDebug.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), nonce:"+nonce+"!");
                ProxyDebug.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), method:"+request.getMethod()+"!");
                
                String A1 = username + ":" + realm+ ":" +   password ;
                String A2 =
                request.getMethod().toUpperCase() + ":" + uri.toString() ;
                byte mdbytes[] = messageDigest.digest(A1.getBytes());
                String HA1 = ProxyUtilities.toHexString(mdbytes);
                
                ProxyDebug.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), HA1:"+HA1+"!");
                mdbytes = messageDigest.digest(A2.getBytes());
                String HA2 = ProxyUtilities.toHexString(mdbytes);
                ProxyDebug.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), HA2:"+HA2+"!");
                String cnonce = authHeader.getCNonce();
                String KD = HA1 + ":" + nonce;
                if (cnonce != null) {
                    KD += ":" + cnonce;
                }
                KD += ":" + HA2;
                mdbytes = messageDigest.digest(KD.getBytes());
                String mdString = ProxyUtilities.toHexString(mdbytes);
                String response = authHeader.getResponse();
                ProxyDebug.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): "+
                    "we have to compare his response: "+response +" with our computed"+
                    " response: "+mdString);
         
                int res=(mdString.compareTo(response));
                if (res==0) {
                    ProxyDebug.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): "+
                    "User authenticated...");
                }
                else {
                    ProxyDebug.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): "+
                    "User not authenticated...");
                }
                
                return  res==0 ;
        }
        
}

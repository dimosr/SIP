/*
 * Authentication.java
 *
 * Created on July 15, 2002, 11:08 AM
 */

package gov.nist.sip.proxy.authentication;

import gov.nist.sip.proxy.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class Authentication  {

    private AuthenticationMethod authenticationMethod;
    private Proxy proxy;
  
    /** Creates new Authentication */
     public Authentication(Proxy proxy){ 
	this.proxy = proxy;
    }
    
    
    public void setAuthenticationMethod(AuthenticationMethod authMethod) {
        authenticationMethod=authMethod;
    }

     public boolean isAuthorized(Request request){
	// Let Acks go through unchallenged.
	if (request.getMethod().equals(Request.ACK) ||
            request.getMethod().equals(Request.CANCEL)) return true;
       ProxyAuthorizationHeader proxyAuthorization=
	(ProxyAuthorizationHeader)request.getHeader
			(ProxyAuthorizationHeader.NAME);
     
       if (proxyAuthorization==null) {
           ProxyDebug.println
	("Authentication failed: ProxyAuthorization header missing!");     
           return false;
       }
       
       String username=proxyAuthorization.getParameter("username");
       
       try{
            boolean res=authenticationMethod.doAuthenticate
		(username,proxyAuthorization,request);
            if (res) ProxyDebug.println
		("Authentication passed for user: "+username);
            else ProxyDebug.println
		("Authentication failed for user: "+username); 
            return res;
       }
       catch(Exception e) {
            e.printStackTrace();
            return false;
       }
    }
    
    
     public Response getResponse(Request request) {
        try{
            if (authenticationMethod==null ) {
                ProxyDebug.println
		("ERROR, you have to initialize the Authentication class");
                return null;
            }
            
            MessageFactory messageFactory=proxy.getMessageFactory();
            HeaderFactory headerFactory=proxy.getHeaderFactory();
            
            Response response=messageFactory.createResponse
            (Response.PROXY_AUTHENTICATION_REQUIRED,request);
            
            ProxyAuthenticateHeader proxyAuthenticate=
		headerFactory.createProxyAuthenticateHeader(
            authenticationMethod.getScheme());
            
            String realm= authenticationMethod.getRealm(null);
            if (! realm.trim().equals("") )
                proxyAuthenticate.setParameter("realm",realm);
            
            proxyAuthenticate.setParameter
            ("nonce",authenticationMethod.generateNonce());
            //proxyAuthenticateImpl.setParameter("domain",authenticationMethod.getDomain());
            proxyAuthenticate.setParameter("opaque","");
            proxyAuthenticate.setParameter("stale","FALSE");
            proxyAuthenticate.setParameter("algorithm",authenticationMethod.getAlgorithm());
            response.setHeader(proxyAuthenticate);
            
            return response;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
}

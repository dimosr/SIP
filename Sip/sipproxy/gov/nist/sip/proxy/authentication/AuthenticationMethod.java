/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
* See ../../../../doc/uncopyright.html for conditions of use.                  *
* Author: M. Ranganathan (mranga@nist.gov)                                     *
* Questions/Comments: nist-sip-dev@antd.nist.gov                               *
*******************************************************************************/
/******************************************************
 * File: AuthenticationMethod.java
 * created 26-Sep-00 2:01:16 PM by mranga
 */

package gov.nist.sip.proxy.authentication;

import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;

public interface AuthenticationMethod
{

	/**
	*  Get the authentication scheme
	*/
	public String getScheme() ;

	/**
	* Initialize the authentication method. This has to be
	* done outside the constructor as the constructor is generic
	* (created from the class name specified in the authentication method).
	*/
	public void initialize(String initString) ;
	
	/**
	*  Get the authentication realm.
	*/
	public String getRealm(String resource);
	
	/**
	*  get the authentication domain.
	*/
	public String getDomain();
	/**
	*  Get the authentication Algorithm
	*/
	public String getAlgorithm();
	/**
	*  Generate the challenge string.
	*/
	public String generateNonce();
        
	/**
	*  Check the response and answer true if authentication succeeds.
	*  Not all of these fields are relevant for every 
	*  method - a basic scheme may simply do a username password check.
	*  @param  username is the username and password.
	*  @param authorizationHeader  is the authorization header from 
	*	the SIP request.
	*  @param  requestLine is the RequestLine from the SIP 
	*		Request.
	*/
	public boolean doAuthenticate(String username, 
		  AuthorizationHeader authorizationHeader,
		  Request request);
        

}

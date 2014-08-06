/*
 * AuthenticationProcess.java
 *
 * Created on January 7, 2003, 5:30 PM
 */

package gov.nist.sip.instantmessaging;

import gov.nist.javax.sip.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import java.util.*;
import gov.nist.sip.instantmessaging.presence.* ;
import gov.nist.sip.instantmessaging.authentication.* ;
/**
 *
 * @author  olivier deruelle
 */
public class AuthenticationProcess {
    
    private Vector usersTagList;
    private IMUserAgent imUA;
    
    /** Creates a new instance of AuthenticationProcess */
    public AuthenticationProcess(IMUserAgent imUA,Vector usersTagList) {
        this.imUA=imUA;
        
        if (usersTagList!=null) this.usersTagList=usersTagList;
        else this.usersTagList=new Vector();
    }
    
    public boolean hasLoginInformations(String realmParameter) {
        for(int i=0;i<usersTagList.size();i++) {
            UserTag userTag=(UserTag)usersTagList.elementAt(i);
            String realm=userTag.getUserRealm();
            if (realm!=null && realm.trim().equals(realmParameter) ) {
                return true;
            }
        }
        return false;
    }
    
    public String getUserName(String realmParameter) {
       for ( int i=0;i<usersTagList.size();i++) {
            UserTag userTag=(UserTag)usersTagList.elementAt(i);
            String realm=userTag.getUserRealm();
            if (realm!=null && realm.trim().equals(realmParameter) ) {
                return userTag.getUserName();
            }
       }
       return null;
    }
    
    public String getPassword(String realmParameter) {
        for(int i=0;i<usersTagList.size();i++) {
            UserTag userTag=(UserTag)usersTagList.elementAt(i);
            String realm=userTag.getUserRealm();
            if (realm!=null && realm.trim().equals(realmParameter) ) {
                return userTag.getUserPassword();
            }
        }
        return null;
    }
    
     public void addUser(String userName,String password,String realm) {
        UserTag userTag=new UserTag();
        userTag.setUserName(userName);
        userTag.setUserRealm(realm);
        userTag.setUserPassword(password);
        
        usersTagList.addElement(userTag);
    }
    
    public Header getHeader(Response response) {
        try {
            
            // Proxy-Authorization header:
            ProxyAuthenticateHeader authenticateHeader=(ProxyAuthenticateHeader)
            response.getHeader(
            ProxyAuthenticateHeader.NAME);
            
            WWWAuthenticateHeader wwwAuthenticateHeader=null;
            CSeqHeader cseqHeader=(CSeqHeader)response.getHeader(CSeqHeader.NAME);
            
            String cnonce=null;
            String uri="sip:"+imUA.getRegistrarAddress()+":"+imUA.getRegistrarPort();
            String method=cseqHeader.getMethod();
            String userName=null;
            String password=null;
            String nonce=null;
            String realm=null;
            String qop=null;
            
            if (authenticateHeader==null) {
                wwwAuthenticateHeader=(WWWAuthenticateHeader)
                response.getHeader(WWWAuthenticateHeader.NAME);
                
                nonce=wwwAuthenticateHeader.getNonce();
                realm=wwwAuthenticateHeader.getRealm();
                if (realm==null) {
                    DebugIM.println("AuthenticationProcess, getProxyAuthorizationHeader(),"+
                    " ERROR: the realm is not part of the 401 response!");
                    return null;
                }
                cnonce=wwwAuthenticateHeader.getParameter("cnonce");
                qop=wwwAuthenticateHeader.getParameter("qop");
            }
            else {
                
                nonce=authenticateHeader.getNonce();
                realm=authenticateHeader.getRealm();
                if (realm==null) {
                    DebugIM.println("AuthenticationProcess, getProxyAuthorizationHeader(),"+
                    " ERROR: the realm is not part of the 407 response!");
                    return null;
                }
                cnonce=authenticateHeader.getParameter("cnonce");
                qop=authenticateHeader.getParameter("qop");
            }
            
           
            /*
            if ( hasLoginInformations(realm) ) {
                // We can send the stored informations:
                userName=getUserName(realm);
                password=getPassword(realm);
            }
            else {
             */
                // We have to ask the user:
                InstantMessagingGUI imGUI=imUA.getInstantMessagingGUI();
                AuthenticationDialog authenticationDialog=new AuthenticationDialog(imGUI,realm);
            
                if ( authenticationDialog.isStop()) return null;
                userName=authenticationDialog.getUserName();
                password=authenticationDialog.getPassword();
                
                // Let's store those informations:
                addUser(userName,password,realm);
            //}
                
            HeaderFactory headerFactory=imUA.getHeaderFactory();
           
            DigestClientAuthenticationMethod digest=new DigestClientAuthenticationMethod();
            digest.initialize(realm,userName,uri,nonce,password,method,cnonce,"MD5");
            
            if (authenticateHeader==null) {
                AuthorizationHeader header=headerFactory.createAuthorizationHeader("Digest");
                header.setParameter("username",userName);
                header.setParameter("realm",realm);
                header.setParameter("uri",uri);
                header.setParameter("algorithm","MD5");
                header.setParameter("opaque","");
                header.setParameter("nonce",nonce);
                header.setParameter("response",digest.generateResponse());
                if (qop!=null)
                    header.setParameter("qop",qop);
                
                
                return header;
                
            }
            else {
                ProxyAuthorizationHeader header=headerFactory.createProxyAuthorizationHeader("Digest");
                header.setParameter("username",userName);
                header.setParameter("realm",realm);
                header.setParameter("uri",uri);
                header.setParameter("algorithm","MD5");
                header.setParameter("opaque","");
                header.setParameter("nonce",nonce);
                header.setParameter("response",digest.generateResponse());
                if (qop!=null)
                    header.setParameter("qop",qop);
                
                
                return header;
                
            }
            

        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
}

/*
 * UserTag.java
 *
 * Created on October 31, 2002, 9:15 PM
 */

package gov.nist.sip.instantmessaging.authentication;

/**
 *
 * @author  deruelle
 */
public class UserTag {
    
    private String userName;
    private String userRealm;
    private String userPassword;
    
    /** Creates a new instance of UserTag */
    public UserTag() {
        userName=null;
        userRealm=null;
        userPassword=null;
    }
    
    public void setUserName(String userName) {
        this.userName=userName;
    }
     
    public void setUserRealm(String userRealm) {
        this.userRealm=userRealm;
    }
       
    public void setUserPassword(String userPassword) {
        this.userPassword=userPassword;
    }
    
    public String  getUserName() {
        return userName;
    }
    
    public String  getUserRealm() {
        return userRealm;
    }
     
    public String  getUserPassword() {
        return userPassword;
    }
    
    public String toString() {
        String res="<User \n";
        if (userName!=null ) res+="name="+"\""+userName+"\"\n";
        if (userRealm!=null ) res+="realm="+"\""+userRealm+"\"\n";
        if (userPassword!=null ) res+="password="+"\""+userPassword+"\"\n";
        res+="/>\n";
        
        return res;
    }
    
}

/*
 * Binding.java
 *
 * Created on June 27, 2002, 2:04 PM
 */

package gov.nist.sip.proxy.registrar;

import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import java.util.*;
import gov.nist.sip.proxy.*;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class Registration {

    protected FromHeader fromHeader;
    protected ToHeader toHeader;
    protected String userName;
    protected String displayName;
    protected String key;
    protected Vector contactsList;
    protected boolean toExport;
    protected Vector buddyList;   // static buddy list stored by the server.
    
    /** Creates new Binding */
    public Registration() {
        toExport=true;
        contactsList=new Vector();
	buddyList = new Vector();
    }
    
    protected ExportedBinding exportBinding() {
	if (! this.toExport) return null;
	ExportedBinding retval = new ExportedBinding();

	if (this.fromHeader != null) 
	   retval.fromAddress = this.fromHeader.getAddress().toString();
	if (this.toHeader != null)
	   retval.toAddress = this.toHeader.getAddress().toString();

	if (this.contactsList != null) 
	   retval.contactAddress = 
	   ((ContactHeader) contactsList.elementAt(0)).getAddress().toString();
        
        retval.key=key;
        
        toExport=false;
	return retval;

    }
    
    public Vector getContactsList() {
        return contactsList;
    }

    public void setContactsList(Vector contactsList) {
        this.contactsList=contactsList;
    }
    
    public void addContactHeader(ContactHeader contactHeader) {
       contactsList.addElement(contactHeader);
    }
    
    public void setDisplayName(String displayName) {
        this.displayName=displayName;
    }
    
    public void setKey(String key) {
        this.key=key;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean hasContacts() {
        return !contactsList.isEmpty();
    }
    
    public void removeContactHeader(ContactHeader contactParameter) {
        Address addressParam=contactParameter.getAddress();
        javax.sip.address.URI  cleanUri=
        Registrar.getCleanUri(addressParam.getURI() );
        String contactParam=cleanUri.toString();
        
        for( int i=0; i<contactsList.size();i++) {
            ContactHeader contactHeader=(ContactHeader)contactsList.elementAt(i);
            Address address=contactHeader.getAddress();
            javax.sip.address.URI  cleanedUri=
            Registrar.getCleanUri(address.getURI() );
            String contact=cleanedUri.toString();
            
            if (contact.equals(contactParam ))  {
                contactsList.remove(i);
                ProxyDebug.println("DEBUG, Registration, removeContactHeader():"+
                " The contact: "+contact+" has been removed for the key: "+key);
                break;
            }
        }
      
    }
    
    public void updateContactHeader(ContactHeader contactParameter) {
        
        Address addressParam=contactParameter.getAddress();
        javax.sip.address.URI  cleanUri=
        Registrar.getCleanUri(addressParam.getURI() );
        String contactParam=cleanUri.toString();
        
        for( int i=0; i<contactsList.size();i++) {
            ContactHeader contactHeader=(ContactHeader)contactsList.elementAt(i);
            Address address=contactHeader.getAddress();
            javax.sip.address.URI  cleanedUri=
            Registrar.getCleanUri(address.getURI() );
            String contact=cleanedUri.toString();
            
            if (contact.equals(contactParam ))  {
                contactsList.remove(i);
                contactsList.add(i,contactParameter);
                ProxyDebug.println("DEBUG, Registration, updateContactHeader():"+
                " The contact: "+contact+" has been updated for the key: "+key);
                break;
            }
        }
    }
    
    public boolean hasContactHeader(ContactHeader contactParameter) {
        Address addressParam=contactParameter.getAddress();
        javax.sip.address.URI  cleanUri=
        Registrar.getCleanUri(addressParam.getURI() );
        String contactParam=cleanUri.toString();
        ProxyDebug.println("Contact to add:"+contactParam+" ?");
        for( int i=0; i<contactsList.size();i++) {
            ContactHeader contactHeader=(ContactHeader)contactsList.elementAt(i);
            Address address=contactHeader.getAddress();
            javax.sip.address.URI  cleanedUri=
            Registrar.getCleanUri(address.getURI() );
            String contact=cleanedUri.toString();
            
            ProxyDebug.println("Contact in the list:"+contact);
            if (contact.equals(contactParam ))  {
                ProxyDebug.println("Contact already in the list");
                return true;
            }
        }
        return false;
    }
    

    public void print() {
        ProxyDebug.println("- contacts: ");
        for( int i=0; i<contactsList.size();i++) {
            ContactHeader contactHeader=(ContactHeader)contactsList.elementAt(i);
            ProxyDebug.print("  contact "+(i+1)+" : "+contactHeader.toString());
        }
    }

    public boolean isMyBuddy(String uri) {
	// Append the buddy list to the contact.
        for( int i=0; i<buddyList.size();i++) {
	    if (uri.equalsIgnoreCase(buddyList.elementAt(i).toString())) 
		return true;
	}
	return false;
    }
	

    public String getXMLTags() {
	StringBuffer retval = new StringBuffer();

	retval.append("<REGISTRATION ");
        if (displayName!=null) {
            retval.append("display_name=\""+displayName+"\"");
        }
     
        retval.append(" uri=\""+key+"\" ");
     
        for( int i=0; i<contactsList.size();i++) {
            retval.append("     <CONTACT ");
            ContactHeader contactHeader=(ContactHeader)contactsList.elementAt(i);
            Address address=contactHeader.getAddress();
            javax.sip.address.URI  cleanedUri=
            Registrar.getCleanUri(address.getURI() );
            String contact=cleanedUri.toString();
            
            if (address.getDisplayName()!=null) {
                retval.append("display_name=\""+address.getDisplayName()+"\"");
            }
            
            retval.append(" uri=\""+contact+"\" ");
            if (contactHeader.getExpires()!=-1) {
                retval.append(" expires=\""+contactHeader.getExpires()+"\" ");
            }
            else retval.append(" expires=\""+Registrar.EXPIRES_TIME_MAX+"\" ");
            retval.append(" />\n");
            
        }
	
	// Append the buddy list to the contact.
        for( int i=0; i<buddyList.size();i++) {
	     retval.append(" <BUDDY  uri= \"").append(buddyList.elementAt(i).toString()).append("/>\n");
	}

        retval.append("</REGISTRATION>\n");
	return retval.toString();
    }
    
}

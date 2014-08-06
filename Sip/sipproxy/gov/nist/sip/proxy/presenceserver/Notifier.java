/*
 * Notifier.java
 */


package gov.nist.sip.proxy.presenceserver;

import java.util.*;
import javax.sip.Dialog;
import javax.sip.header.CallIdHeader; 
import java.io.*;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.w3c.dom.Document;

/**
 *  The notifier object represents a registered sip user.
 *  It keeps track of all the users UAs (contacts)
 *
 *  <h3>Signing off</h3>
 *  When a presentity logs off, the object is active until all
 *  subscribers have been notified and been removed from the list
 *  of subscribers.
 *
 *
 * @author  Henrik Leion
 * @version 0.1
 */


public class Notifier {

    private Document doc;

    private String    notifierURI;
    private Vector    contacts;
    private File      winfoFile;
    private File      authFile;
    /** Maps the subscriber URIs of this notifier to dialogIds. 
	These dialogIds are used by the presentityManager to find
        the correct subscriber when the notifier wants to update it**/
    private HashMap   subscriberKeys;
    private long      creationTime;
    private int       expires;

    //Presence specific fields
    private String    fullState; //file instead?
    private String    userDir;   //the notifiers home directory



    /* 
       A possible record for keeping event states .
    private class EventRecord {
	protected String event;
	protected String entity;
	protected String state;
	protected String contact
	protected int    expires;
	protected long   creationTime;
	
	public EventRecord(String ev, String en, String st, String ct, int ex) {
	    this.event=ev;
	    this.entity=en;
	    this.state=st;
	    this.contact=ct;
	    this.expires=ex;
	    this.creationTime=System.currentTimeMillis();
	}
    }
    */


    public Notifier(String uri, int exp, String contact) {
	this.notifierURI = uri;
	
	//User directory
	String username = uri.substring(uri.indexOf("sip:")+4,
					uri.indexOf("@"));
	this.userDir = "users"+File.separator+username+File.separator;
	
	//Watcher info
	String watcherText = 
	    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
	    " <watcherinfo version=\"0\" state=\"full\">\n" +
	    "  <watcher-list resource=\"" + uri + "\" package=\"presence\">\n" +
	    "  </watcher-list>\n" + 
	    " </watcherinfo>";
	this.winfoFile = new File(userDir+"winfo.xml");
	try {
	    PrintWriter out = new PrintWriter(new FileWriter(winfoFile));
	    out.println(watcherText);
	    out.close();
	} catch (Exception IOexception) {
	    System.out.println("Notifier couldn't write to file " + winfoFile);
	}


	this.authFile = null;
	/** Matches subscriberURIs with dialogIds **/
	this.subscriberKeys  = new HashMap();
	this.creationTime = System.currentTimeMillis();
	this.expires = exp;
	this.contacts = new Vector();
	if (contact != null) contacts.add((Object)contact);
	this.fullState = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
	                 "<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"" +
	                 " entity=\"" + notifierURI  + "\">\n" +
	                 "</presence>";

    }

    
    protected void finalize() {
	contacts.clear();
	subscriberKeys.clear();
	
    }



    //************************
    //*   Methods
    //************************
    

    
    protected int addSubscriber(String subscriberKey, String dialogId, String status) {
	//String previousValue = (String)
	subscriberKeys.put((Object)subscriberKey, (Object)dialogId);
	
	//write Winfo
	//new subscriber
	String text = "watcher id=\"" + subscriberKey + "\" status=\"" + 
	    status + "\" event=\"subscribe\">"+
	    subscriberKey + ">/watcher>";
	return Response.OK;
    }
    


    /**
     *  @param subscriber assumed to be not null
     *  @return responseCode
     */
    protected int removeSubscriber(String subscriberKey) {
	String key = (String)subscriberKeys.remove((Object)subscriberKey);
	if (key==null)
	    return Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST;
	else 
	    return Response.OK;
    }

   




    /** 
     *  Todo: <ul>
     *    <li>Send 412(Precondition failed) if entity-tag is not recognized</li>
     *    <li>Add support for expiration of published states</li>
     *    <li>Send 415 if content-type header is un-supported</li>
     *    <li>Allow several events</li>
     *    <li>The storage of event states is currently in a pidf-string (only presence),
     *        a better solution is some sort of record or database entry with
     *        <entity,event,state,expiration>, and generate the xml-code from this.<br />
     *        But this means that the server must understand all xml-namespaces the
     *        EPAs might want to use.</li>
     *  </ul>
     *  @returns 200 or 415 (Unsupported Media Type)
     */
    protected int processPublish(String newBody, String entityTag) {
	if (newBody == null) { 
	    return 415; 
	} else {
	    return Response.OK;
	}
    }
    


    /** Should be a bit more sophisticated **/
    protected int authorize(String uri, String method) {
       

	if (method.equals("PUBLISH")) {
	    if (uri.equals(notifierURI))
		return Response.OK;
	    //Should check if the publish came from any
	    //of the registered contacts
	    
	} else if (method.equals(Request.SUBSCRIBE)) {
	    //Check auth file
	    //check uri in authorization file
	    return  Response.OK;

	} else {
	    return Response.FORBIDDEN;
	
	}
	
	return Response.FORBIDDEN;
    }


    //************************
    //*   Getters & Setters
    //************************
    

    protected String getNotifierURI() {
	return notifierURI;
    }


    protected void setExpires(int newValue) {
	expires = newValue;
    }
    
    
    protected void addContact(String contact) {
	//See if contact already exists
	Iterator it = contacts.iterator();
	while(it.hasNext()) {
	    String temp = (String)it.next();
	    if(temp.indexOf(contact)>0) {
		break;
	    }
	}
	contacts.add((Object)contact);
    }

    
    /** One of the notifiers UA has deregistered
     *  so it must be removed and the fullState
     *  document must be updated.
     * @return the tuple-id of the removed contact
     */
    protected String removeContact(String contact) {
	
	String tupleId = new String();
	
	//remove all parameters from contact, just in case
	int scolon = contact.indexOf(";");
	contact = contact.substring(0,scolon+1).trim();
	
	//find contact and remove it
	Iterator it = contacts.iterator();
	int i = -1;
	while(it.hasNext()) {
	    String temp = (String)it.next();
	    i++;
	    if(temp.indexOf(contact)>0) {
		contacts.remove(i); //iterators operate in proper order
		break;
	    }
	}


	/*  There is no guarante that there is a known link between
	 *  a tuple id and a contact URI. This means that a de-register
	 *  cannot always find and update a removed contact.   
	 *  The clients should use Publish to update their presence status.
	 *
	 *  Strange exception when parsing contact-elements. Commented out it
	 */
	
	//System.out.println("Notifier: removeContact - Trying to remove " + contact +
	//		   " from:\n " + fullState);

	//Remove this <tuple>-element from fullState.
	//loop through oldBody, if known id is encountered, replace the tuple
	//     and remove from newBody-list. Add remaining tuples from newBodylist


	int contactIndex = fullState.indexOf(contact);
	//System.out.println("Notifier: removeContact - found contact at " + contactIndex);
	if (contactIndex>0) {
	    int tupleStart = fullState.indexOf("<tuple"); //at least one tuple always exist
	    int tupleEnd = 0;
	    String newFullState = fullState.substring(0,tupleStart);
	    while (tupleStart != -1) {
		//System.out.println("Notifier: removeContact - while");
		tupleStart = fullState.indexOf("<tuple",tupleEnd);//where the last tuple ended
		tupleEnd   = fullState.indexOf("</tuple>",tupleStart)+8;
		if(tupleEnd<contactIndex) {
		    newFullState = fullState.substring(tupleStart,tupleEnd);
		} else { //tupleStart<contactIndex<tupleEnd
		    int tupleIdStart = fullState.indexOf("id=\"",tupleStart)+4;
		    int tupleIdEnd = fullState.indexOf("\"", tupleIdStart);
		    tupleId = fullState.substring(tupleIdStart, tupleIdEnd);
		    //System.out.println("Notifier.removeContact - Removed " + 
		    //	       fullState.substring(tupleStart,tupleEnd) +
		    //		   "\n    Returned: " + tupleId);
		    break;
		}
	    }
	    newFullState.concat(fullState.substring(tupleEnd, fullState.length()));

	}
	/*System.out.println("Notifier: removeContact - finished.\n" + 
			   "\n        tupleId = " + tupleId + 
			   "\n        " + fullState);*/
	return tupleId;
    }

    
    protected boolean hasExpired() {
	return false;
	//return ((creationTime + expires*1000) < System.currentTimeMillis());
    }


    protected boolean hasEvent(String event) {

	return true;

	/*
        Iterator it = allowEventsHeaders.iterator();
	while (it.hasNext()) {
	    String temp = (String) it.next();
	    if (temp.equalsIgnoreCase(event))
		return true;
	}
	return false;
	*/
    }
    
    
    protected boolean hasNoContacts() {
	return contacts.isEmpty();
    }


    /** Are there any non-null references to subscribers? **/
    protected boolean hasSubscriber() {
	Iterator it = subscriberKeys.values().iterator();
	while (it.hasNext()) {
	    if(it.next() == null) {
		it.remove(); //a nullpointer is only a waste of space
	    } else {
		return true;
	    }
	}
	return false;
    }

   
    protected boolean hasSubscriber(String subscriberURI) {
	return subscriberKeys.containsKey((Object)subscriberURI);
    }


    protected Collection getSubscribers() {
	return subscriberKeys.values();
    }

    /*********************************
     * Methods for the presence event
     *********************************/

    protected String getFullState() {
	if (fullState.length()>0)
	    return fullState;
	else 
	    return getOfflineState();
    }

    protected void setFullState(String newFullState) {
	fullState  = newFullState;
    }

    protected String getOfflineState() {
	String basicOpen = "<basic>/sopen/s<//basic>";
	String basicClosed = "<basic>closed<//basic>";
	String offlineState = fullState.replaceAll(basicOpen,basicClosed);
	return offlineState.replaceAll("<note>.<//note>", "<note>offline<//note>");
    }

}

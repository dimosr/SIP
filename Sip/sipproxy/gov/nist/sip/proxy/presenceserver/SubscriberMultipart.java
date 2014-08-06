/*
 * SubscriberMultipart.java
 */

package gov.nist.sip.proxy.presenceserver;

import java.io.File;
import javax.sip.Dialog;
import java.util.HashMap;


/** The multipart subscriber is capable of handling a multipart
 *  notification body, as needed if subscribing to a resourcelist.
 *
 *  <h3>To add</h3><ul>
 *   <li>The very last boundary should always aend with --</li>
 *  </ul>
 *
 * @see draft-ietf-simple-event-list-04 and draft-ietf-simple-xcap-list-usage-02
 * @author Henrik Leion
 * @version 0.1
 */


public class SubscriberMultipart extends Subscriber {

    /**
     *
     * @param fromURL As seen in the Subscribe From-header, cleaned from parameters
     * @param part true if the subscriber supports partial notify
     **/

    private int version;
    private HashMap cids;
    private String boundary;
    private String mainCid;


    public SubscriberMultipart(String fromURI, Dialog d, int exp) {
	super(fromURI,d,exp);
	
	this.version = 1;
	
	//These should be generated random value of course
	this.boundary = "50sdfieidfsdfl\n";
	this.mainCid = "hsJSktsM" + fromURI.substring(fromURI.indexOf("@"),fromURI.length());
	
	this.cids = new HashMap();
	this.notifyBody = new String();
    }

    


    /** Simply takes the resource-list and adds attributes
     *  "fullState" and "version" to the <resource-lists>-element
     *  and adds "cid"-attributes to all <entity>-tags.
     *  It's probably not very correct conduct, but as of writing
     *  it's not clear how these things are going to work.
     **/
    public void setRlmiFile(File rlmi) {
	
	notifyBody = "--" + boundary +"\n" +
	    "Content-Transfer-Encoding: binary\n" +
	    "Content-ID: <" + mainCid + ">\n" +
	    "Content-Type: application/rlmi+xml\n" + 
	    //parseRlmi(rlmi) + 
	    "\n--" + boundary;
    }
    


    /**
     * @param newString The full pidf body of the Publish method
     **/
    public void updateNotifyBody(String newBody) {
	//update version
	version++;
	int resourcelistTag = newBody.indexOf("<resourcelists");
	int start = newBody.indexOf("version=\"", resourcelistTag)+9;
	int end = newBody.indexOf("\"",start);
	newBody = newBody.substring(0,start) + version +
	    newBody.substring(end,newBody.length());
	
	//Find notifier uri
	start = newBody.indexOf("entity=\"")+8;
	end = newBody.indexOf("\"", start);
	String fromURI = newBody.substring(start,end);
	String cid = (String)cids.get((Object)fromURI);

	//update notifyBody
	notifyBody = 
	    notifyBody.concat("Content-Transfer-Encoding: binary\n" +
			      "Content-ID: " + cid + "\n" +
			      "Content-Type: application/pidf+xml\n\n" +
			      newBody + "\n\n" + boundary);
    }

    


}

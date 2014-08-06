/*
 * SubscriberPresence.java
 */

package gov.nist.sip.proxy.presenceserver;

import java.io.File;
import javax.sip.Dialog;


/**
 *  This subscriber supports the presence event package, 
 *  cpid-pidf, partial notify, notify filtering as described
 *  in the SIMPLE internet-drafts.
 */


public class SubscriberPresence extends Subscriber {

    private File filter;
    private boolean partial;



    /**
     *
     * @param fromURL As seen in the Subscribe From-header, cleaned from parameters
     * @param part true if the subscriber supports partial notify
     **/
    public SubscriberPresence(String fromURI, Dialog d, int exp, boolean part) {
	super(fromURI,d,exp);
	    
	this.partial = part;

	//Strip domain from fromURI and use this as a directory name
	//If exists, this is where the files are found
	int colon = fromURI.indexOf(":");
	int at = fromURI.indexOf("@");
	String fileName = new String(fromURI.substring(colon,at) + File.separator + 
				     "filter.xml"); 
	this.filter = new File(fileName);
    }


    public void setFilter(File f) {
	filter = f;
    }


    


    /**
     * @param newString The full pidf body of the Publish method
     **/
    public void updateNotifyBody(String newString) {
	notifyBody = newString;
	
    }



}

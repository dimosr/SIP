/*
 * SubscriberResourceList.java
 */

package gov.nist.sip.proxy.presenceserver;

import java.io.File;
import java.util.HashMap;
import javax.sip.Dialog;


/**
 * A ResourceList is a subscriber. It can be added to a notifiers
 * list of subscribers and has a updateNotifyBody-method just like
 * all other Subscribers, but a ResourceList also has a set of 
 * subscribers on it's own, and in that sense acts as a Notifier.
 * It would be suitable to convert at least  the Subscriber class into
 * an Interface, so that this class will be more natural.
 *
 * @author Henrik Leion
 * @version 0.1
 */


public class ResourceList extends Subscriber {

    private File resourceList;
    /** Maps the subscriber URIs of this resourcelist to dialogIds. 
	These dialogIds are used by the presentityManager to find
        the correct subscriber when the resourcelist wants to update it**/
    private HashMap   subscriberKeys;


    /**
     *
     * @param fromURL As seen in the Subscribe From-header, cleaned from parameters
     * @param part true if the subscriber supports partial notify
     **/
    public ResourceList(String resourceListURI, Dialog d, int exp, File rl) {
	super(resourceListURI,d,exp);
	    
	this.resourceList = rl;
	this.subscriberKeys  = new HashMap();

    }



    /**
     * @param newString The full pidf body of the Publish method
     **/
    public void updateNotifyBody(String newString) {
	notifyBody = newString;
	
    }

    public int addSubscriber(String subscriberUrl, String dialogId, String subscriptionState) {
	subscriberKeys.put((Object)subscriberUrl, (Object)dialogId);
	//Resourcelist should also have Winfo-files, copy from Notifier
	return 200;
    }

    
    public String getFilename() {
	return resourceList.getName();
    }

    public int  authorize(String subscriberKey, String method) {
	return 200;
    }

}

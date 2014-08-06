/**
 * VirtualSubscription.java
 */

package gov.nist.sip.proxy.presenceserver;

import gov.nist.sip.proxy.*;
import java.util.*;
import javax.sip.Dialog;


/**
 *  A virtual subscription is a subscription that
 *  the Resource List Server (colocated with the Presence Server
 *  in this implementation) issues if someone subscribes to a 
 *  resource list containing external references.
 *  <p />
 *  VirtualSubscriptions are sent to one PA, and thus what we
 *  receive is the full pidf for all the PA's PUAs (in other
 *  words, if the notifier we are subscribing to has a
 *  presenceServer like this one, the presenceServer will
 *  compose a full state for us).
 *  <p />
 *  A VirtualSubscription has Subscribers, just like the Notifier object.
 *
 *
 * @author Henrik Leion
 * @version 0.1
 * @see draft-ietf-simple-event-list-04  
 */
public class VirtualSubscription {

    private Dialog    dialog;
    private HashMap   subscribers;
    /** Collects the extra time that the subscribers require. Used when resubscribing to th virtualSubscription **/
    private int       extraTime; 
    private int       expiresTime;
    private long      creationTime;
    private String    subscriptionState;
    private String    fullState; //File instead?
    private String    subscriberURI;
    

    protected VirtualSubscription(String subscriberURI, Dialog d, int e) {
	this.dialog = d;
	this.expiresTime = e;
	this.subscribers = new HashMap();
	this.creationTime = System.currentTimeMillis();
	this.fullState = new String();
	this.extraTime = 0;
	this.subscriptionState = javax.sip.header.SubscriptionStateHeader.PENDING;
    }


    /** Todo: <ul>
     *    <li>Could support partial presence</li>
     *  </ul>
     */
    protected int processNotify(String newBody) {
	fullState = newBody;
	return 200;
    }

    
    /** Adds a new subscriber and sets the extraTime field
     *  to this subscribers expires-time if larger. This means that
     *  the newExpires field will contain a sufficiently large
     *  number when resubscribing to the notifier.
     *
     */
    protected void addSubscriber(Subscriber subscriber, 
				 String  subscriberKey, int subscribeExpires) {

	//Does this subscription need more time than what we have subscribed for?
	int temp = subscribeExpires-getExpiresTime();
	if (temp>0) {
	    extraTime = temp;
	}

	
	

    }

    /**
     * If we have added subscribers that need this subscription
     * to stay alive longer than what is negotiated, the extraTime
     * field is larger than zero.
     */
    protected boolean needToResubscribe() {
	return extraTime>0;
    }


    //***********************
    //  Getters & Setters
    //***********************


    protected Dialog getDialog() {
	return dialog;
    }

    public String getURI() {
	return subscriberURI;
    }


    /** Returns remaining time of subscription
     *  @author Henrik Leion
     */
    public int getExpiresTime() {
	long temp = creationTime + expiresTime*1000 - System.currentTimeMillis();
       	return (int)temp/1000;
    }
    
    public void setExpiresTime(int newExpires) {
	creationTime = System.currentTimeMillis();
	expiresTime=newExpires;
	extraTime = extraTime-newExpires;
	if (extraTime<newExpires)
	    extraTime=0;
	else
	    extraTime=extraTime-newExpires;
    }

    /** Returns an expires time that is as large
     *  as the current subscribers require
     */
    public int getRequiredExpiresTime() {
	return extraTime;
    }


    protected boolean hasExpired() {
	return ((creationTime + expiresTime*1000) < System.currentTimeMillis());
    }

    protected boolean hasNoSubscribers() {
	return subscribers.isEmpty();
    }


    protected String getSubscriptionState() {
	return subscriptionState;
    }

    protected void setSubscriptionState(String newState) {
	subscriptionState = newState;
    }


    protected Collection getSubscribers() {
	return subscribers.values();
    }


}


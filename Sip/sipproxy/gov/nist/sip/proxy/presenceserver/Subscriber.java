/*
 * Subscriber.java
 */

package gov.nist.sip.proxy.presenceserver;


import javax.sip.Dialog;


/**
 *  The subscriber object contains information of a subscriber
 *  and the subscription. It keeps track of creation and expires
 *  times and the dialog in which the subscription exists.
 *  The object also buffers the next notification body so that
 *  several notifiers may update it.
 *
 *  This object is subclassed for use in specific event packages.
 *  Note that Java uses dynamic method lookup, which means that the
 *  correct overridden method is used. Very handy.
 *
 *  Subscriber should be upgraded to an Interface, that would make the
 *  ResourceList extension more comprehensible.
 * 
 *  @author Henrik Leion
 *  @version 0.1
 * 
 **/



public class Subscriber {

    /** As found in the From-header of a Subscribe **/
    protected String subscriberURI;
    protected Dialog dialog;
    protected int    expires;
    protected long   creationTime;
    protected String notifyBody;
    protected String subscriptionState;



    public Subscriber(String subscriberURI, Dialog d, int exp) {
	this.subscriberURI = subscriberURI;
	this.dialog = d;
	this.expires = exp;
	this.creationTime = System.currentTimeMillis();
	this.notifyBody = new String();
    }


    /** This default method overwrites the notifyBody string 
     *  @param newString Must be a valid message, so that it can be put 
     *         into a Notify request directly. 
     **/
    public void updateNotifyBody(String newString) {

	//Should be able to merge notifyBodies. See PresentityManager:processPublish()
	
	notifyBody = newString;
    }


    //**********************
    //*  Getters & Setters
    //**********************


    public void setSubscriptionState(String newState) {
	subscriptionState = newState;
    }

    public String getSubscriptionState() {
	return subscriptionState;
    }

    public void setExpires(int newValue) {
	expires = newValue;
	creationTime = System.currentTimeMillis();
    }

    
    /** Returns remaining time of subscription
     *  @author Henrik Leion
     */
    public int getExpiresTime() {
	long temp = creationTime + expires*1000 - System.currentTimeMillis();
       	return (int)temp/1000;
    }

    protected boolean hasExpired() {
	return (getExpiresTime()<=0);
     }

    public boolean isTerminated() {
	return subscriptionState.equals(javax.sip.header.SubscriptionStateHeader.TERMINATED);
    }

    public String getSubscriberURL() {
	return subscriberURI;
    }

    public String removeNotifyBody() {
	String temp = notifyBody;
	notifyBody = new String();
	return temp;
    }

    protected Dialog getDialog() {
	return dialog;
    }


    /* returns true if the subscriber wishes to send a Notify reqiest */
    public boolean sendNotify() {
	return ((notifyBody.length()>0) && !isTerminated());
    }

   public String getNotifyBody() {
	return notifyBody;
    }

}

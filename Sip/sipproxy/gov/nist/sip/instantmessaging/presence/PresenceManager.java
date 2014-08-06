/*
 * SubscriberController.java
 *
 * Created on September 26, 2002, 6:00 PM
 */

package gov.nist.sip.instantmessaging.presence;

import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import java.util.*;
import gov.nist.sip.instantmessaging.*;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class PresenceManager {

    protected int expiresTime;
    private PresentityList presentityList;
    private SubscriberList subscriberList;
    private IMUserAgent imUserAgent;
    
    /** Creates new SubscriberController */
    public PresenceManager(IMUserAgent imUserAgent) {
        expiresTime=3600;
        this.imUserAgent=imUserAgent;
        presentityList=new PresentityList();
        subscriberList=new SubscriberList();
    }
    
    public int getExpiresTime(){
        return expiresTime;
    }
    
    public Vector getAllSubscribers() {
        return subscriberList.getAllSubscribers();
    }
    
    public Vector getAllPresentities() {
        return presentityList.getAllPresentities();
    }
    
    public void updatePresentity(String presentityName,String status) {
       presentityList.updatePresentity(presentityName,status);
    }
    
    public Presentity getPresentity(String presentityName) {
        return presentityList.getPresentity(presentityName);
    }
    
    public Subscriber getSubscriber(String subscriberName) {
        return subscriberList.getSubscriber(subscriberName);
    }
    
    public boolean hasPresentity(String presentityName) {
        return presentityList.hasPresentity(presentityName);
    }
    
    public void addPresentity(String presentityName,Response okReceived,Dialog dialog) {
       Presentity presentity=new Presentity(presentityName,okReceived);
       presentity.setDialog(dialog);
       presentityList.addPresentity(presentity);
    }
    
    public void removePresentity(String presentityName) {
        presentityList.removePresentity(presentityName);
    }
    
    public void changePresentityStatus(String presentityName,String status) {
        presentityList.changePresentityStatus(presentityName,status);
    }
    
    
    public boolean hasSubscriber(String subscriberName) {
        return subscriberList.hasSubscriber(subscriberName);
    }
    
    public void addSubscriber(String subscriberName,Response okSent,Dialog dialog) {
       Subscriber subscriber=new Subscriber(subscriberName,okSent);
       subscriber.setDialog(dialog);
       subscriberList.addSubscriber(subscriber);
    }
    
    public void removeSubscriber(String subscriberName) {
        subscriberList.removeSubscriber(subscriberName);
    }
    
}

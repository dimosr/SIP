/*
 * SubscriberList.java
 *
 * Created on October 3, 2002, 6:48 PM
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
public class SubscriberList {

    private Hashtable subscriberList;
   
    /** Creates new SubscriberController */
    public SubscriberList() {
         subscriberList=new Hashtable();
    }
    
    public Vector getAllSubscribers() {
        if (subscriberList!=null) {
            Collection collection=subscriberList.values();
            return new Vector(collection);
        }
        return new Vector();
    }
    
    public boolean hasSubscriber(String subscriberName) {
        Subscriber subscriber=(Subscriber)subscriberList.get(subscriberName);
        if (subscriber==null) return false;
        else return true;
    }
    
    public Subscriber getSubscriber(String subscriberName) {
        return (Subscriber)subscriberList.get(subscriberName);
    }
    
    public void addSubscriber(Subscriber subscriber) {
        String subscriberName=subscriber.getSubscriberName();
        if ( hasSubscriber(subscriberName) ) {
           DebugIM.println("DEBUG, subscriberList, addSubscriber(), "+
            "We add a new subscriber: "+subscriberName);
           subscriberList.put(subscriberName,subscriber);
        }
        else {
            DebugIM.println("DEBUG, subscriberList, addSubscriber(), "+
            "We update the subscriber: "+subscriberName);
            subscriberList.put(subscriberName,subscriber);
        }
        printSubscriberList();
    }
    
    public void removeSubscriber(String subscriberName) {
        Subscriber subscriber=(Subscriber)subscriberList.get(subscriberName);
        if (subscriber!=null) {
            DebugIM.println("DEBUG: subscriberList, removeSubscriber(), "+
            " the subscriber "+subscriberName+" has been removed");
            subscriberList.remove(subscriberName);
        }
        else DebugIM.println("DEBUG: subscriberList, removeSubscriber(), "+
            " the subscriber: "+subscriberName+" was not found...");
        printSubscriberList();
    }
    
    public void printSubscriberList() {
        Collection collection=subscriberList.values();
        Vector subscribers=new Vector(collection);
        DebugIM.println();
        DebugIM.println("************* DEBUG subscriberList    ************************************");
        DebugIM.println("************* Subscribers  record:    ************************************");
        DebugIM.println();
        for (int i=0;i<subscribers.size();i++) {
            Subscriber subscriber=(Subscriber)subscribers.elementAt(i);
            DebugIM.println("subscriber URL : "+subscriber.getSubscriberName());
            DebugIM.println();
        }
        DebugIM.println("**************************************************************************");
        DebugIM.println();
    }
    
}

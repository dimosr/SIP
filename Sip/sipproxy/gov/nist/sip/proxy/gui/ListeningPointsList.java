/*
 * ListeningPointsList.java
 *
 * Created on March 17, 2003, 11:41 AM
 */

package gov.nist.sip.proxy.gui;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import gov.nist.sip.proxy.*;
/**
 *
 * @author  Administrator
 */
public class ListeningPointsList extends JList {
    
    protected DefaultListModel list;
    protected ProxyLauncher proxyLauncher;
    protected Hashtable lps;
    protected int counter=0;
    
    /** Creates a new instance of ListeningPointsList */
    public ListeningPointsList(ProxyLauncher proxyLauncher) {
        this.proxyLauncher=proxyLauncher;
        list=new DefaultListModel();
        lps=new Hashtable();
        this.setModel(list);
    }
    
    public synchronized void displayList(Hashtable listeningPoints) {
        list.removeAllElements();
        
        if (listeningPoints!=null && listeningPoints.size()!=0) {
            Vector vec=new Vector(listeningPoints.values());
            for (int i=0;i<vec.size();i++) {
                Association association=(Association) vec.elementAt(i);
                lps.put("lp"+i,association);
                list.addElement(association.port+":"+association.transport);
            }
        }
        else  list.addElement("(empty)");
    }
    
    public boolean hasListeningPoint(String port,String transport) {
       Enumeration e=lps.keys();
       while (e!=null && e.hasMoreElements()) {
            String key=(String)e.nextElement();
            Association asso=(Association)lps.get(key);
            if (asso.port.equals(port) && asso.transport.equals(transport))
            {
                return true;
            }
       }      
       return false;
    }
    
    public void addListeningPoint(String port,String transport) {
       
        if ( hasListeningPoint(port,transport) )
             new AlertFrame("The listening point is already in the list",
                JOptionPane.ERROR_MESSAGE);
        else {
            counter++;
            lps.put
		("listeningPoint"+counter,new Association(port,transport) );
            if (list.size()==1) {
                String e=(String)list.get(0);
                if (e.equals("(empty)")) {
                    list.remove(0);
                }
            }
            list.addElement(port+":"+transport);
        }
    }
    
    public void removeListeningPoint(String port,String transport) {
        Enumeration e=lps.keys();
        while (e!=null && e.hasMoreElements()) {
            String key=(String)e.nextElement();
            Association asso=(Association)lps.get(key);
            if (asso.port.equals(port) && asso.transport.equals(transport))
            {
                lps.remove(key);
                break;
            }
        }      
    }
    
    public String getLP(int index) {
        try{
            String lp=(String)list.get(index);
            if (lp.equals("(empty)"))
                return null;
            
            return lp;
        }
        catch(Exception e) {
            return null;
        }
    }
    
     public boolean isSelected() {
        String lp=null;
        if (this.getSelectedIndex()==-1)
            return false;
        else {
            lp=getLP(this.getSelectedIndex());
          
            if ( lp==null || lp.equals("(empty)"))
                return false;
            else return true;
        }
    }
    
    public void removeSelectedListeningPoint() {
        if (isSelected()) {
            String lp=getLP(this.getSelectedIndex());
            
            if (lp!=null) {
                try{
                   int i=lp.indexOf(":");
                   String port=lp.substring(0,i);
                   String transport=lp.substring(i+1);
                   removeListeningPoint(port,transport);
                   
                   list.remove(this.getSelectedIndex());
                  
                }
                catch(Exception e) {

                }
            }
            
            if (list.size()==0)
                list.addElement("(empty)");
        }
        else {
            new AlertFrame("You must select a listening point to remove!",
            JOptionPane.ERROR_MESSAGE);
        }
    }
    

}

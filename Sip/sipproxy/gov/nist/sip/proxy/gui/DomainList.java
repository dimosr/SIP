/*
 * DomainList.java
 *
 * Created on April 11, 2003, 12:31 PM
 */

package gov.nist.sip.proxy.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import gov.nist.sip.proxy.registrar.*;
import gov.nist.sip.proxy.*;
/**
 *
 * @author  deruelle
 */
public class DomainList  extends JList{
    
    protected DefaultListModel list;
    protected ProxyLauncher proxyLauncher;
    protected Vector domains;
    protected int counter=0;
    
    /** Creates a new instance of DomainList */
    public DomainList(ProxyLauncher proxyLauncher) {
        this.proxyLauncher=proxyLauncher;
        list=new DefaultListModel();
        domains=new Vector();
        this.setModel(list);
    }
    
    public synchronized void displayList(Vector v) {
        list.removeAllElements();
        
        if (v!=null ) {
            for (int i=0;i<v.size();i++) {
                String domain=(String) v.elementAt(i);
                domains.addElement(domain);
                list.addElement(domain);
            }
        }
        else  list.addElement("(empty)");
    }
    
    public boolean hasDomain(String dom) {
        if (domains!=null ) {
            for (int i=0;i<domains.size();i++) {
                String domain=(String) domains.elementAt(i);
                if (domain.equals(dom))
                    return true;
            }
            
        }return false;
    }

    public void addDomain(String dom) {
       
        if ( hasDomain(dom) )
             new AlertFrame("The domain is already in the list",
                JOptionPane.ERROR_MESSAGE);
        else {
            domains.addElement(dom);
            if (list.size()==1) {
                String e=(String)list.get(0);
                if (e.equals("(empty)")) {
                    list.remove(0);
                }
            }
            list.addElement(dom);
        }
    }
    
    public void removeDomain(String dom) {
       if (domains!=null ) {
            for (int i=0;i<domains.size();i++) {
                String domain=(String) domains.elementAt(i);
                if (domain.equals(dom)) {
                    domains.remove(i);
                    return;
                }
            }
        }
    }
    
    public String getDom(int index) {
        try{
            String dom=(String)list.get(index);
            if (dom.equals("(empty)"))
                return null;
            
            return dom;
        }
        catch(Exception e) {
            return null;
        }
    }
    
     public boolean isSelected() {
        String dom=null;
        if (this.getSelectedIndex()==-1)
            return false;
        else {
            dom=getDom(this.getSelectedIndex());
          
            if ( dom==null || dom.equals("(empty)"))
                return false;
            else return true;
        }
    }
    
    public void removeSelectedDomain() {
        if (isSelected()) {
            String dom=getDom(this.getSelectedIndex());
            
            if (dom!=null) {
                try{
                   removeDomain(dom);
                   
                   list.remove(this.getSelectedIndex());
                  
                }
                catch(Exception e) {

                }
            }
            
            if (list.size()==0)
                list.addElement("(empty)");
        }
        else {
            new AlertFrame("You must select a domain to remove!",
            JOptionPane.ERROR_MESSAGE);
        }
    }
    

}

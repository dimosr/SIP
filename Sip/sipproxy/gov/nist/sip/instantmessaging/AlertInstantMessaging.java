/*
 * AlertFrame.java
 *
 * Created on April 10, 2002, 12:11 PM
 */

package gov.nist.sip.instantmessaging;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class AlertInstantMessaging extends JOptionPane {

    public String finalInputValue;
    public static int CONFIRMATION=12345;
    private int confirmationResult;
  
    /** Creates new AlertIM */
    public AlertInstantMessaging(String text, int messageType) {
        super("Alert");
        if (messageType==JOptionPane.ERROR_MESSAGE)
            showMessageDialog(this,text,"Error",messageType);
        else
        if (messageType==JOptionPane.WARNING_MESSAGE)
            showMessageDialog(this,text,"Warning",messageType);
        else
        if (messageType==JOptionPane.INFORMATION_MESSAGE)
            showMessageDialog(this,text,"Information",messageType);
        else  showMessageDialog(this,"Unknown alert message");
        
    }
    
    public int getConfirmationResult() {
        return confirmationResult;
    }
    
     /** Creates new AlertFrame */
    public AlertInstantMessaging(String text, int messageType, String initialInputValue) {
        super("Alert");
        finalInputValue=null;
        //Debug.println(messageType);
        if (messageType==JOptionPane.ERROR_MESSAGE)
            showMessageDialog(this,text,"Error",messageType);
        else
        if (messageType==JOptionPane.WARNING_MESSAGE)
            showMessageDialog(this,text,"Warning",messageType);
        else
        if (messageType==JOptionPane.INFORMATION_MESSAGE) {
                finalInputValue=(String)showInputDialog(this,text,"Information",messageType,
                null,null,initialInputValue);
        }
        else 
        if (messageType==CONFIRMATION ) {
            confirmationResult=JOptionPane.showConfirmDialog(this,text ,
               "Confirmation", JOptionPane.YES_NO_OPTION);
        }
        else  showMessageDialog(this,"Unknown alert message");
        
    }
    
     /** Creates new AlertFrame */
    public AlertInstantMessaging(String text) {
        super("Alert");
        // information message by default
        showMessageDialog(this,text);
    }
   
}
   
   
        
     

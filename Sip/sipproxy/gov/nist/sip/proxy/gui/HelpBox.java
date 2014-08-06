/*
 * HelpBox.java
 *
 * Created on April 15, 2002, 10:55 AM
 */

package gov.nist.sip.proxy.gui;


import javax.swing.*;
import javax.swing.border.*;
import java.awt.* ;
import java.awt.event.* ;
import java.io.* ;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class HelpBox extends Dialog {

    TextArea helpTextArea;
    Button ok;
    File helpFile;
    
    
    /** Creates new HelpBox */
    public HelpBox() {
        super(new Frame()," Proxy Help ",false);
        try{
            // ECE355 - Change path to help file so that it will work from Eclipse
            helpFile = new File("./gov/nist/sip/proxy/gui/helpProxy.txt") ;
            this.setLayout(new BorderLayout()) ;
            this.setBackground(Color.lightGray);
            helpTextArea = new TextArea();
            helpTextArea.setBackground(Color.white);
            helpTextArea.setEditable(false) ;
            
            Color color=helpTextArea.getBackground();
            //System.out.println("color:"+color.equals(Color.white));
           
     
            ok = new Button(" Ok ") ;
            ok.setBackground(Color.lightGray);
            ok.setForeground(Color.black);
            this.add(helpTextArea,BorderLayout.CENTER) ;
            this.add(ok,BorderLayout.SOUTH) ;
            ok.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){
                    setVisible(false) ;	
                    dispose();
                }
            }
            ) ;
            this.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    setVisible(false) ;
                    dispose();
                }
            }) ;
            // width, height
            this.setSize(400,400) ;		
            
                   // fill the help box.
            
            BufferedReader buffReader  = 
            new BufferedReader(
            new InputStreamReader(
            new FileInputStream(helpFile))) ;
            String line = null ;
            
            while((line = buffReader.readLine()) != null)					
                helpTextArea.append(line+"\n") ;
            
            
            
            
            
        }
        catch(Exception e){
            System.out.println
	     ("Problem while opening the help file \"help/gov/nist/sip/proxy/gui/helpProxy.txt\"") ;
            new AlertFrame("The help file can not be found",JOptionPane.ERROR_MESSAGE);
        }
    }
    
}

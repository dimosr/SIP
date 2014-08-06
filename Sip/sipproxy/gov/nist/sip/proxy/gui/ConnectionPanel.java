/*
 * 
 *
 * Created on April 1, 2002, 3:08 PM
 */

package gov.nist.sip.proxy.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import gov.nist.sip.proxy.*;
import javax.swing.event.*;
/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class ConnectionPanel extends JPanel implements ChangeListener{
    
     protected JPanel firstPanel;
   
     protected JLabel maxConnectionsLabel;
     protected JLabel maxServerTransactionsLabel;
     protected JLabel threadPoolSizeLabel;
     
     protected JSlider maxConnectionsSlider;
     protected JSlider maxServerTransactionsSlider;
     protected JSlider threadPoolSizeSlider;
    
     protected ProxyLauncher proxyLauncher;
    
    /** Creates new form DebuggingFeaturesFrame */
     public ConnectionPanel(ProxyLauncher proxyLauncher) {
        this.proxyLauncher=proxyLauncher;
        
    
        initComponents();
        
        // Init the components input:
        try{
            Configuration configuration=proxyLauncher.getConfiguration();
            if (configuration==null) return;
            
            if (configuration.maxConnections!=null && !configuration.maxConnections.trim().equals("")) {
                try{
                    maxConnectionsSlider.setValue(Integer.valueOf(configuration.maxConnections).intValue());
                }
                catch(Exception e) {}
            }
            else maxConnectionsSlider.setValue(20);
            if (configuration.maxServerTransactions!=null && !configuration.maxServerTransactions.trim().equals("")) {
                try{
                    maxServerTransactionsSlider.setValue(Integer.valueOf(configuration.maxServerTransactions).intValue());
                }
                catch(Exception e) {}
            }
            else maxServerTransactionsSlider.setValue(20);
            if (configuration.threadPoolSize!=null && !configuration.threadPoolSize.trim().equals("")) {
                try{
                    threadPoolSizeSlider.setValue(Integer.valueOf(configuration.threadPoolSize).intValue());
                }
                catch(Exception e) {}
            }
            else threadPoolSizeSlider.setValue(20);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
     
    }
    
  
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    public void initComponents() {
   
        /****************** The components    **********************************/
        JPanel panel=new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10,2,2,2));
        // If put to False: we see the container's background
        panel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        panel.setLayout( new GridLayout(2,1,0,3) );
        this.setLayout( new GridLayout(3,1,3,3) );
        this.setBorder(BorderFactory.createEmptyBorder(3,2,2,3));
        this.add(panel);
        
        maxConnectionsLabel=new JLabel("Maximum connections:");
        maxConnectionsLabel.setToolTipText("The maximum number of connections authorized by the proxy");
        // Alignment of the text
        maxConnectionsLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        maxConnectionsLabel.setForeground(Color.black);
        // Size of the text
        maxConnectionsLabel.setFont(new Font("Dialog", 1, 12));
        // If put to true: we see the label's background
        maxConnectionsLabel.setOpaque(true);
        maxConnectionsLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        maxConnectionsLabel.setBorder(ProxyLauncher.labelBorder);
        panel.add( maxConnectionsLabel);
        
        maxConnectionsSlider= new JSlider(JSlider.HORIZONTAL,
                1, 20, 1);
        maxConnectionsSlider.addChangeListener(this);
        maxConnectionsSlider.setMajorTickSpacing(5);
        maxConnectionsSlider.setMinorTickSpacing(1);
        maxConnectionsSlider.setPaintTicks(true);
        maxConnectionsSlider.setPaintLabels(true);
        maxConnectionsSlider.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        panel.add( maxConnectionsSlider);
        
        panel=new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10,2,2,2));
        // If put to False: we see the container's background
        panel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        panel.setLayout( new GridLayout(2,1,0,3) );
        this.setBorder(BorderFactory.createEmptyBorder(3,2,2,3));
        this.add(panel);
        
        maxServerTransactionsLabel=new JLabel("Maximum server transactions:");
        maxServerTransactionsLabel.setToolTipText("The maximum number of server transactions"+
        " authorized by the proxy");
        // Alignment of the text
        maxServerTransactionsLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        maxServerTransactionsLabel.setForeground(Color.black);
        // Size of the text
        maxServerTransactionsLabel.setFont(new Font("Dialog", 1, 12));
        // If put to true: we see the label's background
        maxServerTransactionsLabel.setOpaque(true);
        maxServerTransactionsLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        maxServerTransactionsLabel.setBorder(ProxyLauncher.labelBorder);
        panel.add( maxServerTransactionsLabel);
        
        maxServerTransactionsSlider= new JSlider(JSlider.HORIZONTAL,
                                      1, 20, 1);
        maxServerTransactionsSlider.addChangeListener(this);
        maxServerTransactionsSlider.setMajorTickSpacing(5);
        maxServerTransactionsSlider.setMinorTickSpacing(1);
        maxServerTransactionsSlider.setPaintTicks(true);
        maxServerTransactionsSlider.setPaintLabels(true);
        maxServerTransactionsSlider.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        panel.add( maxServerTransactionsSlider);
        
        panel=new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10,2,2,2));
        // If put to False: we see the container's background
        panel.setOpaque(false);
        //rows, columns, horizontalGap, verticalGap
        panel.setLayout( new GridLayout(2,1,0,2) );
        this.setBorder(BorderFactory.createEmptyBorder(3,2,2,3));
        this.add(panel);
        
        threadPoolSizeLabel=new JLabel("Thread pool size:");
        threadPoolSizeLabel.setToolTipText("The size of the thread pool"+
        " authorized by the proxy");
        // Alignment of the text
        threadPoolSizeLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        threadPoolSizeLabel.setForeground(Color.black);
        // Size of the text
        threadPoolSizeLabel.setFont(new Font("Dialog", 1, 12));
        // If put to true: we see the label's background
        threadPoolSizeLabel.setOpaque(true);
        threadPoolSizeLabel.setBackground(ProxyLauncher.labelBackGroundColor);
        threadPoolSizeLabel.setBorder(ProxyLauncher.labelBorder);
        panel.add( threadPoolSizeLabel);
        
        threadPoolSizeSlider= new JSlider(JSlider.HORIZONTAL,
                                      1, 20, 1);
        threadPoolSizeSlider.addChangeListener(this);
        threadPoolSizeSlider.setMajorTickSpacing(5);
        threadPoolSizeSlider.setMinorTickSpacing(1);
        threadPoolSizeSlider.setPaintTicks(true);
        threadPoolSizeSlider.setPaintLabels(true);
        threadPoolSizeSlider.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        panel.add( threadPoolSizeSlider);  
    }
    
   public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
	    int fps = (int)source.getValue();
	    //ProxyDebug.println("DEBUG, the slider moved.");
        }
    }
    
   
   public String getMaxConnections() {
        int value= maxConnectionsSlider.getValue();
        return String.valueOf(value);
   }
   
    public String getMaxServerTransactions() {
        int value= maxServerTransactionsSlider.getValue();
        return String.valueOf(value);
   }
    
   public String getThreadPoolSize() {
        int value= threadPoolSizeSlider.getValue();
        return String.valueOf(value);
   }
   
    
}


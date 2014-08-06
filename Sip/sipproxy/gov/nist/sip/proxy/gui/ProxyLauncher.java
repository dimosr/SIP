package gov.nist.sip.proxy.gui;

/*
 * ProxyLauncher.java
 *
 * Created on April 8, 2002, 10:10 AM
 */

import gov.nist.sip.proxy.Configuration;
import gov.nist.sip.proxy.Proxy;
import gov.nist.sip.proxy.ProxyDebug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
/**
 *
 * @author  olivier
 * @version 1.0
 */
public class ProxyLauncher extends JFrame{

    // Menus
    protected JMenuBar menuBar;
    protected JMenu menuMenu;
    protected JMenu optionsMenu;
    protected JMenu helpMenu;
    protected JMenu quit;
    
    protected JMenuItem configurationMenuItem;
  
    // The 2 panels: one for the labels and texts, the other for the buttons
    protected JPanel firstPanel;
    protected JPanel secondPanel;
    protected JPanel thirdPanel;
    protected JPanel fourthPanel;
    
    protected JButton proxyButton;
    protected JButton traceViewerButton;
    
    protected RegistrationsList registrationsList;
    protected Configuration configuration;
    protected ListenerProxy listenerProxy;
   
    private String configurationFile;
    protected Proxy proxy;
    
    // All for the container:
    protected static Color containerBackGroundColor=new Color(204,204,204);
    protected static String logo="logoNist-gray.jpg";
    
    // All for the labels:
    protected static Border labelBorder=new EtchedBorder(EtchedBorder.RAISED);
    protected static Color labelBackGroundColor=new Color(217,221,221);
    
    // All for the TextField
    protected static Color textBackGroundColor=Color.white;
    
    // All for the Button
    protected static Border buttonBorder=new BevelBorder(BevelBorder.RAISED);
    protected static Color  buttonBackGroundColor=new Color(186,175,175);
   
  
    public ProxyLauncher(String configFile) {
        super("NIST-SIP proxy interface");
        System.out.println("Initialisation Proxy Interface");
        
        try {
            if (configFile==null) {
                throw new Exception("ERROR, specify the configuration file on the"+
                " command line.");
            }
            else configurationFile=configFile;
              
            // First thing to do, get the configurations.
            proxy=new Proxy(configurationFile);
            
            listenerProxy=new ListenerProxy(this);
            initComponents();
            
            show();
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
          
    public Proxy getProxy() {
        return proxy;
    }
    
    public void setProxy(Proxy proxy) {
        this.proxy=proxy;
    }
    
    public RegistrationsList getRegistrationsList() {
        return registrationsList;
    }
    
    public String getConfigurationFile() {
            return configurationFile;
    }
    
    public Configuration getConfiguration() {
        if (proxy!=null) return proxy.getConfiguration();
        else return null;
    }
   
    public ListenerProxy getListenerProxy(){
         return listenerProxy;
    }
    
  

/*******************************************************************************/
/*******************************************************************************/
/*******************************************************************************/
    
    
    public void initComponents() {
        /********************** The main container ****************************/
        Container container=this.getContentPane();
        container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));
        container.setBackground(containerBackGroundColor);
        
        // width, size:
        setSize(350,400);
        setLocation(0,0);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                clean();
            }
        });
   
        
        /********************** Menu bar **************************************/
        menuBar=new JMenuBar();
        setJMenuBar(menuBar);
        // create a menu and add it to the menubar
        menuMenu=new JMenu(" Menu ");
        menuMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
        menuMenu.setToolTipText("Main menu of the proxy");
        
        // create sub-menus belonging to the main menu
        configurationMenuItem=new JMenuItem("Configuration");
        configurationMenuItem.setToolTipText("Configure the stack");
        configurationMenuItem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                  listenerProxy.configurationActionPerformed(evt);
              }
          }
        );
        
     
        menuMenu.add(configurationMenuItem);
        
      
     
        // add the menu to the menu bar
        menuBar.add(menuMenu);
        
        //...create and add some menus...
        menuBar.add(Box.createHorizontalGlue());
        
         
        helpMenu=new JMenu(" Help ");
        helpMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
        helpMenu.setToolTipText("Some useful notes about this tool");
        helpMenu.addMouseListener( new MouseAdapter() {
            public void mouseClicked(MouseEvent evt){
                    listenerProxy.helpMenuMouseEvent(evt);
              }
          }
        );
        menuBar.add(helpMenu);
          
        quit=new JMenu(" Quit ");
        quit.setBorder(new BevelBorder(BevelBorder.RAISED));
        quit.setToolTipText("Quit the application");
        quit.addMouseListener( new MouseAdapter() {
            public void mouseClicked(MouseEvent evt){
                clean();
            }
        }
        );
        menuBar.add(quit);
        
      
      
        /*************************** Main Panel ********************************/
       
        firstPanel=new JPanel();
        // Top, left, bottom, right
        firstPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        // If put to False: we see the container's background
        firstPanel.setOpaque(false);
        firstPanel.setBackground(Color.lightGray);
        //rows, columns, horizontalGap, verticalGap
        firstPanel.setLayout( new BorderLayout() );
        container.add(firstPanel);
      
        JLabel registrationsLabel=new JLabel("Registrations:");
        //registrationsLabel.setToolTipText("Click on a registration to get the contacts addresses!!");
        // Alignment of the text
        registrationsLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        registrationsLabel.setForeground(Color.black);
        // Size of the text
        registrationsLabel.setFont(new Font ("Dialog", 1, 14));
        // If put to true: we see the label's background
        registrationsLabel.setOpaque(true);
        registrationsLabel.setBackground(labelBackGroundColor);
        registrationsLabel.setBorder(labelBorder);
        firstPanel.add("North",registrationsLabel);
        
        registrationsList=new RegistrationsList(this);
       // registrationsList.setToolTipText("Double click on a registration to get the contacts addresses!!");
        MouseListener mouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
           // listenerProxy.registrationsListMouseClicked(e);
        }
        };
        registrationsList.addMouseListener(mouseListener);
        
        JScrollPane scrollPane = new JScrollPane(registrationsList,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        firstPanel.add("Center",scrollPane);
      
        
        /*************************** Secondary Panel ********************************/
       
        secondPanel=new JPanel();
        secondPanel.setOpaque(false);
        secondPanel.setBorder(BorderFactory.createEmptyBorder(5,20,10,20));
        container.add(secondPanel);
        // row, column, gap, gap
        secondPanel.setLayout( new GridLayout(1,2,5,5) );
        
        proxyButton=new JButton("Start the proxy");
        proxyButton.setToolTipText("Please, start/stop the proxy!!!");
        proxyButton.setFont(new Font ("Dialog", 1, 14));
        proxyButton.setFocusPainted(false);
        proxyButton.setBackground(buttonBackGroundColor);
        proxyButton.setBorder(buttonBorder);
        proxyButton.setVerticalAlignment(AbstractButton.CENTER);
        proxyButton.setHorizontalAlignment(AbstractButton.CENTER);
        secondPanel.add(proxyButton);
        proxyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                  listenerProxy.proxyActionPerformed(evt);
            }
          }
        );
        
        traceViewerButton=new JButton("View the traces");
        traceViewerButton.setToolTipText("The traces are waiting for you!!!");
        traceViewerButton.setFont(new Font ("Dialog", 1, 14));
        traceViewerButton.setFocusPainted(false);
        traceViewerButton.setBackground(buttonBackGroundColor);
        traceViewerButton.setBorder(buttonBorder);
        traceViewerButton.setVerticalAlignment(AbstractButton.CENTER);
        traceViewerButton.setHorizontalAlignment(AbstractButton.CENTER);
        secondPanel.add(traceViewerButton);
        traceViewerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                  listenerProxy.traceViewerActionPerformed(evt);
            } 
          }
        );
        
        JPanel imagesPanel=new JPanel();
        imagesPanel.setOpaque(false);
        // top, left, bottom, right
        imagesPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        container.add(imagesPanel);
        imagesPanel.setLayout( new FlowLayout(FlowLayout.CENTER,0,0) );
        
        ImageIcon icon=new ImageIcon("./gui/images/"+logo);
      
        JLabel label=new JLabel(icon);
        label.setVisible(true);
        label.setToolTipText("What a spacey NIST logo!!!");
        label.setHorizontalAlignment(AbstractButton.CENTER);
        label.setForeground(Color.black);
        label.setFont(new Font ("Dialog", 1, 14));
        label.setOpaque(true);
        label.setBackground(Color.lightGray);
        imagesPanel.add(label);
        
    }
   
    public void clean() {
        // We kill the proxy:
        ProxyDebug.println("Proxy Clean up");
        try {
                listenerProxy.stopProxy();
                if (listenerProxy.rmiregistryProcess!=null) 
                        listenerProxy.rmiregistryProcess.destroy();
        }
        catch(Exception e) { 
            e.printStackTrace();
        }
        System.exit(0);
    }
    
    
    /*************************************************************************/
    /************ The main method: to launch the proxy          *************/
    /************************************************************************/
    
    
    public static void main(String args[]) {
        try{
            // the Proxy:
            String confFile= (String) args[1];
            ProxyLauncher proxyLauncher= new ProxyLauncher(confFile);
            //proxyLauncher.start();
            //ProxyDebug.println("Proxy ready to work");
            System.out.println("in main ProxyLauncher");
        }
        catch(Exception e) {
            System.out.println
            ("ERROR: Set the configuration file flag: " +
            "USE: -cf configuration_file_location.xml"  );
            System.out.println("ERROR, the proxy can not be started, " +
            " exception raised:\n");
            e.printStackTrace();
        }
    }
    
    
}

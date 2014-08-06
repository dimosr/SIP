package gov.nist.sip.proxy.gui;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import gov.nist.sip.proxy.*;
import gov.nist.sip.proxy.Proxy;
import tools.tracesviewer.*;
import gov.nist.sip.proxy.registrar.*;

public class ListenerProxy {
 
    protected ProxyLauncher proxyLauncher;
    protected ConfigurationFrame configurationFrame;
    protected HelpBox helpBox;
    protected boolean PROXY_STARTED;
   
    protected Process rmiregistryProcess;
    protected TracesViewer tracesViewer;
    
    public boolean isProxyStarted() {
        return PROXY_STARTED;
    }

    
    
    public ListenerProxy(ProxyLauncher proxyLauncher) {
        this.proxyLauncher=proxyLauncher;
        PROXY_STARTED=false;
        
        try{
            configurationFrame=new ConfigurationFrame(proxyLauncher,"Configuration");
            helpBox=new HelpBox();
            
            // First, we have to start a registry for logging the traces
            //startRMIregistry();
            
        }
        catch(Exception e) {
           e.printStackTrace();
        }
    }
    
   
    
    public void configurationActionPerformed(ActionEvent em){
        try{
            // Open the help page:
            Point point=proxyLauncher.getLocation();
            configurationFrame.setLocation(point.x,point.y);
            configurationFrame.show();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

   
 
    public void helpMenuMouseEvent(MouseEvent ev) {
        try{
            // Open the help page:
            Point point=proxyLauncher.getLocation();
            helpBox.setLocation(point.x,point.y);
            helpBox.show();
             
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
  public void startRMIregistry() {
         // Launches the rmiregistry:
         try{
             rmiregistryProcess=null;
             Runtime runtime=Runtime.getRuntime();
             
             // The root directory
             File file=new File("../../..");
             String localRootDirectory=file.getAbsolutePath();
             String javaHome= System.getProperty("java.home")+"/bin/";
             String localSeparator= System.getProperty("path.separator");
            
             String rmiregistryClasspath=
                localRootDirectory+"/classes"+localSeparator+
                localRootDirectory+"/lib/antlr/antlrall.jar"+localSeparator+
                localRootDirectory+"/lib/xerces/xerces.jar";
             
             // The command to execute
             String commandLine=javaHome+"rmiregistry -J-Denv.class.path="+rmiregistryClasspath;
            
             ProxyDebug.println("Starting the rmiregistry:");
             ProxyDebug.println(commandLine);
             rmiregistryProcess=runtime.exec(commandLine);
             
         }
         catch (Exception e) {
             ProxyDebug.println("ERROR, starting the rmiregistry, exception raised:"+e.getMessage());
             //e.printStackTrace();
         }    
    }
    
    
    
    public void proxyActionPerformed(ActionEvent ev) {
        try{
            if (! PROXY_STARTED) {
                ProxyDebug.println("\n*****************************************************\n");
                // First thing to do, get the configurations.
                Proxy proxy=new Proxy(proxyLauncher.getConfigurationFile());
                proxyLauncher.setProxy(proxy);
                Registrar registrar=proxy.getRegistrar();
                if (registrar!=null) {
                    ProxyDebug.println("DEBUG, GUI chained to the registrar");
                    registrar.setRegistrationsList(proxyLauncher.registrationsList);
                }
                
                if (proxy.getConfiguration()!=null
                && proxy.getConfiguration().isValidConfiguration()) {
                    proxy.start();
                    
                    PROXY_STARTED=true;
                    proxyLauncher.proxyButton.setBackground(new Color(51,153,255));
                    proxyLauncher.proxyButton.setText("Stop the proxy");

                }
                else {
                    new AlertFrame("ERROR: the configuration parameters are not correct!",
                       JOptionPane.ERROR_MESSAGE);
                }
                ProxyDebug.println("\n*****************************************************\n");
            } 
            else stopProxy();
        }
        catch(Exception e) {
           ProxyDebug.println("ERROR trying to start the proxy, exception raised:"+e.getMessage());
           //e.printStackTrace();
        }
    }
    
  
    public void stopProxy() {
        try{
            ProxyDebug.println("\n*****************************************************\n");
            ProxyDebug.println("Stopping the proxy");
            PROXY_STARTED=false;
            
            proxyLauncher.proxyButton.setBackground(ProxyLauncher.buttonBackGroundColor);
            proxyLauncher.proxyButton.setText("Start the proxy");
        
            Proxy proxy=proxyLauncher.getProxy();
            RegistrationsList registrationsList=proxyLauncher.getRegistrationsList();
            registrationsList.clean();
            
            if (tracesViewer!=null) {
                try{
                    tracesViewer.close();
                    tracesViewer=null;
                 
                   ProxyDebug.println("DEBUG, traces viewer closed.");
                }
                catch(Exception e) {
                    ProxyDebug.println("DEBUG, traces viewer"+
                    " not closed, exception raised:");
                    e.printStackTrace();
                }
            }
            if (proxy!=null) {
                proxy.stop();
                proxy=null;
                ProxyDebug.println("Proxy stopped");
            }
            ProxyDebug.println("\n*****************************************************\n");
        }
        catch(Exception e) {
            ProxyDebug.println("ERROR trying to stop the proxy, exception raised:"+e.getMessage());
            e.printStackTrace();
        }
    }
    
 
   public void traceViewerActionPerformed(ActionEvent evt){
        try{
          
            if (! PROXY_STARTED) {
                   new AlertFrame("ERROR: Start the proxy before viewing the traces!",
                       JOptionPane.ERROR_MESSAGE);
                   return;
            }
            
            if (tracesViewer!=null) {
                tracesViewer.show();
                return;
            }
                
            Proxy proxy=proxyLauncher.getProxy();
            if (proxy==null)  {
                  new AlertFrame("ERROR: Start the proxy before viewing the traces!",
                       JOptionPane.ERROR_MESSAGE);
            return;
            }
            Configuration configuration=proxy.getConfiguration();
            if (configuration==null ) {
                 new AlertFrame("ERROR: Configure the proxy before viewing the traces!",
                       JOptionPane.ERROR_MESSAGE);
                return;
            }
                
         
             if(
            configuration.stackIPAddress==null 
            ) {
                  new AlertFrame("ERROR: Specify the stack IP address!",
                       JOptionPane.ERROR_MESSAGE);
                return;
            }
            /*
                String back="./tools/tracesviewer/images/back.gif";
            String faces="../../tools/tracesviewer/images/faces.jpg";
            String actors="../../tools/tracesviewer/images/comp.gif";
            String logoNist="../../tools/tracesviewer/images/nistBanner.jpg";
           */
            String back="images/back.gif";
            String faces="images/faces.jpg";
            String actors="images/comp.gif";
            String logoNist="images/nistBanner.jpg";
            
            //tracesViewer=new TracesViewer("Proxy Traces Viewer", 
            //configuration.stackIPAddress,"0",configuration.stackName,back,faces,actors,logoNist); 
            if (configuration.serverLogFile!=null) {
                String fileName = configuration.serverLogFile;
		LogFileParser parser = new LogFileParser();
		Hashtable traces = parser.parseLogsFromFile(fileName);
		tracesViewer=new TracesViewer
		      (configuration.serverLogFile,traces,parser.logName,parser.logDescription,
			parser.auxInfo,"Proxy Traces Viewer", 
                        back,faces,actors,logoNist);
                tracesViewer.show();
            } 
            else { 
                new AlertFrame("ERROR: Specify a server log file before viewing the traces!",
                       JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        catch(Exception e) {
            ProxyDebug.println("ERROR, unable to see the traces, exception raised:");
            e.printStackTrace();
        }
    }
   
}

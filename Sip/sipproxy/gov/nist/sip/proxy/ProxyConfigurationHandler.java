/*
 * ProxyHandler.java
 * Used for creating the table containing the association between the method
 * names and the classes processing the corresponding messages.
 * Created on February 3, 2003, 10:52 AM
 */

package gov.nist.sip.proxy;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.*;
import java.util.*;
import gov.nist.sip.proxy.registrar.*;
/**
 *
 * @author  deruelle
 */
public class ProxyConfigurationHandler extends DefaultHandler {
    
    private Configuration configuration;
   

    private String element;

   private void configurationMessage(String message) {

	System.out.println(element + ":" + message);

   }
    
    /** Creates a new instance of ProxyHandler */
    public ProxyConfigurationHandler(String confFile) {
        try{
	    SAXParserFactory saxParserFactory=SAXParserFactory.newInstance();
	    XMLReader saxParser = saxParserFactory.newSAXParser().getXMLReader();
            saxParser.setContentHandler(this);
            saxParser.setFeature
            ("http://xml.org/sax/features/validation",true);
           
            saxParser.parse(confFile);
            
        } catch (SAXParseException spe) {
            spe.printStackTrace();
        } catch (SAXException sxe) {
            sxe.printStackTrace();
        } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
        } catch (Exception pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        }
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    
    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================

    public void startDocument() throws SAXException {
        try {
             ProxyDebug.println("---------------------------------------");
             ProxyDebug.println("Parsing the XML configuration file...");
             
             configuration=new Configuration();
        } 
        catch (Exception e) {
            throw new SAXException("XMLRegistrationsParser error", e);
        }
    }

    public void endDocument() throws SAXException {
        try {
             ProxyDebug.println
		("The XML configuration file has been successfully parsed!");
             ProxyDebug.println("---------------------------------------");
        } 
        catch (Exception e) {
            throw new SAXException("XMLRegistrationsParser error", e);
        }
    }
    
    public void startElement(String namespaceURI,
    String lName, // local name
    String qName, // qualified name
    Attributes attrs)
    throws SAXException {
        String element=qName;
	this.element = element;
        
        if (element.compareToIgnoreCase("SIP_STACK") ==0 ) {
            
            String stackName=attrs.getValue("stack_name");
            if (stackName==null ||  stackName.trim().equals("")) {
                configurationMessage("ERROR: the name of the stack is not set");
		
            }
            else {
                configuration.stackName=stackName.trim();
                configurationMessage("INFORMATION: the name of the stack is set to:"+
                configuration.stackName);
            }
            
            String stackIPAddress=attrs.getValue("stack_IP_address");
            if (stackIPAddress==null || stackIPAddress.trim().equals("")) {
		configurationMessage
		("ERROR: the IP address of the stack is not set");
		
            }
            else {
                configuration.stackIPAddress=stackIPAddress.trim();
	        configurationMessage
	        ("INFORMATION: the stack IP address is set to:" + stackIPAddress);
            }
            
            String outboundProxy=attrs.getValue("outbound_proxy");
            if (outboundProxy==null || outboundProxy.trim().equals("")) {
                configurationMessage
		("INFORMATION: the outbound proxy is not set");
            }
            else {
                configuration.outboundProxy=outboundProxy.trim();
	        configurationMessage
		("INFORMATION: the outbound proxy is " + outboundProxy);
            }
            
            String routerPath=attrs.getValue("router_path");
            if (routerPath==null || routerPath.trim().equals("")) {
                configurationMessage
		("WARNING: the path for the router is not set using default");
            }
            else {
                configuration.routerPath=routerPath.trim();
                configurationMessage
		("INFORMATION: the path for the router is set to: " + routerPath.trim());
            }
            
            String extensionMethods=attrs.getValue("extension_methods");
            if (extensionMethods==null || extensionMethods.trim().equals("")  ) {
                configurationMessage
                ("INFORMATION: no extensions are supported");
            }
            else {
                configuration.extensionMethods=extensionMethods.trim();
                configurationMessage
                ("INFORMATION: extensions supported: " + extensionMethods);
            }
            
            String retransmissionFilter=attrs.getValue("retransmission_filter");
            if (retransmissionFilter!=null && !retransmissionFilter.trim().equals("")) {
                configurationMessage
                ("INFORMATION: the retransmission filter is set to:" + retransmissionFilter);
                configuration.retransmissionFilter=retransmissionFilter.trim();
            } else 
                configurationMessage
                ("INFORMATION: no default retransmission filter");

            String stopTime=attrs.getValue("stop_after");
            if (stopTime!=null && !stopTime.trim().equals("")) {
		configurationMessage
		("INFORMATION: The proxy will stop automatically after "
			+stopTime+" ms");
                configuration.stopTime=stopTime.trim();
            }
            
            String maxConnections=attrs.getValue("max_connections");
            if (maxConnections!=null && !maxConnections.trim().equals("")) {
		configurationMessage
		("INFORMATION: The proxy will authorize only "
			+maxConnections+" connections");
                configuration.maxConnections=maxConnections.trim();
            }
             
            String maxServerTransactions=attrs.getValue("max_server_transactions");
            if (maxServerTransactions!=null && !maxServerTransactions.trim().equals("")) {
		configurationMessage
		("INFORMATION: The proxy will authorize only "
			+maxServerTransactions+" server transactions");
                configuration.maxServerTransactions=maxServerTransactions.trim();
            }
              
            String threadPoolSize=attrs.getValue("thread_pool_size");
            if (threadPoolSize!=null && !threadPoolSize.trim().equals("")) {
		configurationMessage
		("INFORMATION: The thread pool size is set to "
			+threadPoolSize);
                configuration.threadPoolSize=threadPoolSize.trim();
            }
            
           
        }
        
        if (element.compareToIgnoreCase("LISTENING_POINT") ==0 ) {
            String stackPort=attrs.getValue("port");
            if (stackPort==null || stackPort.trim().equals("")) {
		configurationMessage
                ("ERROR: the port of the stack is not set");
               
            }
            else {
                String stackTransport=attrs.getValue("transport");
                if (stackTransport==null || stackTransport.trim().equals("")) {
		    configurationMessage
                    ("ERROR: the transport is not set");
                    
                }
                else {
		    configurationMessage
		    ("LISTENING_POINT port = " + stackPort);
		    configurationMessage
		    ("LISTENING_POINT transport = " + stackTransport);
                    configuration.addListeningPoint(stackPort,stackTransport);
                }
            }
        }
        
         if (element.compareToIgnoreCase("DOMAIN") ==0 ) {
              String stackDomain=attrs.getValue("domain");
              if (stackDomain!=null && !stackDomain.trim().equals("")) {
		configurationMessage
		("INFORMATION: One domain to take care of is: "
			+stackDomain);
                configuration.domainList.addElement(stackDomain.trim());
              }
        }
        
        if (element.compareToIgnoreCase("REGISTER_TO_PROXY") ==0 ) {
              String hostName=attrs.getValue("host_name");
              Domain domain=new Domain();
              if (hostName!=null && !hostName.trim().equals("")) {
                 
                  domain.setHostName(hostName);
                  String hostPort=attrs.getValue("host_port");
                  if (hostPort!=null && !hostPort.trim().equals("")) {
                      domain.setHostPort(hostPort);
                      configurationMessage
                      ("INFORMATION: One proxy to register with: "
                      +hostName+":"+hostPort);
                  }
                  else
                      configurationMessage
                      ("INFORMATION: One proxy to register with: "
                      +hostName);
                  
                  String from=attrs.getValue("from");
                  if (from!=null && !from.trim().equals("")) {
                      domain.setFrom(from);
                  }
                  
                  configuration.proxyToRegisterWithList.addElement(domain);
              } 
        }
        
        if (element.compareToIgnoreCase("REGISTRAR") ==0 ) {
            String rfc2543Compatible =attrs.getValue("rfc2543_compatible");
	    if (rfc2543Compatible != null && "true".equalsIgnoreCase(
						rfc2543Compatible.trim()) ) {
		configuration.rfc2543Compatible = true;
	    }
            String exportRegistry=attrs.getValue("export_registrations");
            if (exportRegistry!=null && !exportRegistry.trim().equals("") ) {
                try{
                    Boolean b=Boolean.valueOf(exportRegistry.trim());
                    if ( b.booleanValue() ) {
			
				configurationMessage
				("INFORMATION: the registrations will be exported");
                          configuration.exportRegistry=true;
                          String registryPort=attrs.getValue("port");
                          if (registryPort==null) {
                            
                                configurationMessage
                            ("WARNING: The registry port is not set; "+
                            "1099 is used by default");
                            
                            configuration.registryPort="1099";
                          }
                          else {
                              
                              configurationMessage("INFORMATION: The registry port is: "+
                            registryPort);
                            configuration.registryPort=registryPort;
                          }
                    }
                    else {
                      
			configurationMessage
			("INFORMATION: the registrations will not be exported");
                          configuration.exportRegistry=false;
                    }
                }
                catch(Exception e) {
                
			configurationMessage
			("INFORMATION: the registrations will not be exported");
                    configuration.exportRegistry=false;
                }
             }
        }


        if (element.compareToIgnoreCase("PRESENCE_SERVER") ==0 ) {
            String enablePresenceServerString=attrs.getValue("enable");
            if (enablePresenceServerString==null) {
                configuration.enablePresenceServer=false;
		configurationMessage
		("WARNING: The presence server feature is disabled");
            }
            else {
                try{
                    Boolean b=Boolean.valueOf
				(enablePresenceServerString.trim());
                    if ( b.booleanValue() ) {
                        configuration.enablePresenceServer=true;
			configurationMessage
		       ("INFORMATION: The presence server feature is enabled");
                    }
                    else {
                        configuration.enablePresenceServer=false;
			configurationMessage
			("WARNING: The presence server feature is disabled");
                    }
                }
                catch(Exception e) {
                    configuration.enablePresenceServer=false;
		    configurationMessage
                    ("WARNING: The presence server feature is disabled");
                }
            }
        }
        
        if (element.compareToIgnoreCase("AUTHENTICATION") ==0 ) {
            String enableAuthenticationString=attrs.getValue("enable");
            if (enableAuthenticationString==null) {
                configuration.enableAuthentication=false;
		configurationMessage
		("WARNING: Authentication support is disabled");
            }
            else {
                try{
                    Boolean b=Boolean.valueOf
                    (enableAuthenticationString.trim());
                    if ( b.booleanValue() ) {
                        configuration.enableAuthentication=true;
                        configurationMessage
                        ("INFORMATION: Authentication feature is enabled");
                        
                    }
                    else {
                        configuration.enableAuthentication=false;
                        configurationMessage
                        ("WARNING: The authentication feature is disabled");
                    }
                    String method=attrs.getValue("method");
                    if (method==null) {
                        configurationMessage
                        ("WARNING: The Authentication method is not set");
                    }
                    else {
                        configuration.method=method.trim();
                    }
                    String classFile=attrs.getValue("class_file");
                    if (classFile==null) {
                        configurationMessage
                        ("WARNING: The class_file parameter is not set"+
                        " for the authentication");
                    }
                    else {
                        configuration.classFile=classFile.trim();
                    }
                    String passwordsFile=attrs.getValue("passwords_file");
                    if (passwordsFile==null) {
                        configurationMessage
                        ("WARNING: The passwords file is not set"+
                        " for the authentication");
                    }
                    else {
                        configuration.passwordsFile=passwordsFile.trim();
                    }
                    
                }
                catch(Exception e) {
                    configuration.enableAuthentication=false;
                    configurationMessage
			("ERROR: Error initializing authentication [" +
			e.getMessage() + "]");
		     System.exit(0);
                }
            }
        }
        
        if (element.compareToIgnoreCase("LOGGING") ==0 ) {
	     String remoteLogAccess = 
			attrs.getValue("access_log_via_rmi");
	    
	     if (remoteLogAccess != null && 
		remoteLogAccess.equalsIgnoreCase("true")) {
                     configurationMessage
                ("INFORMATION: RMI access to log file enabled"); 
		configuration.accessLogViaRMI = true;
		String rmiPort = attrs.getValue("rmi_port");
		if (rmiPort != null) {
		   configuration.logRMIPort = rmiPort;
	            configurationMessage
	            ("INFORMATION: the RMI port for log file is: " 
				+ configuration.logRMIPort);
		} else {
		   configuration.logRMIPort = "0";
	        }
                configurationMessage
	            ("INFORMATION: the default RMI port for log file is: " 
				+ configuration.logRMIPort);
                
		configuration.logLifetime = attrs.getValue("log_lifetime");
		if (configuration.logLifetime != null) 
	            configurationMessage
	            ("INFORMATION: the log lifetime is: " + 
			configuration.logLifetime); 
	     }
             else { 
                    configurationMessage
                    ("INFORMATION: RMI access to log file disabled"); 
             }

	     String enableDebug=attrs.getValue("enable_debug");
             if (enableDebug==null || enableDebug.trim().equals("") ) {
                 configuration.enableDebug=false;
		  configurationMessage
		  ("INFORMATION: the Debug feature is not set");
             } else {
                  try{
                      boolean deb=Boolean.valueOf
				(enableDebug.trim()).booleanValue();
                      if ( deb) {
                            configurationMessage
		  ("INFORMATION: the Debug feature is set");
                               configuration.enableDebug=deb;
                      }
                      else  {
                          configuration.enableDebug=false;
                          configurationMessage
		  ("INFORMATION: the Debug feature is not set");
                      }
                  }   
                  catch(Exception e) {
                      configuration.enableDebug=false;
                      configurationMessage
		  ("INFORMATION: the Debug feature is not set");
                  }
             }
             String serverLogFile=attrs.getValue("server_log");
             if (serverLogFile==null || serverLogFile.trim().equals("")) {
		  configurationMessage
		  ("INFORMATION: the server message log file is not set");
             } else {
                 configuration.serverLogFile=serverLogFile.trim();
		 configurationMessage
		  ("INFORMATION: the server message log file is: " 
		   + configuration.serverLogFile);
             }
             String badMessageLog=attrs.getValue("bad_message_log");
             if (badMessageLog==null || badMessageLog.trim().equals("")) {
	        configurationMessage
		("INFORMATION: the bad message log file is not set");
             } else {
                configuration.badMessageLogFile= badMessageLog.trim();
	        configurationMessage
		("INFORMATION: the bad message log is: " + 
		configuration.badMessageLogFile);
             }
             String debugLog=attrs.getValue("debug_log");
            if (debugLog==null || debugLog.trim().equals("")) {
		configurationMessage
		("INFORMATION: the debug log file is not set");
             } else {
                 configuration.debugLogFile=debugLog.trim();
                 configurationMessage
		("INFORMATION: the debug log file is: " + 
		configuration.debugLogFile);
              }
              String outputProxy=attrs.getValue("output_proxy");
              if (outputProxy==null || outputProxy.trim().equals("")) {
		  configurationMessage
		  ("INFORMATION: the output proxy file is not set");
              } else {
                  configuration.outputProxy=outputProxy.trim();
                   configurationMessage
		("INFORMATION: the output proxy file is: " + 
		configuration.outputProxy);
               }
        }
        if (element.compareToIgnoreCase("REGISTRATIONS") ==0 ) {
            
            String expiresTimeString=attrs.getValue("expires_time");
            if (expiresTimeString!=null && !expiresTimeString.trim().equals("") ) {
                try{
                    configuration.expiresTime=
		     Integer.valueOf(expiresTimeString.trim()).intValue();
                    configurationMessage
                    ("INFORMATION: expires time is set to:"
			+configuration.expiresTime);
                }
                catch(Exception e) {
                     configurationMessage
		("WARNING: expires time is set to default value:"
				+configuration.expiresTime);
                }
            }
            else {
                configurationMessage
		("WARNING: expires time is set to default value:"
				+configuration.expiresTime);
            }
                
            String enableRegistrationsString=attrs.getValue("enable");
            if (enableRegistrationsString==null) {
                configuration.enableRegistrations=false;
		configurationMessage
		("INFORMATION: registration uploading feature is disabled");
            }
            else {
                try{
                    Boolean b=Boolean.valueOf(enableRegistrationsString.trim());
                    if ( b.booleanValue()) {
                        configuration.enableRegistrations=true;
                        configurationMessage
                        ("INFORMATION: Uploading static registrations");
                    }
                    else {
                        configuration.enableRegistrations=false;
                        configurationMessage
                        ("WARNING: No static registrations to upload" );
                    }
                    String registrationsFile=attrs.getValue
                    ("registrations_file");
                    if (registrationsFile==null) {
                        configurationMessage
                        ("WARNING: No registration file specified");
                    }
                    else {
                        configurationMessage
                        ("INFORMATION: registrations file is " + registrationsFile);
                        configuration.registrationsFile=
                        registrationsFile.trim();
                    }

                }
                catch(Exception e) {
                   configuration.enableRegistrations=false;
		    configurationMessage
		     ("ERROR: Error uploading static registrations [" + 
				e.getMessage()  + "]" );
                }
            }
        }
        
     
    }
    
    public void endElement(String namespaceURI,
    String sName, // simple name
    String qName  // qualified name
    )
    throws SAXException
    {
        String element=qName;
        if (element.compareToIgnoreCase("SIP_STACK") ==0 ) {
            
        }
        if (element.compareToIgnoreCase("LISTENING_POINT") ==0 ) {
           
        }
        if (element.compareToIgnoreCase("RMI") ==0 ) {
         
        }
        if (element.compareToIgnoreCase("DEBUG") ==0 ) {
            
        }
        if (element.compareToIgnoreCase("PRESENCE_SERVER") ==0 ) {
            
        }
        if (element.compareToIgnoreCase("AUTHENTICATION") ==0 ) {
            
        }
        if (element.compareToIgnoreCase("REGISTRATIONS") ==0 ) {
            
        }
        if (element.compareToIgnoreCase("EXTENSION_HANDLERS") ==0 ) {
            
        }
       
    }
    
    public void characters(char buf[], int offset, int len)
    throws SAXException
    {
        String str = new String(buf, offset, len);
    }
    
    public static String createTags(Configuration configuration) {
	String res="<?xml version='1.0' encoding='us-ascii'?>\n"+ 
		   "<CONFIGURATION> \n"+
    		   "	<SIP_STACK \n";
	if (configuration.stackName!=null)
   		res+="stack_name=\""+configuration.stackName+"\"\n";
	if (configuration.stackIPAddress!=null)
   		res+="stack_IP_address=\""+configuration.stackIPAddress+"\"\n";
	if (configuration.outboundProxy!=null)
   		res+="outbound_proxy=\""+configuration.outboundProxy+"\"\n";
	if (configuration.routerPath!=null)
   		res+="router_path=\""+configuration.routerPath+"\"\n";
	if (configuration.extensionMethods!=null)
   		res+="extension_methods=\""+configuration.extensionMethods+"\"\n";
        if (configuration.retransmissionFilter!=null)
   		res+="retransmission_filter=\""+configuration.retransmissionFilter+"\"\n";
        if (configuration.stopTime!=null)
   		res+="stop_after=\""+configuration.stopTime+"\"\n";
        if (configuration.maxConnections!=null)
   		res+="max_connections=\""+configuration.maxConnections+"\"\n";
        if (configuration.maxServerTransactions!=null)
   		res+="max_server_transactions=\""+configuration.maxServerTransactions+"\"\n";
        if (configuration.threadPoolSize!=null)
   		res+="thread_pool_size=\""+configuration.threadPoolSize+"\"\n";
        
       
	res+="    >       \n\n";
	Enumeration e=configuration.listeningPoints.elements();
	while (e!=null && e.hasMoreElements() ) {
		Association a=(Association)e.nextElement();
		String port=a.port;
		String transport=a.transport;
		if (port!=null && transport!=null) {
                        
        		res+="<LISTENING_POINT port=\""+port+"\" transport=\""+transport+"\" />\n"; 
                }
        } 
        
        if (configuration.domainList!=null)
        for (int j=0;j<configuration.domainList.size();j++) {
            String domain=(String)configuration.domainList.elementAt(j);
            res+="<DOMAIN domain=\""+domain+"\" />\n"; 
        }
        
        if (configuration.proxyToRegisterWithList!=null)
        for (int j=0;j<configuration.proxyToRegisterWithList.size();j++) {
            Domain domain=(Domain)configuration.proxyToRegisterWithList.elementAt(j);
            if (domain.hostPort!=null)
                res+="<REGISTER_TO_PROXY from="+domain.from+" hostName=\""+domain.hostName+"\" hostPort=\""+
                domain.hostPort+"\" />\n"; 
            else res+="<REGISTER_TO_PROXY from="+domain.from+" hostName=\""+domain.hostName+"\" />\n"; 
        }
        
    	res+="</SIP_STACK>\n\n";
    
    	res+="<REGISTRAR\n";
        res+="export_registrations=\""+configuration.exportRegistry+"\"\n";
   	if (configuration.registryPort!=null)
        	res+="port=\""+configuration.registryPort+"\"\n";
    	res+="/>\n\n";

    	res+="<LOGGING\n";
	res+="access_log_via_rmi=\""+configuration.accessLogViaRMI+"\"\n";
	
        if (configuration.logRMIPort!=null)
		res+="rmi_port=\""+configuration.logRMIPort+"\"\n";
        if (configuration.logLifetime!=null)
		res+="log_lifetime=\""+configuration.logLifetime+"\"\n";
        res+="enable_debug=\""+configuration.enableDebug+"\"\n";
       	if (configuration.outputProxy!=null)
		res+="output_proxy=\""+configuration.outputProxy+"\"\n";
	if (configuration.serverLogFile!=null)
		res+="server_log=\""+configuration.serverLogFile+"\"\n";
     	if (configuration.badMessageLogFile!=null)
		res+="bad_message_log=\""+configuration.badMessageLogFile+"\"\n";
        if (configuration.debugLogFile!=null)
		res+="debug_log=\""+configuration.debugLogFile+"\"\n";
    	res+="/>\n\n";
    
    	res+="<PRESENCE_SERVER \n";
        res+="enable=\""+configuration.enablePresenceServer+"\"\n";
    	res+="/>\n\n";
    
    
    	res+="<AUTHENTICATION\n";
        res+="enable=\""+configuration.enableAuthentication+"\"\n";
	if (configuration.method!=null)
        	res+="method=\""+configuration.method+"\"\n";
	if (configuration.classFile!=null)
        	res+="class_file=\""+configuration.classFile+"\"\n";
	if (configuration.passwordsFile!=null)
        	res+="passwords_file=\""+configuration.passwordsFile+"\"\n";
    	res+="/>\n\n";
    
    	res+="<REGISTRATIONS \n";
        res+="enable=\""+configuration.enableRegistrations+"\"\n";
        res+="expires_time=\""+configuration.expiresTime+"\"\n";
	if (configuration.registrationsFile!=null)
        	res+="registrations_file=\""+configuration.registrationsFile+"\"\n";
        res+="/>\n\n";

	
    	
    	res+="</CONFIGURATION> \n\n"; 
        
        return res;
    }
    
      /** Utility method for writing in a file
       * @param outFile: file to write
       * @param text String to set
       */
    public static void writeFile(String outFile, String text) {
        try {
            
            File file=new File(outFile);
            
            if (file.exists()) {
                FileWriter fileWriter = new FileWriter(outFile,false);
                PrintWriter pw = new PrintWriter(fileWriter,true);
                
                pw.write(text);
                
                pw.close();
                fileWriter.close();
            }
            else ProxyDebug.println("ProxyDebug, writeFile(),  unable to write, the file:"+
                    outFile +" is missing.");
        }
        catch (Exception e) {
            ProxyDebug.println("ProxyDebug, writeFile(),  unable to write, the file:"+
                    outFile +" is missing.");
            e.printStackTrace();
        }
    }
    
    
}

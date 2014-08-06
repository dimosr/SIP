/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
* Creator: O. Deruelle (deruelle@nist.gov)                                     *
* Questions/Comments: nist-sip-dev@antd.nist.gov                               *
*******************************************************************************/
package gov.nist.sip.proxy.authentication;

import java.io.*;
import java.util.*;

import gov.nist.sip.proxy.*;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;


/** parser for a XML file
 *@author deruelle@antd.nist.gov
 */
public class XMLAuthenticationParser extends DefaultHandler {
    
    private Vector usersTagList;
    private UserTag userTag;

    private XMLReader saxParser;
    private String file;
    private String realm;
   
    
    
    /** start the parsing
     *
     * @param file to parse
     */
    public XMLAuthenticationParser(String fileLocation) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            SAXParser saxParser = factory.newSAXParser();
            file = fileLocation;
            saxParser.parse(fileLocation, this);
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

    public String getRealm() {
        return realm;
    }
    
    public Vector getUsersTagList() {
        return usersTagList;
    }
    
    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================

    public void startDocument() throws SAXException {
        try {
             ProxyDebug.println("ProxyDebug, XMLAuthenticationParser, startDocument(): "+
             " Parsing XML passwords file");
        } 
        catch (Exception e) {
            throw new SAXException("XMLAuthenticationParser error", e);
        }
    }

    public void endDocument() throws SAXException {
        try {
           ProxyDebug.println("ProxyDebug, XMLAuthenticationParser, endDocument(): "+
           " XML passwords file parsed successfully!!!");
        } 
        catch (Exception e) {
            throw new SAXException("ProxyDebug XMLAuthenticationParser error", e);
        }
    }

    public void startElement(String namespaceURI,
                             String lName, // local name
                             String qName, // qualified name
                             Attributes attrs)
                             throws SAXException
    {
        String element=qName;
        if (element.compareToIgnoreCase("authentication") ==0 ) {
            usersTagList=new Vector();
            String userRealm= attrs.getValue("realm");
            if (userRealm!=null) {
                userRealm=userRealm.trim();
                realm=userRealm;
            }
        }
        if (element.compareToIgnoreCase("user") ==0 ) {
            userTag=new UserTag();
            String userName= attrs.getValue("name");
            if (userName!=null) {
                userName=userName.trim();
                userTag.setUserName(userName);
            }
            
            String userPassword= attrs.getValue("password");
            if (userPassword!=null) {
                userPassword=userPassword.trim();
                userTag.setUserPassword(userPassword);
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
        if (element.compareToIgnoreCase("authentication") ==0 ) {
        }
        if (element.compareToIgnoreCase("user") ==0 ) {
            usersTagList.addElement(userTag);
        }
    }

    public void characters(char buf[], int offset, int len)
    throws SAXException
    {
        String str = new String(buf, offset, len);
    }

    
    public void writeToXMLFile(Vector usersTagList) {
        try{
            String res="<?xml version='1.0' encoding='us-ascii'?> \n"+
            "<AUTHENTICATION> \n";
            for (int i=0;i<usersTagList.size();i++) {
                UserTag userTag=(UserTag)usersTagList.elementAt(i);
                res+=userTag.toString()+"\n";
            }
            res+="</AUTHENTICATION>\n";
            
            writeFile(null,file,res);
            ProxyDebug.println("ProxyDebug, XMLAuthenticationParser, writeToXMLFile(), New"+
            " authentications wrote to the file!!");
        }
        catch(Exception e) {
            ProxyDebug.println("ProxyDebug, XMLAuthenticationParser, writeToXMLFile(), exception"+
            " raised:");
            e.printStackTrace();
        }
    }
    
    /** Utility method for reading a file and append in a other file the text
     * @param inFile: file to read
     * @param outFile: file to write
     * @param text String to set
     */
    public void writeFile(String inFile,String outFile, String text) {
        // we readthis file to obtain the options 
        try{
            FileWriter fileWriter = new FileWriter(outFile,false);
            PrintWriter pw = new PrintWriter(fileWriter,true);
            
            if (inFile!=null) {
                FileReader in=new FileReader(inFile);
                
                int c;
                int cpt=0;
                StringBuffer s=new StringBuffer();
                while ( ( c = in.read() ) != -1)
                    cpt++;
                in.close();
                char[] cbuf=new char[cpt];
                in=new FileReader(inFile);
                in.read(cbuf,0,cpt);
                in.close();
                s.append(cbuf);
                String content=s.toString();
                pw.println(content);
            }
            
            pw.println(text);
            pw.close();
            fileWriter.close();
        }
        catch(Exception e) {
            ProxyDebug.println("ProxyDebug, XMLAuthenticationParser, writeFile(), Unable"+
            " to write in the XML authentication file!!");
            e.printStackTrace();
        }
    }
   
}

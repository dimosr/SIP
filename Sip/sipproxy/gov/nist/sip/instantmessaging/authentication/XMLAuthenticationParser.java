/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
* Creator: O. Deruelle (deruelle@nist.gov)                                     *
* Questions/Comments: nist-sip-dev@antd.nist.gov                               *
*******************************************************************************/
package gov.nist.sip.instantmessaging.authentication;

import java.io.*;
import java.util.*;

import gov.nist.sip.instantmessaging.*;

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

	    SAXParserFactory saxParserFactory=SAXParserFactory.newInstance();
	    saxParser = saxParserFactory.newSAXParser().getXMLReader();
            saxParser.setContentHandler(this);
            saxParser.setFeature
            ("http://xml.org/sax/features/validation",true);
            // parse the xml specification for the event tags.
	    saxParser.parse(fileLocation);
            file=fileLocation;
           
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
             DebugIM.println("DebugIM, XMLAuthenticationParser, startDocument(): "+
             " Parsing XML passwords file");
        } 
        catch (Exception e) {
            throw new SAXException("XMLAuthenticationParser error", e);
        }
    }

    public void endDocument() throws SAXException {
        try {
           DebugIM.println("DebugIM, XMLAuthenticationParser, endDocument(): "+
           " XML passwords file parsed successfully!!!");
        } 
        catch (Exception e) {
            throw new SAXException("DebugIM XMLAuthenticationParser error", e);
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
            
        }
        if (element.compareToIgnoreCase("user") ==0 ) {
            userTag=new UserTag();
            String userName= attrs.getValue("name");
            if (userName!=null) {
                userName=userName.trim();
                userTag.setUserName(userName);
            }
            String userRealm= attrs.getValue("realm");
            if (userRealm!=null) {
                userRealm=userRealm.trim();
                userTag.setUserRealm(userRealm);
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
            
            IMUtilities.writeFile(file,res);
        }
        catch(Exception e) {
            DebugIM.println("DebugIM, XMLAuthenticationParser, writeToXMLFile(), exception"+
            " raised:");
            e.printStackTrace();
        }
    }
    
    
}

package gov.nist.sip.instantmessaging;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;


/** parser for a XML file
 */
public class XMLBuddyParser extends DefaultHandler {
    
    private BuddyTag buddyTag;
    private Vector buddies;
    
    private XMLReader saxParser;
    
   
    /** start the parsing
     * @param file to parse
     * @return Vector containing the test cases
     */
    public XMLBuddyParser(String fileLocation) {
           try {
	    SAXParserFactory saxParserFactory=SAXParserFactory.newInstance();
	    saxParser = saxParserFactory.newSAXParser().getXMLReader();
            saxParser.setContentHandler(this);
            saxParser.setFeature
            ("http://xml.org/sax/features/validation",true);
            // parse the xml specification for the event tags.
	    saxParser.parse(fileLocation);
           
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
    
    public Vector getBuddies() {
        return buddies;
    }
    
    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================

    public void startDocument() throws SAXException {
        try {
             DebugIM.println("DebugIM, Parsing XML buddies");
        } 
        catch (Exception e) {
            buddies=null;
            throw new SAXException("XMLBuddyParser error", e);
        }
    }

    public void endDocument() throws SAXException {
        try {
           DebugIM.println("DebugIM, XML buddies parsed successfully!!!");
        } 
        catch (Exception e) {
            throw new SAXException("XMLBuddyParser error", e);
        }
    }

    public void startElement(String namespaceURI,
                             String lName, // local name
                             String qName, // qualified name
                             Attributes attrs)
                             throws SAXException
    {
        String element=qName;
        if (element.compareToIgnoreCase("buddies") ==0 ) {
            buddies=new Vector();
        }
        if (element.compareToIgnoreCase("buddy") ==0 ) {
            buddyTag=new BuddyTag();
            String uri=attrs.getValue("uri");
            if (uri!=null && !uri.trim().equals("") && checkURI(uri) )
                buddyTag.setURI(uri);
            else {
                DebugIM.println("DebugIM, the buddy format has to be a sip uri.");
                throw new SAXException("ERROR parsing the buddy");
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
        if (element.compareToIgnoreCase("buddies") ==0 ) {
        }
        if (element.compareToIgnoreCase("buddy") ==0 ) {
           buddies.addElement(buddyTag);
        }
        
    }

    public void characters(char buf[], int offset, int len)
    throws SAXException
    {
        String str = new String(buf, offset, len);
    }
    
    public static boolean checkURI(String uri) {
        try{
           return (uri.startsWith("sip:"));
           
        }
        catch(Exception e) {
            DebugIM.println("ERROR, DebugIM, the buddy uri has to be a sip uri.");
            return false;
        }
    }
    
}

package gov.nist.sip.proxy.presenceserver.pidfparser;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import gov.nist.sip.proxy.ProxyDebug;

//import gov.nist.sip.instantmessaging.*;


/** parser for a XML file
 *
 *@author olivier deruelle
 */
public class XMLcpimParser extends DefaultHandler {
    
    private PresenceTag presenceTag;
    private PresentityTag presentityTag;
    private StatusTag statusTag;
    private TupleTag tupleTag;
    private ContactTag contactTag;
    private BasicTag basicTag;
    private NoteTag noteTag;
    
    private String element;
    private XMLReader saxParser;
   
    /** start the parsing
     * @param file to parse
     * @return Vector containing the test cases
     */
    public XMLcpimParser(String fileLocation) {
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

    /** start the parsing
     * @param file to parse
     * @return Vector containing the test cases
     */
    public XMLcpimParser() {
        try {
	    SAXParserFactory saxParserFactory=SAXParserFactory.newInstance();
	    saxParser = saxParserFactory.newSAXParser().getXMLReader();
            saxParser.setContentHandler(this);
            saxParser.setFeature
		("http://xml.org/sax/features/validation",true);
            // parse the xml specification for the event tags.
	   
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    }
    
    public void parseCPIMString(String body) {
        try {
            StringReader stringReader=new StringReader(body);
            InputSource inputSource=new InputSource(stringReader);
            saxParser.parse(inputSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public PresenceTag getPresenceTag() {
        return presenceTag;
    }
    
    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================

    public void startDocument() throws SAXException {
        try {
	    ProxyDebug.println("Parsing XML cpim string");
        } 
        catch (Exception e) {
            throw new SAXException("XMLcpimParser error", e);
        }
    }

    public void endDocument() throws SAXException {
        try {
	    ProxyDebug.println("XML cpim string parsed successfully!!!");
        } 
        catch (Exception e) {
            throw new SAXException("XMLcpimParser error", e);
        }
    }

    public void startElement(String namespaceURI,
                             String lName, // local name
                             String qName, // qualified name
                             Attributes attrs)
	throws SAXException
    {
        element=qName;
         if (element.compareToIgnoreCase("presence") ==0 ) {
            presenceTag=new PresenceTag();
            String entity=attrs.getValue("entity").trim();
            presenceTag.setEntity(entity);
     }
        if (element.compareToIgnoreCase("presentity") ==0 ) {
          presentityTag=new PresentityTag();
            String id=attrs.getValue("id").trim();
            presentityTag.setId(id);
      }
        if (element.compareToIgnoreCase("tuple") ==0 ) {
            tupleTag=new TupleTag();
            String id=attrs.getValue("id").trim();
            tupleTag.setId(id);
        }
        if (element.compareToIgnoreCase("status") ==0 ) {
            statusTag=new StatusTag();
       }
        if (element.compareToIgnoreCase("basic") ==0 ) {
          basicTag=new BasicTag();
        }
        if (element.compareToIgnoreCase("contact") ==0 ) {
            contactTag=new ContactTag();
            String priority=attrs.getValue("priority").trim();
            if (priority!=null) {
                try {
                    contactTag.setPriority(Float.parseFloat(priority));
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
     }
        if (element.compareToIgnoreCase("note") ==0 ) {
            noteTag=new NoteTag();
        }
    }
    
    public void endElement(String namespaceURI,
			   String sName, // simple name
			   String qName  // qualified name
			   )
	throws SAXException
    {
        String element=qName;
        if (element.compareToIgnoreCase("presence") ==0 ) {
        }
        if (element.compareToIgnoreCase("presentity") ==0 ) {
	    presenceTag.setPresentityTag(presentityTag);
        }
        if (element.compareToIgnoreCase("tuple") ==0 ) {
	    presenceTag.addTupleTag(tupleTag);
        }
        if (element.compareToIgnoreCase("status") ==0 ) {
	    tupleTag.setStatusTag(statusTag);
        }
        if (element.compareToIgnoreCase("basic") ==0 ) {
            statusTag.setBasicTag(basicTag);
        }
        if (element.compareToIgnoreCase("contact") ==0 ) {
            tupleTag.setContactTag(contactTag);
        }
        if (element.compareToIgnoreCase("note") ==0 ) {
            tupleTag.setNoteTag(noteTag);
        }
    }
    
    public void characters(char buf[], int offset, int len)
	throws SAXException
    {
        String str = new String(buf, offset, len);
        if (str!=null && !str.trim().equals("") ) {
            if (element.compareToIgnoreCase("basic") ==0 ) {
                basicTag.setValue(str);
                
            }
            if (element.compareToIgnoreCase("contact") ==0 ) {
                contactTag.setContact(str);
                
            }
            if (element.compareToIgnoreCase("note") ==0 ) {
                noteTag.setNote(str);
                
            }
        }
    }

}

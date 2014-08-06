/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
* Creator: O. Deruelle (deruelle@nist.gov)                                     *
* Questions/Comments: nist-sip-dev@antd.nist.gov                               *
*******************************************************************************/
package gov.nist.sip.instantmessaging.presence.cpimparser;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import gov.nist.sip.instantmessaging.*;


/** parser for a XML file
 */
public class XMLcpimParser extends DefaultHandler {
    
    private PresenceTag presenceTag;
    private PresentityTag presentityTag;
    private StatusTag statusTag;
    private TupleTag tupleTag;
    private ContactTag contactTag;
    private ValueTag valueTag;
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
	    this.saxParser = saxParserFactory.newSAXParser().getXMLReader();
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
	    this.saxParser = saxParserFactory.newSAXParser().getXMLReader();
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
            this.saxParser.parse(inputSource);
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
             DebugIM.println("Parsing XML cpim string");
        } 
        catch (Exception e) {
            throw new SAXException("XMLcpimParser error", e);
        }
    }

    public void endDocument() throws SAXException {
        try {
           DebugIM.println("XML cpim string parsed successfully!!!");
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
        System.out.println("StartElement:"+element);
        if (element.compareToIgnoreCase("presence") ==0 ) {
            //System.out.println("presence!!!!");
            presenceTag=new PresenceTag();
            String entity=attrs.getValue("entity").trim();
            presenceTag.setEntity(entity);
             //System.out.println("presence!!!!");
        }
        if (element.compareToIgnoreCase("presentity") ==0 ) {
             //System.out.println("presentity!!!!");
            presentityTag=new PresentityTag();
            String id=attrs.getValue("id").trim();
            presentityTag.setId(id);
           //System.out.println("presentity!!!!");
        }
        if (element.compareToIgnoreCase("tuple") ==0 ) {
              //System.out.println("tuple!!!!");
            tupleTag=new TupleTag();
            String id=attrs.getValue("id").trim();
            tupleTag.setId(id);
             //System.out.println("tuple!!!!");
        }
        if (element.compareToIgnoreCase("status") ==0 ) {
             //System.out.println("status!!!!");
            statusTag=new StatusTag();
             //System.out.println("status!!!!");
        }
        if (element.compareToIgnoreCase("basic") ==0 ) {
             //System.out.println("basic!!!!");
            valueTag=new ValueTag();
             //System.out.println("basic!!!!");
        }
        if (element.compareToIgnoreCase("contact") ==0 ) {
             //System.out.println("contact!!!!");
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
            //System.out.println("contact!!!!");
        }
        if (element.compareToIgnoreCase("note") ==0 ) {
            //System.out.println("note!!!!");
            noteTag=new NoteTag();
            //System.out.println("note!!!!");
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
              //System.out.println("/presentity!!!!");
           presenceTag.setPresentityTag(presentityTag);
             //System.out.println("/presentity!!!!");
        }
        if (element.compareToIgnoreCase("tuple") ==0 ) {
              //System.out.println("/tuple!!!!");
           presenceTag.addTupleTag(tupleTag);
             //System.out.println("/tuple!!!!");
        }
        if (element.compareToIgnoreCase("status") ==0 ) {
             //System.out.println("/status!!!!");
           tupleTag.setStatusTag(statusTag);
            //System.out.println("/status!!!");
        }
        if (element.compareToIgnoreCase("basic") ==0 ) {
             //System.out.println("/basic!!!!");
            statusTag.setValueTag(valueTag);
            //System.out.println("/basic!!!!");
        }
        if (element.compareToIgnoreCase("contact") ==0 ) {
             //System.out.println("/contact!!!!");
            tupleTag.setContactTag(contactTag);
            //System.out.println("/contact!!!!");
        }
        if (element.compareToIgnoreCase("note") ==0 ) {
             //System.out.println("/note!!!!");
            tupleTag.setNoteTag(noteTag);
            //System.out.println("//note!!!!");
        }
    }
    
    public void characters(char buf[], int offset, int len)
    throws SAXException
    {
        String str = new String(buf, offset, len);
        if (str!=null && !str.trim().equals("") ) {
            if (element.compareToIgnoreCase("basic") ==0 ) {
                valueTag.setValue(str);
                
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

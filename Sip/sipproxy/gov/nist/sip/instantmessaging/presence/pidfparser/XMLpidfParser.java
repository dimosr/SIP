/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
* Creator: O. Deruelle (deruelle@nist.gov)                                     *
* Questions/Comments: nist-sip-dev@antd.nist.gov                               *
*******************************************************************************/
package gov.nist.sip.instantmessaging.presence.pidfparser;

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
public class XMLpidfParser extends DefaultHandler {
    
    private PresenceTag presenceTag;
    private PresentityTag presentityTag;
    private AtomTag atomTag;
    private AddressTag addressTag;
    private StatusTag statusTag;
    private MSNSubStatusTag msnSubStatusTag;
    
    private XMLReader saxParser;
    
    private long id=1000;
    
    /** start the parsing
     * @param file to parse
     * @return Vector containing the test cases
     */
    public XMLpidfParser(String fileLocation) {
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
    public XMLpidfParser() {
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
    
    public void parsePidfString(String body) {
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
             DebugIM.println("Parsing XML pidf string");
        } 
        catch (Exception e) {
            throw new SAXException("XMLpidfParser error", e);
        }
    }

    public void endDocument() throws SAXException {
        try {
           DebugIM.println("XML pidf string parsed successfully!!!");
        } 
        catch (Exception e) {
            throw new SAXException("XMLpidfParser error", e);
        }
    }

    public void startElement(String namespaceURI,
                             String lName, // local name
                             String qName, // qualified name
                             Attributes attrs)
                             throws SAXException
    {
        String element=qName;
        if (element.compareToIgnoreCase("presence") ==0 ) {
            
            presenceTag=new PresenceTag();
        }
        if (element.compareToIgnoreCase("presentity") ==0 ) {
            presentityTag=new PresentityTag();
            String uri= attrs.getValue("uri");
            if (uri!=null) {
                uri=uri.trim();
                presentityTag.setURI(uri);
            }
            else {
                DebugIM.println("ERROR, XMLpidfParser, the presentity uri is null");
            }
        }
        if (element.compareToIgnoreCase("atom") ==0 ) {
            atomTag=new AtomTag();
            String id=attrs.getValue("id");
            if (id!=null) {
                id=id.trim();
                atomTag.setId(id);
            }
            else {
                DebugIM.println("ERROR, XMLpidfParser, the atom id is null");
            }
        }
        if (element.compareToIgnoreCase("address") ==0 ) {
            addressTag=new AddressTag();
            String uri=attrs.getValue("uri");
            if (uri!=null) {
                uri=uri.trim();
                addressTag.setURI(uri);
            }
            else {
                DebugIM.println("ERROR, XMLpidfParser, the address uri is null");
            }
            
            String priority=attrs.getValue("priority");
            if (priority!=null) {
                try {
                    addressTag.setPriority(Float.parseFloat(priority.trim()));
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                DebugIM.println("DEBUG, XMLpidfParser, the priority is null");
            }
        }
        if (element.compareToIgnoreCase("status") ==0 ) {
            statusTag=new StatusTag();
            String status=attrs.getValue("status");
            if (status!=null) {
                status=status.trim();
                statusTag.setStatus(status);
            }
            else {
                DebugIM.println("ERROR, XMLpidfParser, the status status is null");
            }
           
        }
        if (element.compareToIgnoreCase("msnsubstatus") ==0 ) {
            msnSubStatusTag=new MSNSubStatusTag();
            String msnSubStatus=attrs.getValue("substatus");
            if (msnSubStatus!=null) {
                msnSubStatus=msnSubStatus.trim();
                msnSubStatusTag.setMSNSubStatus(msnSubStatus);
            }
            else {
                DebugIM.println("ERROR, XMLpidfParser, the msnsubstatus substatus is null");
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
        if (element.compareToIgnoreCase("presence") ==0 ) {
        }
        if (element.compareToIgnoreCase("presentity") ==0 ) {
           presenceTag.setPresentityTag(presentityTag);
        }
        if (element.compareToIgnoreCase("atom") ==0 ) {
           presenceTag.addAtomTag(atomTag);
        }
        if (element.compareToIgnoreCase("address") ==0 ) {
           atomTag.setAddressTag(addressTag);
        }
        if (element.compareToIgnoreCase("status") ==0 ) {
            addressTag.setStatusTag(statusTag);
        }
        if (element.compareToIgnoreCase("msnsubstatus") ==0 ) {
            addressTag.setMSNSubStatusTag(msnSubStatusTag);
        }
    }

    public void characters(char buf[], int offset, int len)
    throws SAXException
    {
        String str = new String(buf, offset, len);
    }

    public String createXMLBody(String status,String subStatus,String subscriberName,
    String contactAddress) {
        PresenceTag presenceTag=new PresenceTag();
        PresentityTag presentityTag=new PresentityTag();
        presentityTag.setURI(subscriberName);
        presenceTag.setPresentityTag(presentityTag);
        AtomTag atomTag=new AtomTag();
        StatusTag statusTag=new StatusTag();
        statusTag.setStatus(status);
        MSNSubStatusTag msnSubStatusTag=new MSNSubStatusTag();
        msnSubStatusTag.setMSNSubStatus(subStatus);
        AddressTag addressTag=new AddressTag();
        addressTag.setStatusTag(statusTag);
        addressTag.setMSNSubStatusTag(msnSubStatusTag);
        addressTag.setURI("sip:"+contactAddress);
        atomTag.setAddressTag(addressTag);
        atomTag.setId("nist-sipId"+id);
        id++;
        presenceTag.addAtomTag(atomTag);
        
        String result=presenceTag.toString();
        
        return result;
    }
    
}

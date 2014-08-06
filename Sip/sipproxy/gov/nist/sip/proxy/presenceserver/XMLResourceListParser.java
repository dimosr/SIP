/*
 * XMLResourceListParser.java
 *
 */

package gov.nist.sip.proxy.presenceserver;


import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.sip.*;
import javax.sip.message.*; 
import javax.sip.header.*;
import javax.sip.address.*;
import gov.nist.sip.proxy.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import gov.nist.javax.sip.*;



public class XMLResourceListParser extends DefaultHandler {

    private PresentityManager presentityManager;

    public XMLResourceListParser(PresentityManager pm) {
	this.presentityManager = pm;
    }
    
    
    public Vector getNotifiers(File resourceListFile) {
	Vector notifiers = new Vector();
	
	//parse the file and add all uri:s to vector

	return notifiers;

    }

}


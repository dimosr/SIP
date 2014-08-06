/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */
package net.java.sip.communicator.sip.simple;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import net.java.sip.communicator.gui.config.xml.*;
import java.io.*;
import java.util.*;
import net.java.sip.communicator.sip.*;
import net.java.sip.communicator.common.*;
import net.java.sip.communicator.common.Console;

import org.xml.sax.*;

/**
 * A helper class used to generate XML presence data. Supported formats are
 * pidf+xml (mandated by the simple and impp ietf working groups) and xpidf+xml
 * (used by msn messenger and kphone)
 *
 * @author Emil Ivov
 * @version 1.0
 */

class XmlPresenceInformationFactory
{
    private static final Console console = Console.getConsole(XmlPresenceInformationFactory.class);

    private static DocumentBuilderFactory factory = null;
    private static DocumentBuilder builder = null;

    //content types
    public static final String PIDF_XML    = "pidf+xml";
    public static final String XPIDF_XML   = "xpidf+xml";

    static final String PRESENCE_ELEMENT   = "presence";
    static final String NS_ELEMENT         = "xmlns";
    static final String NS_VALUE           = "urn:ietf:params:xml:ns:pidf";
    static final String ENTITY_ATTRIBUTE   = "entity";
    static final String TUPLE_ELEMENT      = "tuple";
    static final String ID_ATTRIBUTE       = "id";
    static final String STATUS_ELEMENT     = "status";
    static final String BASIC_ELEMENT      = "basic";
    static final String CONTACT_ELEMENT    = "contact";
    static final String PRIORITY_ATTRIBUTE = "priority";
    static final String NOTE_ELEMENT       = "note";
    static final String LANGUAGE_ATTRIBUTE = "xml:lang";

    //tags needed for the xpidf+xml document
    static final String PRESENTITY_ELEMENT       = "presentity";
    static final String URI_ATTRIBUTE            = "uri";
    static final String ADDRESS_ELEMENT          = "address";
    static final String ATOM_ELEMENT             = "atom";
    static final String MSNSUBSTATUS_ELEMENT     = "msnsubstatus";
    static final String SUBSTATUS_ELEMENT        = "substatus";

    /**
     * Creates an XML representaion of <code>presenceData</code> using the
     * the specified format format.
     *
     * @param format the format to use when encoding (pidf+xml or xpidf+xml)
     * @param presenceData the PresenceTuple to encode
     * @throws CommunicationsException if format is not recognized or if any
     * thing goes wrong while encoding the data.
     * @return a binary array containing the result xml string.
     */
    public static byte[] serializePresenceData(String        format,
                                               PresenceTuple presenceData)
        throws CommunicationsException
    {
        if (format.equals(XPIDF_XML))
                return serializeToXPidfXml(presenceData);
            else if (format.equals(PIDF_XML))
                return serializeToPidfXml(presenceData);
            else
                throw new CommunicationsException(format +
                                                  " is not a supported Presence Information Data Format!");
    }

    /**
     * Creates a CPIM representaion of the specified presence tuple using the
     * pidf+xml format.
     *
     * @param presenceData the presence tuple that we need converted in xml
     * @throws CommunicationsException if we fail to init the XML API.
     * @return a byte array containing pidf+xml (CPIM) presence data.
     */
    private static byte[] serializeToPidfXml(PresenceTuple presenceData)
        throws CommunicationsException
    {
            DocumentBuilder builder = null;
            try {
                builder = getDocumentBuilder();
            }
            catch (ParserConfigurationException ex) {
                throw new CommunicationsException("Failed to create pidf+xml document builder", ex);
            }
            Document document = builder.newDocument();


            Element presence = document.createElement(PRESENCE_ELEMENT);
            presence.setAttribute(NS_ELEMENT, NS_VALUE);
            presence.setAttribute(ENTITY_ATTRIBUTE,presenceData.getPresenceUri().toString());
            document.appendChild(presence);

            Element tUple = document.createElement(TUPLE_ELEMENT);
            tUple.setAttribute(ID_ATTRIBUTE, String.valueOf( System.currentTimeMillis()));
            presence.appendChild(tUple);

            //Write status First
            Element status = document.createElement(STATUS_ELEMENT);
            tUple.appendChild(status);

            Element basic = document.createElement(BASIC_ELEMENT);
            basic.appendChild( document.createTextNode(presenceData.getBasicStatus()));
            status.appendChild(basic);

            //Copy contact URIs
            Iterator iter = presenceData.getContacts();
            Element contactUriEl = null;
            while(iter.hasNext())
            {
                ContactUri uri = (ContactUri) iter.next();
                contactUriEl = document.createElement(CONTACT_ELEMENT);
                contactUriEl.setAttribute(PRIORITY_ATTRIBUTE,
                                          Float.toString(uri.getPriority()));

                Node cValue = document.createTextNode(uri.getContactValue());
                contactUriEl.appendChild(cValue);

                tUple.appendChild(contactUriEl);
            }

            //Add notes
            Enumeration supportedLanguages = presenceData.getNoteLanguages();
            Element noteNodeEl = null;
            while (supportedLanguages.hasMoreElements())
            {
                Locale language = (Locale)supportedLanguages.nextElement();
                noteNodeEl = document.createElement(NOTE_ELEMENT);
                noteNodeEl.setAttribute(LANGUAGE_ATTRIBUTE,language.getLanguage());
                noteNodeEl.appendChild(document.createTextNode( presenceData.getNote(language)));
                tUple.appendChild(noteNodeEl);
            }

            StringWriter stringWriter = new StringWriter();
            StringBuffer buff = new StringBuffer();
            printChildElements(document.getDocumentElement(), buff, true, "");
            System.out.println(buff.toString());
            XMLUtils.writeXML(stringWriter, document, null, null);

            return stringWriter.toString().getBytes();


    }

    /**
     * Creates a CPIM representaion of the specified presence tuple using the
     * xpidf+xml format.
     *
     * @param presenceData the presence tuple that we need converted in xml
     * @throws CommunicationsException if we fail to init the XML API.
     * @return a byte array containing xpidf+xml (CPIM) presence data.
     */
    private static byte[] serializeToXPidfXml(PresenceTuple presenceData)
        throws CommunicationsException
    {
            DocumentBuilder builder = null;
            try {
                builder = getDocumentBuilder();
            }
            catch (ParserConfigurationException ex) {
                throw new CommunicationsException("Failed to create pidf+xml document builder", ex);
            }
            Document document = builder.newDocument();

            Element presence = document.createElement(PRESENCE_ELEMENT);
            document.appendChild(presence);

            //PRESENTITY
            Element presentity = document.createElement(PRESENTITY_ELEMENT);
            presentity.setAttribute(URI_ATTRIBUTE, presenceData.getPresenceUri()+";method=SUBSCRIBE");
            presence.appendChild(presentity);


            //ATOM
            Element atom = document.createElement(ATOM_ELEMENT);
            atom.setAttribute(ID_ATTRIBUTE, String.valueOf(System.currentTimeMillis()));
            presence.appendChild(atom);

            //Copy contact URIs
           Iterator iter = presenceData.getContacts();
           Element addressEl = null;
           Element statusEl = null;
           Element msnSubStatusEl = null;
           while(iter.hasNext())
           {
               ContactUri uri = (ContactUri) iter.next();
               addressEl = document.createElement(ADDRESS_ELEMENT);
               addressEl.setAttribute(URI_ATTRIBUTE, uri.getContactValue());
               addressEl.setAttribute(PRIORITY_ATTRIBUTE,
                                         Float.toString(uri.getPriority()));

               statusEl = document.createElement(STATUS_ELEMENT);
               statusEl.setAttribute(STATUS_ELEMENT, presenceData.getBasicStatus());
               addressEl.appendChild(statusEl);


               msnSubStatusEl = document.createElement(MSNSUBSTATUS_ELEMENT);
               msnSubStatusEl.setAttribute(SUBSTATUS_ELEMENT, presenceData.getExtendedStatus());
               addressEl.appendChild(msnSubStatusEl);

               atom.appendChild(addressEl);
           }

           StringWriter stringWriter = new StringWriter();
           XMLUtils.writeXML(stringWriter, document, null, null);

           return stringWriter.toString().getBytes();


    }

    private static DocumentBuilder getDocumentBuilder()
        throws ParserConfigurationException
    {
        if (factory == null)
            factory = DocumentBuilderFactory.newInstance();

        if (builder == null)
            builder = factory.newDocumentBuilder();

        return builder;
    }
/*
    public static void main(String[] args )
        throws Exception
    {
        PresenceTuple tuple = new PresenceTuple();

        String presUri = "pres:emcho@iptel.org";
        tuple.setPresenceUri(presUri);

        ContactUri uri1 = new ContactUri();
        uri1.setContactValue("tel:0672811555");
        uri1.setPriority(0.1f);

        tuple.addContact(uri1);

        ContactUri uri2 = new ContactUri();
        uri2.setContactValue("sip:emcho@iptel.org");
        uri2.setPriority(0.9f);

        tuple.addContact(uri2);

        tuple.setBasicStatus("open");
        tuple.addNote(Locale.ENGLISH, "Ready to communicate!");
        tuple.addNote(Locale.FRENCH, "Allez y parlez moi!");


        createPidXmlfData(tuple);
        createXPidXmlfData(tuple);
    }
*/
    /**
     * Used for debugging ...
     * @param parent Element
     * @param buff StringBuffer
     * @param deep boolean
     * @param prefix String
     */
    private static void printChildElements(Element parent, StringBuffer buff, boolean deep, String prefix)
    {
        StringBuffer res = new StringBuffer();
        buff.append(prefix + "<" + parent.getNodeName());
        NamedNodeMap attrs = parent.getAttributes();
        Node node;
        for(int i = 0; i < attrs.getLength(); i++)
        {
            node = attrs.item(i);
            buff.append(" " + node.getNodeName() + "=\"" + node.getNodeValue() + "\"");
        }
        buff.append(">").append("\n");

        String data = XMLUtils.getElementTextValue(parent);
        if(data != null && data.trim().length() > 0)
            buff.append(prefix + "\t" + data).append("\n");

        data = XMLUtils.getElementCDataValue(parent);
        if(data != null && data.trim().length() > 0)
            buff.append(prefix + "\t<![CDATA[" + data + "]]>").append("\n");

        NodeList nodes = parent.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++)
        {
            node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE)
            {
                if(deep)
                    printChildElements((Element)node, buff, deep, prefix + "\t");
                else
                    buff.append(prefix + node.getNodeName()).append("\n");
            }
            else if(node.getNodeType() == Node.DOCUMENT_TYPE_NODE)
                buff.append(prefix + node.getNodeName()).append("\n");
        }

        buff.append(prefix + "</" + parent.getNodeName() + ">").append("\n");
    }

    //================================ DECODING ================================
    /**
     * Decodes <code>data</code> using the specified <code>contentSubtype</code>
     * into a PresenceTuple instance.
     * @param contentSubtype the subtype part of data's content/type
     * @param data the presence information data itself
     * @throws CommunicationsException if an error occurs while decoding data.
     * @return a PresenceTuple instance corresponding to the specified data array.
     */
    public static PresenceTuple decodePresenceInformationData(String contentSubtype, byte[] data)
        throws CommunicationsException
    {
       try{
           console.logEntry();

           DocumentBuilder builder = null;
           try {
               builder = getDocumentBuilder();
           }
           catch (ParserConfigurationException ex) {
               throw new CommunicationsException("Failed to create pidf+xml document builder", ex);
           }


           PresenceTuple presenceTuple = new PresenceTuple();

           //remove doctype node cos XML parser freaks out on xpidf.dtd
           String       dataStr = new String(data);
           int dTypeStart =  dataStr.toLowerCase().indexOf("<!doctype");
           if(dTypeStart  > 0)
           {
                   int dTypeEnd = dataStr.indexOf(">", dTypeStart);
                dataStr = dataStr.substring(0, dTypeStart )
                         + dataStr.substring(dTypeEnd+1);
                data = dataStr.getBytes();

           }


            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(data);
            Document document = null;
            try {
                document = builder.parse(byteInputStream);
            }
            catch (Exception ex) {
                throw new CommunicationsException(
                            "Failed to parse presence information data", ex);
            }

            Node root = document.getFirstChild();

            if(contentSubtype.equals(XPIDF_XML))
                decodeXPidfXml(presenceTuple, root);
            else
                decodePidfXml(presenceTuple, root);

           return presenceTuple;
       }
       finally
       {
           console.logExit();
       }

   }

   /**
    * Scans the specified XML node ant sets presenceTuple values accordingly.
    * @param presenceTuple the presenceTuple instance that we should initialize
    * according to root's contents
    * @param root the root XML node of a PIDF document.
    */
   private static void decodeXPidfXml(PresenceTuple presenceTuple, Node root)
   {
        Element presentityEl =
               XMLUtils.getChildElementByTagName((Element)root, "presentity");
        String uri = presentityEl.getAttribute("uri");

        presenceTuple.setPresenceUri(GenericURI.parseURI(uri));

        //get first atom (one dayl will have to add multi atom support)
        Element atomEl =
            XMLUtils.getChildElementByTagName((Element)root, "atom");

        //go through attom addresses
        NodeList children = atomEl.getChildNodes();
        for(int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if(child.getNodeType() == Node.ELEMENT_NODE)
            {
                Element childEl = (Element)child;
                if(!childEl.getNodeName().equalsIgnoreCase("address"))
                    continue;

                //get contact value
                ContactUri contactUri = new ContactUri();
                contactUri.setContactValue(childEl.getAttribute("uri"));

                float priority = 0.8f;
                try {
                    priority = Float.parseFloat(childEl.getAttribute("priority"));
                }
                catch (NumberFormatException ex) {
                    console.error("Failed to parse contact priority for presentity: "
                                  + presenceTuple.getPresenceUri()
                                  + " address " + contactUri.getContactValue());
                }
                contactUri.setPriority(priority);
                presenceTuple.addContact(contactUri);

                //get status information
                Element statusEl = XMLUtils.getChildElementByTagName(childEl, "status");
                if(statusEl != null)
                    presenceTuple.setBasicStatus(statusEl.getAttribute("status"));
                else
                    presenceTuple.setBasicStatus(PresenceTuple.BASIC_STATUS_CLOSED);

                Element msnSubStatusEl = XMLUtils.getChildElementByTagName(childEl, "msnsubstatus");
                if(msnSubStatusEl != null)
                    presenceTuple.setExtendedStatus(msnSubStatusEl.getAttribute("substatus"));
                else
                    presenceTuple.setExtendedStatus(presenceTuple.getBasicStatus());
            }
        }
   }

  /**
   * Scans the specified XML node ant sets presenceTuple values accordingly.
   * @param presenceTuple the presenceTuple instance that we should initialize
   * according to root's contents
   * @param root the root XML node of a PIDF document.
   */
  private static void decodePidfXml(PresenceTuple presenceTuple, Node root)
  {
       String uri = ((Element)root).getAttribute("entity");

       presenceTuple.setPresenceUri(GenericURI.parseURI(uri));

       //get first tuple (one dayl will have to add multi atom support)
       Element tupleEl =
           XMLUtils.getChildElementByTagName((Element)root, "tuple");

        Element statusEl = XMLUtils.getChildElementByTagName(tupleEl, "status");

       //go through available statuses
       NodeList children = statusEl.getChildNodes();
       for(int i = 0; i < children.getLength(); i++)
       {
           Node child = children.item(i);
           if(child.getNodeType() == Node.ELEMENT_NODE)
           {
               Element childEl = (Element)child;
               if(!childEl.getNodeName().equalsIgnoreCase("basic"))
               {
                       presenceTuple.setBasicStatus( XMLUtils.getElementTextValue(childEl).trim() );
               }
               else
               {
                   presenceTuple.setExtendedStatus( XMLUtils.getElementTextValue(childEl).trim() );
               }
           }
       }

       //get contact values
       for (int i = 0; i < children.getLength(); i++) {
           Node child = children.item(i);
           if (   child.getNodeType() != Node.ELEMENT_NODE
               || !child.getNodeName().equalsIgnoreCase("contact"))
                continue;
            Element childEl = (Element)child;
            //get contact value
            ContactUri contactUri = new ContactUri();
            contactUri.setContactValue(childEl.getAttribute("uri"));

            float priority = 0.8f;
            try {
                priority = Float.parseFloat(childEl.getAttribute("priority"));
            }
            catch (NumberFormatException ex) {
                console.error("Failed to parse contact priority for presentity: "
                              + presenceTuple.getPresenceUri()
                              + " address " + contactUri.getContactValue());
            }
            contactUri.setPriority(priority);
            presenceTuple.addContact(contactUri);
        }
}


}
/*=========================== XPIDF+XML ========================================
<?xml version="1.0"?>
<!DOCTYPE presence
PUBLIC "-//IETF//DTD RFCxxxx XPIDF 1.0//EN" "xpidf.dtd">
<presence>
    <presentity uri="sip:emcho@iptel.org;method=SUBSCRIBE" />
    <atom id="1000">
        <address uri="sip:emcho@130.79.90.54;user=ip" priority="0,800000">
            <status status="open" />
            <msnsubstatus substatus="online" />
        </address>
    </atom>
</presence>

============================= PIDF+XML =========================================
<?xml version="1.0" encoding="UTF-8"?>
<presence xmlns="urn:ietf:params:xml:ns:pidf" entity="pres:someone@example.com">
    <tuple id="sg89ae">
        <status>
            <basic>open</basic>
        </status>
       <contact priority="0.8">tel:+09012345678</contact>
    </tuple>
 </presence>

 */

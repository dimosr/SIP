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
package net.java.sip.communicator.sip.simple.storage;

import net.java.sip.communicator.sip.simple.*;
import org.w3c.dom.*;
import java.io.*;
import javax.xml.parsers.*;
import net.java.sip.communicator.common.*;
import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.gui.config.xml.*;
import net.java.sip.communicator.sip.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class ContactListSerializer
{
    private static final Console console = Console.getConsole(ContactListSerializer.class);

    private static DocumentBuilderFactory factory = null;
    private static DocumentBuilder        builder = null;


    public static final String CONTACT_LIST = "contact-list";
    public static final String GROUP        = "group";
    public static final String CONTACT      = "contact";
    public static final String ALIAS        = "alias";
    public static final String NAME         = "name";
    public static final String PRESENTITY   = "presentity";
    public static final String URI          = "uri";
    public static final String PRIORITY     = "priority";

    private ContactListSerializer()
    {
    }

    /**
     * Load a Contact List from the location (file) specified by
     * <code>resourceName</code>.
     * @param resourceName The name of the file containing the contact list.
     * @throws IOException if reading the contact list file fails.
     * @return the contact list contained in the file specified by
     * <code>resourceName</code>
     */
    public static ContactGroup loadList(String resourceName) throws IOException
    {
        try{
            console.logEntry();

            ContactGroup contactList = new ContactGroup();
            contactList.setDisplayName("Contact List");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            File file = new File(resourceName);
            if(!file.exists())
                file.createNewFile();

            if(file.length() > 0)
            {
                FileInputStream fileInputStream = new FileInputStream(file);
                Document document = builder.parse(fileInputStream);

                Node root = document.getFirstChild();

                Node temp = null;
                NodeList children = root.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    temp = children.item(i);

                    if (temp.getNodeType() == Node.ELEMENT_NODE) {
                        Contact newC = processNode(temp);
                        contactList.addContact(newC);
                    }
                }
            }

            return contactList;
        }
        catch(Exception exc)
        {
            console.error(exc);
            throw new IOException("An exception aoccurred while reading contact list file.");
        }
        finally
        {
            console.logExit();
        }

    }

    private static Contact processNode(Node node)
    {
        Node temp = null;
        NodeList children = node.getChildNodes();

        //process groups
        if (node.getNodeName().equalsIgnoreCase(GROUP)) {
            ContactGroup group = new ContactGroup();
            group.setDisplayName(XMLUtils.getAttributeByName(node, NAME));

            for (int i = 0; i < children.getLength(); i++) {
                temp = children.item(i);

                if (temp.getNodeType() == Node.ELEMENT_NODE) {
                        group.addContact( processNode(temp) );
                }
            }


            return group;
        }
        //process contacts
        else if (node.getNodeName().equalsIgnoreCase(CONTACT)) {
            Contact contact = new Contact();
            contact.setDisplayName(XMLUtils.getAttributeByName(node, ALIAS));
            contact.setPresenceUri(GenericURI.parseURI( XMLUtils.getAttributeByName(node, PRESENTITY)));

            if(console.isDebugEnabled())
                console.debug("Processing contact "
                              + contact.getDisplayName()
                              + " - " + contact.getPresenceUri());

            for (int i = 0; i < children.getLength(); i++) {
                temp = children.item(i);

                if (temp.getNodeType() == Node.ELEMENT_NODE
                    && temp.getNodeName() == URI) {
                        ContactUri uri = new ContactUri();

                        String priorityStr =  XMLUtils.getAttributeByName(temp, PRIORITY);
                        float priority = 0.5F;
                        try {
                            priority = Float.parseFloat(priorityStr);
                        }
                        catch (NumberFormatException ex) {
                            console.error("Failed to read priority for presence entity \""
                                          + contact.getPresenceUri() + "\". "
                                          + priorityStr
                                          + " is not a valid priority. Using default priority - "
                                          + priority);
                        }

                        String contactValue = XMLUtils.getElementTextValue((Element)temp);
                        if(contactValue != null)
                                uri.setContactValue( contactValue );

                        uri.setPriority(priority);
                        contact.addContactUri(uri);

                        if(console.isDebugEnabled())
                            console.debug("Added contact uri " + uri.getContactValue() + " with priority of " + priority);
                }
            }

            return contact;
        }

        return null;
    }

    /**
     * Stores <code>contactList</code> to the location specified by
     * <code>resourceName</code>.
     * @param resourceName the name of the file where contactList is to be stored.
     * @throws CommunciationsException if we fail while writing to the specified
     * location.
     */
    public static void storeContactList(String       resourceName,
                                        ContactGroup contactList)
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


        Element contactListRoot = document.createElement(CONTACT_LIST);
        document.appendChild(contactListRoot);
        appendContactGroupToXmlDocument(contactList, contactListRoot, document);

        File file = new File(resourceName);
        if (!file.exists())
            try {
                file.createNewFile();
            }
            catch (IOException ex) {
                throw new CommunicationsException("Failed to create contact list file with name:" + resourceName, ex);
            }

        try {
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(file);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(domSource, streamResult);
        }
        catch (Exception ex) {
            throw new CommunicationsException("Failed to transfomr contact-list XMLdocument to a file!", ex);
        }


    }

    private static void appendContactGroupToXmlDocument(ContactGroup    group,
                                                        Element         root,
                                                        Document        document)
    {
        for (int i = 0; i < group.getChildCount(); i++)
        {
            Contact currentChild = group.getChild(i);
            if(currentChild instanceof ContactGroup)
            {
                //Create and init an element for the group
                Element groupElement = document.createElement(GROUP);
                groupElement.setAttribute(NAME, currentChild.getDisplayName());

                //append the group to the xml tree
                root.appendChild(groupElement);

                //append group children to the xml tree
                appendContactGroupToXmlDocument((ContactGroup)currentChild, groupElement, document);
            }
            else
            {
                //Creat and init the child element.
                Element contactElement = document.createElement(CONTACT);
                contactElement.setAttribute(ALIAS, currentChild.getDisplayName());
                contactElement.setAttribute(PRESENTITY, currentChild.getPresenceUri().toString());

                //Add the child to the xml tree
                root.appendChild(contactElement);

                /** @todo  save ContactURIs*/
            }
        }
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


}

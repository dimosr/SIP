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
package net.java.sip.communicator.gui.config.xml;

import java.io.File;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import net.java.sip.communicator.common.Console;
import java.io.PrintStream;
import org.w3c.dom.NamedNodeMap;
import java.io.*;

/**
 * Common XML Tasks
 *
 * @author Damian Minkov
 */
public class XMLUtils
{
    private static Console console = Console.getConsole(XMLUtils.class);
    static
    {
        //avoid trace messages in this class
        if(console.isTraceEnabled())
            console.setToDebugLevel();
    }

    /**
     * Get the attribute with given name's value
     * @param node the node which attribute's value is returned
     * @param name name of the attribute
     * @return the value af the attribute
     */
    public static String getAttributeByName(Node node, String name)
    {
        try
        {
            console.logEntry();

            if(node == null)
                return null;

            Node attribute = node.getAttributes().getNamedItem(name);
            if(attribute == null)
                return null;
            else
                return attribute.getNodeValue().trim();
        }
        finally
        {
           console.logExit();
        }
    }

    /**
     * Get the data of the element , no matter whether it is
     * TXT ot CDATA
     *
     * @param parentNode the node which data is returned
     * @return the TEXT or CDATA of the parentNode
     */
    public static String getElementTextValue(Element parentNode)
    {
        try
        {
            console.logEntry();

            Text text = getElementTextNode(parentNode);
            if(text != null)
                return text.getData();
            else
                return null;
        }
        finally
        {
           console.logExit();
        }
    }

    /**
     * Sets element TEXT data
     *
     * @param e the element
     * @param data the new data
     */
    public static void setElementTextValue(Element e, String data)
    {
        try
        {
            console.logEntry();

            Text txt = getElementTextNode(e);
            if(txt != null)
                txt.setData(data);
            else
            {
                txt = e.getOwnerDocument().createTextNode(data);
                e.appendChild(txt);
            }
        }
        finally
        {
           console.logExit();
        }
    }

    /**
     * Sets element CDATA data
     *
     * @param e the lement
     * @param data the new data
     */
    public static void setElementCDataValue(Element e, String data)
    {
        try
        {
            console.logEntry();

            CDATASection txt = getElementCDataNode(e);
            if(txt != null)
                txt.setData(data);
            else
            {
                txt = e.getOwnerDocument().createCDATASection(data);
                e.appendChild(txt);
            }
        }
        finally
        {
           console.logExit();
        }
    }

    /**
     * Gets CDATA value of an element
     * @param e the element
     * @return CDATA value of element e
     */
    public static String getElementCDataValue(Element e)
    {
        try
        {
            console.logEntry();

            CDATASection text = getElementCDataNode(e);
            if(text != null)
                return text.getData().trim();
            else
                return null;
        }
        finally
        {
           console.logExit();
        }
    }


    /**
     * Returns element's CDATA Node
     * @param element the element which CDATA node is returned
     * @return CDATA node
     */
    public static CDATASection getElementCDataNode(Element element)
    {
        try
        {
            console.logEntry();

            return (CDATASection)getChildNodeByType(element, Node.CDATA_SECTION_NODE);
        }
        finally
        {
           console.logExit();
        }
    }

    /**
     * Returns element's TEXT Node
     * @param element the element which TEXT node is returned
     * @return TEXT node
     */
    public static Text getElementTextNode(Element element)
    {
        try
        {
            console.logEntry();

            return (Text)getChildNodeByType(element, Node.TEXT_NODE);
        }
        finally
        {
           console.logExit();
        }
    }


    private static Node getChildNodeByType(Element element, short nodeType)
    {
        if(element == null)
            return null;

        NodeList nodes = element.getChildNodes();
        if(nodes == null || nodes.getLength() < 1)
            return null;

        Node node;
        String data;
        for(int i = 0; i < nodes.getLength(); i++)
        {
            node = nodes.item(i);
            short type = node.getNodeType();
            if(type == nodeType)
            {
                if(type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE)
                {
                    data = ((Text)node).getData();
                    if(data == null || data.trim().length() < 1)
                        continue;
                }

                return node;
            }
        }

        return null;
    }

    /**
     * Writes the specified document to the given file.
     * The default encoding is UTF-8.
     *
     * @param out the output File
     * @param document the document to be writen
     */
    public static void writeXML(File out, Document document)
    {
        try {
            console.logEntry();

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(out);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(domSource, streamResult);
//			printChildElements((Element)document.getFirstChild(), System.out, true, "");
        }
        catch (TransformerException ex) {
            console.error("Error saving configuration file", ex);
        }
        catch (IllegalArgumentException ex) {
            console.error("Error saving configuration file", ex);
        }
        finally {
            console.logExit();
        }
    }


    public static void writeXML(Writer   writer,
                                Document document,
                                String   doctypeSystem,
                                String   doctypePublic)
    {
        try
       {
           console.logEntry();

           DOMSource domSource = new DOMSource(document);
           StreamResult streamResult = new StreamResult(writer);
           TransformerFactory tf = TransformerFactory.newInstance();
           Transformer serializer = tf.newTransformer();
           if(doctypeSystem != null)
                   serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctypeSystem);
            if(doctypePublic != null)
                   serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctypePublic);
           serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
           serializer.setOutputProperty(OutputKeys.INDENT, "yes");
           serializer.transform(domSource, streamResult);
//			printChildElements((Element)document.getFirstChild(), System.out, true, "");
       }
       catch(TransformerException ex)
       {
           console.error("Error saving configuration file", ex);
       }
       catch(IllegalArgumentException ex)
       {
           console.error("Error saving configuration file", ex);
       }
       finally
       {
          console.logExit();
       }

    }

    /**
     * Returns the element which is at the end of the specified
     * chain  <parent><child><grandchild>...
     * @param element
     * @param chain
     * @return
     */
    public static Element getChildElementByChain(Element element, String[] chain, boolean create)
    {
        if(chain == null)
            return null;
        Element e = element;
        for(int i=0; i<chain.length; i++)
        {
            if(e == null)
                return null;
            e = getChildElementByTagName(e, chain[i]);
        }
        return e;
    }

    /**
     * Creates (only if necessary) and returns the element which is at the end of the specified
     * path.
     * @param doc the target document where the specified path should be created
     * @param path a dot separated string indicating the path to be created
     * @return the component at the end of the newly created path.
     */
    public static Element createLastPathComponent(Document doc, String[] path)
    {
        Element parent = (Element)doc.getFirstChild();
        if(   path   == null
           || parent == null
           || doc   == null)
            throw new IllegalArgumentException("Document parent and path must not be null");

        Element e = parent;
        for(int i=0; i < path.length; i++)
        {
            Element newEl = getChildElementByTagName(e, path[i]);
            if(newEl == null)
            {
                newEl = doc.createElement(path[i]);
                e.appendChild(newEl);
            }
            e = newEl;
        }
        return e;
    }

    /**
     * Returns the child element with the specified tagName for the specified parent element
     * @param parent
     * @param tagName
     * @return
     */
    public static Element getChildElementByTagName(Element parent, String tagName)
    {
        if(parent == null || tagName == null)
            return null;

        NodeList nodes = parent.getChildNodes();
        Node node;
        int len = nodes.getLength();
        for(int i = 0; i < len; i++)
        {
            node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && ((Element)node).getNodeName().equals(tagName))
                return (Element)node;
        }

        return null;
    }

    /**
     * Used for debuging
     *
     * @param parent Element
     * @param out PrintStream
     * @param deep boolean
     * @param prefix String
     */
    private static void printChildElements(Element parent, PrintStream out, boolean deep, String prefix)
    {
        out.print(prefix + "<" + parent.getNodeName());
        NamedNodeMap attrs = parent.getAttributes();
        Node node;
        for(int i = 0; i < attrs.getLength(); i++)
        {
            node = attrs.item(i);
            out.print(" " + node.getNodeName() + "=\"" + node.getNodeValue() + "\"");
        }
        out.println(">");

        String data = getElementTextValue(parent);
        if(data != null && data.trim().length() > 0)
            out.println(prefix + "\t" + data);

        data = getElementCDataValue(parent);
        if(data != null && data.trim().length() > 0)
            out.println(prefix + "\t<![CDATA[" + data + "]]>");

        NodeList nodes = parent.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++)
        {
            node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE)
            {
                if(deep)
                    printChildElements((Element)node, out, deep, prefix + "\t");
                else
                    out.println(prefix + node.getNodeName());
            }
        }

        out.println(prefix + "</" + parent.getNodeName() + ">");
    }

}

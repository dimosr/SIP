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
package net.java.sip.communicator.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.java.sip.communicator.gui.config.xml.XMLUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 *  Cares for the properties either comming from the
 *  config files or from the system
 *
 *  @author Damian Minkov
 */

public class PropertiesDepot
{
    private static Console console = Console.getConsole(PropertiesDepot.class);

    private static Hashtable properties = new Hashtable();

    private static Hashtable newProperties = new Hashtable();

    private static final String ATTRIBUTE_VALUE = "value";
    public static final String ATTRIBUTE_TYPE = "system";
    public static final String SYSTEM_PROPERTY_TYPE = "true";

    public PropertiesDepot()
    {}

    /**
     * Loads the properties from the configuration file
     */
    public static void loadProperties()
    {
        try
        {
            console.logEntry();

            traverseConfigurationFile(false);
        }
        finally
        {
            console.logExit();
        }
    }

    private static Document traverseConfigurationFile(boolean writing)
    {
        try
        {
            console.logEntry();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(getConfigurationFile());

            Node root = document.getFirstChild();

            Node temp = null;
            NodeList children = root.getChildNodes();
            for(int i = 0; i < children.getLength(); i++)
            {
                temp = children.item(i);

                if(temp.getNodeType() == Node.ELEMENT_NODE)
                {

                    StringBuffer propertyNameBuff = new StringBuffer();
                    propertyNameBuff.append(temp.getNodeName());
                    parseNode(temp, propertyNameBuff, writing);
                }
            }

            return document;
        }
        catch(IOException ex)
        {
            console.error("Cannot find the configuration File", ex);
            return null;
        }
        catch(SAXException ex)
        {
            console.error("Error parsing configuration file", ex);
            return null;
        }
        catch(ParserConfigurationException ex)
        {
            console.error("Error finding configuration for default parsers", ex);
            return null;
        }
        finally
        {
            console.logExit();
        }
    }

    private static void parseNode(Node node, StringBuffer propertyNameBuff, boolean writing)
    {
        Node temp = null;
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); i++)
        {
            temp = children.item(i);

            if(temp.getNodeType() == Node.ELEMENT_NODE)
            {
                StringBuffer newPropBuff = new StringBuffer(propertyNameBuff.toString()).append(".").append(temp.getNodeName());

                String value = XMLUtils.getAttributeByName(temp, ATTRIBUTE_VALUE);

                // the value attr is present we must handle the desired property
                if(value != null)
                {
                    if(!writing)
                    {
                        handleProperty
                        (
                            newPropBuff.toString(),
                            value,
                            XMLUtils.getAttributeByName(temp, ATTRIBUTE_TYPE)
                        );
                        parseNode(temp, newPropBuff, writing);
                    }
                    else
                    {
                        Attr attr = ((Element)temp).getAttributeNode(ATTRIBUTE_VALUE);
                        if(attr != null)
                        {
                            String prop = getProperty(newPropBuff.toString());
                            if(prop != null)
                                attr.setNodeValue(prop);
                            else
                                attr.setNodeValue("");

                        }
                    }
                }
                else
                {
                    parseNode(temp, newPropBuff, writing);
                }
            }
        }
    }

    private static void handleProperty(String name, String value, String type)
    {
        if(type == null || type.equals(SYSTEM_PROPERTY_TYPE))
        {
            System.setProperty(name, value);
        }
        else
        {
            properties.put(name, value);
        }
    }

    /**
     * Get the gonfiguration file.
     *
     * @return the configuration File
     */
    protected static File getConfigurationFile()
    {
        try {
                   console.logEntry();
                   String pFileName = Utils.getSystemProperty(
                       "net.java.sip.communicator.PROPERTIES");
                   if (pFileName == null) {
                       pFileName = "sip-communicator.xml";
                   }

                   // check in working directory
                   File configFileInWorkingDir = new File(pFileName);
                   if(configFileInWorkingDir.exists())
                   {
               console.trace("work dir exists");
                       return configFileInWorkingDir;
                   }

                   // check in user.home directory
                   File configDir = new File(Utils.getSystemProperty("user.home") +
                                             File.separator +
                                             ".sip-communicator");

                   File configFileInUserHomeDir =
                       new File(configDir, pFileName);

                   if(configFileInUserHomeDir.exists())
                   {
               console.trace("file exists in userhome");
                       return configFileInUserHomeDir;
                   }

                   // if doesn't exist - create it
                   configDir.mkdirs();
               console.trace("creating properties file");
                   InputStream in = PropertiesDepot.class.getClassLoader().
                       getResourceAsStream(pFileName);
                   BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                   PrintWriter writer = new PrintWriter(new FileWriter(configFileInUserHomeDir));

                   String line = null;
                   System.out.println("Copying properties file:");
                   while( (line = reader.readLine()) != null)
                   {
                       writer.println(line);
                       console.debug(line);
                   }
                   writer.flush();
                   return configFileInUserHomeDir;
               }
               catch(IOException ex)
               {
                   console.error("Error creating config file", ex);
                   return null;
               }
               finally {
                   console.logExit();
               }
    }

    /**
     * Set configuration property
     *
     * @param propertyName name of property
     * @param propertyValue value of property
     */
    public static void setProperty(String propertyName, String propertyValue)
    {
        try
        {
            console.logEntry();

            if(propertyValue == null)
                propertyValue = "";

            if(System.getProperty(propertyName) == null)
                newProperties.put(propertyName, propertyValue);

            System.setProperty(propertyName, propertyValue);
            properties.put(propertyName, propertyValue);
        }
        finally
        {
           console.logExit();
        }
    }

    /**
     * Get configuration property value
     *
     * @param property propert name to get
     * @return the property  value
     */
    public static String getProperty(String property)
    {
        try
        {
//            console.logEntry();

            String val = Utils.getSystemProperty(property);
            if(val == null)
                val = (String)properties.get(property);

            console.debug("getProperty["+property+"="+val+"]");
            return val;

        }
        finally
        {
//           console.logExit();
        }
    }

    /**
     * Store the current properties values in
     * the configuration file
     */
    public synchronized static void storeProperties()
    {
        try
        {
            console.logEntry();

            Document doc = traverseConfigurationFile(true);
            processNewProperties(doc);
            XMLUtils.writeXML(getConfigurationFile(), doc);
        }
        finally
        {
           console.logExit();
        }
    }

    private static void processNewProperties(Document doc)
    {
        Enumeration<String> enumeration = newProperties.keys();
        while(enumeration.hasMoreElements())
        {
            String key = (String)enumeration.nextElement();
            String value = (String)newProperties.get(key);
            processNewProperty(doc, key, value);
        }
    }

    private static void processNewProperty(Document doc, String key, String value)
    {
        StringTokenizer tokenizer = new StringTokenizer(key, ".");
        String[] toks = new String[tokenizer.countTokens()];
        int i = 0;
        while(tokenizer.hasMoreTokens())
            toks[i++] = tokenizer.nextToken();

        String[] chain = new String[toks.length - 1];
        for (int j = 0; j < chain.length; j++)
        {
            chain[j] = toks[j];
        }

        String nodeName = toks[toks.length - 1];

        Element parent = XMLUtils.createLastPathComponent(doc, chain);
        Element newNode = XMLUtils.getChildElementByTagName(parent, nodeName);
        if (newNode == null)
        {
            newNode = doc.createElement(nodeName);
            parent.appendChild(newNode);
        }
        newNode.setAttribute("value", value);

    }


    public static void main(String[] args)
    {
        PropertiesDepot p = new PropertiesDepot();
        p.loadProperties();
        p.setProperty("javax.sip.EXTENSION_METHODS", "damencho");
        p.storeProperties();
    }
}

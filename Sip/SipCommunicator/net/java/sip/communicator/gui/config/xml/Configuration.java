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
import java.io.IOException;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import net.java.sip.communicator.common.Console;

/**
 * Loads the configuration from the config file
 * ans stores it in the appropriate objects
 *
 * @author Damian Minkov
 */

public class Configuration
{
	private static Console console = Console.getConsole(Configuration.class);

    private ConfiguraionTree configuraionTree = null;

    private Document document = null;

    public static final String DEFAULT_PARSER_NAME = "dom.wrappers.Xerces";

    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_TYPE = "type";

    public static final String VALUE_NODE_NAME = "value";
    public static final String ALLOWEDVALUES_NODE_NAME = "allowedvalues";
    public static final String LABLENAME_NODE_NAME = "labelname";
    public static final String DESCRIPTION_NODE_NAME = "description";
    private String tittle;

    public Configuration()
    {
        configuraionTree = new ConfiguraionTree();
    }

	/**
	 * Loads the configuration
	 */
	public void loadConfiguration()
    {
		try
		{
			console.logEntry();

		    buildTree();
		}
		finally
		{
		   console.logExit();
		}
    }

    private void buildTree()
    {
        try
        {
			console.logEntry();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(this.getClass().getResourceAsStream(getConfigurationFile()));

            Node root = document.getFirstChild();

		    tittle =  XMLUtils.getAttributeByName(root, "tittle");

            Node temp = null;
            NodeList children = root.getChildNodes();
            for(int i = 0; i < children.getLength(); i++)
            {
                temp = children.item(i);

                if(temp.getNodeType() == Node.ELEMENT_NODE)
                {
                    configuraionTree.addConfiguration
                    (
						XMLUtils.getAttributeByName(temp, ATTRIBUTE_NAME).trim(),
                        buildConfigNodes(temp)
                    );
                }
            }
        }
        catch(IOException ex)
        {
            console.error(ex);
        }
        catch(SAXException ex)
        {
            console.error(ex);
        }
        catch(ParserConfigurationException ex)
        {
            console.error(ex);
        }
		finally
		{
		    console.logExit();
		}
    }

	/**
	 * Returns instance of the configuration file
	 * @return
	 */
    public String getConfigurationFile()
    {
		return "gui-config.xml";
    }

	/**
	 *  Adding Properties to all ConfigNodes
	 */
    private Vector buildConfigNodes(Node node)
    {
		try
		{
			console.logEntry();

			Vector results = new Vector();
			Node temp = null;
			NodeList children = node.getChildNodes();
			for(int i = 0; i < children.getLength(); i++)
			{
				temp = children.item(i);
				if(temp.getNodeType() == Node.ELEMENT_NODE)
				{
					results.add(buildSingleConfigNode(temp));
				}
			}

			return results;
		}
		finally
		{
			console.logExit();
		}
    }

    private ConfigNode buildSingleConfigNode(Node node)
    {
		try
		{
			console.logEntry();

			ConfigNode res = new ConfigNode(node.getNodeName().trim());

			Node temp = null;
			NodeList children = node.getChildNodes();
			for(int i = 0; i < children.getLength(); i++)
			{
				temp = children.item(i);
				if(temp.getNodeType() == Node.ELEMENT_NODE)
				{
					res.addProperty(buildProperty(temp));
				}
			}

			return res;
		}
		finally
		{
			console.logExit();
		}
    }

    private Property buildProperty(Node node)
    {
        String name = XMLUtils.getAttributeByName(node, ATTRIBUTE_NAME);
        String allowedValuesType = null;
        String[] allowedValues = null;
        String labelName = null;
        String description = null;

        Node temp = null;
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); i++)
        {
            temp = children.item(i);
            if(temp.getNodeType() == Node.ELEMENT_NODE)
            {
                String nodeName = temp.getNodeName();
                if(nodeName.equals(ALLOWEDVALUES_NODE_NAME))
                {
                    allowedValuesType = XMLUtils.getAttributeByName(temp,
                        ATTRIBUTE_TYPE);
                    Vector re = new Vector();
				    Node tempValueNode = null;
                    NodeList childs = temp.getChildNodes();
                    for(int j = 0; j < childs.getLength(); j++)
                    {
						tempValueNode = childs.item(j);
						if(tempValueNode.getNodeType() == Node.ELEMENT_NODE)
						{
                            String v = XMLUtils.getElementTextValue((Element)tempValueNode);
	                        if(v != null && v.length() > 0)
	                            re.add(v);
						}
                    }

                    if(re.size() > 0)
                    {
                        allowedValues = new String[re.size()];
                        re.toArray(allowedValues);
                    }
                }
                else
					if(nodeName.equals(LABLENAME_NODE_NAME))
					{
						labelName = XMLUtils.getElementTextValue((Element)temp);
					}
	                else
						if(nodeName.equals(DESCRIPTION_NODE_NAME))
						{
						    description = XMLUtils.getElementCDataValue((Element)temp);
						}
            }

        }

        return new Property(name, allowedValuesType, allowedValues,
                            labelName, description);
    }

	/**
	 * Returns the configuration tree
	 * @return
	 */
    public ConfiguraionTree getConfiguraionTree()
    {
        return configuraionTree;
    }

    public String toString()
    {
        return "Configuration";
    }

	/**
	 * The tittle for this configuration
	 * Mostly used for the GUI-Window Tittle
	 * @return the tittle.
	 */
    public String getTittle()
    {
	    return tittle;
    }
}

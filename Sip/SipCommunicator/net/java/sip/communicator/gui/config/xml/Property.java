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

import org.w3c.dom.Node;
import net.java.sip.communicator.common.PropertiesDepot;

/**
 * Representing one Config property
 * This values helps building dynamicly the GUI
 *
 * @author Damian Minkov
 */
public class Property
{
	private String name = null;
	private String value = null;
	private String allowedValuesType = null;
	private String[] allowedValues = null;
	private String labelName = null;
	private String description = null;

	private Node source = null;

	/**
	 * Constructs property
	 *
	 * @param name of the property
	 * @param allowedValuesType type of visualization for allowed values
	 *                          - currently only Combobox is implemented
	 * @param allowedValues the values allowed for this property
	 * @param labelName the label for this property
	 * @param description the description
	 */
	public Property(String name,
					String allowedValuesType,
					String[] allowedValues,
					String labelName,
					String description)
	{
		this.source = source;
		this.name = name;
		this.value = value;
		this.allowedValuesType = allowedValuesType;
		this.allowedValues = allowedValues;
		this.labelName = labelName;
		this.description = description;
	}

	/**
	 *  Constructs a Property with no allowed values
	 *
	 * @param name name
	 * @param labelName label
	 * @param description description
	 */
	public Property(String name,
					String labelName,
					String description)
	{
		this(name, null, null, labelName, description);
	}

	/**
	 * The name of the property
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the value that was read from the config file
	 * @return the value
	 */
	public String getValue()
	{
		if(value == null)
		{
			value = PropertiesDepot.getProperty(name);
		}

		return value;
	}

	/**
	 * Returns the allowed values representation type if any
	 * @return the type
	 */
	public String getAllowedValuesType()
	{
		return allowedValuesType;
	}

	/**
	 * Returns the allowed values
	 * @return the allowed values
	 */
	public String[] getAllowedValues()
	{
		return allowedValues;
	}

	/**
	 * Returns the label
	 * @return the label
	 */
	public String getLabelName()
	{
		return labelName;
	}

	/**
	 * The description for this property
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets this property value
	 *
	 * @param value the new value
	 */
	public void setValue(String value)
	{
	    PropertiesDepot.setProperty(name, value);
		this.value = value;
	}
}
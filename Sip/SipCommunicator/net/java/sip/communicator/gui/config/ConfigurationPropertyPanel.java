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
package net.java.sip.communicator.gui.config;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.gui.config.xml.ConfigNode;
import net.java.sip.communicator.gui.config.xml.Property;

/**
 *  This is a property panel
 *  Panel which is shown on the right of the tree with configurations.
 *  The content is dynamicly generated from the config file and
 *  coresponding the selection on the tree
 *
 *  @author Damian Minkov
 */

public class ConfigurationPropertyPanel
	extends JPanel
{
	private static Console console = Console.getConsole(ConfigurationPropertyPanel.class);

	private ConfigNode configNode = null;
	private static final String COMBOBOX_TYPE = "combo";

	// the parent panel instance is static in order not to set it every time
	// it is same for all instances
	private static ConfigurationPanel parentPanel = null;

	private ChangesListener changesListener = null;
	// Property to changed value connection
	private static Hashtable changes = null;

	private static Hashtable fieldToPropertyConnection = new Hashtable();

	public ConfigurationPropertyPanel(ConfigNode configNode)
	{
		this.configNode = configNode;

		this.setMinimumSize(new Dimension(700, 200));
		init();
	}

	private void init()
	{
		try
		{
			console.logEntry();

			// in order to create an empty one to fill the rigt part of the window
			// when we initsialise it and we haven't selected nothing in the tree yet
			if(configNode == null)
			{
				return;
			}

			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

//			fieldToPropertyConnection = new Hashtable();
			changes = new Hashtable();
			changesListener = new ChangesListener();

			Property[] props = configNode.getProperties();
			for(int i = 0; i < props.length; i++)
			{
				this.add(generatePropertyComponent(props[i]));
				this.add(Box.createVerticalStrut(10));
			}
		}
		finally
		{
		   console.logExit();
		}
	}

	private Component generatePropertyComponent(Property property)
	{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Property"));


		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.setPreferredSize(new Dimension(340, 160));
		panel.setMinimumSize(new Dimension(340, 160));
		panel.setMaximumSize(new Dimension(400, 160));

		JPanel labelTextPanel = new JPanel();

		JLabel label = new JLabel(property.getLabelName());
		label.setMinimumSize(new Dimension(150, 25));
		label.setMaximumSize(new Dimension(150, 25));
		label.setPreferredSize(new Dimension(150, 25));
		labelTextPanel.add(label);

		if(property.getAllowedValuesType() != null &&
		   property.getAllowedValuesType().length() > 0)
		{
			if(property.getAllowedValuesType().equals(COMBOBOX_TYPE))
			{
				String values[] = property.getAllowedValues();
				JComboBox combo = new JComboBox(values);
				combo.addFocusListener(changesListener);
				if(Arrays.asList(values).contains(property.getValue()))
				{
					combo.setSelectedItem(property.getValue());
				}
				combo.setMinimumSize(new Dimension(100, 25));
				combo.setMaximumSize(new Dimension(100, 25));
				combo.setPreferredSize(new Dimension(100, 25));

				labelTextPanel.add(combo);

				fieldToPropertyConnection.put(new Integer(combo.hashCode()),
											  property);
			}
		}
		else
		{
			JTextField field = new JTextField(property.getValue(), 20);
			field.addFocusListener(changesListener);
			field.setMinimumSize(new Dimension(40, 25));
			field.setMaximumSize(new Dimension(40, 25));
			field.setPreferredSize(new Dimension(40, 25));
			labelTextPanel.add(field);

			fieldToPropertyConnection.put(new Integer(field.hashCode()),
										  property);
		}

		panel.add(labelTextPanel);

		JPanel scrollPanel = new JPanel(new BorderLayout());
		TitledBorder border =  BorderFactory.createTitledBorder("Description");
		border.setTitleJustification(TitledBorder.CENTER);
		scrollPanel.setBorder(border);

		JScrollPane scroll = new JScrollPane();
		scroll.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		scroll.setPreferredSize(new Dimension(200, 70));
		scroll.setMinimumSize(new Dimension(200, 70));
		scroll.setMaximumSize(new Dimension(200, 70));
		scroll.setViewportView(new JLabel("<html><body><p width=335 border=1>" +
										  property.getDescription() +
										  "</p></body></html>"));
		scrollPanel.add(scroll, BorderLayout.CENTER);

		panel.add(scrollPanel);

		return panel;
	}

	/**
	 * Saves the changes made in the panel
	 */
	public static void saveChanges()
	{
		try
		{
			console.logEntry();

			Iterator iter = changes.keySet().iterator();
			while(iter.hasNext())
			{
				Object item = (Object)iter.next();
				if(item instanceof Property)
				{
					((Property)item).setValue((String)changes.get(item));
				}
			}

			changes.clear();

			setSaveButtonEditable(false);
		}
		finally
		{
		   console.logExit();
		}
	}

	/**
	 * Sets  the parent of this panel
	 *
	 * @param parentPanel the parent
	 */
	public void setParentPanel(ConfigurationPanel parentPanel)
	{
		this.parentPanel = parentPanel;
	}

	private static void setSaveButtonEditable(boolean b)
	{
		parentPanel.getSaveButton().setEnabled(b);
	}

	private class ChangesListener
		implements FocusListener
	{
		public void focusGained(FocusEvent e)
		{
		    setSaveButtonEditable(true);
		}

		public void focusLost(FocusEvent e)
		{
			Component c = e.getComponent();
			if(c instanceof JTextField)
			{
				String value = ((JTextField)c).getText();

				changes.put(fieldToPropertyConnection.get(new Integer(c.
					hashCode())), value);
			}
			else
			if(c instanceof JComboBox)
			{
				String value = ((JComboBox)c).getSelectedItem().toString();

				changes.put(fieldToPropertyConnection.get(new Integer(c.
					hashCode())), value);
			}
		}
	}
}
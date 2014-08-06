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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.tree.DefaultMutableTreeNode;

import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.common.PropertiesDepot;
import net.java.sip.communicator.gui.config.xml.Configuration;

/**
 *  This is the main panel for configuration
 *  it is supposed to be embded in some conatiner like JFrame or JWindow
 *
 *  @author Damian Minkov
 */

public class ConfigurationPanel
	extends JPanel
{
	private static Console console = Console.getConsole(ConfigurationPanel.class);

	private BorderLayout borderLayout1 = new BorderLayout();
	private JSplitPane splitPane = null;
	private JButton closeButton = null;
	private JButton saveButton = null;
	private Container owner = null;
	private Configuration configuration = null;
	private ConfigurationPropertyPanel rightPanel = null;

	public ConfigurationPanel(Window owner)
	{
		this.owner = owner;

		try
		{
			jbInit();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	/**
	 * Initsialisation goes here
	 *
	 * @throws java.lang.Exception
	 */
	void jbInit()
		throws Exception
	{
		try
		{
			console.logEntry();

			this.setLayout(borderLayout1);

			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setDividerLocation(180);

			configuration = new Configuration();
			configuration.loadConfiguration();

			JScrollPane treeView = new JScrollPane(new ConfigurationTreeUI(new
				DefaultMutableTreeNode(configuration), splitPane));
			treeView.setMinimumSize(new Dimension(180, 200));
			splitPane.setLeftComponent(treeView);

			rightPanel = new ConfigurationPropertyPanel(null);
			rightPanel.setParentPanel(this);
			JScrollPane rigth = new JScrollPane(rightPanel);
			splitPane.setRightComponent(rigth);

			this.add(splitPane, BorderLayout.CENTER);

			JPanel buttonPanel = new JPanel();
			saveButton = new JButton("Save");
			saveButton.setEnabled(false);
			saveButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ConfigurationPropertyPanel.saveChanges();
						save();
						getSaveButton().setEnabled(false);
					}
				}
			);

			buttonPanel.add(saveButton);

			closeButton = new JButton("Close");
			closeButton.addActionListener(
				new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					owner.setVisible(false);
				}
			}
			);
			buttonPanel.add(closeButton);

			this.add(buttonPanel, BorderLayout.SOUTH);
		}
		finally
		{
		    console.logExit();
		}
	}

	private void save()
	{
	    PropertiesDepot.storeProperties();
	}

	/**
	 * Gets the Save button from the panel
	 * @return the save button
	 */
	public JButton getSaveButton()
	{
		return saveButton;
	}

	/**
	 * Returns the tittlew that should be on
	 * the parent conatainer of this panel
	 *
	 * @return the tittle
	 */
	public String getTittle()
	{
		return configuration.getTittle();
	}
}
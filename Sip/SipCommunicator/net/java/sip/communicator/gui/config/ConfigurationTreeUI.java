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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.gui.config.xml.ConfigNode;
import net.java.sip.communicator.gui.config.xml.Configuration;

/**
 *  Represents the tree on the left of the
 *  configuration panel
 *
 * @author Damian Minkov
 */

public class ConfigurationTreeUI
	extends JTree
{
	private static Console console = Console.getConsole(ConfigurationTreeUI.class);

	private DefaultMutableTreeNode root = null;
	private JSplitPane splitPanel = null;

	public ConfigurationTreeUI(DefaultMutableTreeNode root, JSplitPane splitPanel)
	{
		super(root);
		this.root = root;
		this.splitPanel = splitPanel;

		this.setRootVisible(false);

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

		// Set some good icons here
//		renderer.setOpenIcon(null);
//		renderer.setClosedIcon(null);
//		renderer.setLeafIcon(null);

		this.setCellRenderer(renderer);

		init();
	}

	private void init()
	{
		try
		{
			console.logEntry();

			Configuration configuration = (Configuration)root.getUserObject();

			String names[] = configuration.getConfiguraionTree().
				getConfigurationNames();
			for(int i = 0; i < names.length; i++)
			{
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(names[i]);
				root.add(node);
				Vector configs = configuration.getConfiguraionTree().getConfigNode(names[i]);
				Iterator iter = configs.iterator();
				while (iter.hasNext())
				{
					ConfigNode item = (ConfigNode)iter.next();
					node.add(new DefaultMutableTreeNode(item));
				}
			}

			expandAll(this, true);

			this.addTreeSelectionListener(
				new TreeSelectionListener()
				{
					public void valueChanged(TreeSelectionEvent e)
					{
						DefaultMutableTreeNode node = (DefaultMutableTreeNode)
							   ConfigurationTreeUI.this.getLastSelectedPathComponent();
						if(node.getUserObject() instanceof ConfigNode)
						{
							ConfigNode configNode = (ConfigNode)node.getUserObject();
							Component component = splitPanel.getRightComponent();
							if(component instanceof JScrollPane)
							{
								((JScrollPane)component).setViewportView(new ConfigurationPropertyPanel(configNode));
							}
						}
					}
				}
			);
		}
		finally
		{
			console.logExit();
		}
	}

	/**
	 * If expand is true, expands all nodes in the tree.
	 * Otherwise, collapses all nodes in the tree.
	 *
	 * @param tree the tree we are operting on
	 * @param expand the expand prameter
	 */
	public void expandAll(JTree tree, boolean expand)
	{
		TreeNode root = (TreeNode)tree.getModel().getRoot();

		// Traverse tree from root
		expandAll(tree, new TreePath(root), expand);
	}

	private void expandAll(JTree tree, TreePath parent, boolean expand)
	{
		// Traverse children
		TreeNode node = (TreeNode)parent.getLastPathComponent();
		if(node.getChildCount() >= 0)
		{
			for(Enumeration e = node.children(); e.hasMoreElements(); )
			{
				TreeNode n = (TreeNode)e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}

		// Expansion or collapse must be done bottom-up
		if(expand)
		{
			tree.expandPath(parent);
		}
		else
		{
			tree.collapsePath(parent);
		}
	}

}
package net.java.sip.communicator.plugin.addressselector;

import java.net.*;
import java.util.*;
import javax.swing.*;
import java.io.*;
import javax.swing.tree.*;
import net.java.sip.communicator.common.*;
import net.java.sip.communicator.common.Console;

import java.awt.Component;
import javax.swing.event.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class NetworkAddressSelector
{
    private static final Console console = Console.getConsole(NetworkAddressSelector.class);

    public NetworkAddressSelector()
    {
    }

    private static class TRenderer
        extends DefaultTreeCellRenderer
        implements TreeCellRenderer
    {

        /**
         * Sets the value of the current tree cell to <code>value</code>.
         *
         * @return the <code>Component</code> that the renderer uses to draw the
         *   value
         * @param tree JTree
         * @param value Object
         * @param selected boolean
         * @param expanded boolean
         * @param leaf boolean
         * @param row int
         * @param hasFocus boolean
         * @todo Implement this javax.swing.tree.TreeCellRenderer method
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus)
        {
           try{
                console.logEntry();
                DefaultTreeCellRenderer r = (DefaultTreeCellRenderer)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

                r.setIcon(
                    leaf
                    ? new ImageIcon(Utils.getResource("@.gif"))
                    : new ImageIcon(Utils.getResource("card.gif")));
                return r;
            }
            finally
            {
                console.logExit();
            }

        }

    }

    /**
     * The class is used to prevent the user from selecting anything else than
     * a network address.
     */
    private static class TsListener implements TreeSelectionListener
    {
        JTree tree = null;
        public TsListener(JTree tree)
        {
            this.tree = tree;
            tree.addTreeSelectionListener(this);
        }
        /**
         * valueChanged
         *
         * @param e TreeSelectionEvent
         */
        public void valueChanged(TreeSelectionEvent e)
        {
           try{
                console.logEntry();
                TreePath path = e.getNewLeadSelectionPath();
                if(path != null
                   && path.getPathCount() > 0
                   && !((TreeNode)path.getLastPathComponent()).isLeaf())
                {
                    tree.removeTreeSelectionListener(this);
                    tree.setSelectionPath(e.getOldLeadSelectionPath());
                    tree.addTreeSelectionListener(this);
                }
            }
            finally
            {
                console.logExit();
            }
        }
    }


    /**
     * Creates a JTree containing local network interfaces as folders
     * and network addresses as leafs.
     * @throws IOException if we fail to extrace network addresses
     * @return JTree a JTree containing local network interfaces as folders
     * and network addresses as leafs.
     */
    public static JTree createAddressTree()
        throws IOException
    {
        return createAddressTree(true);
    }

    /**
     * Creates a JTree containing local network interfaces as folders
     * and network addresses as leafs.
     * @param includeOnlyRoutableAddresses indicates whether non-routable (localhost)
     * addresses should be included in the tree.
     * @throws IOException if we fail to extrace network addresses
     * @return JTree a JTree containing local network interfaces as folders
     * and network addresses as leafs.
     */
    public static JTree createAddressTree(boolean includeOnlyRoutableAddresses)
        throws IOException
    {
        try{
            console.logEntry();
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();

            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = (NetworkInterface) interfaces.
                    nextElement();

                NetworkInterfaceTreeNode ifaceNode = new NetworkInterfaceTreeNode(
                    iface, includeOnlyRoutableAddresses);
                if(ifaceNode.getChildCount() > 0)
                    root.add(ifaceNode);
            }

            JTree addressTree = new JTree(root);
            addressTree.setRootVisible(false);
            addressTree.setShowsRootHandles(true);

            for (int i = root.getChildCount() - 1; i >= 0; i--) {
                addressTree.expandRow(i);
            }

            TRenderer r = new TRenderer();
            new TsListener(addressTree);
            addressTree.setCellRenderer(r);
            addressTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

            if(root.getChildCount() > 0 && root.getChildAt(0).getChildCount() > 0)
            {
                TreePath selection = new TreePath(new Object[]{root, root.getChildAt(0), root.getChildAt(0).getChildAt(0)});
                addressTree.setSelectionPath(selection);
            }


            return addressTree;
        }
        finally
        {
            console.logExit();
        }

    }

    /**
     *
     */
    public static void preselectAddress(JTree tree)
    {
        try {
            console.logEntry();

            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)
                                                    (tree.getModel().getRoot());

            NetworkAddressManager.start();
            InetAddress localhost = NetworkAddressManager.getLocalHost(false);
            NetworkAddressManager.shutDown();

            for(int i = 0; i < rootNode.getChildCount(); i++)
            {
                DefaultMutableTreeNode ifaceNode =
                    ((DefaultMutableTreeNode)rootNode.getChildAt(i));

                for (int j = 0; j < ifaceNode.getChildCount(); j++)
                {
                    DefaultMutableTreeNode addrNode = (DefaultMutableTreeNode)ifaceNode.getChildAt(j);
                    InetAddress addr = (InetAddress)addrNode.getUserObject();
                    if(localhost.equals(addr))
                    {
                        tree.setSelectionPath(new TreePath(new Object[] {
                            rootNode, ifaceNode, addrNode}));
                        break;
                    }
                }
            }

        }
        finally {
            console.logExit();
        }
    }

    public static void discoverUsableNetworkAddress(JTree tree)
    {
        try {
            console.logEntry();

            /** @todo  implement */
        }
        finally {
            console.logExit();
        }
    }


}

package net.java.sip.communicator.plugin.addressselector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.*;
import java.net.*;
import net.java.sip.communicator.common.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

class NetworkAddressTreeNode
    extends DefaultMutableTreeNode
{
    private static final Console console = Console.getConsole(NetworkAddressTreeNode.class);
    public NetworkAddressTreeNode(InetAddress userObject)
    {
        super(userObject);
    }

    /**
     * Returns true if the receiver is a leaf.
     *
     * @return boolean
     * @todo Implement this javax.swing.tree.TreeNode method
     */
    public boolean isLeaf()
    {
        return true;
    }

}

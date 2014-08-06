package net.java.sip.communicator.plugin.addressselector;

import javax.swing.tree.*;
import java.net.*;
import java.util.Enumeration;
import java.util.*;
import net.java.sip.communicator.common.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

class NetworkInterfaceTreeNode
    extends DefaultMutableTreeNode
{
    private boolean  includeOnlyRoutableAddresses = false;
    private static final Console console = Console.getConsole(NetworkInterfaceTreeNode.class);
    public NetworkInterfaceTreeNode(NetworkInterface netInterface,
                                    boolean includeOnlyRoutableAddresses)
    {
        super(netInterface);
        this.includeOnlyRoutableAddresses = includeOnlyRoutableAddresses;
        setUserObject(netInterface);
    }

    /**
     * Resets the user object of the receiver to <code>object</code>.
     *
     * @param object Object
     * @todo Implement this javax.swing.tree.MutableTreeNode method
     */
    public void setUserObject(Object object)
    {
        super.setUserObject(object);
        setChildren((NetworkInterface)object);

    }

    private void setChildren(NetworkInterface iface)
    {
        Enumeration inetAddresses = iface.getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
            InetAddress addr = (InetAddress) inetAddresses.nextElement();
            if(!(includeOnlyRoutableAddresses
                 && !NetworkAddressManager.isRoutable(addr)))
                add(new NetworkAddressTreeNode(addr));
        }
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString()
    {
        return ((NetworkInterface)getUserObject()).getDisplayName();
    }

    public boolean isLeaf()
    {
        return false;
    }

}

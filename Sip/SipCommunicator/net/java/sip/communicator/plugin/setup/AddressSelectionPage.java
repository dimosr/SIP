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

package net.java.sip.communicator.plugin.setup;

import java.awt.*;
import javax.swing.*;
import net.java.sip.communicator.plugin.addressselector.*;
import java.io.*;
import net.java.sip.communicator.common.*;
import net.java.sip.communicator.common.Console;

import javax.swing.border.*;
import javax.swing.text.html.*;
import java.awt.event.*;
import javax.swing.tree.*;
import java.net.*;
import java.util.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class AddressSelectionPage
    extends JPanel
    implements ActionListener, SetupWizardPage
{
    private static final Console console = Console.getConsole(AddressSelectionPage.class);

    /** Use this variable if we fail to load html file */
    public static final String DEFAULT_TEXT_CONTENT                        = "Please choose the IP address you'd like to use or (if unsure) use the autodetection feature.";

    public static final String PREFERRED_ADDRESS_PROPERTY_NAME             = "net.java.sip.communicator.common.PREFERRED_NETWORK_ADDRESS";
    public static final String PREFERRED_ADDRESS_HR_PROPERTY_NAME          = "Preferred Network Address";
    public static final String PREFERRED_INTERFACE_PROPERTY_NAME           = "net.java.sip.communicator.common.PREFERRED_NETWORK_INTERFACE";
    public static final String PREFERRED_INTERFACE_HR_PROPERTY_NAME        = "Preferred Network Interface";
    public static final String PREFER_IPV4_STACK_PROPERTY_NAME             = "java.net.preferIPv4Stack";
    public static final String PREFER_IPV4_STACK_HR_PROPERTY_NAME          = "Use IPv4 Stack";
    public static final String PREFER_IPV6_ADDRESSES_PROPERTY_NAME         = "java.net.preferIPv6Addresses";
    public static final String PREFER_IPV6_ADDRESSES_HR_PROPERTY_NAME      = "Prefer IPv6 Addresses";

    private WizardPropertySet pageProperties = new WizardPropertySet();

    BorderLayout borderLayout1 = new BorderLayout();
    JScrollPane  treePane = new JScrollPane();
    JPanel       buttonPane = new JPanel();
    Border       border1;
    JButton      autoDetectButton = new JButton();
    JEditorPane  helpMessagePane = new JEditorPane();
    JTree        addressTree = null;

    public AddressSelectionPage()
    {
        try
        {
            initComponents();
            jbInit();
            treePane.setBackground(helpMessagePane.getBackground());
            NetworkAddressSelector.preselectAddress(addressTree);

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    public void initComponents()
    {
        try{
            console.logEntry();
            try {
                addressTree = NetworkAddressSelector.createAddressTree();
                treePane.setViewportView(addressTree);
                helpMessagePane.setEditorKit(new HTMLEditorKit());
                helpMessagePane.setEditable(false);
                try {
                    helpMessagePane.setText(readFile(
                        "SetupWizardAddressSelectionPage.html"));
                }
                catch (IOException ex) {
                    console.error("Failed to load SetupWizardAddressSelectionPage.html", ex);
                    helpMessagePane.setText(DEFAULT_TEXT_CONTENT);
                }
                autoDetectButton.addActionListener(this);
            }
            catch (IOException ex) {//Network address exceptions
                console.error(ex);
                Console.showException(ex);
            }
            buttonPane.setBackground(helpMessagePane.getBackground());
        }
        finally
        {
            console.logExit();
        }

    }

    public String getName()
    {
        return "Network Address";
    }
    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createEmptyBorder(10,10,10,10);
        this.setLayout(borderLayout1);
        treePane.getViewport().setBackground(Color.white);
        treePane.setMaximumSize(new Dimension(22, 22));
        treePane.setMinimumSize(new Dimension(22, 22));
        treePane.setPreferredSize(new Dimension(22, 22));
        buttonPane.setBackground(Color.white);
        buttonPane.setBorder(border1);
        buttonPane.setMaximumSize(new Dimension(20, 20));
        autoDetectButton.setMnemonic('D');
        autoDetectButton.setText("Detect Automatically");
        helpMessagePane.setMaximumSize(new Dimension(20, 100));
        helpMessagePane.setMinimumSize(new Dimension(20, 21));
        helpMessagePane.setPreferredSize(new Dimension(20, 150));
        this.add(treePane, BorderLayout.CENTER);
        this.add(buttonPane,  BorderLayout.SOUTH);
        buttonPane.add(autoDetectButton, null);
        this.add(helpMessagePane, BorderLayout.NORTH);
    }

    /**
     * Read the html file with the page instructions. An IOException is thrown
     * if the method fails reading the html content
     * @param file name of the file (without the path)
     * @return the (html) string contained by the file.
     * @throws IOException if we fail reading html content
     */
    private String readFile(String file)
        throws IOException
    {
        try{
            console.logEntry();

            BufferedReader reader = null;

            try {
                reader = new BufferedReader( new InputStreamReader(
                    getClass().getResourceAsStream( "resource" + File.separator + file)));
            }
            catch (Exception ex) {
                console.error("Failed to read html content.", ex);
                throw new IOException("Failed to read html content.");
            }

            String line = "";
            StringBuffer buff = new StringBuffer();
            try {
                while ( (line = reader.readLine() ) != null) {
                    buff.append(line).append(" ");
                }
            }
            finally{
                console.error("Failed to read html content.");
            }
            return buff.toString();
        }
        finally
        {
            console.logExit();
        }
    }

    public void actionPerformed(ActionEvent evt)
    {
        NetworkAddressSelector.discoverUsableNetworkAddress(addressTree);
    }

    public void validateContent()
        throws IllegalArgumentException
    {
        //nothing to validate
        // in the worst case there's no selection and that would only occur
        // if there are no routable addresses which means that there's nothing to do
    }

    public WizardPropertySet getPageProperties()
    {
        try{
            console.logEntry();
            TreePath selectionPath = addressTree.getSelectionPath();

            if (selectionPath != null && selectionPath.getPathCount() == 3) {
                DefaultMutableTreeNode addrNode = (DefaultMutableTreeNode)
                    selectionPath.getPathComponent(2);
                InetAddress selectedAddress = (InetAddress) addrNode.getUserObject();
                DefaultMutableTreeNode ifaceNode = (DefaultMutableTreeNode)
                    selectionPath.getPathComponent(1);
                NetworkInterface selectedInterface = (NetworkInterface) ifaceNode.
                    getUserObject();
                String preferIPv4Stack = null;
                String preferIPv6Addresses = null;

                if (selectedAddress instanceof Inet6Address)
                {
                    preferIPv4Stack = "false";
                    preferIPv6Addresses = "true";
                }
                else
                {
                    preferIPv4Stack = "true";
                    preferIPv6Addresses = "false";
                }




                pageProperties.setProperty(PREFERRED_ADDRESS_PROPERTY_NAME,
                                           PREFERRED_ADDRESS_HR_PROPERTY_NAME,
                                           selectedAddress.getHostAddress());
                pageProperties.setProperty(PREFERRED_INTERFACE_PROPERTY_NAME,
                                           PREFERRED_INTERFACE_HR_PROPERTY_NAME,
                                           selectedInterface.getDisplayName());
                pageProperties.setProperty(PREFER_IPV4_STACK_PROPERTY_NAME,
                                           PREFER_IPV4_STACK_HR_PROPERTY_NAME,
                                           preferIPv4Stack);
                pageProperties.setProperty(PREFER_IPV6_ADDRESSES_PROPERTY_NAME,
                                           PREFER_IPV6_ADDRESSES_HR_PROPERTY_NAME,
                                           preferIPv6Addresses);

            }

            return pageProperties;
        }
        finally
        {
            console.logExit();
        }

    }

    public void setPageProperties(WizardPropertySet pageProperties)
    {
        this.pageProperties = pageProperties;
    }
}

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

import javax.swing.text.html.*;
import java.io.*;
import net.java.sip.communicator.common.*;
import net.java.sip.communicator.common.Console;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Hashtable;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 */

public class IdentityPage
    extends JPanel
    implements SetupWizardPage
{
    private static final Console console = Console.getConsole(WelcomePage.class);

    /** Use this variable if we fail to load html file */
    public static final String DEFAULT_NAME_TEXT_CONTENT =
        "SIP Identity is the information that identifies you to "+
        "others when they receive your calls or instant messages.\nEnter the "+
        "name you would like to appear in the \"From\" field of your "+
        "outgoing calls or instant messages (for example \"John Smith\") ";
    public static final String DEFAULT_ADDRESS_TEXT_CONTENT =
        "Enter your SIP address. This is the address others will " +
        "use to send you instant messages or call you (for example, " +
        "\"user@example.net\")";


    public static final String DISPLAY_NAME_PROPERTY_NAME = "net.java.sip.communicator.sip.DISPLAY_NAME";
    public static final String DISPLAY_NAME_HR_PROPERTY_NAME = "Your Name";
    public static final String PUBLIC_ADDRESS_PROPERTY_NAME = "net.java.sip.communicator.sip.PUBLIC_ADDRESS";
    public static final String PUBLIC_ADDRESS_HR_PROPERTY_NAME = "SIP Address";

    private WizardPropertySet pageProperties = new WizardPropertySet();

    JEditorPane nameHelpMessage = new JEditorPane();
    JPanel namePane = new JPanel();
    JEditorPane addressHelpMessage = new JEditorPane();
    JPanel addressPane = new JPanel();
    JLabel nameLabel = new JLabel();
    JTextField nameField = new JTextField();
    JLabel addressLabel = new JLabel();
    JTextField addressField = new JTextField();
    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    Border border1;
    JPanel addressContainer = new JPanel();
    JPanel nameContainer = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();
    BorderLayout borderLayout4 = new BorderLayout();
    JEditorPane addressHelpMessage1 = new JEditorPane();
    BorderLayout borderLayout5 = new BorderLayout();

    public IdentityPage ()
    {
        initComponents();
        try
        {
            jbInit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void initComponents()
    {
        setPreferredSize(new Dimension(100, 100));
        nameHelpMessage.setEditable(false);
        nameHelpMessage.setEditorKit(new HTMLEditorKit());

        addressHelpMessage.setEditable(false);
        addressHelpMessage.setEditorKit(new HTMLEditorKit());

        try {
            nameHelpMessage.setText(readFile(
                "SetupWizardIdentityPage-Part1.html"));
        }
        catch (IOException ex) {
            console.error("Failed to load SetupWizardIdentityPage-Part1.html", ex);
            nameHelpMessage.setText(DEFAULT_NAME_TEXT_CONTENT);
        }
        try {
            addressHelpMessage.setText(readFile(
                "SetupWizardIdentityPage-Part2.html"));
        }
        catch (IOException ex) {
            console.error("Failed to load SetupWizardIdentityPage-Part2.html", ex);
            nameHelpMessage.setText(DEFAULT_ADDRESS_TEXT_CONTENT);
        }

        setBackground(addressHelpMessage.getBackground());
        namePane.setBackground(nameHelpMessage.getBackground());
        addressPane.setBackground(addressHelpMessage.getBackground());

        String displayName = PropertiesDepot.getProperty(DISPLAY_NAME_PROPERTY_NAME);
        String address     = PropertiesDepot.getProperty(PUBLIC_ADDRESS_PROPERTY_NAME);

        if( displayName != null)
            nameField.setText(displayName);
        if( address != null)
            addressField.setText(address);
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

    public String getName()
    {
        return "Identity";
    }
    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createEmptyBorder(10,40,5,40);
        this.setLayout(borderLayout5);
        nameLabel.setText("Your Name: ");
        addressLabel.setText("SIP Address: ");
        nameHelpMessage.setMaximumSize(new Dimension(400, 2147483647));
        namePane.setLayout(borderLayout1);
        addressPane.setLayout(borderLayout2);
        addressPane.setBorder(border1);
        addressPane.setMaximumSize(new Dimension(2147483647, 100));
        namePane.setBorder(border1);
        namePane.setMaximumSize(new Dimension(2147483647, 100));
        addressHelpMessage.setMaximumSize(new Dimension(400, 2147483647));
        nameContainer.setLayout(borderLayout3);
        addressContainer.setLayout(borderLayout4);
        addressHelpMessage1.setMaximumSize(new Dimension(400, 2147483647));
        addressContainer.add(addressHelpMessage, BorderLayout.CENTER);
        addressContainer.add(addressPane,  BorderLayout.SOUTH);
        namePane.add(nameLabel, BorderLayout.WEST);
        namePane.add(nameField, BorderLayout.CENTER);
        nameContainer.add(nameHelpMessage, BorderLayout.CENTER);
        nameContainer.add(namePane, BorderLayout.SOUTH);
        addressPane.add(addressLabel,  BorderLayout.WEST);
        addressPane.add(addressField, BorderLayout.CENTER);
        this.add(nameContainer,  BorderLayout.CENTER);
        this.add(addressContainer,  BorderLayout.SOUTH);
    }

    public void validateContent()
        throws IllegalArgumentException
    {
        try{
            console.logEntry();
            if(nameField.getText() == null | nameField.getText().trim().length() == 0)
                throw new IllegalArgumentException("Please enter a valid display name!");


            String sipAddress = this.addressField.getText();
            if (sipAddress == null | sipAddress.trim().length() == 0)
            {
                console.error("Invalid SIP address.");
                throw new IllegalArgumentException(
                    "Please enter a valid SIP address!");
            }

            if (!sipAddress.toLowerCase().startsWith("sip:"))
                addressField.setText("sip:" + sipAddress);
        }
        finally
        {
            console.logExit();
        }

    }

    public WizardPropertySet getPageProperties()
    {
        try{
            console.logEntry();


            pageProperties.setProperty(DISPLAY_NAME_PROPERTY_NAME, DISPLAY_NAME_HR_PROPERTY_NAME, nameField.getText());
            pageProperties.setProperty(PUBLIC_ADDRESS_PROPERTY_NAME, PUBLIC_ADDRESS_HR_PROPERTY_NAME, addressField.getText());

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

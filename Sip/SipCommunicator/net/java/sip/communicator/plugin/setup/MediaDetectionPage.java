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
import java.awt.event.*;
import net.java.sip.communicator.media.*;
import java.util.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 */

public class MediaDetectionPage
    extends JPanel
    implements ActionListener,
    SetupWizardPage

{
    private static final Console console = Console.getConsole(MediaDetectionPage.class);

    /** Use this variable if we fail to load html file */
    public static final String DEFAULT_TITLE_CONTENT = "Java Media Framework Initialization";
    public static final String DEFAULT_HELP_CONTENT =
        "Click on the button below to start detection of audio/video " +
        "devices. Attention, UNIX users, you may need to be logged as root to do that!";

    String welcomeMessage = new String();
    JEditorPane helpMessagePane = new JEditorPane();
    GridLayout gridLayout1 = new GridLayout();
    JEditorPane titlePane = new JEditorPane();
    JPanel iconPane = new JPanel();
    JPanel buttonPane = new JPanel();
    JButton startDetectionButton = new JButton();
    FlowLayout flowLayout1 = new FlowLayout();

    private WizardPropertySet pageProperties = new WizardPropertySet();

    public MediaDetectionPage()
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
       try{
            console.logEntry();
            helpMessagePane.setEditable(false);
            helpMessagePane.setEditorKit(new HTMLEditorKit());
            try {
                helpMessagePane.setText(readFile(
                    "SetupWizardMediaDetectionPage-Part2.html"));
            }
            catch (IOException ex) {
                console.error("Failed to help message page.", ex);
                titlePane.setText(DEFAULT_HELP_CONTENT);
            }

            buttonPane.setBackground(helpMessagePane.getBackground());
            startDetectionButton.addActionListener(this);

            iconPane.setBackground(helpMessagePane.getBackground());
            iconPane.add(new JLabel(new ImageIcon(getClass().getResource("resource/jmf.jpg"))), BorderLayout.CENTER);

            titlePane.setEditable(false);
            titlePane.setEditorKit(new HTMLEditorKit());
            try {
                titlePane.setText(readFile(
                    "SetupWizardMediaDetectionPage-Part1.html"));
            }
            catch (IOException ex) {
                console.error("Failed to load title page.", ex);
                titlePane.setText(DEFAULT_TITLE_CONTENT);
            }
        }
        finally
        {
            console.logExit();
        }

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
                console.error("Failed to read html content.");
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
        return "Media Detection";
    }
    private void jbInit() throws Exception
    {
        gridLayout1.setColumns(1);
        gridLayout1.setHgap(0);
        gridLayout1.setRows(0);
        this.setLayout(gridLayout1);
        this.setDebugGraphicsOptions(0);
        this.setMinimumSize(new Dimension(100, 63));
        this.setPreferredSize(new Dimension(100, 63));
        startDetectionButton.setMnemonic('S');
        startDetectionButton.setText("Start JMF Initialization");
        buttonPane.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.CENTER);
        this.add(titlePane, null);
        this.add(iconPane, null);
        this.add(helpMessagePane, null);
        this.add(buttonPane, null);
        buttonPane.add(startDetectionButton, null);
    }

    public void actionPerformed(ActionEvent evt)
    {
        try {
            MediaManager.setupJMF();
        }
        catch (MediaException ex) {
            Console.showException("Media Error!", ex);
        }
    }

    public void validateContent()
   throws IllegalArgumentException
   {

   }

   public WizardPropertySet getPageProperties()
   {
       return pageProperties;
   }

   public void setPageProperties(WizardPropertySet pageProperties)
   {
       this.pageProperties = pageProperties;
   }



}

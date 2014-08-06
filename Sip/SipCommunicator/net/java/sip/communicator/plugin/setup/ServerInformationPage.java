package net.java.sip.communicator.plugin.setup;

import java.io.*;
import net.java.sip.communicator.common.*;
import net.java.sip.communicator.common.Console;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.html.*;
import java.util.Hashtable;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class ServerInformationPage
    extends JPanel
    implements SetupWizardPage
{
    private static final Console console = Console.getConsole(ServerInformationPage.class);

    /** Use this variable if we fail to load html file */
    public static final String DEFAULT_SERVER_TEXT_CONTENT =
        "Enter the name of your SIP Server (for example,\"sip.company.com\")";
    public static final String DEFAULT_UNAME_TEXT_CONTENT =
        "Enter the user name given to you by your SIP provider (for example, \"jsmith\".";

    public static final String REGISTRAR_PROPERTY_NAME = "net.java.sip.communicator.sip.REGISTRAR_ADDRESS";
    public static final String REGISTRAR_HR_PROPERTY_NAME = "SIP Server";
    public static final String USER_NAME_PROPERTY_NAME = "net.java.sip.communicator.sip.USER_NAME";
    public static final String USER_NAME_HR_PROPERTY_NAME = "User Name";

    private WizardPropertySet pageProperties = new WizardPropertySet();

    JEditorPane serverHelpMessage = new JEditorPane();
     JPanel serverPane = new JPanel();
     JEditorPane userNameHelpMessage = new JEditorPane();
     JPanel userNamePane = new JPanel();
     JLabel serverLabel = new JLabel();
     JTextField serverField = new JTextField();
     JLabel userNameLabel = new JLabel();
     JTextField userNameField = new JTextField();
     BorderLayout borderLayout1 = new BorderLayout();
     BorderLayout borderLayout2 = new BorderLayout();
     Border border1;
     JPanel userNameContainer = new JPanel();
     JPanel serverContainer = new JPanel();
     BorderLayout borderLayout3 = new BorderLayout();
     BorderLayout borderLayout4 = new BorderLayout();
     JEditorPane addressHelpMessage1 = new JEditorPane();
    GridLayout gridLayout1 = new GridLayout();
    Border border2;

     public ServerInformationPage ()
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
             setPreferredSize(new Dimension(100, 100));
             serverHelpMessage.setEditable(false);
             serverHelpMessage.setEditorKit(new HTMLEditorKit());

             userNameHelpMessage.setEditable(false);
             userNameHelpMessage.setEditorKit(new HTMLEditorKit());

            try {
                serverHelpMessage.setText(readFile(
                    "SetupWizardServerInformationPage-Part1.html"));
            }
            catch (IOException ex) {
                console.error("Failed to load SetupWizardServerInformationPage-Part1.html");
                serverHelpMessage.setText(DEFAULT_SERVER_TEXT_CONTENT);
            }
            try {
                userNameHelpMessage.setText(readFile(
                    "SetupWizardServerInformationPage-Part2.html"));
            }
            catch (IOException ex) {
                console.error("Failed to load SetupWizardServerInformationPage-Part2.html");
                userNameHelpMessage.setText(DEFAULT_UNAME_TEXT_CONTENT);
            }

             setBackground(userNameHelpMessage.getBackground());
             serverPane.setBackground(serverHelpMessage.getBackground());
             userNamePane.setBackground(userNameHelpMessage.getBackground());

             String server = Utils.getProperty(
                 REGISTRAR_PROPERTY_NAME);
             String userName = Utils.getProperty(
                 USER_NAME_PROPERTY_NAME);

             if (server != null)
                 serverField.setText(server);
             if (userName != null)
                 userNameField.setText(userName);

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
         return "Server Information";
     }
     private void jbInit() throws Exception
     {
         border1 = BorderFactory.createEmptyBorder(10,40,40,40);
         border2 = BorderFactory.createEmptyBorder(10,40,20,40);
        this.setLayout(gridLayout1);
         serverLabel.setText("SIP Server: ");
         userNameLabel.setText("User Name: ");
         serverHelpMessage.setMaximumSize(new Dimension(400, 2147483647));
        serverHelpMessage.setPreferredSize(new Dimension(400, 21));
         serverPane.setLayout(borderLayout1);
         userNamePane.setLayout(borderLayout2);
         userNamePane.setBorder(border1);
         userNamePane.setMaximumSize(new Dimension(2147483647, 100));
         serverPane.setBorder(border2);
        serverPane.setDebugGraphicsOptions(0);
        serverPane.setMaximumSize(new Dimension(2147483647, 100));
         userNameHelpMessage.setMaximumSize(new Dimension(400, 2147483647));
         serverContainer.setLayout(borderLayout3);
         userNameContainer.setLayout(borderLayout4);
         addressHelpMessage1.setMaximumSize(new Dimension(400, 2147483647));
         gridLayout1.setColumns(1);
        gridLayout1.setRows(2);
        userNameContainer.add(userNameHelpMessage, BorderLayout.CENTER);
         userNameContainer.add(userNamePane,  BorderLayout.SOUTH);
         serverPane.add(serverLabel, BorderLayout.WEST);
         serverPane.add(serverField, BorderLayout.CENTER);
         serverContainer.add(serverHelpMessage, BorderLayout.CENTER);
         serverContainer.add(serverPane, BorderLayout.SOUTH);
         userNamePane.add(userNameLabel,  BorderLayout.WEST);
         userNamePane.add(userNameField, BorderLayout.CENTER);
         this.add(serverContainer, null);
         this.add(userNameContainer, null);
     }

    /**
     * Verifies whether the server information was validly entered by the user.
     *
     * @throws IllegalArgumentException
     */
    public void validateContent() throws IllegalArgumentException
    {
        try{
           console.logEntry();
           if(serverField.getText() == null | serverField.getText().trim().length() == 0)
               throw new IllegalArgumentException("Please enter a valid server name!");

           if(userNameField.getText() == null | userNameField.getText().trim().length() == 0)
               throw new IllegalArgumentException("Please enter a valid user name!");

       }
       finally
       {
           console.logExit();
       }

    }

    /**
     * getProperties
     *
     * @return Hashtable
     * @todo Implement this
     *   net.java.sip.communicator.plugin.setup.SetupWizardPage method
     */
    public WizardPropertySet getPageProperties()
    {

        pageProperties.setProperty(REGISTRAR_PROPERTY_NAME, REGISTRAR_HR_PROPERTY_NAME, serverField.getText());
        pageProperties.setProperty(USER_NAME_PROPERTY_NAME, USER_NAME_HR_PROPERTY_NAME, userNameField.getText());

        return pageProperties;
    }

    public void setPageProperties(WizardPropertySet pageProperties)
    {
        this.pageProperties = pageProperties;
    }


}

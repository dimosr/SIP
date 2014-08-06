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

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import net.java.sip.communicator.common.*;
import net.java.sip.communicator.plugin.setup.WizardPropertySet.*;



public class SetupWizard
    implements ActionListener
{
    private static SetupWizard setupWizardInstance = null;

    private static final Console console = Console.getConsole(SetupWizard.class);
    private ArrayList 		wizardPages  = new ArrayList();
    private WizardDialog	wizardDialog = new WizardDialog(null);
    private int 		    currentPage  = 0;
    private WizardPropertySet pProperties = null;


    public SetupWizard()
    {
        initComponents();
        initPages();
    }

    private void initPages()
    {
        try{
            console.logEntry();
            wizardPages.add(new WelcomePage());
            wizardPages.add(new IdentityPage());
            wizardPages.add(new ServerInformationPage());
            wizardPages.add(new MediaDetectionPage());
            wizardPages.add(new AddressSelectionPage());
            wizardPages.add(new SummaryPage());
        }
        finally
        {
            console.logExit();
        }

    }

    public static void start()
    {
        if(setupWizardInstance == null)
            setupWizardInstance = new SetupWizard();

        setupWizardInstance.setCurrentPage(0);
        setupWizardInstance.wizardDialog.show();
    }

    private void initComponents()
    {
        wizardDialog.nextButton.addActionListener(this);
        wizardDialog.backButton.addActionListener(this);
        wizardDialog.cancelButton.addActionListener(this);

        PropertiesDepot.loadProperties();
    }

    public void actionPerformed(ActionEvent evt)
    {
        try{
            console.logEntry();
            if(evt.getActionCommand().equals(WizardDialog.NEXT_COMMAND))
            {
                processNext();
            }
            if(evt.getActionCommand().equals(WizardDialog.FINISH_COMMAND))
            {
                processFinish();
            }

            else if(evt.getActionCommand().equals(WizardDialog.BACK_COMMAND))
            {
                processBack();
            }
            else if(evt.getActionCommand().equals(WizardDialog.CANCEL_COMMAND))
            {
                processCancel();
            }
        }
        finally
        {
            console.logExit();
        }

    }

    void setCurrentPage(int index)
    {
        try{
            console.logEntry();
            if(index < 0 | index > wizardPages.size() - 1)
                return;

            //validate content of current form if we're to move ahead.
            if(index >= currentPage)
                try {
                    ( (SetupWizardPage) wizardPages.get(currentPage)).
                        validateContent();
                }
                catch (IllegalArgumentException ex) {
                    console.showMsg("Error!", ex.getMessage());
                    return;
                }


            int prevPage = currentPage;
            currentPage = index;

            if(index == 0)
            {
                wizardDialog.backButton.setEnabled(false);
            }
            else
            {
                wizardDialog.backButton.setEnabled(true);
            }

            if(index == wizardPages.size() -1)
            {
                wizardDialog.nextButton.setText("Finish");
                wizardDialog.nextButton.setMnemonic('F');
                wizardDialog.nextButton.setActionCommand(WizardDialog.FINISH_COMMAND);
            }
            else
            {
                wizardDialog.nextButton.setText("Next >");
                wizardDialog.nextButton.setMnemonic('N');
                wizardDialog.nextButton.setActionCommand(WizardDialog.NEXT_COMMAND);
            }

            String pageTitle = ((JComponent)wizardPages.get(index)).getName()
                                  + " - page " + (int)(currentPage + 1) + "/" + wizardPages.size();
            wizardDialog.setTitle(WizardDialog.DIALOG_TITLE
                                  + " - " + pageTitle
                                  );
            wizardDialog.pageTitle.setText(pageTitle);

            wizardDialog.wizardContentPane.setViewportView((JComponent)wizardPages.get(index));
            wizardDialog.wizardContentPane.updateUI();

            pProperties = ((SetupWizardPage)wizardPages.get(prevPage)).getPageProperties();
            if(pProperties == null)
               pProperties = new WizardPropertySet();

            ((SetupWizardPage)wizardPages.get(currentPage)).setPageProperties(pProperties);

        }
        finally
        {
            console.logExit();
        }


    }

    void processBack()
    {
        setCurrentPage(currentPage - 1);

    }

    void processNext()
    {
        setCurrentPage(currentPage + 1);
    }

    void processFinish()
    {
        wizardDialog.dispose();
        if(pProperties != null)
        {
            for(int i = 0; i < pProperties.getPropertyCount(); i++)
            {
                WizardProperty prop = pProperties.getPropertyAt(i);
                if(prop != null)
                {
                    PropertiesDepot.setProperty(prop.propertyName, prop.propertyValue);
                }
            }
            PropertiesDepot.storeProperties();
        }
    }


    void processCancel()
    {
        wizardDialog.dispose();
    }

    public static void main(String args[])
    {
        WizardDialog.initLookAndFeel();
        new SetupWizard().start();
    }
}

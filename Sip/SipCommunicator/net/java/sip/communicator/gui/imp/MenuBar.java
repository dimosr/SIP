package net.java.sip.communicator.gui.imp;

import javax.swing.JMenuBar;
import javax.swing.*;
import java.awt.event.*;

/**
 * <p>Title: SIP Communicator</p>
 * <p>Description: A SIP UA</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Network Research Team, Louis Pasteur University</p>
 * @author Emil Ivov
 * @version 1.0
 */

public class MenuBar extends JMenuBar
{
    private JMenu menu         = new JMenu("Menu");
    private JMenu settingsMenu = new JMenu("Settings");
    private JMenu helpMenu     = new JMenu("Help");

    public MenuBar()
    {
        menu.setMnemonic('M');
        settingsMenu.setMnemonic('S');
        helpMenu.setMnemonic('H');

        menu.add(settingsMenu);
        menu.add(helpMenu);

        add(menu);

        setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    }

    /**
     * Creates a JMenuItem using the specified <code>action</code> sets X as
     * its mnemonic character and adds it together with a separator to the main
     * menu.
     * @param action the exit action
     */
    public void addExitAction(Action action)
    {
        menu.addSeparator();

        JMenuItem menuItem = new JMenuItem(action);
        menuItem.setMnemonic('X');

        menu.add(menuItem);
    }

    /**
     * Creates a JMenuItem using the specified <code>action</code> sets C as
     * its mnemonic character and adds it to the settings menu.
     * @param action the config action
     */
    public void addConfigAction(Action action)
    {
        JMenuItem config = new JMenuItem(action);
        config.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        config.setMnemonic('C');

        settingsMenu.add(config);
    }








    /**
     * Creates a JMenuItem using the specified <code>action</code> sets M as
     * its mnemonic character and adds it to the settings menu.
     * @param action the configMedia action
     */
    public void addConfigMediaAction(Action action)
    {
        JMenuItem mItem = new JMenuItem(action);
        mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
            KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK));
        mItem.setMnemonic('M');
        settingsMenu.add(mItem);
    }

    /**
     * Creates a JMenuItem using the specified <code>action</code> sets M as
     * its mnemonic character and adds it to the settings menu.
     * @param action the configMedia action
     */
    public void addSetupWizardAction(Action action)
    {
        JMenuItem mItem = new JMenuItem(action);
        mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
            KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK));
        mItem.setMnemonic('S');
        settingsMenu.addSeparator();
        settingsMenu.add(mItem);
    }

    /**
     * Creates a JMenuItem using the specified <code>action</code> sets A as
     * its mnemonic character and adds it to the settings menu.
     * @param action the about action
     */
    public void addAboutAction(Action action)
    {
        JMenuItem mItem = new JMenuItem(action);
        mItem.setMnemonic('A');
        helpMenu.addSeparator();
        helpMenu.add(mItem);
    }


}

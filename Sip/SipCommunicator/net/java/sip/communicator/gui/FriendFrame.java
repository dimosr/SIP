/*
 * 
 * 	Raptis Dimos - Dimitrios (dimosrap@yahoo.gr) - 03109770
 *  Lazos Philippos (plazos@gmail.com) - 03109082
 * 	Omada 29
 * 
 */

package net.java.sip.communicator.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;

public class FriendFrame extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final Action action = new SwingAction_3();
	private boolean canceled;
	private final Action action_1 = new SwingAction_4();
	private JRadioButton toUnblock = null;
	private JRadioButton toBlock = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			FriendFrame dialog = new FriendFrame();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public FriendFrame() {
		setModal(true);
		canceled = true;
		setTitle("Add or Remove Friends");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.SOUTH);
			{
				toBlock = new JRadioButton("Friend");
				buttonGroup.add(toBlock);
				toBlock.setMnemonic('B');
				panel.add(toBlock);
			}
			{
				toUnblock = new JRadioButton("UnFriend");
				buttonGroup.add(toUnblock);
				toUnblock.setMnemonic('B');
				panel.add(toUnblock);
			}
		}
		{
			JLabel lblUsername = new JLabel("Username:");
			contentPanel.add(lblUsername, BorderLayout.WEST);
		}
		{
			textField = new JTextField();
			contentPanel.add(textField, BorderLayout.CENTER);
			textField.setColumns(20);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setAction(action_1);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setAction(action);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		pack();
	}
	
    public void showBlockFrame()
    {
        setVisible(true);
    }

	private class SwingAction_3 extends AbstractAction {
		public SwingAction_3() {
			putValue(NAME, "Cancel");
			putValue(SHORT_DESCRIPTION, "Cancel");
		}
		public void actionPerformed(ActionEvent e) {
			canceled = true;
			setVisible(false);
		}
	}
	private class SwingAction_4 extends AbstractAction {
		public SwingAction_4() {
			putValue(NAME, "OK");
			putValue(SHORT_DESCRIPTION, "Block or Unblock these users!");
		}
		public void actionPerformed(ActionEvent e) {
			canceled = false;
			setVisible(false);
		}
	}
	
	public boolean canceled()
	{
		return canceled;
	}
	
	public String getUser()
	{
		return textField.getText();
	}
	
	public boolean block()
	{
		return toBlock.isSelected();
	}
}

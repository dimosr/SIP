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
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;

public class ForwardFrame extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField forwardTo;
	public boolean canceled;
	private final Action action = new SwingAction();
	private final Action action_1 = new SwingAction_1();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ForwardFrame dialog = new ForwardFrame();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ForwardFrame() {
		setModal(true);
		canceled = true;
		setTitle("Forward Calls\r\n");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JLabel lblNewLabel = new JLabel("Enter username to forward to (leave blank to disable forwarding)");
			contentPanel.add(lblNewLabel);
		}
		{
			forwardTo = new JTextField();
			contentPanel.add(forwardTo);
			forwardTo.setColumns(15);
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
	
	public String getForwardUser()
	{
		return forwardTo.getText();
	}
	
    public void showForwardFrame()
    {
        setVisible(true);
    }

	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "Cancel");
			putValue(SHORT_DESCRIPTION, "Closes the window");
		}
		public void actionPerformed(ActionEvent e) {
			canceled = true;
			setVisible(false);
		}
	}
	private class SwingAction_1 extends AbstractAction {
		public SwingAction_1() {
			putValue(NAME, "OK");
			putValue(SHORT_DESCRIPTION, "Forwards");
		}
		public void actionPerformed(ActionEvent e) {
			canceled = false;
			setVisible(false);
		}
	}
}

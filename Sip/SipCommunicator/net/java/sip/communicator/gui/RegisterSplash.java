/*
 * 
 * 	Raptis Dimos - Dimitrios (dimosrap@yahoo.gr) - 03109770
 *  Lazos Philippos (plazos@gmail.com) - 03109082
 * 	Omada 29
 * 
 */

package net.java.sip.communicator.gui;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JTextField;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JPasswordField;
import javax.swing.JButton;

import net.java.sip.communicator.common.Console;
import net.java.sip.communicator.sip.CommunicationsException;
import net.java.sip.communicator.sip.SipManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class RegisterSplash extends JDialog {
	boolean done;
	String username;
	String password;
	String address;
	String email;
	private JTextField usernameTextField;
	private JPasswordField passwordTextField;
	private JTextField emailTextField;
	private JTextField addressTextField;
	private SipManager sipManager = null;
    private static final Console console =
            Console.getConsole(RegisterSplash.class);
	
	public RegisterSplash(Frame parent, boolean modal, SipManager sM) {
		super(parent,modal);
		sipManager = sM;
		initComponents();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void initComponents()
	{
		setResizable(false);
		setTitle("Register");
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JLabel lblPleaseEnterYour = new JLabel("Please enter your registration information:");
		lblPleaseEnterYour.setBorder(new EmptyBorder(10, 10, 10, 10));
		lblPleaseEnterYour.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblPleaseEnterYour, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		
		JLabel lblUsername = new JLabel("Username:");
		
		usernameTextField = new JTextField();
		usernameTextField.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password:");
		
		passwordTextField = new JPasswordField();
		
		JLabel lblEmail = new JLabel("Email:");
		
		emailTextField = new JTextField();
		emailTextField.setColumns(10);
		
		JLabel lblAddress = new JLabel("Address:");
		
		addressTextField = new JTextField();
		addressTextField.setColumns(10);
		
		JButton btnRegister = new JButton("Register");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				username = new String(usernameTextField.getText());
				password = new String(passwordTextField.getPassword());
				email = new String(emailTextField.getText());
				address = new String(addressTextField.getText());
				done = true;
				
				try {
					sipManager.firstTimeRegister(username, password, email, address);
					sipManager.resetHeader();
				} catch (CommunicationsException ex) {
	                console.error("Failed to firstTimeRegister");
					ex.printStackTrace();
				}
				
				setVisible(false);
				dispose();
			}
		});
		
		JButton Cancel = new JButton("Cancel");
		Cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				done = false;
				setVisible(false);
				dispose();
			}
		});
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(59)
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblUsername)
						.addComponent(lblPassword)
						.addComponent(lblEmail)
						.addComponent(lblAddress))
					.addGap(20)
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(addressTextField, GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
						.addComponent(emailTextField, GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
						.addComponent(passwordTextField, GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
						.addComponent(usernameTextField, GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
					.addContainerGap(85, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(146)
					.addComponent(btnRegister)
					.addGap(18)
					.addComponent(Cancel)
					.addContainerGap(142, Short.MAX_VALUE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(58)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblUsername)
						.addComponent(usernameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPassword)
						.addComponent(passwordTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblEmail)
						.addComponent(emailTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAddress)
						.addComponent(addressTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(Cancel)
						.addComponent(btnRegister))
					.addGap(30))
		);
		panel.setLayout(gl_panel);
	}

}

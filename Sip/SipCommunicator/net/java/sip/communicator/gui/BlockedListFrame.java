package net.java.sip.communicator.gui;

import java.awt.EventQueue;
import java.awt.ScrollPane;

import javax.swing.JDialog;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JList;

public class BlockedListFrame extends JDialog {

	private JList<String> list = new JList<String>();
	private JScrollPane scrollPane = new JScrollPane(list);
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FriendsListFrame dialog = new FriendsListFrame();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.repaint();
					dialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the dialog.
	 */
	public BlockedListFrame() {
		SetupGUI();
		//ShowList();
		//clearBlockedUsers();
	}
	
	public void SetupGUI()
	{
		setPreferredSize(new Dimension(200, 200));
		setTitle("Blocked Users");
		setBounds(100, 100, 326, 274);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("Users Blocked:");
		getContentPane().add(lblNewLabel, BorderLayout.NORTH);

		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		//this.setVisible(true);  
	}
	
	
	public void ShowList()
	{	
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		
		listModel.addElement("Maph4cker");
		listModel.addElement("Plazos");
		
		list.setModel(listModel);
		list.setLayoutOrientation(JList.VERTICAL);
	    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    list.setVisibleRowCount(20);
		
		this.repaint();
		this.revalidate();
	}
	
	public void clearBlockedUsers()
	{
		list.setListData(new String[0]);
	}
	
	public void updateBlockedUsers(String b)
	{
		clearBlockedUsers();
		//String users[] = b.split("\n");
		//DefaultListModel<String> listModel = new DefaultListModel<String>();
		//String users[] = { "Maph4cker" , "Plazos" };
		//listModel.addElement(users[0]);
		//listModel.addElement(users[1]);
		//list.setModel(listModel);
		
		String users[] = b.split("\n");
		list.setListData(users);
	}

}

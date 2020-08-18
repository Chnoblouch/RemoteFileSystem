package app.ui.files;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import app.client.Client;
import app.ui.ClientUI;

public class TextEditorUI
extends JPanel {
	
	private JTextArea textArea;	
	private JButton close;
	private JButton save;
	
	public TextEditorUI(String fileName, String text, Client client, ClientUI ui, int index) {
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.weightx = 0.5;
		constraints.weighty = 0.01;
		constraints.insets.right = 10;
		
		close = new JButton("Close");
		close.addActionListener((event) -> ui.closeTab(index));
		add(close, constraints);
		
		constraints.insets.right = 0;
		constraints.insets.left = 10;
		constraints.gridx = 1;
		
		save = new JButton("Save");
		save.addActionListener((event) -> {
			client.getSendingMaster().writeFileContent(fileName, textArea.getText().getBytes());
		});
		add(save, constraints);
		
		constraints.insets.left = 0;
		constraints.insets.top = 10;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 0.99;
		constraints.gridwidth = 2;	
		
		textArea = new JTextArea(text);
		textArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		textArea.setFont(new Font("Lucida Console", Font.PLAIN, 11));
		add(new JScrollPane(textArea), constraints);
		
		textArea.requestFocus();
	}

}

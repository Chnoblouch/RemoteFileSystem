package app.ui.files;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import app.client.Client;
import app.ui.ClientUI;

public class SystemProcessUI
extends JPanel {
		
	private JTextArea input;
	private JTextArea output;
	
	private JButton stop;
	
	public SystemProcessUI(long pid, Client client, ClientUI ui, int index) {		
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.weightx = 1.0;
		constraints.weighty = 0.01;
		constraints.gridwidth = 2;
		
		stop = new JButton("Stop Process");
		stop.addActionListener((event) -> {
			client.getSendingMaster().sendSystemProcessStop(pid);
			ui.closeTab(index);
		});
		add(stop, constraints);
		
		constraints.gridwidth = 1;
		constraints.insets.right = 10;
		constraints.insets.left = 0;
		constraints.insets.top = 10;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 0.5;
		constraints.weighty = 0.99;
		
		input = new JTextArea();
		input.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		input.setFont(new Font("Lucida Console", Font.PLAIN, 11));
		input.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					client.getSendingMaster().sendSystemProcessInput(pid, input.getText());
					input.setText(null);
					e.consume();
				}
			}
		});
		add(new JScrollPane(input), constraints);

		constraints.insets.right = 0;
		constraints.insets.left = 10;
		constraints.gridx = 1;
		
		output = new JTextArea();
		output.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		output.setFont(new Font("Lucida Console", Font.PLAIN, 11));
		output.setEditable(false);
		add(new JScrollPane(output), constraints);
		
		input.requestFocus();
	}
	
	public void output(String output, boolean error) {
		this.output.setText(this.output.getText() + output);
	}

}

package app.ui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;

import app.client.Client;
import app.client.RemoteFile;
import app.ui.files.AudioPlayerUI;
import app.ui.files.ImageDisplayUI;
import app.ui.files.SystemProcessUI;
import app.ui.files.TextEditorUI;

public class ClientUI
implements ActionListener {
	
	private Client client;
	
	private JFrame frame;
	private JTabbedPane tabbedPane;
	private JPanel panel;
		
	private JPanel actions;
	private JPanel fileSystem;
	private JPanel filePanel;
	
	private JList<RemoteFile> buttons;
	private JScrollPane scrollPane;
	
	private JButton create;
	private JButton delete;
	private JButton rename;
	private JButton move;
	private JButton copy;
	private JButton upload;
	private JButton download;
	private JButton directoryUp;
	
	private JTextField directoryPanel;
		
	private String directory;	
	private JFileChooser fileChooser;
	
	private HashMap<Long, SystemProcessUI> systemProcessUIs = new HashMap<>();
	
	public ClientUI(Client client) {
		this.client = client;
				
		frame = new JFrame("Remote File System Client");
		frame.setSize(1000, 500);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
		
		tabbedPane = new JTabbedPane();
		frame.setContentPane(tabbedPane);
		
		panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2, 0, 0));
		tabbedPane.addTab("File Explorer", panel);
		
		actions = new JPanel();
		actions.setLayout(new GridLayout(9, 1, 0, 10));
		actions.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
		panel.add(actions);
		
		fileSystem = new JPanel();
		fileSystem.setBorder(BorderFactory.createEmptyBorder(50, 10, 10, 10));
		fileSystem.setLayout(null);
		panel.add(fileSystem);
		
		filePanel = new JPanel();
		filePanel.setBounds(0, 50, 400, 350);
		filePanel.setLayout(new GridLayout(1, 1));
		fileSystem.add(filePanel);
		
		create = new JButton("Create File");
		create.addActionListener(this);
		actions.add(create);
		
		delete = new JButton("Delete File");
		delete.addActionListener(this);
		actions.add(delete);
		
		rename = new JButton("Rename File");
		rename.addActionListener(this);
		actions.add(rename);
		
		move = new JButton("Move File");
		move.addActionListener(this);
		actions.add(move);
		
		copy = new JButton("Copy File");
		copy.addActionListener(this);
		actions.add(copy);
				
		upload = new JButton("Upload File");
		upload.addActionListener(this);
		actions.add(upload);
		
		download = new JButton("Download File");
		download.addActionListener(this);
		actions.add(download);
		
		directoryUp = new JButton("Directory Up");
		directoryUp.addActionListener(this);
		actions.add(directoryUp);
		
		directoryPanel = new JTextField("Directory");
		directoryPanel.setBounds(0, 10, 400, 20);
		directoryPanel.setBorder(new CompoundBorder(
				BorderFactory.createLineBorder(Color.BLACK, 1),
				BorderFactory.createEmptyBorder(0, 5, 0, 0)));
		directoryPanel.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) updateDirectory(directoryPanel.getText());
			}
		});
		
		fileSystem.add(directoryPanel);
		
		frame.validate();
		frame.repaint();
		
		fileChooser = new JFileChooser();
	}
	
	public void updateDirectory(String directory) {	
		directory = directory.replace("\\", "/");
		if(directory.endsWith("/")) directory = directory.substring(directory.length() - 1);
		
		this.directory = directory;
		directoryPanel.setText(directory);
		
		client.getSendingMaster().requestFiles(directory);
	}
	
	public void updateDirectory() {
		client.getSendingMaster().requestFiles(directory);
	}

	public void updateFileSystem(RemoteFile[] files) {
		if(scrollPane != null) filePanel.remove(scrollPane);
				
		buttons = new JList<RemoteFile>(files);
		buttons.setCellRenderer(new FileListCellRenderer());
		buttons.setFixedCellHeight(20);
//		buttons.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		buttons.setBounds(0, 0, 400, 400);
		buttons.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RemoteFile selected = files[buttons.locationToIndex(e.getPoint())];
				if(selected != null && e.getClickCount() == 2) {
					runFile(selected.getName());
					e.consume();
				}
			}
		});
		buttons.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					for(String f : selectedFiles()) runFile(f);
				} else if(e.getKeyCode() == KeyEvent.VK_DELETE) {
					for(String f : selectedFiles()) client.getSendingMaster().deleteFile(directory + "/" + f);	
					client.getSendingMaster().requestFiles(directory);
				}
			}
		});
		
		scrollPane = new JScrollPane(buttons);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		filePanel.add(scrollPane);
				
		frame.validate();
		frame.repaint();
		
		buttons.setSelectedIndex(0);
		buttons.requestFocus();
	}
	
	public void openTextEditor(String fileName, String text) {		
		TextEditorUI ui = new TextEditorUI(fileName, text, client, this, tabbedPane.getTabCount());
		fileName = fileName.replace("\\", "/");
		tabbedPane.addTab(fileName.substring(fileName.lastIndexOf("/") + 1), ui);
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
		
		frame.validate();
		frame.repaint();
	}
	
	public void openImageDisplay(String fileName, BufferedImage image) {
		ImageDisplayUI ui = new ImageDisplayUI(image, client, this, tabbedPane.getTabCount());
		fileName = fileName.replace("\\", "/");
		tabbedPane.addTab(fileName.substring(fileName.lastIndexOf("/") + 1), ui);
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
		
		frame.validate();
		frame.repaint();
	}

	public void openAudioPlayerUI(String fileName, AudioInputStream inputStream) {
		AudioPlayerUI ui = new AudioPlayerUI(inputStream, client, this, tabbedPane.getTabCount());
		fileName = fileName.replace("\\", "/");
		tabbedPane.addTab(fileName.substring(fileName.lastIndexOf("/") + 1), ui);
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
		
		frame.validate();
		frame.repaint();
	}
	
	public void openSystemProcessUI(long pid) {
		SystemProcessUI ui = new SystemProcessUI(pid, client, this, tabbedPane.getTabCount());
		tabbedPane.addTab("Process (pid: " + pid + ")", ui);
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
		
		systemProcessUIs.put(pid, ui);
		
		frame.validate();
		frame.repaint();
	}
	
	public void systemProcessOutput(long pid, String output, boolean error) {
		systemProcessUIs.get(pid).output(output, error);
	}
	
	public void closeTab(int index) {
		tabbedPane.removeTabAt(index);
		buttons.requestFocus();
	}
	
	private String selectedFile() {
		return buttons.getSelectedValue().getName();
	}
	
	private String[] selectedFiles() {
		String[] files = new String[buttons.getSelectedValuesList().size()];
		for(int i = 0; i < buttons.getSelectedValuesList().size(); i++) {
			files[i] = buttons.getSelectedValuesList().get(i).getName();
		}
		
		return files;
	}
	
	private void runFile(String file) {
		if(!file.contains(".")) updateDirectory(directory + "/" + file);
		else client.getSendingMaster().runFile(directory + "/" + file);
	}
	
	private int directorySelection() {
		Object[] options = new Object[]{"File", "Directory"};
		
		int o = JOptionPane.showOptionDialog(null,
											"What do you want to upload?",
											"Upload",
											JOptionPane.DEFAULT_OPTION,
											JOptionPane.QUESTION_MESSAGE,
											null,
											options,
											options[0]);
				
		return o;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == create) {			
			String name = JOptionPane.showInputDialog("File Name");
			if(name == null) return;
			
			client.getSendingMaster().createFile(directory + "/" + name);
			updateDirectory();
		} else if(e.getSource() == delete) {
			for(String f : selectedFiles()) client.getSendingMaster().deleteFile(directory + "/" + f);
			updateDirectory();
		} else if(e.getSource() == rename) {
			String f = selectedFile();
			if(f == null) return;
			
			String name = JOptionPane.showInputDialog("New File Name");
			if(name == null) return;
			
			client.getSendingMaster().moveFile(directory + "/" + f, directory + "/" + name);
			updateDirectory();
		} else if(e.getSource() == move) {
			String f = selectedFile();
			if(f == null || !f.contains(".")) return;
			
			String newDirectory = JOptionPane.showInputDialog("New Directory");
			if(newDirectory == null) return;
			
			newDirectory = newDirectory.replace("\\", "/");
			if(!newDirectory.endsWith("/")) newDirectory += "/";
			if(newDirectory.startsWith("/")) newDirectory = newDirectory.substring(1);
			
			client.getSendingMaster().moveFile(directory + "/" + f, directory + "/" + newDirectory + f);
			updateDirectory();
		} else if(e.getSource() == copy) {
			client.getSendingMaster().copyFile(directory + "/" + selectedFile());
			updateDirectory();
		} else if(e.getSource() == upload) {
			try {
				int dirSelection = directorySelection();
				if(dirSelection == -1) return;
				
				boolean isDirectory = dirSelection == 1;
				int mode = isDirectory ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY;
				
				fileChooser.setFileSelectionMode(mode);
				fileChooser.showOpenDialog(null);
				File f = fileChooser.getSelectedFile();
				if(f == null) return;
				
				String remotePath = directory + "/" + f.getName();
				client.getSendingMaster().uploadFile(f.getAbsolutePath(), remotePath, isDirectory);
				updateDirectory();
			} catch(IOException e1) {
				e1.printStackTrace();
			}
		} else if(e.getSource() == download) {
			String name = selectedFile();
			if(name == null) return;
			
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.showOpenDialog(null);
			File f = fileChooser.getSelectedFile();
			if(f == null) return;
			
			String localPath = f.getAbsolutePath().replace("\\", "/") + "/" + name;
			String remotePath = directory + "/" + name;
			client.getSendingMaster().requestFileDownload(remotePath, localPath, !name.contains("."));
		} else if(e.getSource() == directoryUp) {
			int i = directory.replace("\\", "/").lastIndexOf("/");
			if(i == -1) return;
			
			String d = directory.substring(0, i);
			updateDirectory(d);
		}
	}

}

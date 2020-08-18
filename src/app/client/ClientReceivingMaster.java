package app.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;

import app.MessageTypes;

public class ClientReceivingMaster {
	
	private Client client;
	
	public ClientReceivingMaster(Client client) {
		this.client = client;
	}
	
	public void receive(int type, byte[] bytes) throws IOException, UnsupportedAudioFileException {
		switch(type) {
			case MessageTypes.STARTING_DIRECTORY: startingDirectory(bytes); break;
			case MessageTypes.FILES: files(bytes); break;
			case MessageTypes.UPLOAD_FILE_DATA: client.getFileMaster().receive(bytes, true); break;
			case MessageTypes.FILE_CONTENT: fileContent(bytes); break;
			case MessageTypes.SYSTEM_PROCESS_STARTED: systemProcessStarted(bytes); break;
			case MessageTypes.SYSTEM_PROCESS_OUTPUT: systemProcessOutput(bytes); break;
		}
	}
	
	private void startingDirectory(byte[] bytes) {
		String directory = new String(bytes);
		System.out.println(directory);
		
		if(client.getUI() != null) client.getUI().updateDirectory(directory);
	}

	private void files(byte[] bytes) throws IOException {
		String[] elements = new String(bytes).split(":");
		int fileCount = elements.length / 2;
		RemoteFile[] files = new RemoteFile[fileCount];
		
		for(int i = 0; i < fileCount; i++) {				
			byte[] iconData = Base64.getDecoder().decode(elements[i * 2 + 1]);
			BufferedImage icon = ImageIO.read(new ByteArrayInputStream(iconData));
			
			files[i] = new RemoteFile(elements[i * 2], new ImageIcon(icon));
		}
		
		if(client.getUI() != null) client.getUI().updateFileSystem(files);
	}
	
	private void fileContent(byte[] bytes) throws IOException, UnsupportedAudioFileException {
		client.getFileMaster().receive(bytes, false);
		if(!client.getFileMaster().isReceivingFile()) {			
			String p = client.getFileMaster().getFilePath();
			
			if(p.endsWith(".jpg") || p.endsWith(".png") || p.endsWith(".gif")) {
				byte[] imgBytes = client.getFileMaster().getByteOutputStream().toByteArray();
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBytes));
				client.getUI().openImageDisplay(client.getFileMaster().getFilePath(), img);
			} else if(p.endsWith(".wav")) {
				byte[] audioBytes = client.getFileMaster().getByteOutputStream().toByteArray();
				AudioInputStream in = AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioBytes));
				client.getUI().openAudioPlayerUI(client.getFileMaster().getFilePath(), in);
			} else {
				byte[] textBytes = client.getFileMaster().getByteOutputStream().toByteArray();
				client.getUI().openTextEditor(client.getFileMaster().getFilePath(), new String(textBytes));
			}
		}
	}
	
	private void systemProcessStarted(byte[] bytes) {
		ByteBuffer msgBuffer = ByteBuffer.wrap(bytes);
		long pid = msgBuffer.getLong();
		
		client.getUI().openSystemProcessUI(pid);
	}
	
	private void systemProcessOutput(byte[] bytes) {
		ByteBuffer msgBuffer = ByteBuffer.wrap(bytes);
		long pid = msgBuffer.getLong(0);
		boolean error = msgBuffer.get(Long.BYTES) == 1;
		
		byte[] outBytes = new byte[bytes.length - Long.BYTES - 1];
		msgBuffer.position(Long.BYTES + 1);
		msgBuffer.get(outBytes);
		String output = new String(outBytes);
		
		client.getUI().systemProcessOutput(pid, output, error);
	}
}

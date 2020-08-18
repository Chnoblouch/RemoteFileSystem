package app.client;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;

import app.Application;
import app.ui.ClientUI;
import app.utils.FileNetworkingMaster;
import app.utils.MessageReader;

public class Client
extends Thread {
	
	private Socket socket;
	
	private MessageReader reader;
	private ClientSendingMaster sendingMaster;
	private ClientReceivingMaster receivingMaster;
	private FileNetworkingMaster fileMaster;
	
	private ClientUI ui;
		
	public void start(String address) {
		try {
			System.out.println("socket started");
			socket = new Socket(Inet4Address.getByName(address), Application.PORT);
					
			reader = new MessageReader(socket.getInputStream(), (buffer, length) -> {
				try {
					receive(buffer, length);
				} catch (IOException | UnsupportedAudioFileException e) {
					e.printStackTrace();
				}
			});
			
			sendingMaster = new ClientSendingMaster(this);
			receivingMaster = new ClientReceivingMaster(this);
			fileMaster = new FileNetworkingMaster();
			
			start();
		} catch(IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					null,
					"Couldn't connect to server!",
					"Connection error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void setUI(ClientUI ui) {
		this.ui = ui;
	}
	
	public void send(byte[] bytes) {
		try {
			socket.getOutputStream().write(bytes);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void send(byte type, byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + 1 + bytes.length);
		buffer.putInt(1 + bytes.length);
		buffer.put(type);
		buffer.put(bytes);
		
		send(buffer.array());
	}
	
	private void receive(ByteBuffer buffer, int length) throws IOException, UnsupportedAudioFileException {
		byte type = buffer.get(0);
		
		byte[] bytes = new byte[length - 1];
		buffer.position(1);
		buffer.get(bytes);
		
		receivingMaster.receive(type, bytes);
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public ClientSendingMaster getSendingMaster() {
		return sendingMaster;
	}
	
	public ClientReceivingMaster getReceivingMaster() {
		return receivingMaster;
	}
	
	public FileNetworkingMaster getFileMaster() {
		return fileMaster;
	}
	
	public ClientUI getUI() {
		return ui;
	}
	
	@Override
	public void run() {				
		while(!socket.isClosed()) reader.read();
	}

}

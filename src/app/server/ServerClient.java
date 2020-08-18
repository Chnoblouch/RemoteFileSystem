package app.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import app.utils.FileNetworkingMaster;
import app.utils.MessageReader;

public class ServerClient 
extends Thread {
	
	private Socket socket;
	private Server server;
		
	private MessageReader reader;
	private ServerSendingMaster sendingMaster;
	private ServerReceivingMaster receivingMaster;
	private FileNetworkingMaster fileMaster;
					
	public ServerClient(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
		
		try {
			reader = new MessageReader(socket.getInputStream(), (buffer, length) -> {
				try {
					receive(buffer, length);
				} catch(Exception e) {
					e.printStackTrace();
				}
			});
			
			sendingMaster = new ServerSendingMaster(this);
			receivingMaster = new ServerReceivingMaster(this);
			fileMaster = new FileNetworkingMaster();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		sendingMaster.sendStartingDirectory();
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
	
	private void receive(ByteBuffer buffer, int length) throws IOException {
		byte type = buffer.get(0);
				
		byte[] bytes = new byte[length - 1];
		buffer.position(1);
		buffer.get(bytes);
		
		receivingMaster.receive(type, bytes);
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public ServerSendingMaster getSendingMaster() {
		return sendingMaster;
	}
	
	public ServerReceivingMaster getReceivingMaster() {
		return receivingMaster;
	}
	
	public FileNetworkingMaster getFileMaster() {
		return fileMaster;
	}
	
	public Server getServer() {
		return server;
	}
	
	@Override
	public void run() {
		while(!socket.isClosed()) reader.read();
	}

}

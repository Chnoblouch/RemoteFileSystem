package app.client;

import java.io.IOException;
import java.nio.ByteBuffer;

import app.MessageTypes;

public class ClientSendingMaster {
	
	private Client client;
	
	public ClientSendingMaster(Client client) {
		this.client = client;
	}
	
	public void requestFiles(String directory) {
		System.out.println("requesting files...");
		
		directory = directory.replace("\\", "/");
				
		byte[] bytes = directory.getBytes();
		client.send(MessageTypes.REQUEST_FILES, bytes);
	}
	
	public void createFile(String name) {
		System.out.println("creating file \"" + name + "\"...");
		
		byte[] bytes = name.getBytes();
		client.send(MessageTypes.CREATE_FILE, bytes);
	}
	
	public void deleteFile(String name) {
		System.out.println("deleting file \"" + name + "\"...");
		
		byte[] bytes = name.getBytes();
		client.send(MessageTypes.DELETE_FILE, bytes);
	}
	
	public void moveFile(String name, String newName) {
		System.out.println("moving file \"" + name + "\"to \"" + newName + "\"...");
		
		byte[] nameBytes = name.getBytes();
		byte[] newNameBytes = newName.getBytes();
		
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + nameBytes.length + newNameBytes.length);
		buffer.putInt(nameBytes.length);
		buffer.put(nameBytes);
		buffer.put(newNameBytes);
		
		client.send(MessageTypes.MOVE_FILE, buffer.array());
	}
	
	public void copyFile(String path) {
		client.send(MessageTypes.COPY_FILE, path.getBytes());
	}
	
	public void uploadFile(String localPath, String remotePath, boolean isDirectory) throws IOException {
		System.out.println("uploading file \"" + localPath + "\"...");
				
		byte[][] messages = client.getFileMaster().send(localPath, remotePath, isDirectory);
		for(byte[] m : messages) client.send(MessageTypes.UPLOAD_FILE_DATA, m);
	}
	
	public void requestFileDownload(String remotePath, String localPath, boolean isDirectory) {		
		if(isDirectory) {
			localPath = localPath.replace("\\", "/");
			if(localPath.endsWith("/")) localPath = localPath.substring(0, localPath.length() - 1);
			localPath += ".zip";
		}
		
		System.out.println("downloading file \"" + remotePath + "\"...");
		
		byte[] bytes = (remotePath + "," + localPath + "," + isDirectory).getBytes();
		client.send(MessageTypes.REQUEST_FILE_DATA, bytes);
	}
	
	public void runFile(String name) {
		System.out.println("running file \"" + name + "\"...");
		
		byte[] bytes = name.getBytes();
		client.send(MessageTypes.RUN_FILE, bytes);
	}
	
	public void writeFileContent(String name, byte[] data) {
		System.out.println("writing to file \"" + name + "\"...");
		
		byte[][] messages = client.getFileMaster().send(data, name);
		for(byte[] m : messages) client.send(MessageTypes.WRITE_FILE_DATA, m);
	}
	
	public void sendSystemProcessInput(long pid, String input) {
		byte[] inputBytes = input.getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + inputBytes.length);
		buffer.putLong(pid);
		buffer.put(inputBytes);
		
		client.send(MessageTypes.SYSTEM_PROCESS_INPUT, buffer.array());
	}
	
	public void sendSystemProcessStop(long pid) {
		byte[] bytes = ByteBuffer.allocate(Long.BYTES).putLong(pid).array();
		client.send(MessageTypes.SYSTEM_PROCESS_STOP, bytes);
	}

}

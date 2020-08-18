package app.server;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;

import app.MessageTypes;

public class ServerSendingMaster {
	
	private ServerClient client;
	
	public ServerSendingMaster(ServerClient client) {
		this.client = client;
	}
	
	public void sendStartingDirectory() {
		File file = new File("");
		String path = file.getAbsolutePath().replace("\\", "/");
		byte[] fileBytes = path.getBytes();
		
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + 1 + fileBytes.length);
		buffer.putInt(1 + fileBytes.length);
		buffer.put(MessageTypes.STARTING_DIRECTORY);
		buffer.put(fileBytes);
		
		client.send(buffer.array());
	}
	
	public void sendFiles(String directory) throws IOException {
		File file = new File(directory);
		if(!file.exists()) {
			client.send(MessageTypes.FILES, new byte[0]);
			return;
		}
		
		StringBuilder builder = new StringBuilder();
		for(File f : file.listFiles()) {
			if(!f.isHidden()) {
				String base64 = Base64.getEncoder().encodeToString(ServerFileUtils.iconOf(f));
				builder.append(f.getName() + ":" + base64 + ":");
			}
		}
		
		byte[] bytes = builder.toString().getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + 1 + bytes.length);
		buffer.putInt(1 + bytes.length);
		buffer.put(MessageTypes.FILES);
		buffer.put(bytes);
		
		client.send(buffer.array());
	}
	
	public void sendFileDownload(String localName, String remoteName, boolean isDirectory) throws IOException {		
		byte[][] messages = client.getFileMaster().send(localName, remoteName, isDirectory);
		for(byte[] m : messages) client.send(MessageTypes.UPLOAD_FILE_DATA, m);
	}
	
	public void sendFileContent(String name) throws IOException {		
		byte[][] messages = client.getFileMaster().send(name);
		for(byte[] m : messages) client.send(MessageTypes.FILE_CONTENT, m);
	}
	
	public void sendSystemProcessOutput(long pid, boolean error, String output) {
		byte[] outputBytes = output.getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + 1 + outputBytes.length);
		buffer.putLong(pid);
		buffer.put(error ? (byte) 1 : (byte) 0);
		buffer.put(outputBytes);
		
		client.send(MessageTypes.SYSTEM_PROCESS_OUTPUT, buffer.array());
	}

}

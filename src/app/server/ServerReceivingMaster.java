package app.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import app.MessageTypes;

public class ServerReceivingMaster {
	
	private ServerClient client;
	
	public ServerReceivingMaster(ServerClient client) {
		this.client = client;
	}
	
	public void receive(int type, byte[] bytes) throws IOException {
		switch(type) {
			case MessageTypes.REQUEST_FILES: requestFiles(bytes); break;
			case MessageTypes.CREATE_FILE: ServerFileUtils.createFile(bytes); break;
			case MessageTypes.DELETE_FILE: ServerFileUtils.deleteFile(bytes); break;
			case MessageTypes.MOVE_FILE: ServerFileUtils.moveFile(bytes); break;
			case MessageTypes.COPY_FILE: ServerFileUtils.copyFile(bytes); break;
			case MessageTypes.UPLOAD_FILE_DATA: uploadFileData(bytes); break;
			case MessageTypes.REQUEST_FILE_DATA: requestFileData(bytes); break;
			case MessageTypes.RUN_FILE: runFile(bytes); break;
			case MessageTypes.WRITE_FILE_DATA: writeFileData(bytes); break;
			case MessageTypes.SYSTEM_PROCESS_INPUT: systemProcessInput(bytes); break;
			case MessageTypes.SYSTEM_PROCESS_STOP: systemProcessStop(bytes); break;
		}
	}
	
	private void requestFiles(byte[] bytes) throws IOException {
		String directory = new String(bytes);
		client.getSendingMaster().sendFiles(directory);
		
		System.out.println("client requested files");
	}
	
	private void uploadFileData(byte[] bytes) {
		System.out.println("client is uploading file data...");
		client.getFileMaster().receive(bytes, true);
	}
	
	private void requestFileData(byte[] bytes) throws IOException {
		String data = new String(bytes);
		String[] split = data.split(",");		
		client.getSendingMaster().sendFileDownload(split[0], split[1], Boolean.parseBoolean(split[2]));
		
		System.out.println("client requested file download \"" + data + "\"");
	}
	
	private void runFile(byte[] bytes) throws IOException {
		String path = new String(bytes);
		if(path.endsWith(".jar")) {
			SystemProcess sp = ServerFileUtils.runFileAsProcess(path, client);
			client.getServer().addSystemProcess(sp);
			
			byte[] b = ByteBuffer.allocate(Long.BYTES).putLong(sp.getPid()).array();
			client.send(MessageTypes.SYSTEM_PROCESS_STARTED, b);
		} else client.getSendingMaster().sendFileContent(path);
								
		System.out.println("client executed file \"" + path + "\"");
	}
	
	private void writeFileData(byte[] bytes) {
		System.out.println("client is writing to file...");
		client.getFileMaster().receive(bytes, true);
	}
	
	private void systemProcessInput(byte[] bytes) throws IOException {
		ByteBuffer msgBuffer = ByteBuffer.wrap(bytes);
		long pid = msgBuffer.getLong(0);
		
		byte[] inBytes = new byte[bytes.length - Long.BYTES];
		msgBuffer.position(Long.BYTES);
		msgBuffer.get(inBytes);
		String input = new String(inBytes);
		
		SystemProcess process = client.getServer().getSystemProcess(pid);
		if(process != null) process.input(input);
	}
	
	private void systemProcessStop(byte[] bytes) throws IOException {
		ByteBuffer pidBuffer = ByteBuffer.wrap(bytes);
		long pid = pidBuffer.getLong();
		SystemProcess process = client.getServer().getSystemProcess(pid);
		if(process != null) {
			client.getServer().removeSystemProcess(process.getPid());
			process.exit();
		}
	}

}

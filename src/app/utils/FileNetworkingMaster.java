package app.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileNetworkingMaster {
		
	private FileOutputStream fileOut;
	private ByteArrayOutputStream byteOut;
	private OutputStream out;
	
	private String filePath = null;
	private int fileSize = 0;
	private int fileBytesWritten = 0;
	
	private boolean receivingFile = false;
	
	private static final int FILE_CHUNK_SIZE = 8192;
	
	public byte[][] send(InputStream in, String remoteName, boolean isDirectory) throws IOException {
		int messageCount = 1 + (int) Math.ceil((float) in.available() / (float) FILE_CHUNK_SIZE);
		byte[][] messages = new byte[messageCount][];
					
		byte[] fileInfoBytes = (remoteName + "," + in.available() + "," + isDirectory).getBytes();
		messages[0] = fileInfoBytes;
					
		int pointer = 1;
		while(in.available() > 0) {
			byte[] bytes = new byte[Math.min(in.available(), FILE_CHUNK_SIZE)];		
			in.read(bytes);
			
			messages[pointer++] = bytes;
		}
		
		in.close();
		
		return messages;
	}
	
	public byte[][] send(String localPath, String remotePath, boolean isDirectory) {
		try {
			File file = new File(localPath);
			InputStream in = null;
			
			if(!isDirectory) in = new FileInputStream(file);
			else {								
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ZipOutputStream zipOut = new ZipOutputStream(out);
				zipDirectory(file, file.getName(), zipOut);
				zipOut.close();
												
				in = new ByteArrayInputStream(out.toByteArray());
				out.close();
			}
			
			return send(in, remotePath, isDirectory);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public byte[][] send(byte[] buffer, String remotePath) {
		try {
			return send(new ByteArrayInputStream(buffer), remotePath, false);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public byte[][] send(String remotePath) {
		try {
			return send(new FileInputStream(new File(remotePath)), remotePath, false);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void prepareIncomingFile(byte[] bytes, boolean createFile) throws IOException {
		String[] info = new String(bytes).split(",");
		filePath = info[0];
		fileSize = Integer.parseInt(info[1]);
		
		if(createFile) {
			File file = new File(filePath);
			if(!file.exists()) file.createNewFile();
			
			fileOut = new FileOutputStream(file);
			out = fileOut;
		} else {
			byteOut = new ByteArrayOutputStream();
			out = byteOut;
		}
		
		if(fileSize == 0) {			
			out.close();
			out = null;
		} else receivingFile = true;
	}
	
	private void writeIncomingFile(byte[] bytes) throws IOException {
		out.write(bytes);
		fileBytesWritten += bytes.length;
		
		if(fileBytesWritten >= fileSize) {
			out.close();
			out = null;
			
			fileBytesWritten = 0;
			fileSize = 0;
			
			receivingFile = false;
		}
	}
	
	public void receive(byte[] bytes, boolean createFile) {
		try {
			if(out == null) prepareIncomingFile(bytes, createFile);
			else writeIncomingFile(bytes);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void zipDirectory(File file, String name, ZipOutputStream zipOut) throws IOException {		
		if(file.isDirectory()) {
			if(!name.endsWith("/")) name += "/";
			
			zipOut.putNextEntry(new ZipEntry(name));
			zipOut.closeEntry();
			
			for(File child : file.listFiles()) {
				if(!child.isHidden()) zipDirectory(child, name + child.getName(), zipOut);
			}
			
			return;
		}
		
		FileInputStream fileIn = new FileInputStream(file);
		ZipEntry entry = new ZipEntry(name);
		zipOut.putNextEntry(entry);
				
		byte[] buffer = new byte[2048];
		int length = 0;
		while((length = fileIn.read(buffer)) != -1) zipOut.write(buffer, 0, length);
		zipOut.closeEntry();
		fileIn.close();
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public boolean isReceivingFile() {
		return receivingFile;
	}

	public FileOutputStream getFileOutputStream() {
		return fileOut;
	}
	
	public ByteArrayOutputStream getByteOutputStream() {
		return byteOut;
	}
}

package app.server;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

public class ServerFileUtils {

	public static void createFile(byte[] buffer) throws IOException {
		String name = new String(buffer);
		File file = new File(name);
		if(name.contains(".")) file.createNewFile();
		else file.mkdir();
		
		System.out.println("client created file \"" + name + "\"");
	}
	
	public static void deleteFile(byte[] buffer) {
		String name = new String(buffer);
		File file = new File(name);
		file.delete();
					
		System.out.println("client deleted file \"" + name + "\"");
	}
	
	public static void moveFile(byte[] buffer) {
		ByteBuffer data = ByteBuffer.wrap(buffer);
		int nameLength = data.getInt(0);
					
		byte[] nameBytes = new byte[nameLength];
		byte[] newNameBytes = new byte[data.capacity() - nameLength - Integer.BYTES];
		
		data.position(Integer.BYTES);
		data.get(nameBytes);
		
		data.position(Integer.BYTES + nameLength);
		data.get(newNameBytes);
		
		String name = new String(nameBytes);
		String newName = new String(newNameBytes);
		
		File file = new File(name);
		file.renameTo(new File(newName));
					
		System.out.println("client moved file \"" + name + "\" to \"" + newName + "\"");
	}
	
	public static void copyFile(byte[] buffer) throws IOException {
		String path = new String(buffer);		
		File file = new File(path);
				
		copyFile(file, createFileCopy(file));
	}
	
	private static File createFileCopy(File file) {
		String path = file.getAbsolutePath();
		int extensionIndex = path.lastIndexOf(".");
		
		File copy;
		int copyNumber = 0;
				
		do {
			copyNumber ++;
			
			String name = extensionIndex == -1 ? path : path.substring(0, extensionIndex);
			String extension = extensionIndex == -1 ? "" : path.substring(extensionIndex);
			copy = new File(name + "_" + copyNumber + extension);
		} while(copy.exists());
		
		return copy;
	}
	
	private static void copyFile(File file, File copy) throws IOException {		
		if(!file.isDirectory()) {
			copy.createNewFile();
			
			FileInputStream in = new FileInputStream(file);
			FileOutputStream out = new FileOutputStream(copy);
			
			byte[] fileBuffer = new byte[4096];
			int bufferLength;
			
			while((bufferLength = in.read(fileBuffer, 0, fileBuffer.length)) != -1)
				out.write(fileBuffer, 0, bufferLength);
			
			in.close();
			out.close();
		} else {
			copy.mkdir();
			for(File f : file.listFiles()) {
				String name = f.getName();
				copyFile(f, new File(copy.getAbsolutePath() + "\\" + name));
			}
		}
	}
	
	public static SystemProcess runFileAsProcess(String path, ServerClient client) throws IOException {
		path = path.replace("\\", "/");
		String dir = path.substring(0, path.lastIndexOf("/"));
		String name = path.substring(path.lastIndexOf("/") + 1);
		
		ProcessBuilder builder = new ProcessBuilder("java", "-jar", name);
		builder.directory(new File(dir));
		builder.redirectErrorStream(true);
		Process p = builder.start();
		
		return new SystemProcess(p, client);
	}
	
	public static byte[] iconOf(File file) throws IOException {
		Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
		
		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics g = img.createGraphics();
		icon.paintIcon(null, g, 0, 0);
		g.dispose();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, "png", out);
		
		return out.toByteArray();
	}

}

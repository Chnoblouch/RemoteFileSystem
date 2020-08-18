package app.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

public class SystemProcess {
	
	private Process process;
	private ServerClient client;
		
	private BufferedWriter writer;
	private BufferedReader readerIn;
	private BufferedReader readerError;
	
	private String outputIn;
	private String outputError;
	
	private long pid = new Random().nextLong();
	
	public SystemProcess(Process process, ServerClient client) throws IOException {
		this.process = process;
		this.client = client;
		
		writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
		readerIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
		readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	}
	
	public Process getProcess() {
		return process;
	}
	
	public String getOutputIn() {
		return outputIn;
	}
	
	public String getOutputError() {
		return outputError;
	}
	
	public long getPid() {
		return pid;
	}
	
	public void input(String input) throws IOException {
		writer.write(input);
		writer.flush();
	}
	
	public void exit() throws IOException {
//		writer.close();
//		readerIn.close();
//		readerError.close();
		
		System.out.println("destroying process...");
		process.destroy();
		System.out.println("process destroyed");
	}
	
	public void update() throws IOException {			
		outputIn = "";
		outputError = "";
		String line = null;
						
		while((line = readerIn.readLine()) != null) {
			outputIn += line + "\n";
			if(outputIn.length() >= 4096 || process.getInputStream().available() < 1) break;
		}
//		while((line = readerError.readLine()) != null) outputError += line + "\n";	
		if(outputIn.length() > 0)
			client.getSendingMaster().sendSystemProcessOutput(getPid(), false, outputIn);
		if(outputError.length() > 0)
			client.getSendingMaster().sendSystemProcessOutput(getPid(), true, outputError);
	}

}

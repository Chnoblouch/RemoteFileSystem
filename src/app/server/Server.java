package app.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import app.Application;

public class Server 
extends Thread {
	
	private ServerSocket socket;
	private ArrayList<ServerClient> clients = new ArrayList<>();
		
	private HashMap<Long, SystemProcess> systemProcesses = new HashMap<>();
	
	public void startServer() {
		try {
			socket = new ServerSocket(Application.PORT);
			System.out.println("server started");
			
			start();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isRunning() {
		return !socket.isClosed();
	}
	
	@Override
	public void run() {
		try {
			while(!socket.isClosed()) {					
				Socket s = socket.accept();
				
				ServerClient client = new ServerClient(s, this);
				client.start();
				clients.add(client);
				
				System.out.println("new client connected: " + client.getSocket().getInetAddress().getHostName());
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addSystemProcess(SystemProcess process) {
		Application.run(() -> systemProcesses.put(process.getPid(), process));
	}
	
	public void removeSystemProcess(long pid) {
		Application.run(() -> systemProcesses.remove(pid));
	}
	
	public SystemProcess getSystemProcess(long pid) {
		return systemProcesses.get(pid);
	}
	
	public HashMap<Long, SystemProcess> getSystemProcesses() {
		return systemProcesses;
	}
	
	public void closeServer() {
		try {
			for(ServerClient c : clients) c.getSocket().close();
			
			socket.close();
			clients.clear();
			System.out.println("server closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

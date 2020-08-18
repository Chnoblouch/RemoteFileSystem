package app;

import java.util.ArrayList;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import app.client.Client;
import app.server.Server;
import app.server.SystemProcess;
import app.ui.ClientUI;

public class Application {
	
	public static final int PORT = 3782;
	
	public static ArrayList<Runnable> runnables = new ArrayList<>();

	public static void run(Runnable runnable) {
		runnables.add(runnable);
	}
	
	public static void main(String[] args) {
		String type = args[0];
//		String type = "client";
		
		try {				
			if(type.equals("server")) {
				Server server = new Server();
				server.startServer();
				
				while(server.isRunning()) {
					for(Runnable r : runnables) r.run();
					runnables.clear();
					
					for(SystemProcess p : server.getSystemProcesses().values())
						p.update();	
					
					Thread.sleep(10);
				}
			} else if(type.equals("client")) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				
				Client client = new Client();
				
				ClientUI ui = new ClientUI(client);
				client.setUI(ui);
				
				client.start(args[1]);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}

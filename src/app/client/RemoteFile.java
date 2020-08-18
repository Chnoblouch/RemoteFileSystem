package app.client;

import javax.swing.Icon;

public class RemoteFile {
	
	private String name;
	private Icon icon;
	
	public RemoteFile(String path, Icon icon) {
		this.name = path;
		this.icon = icon;
	}
	
	public String getName() {
		return name;
	}
	
	public Icon getIcon() {
		return icon;
	}

}

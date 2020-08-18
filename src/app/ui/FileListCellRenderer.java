package app.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import app.client.RemoteFile;

public class FileListCellRenderer
extends DefaultListCellRenderer {
	
	private static final long serialVersionUID = 1L;
	private JLabel label;
	
	public FileListCellRenderer() {
		label = new JLabel();
		label.setOpaque(true);
	}
	
	@Override
	public Component getListCellRendererComponent(
			JList<?> list, Object value, int index, boolean selected, boolean expanded) {
		RemoteFile file = (RemoteFile) value;
		label.setText(file.getName());
		label.setIcon(file.getIcon());
		label.setBackground(selected ? list.getSelectionBackground() : Color.WHITE);
		label.setForeground(selected ? Color.WHITE : Color.BLACK);
				
		return label;
	}

}

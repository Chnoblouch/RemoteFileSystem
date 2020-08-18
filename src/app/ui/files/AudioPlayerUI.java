package app.ui.files;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;

import app.client.Client;
import app.ui.ClientUI;

public class AudioPlayerUI
extends JPanel
implements ActionListener {
	
	private Clip clip;
	private boolean playing = true;
	
	private ClientUI ui;
	private int index;
	
	private boolean closed = false;
	
	private JButton close;
	private JButton toggle;
	
	private JSlider slider;
	private boolean pressed = false;
	
	private GridBagConstraints constraints = new GridBagConstraints();
		
	public AudioPlayerUI(AudioInputStream inputStream, Client client, ClientUI ui, int index) {		
		this.ui = ui;
		this.index = index;
		
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			
		try {
			clip = AudioSystem.getClip();
			clip.open(inputStream);
			clip.start();
		} catch(LineUnavailableException | IOException e) {
			e.printStackTrace();
		}
		
		constraints.fill = GridBagConstraints.BOTH;
		
		close = new JButton("Close");
		close.addActionListener(this);
		add(close, constraints(0, 0, 1.0, 1.0, 1, 100, 100, 75, 10));
		
		toggle = new JButton("Pause");
		toggle.addActionListener(this);
		add(toggle, constraints(0, 1, 1.0, 1.0, 1, 100, 100, 10, 75));
		
		slider = new JSlider();
		slider.setMaximum(clip.getFrameLength());
		slider.setPaintTicks(true);
		slider.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				pressed = true;
				if(playing) clip.stop();
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				pressed = false;
				clip.setFramePosition(slider.getValue());
				if(playing) clip.start();
			}
		});
		add(slider, constraints(0, 2, 1.0, 1.0, 1, 0, 0, 0, 0));
		
		new Thread(() -> {
			while(!closed) {
				if(!pressed) slider.setValue(clip.getFramePosition());
				if(clip.getFramePosition() >= clip.getFrameLength() && playing) {
					playing = false;
					clip.stop();
					clip.setFramePosition(0);
					updateToggleButton();
				}
			}
		}).start();
		
		updateToggleButton();
	}
	
	private void updateToggleButton() {
		toggle.setText(playing ? "Pause" : "Play");
	}
	
	private GridBagConstraints constraints(int gridx, int gridy, double weightx, double weighty, int gridwidth,
			int insetsRight, int insetsLeft, int insetsTop, int insetsBottom) {
		constraints.gridx = gridx;
		constraints.gridy = gridy;
		constraints.weightx = weightx;
		constraints.weighty = weighty;
		constraints.gridwidth = gridwidth;
		constraints.insets.right = insetsRight;
		constraints.insets.left = insetsLeft;
		constraints.insets.top = insetsTop;
		constraints.insets.bottom = insetsBottom;
		
		return constraints;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == close) {
			clip.stop();
			closed = true;
			ui.closeTab(index);
		} else if(e.getSource() == toggle) {
			if(clip.isRunning()) clip.stop();
			else {
				if(clip.getFramePosition() >= clip.getFrameLength()) clip.setFramePosition(0);
				clip.start();
			}
			
			playing = !playing;
			updateToggleButton();
		}
	}
}

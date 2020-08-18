package app.ui.files;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import app.client.Client;
import app.ui.ClientUI;

public class ImageDisplayUI
extends JPanel
implements MouseWheelListener, MouseListener, MouseMotionListener {
		
	private BufferedImage img;
	
	private float scale = 1.0f;
	private float offsetX = 0.0f, offsetY = 0.0f;
	
	private float lastMouseX = 0.0f, lastMouseY = 0.0f;
	
	private JPanel imagePanel;
	private JButton close;
	
	public ImageDisplayUI(BufferedImage img, Client client, ClientUI ui, int index) {
		this.img = img;
		
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		addMouseWheelListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.ipadx = 40;
		constraints.ipady = 10;
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		
		close = new JButton("Close");
		close.addActionListener((event) -> ui.closeTab(index));
		add(close, constraints);
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		
		imagePanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				Graphics2D g2d = (Graphics2D) g;
				
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
									 RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				
				int width = (int) (img.getWidth() * scale);
				int height = (int) (img.getHeight() * scale);
				
				float centerX = g2d.getClipBounds().width * 0.5f - width * 0.5f;
				float centerY = g2d.getClipBounds().height * 0.5f - height * 0.5f;
				
				int x = (int) (centerX + offsetX * scale);
				int y = (int) (centerY + offsetY * scale);
				g2d.drawImage(img, x, y, width, height, null);
				
				g2d.drawRect(x, y, width, height);
			}
		};
		imagePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		add(imagePanel, constraints);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		scale -= e.getUnitsToScroll() * scale * 0.025f;
		scale = Math.min(Math.max(scale, 0.1f), 10.0f);
		
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		lastMouseX = e.getPoint().x;
		lastMouseY = e.getPoint().y;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		float deltaX = e.getPoint().x - lastMouseX;
		float deltaY = e.getPoint().y - lastMouseY;
		
		offsetX += deltaX / scale;
		offsetY += deltaY / scale;
		
		offsetX = Math.max(Math.min(img.getWidth() * 0.5f, offsetX), -img.getWidth() * 0.5f);
		offsetY = Math.max(Math.min(img.getHeight() * 0.5f, offsetY), -img.getHeight() * 0.5f);
		
		lastMouseX = e.getPoint().x;
		lastMouseY = e.getPoint().y;
		
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

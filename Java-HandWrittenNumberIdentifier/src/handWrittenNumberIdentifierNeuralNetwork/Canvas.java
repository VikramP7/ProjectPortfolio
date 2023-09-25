package handWrittenNumberIdentifierNeuralNetwork;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

//-----Canvas is a JPannel-----

public class Canvas extends JPanel{
	
	int [] img;
	
	int imageWidth = -1; // in number of pixels from img[]
	int imageHeight = -1; // number of pixels from img[]
	
	public Canvas(int canvasWidth, int canvasHeight) {
		this.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
		img = new int[canvasWidth*canvasHeight];
		
	}
	
	public void paint(Graphics g) {
		
		Graphics2D g2d = (Graphics2D) g;
		
		int pixelWidth = this.getWidth() / imageWidth;
		int pixelHeight = this.getHeight() / imageHeight;
		
		for (int x = 0; x < imageWidth; x++) {
			for (int y = 0; y < imageHeight; y++) {
				int curColour = img[(x*imageWidth)+y];
				g2d.setColor(new Color(255-curColour, 255-curColour, 255-curColour));
				g2d.fillRect(x*pixelWidth, y*pixelHeight, pixelWidth, pixelHeight);
			}
		}		
	}
	
	public void DrawCanvas(int [] imagePixelArr, int imagewidth) {
		img = imagePixelArr;
		imageWidth = imagewidth;
		imageHeight = img.length / imagewidth;
		this.repaint();
	}
	
	
}

package handWrittenNumberIdentifierNeuralNetwork;

import java.awt.*;
import javax.swing.*;

//-----Window is a JFrame-----

public class Window extends JFrame{
	
	public Canvas canvas;
	
	public Window(int width, int height, String title) {
		
		canvas = new Canvas(width, height);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle(title);
		this.add(canvas);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void ImageToDraw(int [] imageArr, int imagewidth) {
		canvas.DrawCanvas(imageArr, imagewidth);
	}
	
}

package handWrittenNumberIdentifierNeuralNetwork;

public class AIImage {
	
	int width;
	int height;
	public int imageArr[];
	public int label;
	String colours = "╤@#W$?!ac;:+=-._";
	
	public AIImage(int w, int h) {
		width = w;
		height = h;
		imageArr = new int [width*height];
	}
	
	public int [] DrawPixelImage() {
		return imageArr;
	}
	
	public void DrawASCIIImage() {
		
		System.out.println((label & 0xff) + ":");
		for (int x = 0; x < width; x++) {
    		for (int y = 0; y < height; y++) {
    			System.out.print(colours.charAt(15-(imageArr[(y*width)+x]/16)));
    			System.out.print(colours.charAt(15-(imageArr[(y*width)+x]/16)));
    		}
    		System.out.println();
    	}
	}
	
}

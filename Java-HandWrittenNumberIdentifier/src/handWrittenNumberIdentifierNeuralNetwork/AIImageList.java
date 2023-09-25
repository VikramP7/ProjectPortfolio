package handWrittenNumberIdentifierNeuralNetwork;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class AIImageList {
	
	String imageFileName = "";
    File imageFile;
    byte [] imageFileBytes = {0};
    ByteBuffer imageWrapped = null;
    
    String labelFileName = "";
    File labelFile;
    byte [] labelFileBytes = {0};
    ByteBuffer labelWrapped = null;
    
    int magicNumber;
    int numberOfImages;
    public int width;
    int height;
    
    public AIImage imageList [];
	
	public AIImageList(String imageNamePath, String labelNamePath) {
		
		imageFileName = imageNamePath;
		labelFileName = labelNamePath;
		
		try {
			imageFile = new File(imageFileName);
			imageFileBytes = Files.readAllBytes(imageFile.toPath());
			imageWrapped = ByteBuffer.wrap(imageFileBytes);
			
			labelFile = new File(labelFileName);
			labelFileBytes = Files.readAllBytes(labelFile.toPath());
			labelWrapped = ByteBuffer.wrap(labelFileBytes);
		} 
	    catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public void Initialize() {
    	magicNumber = imageWrapped.getInt();
	    numberOfImages = imageWrapped.getInt();
	    width = imageWrapped.getInt();
	    height = imageWrapped.getInt();
	    
	    labelWrapped.getInt();
	    labelWrapped.getInt();

	    
	    imageList = new AIImage[numberOfImages];
	    
	    for (int n = 0; n < imageList.length; n++) {
	    	
	    	AIImage aiImage = new AIImage(width, height);
	    	aiImage.label = (labelWrapped.get()) & 0xff;
	    	
	    	for (int x = 0; x < width; x++) {
	    		for (int y = 0; y < height; y++) {
	    			aiImage.imageArr[(y*width)+x] = (imageWrapped.get() & 0xff);
	    		}
	    	}
	    	
	    	imageList[n] = aiImage;
	    }

	}
	

}

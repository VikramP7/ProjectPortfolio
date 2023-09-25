package flappyBirdAI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PFont;

public class FlappyBirdAI extends PApplet {
	
	Bird bird; // for a player to play
	SmartBird smartBird; // real simple Rule based AI
	GeneticBird[] geneticBirds = new GeneticBird[100];
	float scrollSpeed;
	float frountPipeX;
	float pipeInterval = 500;
	float pipeGap = 100.0f;
	float pipeWidth = 75.0f;
	float gravity = 0.03f;
	float terminalVelocity = 15.0f;
	int mode = 2; // 0=player, 1=RuleBased, 2=Genetic, 3=NeuralNetwork
	int generation = 1;
	boolean gameOver = false;
	boolean start = false;
	List<Float> pipes = new ArrayList<Float>();
	List<Float> clouds = new ArrayList<Float>();
	int timeSinceLastJump = 0;
	

    // The argument passed to main must match the class name
    public static void main(String[] args) {
        PApplet.main("flappyBirdAI.FlappyBirdAI");
    }

    // method used only for setting the size of the window
    public void settings(){
    	
        size(1080, 690); // initially 1480 960
    }

    // identical use to setup in Processing IDE except for size()
    public void setup(){
    	background(232, 12);
    	if (mode == 0) {
    		//          intialY, intialx, velocity, acceleration, terminalVelocity
    		bird = new Bird(height/2, 200, 0.0f, gravity, 50.0f, terminalVelocity);
		}else if (mode == 1) {
			smartBird = new SmartBird(height/2, 200, 0.0f, gravity, 50.0f, terminalVelocity);
		}else if (mode == 2) {
			for (int i = 0; i < geneticBirds.length; i++) {
				geneticBirds[i] = new GeneticBird(height/2, 200, 0.0f, gravity, 50.0f, terminalVelocity, System.currentTimeMillis()+i);
			}
		}
    	
    	scrollSpeed = 8.0f;
    	frountPipeX = width;
    	
    	Random random = new Random(System.currentTimeMillis());
    	for(int i = 0; i < width/(width/40); i++) {
    		clouds.add(random.nextFloat()*300);
    	}
    	DrawBackDrop();
    }

    // identical use to draw in Processing IDE
    public void draw(){
    	boolean jumping = false;
    	boolean[] birdsJumping = new boolean[geneticBirds.length];
    	boolean noOneAlive = true;
    	if (!start) {
    		start = (keyCode == 32);
    		keyCode = 0;
    	}
    	if (start) {
        	// move stuff
        	AdvancePipes();
        	if(mode == 0) {
        		// bird jumps 32 = spacebar
        		if (keyCode == 32 && timeSinceLastJump > 1) {
            		bird.Jump();
            	}
            	keyCode = 0;
            	timeSinceLastJump++;
        		bird.AdvanceBird(frountPipeX);
        	}
        	else if (mode == 1) {
        		jumping = smartBird.AdvanceBird(frountPipeX, pipes, pipeGap, pipeWidth, pipeInterval);
			}
        	else if(mode == 2) {
        		for (int i = 0; i < geneticBirds.length; i++) {
        			float output = geneticBirds[i].AdvanceBird(frountPipeX, pipes, pipeGap, pipeWidth, pipeInterval, scrollSpeed, (height/8)*7);
        			birdsJumping[i] = (output>0.5);
				}
			}
        	
        	
        	//graphics
        	clear();
        	DrawBackDrop();
        	DrawPipes();
        	if(mode == 0) {
        		DrawBird(bird);
            	DrawScore(bird);
        	}else if (mode == 1) {
        		DrawBird(smartBird, jumping);
            	DrawScore(smartBird);
			}
        	else if (mode == 2) {
				for (int i = 0; i < birdsJumping.length; i++) {
					DrawBird(geneticBirds[i], birdsJumping[i]);
					DrawScore(geneticBirds[i]);
					DrawGeneration();
				}
			}
        	
        	if(mode == 0) {
        		if (!IsBirdDead(bird)) {
            		bird.alive = false;
            		start = false;
            		gameOver = true;
            		Reset();
            	}
        	}else if (mode == 1) {
        		if (!IsBirdDead(smartBird)) {
            		smartBird.alive = false;
            		start = false;
            		gameOver = true;
            		Reset();
            	}
			}else if (mode == 2) {
				noOneAlive = true;
				for (int i = 0; i < geneticBirds.length; i++) {
					if ((!IsBirdDead(geneticBirds[i]))) { // checks if the bird is die (!geneticBirds[i].alive) || 
	            		geneticBirds[i].alive = false;
	            		System.out.println("Bird Dead: " + str(i) + " | Time Alive: " + str(geneticBirds[i].timeAlive));
	            	}
					else if (geneticBirds[i].alive) {
						noOneAlive = false;
					}
				}
				if(noOneAlive) {
	        		System.out.println("All Dead");
	        		System.out.println("|-----------------------------------------------------------------------------------|");
	        		gameOver = true;
	        		start = false;
	        	}
			}
        	
    	}
    	if (!gameOver) {
    		if(mode == 0) {
        		DrawBird(bird);
            	DrawScore(bird);
        	}else if (mode == 1) {
        		DrawBird(smartBird, jumping);
            	DrawScore(smartBird);
			}
        	else if (mode == 2) {
        		for (int i = 0; i < birdsJumping.length; i++) {
					DrawBird(geneticBirds[i], birdsJumping[i]);
					DrawScore(geneticBirds[i]);
				}
			}
    	}else { // game over
			if(mode == 2) {
				// find the most powerfull birds (those ten who lived the longest)
				int[] bestBirdIndecies = new int[(int)(geneticBirds.length*(0.10))]; // list of indecies for the top ten percent of the pop.
				for (int j = 0; j < bestBirdIndecies.length; j++) {
					int curBest = 0;
					int curBestIndex = 0;
					for (int i = 0; i < geneticBirds.length; i++) {
						if((geneticBirds[i].timeAlive>curBest)) {
							curBest = geneticBirds[i].timeAlive;
							curBestIndex = i;
						} 
					}
					geneticBirds[curBestIndex].timeAlive = -2;
					bestBirdIndecies[j] = curBestIndex;
				}
				
				// now there is a list of the ten best indexes of birds
				GeneticBird[] nextGeneration = new GeneticBird[geneticBirds.length];
				for (int i = 0; i < nextGeneration.length; i++) {
					nextGeneration[i] = geneticBirds[bestBirdIndecies[i%bestBirdIndecies.length]].CopyGenes();
					nextGeneration[i].MutateGenetics(System.currentTimeMillis()+i);
					nextGeneration[i].Reset(height/2, 200, 0.0f, gravity, 50.0f, terminalVelocity, System.currentTimeMillis()+i);
				}
				
				geneticBirds = nextGeneration;
				gameOver = false;
				start = false;
				generation++;
				Reset();
			}
		}
    	
    }
    
    public void DrawBackDrop() {
    	//sky
    	background(90, 224, 213);
    	//clouds
    	fill(255);
    	//stroke(245, 245, 245);
    	//strokeWeight(10);
    	noStroke();
    	for(int i = 0; i < width/(width/40); i++) {
    		ellipse(i*(width/40), height/2, clouds.get(i), clouds.get(i));
    	}
    	noStroke();
    	strokeWeight(1);
    	rect(0, (height/2), width, height/2);
    	//ground
    	fill(224, 187, 139);
    	noStroke();
    	rect(0, (height/8)*7, width, (height/8));
    	//grass
    	fill(166, 237, 0);
    	rect(0, (height/8)*7, width, (height/32));
    	fill(196, 267, 30);
    	rect(0, (height/8)*7, width, (height/64));
	}
    
    public void DrawBird(Bird dbird) {
    	fill(237, 217, 0);
    	stroke(0);
    	ellipse(dbird.X, dbird.Y, dbird.size, dbird.size);
	}
    public void DrawBird(SmartBird dbird, boolean jump) {
    	if (jump) {
    		fill(0,255, 0);
    	}else {
    		fill(237, 217, 0);
    	}
    	stroke(0);
    	ellipse(dbird.X, dbird.Y, dbird.size, dbird.size);
	}
    public void DrawBird(GeneticBird dbird, boolean jump) {
    	if (jump) {
    		fill(0,255, 0);
    	}else {
    		fill(dbird.race.getRed(), dbird.race.getGreen(), dbird.race.getBlue());
    	}
    	stroke(0);
    	ellipse(dbird.X, dbird.Y, dbird.size, dbird.size);
	}
    
    public void DrawPipes() {
    	fill(133, 230, 64);
    	stroke(0);
		for(int i = 0; i < pipes.size(); i++) {
			//bottom pipe
			rect(frountPipeX + (i*pipeInterval)-(pipeWidth/2), pipes.get(i)+pipeGap, pipeWidth, height-(height/8));
			rect((frountPipeX + (i*pipeInterval)) - (pipeWidth/2) - (pipeWidth/4), pipes.get(i)+pipeGap, pipeWidth+(pipeWidth/2), 30);
			//top pipe
			rect(frountPipeX + (i*pipeInterval)-(pipeWidth/2), 0, pipeWidth, pipes.get(i)-pipeGap);
			rect((frountPipeX + (i*pipeInterval))- (pipeWidth/2) - (pipeWidth/4), pipes.get(i)-pipeGap-30, pipeWidth+(pipeWidth/2), 30);
		}
	}
    
    public void DrawScore(Bird dird) {
    	fill(0);
    	PFont font = createFont("Arial", 16, true);
    	textFont(font, 16);
    	text(str(dird.score), dird.X, dird.Y);
	}
    public void DrawScore(SmartBird dird) {
    	fill(0);
    	PFont font = createFont("Arial", 16, true);
    	textFont(font, 16);
    	text(str(dird.score), dird.X, dird.Y);
    	//text(str(dird.score), 20, 20);
	}
    public void DrawScore(GeneticBird dird) {
    	fill(0);
    	PFont font = createFont("Arial", 16, true);
    	textFont(font, 16);
    	//text(str(dird.timeAlive), dird.X, dird.Y);
    	text(str(dird.score), dird.X, dird.Y);
	}
    
    public void DrawGeneration() {
    	fill(0);
    	PFont font = createFont("Arial", 16, true);
    	textFont(font, 16);
    	text(str(generation), 20, 20);
	}
    
    public void AdvancePipes() {
		frountPipeX += -scrollSpeed;
		if (frountPipeX < -50) {
			frountPipeX = frountPipeX + pipeInterval;
			pipes.remove(0);
		}
		Random random = new Random(System.currentTimeMillis());
		if ((frountPipeX + (pipeInterval*(pipes.size()-1))<(width + 200))){
			pipes.add(random.nextInt((height/2)-(int)pipeGap)+(height/4.0f));
			
		}
	}
    
    public boolean IsBirdDead(Bird dird) {
		boolean livelyness = true;
		
		// check for collisions
    	float realFrountPipeX = frountPipeX;
    	int pipeIndex = 0;
    	while ((realFrountPipeX + pipeWidth) < (dird.X-dird.size)) {
    		pipeIndex++;
			realFrountPipeX = realFrountPipeX + pipeInterval;
		}
    	
    	// works if bird is square
		if (((dird.X + (dird.size/2)) > (realFrountPipeX - (pipeWidth/2) - (pipeWidth/4)))) {
    		if (((dird.Y-(dird.size/2)) < ((pipes.get(pipeIndex))-pipeGap)) || ((dird.Y+(dird.size/2)) > ((pipes.get(pipeIndex))+pipeGap))) {
    			livelyness = false;
    		}
    	}
		
		//line(dird.X, dird.Y, realFrountPipeX, pipes.get(pipeIndex));
		
		// because bird is not square we have to do more checks to prove its not a false positive
		if (!livelyness) {
			livelyness = true;
			// this is where stuff gets stupid
			for (int x = (int) (dird.X-(dird.size/2)); x < (int) (dird.X+dird.size); x++) {
				for (int y = (int) (dird.Y-(dird.size/2)); y < (int) (dird.Y+dird.size); y++) {
					if(((int)Math.hypot((x-dird.X), (y-dird.Y)) == (dird.size/2))) { // currently <= if its slow try ==
						// checks if this pixel is actually within the bird
						for (int i = (int)(realFrountPipeX-(pipeWidth/2)-(pipeWidth/4)); i < realFrountPipeX+((pipeWidth/2)+(pipeWidth/4)); i++) {
							// top
							for (int j = 0; j < pipes.get(pipeIndex)-(pipeGap); j++) {
								if ((x == i) && (y == j)) {
									livelyness = false;
									fill(0, 0, 255);
									ellipse(x, y, 1, 1);
								}
							}
							
							// bottom
							for (int j = height; j > pipes.get(pipeIndex)+pipeGap; j--) {
								if ((x == i) && (y == j)) {
									livelyness = false;
									fill(0, 0, 255);
									ellipse(x, y, 1, 1);
								}
							}
						}
					}
				}
			}
		}
		
		if((dird.Y > (height/8)*7)) {
			livelyness = false;
		}
		if(dird.Y < 0) {
			livelyness = false;
		}
		
		return livelyness;
		
	}
    public boolean IsBirdDead(SmartBird dird) {
		boolean livelyness = true;
		
		// check for collisions
    	float realFrountPipeX = frountPipeX;
    	int pipeIndex = 0;
    	while ((realFrountPipeX + pipeWidth) < (dird.X-dird.size)) {
    		pipeIndex++;
			realFrountPipeX = realFrountPipeX + pipeInterval;
		}
    	
    	//line(dird.X, dird.Y, realFrountPipeX, pipes.get(pipeIndex));
    	
    	// works if bird is square
		if (((dird.X + (dird.size/2)) > (realFrountPipeX - (pipeWidth/2) - (pipeWidth/4)))) {
    		if (((dird.Y-(dird.size/2)) < ((pipes.get(pipeIndex))-pipeGap)) || ((dird.Y+(dird.size/2)) > ((pipes.get(pipeIndex))+pipeGap))) {
    			livelyness = false;
    		}
    	}
		
		// because bird is not square we have to do more checks to prove its not a false positive
		if (!livelyness) {
			livelyness = true;
			// this is where stuff gets stupid
			for (int x = (int) (dird.X-(dird.size/2)); x < (int) (dird.X+dird.size); x++) {
				for (int y = (int) (dird.Y-(dird.size/2)); y < (int) (dird.Y+dird.size); y++) {
					if(((int)Math.hypot((x-dird.X), (y-dird.Y)) == (dird.size/2))) { // currently <= if its slow try ==
						// checks if this pixel is actually within the bird
						for (int i = (int)(realFrountPipeX-(pipeWidth/2)-(pipeWidth/4)); i < realFrountPipeX+((pipeWidth/2)+(pipeWidth/4)); i++) {
							// top
							for (int j = 0; j < pipes.get(pipeIndex)-(pipeGap); j++) {
								if ((x == i) && (y == j)) {
									livelyness = false;
									fill(0, 0, 255);
									ellipse(x, y, 1, 1);
								}
							}
							
							// bottom
							for (int j = height; j > pipes.get(pipeIndex)+pipeGap; j--) {
								if ((x == i) && (y == j)) {
									livelyness = false;
									fill(0, 0, 255);
									ellipse(x, y, 1, 1);
								}
							}
						}
					}
				}
			}
		}
		
		if((dird.Y > (height/8)*7)) {
			livelyness = false;
		}
		if(dird.Y < 0) {
			livelyness = false;
		}
		
		return livelyness;
	}
    public boolean IsBirdDead(GeneticBird dird) {
		boolean livelyness = true;
		
		// check for collisions
    	float realFrountPipeX = frountPipeX;
    	int pipeIndex = 0;
    	while ((realFrountPipeX + pipeWidth) < (dird.X-dird.size)) {
    		pipeIndex++;
			realFrountPipeX = realFrountPipeX + pipeInterval;
		}
    	
    	//line(dird.X, dird.Y, realFrountPipeX, pipes.get(pipeIndex));
    	
    	// works if bird is square
		if (((dird.X + (dird.size/2)) > (realFrountPipeX - (pipeWidth/2) - (pipeWidth/4)))) {
    		if (((dird.Y-(dird.size/2)) < ((pipes.get(pipeIndex))-pipeGap)) || ((dird.Y+(dird.size/2)) > ((pipes.get(pipeIndex))+pipeGap))) {
    			livelyness = false;
    		}
    	}
		
		// because bird is not square we have to do more checks to prove its not a false positive
		if (!livelyness) {
			livelyness = true;
			// this is where stuff gets stupid
			for (int x = (int) (dird.X-(dird.size/2)); x < (int) (dird.X+dird.size); x++) {
				for (int y = (int) (dird.Y-(dird.size/2)); y < (int) (dird.Y+dird.size); y++) {
					if(((int)Math.hypot((x-dird.X), (y-dird.Y)) == (dird.size/2))) { // currently <= if its slow try ==
						// checks if this pixel is actually within the bird
						for (int i = (int)(realFrountPipeX-(pipeWidth/2)-(pipeWidth/4)); i < realFrountPipeX+((pipeWidth/2)+(pipeWidth/4)); i++) {
							// top
							for (int j = 0; j < pipes.get(pipeIndex)-(pipeGap); j++) {
								if ((x == i) && (y == j)) {
									livelyness = false;
									fill(0, 0, 255);
									ellipse(x, y, 1, 1);
								}
							}
							
							// bottom
							for (int j = height; j > pipes.get(pipeIndex)+pipeGap; j--) {
								if ((x == i) && (y == j)) {
									livelyness = false;
									fill(0, 0, 255);
									ellipse(x, y, 1, 1);
								}
							}
						}
					}
				}
			}
		}
		
		if((dird.Y > (height/8)*7)) {
			livelyness = false;
		}
		if(dird.Y < 0) {
			livelyness = false;
		}
		
		return livelyness;
	}

    public void Reset() {
    	//background(232, 12);
    	//           intialY, intialx, velocity, acceleration, terminalVelocity
    	bird = new Bird(height/2, 200, 0.0f, gravity, 50.0f, terminalVelocity);
    	smartBird = new SmartBird(height/2, 200, 0.0f, gravity, 50.0f, terminalVelocity);
    	scrollSpeed = 8.0f;
    	pipes = new ArrayList<Float>();
    	frountPipeX = width;
    	keyCode = 0;
	}
}
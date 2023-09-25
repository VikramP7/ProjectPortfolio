package flappyBirdAI;

import java.util.List;

public class SmartBird {
	public float Y;
	public float X;
	public float YVelocity;
	public float YAccelleration;
	public float size; // diameter
	public float timeSinceLastJump;
	public float terminalVelocity;
	public boolean alive;
	public int score;
	
	public SmartBird(float initalBirdY, float constantBirdX, float initialBirdVelocity, float constantBirdYAccelleration, float birdSize, float constantTerminalVelocity) {
		Y = initalBirdY;
		X = constantBirdX;
		YVelocity = initialBirdVelocity;
		YAccelleration = constantBirdYAccelleration;
		size = birdSize;
		terminalVelocity = constantTerminalVelocity;
		timeSinceLastJump = 0;
		alive = true;
		score = 0;
	}
	
	public boolean AdvanceBird(float frountPipeLocationX, List<Float> frountPipeLocationsY, float pipeGap, float pipeWidth, float pipeInterval) {
		boolean jumping = false;
		YVelocity = Math.min((YVelocity+(YAccelleration*timeSinceLastJump)), terminalVelocity);
		Y += YVelocity;
		timeSinceLastJump++;
		if (frountPipeLocationX <= X + 3 && frountPipeLocationX >= X - 4) {
			score++;
		}
		
		// its name is smart but this is gonna be some simmple AI stuff
		// check if the bird is lower than the lowest pipe, if so, jump. else don't jump
		float realFrountPipeX = frountPipeLocationX;
    	int pipeIndex = 0;
    	while ((realFrountPipeX + pipeWidth) < (X-size)) {
    		pipeIndex++;
			realFrountPipeX = realFrountPipeX + pipeInterval;
		}
		float margin = 15;
		if((Y+(size/2))>(frountPipeLocationsY.get(pipeIndex)+pipeGap-margin)) {
			jumping = true;
		}
		if ((X) > (frountPipeLocationX) && (Y+(size/2)) < (frountPipeLocationsY.get(pipeIndex+1)-pipeGap)) {
			jumping = false;
		}
		
		if (jumping) {
			Jump(); 
		}
		
		return jumping;
	}
	
    public void Jump() {
    	YVelocity = -8;
		timeSinceLastJump = 0;
	}

}

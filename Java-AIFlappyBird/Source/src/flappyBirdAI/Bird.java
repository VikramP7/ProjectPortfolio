package flappyBirdAI;

public class Bird {
	public float Y;
	public float X;
	public float YVelocity;
	public float YAccelleration;
	public float size; // diameter
	public float timeSinceLastJump;
	public float terminalVelocity;
	public boolean alive;
	public int score;
	
	public Bird(float initalBirdY, float constantBirdX, float initialBirdVelocity, float constantBirdYAccelleration, float birdSize, float constantTerminalVelocity) {
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
	
	public void AdvanceBird(float frountPipeLocation) {
		YVelocity = Math.min((YVelocity+(YAccelleration*timeSinceLastJump)), terminalVelocity);
		Y += YVelocity;
		timeSinceLastJump++;
		if (frountPipeLocation <= X + 3 && frountPipeLocation >= X - 4) {
			score++;
		}
	}
    public void Jump() {
    	YVelocity = -8;
		timeSinceLastJump = 0;
	}

}

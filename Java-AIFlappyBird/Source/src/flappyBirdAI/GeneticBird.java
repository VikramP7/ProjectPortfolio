package flappyBirdAI;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import flappyBirdAI.Matrix;

public class GeneticBird {

	public float Y;
	public float X;
	public float YVelocity;
	public float YAccelleration;
	public float size; // diameter
	public float timeSinceLastJump;
	public float terminalVelocity;
	public boolean alive;
	public int score;
	public int timeAlive;
	
	// THE BRAIN:
	Matrix inputLayer = new Matrix(3, 1); // bird's y, pipes x and y
	Matrix inputHiddenWeights = new Matrix(2, 3);
	Matrix hiddenBias = new Matrix(2, 1);
	Matrix hiddenLayer = new Matrix(2, 1);
	Matrix hiddenOutputWeights = new Matrix(1, 2);
	Matrix outputBias = new Matrix(1, 1);
	Matrix outputLayer = new Matrix(1,1);
	Function<Float, Float> sigmoindFunction = x -> (1/(1 + (float)Math.exp(-x)));
	Color race = new Color(237, 217, 0);
	
	float MUTATIONNESS = 0.2f;
	
	public GeneticBird(float initalBirdY, float constantBirdX, float initialBirdVelocity, float constantBirdYAccelleration, float birdSize, float constantTerminalVelocity, long seed) {
		Y = initalBirdY;
		X = constantBirdX;
		YVelocity = initialBirdVelocity;
		YAccelleration = constantBirdYAccelleration;
		size = birdSize;
		terminalVelocity = constantTerminalVelocity;
		timeSinceLastJump = 0;
		alive = true;
		score = 0;
		timeAlive = 0;
		
		Random random = new Random(seed);
		race = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
		
		// THE BRAIN:
		inputHiddenWeights.RandomFill(seed);
		hiddenBias.RandomFill(seed);
		hiddenOutputWeights.RandomFill(seed);
		outputBias.RandomFill(seed);
	}
	
	public void Reset(float initalBirdY, float constantBirdX, float initialBirdVelocity, float constantBirdYAccelleration, float birdSize, float constantTerminalVelocity, long seed) {
		Y = initalBirdY;
		X = constantBirdX;
		YVelocity = initialBirdVelocity;
		YAccelleration = constantBirdYAccelleration;
		size = birdSize;
		terminalVelocity = constantTerminalVelocity;
		timeSinceLastJump = 0;
		alive = true;
		score = 0;
		timeAlive = 0;
		
		Random random = new Random(seed);
		race = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}
		
	public float AdvanceBird(float frountPipeLocationX, List<Float> pipeLocationsY, float pipeGap, float pipeWidth, float pipeInterval, float scrollSpeed, float ground) {
		boolean jumping = false;
		
		float output = -1.0f;
		
		if (alive) {
			// update the physics of the bird
			YVelocity = Math.min((YVelocity+(YAccelleration*timeSinceLastJump)), terminalVelocity);
			Y += YVelocity;
			timeSinceLastJump++;
			timeAlive++;
			
			// Manage Score Keeping
			if (frountPipeLocationX <= X + 3 && frountPipeLocationX >= X - 4) {
				score++;
			}
			
			// BRAIN:
			
			float realFrountPipeX = frountPipeLocationX;
	    	int pipeIndex = 0;
	    	while ((realFrountPipeX + pipeWidth) < (X-size)) {
	    		pipeIndex++;
				realFrountPipeX = realFrountPipeX + pipeInterval;
			}
			
			float[] input = {Y, realFrountPipeX, pipeLocationsY.get(pipeIndex)};
			
			inputLayer = Matrix.FromArray(input);
			
			// Creates hidden node values by multiplying the input values by the coresponding weights
			hiddenLayer = Matrix.MatrixProduct(inputHiddenWeights, inputLayer);
			hiddenLayer.add(hiddenBias);
			// Activation Function!
			hiddenLayer.map(sigmoindFunction);
			
			// Creates the Output node values by multiplying the hidden values by the coresponding weights
			outputLayer = Matrix.MatrixProduct(hiddenOutputWeights, hiddenLayer);
			outputLayer.add(outputBias);
			// Activation Function!
			outputLayer.map(sigmoindFunction);
			
			output = outputLayer.data[0][0];
			
			if (output>0.5) {
				jumping = true; 
			}
			else {
				jumping = false;
			}
			
			if(jumping) {
				Jump();
			}
		}
		else {
			// update the physics of the bird
			if (Y<ground) {
				YVelocity = Math.min((YVelocity+(YAccelleration*timeSinceLastJump)), terminalVelocity);
				Y += YVelocity;
			}
			X = X-scrollSpeed;
			timeSinceLastJump++;
			race = new Color(255, 0, 0);
		}
		
		return output;
	}
	
	public void MutateGenetics(long seed) {
		Random random = new Random(seed);
		
		Matrix inputHiddenWeightDelta = new Matrix(inputHiddenWeights.row, inputHiddenWeights.col);
		inputHiddenWeightDelta.RandomFill(seed+1);
		inputHiddenWeightDelta.multiply(MUTATIONNESS);
		inputHiddenWeights.add(inputHiddenWeightDelta);
		
		Matrix hiddenBiasDelta = new Matrix(hiddenBias.row, hiddenBias.col);
		hiddenBiasDelta.RandomFill(seed+2);
		hiddenBiasDelta.multiply(MUTATIONNESS);
		hiddenBias.add(hiddenBiasDelta);
		
		Matrix hiddenOutputWeightDelta = new Matrix(hiddenOutputWeights.row, hiddenOutputWeights.col);
		hiddenOutputWeightDelta.RandomFill(seed+3);
		hiddenOutputWeightDelta.multiply(MUTATIONNESS);
		hiddenOutputWeights.add(hiddenOutputWeightDelta);
		
		Matrix outputBiasDelta = new Matrix(outputBias.row, outputBias.col);
		outputBiasDelta.RandomFill(seed+4);
		outputBiasDelta.multiply(MUTATIONNESS);
		outputBias.add(outputBiasDelta);
	}
	
	public GeneticBird CopyGenes() {
		GeneticBird nextBird = new GeneticBird(Y, X, YVelocity, YAccelleration, size, terminalVelocity, score);
		
		nextBird.hiddenBias = new Matrix(hiddenBias.row, hiddenBias.col);
		for (int i = 0; i < hiddenBias.row; i++) {
			for (int j = 0; j < hiddenBias.col; j++) {
				nextBird.hiddenBias.data[i][j] = hiddenBias.data[i][j];
			}
		}
		nextBird.inputHiddenWeights = new Matrix(inputHiddenWeights.row, inputHiddenWeights.col);
		for (int i = 0; i < inputHiddenWeights.row; i++) {
			for (int j = 0; j < inputHiddenWeights.col; j++) {
				nextBird.inputHiddenWeights.data[i][j] = inputHiddenWeights.data[i][j];
			}
		}
		nextBird.hiddenBias = new Matrix(hiddenBias.row, hiddenBias.col);
		for (int i = 0; i < hiddenBias.row; i++) {
			for (int j = 0; j < hiddenBias.col; j++) {
				nextBird.hiddenBias.data[i][j] = hiddenBias.data[i][j];
			}
		}
		nextBird.hiddenOutputWeights = new Matrix(hiddenOutputWeights.row, hiddenOutputWeights.col);
		for (int i = 0; i < hiddenOutputWeights.row; i++) {
			for (int j = 0; j < hiddenOutputWeights.col; j++) {
				nextBird.hiddenOutputWeights.data[i][j] = hiddenOutputWeights.data[i][j];
			}
		}
		
		return nextBird;
	}
	
    public void Jump() {
    	YVelocity = -8;
		timeSinceLastJump = 0;
	}

}
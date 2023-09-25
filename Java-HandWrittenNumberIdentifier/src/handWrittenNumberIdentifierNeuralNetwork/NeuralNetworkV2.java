package handWrittenNumberIdentifierNeuralNetwork;

import java.util.function.Function;

public class NeuralNetworkV2 {
	int NumberOfInputNodes;
	int[] HiddenNodes;
	int NumberOfOutputNodes;
	public Matrix[] weightsMatricies;
	public Matrix[] biasMatricies;
	public Matrix[] neuronActivationMatricies; // Just used for external speculation, no calculations are done to this or from this

	int BATCHSIZE = 0;
	float LEARNINGRATE = 8.5f;
	
	Function<Float, Float> ACTIVATIONFUNCTION;
	Function<Float, Float> ACTIVATIONDERIVATIVEFUNCTION;
	
	Function<Float, Float> sigmoidFunction = x -> (1.0f/(1.0f + (float)Math.exp(-x)));
	Function<Float, Float> sigmoidDerivativeFunction = x -> (float)((Math.exp(-x))/(Math.pow((1+Math.exp(-x)), 2.0f)));
	
	Function<Float, Float> ReLUFunction = x -> ((x>0.0f) ? x : 0.0f);
	Function<Float, Float> ReLUDerivativeFunction = x -> ((x>0.0f) ? 1.0f : 0.0f);
	
	Function<Float, Float> SiLUFunction = x -> (x/(1.0f + (float)Math.exp(-x)));
	Function<Float, Float> SiLUDerivativeFunction = x -> (float)((1.0f+Math.exp(-x)+(x*Math.exp(-x)))/(Math.pow((1.0f+Math.exp(-x)), 2.0f)));
	
	Function<Float, Float> binaryFunction = x -> ((x>=0.0f) ? 1.0f : 0.0f);
	Function<Float, Float> binaryDerivativeFunction = x -> (0.0f*x);
	
	Function<Float, Float> linearFunction = x -> x;
	Function<Float, Float> linearDerivativeFunction = x -> (0.0f*x)+1;
	
	Function<Float, Float> softPlusFunction = x -> (float)(Math.log(1+Math.exp(x)));
	Function<Float, Float> softPlusDerivativeFunction = x -> (1.0f/(1.0f + (float)Math.exp(-x)));
		
	
	public NeuralNetworkV2(int inputs, int[] hidden, int output, Activation activation) {
		this.NumberOfInputNodes = inputs;
		this.HiddenNodes = hidden;
		this.NumberOfOutputNodes = output;
		
		weightsMatricies = new Matrix[hidden.length+1];
		weightsMatricies[0] = new Matrix(hidden[0], inputs);
		weightsMatricies[0].RandomFill();
		for (int i = 1; i < weightsMatricies.length-1; i++) {
			weightsMatricies[i] = new Matrix(hidden[i], hidden[i-1]);
			weightsMatricies[i].RandomFill();
		}
		weightsMatricies[weightsMatricies.length-1] = new Matrix(output, hidden[hidden.length-1]);
		weightsMatricies[weightsMatricies.length-1].RandomFill();
		
		biasMatricies = new Matrix[hidden.length+1];
		for (int i = 0; i < hidden.length; i++) {
			biasMatricies[i] = new Matrix(hidden[i], 1);
			biasMatricies[i].RandomFill();
		}
		biasMatricies[biasMatricies.length-1] = new Matrix(output, 1);
		biasMatricies[biasMatricies.length-1].RandomFill();
		
		neuronActivationMatricies = new Matrix[1+hidden.length+1]; // One for input layer, one for each hidden and one for ouput
		neuronActivationMatricies[0] = new Matrix(inputs, 1);
		for (int i = 0; i < hidden.length; i++) {
			neuronActivationMatricies[i+1] = new Matrix(hidden[i], 1);
		}
		neuronActivationMatricies[neuronActivationMatricies.length-1] = new Matrix(output, 1);
		
		SetActivation(activation);
	}
	
	public void Reset(Activation activation) {
		weightsMatricies = new Matrix[HiddenNodes.length+1];
		weightsMatricies[0] = new Matrix(HiddenNodes[0], NumberOfInputNodes);
		weightsMatricies[0].RandomFill();
		for (int i = 1; i < weightsMatricies.length-1; i++) {
			weightsMatricies[i] = new Matrix(HiddenNodes[i], HiddenNodes[i-1]);
			weightsMatricies[i].RandomFill();
		}
		weightsMatricies[weightsMatricies.length-1] = new Matrix(NumberOfOutputNodes, HiddenNodes[HiddenNodes.length-1]);
		weightsMatricies[weightsMatricies.length-1].RandomFill();
		
		biasMatricies = new Matrix[HiddenNodes.length+1];
		for (int i = 0; i < HiddenNodes.length; i++) {
			biasMatricies[i] = new Matrix(HiddenNodes[i], 1);
			biasMatricies[i].RandomFill();
		}
		biasMatricies[biasMatricies.length-1] = new Matrix(NumberOfOutputNodes, 1);
		biasMatricies[biasMatricies.length-1].RandomFill();
		
		neuronActivationMatricies = new Matrix[1+HiddenNodes.length+1]; // One for input layer, one for each hidden and one for ouput
		neuronActivationMatricies[0] = new Matrix(NumberOfInputNodes, 1);
		for (int i = 0; i < HiddenNodes.length; i++) {
			neuronActivationMatricies[i+1] = new Matrix(HiddenNodes[i], 1);
		}
		neuronActivationMatricies[neuronActivationMatricies.length-1] = new Matrix(NumberOfOutputNodes, 1);
		
		SetActivation(activation);
	}
	
	public void SetLearningRate(float learningRate) {
		LEARNINGRATE = learningRate;
	}
	
	private void SetActivation(Activation activation) {
		switch (activation) {
		case SIGMOID:
			ACTIVATIONFUNCTION = sigmoidFunction;
			ACTIVATIONDERIVATIVEFUNCTION = sigmoidDerivativeFunction;
			break;
		case RELU:
			ACTIVATIONFUNCTION = ReLUFunction;
			ACTIVATIONDERIVATIVEFUNCTION = ReLUDerivativeFunction;
			break;
		case SILU:
			ACTIVATIONFUNCTION = SiLUFunction;
			ACTIVATIONDERIVATIVEFUNCTION = SiLUDerivativeFunction;
			break;
		case BINARY:
			ACTIVATIONFUNCTION = binaryFunction;
			ACTIVATIONDERIVATIVEFUNCTION = binaryDerivativeFunction;
			break;
		case LINEAR:
			ACTIVATIONFUNCTION = linearFunction;
			ACTIVATIONDERIVATIVEFUNCTION = linearDerivativeFunction;
			break;
		case SOFTPLUS:
			ACTIVATIONFUNCTION = softPlusFunction;
			ACTIVATIONDERIVATIVEFUNCTION = softPlusDerivativeFunction;
			break;
		default:
			ACTIVATIONFUNCTION = sigmoidFunction;
			ACTIVATIONDERIVATIVEFUNCTION = sigmoidDerivativeFunction;
			break;
		}
	}
	
	public float[] FeedForward(float[] inputArray, PrintResult printResult) {
		Matrix inputValues = Matrix.FromArray(inputArray);
		
		Matrix[] neuronValues = new Matrix[weightsMatricies.length];
		
		// calculates the values of the nodes, weighted sum, added bias and normalized sigmoid
		neuronValues[0] = Matrix.MatrixProduct(weightsMatricies[0], inputValues);
		neuronValues[0].add(biasMatricies[0]);
		neuronValues[0].map(ACTIVATIONFUNCTION);
		for (int i = 1; i < neuronValues.length; i++) {
			neuronValues[i] = Matrix.MatrixProduct(weightsMatricies[i], neuronValues[i-1]);
			neuronValues[i].add(biasMatricies[i]);
			if(i != neuronValues.length-1) {
				neuronValues[i].map(ACTIVATIONFUNCTION);
			}else {
				neuronValues[i].map(sigmoidFunction);
			}
			
		}
		
		// Placing Values in Neuron Activation Matrices for external speculation
		neuronActivationMatricies[0] = inputValues;
		for (int i = 1; i < neuronValues.length; i++) {
			neuronActivationMatricies[i+1] = neuronValues[i];
		}
		
		if (printResult ==PrintResult.PRETTYPRINT) {
			String[] labels = new String[Matrix.Transpose(neuronValues[neuronValues.length-1]).data[0].length];
			for (int i = 0; i < Matrix.Transpose(neuronValues[neuronValues.length-1]).data[0].length; i++) {
				labels[i] = Integer.toString(i);
			}
			FormatResult("Neural Network Output", labels, Matrix.Transpose(neuronValues[neuronValues.length-1]).data[0], 7);
		}else if (printResult ==PrintResult.UGLYPRINT) {
			String strOutput = "";
			for (int i = 0; i < Matrix.Transpose(neuronValues[neuronValues.length-1]).data[0].length; i++) {
				strOutput += Float.toString(Matrix.Transpose(neuronValues[neuronValues.length-1]).data[0][i]);
			}
			System.out.println(strOutput);
		}
		
		// returning the calculated outputs
		return neuronValues[neuronValues.length-1].ToArray();
	}
	
	public void Train(float[][] allInputsArray, float[][] allTargetsArray, int batchSize, int itterations, PrintProgress printProgress) {
		BATCHSIZE = batchSize;
		
		for (int trainingItterations = 0; trainingItterations < itterations; trainingItterations++) {
			for (int n = 0; n < allInputsArray.length-batchSize; n = n+batchSize) {
				
				float[][] inputs = new float[batchSize][allInputsArray[n].length];
				float[][] targets = new float[batchSize][allTargetsArray[n].length];
				
				for (int i = 0; i < batchSize; i++) {
					for (int f = 0; f < allInputsArray[n].length; f++) {
						inputs[i][f] = allInputsArray[n+i][f];
					}
					
					for (int f = 0; f < targets[i].length; f++) {
						targets[i][f] = allTargetsArray[n+i][f];
					}
				}
				
				GradientDescent(inputs, targets);
				
				if ((printProgress == PrintProgress.PRINTPROGRESS) && ((n % ((int)(allInputsArray.length/100))) == 0)) {
					System.out.println("Training: " + Integer.toString((int)(((float)n/(float)allInputsArray.length)*100)) + "%");
				}
			}
			if (printProgress == PrintProgress.PRINTPROGRESS){
				System.out.println("Completed  iteration " + (trainingItterations+1) + " out of " + itterations);
			}
		}
		
	}
	
	public void GradientDescent(float[][] inputsArray, float[][] targetsArray) {
		
		int batchSize = inputsArray.length;
		BATCHSIZE = batchSize;
		
		Matrix[] weightGradientMatrices = new Matrix[weightsMatricies.length];
		for (int i = 0; i < weightGradientMatrices.length; i++) {
			weightGradientMatrices[i] = new Matrix(weightsMatricies[i].row, weightsMatricies[i].col);
		}
		Matrix[] biasGradientMatricies = new Matrix[biasMatricies.length];
		for (int i = 0; i < biasMatricies.length; i++) {
			biasGradientMatricies[i] = new Matrix(biasMatricies[i].row, biasMatricies[i].col);
		}
		
		for (int batch = 0; batch < batchSize; batch++) {
			// -------------------Feed Forward Algorithm----------------------
			Matrix inputValues = Matrix.FromArray(inputsArray[batch]);
			Matrix targetValues = Matrix.FromArray(targetsArray[batch]);
			
			Matrix[] neuronValues = new Matrix[weightsMatricies.length];
			Matrix[] neuronValuesSansActivation = new Matrix[weightsMatricies.length];
			
			// calculates the values of the nodes, weighted sum, added bias and normalized sigmoid
			neuronValues[0] = Matrix.MatrixProduct(weightsMatricies[0], inputValues);
			neuronValues[0].add(biasMatricies[0]);
			neuronValues[0].map(ACTIVATIONFUNCTION);
			neuronValuesSansActivation[0] = Matrix.MatrixProduct(weightsMatricies[0], inputValues);
			neuronValuesSansActivation[0].add(biasMatricies[0]);
			for (int i = 1; i < neuronValues.length; i++) {
				neuronValues[i] = Matrix.MatrixProduct(weightsMatricies[i], neuronValues[i-1]);
				neuronValues[i].add(biasMatricies[i]);
				if(i != neuronValues.length-1) {
					neuronValues[i].map(ACTIVATIONFUNCTION);
				}else {
					neuronValues[i].map(sigmoidFunction);
				}
				neuronValuesSansActivation[i] = Matrix.MatrixProduct(weightsMatricies[i], neuronValues[i-1]);
				neuronValuesSansActivation[i].add(biasMatricies[i]);
			}
			
			// -------------------Gradient Calculations-----------------------
			// C/wL = (2(aL-y)(derSig(zL)))0T(aL-1)
			// C/wL-j = (derSig(zL-1)(C/aL-j))0T(aL-j-1)
			// C/aL = sum(wL*derSig(zL)2(aL-yL))
			// C/aL-j = sum(wL-j*derSig(zL)C/aL-j+1)
			// C/bL = derSig(zL)2(aL-yL)
			// C/bL-j = derSig(zL-j)(aL-j-1)
			
			// Matrix Creation
			Matrix[] curWeightGradientMatrices = new Matrix[weightsMatricies.length];
			for (int i = 0; i < curWeightGradientMatrices.length; i++) {
				curWeightGradientMatrices[i] = new Matrix(weightsMatricies[i].row, weightsMatricies[i].col);
			}
			Matrix[] curBiasGradientMatricies = new Matrix[biasMatricies.length];
			for (int i = 0; i < curBiasGradientMatricies.length; i++) {
				curBiasGradientMatricies[i] = new Matrix(biasMatricies[i].row, biasMatricies[i].col);
			}
			
			Matrix preCurWeightGradientMatrices = new Matrix(neuronValues[neuronValues.length-1].row, neuronValues[neuronValues.length-1].col);
			
			Matrix curNeuronActivationGradient[] = new Matrix[neuronValues.length];
			for (int i = 0; i < curNeuronActivationGradient.length; i++) {
				curNeuronActivationGradient[i] = new Matrix(neuronValues[i].row, neuronValues[i].col);
			}
			
			// Matrix Mathematics
			
			// ERROR RELATED CALCULATIONS 
			
			// --Weights--
			// C/wL = (aL-y)
			preCurWeightGradientMatrices = Matrix.Subtract(neuronValues[neuronValues.length-1], targetValues);
			// C/wL = 2(aL-y)
			preCurWeightGradientMatrices.multiply(2.0f);
			// C/wL = 2(aL-y)(derSig(zL)
			preCurWeightGradientMatrices.multiply((Matrix.Map(neuronValuesSansActivation[neuronValuesSansActivation.length-1], ACTIVATIONDERIVATIVEFUNCTION)));
			// C/wL = (2(aL-y)(derSig(zL)))0T(aL-1)
			curWeightGradientMatrices[curWeightGradientMatrices.length-1] = Matrix.MatrixProduct(preCurWeightGradientMatrices, (Matrix.Transpose(neuronValues[neuronValues.length-2])));
			
			// --Bias--
			// C/bL = (aL-y)
			curBiasGradientMatricies[curBiasGradientMatricies.length-1] = Matrix.Subtract(neuronValues[neuronValues.length-1], targetValues);
			// C/bL = 2(aL-y)
			curBiasGradientMatricies[curBiasGradientMatricies.length-1].multiply(2.0f);
			// C/bL = 2(aL-y)(derSig(zL)
			curBiasGradientMatricies[curBiasGradientMatricies.length-1].multiply((Matrix.Map(neuronValuesSansActivation[neuronValuesSansActivation.length-1], ACTIVATIONDERIVATIVEFUNCTION)));
			
			// --Activations--
			// C/aL = sum(wL*derSig(zL)2(aL-yL))
			// Element wise loop
			for (int k = 0; k < curNeuronActivationGradient[curNeuronActivationGradient.length-1].row; k++) {
				// Summation loop
				for (int i = 0; i < neuronValues[neuronValues.length-1].row; i++) {
					float sumValue = 0.0f;
					// This is calculated each time wL*derSig(zL)2(aL-yL)
					sumValue = weightsMatricies[weightsMatricies.length-1].data[i][k];
					sumValue = sumValue*((Matrix.Map(neuronValuesSansActivation[neuronValuesSansActivation.length-1], ACTIVATIONDERIVATIVEFUNCTION)).data[i][0]);
					sumValue = sumValue*(Matrix.Subtract(neuronValues[neuronValues.length-1], targetValues).data[i][0]);
					// Then added as a sum to the final
					curNeuronActivationGradient[curNeuronActivationGradient.length-1].data[k][0] += sumValue;
				}
			}
			
			
			// BACKPROPAGATION 
			
			//--Activations--
			for (int j = 2; j < neuronValues.length+1; j++) { // j starts at two because the first one is already calculated and -1 is index
				// Element wise loop
				for (int k = 0; k < curNeuronActivationGradient[curNeuronActivationGradient.length-j].row; k++) {
					// Summation loop
					for (int i = 0; i < neuronValues[neuronValues.length-j+1].row; i++) {
						float sumValue = 0.0f;
						// Value to be added to the slope wL*derSig(zL)2(aL-yL)
						sumValue = weightsMatricies[weightsMatricies.length-j].data[i][k];
						sumValue = sumValue*((Matrix.Map(neuronValuesSansActivation[neuronValuesSansActivation.length-j], ACTIVATIONDERIVATIVEFUNCTION)).data[k][0]);
						sumValue = sumValue*(curNeuronActivationGradient[curNeuronActivationGradient.length-j+1].data[i][0]);
						
						curNeuronActivationGradient[curNeuronActivationGradient.length-j].data[k][0] += sumValue;
					}
				}
			}
			
			
			// --Weights--
			for (int j = 2; j < curWeightGradientMatrices.length; j++) {
				// C/wL-j = (derSig(zL-1)(C/aL-j))0T(aL-j-1)
				preCurWeightGradientMatrices = curNeuronActivationGradient[curNeuronActivationGradient.length-j];
				preCurWeightGradientMatrices.multiply(Matrix.Map(neuronValuesSansActivation[neuronValuesSansActivation.length-j], ACTIVATIONDERIVATIVEFUNCTION));
				curWeightGradientMatrices[curWeightGradientMatrices.length-j] = Matrix.MatrixProduct(preCurWeightGradientMatrices, Matrix.Transpose(neuronValues[neuronValues.length-j]));
			}
			preCurWeightGradientMatrices = curNeuronActivationGradient[0];
			preCurWeightGradientMatrices.multiply(Matrix.Map(inputValues, ACTIVATIONDERIVATIVEFUNCTION));
			curWeightGradientMatrices[0] = Matrix.MatrixProduct(preCurWeightGradientMatrices, Matrix.Transpose(inputValues));
			
			// --Bias--
			for (int j = 2; j < curBiasGradientMatricies.length; j++) {
				// C/bL-j = derSig(zL-j)(aL-j-1)
				curBiasGradientMatricies[curBiasGradientMatricies.length-j] = curNeuronActivationGradient[curNeuronActivationGradient.length-j];
				curBiasGradientMatricies[curBiasGradientMatricies.length-j].multiply(Matrix.Map(neuronValues[neuronValues.length-j], ACTIVATIONDERIVATIVEFUNCTION));
			}
			curBiasGradientMatricies[0] = curNeuronActivationGradient[0];
			curBiasGradientMatricies[0].multiply(Matrix.Map(inputValues, ACTIVATIONDERIVATIVEFUNCTION));
			
			// -----------------ALL Gradients Have Been Calculated------------------
			
			// ADDING FOR AVERAGE WITH BATCH
			for (int i = 0; i < weightGradientMatrices.length; i++) {
				weightGradientMatrices[i].add(curWeightGradientMatrices[i]);
			}
			for (int i = 0; i < biasGradientMatricies.length; i++) {
				biasGradientMatricies[i].add(curBiasGradientMatricies[i]);
			}
		}
		// AVERAGING FOR BATCH
		for (int i = 0; i < weightGradientMatrices.length; i++) {
			weightGradientMatrices[i].multiply(1.0f/batchSize);
		}
		for (int i = 0; i < biasGradientMatricies.length; i++) {
			biasGradientMatricies[i].multiply(1.0f/batchSize);
		}
		
		// APPLY LEARNINGRATE
		for (int i = 0; i < weightGradientMatrices.length; i++) {
			weightGradientMatrices[i].multiply(LEARNINGRATE);
		}
		for (int i = 0; i < biasGradientMatricies.length; i++) {
			biasGradientMatricies[i].multiply(LEARNINGRATE);
		}
		
		// ------------------Tweaking Weights and Biases Based on Averaged Gradients------------------
		for (int i = 0; i < weightsMatricies.length; i++) {
			weightsMatricies[i].subtract(weightGradientMatrices[i]);
		}
		for (int i = 0; i < biasMatricies.length; i++) {
			biasMatricies[i].subtract(biasGradientMatricies[i]);
		}
	}
	
	public void Test(float[][] inputsArray, float[][] targetsArray, PrintProgress printProgress, ResultType choice, PrintResult prettyResult) {
		float averageSureness = 0.0f;
		float correctness = 0.0f;
		int correctOnes = 0;
		float cost = 0.0f;
		float[] averageError = new float[targetsArray[0].length];
		
		for (int n = 0; n < inputsArray.length; n++) {
			float[] outputs = new float[10];
			
			outputs = FeedForward(inputsArray[n], PrintResult.DONTPRINT);
			int strongestChoice = -1;
			int targetChoice = -1;
			float sureness = 0.00f;
			float targetSureness = 0.00f;
			for (int f = 0; f < outputs.length; f++) {
				if (outputs[f]>sureness) {
					strongestChoice = f;
					sureness = outputs[f];
				}
				if (targetsArray[n][f]>targetSureness) {
					targetChoice = f;
					targetSureness = targetsArray[n][f];
				}
				cost += Math.pow((outputs[f]- (float)targetsArray[n][f]), 2);
				averageError[f] += (float) Math.pow((outputs[f]- (float)targetsArray[n][f]), 2);
			}
			if (strongestChoice == targetChoice) {
				correctness = correctness + 1.00f;
				averageSureness += sureness;
				correctOnes++;
			}
			
			if(printProgress == PrintProgress.PRINTPROGRESS) {
				if ((n % ((int)(inputsArray.length/100))) == 0) {
					System.out.println("Testing: " + Integer.toString((int)(((float)n/(float)inputsArray.length)*100)) + "%");
				}
			}
		}
		
		correctness = correctness / inputsArray.length;
		averageSureness = averageSureness / correctOnes;
		correctness = correctness * 100.00f;
		averageSureness = averageSureness * 100.00f;
		
		if (choice == ResultType.CHOICE) {
			if (prettyResult == PrintResult.PRETTYPRINT) {
				FormatResult("TESTING COMPLETE", new String[] {"Learning Rate", "Batch Size", "Average Correctness", "Average Sureness", "Cost"},
						new float[] {LEARNINGRATE, BATCHSIZE, correctness, averageSureness, cost}, 7);
			}else if (prettyResult == PrintResult.UGLYPRINT) {
				System.out.println(BATCHSIZE + "\t" + LEARNINGRATE + "\t" + correctness + "\t" + averageSureness + "\t" + cost);
			}
		}else {
			if (prettyResult == PrintResult.PRETTYPRINT) {
				FormatResult("TESTING COMPLETE", new String[] {"Learning Rate", "Batch Size", "Cost"},
						new float[] {LEARNINGRATE, BATCHSIZE, cost}, 7);
				String[] labels = new String[targetsArray[0].length];
				for (int i = 0; i < targetsArray[0].length; i++) {
					labels[i] = Integer.toString(i);
				}
				FormatResult("Error By Output", labels, averageError, 5);
			}else if (prettyResult == PrintResult.UGLYPRINT) {
				System.out.println(BATCHSIZE + "\t" + LEARNINGRATE + "\t" + cost);
				for (int i = 0; i < averageError.length; i++) {
					averageError[i] = averageError[i]/inputsArray.length;
					System.out.print("\t" + averageError);
				}
				System.out.println();
			}
		}
	}
	
	public enum Activation {
		SIGMOID,
		RELU,
		SILU,
		BINARY,
		LINEAR,
		SOFTPLUS,
	}
	
	public enum PrintProgress {
		PRINTPROGRESS,
		DONTPRINTPROGRESS
	}
	
	public enum PrintResult {
		DONTPRINT,
		PRETTYPRINT,
		UGLYPRINT,
	}
	
	public enum ResultType {
		CHOICE,
		CALCULATION,
	}
	
	public void FormatResult(String title, String[] labels, float[] values, int places) {
		places = Math.max(places, 3);
		if (labels.length == values.length) {
			int longestLabel = -1;
			for (int i = 0; i < labels.length; i++) {
				if (labels[i].length() > longestLabel) {
					longestLabel = labels[i].length();
				}
			}
			title = title.replace(' ', '-');
			int width = Math.max(title.length()+4, (longestLabel + places+8));
			String bottomCap = "|";
			String topCap = "|";
			String spacer = "|";
			for (int i = 0; i < width-2; i++) {
				bottomCap += "-";
				spacer += " ";
			}
			for (int i = 0; i < width-2; i++) {
				if(i == (width-title.length()-2)/2) {
					topCap += title;
					i = i + title.length()-1;
				}else {
					topCap += "-";
				}
			}
			spacer += "|";
			topCap += "|";
			bottomCap += "|";
			System.out.println(topCap);
			System.out.println(spacer);
			for (int i = 0; i < values.length; i++) {
				String strValue = Float.toString(values[i]);
				String outputString = "-1";
				if (strValue.contains("E")) {
					// Scientific Notation
					int exponent = Integer.parseInt(strValue.substring((strValue.indexOf("E")+1), strValue.length()));
					if (exponent < -(places-2)) {
						// Very small number 
						outputString = "0.";
						for (int j = 0; j < places-2; j++) {
							outputString += "0";
						}
					}else if (exponent > (places-2)) {
						// Very Large Number
						// find the substring starting at the begining and ending either where the E is or places
						// checks if the exponent is greater that 10, 2 places must be reserved
						outputString = strValue.substring(0, Math.min(strValue.indexOf("E"), (places-3 - (((exponent>=10)) ? 1 : 0))));
						outputString += "E";
						outputString += Integer.toString(exponent);
						if (outputString.length() < (places-3 - ((exponent>=10) ? 1 : 0) - ((outputString.charAt(0)=='-') ? 1 : 0))) {
							outputString += " ";
						}
					}
					else if (exponent < 0) {
						// medium small number
						outputString = strValue.substring(0 + ((strValue.charAt(0)=='-') ? 1 : 0), 1 + ((strValue.charAt(0)=='-') ? 1 : 0));
						outputString = outputString + strValue.substring(2 + ((strValue.charAt(0)=='-') ? 1 : 0), strValue.indexOf("E"));
						for (int j = 0; j < Math.abs(exponent); j++) {
							outputString = "0" + outputString;
						}
						outputString = "0." + outputString;
						if (outputString.charAt(0)=='-') {
							outputString  = "-" + outputString;
						}
						outputString = outputString.substring(0, places);
					}else if (exponent > 0) {
						// medium large number
						outputString = strValue.substring(0, 1);
						outputString = outputString + strValue.substring(2, strValue.indexOf("E"));
						for (int j = 0; j < exponent; j++) {
							outputString = outputString + "0";
						}
						outputString = outputString.substring(0, places);
					}
				}else {
					// Not Scientific Notation
					if (strValue.indexOf('.') > (places-1)) {
						// Larger Number
						int exponent = strValue.indexOf('.')-1;
						outputString = strValue.substring(0, 1) + "." + strValue.substring(2, strValue.indexOf('.')) + strValue.substring(strValue.indexOf('.')+1, strValue.length());
						outputString = outputString.substring(0, places-2);
						outputString = outputString + "E" + exponent;
					}
					else {
						// Smaller Number
						outputString = strValue;
						for (int j = 0; j < (places - strValue.length()) ; j++) {
							outputString = outputString + "0";
						}
						
					}
					//outputString = strValue.substring(0, places);
				}
				String valueLine = "|";
				for (int j = 0; j < (width - 4); j++) {
					int textWidth = places + labels[i].length() + 2;
					if (j == (int)((width/2) - (textWidth/2)-1)) {
						valueLine += labels[i] + ": " + outputString;
						j = j + labels[i].length() + outputString.length();
					}
					valueLine += " ";
				}
				valueLine += "|";
				System.out.println(valueLine);
				System.out.println(spacer);
			}
			System.out.println(bottomCap);
		}
		else {
			System.out.println("Error: Format result label array value array mismatched dimensions.");
		}
	}
}

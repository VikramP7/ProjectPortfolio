import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class NeuralNetworkV3 {
	private final int NumberOfInputNodes;
	private final int[] HiddenNodes;
	private final int NumberOfOutputNodes;
	private Matrix[] weightsMatricies;
	private Matrix[] biasMatricies;
	public Matrix[] neuronActivationMatricies; // Just used for external speculation, no calculations are done to this or from this

	private float LEARNINGRATE = 8.5f;
	private float MUTATIONRATE = 0.15f;

	public final Function<Float, Float> sigmoindFunction = x -> (1 / (1 + (float) Math.exp(-x)));
	public final Function<Float, Float> fakeDSigmoindFunction = x -> x * (1 - x);
	public final Function<Float, Float> SigmoidDerivativeFunction = x -> (float) ((Math.exp(-x))
			/ (Math.pow((1 + Math.exp(-x)), 2)));

	// seeded by curent time
	public NeuralNetworkV3(int inputs, int[] hidden, int output) {
		this.NumberOfInputNodes = inputs;
		this.HiddenNodes = hidden;
		this.NumberOfOutputNodes = output;

		weightsMatricies = new Matrix[hidden.length + 1];
		weightsMatricies[0] = new Matrix(hidden[0], inputs);
		weightsMatricies[0].RandomFill();
		for (int i = 1; i < weightsMatricies.length - 1; i++) {
			weightsMatricies[i] = new Matrix(hidden[i], hidden[i - 1]);
			weightsMatricies[i].RandomFill();
		}
		weightsMatricies[weightsMatricies.length - 1] = new Matrix(output, hidden[hidden.length - 1]);
		weightsMatricies[weightsMatricies.length - 1].RandomFill();

		biasMatricies = new Matrix[hidden.length + 1];
		for (int i = 0; i < hidden.length; i++) {
			biasMatricies[i] = new Matrix(hidden[i], 1);
			biasMatricies[i].RandomFill();
		}
		biasMatricies[biasMatricies.length - 1] = new Matrix(output, 1);
		biasMatricies[biasMatricies.length - 1].RandomFill();

		neuronActivationMatricies = new Matrix[1 + hidden.length + 1]; // One for input layer, one for each hidden and one for ouput
		neuronActivationMatricies[0] = new Matrix(inputs, 1);
		for (int i = 0; i < hidden.length; i++) {
			neuronActivationMatricies[i + 1] = new Matrix(hidden[i], 1);
		}
		neuronActivationMatricies[neuronActivationMatricies.length - 1] = new Matrix(output, 1);
	}

	// seeded by provided seed
	public NeuralNetworkV3(int inputs, int[] hidden, int output, long seed) {
		this.NumberOfInputNodes = inputs;
		this.HiddenNodes = hidden;
		this.NumberOfOutputNodes = output;
		Random random = new Random(seed);

		weightsMatricies = new Matrix[hidden.length + 1];
		weightsMatricies[0] = new Matrix(hidden[0], inputs);
		weightsMatricies[0].RandomFill(random.nextLong());
		for (int i = 1; i < weightsMatricies.length - 1; i++) {
			weightsMatricies[i] = new Matrix(hidden[i], hidden[i - 1]);
			weightsMatricies[i].RandomFill(random.nextLong());
		}
		weightsMatricies[weightsMatricies.length - 1] = new Matrix(output, hidden[hidden.length - 1]);
		weightsMatricies[weightsMatricies.length - 1].RandomFill(random.nextLong());

		biasMatricies = new Matrix[hidden.length + 1];
		for (int i = 0; i < hidden.length; i++) {
			biasMatricies[i] = new Matrix(hidden[i], 1);
			biasMatricies[i].RandomFill(random.nextLong());
		}
		biasMatricies[biasMatricies.length - 1] = new Matrix(output, 1);
		biasMatricies[biasMatricies.length - 1].RandomFill(random.nextLong());

		neuronActivationMatricies = new Matrix[1 + hidden.length + 1]; // One for input layer, one for each hidden and one for ouput
		neuronActivationMatricies[0] = new Matrix(inputs, 1);
		for (int i = 0; i < hidden.length; i++) {
			neuronActivationMatricies[i + 1] = new Matrix(hidden[i], 1);
		}
		neuronActivationMatricies[neuronActivationMatricies.length - 1] = new Matrix(output, 1);
	}

	// non mutable Neural network recreation
	public NeuralNetworkV3(NeuralNetworkV3 nn) {
		LEARNINGRATE = nn.GetLearningRate();
		MUTATIONRATE = nn.GetMutationRate();

		int[] structure = nn.GetStructure();
		int[] hidden = new int[structure.length - 2];
		for (int i = 0; i < hidden.length; i++) {
			hidden[i] = structure[i+1];
		}
		this.NumberOfInputNodes = structure[0];
		this.HiddenNodes = hidden;
		this.NumberOfOutputNodes = structure[structure.length-1];

		weightsMatricies = nn.GetWeights();
		biasMatricies = nn.GetBiases();

		neuronActivationMatricies = new Matrix[1 + hidden.length + 1]; // One for input layer, one for each hidden and one for ouput
		neuronActivationMatricies[0] = new Matrix(this.NumberOfInputNodes, 1);
		for (int i = 0; i < hidden.length; i++) {
			neuronActivationMatricies[i + 1] = new Matrix(hidden[i], 1);
		}
		neuronActivationMatricies[neuronActivationMatricies.length - 1] = new Matrix(this.NumberOfOutputNodes, 1);
	}

	// import essentially creates a neural network based on a json file
	public NeuralNetworkV3(String directoryPath, String name) {
		int inputs;
		int[] hidden;
		int outputs;
		// -----Imports the Data from File-----
		List<String> lines = new ArrayList<String>();
		String path = directoryPath + "\\" + name + ".json";
		try {
			lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("--Error durring file save--");
		}

		// condense down to one line
		String line = "";
		for (int i = 0; i < lines.size(); i++) {
			line += lines.get(i);
		}

		String curValue = "";

		int structureIndex = line.indexOf("structure") + 13;
		int weightsIndex = line.indexOf("weights") + 11;
		int biasesIndex = line.indexOf("biases") + 10;
		int characterIndex = structureIndex;
		boolean findingStructure = true;
		List<Integer> struct = new ArrayList<Integer>();
		while (findingStructure) {
			char curChar = line.charAt(characterIndex);
			if (curChar == ' ') {
				// it must be a new number
				curValue = "";
			} else if (curChar == ']') {
				// Done the stucture and ended curent value
				try {
					struct.add(Integer.parseInt(curValue));
				} catch (NumberFormatException e) {
					System.out.println("Integer Parse Error");
					e.printStackTrace();
				}
				curValue = "";
				findingStructure = false;
			} else if (curChar == ',') {
				// ended curent value
				try {
					struct.add(Integer.parseInt(curValue));
				} catch (NumberFormatException e) {
					System.out.println("Integer Parse Error");
					e.printStackTrace();
				}
				curValue = "";
			} else {
				// character must be either a number or a dot
				curValue += curChar;
			}
			characterIndex++;
		}

		inputs = struct.get(0);
		outputs = struct.get(struct.size() - 1);
		hidden = new int[struct.size() - 2];
		for (int i = 0; i < hidden.length; i++) {
			hidden[i] = struct.get(i + 1);
		}

		this.NumberOfInputNodes = inputs;
		this.HiddenNodes = hidden;
		this.NumberOfOutputNodes = outputs;

		// moves current index past weight lable ex "0": 
		characterIndex = line.substring(weightsIndex).indexOf("\"0") + 4 + weightsIndex;

		weightsMatricies = new Matrix[hidden.length + 1];
		weightsMatricies[0] = new Matrix(hidden[0], inputs, line.substring(characterIndex));
		characterIndex = line.substring(weightsIndex).indexOf("\"1") + 4 + weightsIndex;
		for (int i = 1; i < weightsMatricies.length - 1; i++) {
			weightsMatricies[i] = new Matrix(hidden[i], hidden[i - 1], line.substring(characterIndex));
			characterIndex = line.substring(weightsIndex).indexOf("\"" + (i + 1)) + 4 + weightsIndex;
		}
		weightsMatricies[weightsMatricies.length - 1] = new Matrix(outputs, hidden[hidden.length - 1],
				line.substring(characterIndex));

		characterIndex = line.substring(biasesIndex).indexOf("\"0") + 4 + biasesIndex;
		biasMatricies = new Matrix[hidden.length + 1];
		for (int i = 0; i < hidden.length; i++) {
			biasMatricies[i] = new Matrix(hidden[i], 1, line.substring(characterIndex));
			characterIndex = line.substring(biasesIndex).indexOf("\"" + (i + 1)) + 4 + biasesIndex;
		}
		biasMatricies[biasMatricies.length - 1] = new Matrix(outputs, 1, line.substring(characterIndex));

		neuronActivationMatricies = new Matrix[1 + hidden.length + 1]; // One for input layer, one for each hidden and one for ouput
		neuronActivationMatricies[0] = new Matrix(inputs, 1);
		for (int i = 0; i < hidden.length; i++) {
			neuronActivationMatricies[i + 1] = new Matrix(hidden[i], 1);
		}
		neuronActivationMatricies[neuronActivationMatricies.length - 1] = new Matrix(outputs, 1);
	}

	// exports any neural network to a json file to be used in constructor above for recreation
	public void Export(String directoryPath, String name) {
		try {
			File save = new File(directoryPath + "\\" + name + ".json");
			if (!save.createNewFile()) {
				System.out.println("File already exist, overwiting file!");
			}
			FileWriter writer = new FileWriter(save);
			writer.write("{\"structure\": [" + NumberOfInputNodes);
			for (int i = 0; i < HiddenNodes.length; i++) {
				writer.write(", " + HiddenNodes[i]);
			}
			writer.write(", " + NumberOfOutputNodes + "],");
			writer.write("\"weights\": {");
			for (int i = 0; i < weightsMatricies.length; i++) {
				writer.write("\"" + i + "\": ");
				writer.write(weightsMatricies[i].ToString());
				if (i != this.weightsMatricies.length - 1) {
					writer.write(",");
				}
			}
			writer.write("},");
			writer.write("\"biases\": {");
			for (int i = 0; i < biasMatricies.length; i++) {
				writer.write("\"" + i + "\": ");
				writer.write(biasMatricies[i].ToString());
				if (i != this.biasMatricies.length - 1) {
					writer.write(",");
				}
			}
			writer.write("}}");
			writer.close();
		} catch (IOException e) {
			System.out.println("--Error durring file save--");
			e.printStackTrace();
		}
	}
	
	public void SetLearningRate(float learningRate) {
		this.LEARNINGRATE = learningRate;
	}

	public float GetLearningRate() {
		return LEARNINGRATE;
	}

	public void SetMutationRate(float learningRate) {
		this.MUTATIONRATE = learningRate;
	}

	public float GetMutationRate() {
		return MUTATIONRATE;
	}

	public int[] GetStructure() {
		// returns a integer array for the structure of the neural network
		// {inputs, hiddenlayer1, hiddenLayer2, etc..., outputs}
		int[] structure = new int[1 + HiddenNodes.length + 1];
		structure[0] = NumberOfInputNodes;
		for (int i = 0; i < HiddenNodes.length; i++) {
			structure[i+1] = HiddenNodes[i];
		}
		structure[structure.length - 1] = NumberOfOutputNodes;
		return structure;
	}
	
	public Matrix[] GetWeights() {
		Matrix[] weights = new Matrix[weightsMatricies.length];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = new Matrix(weightsMatricies[i]);
		}
		return weights;
	}

	public Matrix[] GetBiases() {
		Matrix[] biases = new Matrix[biasMatricies.length];
		for (int i = 0; i < biases.length; i++) {
			biases[i] = new Matrix(biasMatricies[i]);
		}
		return biases;
	}

	public void PrintNetwork() {
		for (int i = 0; i < weightsMatricies.length; i++) {
			System.out.println("Layer: " + i);
			System.out.println("Weights: ");
			weightsMatricies[i].show();
			System.out.println("Bias: ");
			biasMatricies[i].show();
		}
	}

	// for genetic algorithms
	public void Mutate(long seed) {
		Random random = new Random(seed);
		for (int i = 0; i < weightsMatricies.length; i++) {
			for (int x = 0; x < weightsMatricies[i].col; x++) {
				for (int y = 0; y < weightsMatricies[i].row; y++) {
					float mutation = random.nextFloat() * MUTATIONRATE;
					if (random.nextInt(2) == 0) {
						mutation = mutation * -1;
					}
					if ((weightsMatricies[i].data[y][x] + mutation) > 0) {
						weightsMatricies[i].data[y][x] = Math.min((weightsMatricies[i].data[y][x] + mutation), 1);
					} else {
						weightsMatricies[i].data[y][x] = Math.max((weightsMatricies[i].data[y][x] + mutation), -1);
					}
				}
			}
			for (int b = 0; b < biasMatricies[i].row; b++) {
				float mutation = random.nextFloat() * MUTATIONRATE;
				if (random.nextInt(2) == 0) {
					mutation = mutation * -1;
				}
				if ((biasMatricies[i].data[b][0] + mutation) > 0) {
					biasMatricies[i].data[b][0] = Math.min((biasMatricies[i].data[b][0] + mutation), 1);
				} else {
					biasMatricies[i].data[b][0] = Math.max((biasMatricies[i].data[b][0] + mutation), -1);
				}
			}
		}
	}

	public float[] FeedForward(float[] inputArray) {
		Matrix inputValues = Matrix.FromArray(inputArray);

		Matrix[] hiddenNodeValues = new Matrix[weightsMatricies.length - 1];

		// calculates the values of the nodes, weighted sum, added bias and normalized
		// sigmoid
		hiddenNodeValues[0] = Matrix.MatrixProduct(weightsMatricies[0], inputValues);
		hiddenNodeValues[0].add(biasMatricies[0]);
		hiddenNodeValues[0].map(sigmoindFunction);
		for (int i = 1; i < hiddenNodeValues.length; i++) {
			hiddenNodeValues[i] = Matrix.MatrixProduct(weightsMatricies[i], hiddenNodeValues[i - 1]);
			hiddenNodeValues[i].add(biasMatricies[i]);
			hiddenNodeValues[i].map(sigmoindFunction);
		}
		Matrix outputValues = Matrix.MatrixProduct(this.weightsMatricies[weightsMatricies.length - 1],
				hiddenNodeValues[hiddenNodeValues.length - 1]);
		outputValues.add(biasMatricies[biasMatricies.length - 1]);
		outputValues.map(sigmoindFunction);

		// Placing Values in Neuron Activation Matrices for external speculation
		neuronActivationMatricies[0] = inputValues;
		for (int i = 0; i < hiddenNodeValues.length; i++) {
			neuronActivationMatricies[i + 1] = hiddenNodeValues[i];
		}
		neuronActivationMatricies[neuronActivationMatricies.length - 1] = outputValues;

		// returning the calculated outputs
		return outputValues.ToArray();
	}

	public void Train(float[][] inputsArray, float[][] targetsArray) {

		int batchItemCount = inputsArray.length; // number of items in one batch

		Matrix[] weightGradientMatrices = new Matrix[weightsMatricies.length];
		for (int i = 0; i < weightGradientMatrices.length; i++) {
			weightGradientMatrices[i] = new Matrix(weightsMatricies[i].row, weightsMatricies[i].col);
		}
		Matrix[] biasGradientMatricies = new Matrix[biasMatricies.length];
		for (int i = 0; i < biasMatricies.length; i++) {
			biasGradientMatricies[i] = new Matrix(biasMatricies[i].row, biasMatricies[i].col);
		}

		for (int batch = 0; batch < batchItemCount; batch++) {
			// -------------------Feed Forward Algorithm----------------------
			Matrix inputValues = Matrix.FromArray(inputsArray[batch]);
			Matrix targetValues = Matrix.FromArray(targetsArray[batch]);

			Matrix[] hiddenNodeValues = new Matrix[weightsMatricies.length - 1];
			Matrix[] hiddenNodeValuesSansSigmoid = new Matrix[weightsMatricies.length - 1];

			// calculates the values of the nodes, weighted sum, added bias and normalized
			// sigmoid
			hiddenNodeValues[0] = Matrix.MatrixProduct(weightsMatricies[0], inputValues);
			hiddenNodeValues[0].add(biasMatricies[0]);
			hiddenNodeValues[0].map(sigmoindFunction);
			hiddenNodeValuesSansSigmoid[0] = Matrix.MatrixProduct(weightsMatricies[0], inputValues);
			hiddenNodeValuesSansSigmoid[0].add(biasMatricies[0]);
			for (int i = 1; i < hiddenNodeValues.length; i++) {
				hiddenNodeValues[i] = Matrix.MatrixProduct(weightsMatricies[i], hiddenNodeValues[i - 1]);
				hiddenNodeValues[i].add(biasMatricies[i]);
				hiddenNodeValues[i].map(sigmoindFunction);
				hiddenNodeValuesSansSigmoid[i] = Matrix.MatrixProduct(weightsMatricies[i], hiddenNodeValues[i - 1]);
				hiddenNodeValuesSansSigmoid[i].add(biasMatricies[i]);
			}
			Matrix outputValues = Matrix.MatrixProduct(this.weightsMatricies[weightsMatricies.length - 1],
					hiddenNodeValues[hiddenNodeValues.length - 1]);
			outputValues.add(biasMatricies[biasMatricies.length - 1]);
			outputValues.map(sigmoindFunction);
			Matrix outputValuesSansSigmoid = Matrix.MatrixProduct(this.weightsMatricies[weightsMatricies.length - 1],
					hiddenNodeValues[hiddenNodeValues.length - 1]);
			outputValuesSansSigmoid.add(biasMatricies[biasMatricies.length - 1]);

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

			Matrix preCurWeightGradientMatrices = new Matrix(outputValues.row, outputValues.col);

			Matrix curNeuronActivationGradient[] = new Matrix[hiddenNodeValues.length];
			for (int i = 0; i < curNeuronActivationGradient.length; i++) {
				curNeuronActivationGradient[i] = new Matrix(hiddenNodeValues[i].row, hiddenNodeValues[i].col);
			}

			// Matrix Mathematics

			// ERROR RELATED CALCULATIONS

			// --Weights--
			// C/wL = (aL-y)
			preCurWeightGradientMatrices = Matrix.Subtract(outputValues, targetValues);
			// C/wL = 2(aL-y)
			preCurWeightGradientMatrices.multiply(2.0f);
			// C/wL = 2(aL-y)(derSig(zL)
			preCurWeightGradientMatrices.multiply((Matrix.Map(outputValuesSansSigmoid, SigmoidDerivativeFunction)));
			// C/wL = (2(aL-y)(derSig(zL)))0T(aL-1)
			curWeightGradientMatrices[curWeightGradientMatrices.length - 1] = Matrix.MatrixProduct(
					preCurWeightGradientMatrices, (Matrix.Transpose(hiddenNodeValues[hiddenNodeValues.length - 1])));

			// --Bias--
			// C/bL = (aL-y)
			curBiasGradientMatricies[curBiasGradientMatricies.length - 1] = Matrix.Subtract(outputValues, targetValues);
			// C/bL = 2(aL-y)
			curBiasGradientMatricies[curBiasGradientMatricies.length - 1].multiply(2.0f);
			// C/bL = 2(aL-y)(derSig(zL)
			curBiasGradientMatricies[curBiasGradientMatricies.length - 1]
					.multiply((Matrix.Map(outputValuesSansSigmoid, SigmoidDerivativeFunction)));

			// --Activations--
			// C/aL = sum(wL*derSig(zL)2(aL-yL))
			// Element wise loop
			for (int k = 0; k < curNeuronActivationGradient[curNeuronActivationGradient.length - 1].row; k++) {
				// Summation loop
				for (int i = 0; i < outputValues.row; i++) {
					float sumValue = 0.0f;
					// This is calculated each time wL*derSig(zL)2(aL-yL)
					sumValue = weightsMatricies[weightsMatricies.length - 1].data[i][k];
					sumValue = sumValue
							* ((Matrix.Map(hiddenNodeValuesSansSigmoid[hiddenNodeValuesSansSigmoid.length - 1],
									SigmoidDerivativeFunction)).data[i][0]);
					sumValue = sumValue * (Matrix.Subtract(outputValues, targetValues).data[i][0]);
					// Then added as a sum to the final
					curNeuronActivationGradient[curNeuronActivationGradient.length - 1].data[k][0] += sumValue;
				}
			}

			// BACKPROPAGATION

			// --Activations--
			for (int j = 2; j < hiddenNodeValues.length; j++) { // j starts at two because the first one is already
																// calculated and -1 is index
				for (int k = 0; k < curNeuronActivationGradient[curNeuronActivationGradient.length - j].row; k++) {
					// Summation loop
					for (int i = 0; i < outputValues.row; i++) {
						float sumValue = 0.0f;
						// Value to be added to the slope wL*derSig(zL)2(aL-yL)
						sumValue = weightsMatricies[weightsMatricies.length - j].data[i][k];
						sumValue = sumValue
								* ((Matrix.Map(hiddenNodeValuesSansSigmoid[hiddenNodeValuesSansSigmoid.length - j],
										SigmoidDerivativeFunction)).data[i][0]);
						sumValue = sumValue
								* (curNeuronActivationGradient[curNeuronActivationGradient.length - j + 1].data[i][0]);

						curNeuronActivationGradient[curNeuronActivationGradient.length - j].data[k][0] += sumValue;
					}
				}
			}

			// --Weights--
			for (int j = 2; j < curWeightGradientMatrices.length - 1; j++) {
				// C/wL-j = (derSig(zL-1)(C/aL-j))0T(aL-j-1)
				preCurWeightGradientMatrices = curNeuronActivationGradient[curNeuronActivationGradient.length - j];
				preCurWeightGradientMatrices
						.multiply(Matrix.Map(hiddenNodeValuesSansSigmoid[hiddenNodeValuesSansSigmoid.length - j],
								SigmoidDerivativeFunction));
				curWeightGradientMatrices[curWeightGradientMatrices.length - j] = Matrix.MatrixProduct(
						preCurWeightGradientMatrices,
						Matrix.Transpose(hiddenNodeValues[hiddenNodeValues.length - j - 1]));
			}
			preCurWeightGradientMatrices = curNeuronActivationGradient[0];
			preCurWeightGradientMatrices.multiply(Matrix.Map(inputValues, SigmoidDerivativeFunction));
			curWeightGradientMatrices[0] = Matrix.MatrixProduct(preCurWeightGradientMatrices,
					Matrix.Transpose(inputValues));

			// --Bias--
			for (int j = 2; j < curBiasGradientMatricies.length - 1; j++) {
				// C/bL-j = derSig(zL-j)(aL-j-1)
				curBiasGradientMatricies[curBiasGradientMatricies.length
						- j] = curNeuronActivationGradient[curNeuronActivationGradient.length - j];
				curBiasGradientMatricies[curBiasGradientMatricies.length - j]
						.multiply(Matrix.Map(hiddenNodeValues[hiddenNodeValues.length - j], SigmoidDerivativeFunction));
			}
			curBiasGradientMatricies[0] = curNeuronActivationGradient[0];
			curBiasGradientMatricies[0].multiply(Matrix.Map(inputValues, SigmoidDerivativeFunction));

			// -----------------ALL Gradients Have Been Calculated------------------
			// APPLY LEARNINGRATE
			for (int i = 0; i < curWeightGradientMatrices.length; i++) {
				curWeightGradientMatrices[i].multiply(LEARNINGRATE);
			}
			for (int i = 0; i < curBiasGradientMatricies.length; i++) {
				curBiasGradientMatricies[i].multiply(LEARNINGRATE);
			}

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
			weightGradientMatrices[i].multiply(1.0f / batchItemCount);
		}
		for (int i = 0; i < biasGradientMatricies.length; i++) {
			biasGradientMatricies[i].multiply(1.0f / batchItemCount);
		}

		// ------------------Tweaking Weights and Biases Based on Averaged
		// Gradients------------------
		for (int i = 0; i < weightsMatricies.length; i++) {
			weightsMatricies[i].subtract(weightGradientMatrices[i]);
		}
		for (int i = 0; i < biasMatricies.length; i++) {
			biasMatricies[i].subtract(biasGradientMatricies[i]);
		}
	}

	public void QuickTrain(float[] inputsArray, float[] targetsArray) {
		Matrix inputValues = Matrix.FromArray(inputsArray);

		Matrix[] hiddenNodeValues = new Matrix[weightsMatricies.length - 1];

		// calculates the values of the nodes, weighted sum, added bias and normalized
		// sigmoid
		hiddenNodeValues[0] = Matrix.MatrixProduct(weightsMatricies[0], inputValues);
		hiddenNodeValues[0].add(biasMatricies[0]);
		hiddenNodeValues[0].map(sigmoindFunction);
		for (int i = 1; i < hiddenNodeValues.length; i++) {
			hiddenNodeValues[i] = Matrix.MatrixProduct(weightsMatricies[i], hiddenNodeValues[i - 1]);
			hiddenNodeValues[i].add(biasMatricies[i]);
			hiddenNodeValues[i].map(sigmoindFunction);
		}
		Matrix outputValues = Matrix.MatrixProduct(this.weightsMatricies[weightsMatricies.length - 1],
				hiddenNodeValues[hiddenNodeValues.length - 1]);
		outputValues.add(biasMatricies[biasMatricies.length - 1]);
		outputValues.map(sigmoindFunction);

		Matrix outputTargets = Matrix.FromArray(targetsArray);

		// calculate the error
		// ERROR = TARGETS - OUTPUTS
		Matrix outputError = Matrix.Subtract(outputTargets, outputValues);

		// Creates transposed versions of the weight matrices
		Matrix[] weightsMatriciesTransposed = new Matrix[weightsMatricies.length];
		for (int i = 0; i < weightsMatriciesTransposed.length; i++) {
			weightsMatriciesTransposed[i] = Matrix.Transpose(weightsMatricies[i]);
		}

		// Calculate hidden layers errors, iterating backwards... You know, cuz back
		// propagation
		Matrix[] hiddenErrors = new Matrix[hiddenNodeValues.length];
		hiddenErrors[hiddenErrors.length - 1] = Matrix
				.MatrixProduct(weightsMatriciesTransposed[weightsMatriciesTransposed.length - 1], outputError);
		for (int i = hiddenErrors.length - 2; i > -1; i--) {
			hiddenErrors[i] = Matrix.MatrixProduct(weightsMatriciesTransposed[i + 1], hiddenErrors[i + 1]);
		}

		// Calculate the gradients for the output layer and the hidden layers
		// calculate output layer gradient
		Matrix outputGradient = Matrix.Map(outputValues, fakeDSigmoindFunction);
		outputGradient.multiply(outputError);
		outputGradient.multiply(LEARNINGRATE);
		// Calculate hidden gradients
		Matrix[] hiddenGradients = new Matrix[hiddenNodeValues.length];
		for (int i = hiddenGradients.length - 1; i > -1; i--) {
			hiddenGradients[i] = Matrix.Map(hiddenNodeValues[i], fakeDSigmoindFunction);
			hiddenGradients[i].multiply(hiddenErrors[i]);
			hiddenGradients[i].multiply(LEARNINGRATE);
		}

		// calculate output layer weight deltas
		// Transpose hidden node values matrices
		Matrix[] hiddenNodeValuesTransposed = new Matrix[hiddenNodeValues.length];
		for (int i = 0; i < hiddenNodeValuesTransposed.length; i++) {
			hiddenNodeValuesTransposed[i] = Matrix.Transpose(hiddenNodeValues[hiddenNodeValues.length - 1]);
		}

		// Calculate deltas
		Matrix[] weightsMatriciesDeltas = new Matrix[hiddenNodeValuesTransposed.length + 1];
		// Calculate the output's weights deltas
		weightsMatriciesDeltas[weightsMatriciesDeltas.length - 1] = Matrix.MatrixProduct(outputGradient,
				hiddenNodeValuesTransposed[hiddenNodeValuesTransposed.length - 1]);
		// Calculate the hidden weights deltas, iterating backwards... You know, cuz
		// back propagation
		for (int i = weightsMatriciesDeltas.length - 2; i > -1; i--) {
			weightsMatriciesDeltas[i] = Matrix.MatrixProduct(hiddenGradients[i], hiddenNodeValuesTransposed[i]);
		}

		// Using calculated Deltas change the weights and biases
		// change weights of output based on calculated deltas
		weightsMatricies[weightsMatricies.length - 1].add(weightsMatriciesDeltas[weightsMatriciesDeltas.length - 1]);
		// change the bias weights by its deltas (which are just the gradients)
		biasMatricies[biasMatricies.length - 1].add(outputGradient);

		// change weights of hidden based on calculated deltas
		for (int i = 1; i < weightsMatricies.length; i++) {
			weightsMatricies[i].add(weightsMatriciesDeltas[i]);
		}
		for (int i = 1; i < biasMatricies.length; i++) {
			biasMatricies[i].add(hiddenGradients[i - 1]);
		}
	}

}

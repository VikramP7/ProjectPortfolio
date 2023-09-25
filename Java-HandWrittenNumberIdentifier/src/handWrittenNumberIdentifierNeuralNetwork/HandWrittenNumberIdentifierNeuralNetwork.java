package handWrittenNumberIdentifierNeuralNetwork;

import java.util.Scanner;

import handWrittenNumberIdentifierNeuralNetwork.NeuralNetworkV2.Activation;
import handWrittenNumberIdentifierNeuralNetwork.NeuralNetworkV2.PrintProgress;
import handWrittenNumberIdentifierNeuralNetwork.NeuralNetworkV2.PrintResult;
import handWrittenNumberIdentifierNeuralNetwork.NeuralNetworkV2.ResultType;

public class HandWrittenNumberIdentifierNeuralNetwork {

	public static void main(String[] args) {
		AIImageList trainingAIImages = new AIImageList(
				"C:\\Users\\YellowFish.YellowFish-PC\\eclipse-workspace\\HandWrittenNumberIdentifierNeuralNetwork\\src\\TrainingData\\train-images.idx3-ubyte",
				"C:\\Users\\YellowFish.YellowFish-PC\\eclipse-workspace\\HandWrittenNumberIdentifierNeuralNetwork\\src\\TrainingData\\train-labels.idx1-ubyte");
		AIImageList testAIImages = new AIImageList(
				"C:\\Users\\YellowFish.YellowFish-PC\\eclipse-workspace\\HandWrittenNumberIdentifierNeuralNetwork\\src\\TrainingData\\t10k-images.idx3-ubyte",
				"C:\\Users\\YellowFish.YellowFish-PC\\eclipse-workspace\\HandWrittenNumberIdentifierNeuralNetwork\\src\\TrainingData\\t10k-labels.idx1-ubyte");

		trainingAIImages.Initialize();
		testAIImages.Initialize();

		NeuralNetworkV2 neuralNetwork = new NeuralNetworkV2(784, new int[] { 32, 16 }, 10, Activation.SIGMOID);

		// Loop for different Network Settings (learning rate, batch size)
		for (int batchMultiplier = 1; batchMultiplier < 21; batchMultiplier++) {
			for (int lRMultiplier = 1; lRMultiplier < 21; lRMultiplier++) {

				neuralNetwork.Reset(Activation.SIGMOID);

				neuralNetwork.SetLearningRate(0.05f * (float) lRMultiplier);

				int batchSize = 5 * batchMultiplier;

				// Create Training Resources
				float[][] trainingInputs = new float[trainingAIImages.numberOfImages][trainingAIImages.imageList[0].imageArr.length];
				float[][] trainingTargets = new float[trainingAIImages.numberOfImages][10];

				for (int i = 0; i < trainingAIImages.numberOfImages; i++) {
					for (int f = 0; f < trainingAIImages.imageList[i].imageArr.length; f++) {
						trainingInputs[i][f] = (trainingAIImages.imageList[i].imageArr[f]) / 255.0f;
					}

					for (int f = 0; f < trainingTargets[i].length; f++) {
						trainingTargets[i][f] = 0.0f;
					}
					trainingTargets[i][trainingAIImages.imageList[i].label] = 1.0f;
				}

				// Create Testing Resources
				float[][] testingInputs = new float[testAIImages.numberOfImages][testAIImages.imageList[0].imageArr.length];
				float[][] testingTargets = new float[testAIImages.numberOfImages][10];

				for (int n = 0; n < testAIImages.numberOfImages; n++) {
					for (int f = 0; f < testAIImages.imageList[n].imageArr.length; f++) {
						testingInputs[n][f] = (testAIImages.imageList[n].imageArr[f]) / 255.0f;
					}

					for (int f = 0; f < testingTargets[n].length; f++) {
						testingTargets[n][f] = 0.0f;
					}
					testingTargets[n][testAIImages.imageList[n].label] = 1.0f;
					// trainingAIImages.imageList[n+i].DrawASCIIImage();
				}

				// Window window = new Window(420, 420,
				// Integer.toString((testAIImages.imageList[130].label)));
				// window.ImageToDraw(testAIImages.imageList[130].DrawPixelImage(),
				// testAIImages.width);

				// Training
				neuralNetwork.Train(trainingInputs, trainingTargets, batchSize, 1, PrintProgress.DONTPRINTPROGRESS);

				boolean testWhileTraining = false;
				if (testWhileTraining) {
					for (int itterations = 0; itterations < 10; itterations++) {
						for (int batch = 0; batch < trainingInputs.length - batchSize; batch = batch + batchSize) {
							float[][] testTrainingInputs = new float[batchSize][trainingAIImages.imageList[0].imageArr.length];
							float[][] testTrainingTargets = new float[batchSize][10];
							for (int i = 0; i < batchSize; i++) {
								for (int j = 0; j < trainingAIImages.imageList[batch].imageArr.length; j++) {
									testTrainingInputs[i][j] = trainingInputs[batch + i][j];
								}
								for (int j = 0; j < trainingTargets[batch].length; j++) {
									testTrainingTargets[i][j] = trainingTargets[batch + i][j];
								}
							}

							// neuralNetwork.Train(testTrainingInputs, testTrainingTargets, batchSize, 1,
							// PrintProgress.DONTPRINTPROGRESS);
							neuralNetwork.GradientDescent(testTrainingInputs, testTrainingTargets);
							if ((batch % (int) (trainingTargets.length / 100)) == 0) {
								System.out.print((batch / (trainingTargets.length / 100) + (itterations * 100)) + "\t");
								neuralNetwork.Test(testingInputs, testingTargets, PrintProgress.DONTPRINTPROGRESS,
										ResultType.CHOICE, PrintResult.UGLYPRINT);
							}

						}
					}
				}

				// Testing
				// neuralNetwork.Test(testingInputs, testingTargets,
				// PrintProgress.DONTPRINTPROGRESS, ResultType.CHOICE, PrintResult.UGLYPRINT);
				neuralNetwork.Test(testingInputs, testingTargets, PrintProgress.DONTPRINTPROGRESS, ResultType.CHOICE,
						PrintResult.UGLYPRINT);
			}
		}

		Scanner scanner = new Scanner(System.in);
		boolean lop = true;
		while (lop) {
			int userinput = 0;
			boolean pooUserInput = true;
			while (pooUserInput) {
				pooUserInput = false;
				System.out.println("Image Number (Type \"quit\" to quit): ");
				String userInStr = scanner.nextLine();
				if (userInStr == "quit") {
					lop = false;
					pooUserInput = false;
				} else {
					try {
						userinput = Integer.parseInt(userInStr);
					} catch (NumberFormatException e) {
						System.out.println("That is not a integer or float value, please provide a number.");
						pooUserInput = true;
					}
				}
			}

			float[] inputs = new float[testAIImages.imageList[userinput].imageArr.length];
			for (int f = 0; f < testAIImages.imageList[userinput].imageArr.length; f++) {
				inputs[f] = (testAIImages.imageList[userinput].imageArr[f]) / 255.0f;
			}

			testAIImages.imageList[userinput].DrawASCIIImage();
			neuralNetwork.FeedForward(inputs, PrintResult.PRETTYPRINT);

			// Window window = new Window(420, 420,
			// Integer.toString((testAIImages.imageList[userinput].label)));

			// window.ImageToDraw(testAIImages.imageList[userinput].DrawPixelImage(),
			// testAIImages.width);

		}
		scanner.close();
	}

	public static String intToString(int i, int places) {
		String intString = Integer.toString(i);

		String rawInt = Integer.toString(i);
		if (rawInt.length() != places) {
			int zeros = places - rawInt.length();
			for (int n = 0; n < zeros; n++) {
				intString = "0" + intString;
			}
		}

		return intString;
	}

}

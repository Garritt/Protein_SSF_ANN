import java.util.*;
import java.io.*;
import java.text.*;

/*
 * Represents a single Perceptron for ANN
 * 
 * */
public class Lab1 {

	static int NUM_CLASSES = 2;
	static int theta = 0;
	static double LR = .1;
	
	public static void main(String args[]){
		
		double localerr, globalerr;
		int i, p, curr, label, instances, stale_count;
		float tune_accuracy, accr_last, accr_best;
		List <Integer> labels_train = new ArrayList<Integer>();
		List <Integer> labels_tune = new ArrayList<Integer>();
		List <Integer> labels_test = new ArrayList<Integer>();

		
		// Input file!
		if (args.length != 3) {
			System.err.println("usage: java perceptron <train file> <tune file> <test file>");
			System.exit(1);
		}
		
		String filename_tr = args[0];
		String filename_tu = args[1];
		String filename_tst = args[2];
		
		double [][] data_train = scan_data(filename_tr, labels_train);
		double [] weights = new double[data_train[0].length + 1];
		instances = data_train.length;
		
		// Random initialization of Weights and bias
		for (i = 0; i < weights.length; i++) {
			weights[i] = rand_num(-1,1);
		}
		
		//////////////////////////////////////////////
		// Begin learning iteration 
		System.out.println("\n-- Beginning Training --\n");
		curr = 0;
		accr_last = 0;
		accr_best = 0;
		tune_accuracy = 0;
		stale_count = 0;
		double [] best_model = new double[weights.length];
		
		do {
			curr++;
			globalerr = 0;
			
			//////// epoch iterations //////////
			for (p = 0; p < instances; p++) {
				label = pred_output(theta, weights, data_train[p]);
				localerr = labels_train.get(p) - label;
				// update weights and bias
				for (int j = 0; j < weights.length; j++) {
					// special case for bias
					if (j == weights.length - 1) {
						weights[j] += LR * localerr;
					} else {
						weights[j] += LR * localerr * data_train[p][j];	// should not execute out of bounds due to above case
					}
				}
				// update global squared error
				globalerr += (localerr * localerr);
			}

			// Mean Squared Error Loss function
			System.out.println("Iteration " + curr + ":  MSE =  " + (globalerr/instances));
			
			// Early Stopping (check every 3 epochs)
			if (curr % 3 == 0) {
				System.out.println("\n-- Evaluating Model with Tuning Data --");
				double [][] data_tune = scan_data(filename_tu, labels_tune);
				accr_last = tune_accuracy; 
				tune_accuracy = naive_accuracy(labels_tune, data_tune, weights);
				// Stopping Rules
				if (accr_last >= tune_accuracy) {
					stale_count++;
					// Break out of training due to Early Stopping
					if (stale_count > 4) {
						System.out.println("\n- Early Stopping Enabled -\n");
						break;
					}
				} else {
					stale_count = 0;
				}
				// Save Best Model
				if (tune_accuracy > accr_best) {
					for (int k = 0; k < weights.length; k++) {
						best_model[k] = weights[k];
					}
					accr_best = tune_accuracy;
				}
			}
			
		} while (globalerr != 0 && curr <= instances);

		///// RUN WITH TEST DATA ///////////
		System.out.println("\n-- Running Model with TEST Data --");
		double [][] data_test = scan_data(filename_tst, labels_test);
		float test_accuracy = naive_accuracy(labels_test, data_test, best_model);
		System.out.println("Naive Test Accuracy: " + test_accuracy);

	}
	
	/*
	 * scan_data scans a UC Urvine Testbed and returns a 2d 
	 * double array representing the data
	 * 
	 * */
	private static double[][] scan_data(String filename, List<Integer> labels) {
		
		int features, examples, linecount, wordcount;
		features = 0;
		examples = 0;
		boolean collect = false;
		String label1 = null;
		String label2 = null;
		
		// Scan Data from input file 
		Scanner scan = null;
		try{
			scan = new Scanner(new File(filename));
		} catch (FileNotFoundException e){
			System.err.println("File not found ERROR: "+ filename);
			System.exit(1);
		}
		
		double [][] data = null;
		linecount = 0;
		while (scan.hasNext()) {
			String line = scan.nextLine().trim();
			// Skip empty lines and comments
			if (line.length() == 0 || line.startsWith("//")){
				continue;
			}
			// Have finished parsing config, begin data extraction 
			if (features != 0 && examples != 0 && collect == false) {
				collect = true;
				data = new double[examples][features];
				// reset linecount for example index in data
				linecount = 0;
			}
			Scanner line_scan = new Scanner(line);
			wordcount = 0;
			while (line_scan.hasNext()) {
				String word = line_scan.next();
				
				// Parse Config Info
				if (linecount == 0 && !collect) {
					features = Integer.parseInt(word);
				} else if ((linecount == features + 1 || linecount == features + 2) && !collect) {
					if (label1 == null){
						label1 = word;
					}
					if (word != label1 && label2 == null) {
						label2 = word;
					}
				} else if (linecount == features + NUM_CLASSES + 1 && !collect) {
					examples = Integer.parseInt(word);
				} 
				
				// Collection 
				if (collect && wordcount == 1) {
					// Define correct classification
					if (word.equals(label1)) {
						labels.add(0);	
					} else if (word.equals(label2)) {
						labels.add(1);
					}
				} else if (collect && wordcount > 1) {
					// Fill in data array with feature data
					if (word.equals("T")) {
						data[linecount][wordcount - 2] = 1;						
					} else if (word.equals("F")) {
						data[linecount][wordcount - 2] = 0;						
					}
				}
				wordcount++;
			}
			line_scan.close();
			linecount++;	
		}
		return data;
	}

	/*
	 * Compute a Naive Accuracy based on labels list.
	 * 
	 * */
	private static float naive_accuracy( List<Integer> labels, double[][] data, double [] weights) {
		
		int instances = data.length;
		int []results = new int[instances];
		int correct = 0;
		for (int i = 0; i < data.length; i++) {
			results[i] = pred_output(theta, weights, data[i]);
			System.out.println("Prediction TEST " + i + ": " + results[i]);
			int label_tune = labels.get(i);
			if (results[i] == label_tune) {
				correct++;
			}
		}
		float n_accr = (((float)correct) / instances);
		return n_accr;
	}
	
	
	/* 
	 * Predicts classification of example. 
	 * Activation Function
	 * 
	 * */
	private static int pred_output(int theta, double[] weights, double[] inputs) {
		int i;
		double sum = 0;
		for (i = 0; i < inputs.length; i++) {
			sum += weights[i] * inputs[i];
		}
		sum += weights[i];
		return  (sum >= theta) ? 1 : 0;
	}
	
	/* 
	 * Gives a random number between low and high
	 * 
	 * */
	private static double rand_num(double low, double high){
		DecimalFormat df = new DecimalFormat("#.####");
		double d = low + Math.random() * (high - low); 
		String s = df.format(d);
		return Double.parseDouble(s);
	}
	
}

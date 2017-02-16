import java.util.*;

public class Network {

	Layer input_layer, output_layer;
	ArrayList<Layer> hidden_layers; 
	
	public Network (int input_n, int output_n, int h_layers, int [] hu_per_layer) {
		
		if (hu_per_layer.length != h_layers) {
			System.err.println("Network Initialization Error: hidden layers does not match hidden unit per layer vector");
		}
		// Layer Initialization (includes a random edge weight initialization)
		this.input_layer = new Layer();
		input_layer.setAsInputLayer();
		input_layer.addNeurons(input_n, null, Neuron_Type.INPUT);
		// Hidden Layers
		Layer prevL = input_layer;
		for (int i = 0; i < h_layers; i++) {
			Layer hl = new Layer();
			hl.addNeurons(hu_per_layer[i], prevL, Neuron_Type.HIDDEN);
			this.hidden_layers.add(hl);
			prevL = hl;
		}
		this.output_layer = new Layer();
		output_layer.addNeurons(output_n, prevL, Neuron_Type.OUTPUT);
	}
	
	void back_propagation (double [] network_output, double [] true_output, double alpha) {
		
	}
	
	/*
	 * 	Feeds one amino acid example through the network.
	 *  Returns the network_output.
	 * 
	 * */
	double [] feed_forward (Window window) {
		
		double[][] inputs = window.getInputs();
		int true_output_sz = window.getOutputs()[8].length; 
		
		// Initialize input Layer with Window 
		ArrayList<Neuron> input_units = input_layer.getLayer();
		for (int k = 0; k < input_units.size(); k++) {	
			input_units.get(k).setInputUnits(inputs[k]);
		}
		// feed forward through all hidden layers
		for (int g = 0; g < this.hidden_layers.size(); g++) {
			Layer h_layer = this.hidden_layers.get(g);
			// All hidden units in this layer
			for (Neuron h_unit : h_layer.getLayer()) {
				h_unit.activate();
			}
		}		
		// feed forward to output layer. Also calculate total squared error
		//double total_err = 0;
		double [] network_output = new double [true_output_sz];
		for (int g = 0; g < output_layer.getLayer().size(); g++) {
			Neuron output_unit = output_layer.getLayer().get(g);
			output_unit.activate();
			network_output[g] = output_unit.getOutput();
			//total_err += Math.pow((true_output[g] - network_output[g]), 2);
		}	
		return network_output;
	}
	
	void run (int epochs, double minError, DataSets data) {
		ArrayList<Protein> train = data.getTrain();
		ArrayList<Protein> tune = data.getTrain();
		ArrayList<Protein> test = data.getTest();
		double error = 1;
		
		for (int i = 0; i < epochs && error > minError; i++) {	
			// Training on this Protein. One Protein provides many training examples
			for (Protein prot : train) {
				for (int j = 0; j < prot.num_acids; j++) {	
					// input window example for this amino acid
					Window window = prot.getWindow();
					double [] true_output = window.getOutputs()[8]; // Output Based on nucleation site, always 9th in window
					double [] network_output = feed_forward(window);
					if (true_output.length != network_output.length) {
						System.err.println("Network Output Vector Inbalance");
						System.exit(1);
					}
					back_propagation(network_output, true_output, .05);
				}
			}
			// Early Stopping. Check against tune.
			if (epochs % 2 == 0) {
				double naive_accuracy = 0;
				double correct = 0;
				double total = 0;
				for (Protein prot : tune) {
					total += prot.num_acids;
					for (int j = 0; j < prot.num_acids; j++) {	
						// input window example for this amino acid
						Window window = prot.getWindow();
						double [] true_output = window.getOutputs()[8];
						double [] network_output = feed_forward(window);
						if (assess_network_output(true_output, network_output) == true) {
							correct++;
						}
					}
				}
				naive_accuracy = correct / total;
				System.out.println("Naive Accuracy TUNE after epoch " + epochs + ": " + naive_accuracy);
			}
		}
		
	}
	
	/*
	 * Assesses one examples network output vs expected output.
	 * Returns true if network was correct, false otherwise.
	 * 
	 * Note: Network output may not be "one hot", so we assume the output unit with the max value to be the prediction.
	 * 
	 * */
	boolean assess_network_output (double [] true_output, double [] network_output) {
		int true_idx = 0;
		int net_max_idx = 0;
		double net_max_out = 0;
		
		for (int i = 0; i < network_output.length; i++) {	
			if (true_output[i] == 1) {
				true_idx = i;
			}
			if (network_output[i] > net_max_out) {
				net_max_out = network_output[i];
				net_max_idx = i;
			}
		}
		if (net_max_idx == true_idx) {
			return true;
		}
		return false;
	}
	
	public static void main (String args[]) {		
		IO in = new IO();
		DataSets data = in.readFile(args[0]);
		// Network Config
		int [] hl_units = {10};
		Network ANN = new Network(17, 3, 1, hl_units);
		ANN.run(10, .37, data);
	}	
}

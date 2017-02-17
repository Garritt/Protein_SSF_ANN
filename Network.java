import java.util.*;

public class Network {

	Layer input_layer, output_layer;
	ArrayList<Layer> hidden_layers = new ArrayList<Layer>(); 
	
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
	
	
	
	void back_propagation (double [] network_output, double [] target_output, double alpha) {

		// Output Layer
		for (int i = 0; i < output_layer.layer.size(); i++) {
			Neuron output_n = output_layer.layer.get(i);
			ArrayList<Edge> edges = output_n.getInputEdges();
			for (int j = 0; j < edges.size(); j++) {
				Neuron input_neuron = edges.get(j).get_N_IN();				
				double delta = -(target_output[i] - network_output[i]) * network_output[i] * (1 - network_output[i]) * input_neuron.getOutput();    // POSSIBLE ERROR
				edges.get(j).weight = edges.get(j).weight - (alpha * delta);
				input_neuron.sumError(delta * edges.get(j).get_weight());
			}
		}
		// All hidden layers
		for (int i = 0; i < hidden_layers.size(); i++) {
			// One hidden Layer
			for (int j = 0; j < hidden_layers.get(i).layer.size(); j++) {
				Neuron hidden_u = hidden_layers.get(i).layer.get(j);
				ArrayList<Edge> edges = hidden_u.getInputEdges();
				// Edges of this Hidden Layers
				for (int k = 0; k < edges.size(); k ++) {
					Neuron input_neuron = edges.get(k).get_N_IN();
					double delta = hidden_u.getOutput() * (1 - hidden_u.getOutput()) * hidden_u.error * input_neuron.getOutput();  // POSSIBLE ERROR
					edges.get(k).weight = edges.get(k).weight - (alpha * delta);
				}
				hidden_u.clearError();
			}
		}
		
	}
	
	/*
	 * 	Feeds one amino acid example through the network.
	 *  Returns the network_output.
	 * 
	 * */
	public double [] feed_forward (Window window) {
		
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
	
	public void run (int epochs, double minError, DataSets data) {
		ArrayList<Protein> train = data.getTrain();
		ArrayList<Protein> tune = data.getTune();
		ArrayList<Protein> test = data.getTest();
		double error = 1;
		
		for (int i = 0; i < epochs && error > minError; i++) {	
			// Training on this Protein. One Protein provides many training examples
			for (Protein prot : train) {
				Window window;
				while((window = prot.getWindow()) != null) {
					// input window example for this amino acid
					// Window window = prot.getWindow();
					double [] true_output = window.getOutputs()[8]; // Output Based on nucleation site, always 9th in window
					double [] network_output = feed_forward(window);
					if (true_output.length != network_output.length) {
						System.err.println("Network Output Vector Inbalance");
						System.exit(1);
					}
					back_propagation(network_output, true_output, .05);
					// neet to reset errors here
				}
			}
			// Early Stopping. Check against tune.
			if (i % 2 == 0) {
				double naive_accuracy = 0;
				double correct = 0;
				double total = 0;
				for (Protein prot : tune) {
					total += prot.num_acids;
					Window window;
					while ((window = prot.getWindow()) != null) {
						// input window example for this amino acid
						// Window window = prot.getWindow();
						double [] true_output = window.getOutputs()[8];
						double [] network_output = feed_forward(window);
						if (assess_network_output(true_output, network_output) == true) {
							correct++;
						}
					}
				}
				naive_accuracy = correct / total;
				System.out.println("Naive Accuracy TUNE after epoch " + i + ": " + naive_accuracy);
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
		DataSets data = IO.readFile(args[0]);
		// Network Config
		int [] hl_units = {10};
		Network ANN = new Network(17, 3, 1, hl_units);
		ANN.run(10, .37, data);
	}	
}

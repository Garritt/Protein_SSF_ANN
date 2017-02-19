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
	
	
	
	void back_propagation (double [] network_output, double [] target_output, double alpha, double momentum) {
		
		// Output Layer
		for (int i = 0; i < output_layer.layer.size(); i++) {
			Neuron output_n = output_layer.layer.get(i);
			ArrayList<Edge> edges = output_n.getInputEdges();
			for (int j = 0; j < edges.size(); j++) {
				Neuron hidden_neuron = edges.get(j).get_N_IN();				
				double delta = -network_output[i] * (1 - network_output[i]) * hidden_neuron.getOutput() * (target_output[i] - network_output[i]);     // POSSIBLE ERROR
				double w_change = -alpha * delta;
				edges.get(j).setPrev_delta_weight(w_change);
				edges.get(j).set_weight(edges.get(j).get_weight() + w_change + (momentum * edges.get(j).get_prev_delta_weight()));
				hidden_neuron.sumError(delta * edges.get(j).get_weight());
			}
		}
		// All hidden layers
		for (int i = hidden_layers.size(); i > 0; i--) {
			// One hidden Layer
			for (int j = 0; j < hidden_layers.get(i-1).layer.size(); j++) {
				Neuron hidden_u = hidden_layers.get(i-1).layer.get(j);
				ArrayList<Edge> edges = hidden_u.getInputEdges();
				// Edges of this Hidden Layers
				for (int k = 0; k < edges.size(); k ++) {
					Neuron input_neuron = edges.get(k).get_N_IN();
					double delta = hidden_u.getOutput() * (1 - hidden_u.getOutput()) * hidden_u.error * input_neuron.getOutput();  // POSSIBLE ERROR
					double w_change = -alpha * delta;
					edges.get(k).setPrev_delta_weight(w_change);
					edges.get(k).set_weight(edges.get(k).get_weight() + w_change + (momentum * edges.get(k).get_prev_delta_weight()));
				}
				hidden_u.clearError();
			}
		}
		
	}
	
	void resetMomentum() {
		ArrayList<Neuron> output_nodes = output_layer.getLayer();
		for(int i = 0; i < output_nodes.size(); i++) {
			ArrayList<Edge> in_edges = output_nodes.get(i).getInputEdges();
			for(int j = 0; j < in_edges.size(); j++) {
				in_edges.get(j).reset();
			}
		}
		
		for(int i = 0; i < hidden_layers.size(); i++) { //backprop! go backwards
			ArrayList<Neuron> in_nodes = hidden_layers.get(i).getLayer();
			for(int j = 0; j < in_nodes.size(); j++) {
				ArrayList<Edge> in_edges = in_nodes.get(j).getInputEdges();
				for(int k = 0; k < in_edges.size(); k++) {
					in_edges.get(k).reset();
				}
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
		if(window.getOutputs()[8].length > 3){
			int temp = window.getOutputs()[8].length;
			System.out.println("here");
		}
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
		double [] network_output = new double [true_output_sz];
		for (int g = 0; g < output_layer.getLayer().size(); g++) {
			Neuron output_unit = output_layer.getLayer().get(g);
			output_unit.activate();
			network_output[g] = output_unit.getOutput();
		}	
		return network_output;
	}

	public void run (int epochs, double minError, DataSets data) {
		
		ArrayList<Protein> train = data.getTrain();
		ArrayList<Protein> tune = data.getTune();
		ArrayList<Protein> test = data.getTest();
		double [][] cm = new double [3][3];
		
		// Training Epochs
		for (int i = 0; i < epochs; i++) {	
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
					back_propagation(network_output, true_output, .05, 0.9);
				}
			}	
			// Early Stopping. Check against tune.
			if (i % 2 == 0 && i != 0) {
				double confu_accuracy = 0;
				double total = 0;				
				for (Protein prot : tune) {
					total += prot.num_acids;
					Window window;
					while ((window = prot.getWindow()) != null) {
						// input window example for this amino acid
						double [] true_output = window.getOutputs()[8];
						double [] network_output = feed_forward(window);
						assess_network_output(true_output, network_output, cm);
					}
				}
				confu_accuracy = (cm[0][0] + cm[1][1] + cm[2][2]) / total;
				System.out.println("Confusion Matrix Accuracy TUNE after epoch " + i + ": " + confu_accuracy);
				clear_print_matrix(cm, 1);
				if (confu_accuracy > 1.0 - minError) {
					break;
				}
			}
			this.resetMomentum();
		}
		// Start on test set
		double confu_accuracy = 0;
		double total = 0;
		for (Protein prot : test ) {
			total+=prot.num_acids;
			Window window;
			while ((window = prot.getWindow()) != null) {
				// input window example for this amino acid
				double [] true_output = window.getOutputs()[8];
				double [] network_output = feed_forward(window);
				assess_network_output(true_output, network_output, cm);
			}
		}
		
		confu_accuracy = (cm[0][0] + cm[1][1] + cm[2][2]) / total;
		double recall_a = cm[2][2] / (cm[2][0] + cm[2][1] + cm[2][2]);
		double precision_a = cm[2][2] / (cm[0][2] + cm[1][2] + cm[2][2]);
		double recall_b = cm[1][1] / (cm[1][0] + cm[1][1] + cm[1][2]);
		double precision_b = cm[1][1] / (cm[0][1] + cm[1][1] + cm[2][1]);
		double recall_c = cm[0][0] / (cm[0][0] + cm[0][1] + cm[0][2]);
		double precision_c = cm[0][0] / (cm[0][0] + cm[1][0] + cm[2][0]);
 		
		System.out.println("\n\nConfusion Matrix Data run on TEST\n");
		clear_print_matrix(cm, 0);
		System.out.println("\nAccuracy:  " + confu_accuracy + "\nError Rate: " + (1 - confu_accuracy));
		System.out.println("\nRecall alpha-helix: " + recall_a);
		System.out.println("Recall beta-strand: " + recall_b);
		System.out.println("Recall coil:        " + recall_c);
		System.out.println("\nPrecision alpha-helix:  " + precision_a);
		System.out.println("Precision beta-strand:  " + precision_b);
		System.out.println("Precision coil:         " + precision_c);
	}
	
	/*
	 * Assesses one examples network output vs expected output.
	 * Returns true if network was correct, false otherwise.
	 * 
	 * Note: Network output may not be "one hot", so we assume the output unit with the max value to be the prediction.
	 * 
	 * */
	void assess_network_output (double [] true_output, double [] network_output, double [][] cm) {
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
		cm[true_idx][net_max_idx]++;
		//System.out.println("target idx: " + true_idx + " netout idx: "+ net_max_idx);
	}
	
	void clear_print_matrix( double [][] cm, int flag) {
		for (int i = 0; i < cm.length; i++) {
			for (int j = 0; j < cm.length; j++) {
				if (flag == 0) {
					System.out.print(cm[i][j] + " ");
				} else if (flag == 1){
					cm[i][j] = 0;
				} else {
					System.out.print(cm[i][j] + " ");
					cm[i][j] = 0;
				}
			}
			if (flag != 1 ) {System.out.println();}
		}
	}
	
	public static void main (String args[]) {		
		DataSets data = IO.readFile(args[0]);
		// Network Config
		int [] hl_units = {10};
		Network ANN = new Network(17, 3, 1, hl_units);
		ANN.run(100, .4, data);
	}	
}



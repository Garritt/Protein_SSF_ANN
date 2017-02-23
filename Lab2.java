
import java.text.DecimalFormat;
import java.io.*;
import java.util.*;

public class Lab2 {

	Layer input_layer, output_layer;
	ArrayList<Layer> hidden_layers = new ArrayList<Layer>(); 
	
	final double drop_rate = .3;									// Set to 0 for no drop out
	final double alpha = .05;
	final double momentum = .9;										// Set to 0 for no momentum
	
	public Lab2 (int input_n, int output_n, int h_layers, int [] hu_per_layer) {
		
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
		for (int i = hidden_layers.size(); i > 0; i--) {    // Backprop -- go backwards
			// One hidden Layer
			for (int j = 0; j < hidden_layers.get(i-1).layer.size(); j++) {
				Neuron hidden_u = hidden_layers.get(i-1).layer.get(j);
				if (hidden_u.getDrop() == false) {								// Drop Out
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
		
	}
	/*
	 * boolean input reset will reset momentum on edges if true.
	 * If false, the method will save the best weight to the edge.
	 * 
	 * */
	void resetM_saveBW(boolean reset) {
		
		ArrayList<Neuron> output_nodes = output_layer.getLayer();
		for (int i = 0; i < output_nodes.size(); i++) {
			ArrayList<Edge> in_edges = output_nodes.get(i).getInputEdges();
			for(int j = 0; j < in_edges.size(); j++) {
				Edge e = in_edges.get(j);
				if (reset){e.reset();}
				else {e.setBestWeight(e.get_weight());}
			}
		}
		for(int i = 0; i < hidden_layers.size(); i++) { 
			ArrayList<Neuron> in_nodes = hidden_layers.get(i).getLayer();
			for(int j = 0; j < in_nodes.size(); j++) {
				ArrayList<Edge> in_edges = in_nodes.get(j).getInputEdges();
				for(int k = 0; k < in_edges.size(); k++) {
					Edge e = in_edges.get(k);
					if (reset) {e.reset();}
					else {e.setBestWeight(e.get_weight());}
				}
			}
		}
	}
	
	/*
	 * 	Feeds one amino acid example through the network.
	 *  Returns the network_output.
	 * 
	 * */
	public double [] feed_forward (Window window, boolean best) {
		
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
				if (h_unit.getDrop() == true){
					continue;
				} else {
					h_unit.activate(drop_rate, best);
				}
			}
		}		
		// feed forward to output layer. Also calculate total squared error
		double [] network_output = new double [true_output_sz];
		for (int g = 0; g < output_layer.getLayer().size(); g++) {
			Neuron output_unit = output_layer.getLayer().get(g);
			output_unit.activate(drop_rate, best);
			network_output[g] = output_unit.getOutput();
		}	
		return network_output;
	}

	/*
	 * Runs a network configuration with test data.
	 * 
	 * */
	public void run (int epochs, double minError, DataSets data) {
		
		ArrayList<Protein> train = data.getTrain();
		ArrayList<Protein> tune = data.getTune();
		ArrayList<Protein> test = data.getTest();
		
		double [] train_accuracy_stream = new double [epochs];
		double [] tune_accuracy_stream = new double [(int) Math.ceil((epochs/2))];
		//double [] test_accuracy_stream = new double [epochs];
		
		double [][] cm = new double [3][3];
		double confu_accuracy;
		double total = 0;
		double best = 0;
		
		// Training Epochs
		for (int i = 0; i < epochs; i++) {	
			//clear_print_matrix(cm, 1);
			confu_accuracy = 0;
			total = 0;
			// Training on this Protein. One Protein provides many training examples.
			for (Protein prot : train) {
				total += prot.num_acids;
				Window window;
				while((window = prot.getWindow()) != null) {
					// Set Drop Out Neurons
					this.setDropN(drop_rate);
					// input window example for this amino acid
					double [] true_output = window.getOutputs()[8]; // Output Based on nucleation site, always 9th in window
					double [] network_output = feed_forward(window, false);
					if (true_output.length != network_output.length) {
						System.err.println("Network Output Vector Inbalance");
						System.exit(1);
					}
					back_propagation(network_output, true_output, alpha, momentum);
					this.removeDropN();
					assess_network_output(true_output, network_output, cm, false);
				}
			}
			confu_accuracy = (cm[0][0] + cm[1][1] + cm[2][2]) / total;
			train_accuracy_stream[i] = confu_accuracy;
			//System.out.println("Confusion-Matrix Accuracy TRAIN after epoch " + i + ": " + confu_accuracy);
			clear_print_matrix(cm, 1);
			
			// Early Stopping. Check against tune.
			if (i % 2 == 0 && i != 0) {
				confu_accuracy = 0;
				total = 0;				
				for (Protein prot : tune) {
					total += prot.num_acids;
					Window window;
					while ((window = prot.getWindow()) != null) {
						// input window example for this amino acid
						double [] true_output = window.getOutputs()[8];
						double [] network_output = feed_forward(window, false);
						assess_network_output(true_output, network_output, cm, false);
					}
				}
				confu_accuracy = (cm[0][0] + cm[1][1] + cm[2][2]) / total;
				if (confu_accuracy > best) {
					best = confu_accuracy;
					this.resetM_saveBW(false);
				}
				//System.out.println("Confusion-Matrix Accuracy TUNE after epoch " + i + ": " + confu_accuracy);
				tune_accuracy_stream[i/2] = confu_accuracy;
				clear_print_matrix(cm, 1);
				if (confu_accuracy > 1.0 - minError) {
					break;															
				}
			}
			this.resetM_saveBW(true);
		}
		// Start on test set
		confu_accuracy = 0;
		total = 0;
		for (Protein prot : test ) {
			total+=prot.num_acids;
			Window window;
			while ((window = prot.getWindow()) != null) {
				// input window example for this amino acid
				double [] true_output = window.getOutputs()[8];
				double [] network_output = feed_forward(window, true);
				assess_network_output(true_output, network_output, cm, true);
			}
		}		
		statPrint(train_accuracy_stream, tune_accuracy_stream, cm, total);
	}
	
	/*
	 * Prints training and testing stats for this network
	 * 
	 * */
	void statPrint (double [] train_stream, double [] tune_stream, double [][] cm, double total) {
		// for (int i = 0; i < train_stream.length; i++) {
		// 	if (train_stream[i] != 0) {
		// 		System.out.println("Accuracy TRAIN after epoch " + i + ": " + train_stream[i]);
		// 	}
		// }
		// System.out.println("\n");
		// for (int i = 0; i < tune_stream.length; i++) {
		// 	if (tune_stream[i] != 0) {
		// 		System.out.println("Accuracy TUNE after epoch " + i*2 + ": " + tune_stream[i]);
		// 	}
		// }
		// System.out.println("\n");
		// for (int i = 0; i < test_stream.length; i++) {
		// 	if (test_stream[i] != 0) {
		// 		System.out.println("Accuracy TEST after epoch " + i + ": " + test_stream[i]);
		// 	}
		// }
		System.out.println("\n");
		double confu_accuracy = (cm[0][0] + cm[1][1] + cm[2][2]) / total;
		double recall_a = cm[2][2] / (cm[2][0] + cm[2][1] + cm[2][2]);
		double precision_a = cm[2][2] / (cm[0][2] + cm[1][2] + cm[2][2]);
		double recall_b = cm[1][1] / (cm[1][0] + cm[1][1] + cm[1][2]);
		double precision_b = cm[1][1] / (cm[0][1] + cm[1][1] + cm[2][1]);
		double recall_c = cm[0][0] / (cm[0][0] + cm[0][1] + cm[0][2]);
		double precision_c = cm[0][0] / (cm[0][0] + cm[1][0] + cm[2][0]);
 		System.out.println("Confusion Matrix Data run on TEST\n");
		clear_print_matrix(cm, 0);
		System.out.println("\nAccuracy:  " + confu_accuracy + "\nError Rate: " + (1 - confu_accuracy));
		System.out.println("\nRecall alpha-helix: " + recall_a);
		System.out.println("Recall beta-strand: " + recall_b);
		System.out.println("Recall coil:        " + recall_c);
		System.out.println("\nPrecision alpha-helix:  " + precision_a);
		System.out.println("Precision beta-strand:  " + precision_b);
		System.out.println("Precision coil:         " + precision_c + "\n\n\n\n\n");
	}
	
	/*
	 * setDropN and removeDropN are used to toggle units as
	 * drop units when using Hinton's DropOut technique.
	 *
	 * */
	
	void setDropN (double drop_rate) {
		for (int g = 0; g < this.hidden_layers.size(); g++) {
			Layer h_layer = this.hidden_layers.get(g);
			// All hidden units in this layer
			for (Neuron h_unit : h_layer.getLayer()) {
				double rand = Math.random();
				if (rand <= drop_rate) {
					h_unit.setDrop();
				}	// could also scale by (1 - droprate) here, but extra complexity... I think.
			}
		}
	}
	void removeDropN () {
		for (int g = 0; g < this.hidden_layers.size(); g++) {
			Layer h_layer = this.hidden_layers.get(g);
			// All hidden units in this layer
			for (Neuron h_unit : h_layer.getLayer()) {
				h_unit.removeDrop();
			}
		}
	}
	
	
	/*
	 *
	 * Increments confusion matrix data in the correct fashion
	 * using network output and target_output.
	 * 
	 * */
	void assess_network_output (double [] true_output, double [] network_output, double [][] cm, boolean print) {
		
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
		if (print) {
			if (net_max_idx == 0) {
				System.out.println("COIL");
			} else if (net_max_idx == 1) {
				System.out.println("ALPHA-HELIX");
			} else if (net_max_idx == 2) {
				System.out.println("BETA-STRAND");
			}
		}
		//System.out.println("target idx: " + true_idx + " netout idx: "+ net_max_idx);
	}
	
	/*
	 * Helper function to clear/print confusion matrix after every calculation.
	 * 
	 * 0 flag = print
	 * 1 flag = clear
	 * anything else = both 
	 * 
	 * */
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
		int [] hl_units_1L_5 = {5};
		int [] hl_units_1L_10 = {10};
		int [] hl_units_1L_30 = {30};
		int [] hl_units_1L_100 = {100};
		int [] hl_units_1L_1000 = {1000};
		// System.out.println("\n\n\n\n - Configuring Network ann_5 - \n");
		// Network ann_5 = new Network(17, 3, 1, hl_units_1L_5);
		// System.out.println("\talpha: " + ann_5.alpha + "\n\tdrop_rate: " + ann_5.drop_rate + "\n\tmomentum term: " + ann_5.momentum + "\n\n\tRunning Network...\n");
		// ann_5.run(100, .358, data);
		// System.out.println("\n\n\n\n - Configuring Network ann_10 - \n");
		// Network ann_10 = new Network(17, 3, 1, hl_units_1L_10);
		// System.out.println("\talpha: " + ann_10.alpha + "\n\tdrop_rate: " + ann_10.drop_rate + "\n\tmomentum term: " + ann_10.momentum + "\n\n\tRunning Network...\n");
		// ann_10.run(100, .358, data);
		// System.out.println("\n\n\n\n - Configuring Network ann_30 - \n");
		Lab2 ann_30 = new Lab2(17, 3, 1, hl_units_1L_30);
		// System.out.println("\talpha: " + ann_30.alpha + "\n\tdrop_rate: " + ann_30.drop_rate + "\n\tmomentum term: " + ann_30.momentum + "\n\n\tRunning Network....\n");
		ann_30.run(100, .358, data);
		// System.out.println("\n\n\n\n - Configuring Network ann_100 - \n");
		// Network ann_100 = new Network(17, 3, 1, hl_units_1L_100);
		// System.out.println("\talpha: " + ann_100.alpha + "\n\tdrop_rate: " + ann_100.drop_rate + "\n\tmomentum term: " + ann_100.momentum + "\n\n\tRunning Network...\n");
		// ann_100.run(100, .358, data);
		//System.out.println("\n\n\n\n - Configuring Network ann_1000 - \n");
		//Network ann_1000 = new Network(17, 3, 1, hl_units_1L_1000);
		//System.out.println("\talpha: " + ann_1000.alpha + "\n\tdrop_rate: " + ann_1000.drop_rate + "\n\tmomentum term: " + ann_1000.momentum + "\n\n\tRunning Network...\n");
		//ann_1000.run(100, .358, data);
	}	
}




 class Edge {

	private double best_weight;
	private double weight;
	private double prev_delta_weight; 	//momentum
	
	final Neuron N_in;
	final Neuron N_out;
	
	public Edge (Neuron in, Neuron out) {
		this.N_in = in;
		this.N_out = out;
		prev_delta_weight = 0;
	}
	public void setBestWeight (double weight){
		this.best_weight = weight;
	}
	public double getBestWeight () {
		return this.best_weight;
	}
	public void set_prev_delta (double prev) {
		this.prev_delta_weight = prev;
	}
	public void reset() {
		this.prev_delta_weight = 0;
	}
	public void set_weight (double w) {
		weight = w;
	}
	public double get_weight () {
		return this.weight;
	}
	public Neuron get_N_IN() {
		return this.N_in;
	}
	public Neuron get_N_OUT() {
		return this.N_out;
	}
	public double get_prev_delta_weight(){
		return this.prev_delta_weight;
	}
	public void set_rand_w(double low, double high){
		DecimalFormat df = new DecimalFormat("#.###");
		double d = low + Math.random() * (high - low); 
		String s = df.format(d);
		weight = Double.parseDouble(s);
	}
	public void setPrev_delta_weight(double prev_delta_weight) {
		this.prev_delta_weight = prev_delta_weight;
	}
}



class Neuron {

	// constant Bias edge weight 
	final double bias = -1;
	Edge bias_edge = null;
	
	// All Inputs to the neuron 
	ArrayList<Edge> input_edges = new ArrayList<Edge>();
	HashMap <Integer,Edge> edge_lookup = new HashMap <Integer, Edge>();
	
	// Neuron ID for hashing edge lookup
	static int id_count = 0;
	final public int id;
	final private Neuron_Type type;
	private double output;
	private Neuron [] input_vector;
	private boolean drop;
	double error;
	
	public Neuron (Neuron_Type type) {
		this.type = type;
		this.drop = false;
		if (type == Neuron_Type.INPUT){
			this.input_vector = new Neuron [21]; // HARDCODE WARNING
		} else if (type == Neuron_Type.BIAS) {
			this.output = bias;
		}
		this.id = id_count;
		id_count++; 
	}
	public void setDrop() {
		this.drop = true;
		this.output = 0;
	}
	public void removeDrop() {
		this.drop = false;
	}
	public boolean getDrop() {
		return this.drop;
	}
	public double getOutput() {
		return this.output;
	}
	public void setOutput(double o) {
		this.output = o;
	}
	public Neuron [] getInputVector() {
		return this.input_vector;
	}
	public Edge get_edge (int neuron_id) {
		return edge_lookup.get(neuron_id);
	}
	public ArrayList<Edge> getInputEdges() {
		return input_edges;
	}
	public void sumError(double error) {
		this.error += error;
	}
	public void clearError() {
		this.error = 0;
	}
	public void setInputUnits (double [] in) {
		for (int i = 0; i < in.length; i++) {
			Neuron N = input_vector[i];
			N.setOutput(in[i]);
		}
	}
	
	public void activate (double drop_rate, boolean best) {
		
		if (this.type.equals(Neuron_Type.INPUT)) {
			// 
		} else {
			// Weighted Linear Sum 
			double sum = 0;
			for (Edge e : input_edges) {
				//System.out.println(e.get_weight());
				Neuron in = e.get_N_IN();
				if (in.getDrop() == true) {										// DROP OUT 
					continue;
				}
				double weight;
				if (best){weight = e.getBestWeight() * (1-drop_rate);}
				else {weight = e.get_weight() * (1-drop_rate);}			
				double prev_output = in.output;
				sum = sum + (weight * prev_output);				
			}	
			//if (this.type == Neuron_Type.HIDDEN) {
			//	this.output = Math.max(0, sum);
			//} else {    // Output Neuron 
				// Sigmoid Activation function
				this.output = 1.0/ (1.0 + Math.exp(-sum));
			//}
		}
	}
		
	/*
	 * Add edges from previous layer of neurons to this Neuron. 
	 * Includes initialization of bias edge. 
	 * 
	 * All edges are initialized with a random weight.
	 * 
	 * */
	public void construct_In_Edges (Layer in_layer, Neuron bias) {
		for (Neuron n : in_layer.layer) {
			if (n.type == Neuron_Type.INPUT) {
				for (int i = 0; i < n.input_vector.length; i++) {
					Neuron ne = n.input_vector[i];
					Edge e = new Edge(ne, this);
					e.set_rand_w(-1, 1);
					input_edges.add(e);
				}
			} else {
				Edge e = new Edge(n, this); 
				e.set_rand_w(-1, 1);
				input_edges.add(e);
				edge_lookup.put(n.id, e);
			}
		}
		Edge edge_bias = new Edge(bias, this);
		bias_edge = edge_bias;
		edge_bias.set_rand_w(-1, 1);
		input_edges.add(bias_edge);
	}
}










class Layer {

	static int layer_count = 0;
	final public int layer_id;
	public boolean input_layer;
	ArrayList<Neuron> layer = new ArrayList<Neuron>();
	
	public Layer () {
		this.layer_id = layer_count;
		this.input_layer = false;
		layer_count++;
	}

	/* 
	 * 
	 * Adds num Neurons to a Layer.
	 * 
	 * An edge is creted between the Neuron and every
	 * Nueron prev Layer.
	 * 
	 * */
	public void addNeurons (int num, Layer prevL, Neuron_Type type) {
		for (int i = 0; i < num; i++) {
			Neuron n = new Neuron(type);
			layer.add(n);
			if (!this.input_layer) {
				Neuron bias = new Neuron(Neuron_Type.BIAS);
				n.construct_In_Edges(prevL, bias);
			} else {
				Neuron [] input_vector = n.getInputVector();
				for (int j = 0; j < input_vector.length; j++) {
					Neuron ne = new Neuron(Neuron_Type.INPUT);
					input_vector[j] = ne;
				}
			}
		}
	}
	
	public ArrayList<Neuron> getLayer() {
		return this.layer;
	}
	
	public void setAsInputLayer() {
		this.input_layer = true;
	}
	
	
}



class IO {

	private static double[] addAcid(String acid) {
		double[] ret;
		switch(acid) {
			case "I": ret = new double[]{1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "E": ret = new double[]{0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "N": ret = new double[]{0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "L": ret = new double[]{0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "K": ret = new double[]{0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "S": ret = new double[]{0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "G": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "M": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "F": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "R": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "V": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "Q": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "Y": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "P": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "C": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "H": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "W": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0};
			break;
			case "T": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0};
			break;
			case "D": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0};
			break;
			case "A": ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0};
			break;
			default: ret = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
		}
		return ret;
	}

	private static double[] addOutput(String output) {
		double[] ret;
		switch(output) {
			case "_": ret = new double[]{1.0,0.0,0.0};
			break;
			case "h": ret = new double[]{0.0,1.0,0.0};
			break;
			case "e": ret = new double[]{0.0,0.0,1.0};
			break;
			default: ret = new double[]{0.0,0.0,0.0};
			;
		}
		return ret;
	}

	public static DataSets readFile(String filename) {
		if (filename == null) {
			System.out.println("Require a filename");
			System.exit(1);
		}

		// Try to open that shit ass scanner bro
		// so we can read in them proteins
		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			System.out.println("Could not find the file bro");
			System.exit(1);
		}

		// WE ABOUT TO ITERATE 
		int linecount = 0;
		int trueCount = 0;
		int numProteins = 1;
		boolean beginning = true;
		ArrayList<double[]> temp = new ArrayList<double[]>();
		ArrayList<double[]> tempOutputs = new ArrayList<double[]>();
		ArrayList<Protein> training = new ArrayList<Protein>();
		ArrayList<Protein> tuning = new ArrayList<Protein>();
		ArrayList<Protein> testing = new ArrayList<Protein>();

		while (fileScanner.hasNext()) {
			String line = fileScanner.nextLine().trim();
			// trueCount++;


			// Skip the empty line, duh
			// or comments
			if (line.length() == 0 || line.startsWith("#")) {
				continue;
			}

			if(line.equals("<>") && beginning) {
				beginning = false;
			}


			// Need another shit ass scanner for the line
			Scanner wordscanner = new Scanner(line);

			
			if((line.equals("end") || line.equals("<end>")) || (line.equals("<>") && !beginning) && linecount > 1) {

				Protein prot = new Protein(linecount, temp, tempOutputs);
				temp = new ArrayList<double[]>();
				tempOutputs = new ArrayList<double[]>();
				linecount = 0;
				numProteins++;
				beginning = true;
				if (trueCount % 5 == 0) {
					tuning.add(prot);
				} else if (trueCount % 5 == 1) {
					testing.add(prot);
				} else {
					training.add(prot);
				}
				trueCount++;
				
			}
			int wordcount = 0;
			while(wordscanner.hasNext()) {
				String word = wordscanner.next().trim();
				if(word.equals("<>")) {
					continue;
				}
				if (wordcount == 0 && !word.equals("<end>")&& !word.equals("end")) {
					temp.add(addAcid(word));
				} else if (wordcount > 0  && !word.equals("<end>")&& !word.equals("end")) {
					
					tempOutputs.add(addOutput(word));
				}
				wordcount++;
			}
			linecount++;


		}
		DataSets data = new DataSets(training, tuning, testing);
		return data;

	}

}

class Window {
	private final double[][] inputs;
	private final double[][] target_outputs;


	public Window(double[][] in, double[][] out) {
		this.inputs = in;
		this.target_outputs = out;
	}

	public double[][] getInputs() {
		return this.inputs;
	}

	public double[][] getOutputs() {
		return this.target_outputs;
	}
}

class Protein {
	
	public final int num_acids;
	private final ArrayList<double[]> acids;
	private final ArrayList<double[]> target_outputs;
	private int top, bottom, middle;


	public Protein (int aminoacids, ArrayList<double[]> acids, ArrayList<double[]> outputs) {
		this.num_acids = aminoacids;
		this.acids = acids;
		this.target_outputs = outputs;
		this.top = 9;
		this.middle = 0;
		this.bottom = -8;
	}

	public void printProtein() {
		System.out.println("Amino Acid Sequence");
		for(int i = 0; i < this.acids.size(); i++) {
			System.out.print(" " + acids.get(i));
		}
		System.out.println();
		System.out.println("Target Outputs");

		for(int i = 0; i < target_outputs.size(); i++) {
			System.out.print(" " + target_outputs.get(i));
		}
		System.out.println();
		System.out.println("######  Size sanity check  ###### \nNUM ACIDS: "+ num_acids);
		System.out.println("Amino Acid Sequence length: " + this.acids.size());
		System.out.println("Target Output Sequence length: " + target_outputs.size());
		System.out.println();
	}


	public ArrayList<double[]> getAcid(){
		return this.acids;
	}

	public ArrayList<double[]> getOutputs(){
		return this.target_outputs;
	}

	public int numAcids(){
		return this.num_acids;
	}

	private void increaseTop() {
		// this.printProtein();
		this.top++;

	}
	private void increaseBottom() {
		this.bottom++;

	}
	private void increaseMiddle() {
		this.middle++;

	}

	private void reset() {
		this.middle = 0;
		this.top = 9;
		this.bottom = -8;
	}


	public Window getWindow(){
		//System.out.println("NUM ACIDS: " + num_acids);
		//System.out.println("ACIDS SIZE: " + this.acids.size());
		if (this.middle == this.acids.size()) {                 ////// possible error
			//System.out.println("MIDDLE: " + this.middle);
			//System.out.println("ACIDS SIZE: " + this.acids.size());
			this.reset();
			return null;
		}
		double[][] in = new double[17][21];
		double[][] out = new double[17][3];
		int k = 0;


		// IF THE ACID SEQUNENCE IS SMALLER THAN WINDOW
		if (this.acids.size() < 17) {
			if (this.bottom < 0) {
				int j = 0;
				// Fill Start Padding 
				for (int i = this.bottom; i < 0; i++) {
					in[k] = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0};
					out[k] = new double[]{0.0,0.0,0.0};
					k++;
					// j++;
				}
				// Fill with acids that occur in window 
				while (k < 17 && j < this.acids.size()) {
					in[k] = this.getAcid().get(j);
					out[k] = this.getOutputs().get(j);
					k++;
					j++;
				}
				// Fill Ending padding 
				while(k < 17) {
					in[k] = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0};
					out[k] = new double[]{0.0,0.0,0.0};
					k++;
				}
			} else {
				int j = this.bottom;
				while (k < 17 && j < this.acids.size()) {
					in[k] = this.getAcid().get(j);
					out[k] = this.getOutputs().get(j);
					k++;
					j++;
				}
				while(k < 17) {
					in[k] = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0};
					out[k] = new double[]{0.0,0.0,0.0};
					k++;
				}
			}
		} else {
			int j = 0;
			if (this.bottom < 0) {
				for (int i = this.bottom; i < 0; i++) {
					in[k] = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0};
					out[k] = new double[]{0.0,0.0,0.0};
					k++;
					// j++;
				}
				while (k < 17 && j < this.acids.size()) {
					in[k] = this.getAcid().get(j);
					out[k] = this.getOutputs().get(j);
					k++;
					j++;
				}
				while (k < 17) {
					in[k] = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0};
					out[k] = new double[]{0.0,0.0,0.0};
					k++;
				}
			} else if (this.top > this.acids.size() && this.bottom > 0) {
				j = this.bottom;
				while(k < 17 && j < this.acids.size()) {
					in[k] = this.getAcid().get(j);
					out[k] = this.getOutputs().get(j);
					k++;
					j++;
				}
				
				while (k < 17) {
					in[k] = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0};
					out[k] = new double[]{0.0,0.0,0.0};
					k++;
				}
				
			} else if (this.bottom > 0 && this.top < this.acids.size()) {
				for (j = this.bottom; j < this.top; j++){
					in[k] = this.getAcid().get(j);
					out[k] = this.getOutputs().get(j);
					k++;
				}
			}

		}



	Window window = new Window(in, out);

	this.increaseBottom();
	this.increaseTop();
	this.increaseMiddle();
	return window;


}

}


class DataSets {

	public final ArrayList<Protein> train;
	public final ArrayList<Protein> tune;
	public final ArrayList<Protein> test;

	public DataSets(ArrayList<Protein> training, ArrayList<Protein> tuning, ArrayList<Protein> testing) {
		this.train = training;
		this.tune = tuning;
		this.test = testing;
	}

	public ArrayList<Protein> getTrain(){
		return this.train;
	}
	public ArrayList<Protein> getTune(){
		return this.tune;
	}
	public ArrayList<Protein> getTest(){
		return this.test;
	}
}

enum Neuron_Type {
	INPUT, HIDDEN, OUTPUT, BIAS
}

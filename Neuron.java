import java.util.*;

public class Neuron {

	// constant Bias edge weight 
	final double bias = -1;
	Edge bias_edge;
	
	// All Inputs to the neuron 
	ArrayList<Edge> input_edges = new ArrayList<Edge>();
	HashMap <Integer,Edge> edge_lookup = new HashMap <Integer, Edge>();
	
	// Neuron ID for hashing edge lookup
	static int id_count = 0;
	final public int id;
	
	// Neuron Output value
	double output; 
	
	public Neuron (){
		this.id = id_count;
		id_count++; 
	}
	
	public double getOutput() {
		return this.output;
	}
	public void setOutput(double o) {
		this.output = o;
	}
	public Edge get_edge(int neuron_id) {
		return edge_lookup.get(neuron_id);
	}
	public ArrayList<Edge> getInputLayer() {
		return input_edges;
	}
	
	/*
	 * Passes Weighted linear Sum to Activation function
	 * 
	 * */
	public void calculate_output(){
		double sum = 0;
		for (Edge e : input_edges) {
			Neuron in = e.get_N_IN();
			double weight = e.get_weight();
			double prev_output = in.output;
			sum = sum + (weight * prev_output);
		}
		sum = sum + (bias_edge.get_weight() * bias);
		this.output = sigmoid(sum);
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
			Edge e = new Edge(n, this); 
			e.set_rand_w(-1, 1);
			input_edges.add(e);
			edge_lookup.put(n.id, e);
		}
		Edge edge_bias = new Edge(bias, this);
		bias_edge = edge_bias;
		input_edges.add(bias_edge);
	}
	
	private double sigmoid (double sum) {
		return 1.0 / (1.0 + (Math.exp(-sum)));
	}
	
	
	// ReLU private calc here as well
	/*
	 * 
	 * */
	
}
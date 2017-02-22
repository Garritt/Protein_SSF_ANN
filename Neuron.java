import java.util.*;

public class Neuron {

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
	
	public void activate (double drop_rate) {
		
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
				double weight = e.get_weight() * (1-drop_rate);			
				double prev_output = in.output;
				sum = sum + (weight * prev_output);				
			}	
			// Sigmoid Activation function
			this.output = 1.0/ (1.0 + Math.exp(-sum));
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

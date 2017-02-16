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
		input_layer.addNeurons(input_n, null);
		// Hidden Layers
		Layer prevL = input_layer;
		for (int i = 0; i < h_layers; i++) {
			Layer hl = new Layer();
			hl.addNeurons(hu_per_layer[i], prevL);
			this.hidden_layers.add(hl);
			prevL = hl;
		}
		this.output_layer = new Layer();
		output_layer.addNeurons(output_n, prevL);
	}
	
	void run (int epochs, double minError, DataSets data) {
		
		ArrayList<Protein> train = data.getTrain();
		ArrayList<Protein> tune = data.getTrain();
		ArrayList<Protein> test = data.getTest();
		double error = 1;
		
		for (int i = 0; i < epochs && error > minError; i++) {
			// Start Forward Feed
			for (Protein prot : train) {
				
				// entire input window for this example set
				Window window = prot.getWindow();
				double[][] inputs = window.getInputs();
				double[][] outputs = window.getOutputs();
				
				// Initialize input Layer with Window 
				ArrayList<Neuron> input_units = input_layer.getLayer();
				for (int k = 0; k < input_units.size(); k++) {	
					input_units.get(k).setInputs(inputs[k]);
				}
				// feed forward through all hidden layers
				for (int g = 0; g < this.hidden_layers.size(); g++) {
					Layer h_layer = this.hidden_layers.get(g);
					// All hidden units in this layer
					for (Neuron unit : h_layer.getLayer()) {
						// Use specific output function for input units
						if (g == 1) {
							unit.calc_output_input_layer();
						} else {
							unit.calc_output();							
						}
					}
				}
				
				// feed forward to output layer
				for (Neuron unit : output_layer.getLayer()) {
					
				}
				// Output stuff
				// Error stuff
				// Backprop
			}
		}
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

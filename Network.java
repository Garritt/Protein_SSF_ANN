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
		// set inputs
		for (int i = 0; i < epochs && error > minError; i++) {
			// Start Forward Feed
			for (Protein prot : train) {
				// entire input window for this example set
				ArrayList<double[]> window = prot.getWindow();
				// Initialize input Layer with window 
				
				
				// feed forward through all hidden layers
				for (Layer h_layer : this.hidden_layers) {
					// All hidden units in this layer
					for (Neuron unit : h_layer.getLayer()) {
						// pass 2d array input index to calculate output?
						unit.calculate_output();
					}
				}
				for (Neuron unit : output_layer.getLayer()) {
					
				}
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

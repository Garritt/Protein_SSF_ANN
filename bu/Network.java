import java.util.*;
public class Network {

	
	public Network (int input_n, int output_n, int h_layers, int [] hu_per_layer) {
		
		if (hu_per_layer.length != h_layers) {
			System.err.println("Network Initialization Error: hidden layers does not match hidden unit per layer vector");
		}
	
		// Layer Initialization (includes a random edge weight initialization)
		Layer input_layer = new Layer();
		input_layer.setAsInputLayer();
		input_layer.addNeurons(input_n, null);
		// Hidden Layers
		Layer prevL = input_layer;
		for (int i = 0; i < h_layers; i++) {
			Layer hl = new Layer();
			hl.addNeurons(hu_per_layer[i], prevL);
			prevL = hl;
		}
		Layer output_layer = new Layer();
		output_layer.addNeurons(output_n, prevL);
		
	}
	
	void run (int epochs, double minError) {
		
		double error = 1;
		// set inputs
		for (int i = 0; i < epochs && error > minError; i++) {
			
		}
		
		
	}
	
}

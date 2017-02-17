import java.util.*;

public class Layer {

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
			if (!input_layer) {
				Neuron bias = new Neuron(Neuron_Type.BIAS);
				n.construct_In_Edges(prevL, bias);
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

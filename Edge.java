import java.text.DecimalFormat;

public class Edge {

	double weight;
	double prev_delta_weight; 	//momentum
	double delta_weight;
	
	final Neuron N_in;
	final Neuron N_out;
	
	public Edge (Neuron in, Neuron out) {
		this.N_in = in;
		this.N_out = out;
	}
	
	public void set_weight (double w) {
		weight = w;
	}
	public double get_weight () {
		return weight;
	}
	public Neuron get_N_IN() {
		return this.N_in;
	}
	public Neuron get_N_OUT() {
		return this.N_out;
	}
	public double get_prev_delta_weight(){
		return prev_delta_weight;
	}
	public void set_rand_w(double low, double high){
		DecimalFormat df = new DecimalFormat("#.###");
		double d = low + Math.random() * (high - low); 
		String s = df.format(d);
		weight = Double.parseDouble(s);
	}
}
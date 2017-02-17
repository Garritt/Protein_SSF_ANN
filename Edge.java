import java.text.DecimalFormat;

public class Edge {

	private double weight;
	private double prev_delta_weight; 	//momentum
	
	final Neuron N_in;
	final Neuron N_out;
	
	public Edge (Neuron in, Neuron out) {
		this.N_in = in;
		this.N_out = out;
		prev_delta_weight = 0;
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

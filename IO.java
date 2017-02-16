import java.io.*;
import java.util.*;


public class IO {

	// double[][] set = new double[128][21];


	public static void main(String[] args) {
		DataSets in = readFile(args[0]);

		// attempting to get window
		System.out.println("window attempt");
		int count = 0;
		for (int i = 0; i < in.getTrain().size()-1; i++) {
			
			count++;

			while(in.getTrain().get(i).getWindow() != null){
				
				// System.out.println(count);
				Window temp = in.getTrain().get(i).getWindow();
				if(temp == null) {
					continue;
				}
				// System.out.println("Protein: " + count);
				Double[][] inp = temp.getInputs();
				Double[][] out = temp.getOutputs();

				// for(int j = 0; j < 16; j++) {
				// 	System.out.println("IN");
				// 	for(int k = 0; k < inp[j].length; k++) {
				// 		System.out.print(inp[j][k] + " ");
				// 	}
				// 	for(int k = 0; k < out[j].length; k++) {
				// 		System.out.print(out[j][k] + " ");
				// 	}
				// }
			}
		}
	}


	private static Double[] addAcid(String acid) {
		Double[] ret;
		switch(acid) {
			case "E": ret = new Double[]{1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "I": ret = new Double[]{0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "N": ret = new Double[]{0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "L": ret = new Double[]{0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "K": ret = new Double[]{0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "S": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "G": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "M": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "F": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "R": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "V": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "Y": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "Q": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "P": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "C": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "H": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0};
			break;
			case "W": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0};
			break;
			case "T": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0};
			break;
			case "D": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0};
			break;
			case "A": ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0};
			break;
			default: ret = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0};
		}
		return ret;
	}

	private static Double[] addOutput(String output) {
		Double[] ret;
		switch(output) {
			case "e": ret = new Double[]{1.0,0.0,0.0};
			break;
			case "h": ret = new Double[]{0.0,1.0,0.0};
			break;
			case "-": ret = new Double[]{0.0,0.0,1.0};
			break;
			default: ret = new Double[]{0.0,0.0,0.0};
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
		int linecount = 0;  // obvi the linecount...
		int trueCount = 0;
		int numProteins = 1;
		boolean beginning = true;
		ArrayList<Double[]> temp = new ArrayList<Double[]>();
		ArrayList<Double[]> tempOutputs = new ArrayList<Double[]>();
		ArrayList<Protein> training = new ArrayList<Protein>();
		ArrayList<Protein> tuning = new ArrayList<Protein>();
		ArrayList<Protein> testing = new ArrayList<Protein>();

		while(fileScanner.hasNext()) {
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
				temp = new ArrayList<Double[]>();
				tempOutputs = new ArrayList<Double[]>();
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
	private final Double[][] inputs;
	private final Double[][] target_outputs;


	public Window(Double[][] in, Double[][] out) {
		this.inputs = in;
		this.target_outputs = out;
	}

	public Double[][] getInputs() {
		return this.inputs;
	}

	public Double[][] getOutputs() {
		return this.target_outputs;
	}
}

class Protein {
	public final int num_acids;
	private final ArrayList<Double[]> acids;
	private final ArrayList<Double[]> target_outputs;
	private int top, bottom, middle;


	public Protein(int aminoacids, ArrayList<Double[]> acids, ArrayList<Double[]> outputs) {
		this.num_acids = aminoacids;
		this.acids = acids;
		this.target_outputs = outputs;
		this.top = 8;
		this.middle = 0;
		this.bottom = -9;
	}

	public void printProtein() {
		System.out.println("Amino Acid Sequence");
		for(int i = 0; i < this.acids.size()-1; i++) {
			System.out.print(" " + acids.get(i));
		}
		System.out.println();
		System.out.println("Target Outputs");

		for(int i = 0; i < target_outputs.size()-1; i++) {
			System.out.print(" " + target_outputs.get(i));
		}
		System.out.println();
		System.out.println("######  Size sanity check  ###### ");
		System.out.println("Amino Acid Sequence length: " + this.acids.size());
		System.out.println("Target Output Sequence length: " + target_outputs.size());
		System.out.println();
	}


	public ArrayList<Double[]> getAcid(){
		return this.acids;
	}

	public ArrayList<Double[]> getOutputs(){
		return this.target_outputs;
	}

	public int numAcids(){
		return this.num_acids;
	}

	private void increaseTop() {
		this.top++;
	}
	private void increaseBottom() {
		this.bottom++;
	}
	private void increaseMiddle() {
		this.middle++;
	}


	public Window getWindow(){
		if (top == num_acids-1) {
			return null;
		}
		Double[][] in = new Double[17][21];
		Double[][] out = new Double[17][3];
		int k = 0;

		// for(int i = this.bottom; i < this.top; i++){
		// 	in[k] = this.getAcid().get()
		// }



		if(this.bottom < 0) {
			for(int i = this.bottom; i < 0; i++){
				in[k] = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0};
				out[k] = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0};
				k++;
			}

			for(int j = 0; j < this.top; j++) {
				in[k] = this.getAcid().get(j);
				out[k] = this.getOutputs().get(j);
				k++;
			}

		} else if ((this.num_acids - this.top) < 7) {
			for(int j = this.top; j < this.acids.size()-1; j++) {
				in[k] = this.getAcid().get(j);
				out[k] = this.getOutputs().get(j);
				k++;
			}
			while(k<17) {
				in[k] = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0};
				out[k] = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0};
				k++;
			}
		} else {
			for (int j = this.bottom; j < this.top; j++){
				in[k] = this.getAcid().get(j);
				out[k] = this.getOutputs().get(j);
				k++;
			}
		}
		Window window = new Window(in, out);
		// System.out.print("\t\t\t\t\t\t");
		// for(int i = 0; i < 17; i++){
		// 	System.out.print(window[0][i] + " ");
		// }
		// System.out.println();
		// System.out.print("\t\t\t\t\t\t");

		// for(int i = 0; i < 17; i++){
		// 	System.out.print(window[1][i] + " ");
		// }
		// System.out.println();
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

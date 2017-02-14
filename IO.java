import java.io.*;
import java.util.*;


public class IO {

	// double[][] set = new double[128][21];


	public static void main(String[] args) {
		DataSets in = readFile(args[0]);

		// System.out.println("We read in " + in.size() + " proteins");

		// for(int i = 0; i < in.getTrain().size()-1; i++)
		// 	in.getTrain().get(i).printProtein();

		// attempting to get window
		System.out.println("window attempt");
		while(in.getTrain().get(0).getWindow() != null){
			String[] temp = in.getTrain().get(0).getWindow();
			// for(int i = 0; i < temp.length; i++) {
			// 	System.out.print(temp[i] + " ");
			// }
			// System.out.println();
		}
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
		ArrayList<String> temp = new ArrayList<String>();
		ArrayList<String> tempOutputs = new ArrayList<String>();
		ArrayList<Protein> training = new ArrayList<Protein>();
		ArrayList<Protein> tuning = new ArrayList<Protein>();
		ArrayList<Protein> testing = new ArrayList<Protein>();

		while(fileScanner.hasNext()) {
			String line = fileScanner.nextLine().trim();
			trueCount++;


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
			// System.out.print("Linecount: " + linecount + " ");

			
			if((line.equals("end") || line.equals("<end>")) || (line.equals("<>") && !beginning) && linecount > 1) {
				// the protein is now complete
				// System.out.println(linecount + " < - linecount");
				Protein prot = new Protein(linecount, temp, tempOutputs);
				temp = new ArrayList<String>();
				tempOutputs = new ArrayList<String>();
				linecount = 0;
				// System.out.println("Protein Number: " + numProteins);
				numProteins++;
				beginning = true;
				// prot.printProtein();
				if (trueCount % 5 == 0) {
					tuning.add(prot);
				} else if (trueCount % 5 == 1) {
					testing.add(prot);
				} else {
					training.add(prot);
				}
				// System.out.println("\n\n\nTrue Count: " + trueCount);
				
			}
			int wordcount = 0;
			while(wordscanner.hasNext()) {
				String word = wordscanner.next().trim();
				if(word.equals("<>")) {
					continue;
				}
				if (wordcount == 0 && !word.equals("<end>")) {
					// System.out.println("WORD YO : " + word);
					temp.add(word);
				} else if (wordcount > 0  && !word.equals("<end>")) {
					
					tempOutputs.add(word);
				}
				// System.out.print(word + " ");
				wordcount++;
			}
			linecount++;

			// System.out.println();

		}
		DataSets data = new DataSets(training, tuning, testing);
		return data;

	}

}

class Protein {
	public final int num_acids;
	public final ArrayList<String> acids;
	public final ArrayList<String> target_outputs;
	private int top, bottom, middle;
	// private String[] window;


	public Protein(int aminoacids, ArrayList<String> acids, ArrayList<String> outputs) {
		this.num_acids = aminoacids;
		this.acids = acids;
		this.target_outputs = outputs;
		this.top = 8;
		this.middle = 0;
		this.bottom = -9;
		// this.window = new String[17];
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


	public ArrayList<String> getAcid(){
		return this.acids;
	}

	public ArrayList<String> getOutputs(){
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

	public String[] getWindow(){
		System.out.println("NUM ACIDS: " + num_acids);
		if (top == num_acids-1) {
			return null;
		}
		String[] window = new String[17];
		int k = 0;

		System.out.println("Bottom: " + this.bottom);
		System.out.println("Middle: " + this.middle);
		System.out.println("Top: " + this.top);

		if(this.bottom < 0) {
			for(int i = this.bottom; i < 0; i++){
				window[k] = "-";
				k++;
			}
			// window[k] = this.getAcid().get(middle);
			// k++;
			for(int j = 0; j < this.top; j++) {
				window[k] = this.getAcid().get(j);
				k++;
			}

		} else if ((this.num_acids - this.top) < 7) {
			for(int j = this.top; j < this.num_acids-1; j++) {
				window[k] = this.getAcid().get(j);
				k++;
			}
			while(k<17) {
				window[k] = "-";
				k++;
			}
		} else {
			for (int j = this.bottom; j < this.top; j++){
				window[k] = this.getAcid().get(j);
				k++;
			}
		}

		for(int i = 0; i < 17; i++){
			System.out.print(window[i] + " ");
		}
		System.out.println();
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

import java.io.*;
import java.util.*;


public class IO {

	// double[][] set = new double[128][21];



	public static void main(String[] args) {
		readFile(args[0]);
	}

	public static void readFile(String filename) {
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
				System.out.println(linecount + " < - linecount");
				Protein prot = new Protein(linecount, temp, tempOutputs);
				temp = new ArrayList<String>();
				tempOutputs = new ArrayList<String>();
				linecount = 0;
				System.out.println("Protein Number: " + numProteins);
				numProteins++;
				beginning = true;
				prot.printProtein();
				System.out.println("\n\n\nTrue Count: " + trueCount);
				
			}
			int wordcount = 0;
			while(wordscanner.hasNext()) {
				String word = wordscanner.next().trim();
				if(word.equals("<>")) {
					continue;
				}
				if (wordcount == 0) {
					// System.out.println("WORD YO : " + word);
					temp.add(word);
				} else {
					
					tempOutputs.add(word);
				}
				// System.out.print(word + " ");
				wordcount++;
			}
			linecount++;

			// System.out.println();

		}

	}

}

class Protein {
	public final int num_acids;
	public final ArrayList<String> acids;
	public final ArrayList<String> target_outputs;


	public Protein(int aminoacids, ArrayList<String> acids, ArrayList<String> outputs) {
		this.num_acids = aminoacids;
		this.acids = acids;
		this.target_outputs = outputs;
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
	}

}

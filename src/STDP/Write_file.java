package STDP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import constants.Constants;
import readdata.ReadData;

public class Write_file {

	public static void writePi(String filename, double[][] pi) throws IOException {
		// System.out.println("Writing Pi of learner" + this.indexLeaner);
		File f = new File(Constants.output_folder + filename + ".csv");
		boolean check = false;
		if (!f.exists()) {
			f.createNewFile();
			check = true;
		}
		PrintWriter out = new PrintWriter(new FileWriter(f, true));
		if (check) {
			for (int s = 0; s < Constants.NUM_SENTIMENTS; s++)
				out.print(",S" + s);
			out.println();
		}

		for (int i = 0; i < pi.length; i++) {
			for (int s = 0; s < Constants.NUM_SENTIMENTS; s++) {
				out.print("," + pi[i][s]);
			}
			out.println();
		}

		out.println();
		out.close();
	}

	private static int maxIndex(double[] s) {
		int index = 0;
		for (int i = 1; i < s.length; i++) {
			if (s[i] > s[index])
				index = i;
		}
		return index;
	}

	private static int[][][] topWord(int top, double[][][] phi) {
		int num_terms = ReadData.all_Word.size();
		int[][][] topWord = new int[Constants.NUM_SENTIMENTS + 1][Constants.NUM_ASPECTS][top];
		double[][][] cPhi = new double[Constants.NUM_SENTIMENTS + 1][Constants.NUM_ASPECTS][num_terms];
		for (int t = 0; t < Constants.NUM_ASPECTS; t++)
			for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++)
				for (int v = 0; v < num_terms; v++) {
					cPhi[s][t][v] = phi[s][t][v];
				}
		for (int j = 0; j < top; j++)
			for (int t = 0; t < Constants.NUM_ASPECTS; t++)
				for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++) {
					int index = maxIndex(cPhi[s][t]);
					topWord[s][t][j] = index;
					cPhi[s][t][index] = -1;
				}
		return topWord;
	}

	public static void writePhi(String filename, double[][][] phi) throws IOException {
		System.out.println("Writing Phi");
		int num_terms = ReadData.all_Word.size();
		int numCheck = 0;
		int numCorrect = 0;
		File f = new File(Constants.output_folder + filename + ".csv");
		if (!f.exists()) {
			f.createNewFile();
		}
		PrintWriter out = new PrintWriter(new FileWriter(f));
		for (int t = 0; t < Constants.NUM_ASPECTS; t++)
			for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++)
				out.print(",T" + t + "-S" + s);
		out.println();
		double[][] a = new double[Constants.NUM_SENTIMENTS + 1][Constants.NUM_ASPECTS];
		for (int j = 0; j < num_terms; j++) {
			for (int t = 0; t < Constants.NUM_ASPECTS; t++) {
				for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++) {
					out.print("," + phi[s][t][j]);
					a[s][t] += phi[s][t][j];
				}
			}
			out.println();
		}
		for (int t = 0; t < Constants.NUM_ASPECTS; t++) {
			for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++) {
				numCheck += 1;
				if (a[s][t] == 1) {
					numCorrect += 1;
				} else {
					// System.out.println("ERROR : PHI " + a[s][t]);
				}
			}
		}

		// System.out.println("CHECK PHI : " + "NumCheck : " + numCheck + ",
		// numCorrect : " + numCorrect);

		out.close();
	}

	public static void writeTopWord(String filename, int top, double[][][] phi) throws IOException {
		// filename == "Phi.csv"
		ArrayList<String> all_Word = ReadData.all_Word;
		System.out.println("Writing top word");
		File f = new File(Constants.output_folder + filename + ".csv");
		if (!f.exists()) {
			f.createNewFile();
		}
		PrintWriter out = new PrintWriter(new FileWriter(f));
		for (int t = 0; t < Constants.NUM_ASPECTS; t++)
			for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++)
				out.print(",T" + t + "-S" + s);
		out.println();
		int[][][] topWord = topWord(top, phi);
		for (int j = 0; j < top; j++) {
			for (int t = 0; t < Constants.NUM_ASPECTS; t++)
				for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++) {
					out.print("," + all_Word.get(topWord[s][t][j]));
				}
			out.println();
		}
		out.close();
	}

	public static void writeTheta(String filename, double[][][] theta) throws IOException {
		// System.out.println("Writing Theta of learner" + this.indexLeaner);
		File f = new File(Constants.output_folder + filename + ".csv");
		boolean check = false;
		if (!f.exists()) {
			f.createNewFile();
			check = true;
		}
		PrintWriter out = new PrintWriter(new FileWriter(f, true));
		if (check) {
			for (int t = 0; t < Constants.NUM_ASPECTS; t++)
				for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++)
					out.print(",T" + t + "-S" + s);
			out.println();
		}
		int numCheck = 0;
		int numCorrect = 0;
		for (int i = 0; i < theta.length; i++) {
			double[] a = new double[Constants.NUM_SENTIMENTS + 1];
			for (int t = 0; t < Constants.NUM_ASPECTS; t++)
				for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++) {
					a[s] += theta[i][s][t];
					out.print("," + theta[i][s][t]);
				}
			for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++) {
				numCheck += 1;
				if (a[s] == 1) {
					numCorrect += 1;
				} else {
					// System.out.println("ERROR : THETA " + a[s]);
				}
			}
			out.println();
		}
		// System.out.println("CHECK THETA : " + "NumCheck : " + numCheck + ",
		// numCorrect : " + numCorrect);
		out.close();
	}

}

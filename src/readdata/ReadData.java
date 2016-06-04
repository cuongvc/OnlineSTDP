package readdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map.Entry;

import STDP.Learn;
import constants.Constants;

public class ReadData {

	public static ArrayList<ArrayList<ArrayList<Integer>>> all_Doc = new ArrayList<ArrayList<ArrayList<Integer>>>();
	public static ArrayList<ArrayList<ArrayList<String>>> all_Pos = new ArrayList<ArrayList<ArrayList<String>>>();
	public static ArrayList<Integer> numWordInDoc = new ArrayList<Integer>();
	public static ArrayList<String> all_Word = new ArrayList<String>();

	public static ArrayList<int[]> countSinD;
	public static Hashtable<String, Integer> word_To_Index = new Hashtable<String, Integer>();
	public static ArrayList<Integer> labels;
	public static double[][] beta;
	public static boolean lockRead = false;
	public static boolean endRead = false;
	public static int numLongSentence = 0;
	public static double[] sumBeta;
	public static int[] numberWordInGroup = new int[5];

	private static void initPosNeg(int size) {
		beta = new double[Constants.NUM_SENTIMENTS + 1][size];
		Arrays.fill(beta[0], Constants.NEUTRAL);
		Arrays.fill(beta[1], Constants.NEUTRAL);
		Arrays.fill(beta[2], Constants.NEUTRAL);

	}

	public static void initAll() {
		init();
		all_Word = new ArrayList<String>();
		word_To_Index = new Hashtable<String, Integer>();
	}

	public static void init() {
		all_Doc = new ArrayList<ArrayList<ArrayList<Integer>>>();
		all_Pos = new ArrayList<ArrayList<ArrayList<String>>>();
	}

	@SuppressWarnings("resource")
	// READ BAG OF SENTENCE WITH MINIBATCH SIZE
	public static void read_Bag_Of_Sentence(BufferedReader inputBagOfSentence, BufferedReader inputPOS)
			throws IOException {
		all_Doc = new ArrayList<ArrayList<ArrayList<Integer>>>();
		all_Pos = new ArrayList<ArrayList<ArrayList<String>>>();
		numWordInDoc = new ArrayList<Integer>();
		String str = "";
		int numDoc = 0;
		while ((str = inputBagOfSentence.readLine()) != null) {
			int numWord = 0;
			inputPOS.readLine();
			String[] outersplit = str.split("\\s+");
			if (outersplit.length > 1) {
				System.out.print("Input wrong format");
				return;
			}

			int numSen = Integer.parseInt(outersplit[0]);

			String line = "";
			String linePos = "";
			ArrayList<ArrayList<Integer>> currentDoc = new ArrayList<ArrayList<Integer>>();
			ArrayList<ArrayList<String>> currentPos = new ArrayList<ArrayList<String>>();

			for (int sen = 0; sen < numSen; sen++) {
				ArrayList<Integer> sentence = new ArrayList<Integer>();
				ArrayList<String> sentencePos = new ArrayList<String>();
				line = inputBagOfSentence.readLine();
				linePos = inputPOS.readLine();
				String[] wordsplit = line.split("\\s+");
				String[] POSsplit = linePos.split("\\s+");
				for (int index = 0; index < wordsplit.length; index++) {
					int i = Integer.parseInt(wordsplit[index]);
					String pos = POSsplit[index];
					int group = Learn.groupOfPos(pos);
					numberWordInGroup[group] += 1;
					numWord += 1;
					// if (group != 4) {
					sentence.add(i);
					sentencePos.add(pos);
					// }

				}
				currentDoc.add(sentence);
				currentPos.add(sentencePos);
			}

			numDoc++;
			numWordInDoc.add(numWord);
			all_Doc.add(currentDoc);
			all_Pos.add(currentPos);
			if (numDoc == Constants.MINIBATCH_SIZE) {
				System.out.println("NUMDOC : " + numDoc);
				return;
			}

		}
		if (numDoc < Constants.MINIBATCH_SIZE)
			ReadData.lockRead = true;
		// System.out.println("Number Document : " + all_Doc.size());
		// for (int i = 0 ; i < 5; i ++){
		// System.out.println("Group " + i + ": " + numberWordInGroup[i]);
		// }
		// System.in.read();
	}

	public static void read_Vocab_File(String vocab_File) throws IOException {
		int currentIndex = 0;
		BufferedReader input = new BufferedReader(new FileReader(vocab_File));
		String str = "";
		while ((str = input.readLine()) != null) {
			String word = str.split("\\s+")[0];
			all_Word.add(word);
			word_To_Index.put(word, currentIndex);
			currentIndex++;
		}
		input.close();
	}

	public static void read_Sentiment_Dic(String pos_Dic, String neg_Dic) throws IOException {
		// read_Vocab_File(total_Dic);
		BufferedReader inputPos = new BufferedReader(new FileReader(pos_Dic));
		BufferedReader inputNeg = new BufferedReader(new FileReader(neg_Dic));
		int lenght = all_Word.size();
		initPosNeg(lenght);
		String strPos = "";
		while ((strPos = inputPos.readLine()) != null) {
			String word = strPos.split("\\s+")[0];
			if (word_To_Index.containsKey(word)) {
				int wordIndex = word_To_Index.get(word);
				beta[0][wordIndex] = Constants.SAME_TYPE;
				beta[1][wordIndex] = Constants.DIFFERENT_TYPE;
				beta[2][wordIndex] = Constants.DIFFERENT_TYPE;
			}
		}
		String strNeg = "";
		while ((strNeg = inputNeg.readLine()) != null) {
			String word = strNeg.split("\\s+")[0];
			if (word_To_Index.containsKey(word)) {
				int wordIndex = word_To_Index.get(word);
				beta[0][wordIndex] = Constants.DIFFERENT_TYPE;
				beta[1][wordIndex] = Constants.SAME_TYPE;
				beta[2][wordIndex] = Constants.DIFFERENT_TYPE;
			}
		}
		sumBeta = new double[Constants.NUM_SENTIMENTS + 1];
		for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++) {
			for (int v = 0; v < ReadData.all_Word.size(); v++) {
				sumBeta[s] += beta[s][v];
			}
		}

		inputPos.close();
		inputNeg.close();
	}

	public static void read_Label(String label_file) throws NumberFormatException, IOException {
		BufferedReader input = new BufferedReader(new FileReader(label_file));
		// Constants.NUM_DOC = (int) input.lines().count();
		input = null;
		input = new BufferedReader(new FileReader(label_file));
		System.out.println(Constants.NUM_DOC);
		labels = new ArrayList<Integer>();
		String str = "";
		int i = 0;
		while ((str = input.readLine()) != null) {
			String label = str.split("\\s+")[0];
			labels.add(Integer.parseInt(label));
			i++;
		}
		input.close();
	}

}

package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import STDP.Learn;
import STDP.Write_file;
import constants.Constants;
import readdata.ReadData;

public class RunStreamingLearning {

	public static Learn learn;

	public static double calc_accuracy(ArrayList<Integer> labels_predict) {
		int numCorrect = 0;
		int numDoc = 0;
		for (int i = 0; i < labels_predict.size(); i++) {
			if (ReadData.labels.get(i) != 0) {
				numDoc += 1;
				if (labels_predict.get(i) == ReadData.labels.get(i)) {
					numCorrect += 1;
				}
			}
		}
		double accuracy = numCorrect / (float) numDoc * 100;
		return accuracy;
	}

	public static double[] recall(ArrayList<Integer> labels, ArrayList<Integer> predicted_labels) {
		double[] a = new double[3];
		if (labels.size() != predicted_labels.size() || labels.size() == 0) {
			System.err.print("Can't compute recall and accuracy");
			a[0] = a[1] = a[2] = -1;
			return a;
		}
		double num_pos = 0, num_neg = 0, pos_true = 0, neg_true = 0;
		int num_labels = labels.size();
		for (int i = 0; i < num_labels; i++) {
			if (labels.get(i) == 1) {
				num_pos += 1;
				if (predicted_labels.get(i) == 1)
					pos_true += 1;
			}
			if (labels.get(i) == -1) {
				num_neg += 1;
				if (predicted_labels.get(i) == -1)
					neg_true += 1;
			}
		}
		a[0] = pos_true / num_pos;
		a[1] = neg_true / num_neg;
		a[2] = (pos_true + neg_true) / (num_neg + num_pos);
		return a;
	}

	public static double[] precision(ArrayList<Integer> labels, ArrayList<Integer> predicted_labels) {
		double[] a = new double[2];
		System.out.println(labels.size() + " " + predicted_labels.size());
		if (labels.size() != predicted_labels.size() || labels.size() == 0) {
			System.err.print("Can't compute precision");
			a[0] = a[1] = -1;
			return a;
		}
		double num_pos = 0, num_neg = 0, pos_true = 0, neg_true = 0;
		int num_labels = labels.size();
		for (int i = 0; i < num_labels; i++) {
			if (labels.get(i) != 0) {
				if (predicted_labels.get(i) == 1) {
					num_pos += 1;
					if (labels.get(i) == 1)
						pos_true += 1;
				}
				if (predicted_labels.get(i) == -1) {
					num_neg += 1;
					if (labels.get(i) == -1)
						neg_true += 1;
				}
			}
		}
		a[0] = pos_true / num_pos;
		a[1] = neg_true / num_neg;
		return a;
	}

	public static void write_file(Learn learn, int i) throws IOException {
		Write_file.writePi("Pi", learn.pi);
		Write_file.writeTheta("Theta", learn.theta);
		Write_file.writeTopWord("TopWord" + i + ".csv", 1000, learn.phi);
		Write_file.writePhi("Phi" + i + ".csv", learn.phi);
	}

	public static void write_result(String folder, ArrayList<Integer> labels_predict, Learn learn, long duration)
			throws IOException {
		System.out.println(labels_predict);
		double accuracy = calc_accuracy(labels_predict);
		System.out.println("ACCURACY : " + accuracy);
		double[] precision = precision(ReadData.labels, labels_predict);
		double[] recall = recall(ReadData.labels, labels_predict);
		BufferedWriter br2 = new BufferedWriter(new FileWriter(new File(folder + "result.txt")));
		br2.append("recall:  " + recall[0] + ";;" + recall[1] + "\n accuracy: " + recall[2] + "\n" + "precision: "
				+ precision[0] + ";;" + precision[1] + "\n");
		br2.append("NUMBER WORD ASSIGNED TO SENTI-TOPIC : " + learn.word_has_sentiment + "\n");
		br2.append("NUMBER WORD ASSIGNED TO NON-SENTI-TOPIC : " + learn.word_has_not_sentiment);
		br2.append("\nTimerun : " + duration / (float) 1000000000);
		br2.close();

		// Write_file.writeTopWord("TopWord.csv", 1000, learn.phi);
	}

	// READ GENERAl DATA
	public static void readGeneralData(String[] dataFolderSettings, int numFolder, int count) throws Exception {
		ReadData.initAll();

		// READ SETTING FILE
		System.out.println("Reading Settings file");
		Constants.readSetting("output/" + dataFolderSettings[numFolder] + "/settings" + count + ".txt");

		// READ WORDLIST FILE
		System.out.println("Reading WordList file:" + Constants.WORD_LIST_FILE);
		ReadData.read_Vocab_File(Constants.WORD_LIST_FILE);

		// READ LABEL
		System.out.println("Reading labels file:" + Constants.LABEL_FILE);
		ReadData.read_Label(Constants.LABEL_FILE);

		// READ SENTIMENT FILE
		System.out
				.println("Reading sentiment file:" + Constants.SENTIMENT_FILE[0] + ";;" + Constants.SENTIMENT_FILE[1]);
		ReadData.read_Sentiment_Dic(Constants.SENTIMENT_FILE[0], Constants.SENTIMENT_FILE[1]);

		// INIT PHI
	}

	public static void main(String[] args) throws Exception {
		// String[] dataFolderSettings = new String[] { "amazon", "yelp",
		// "automotive", "electronics", "apparel", "baby", "beauty",
		// "kitchen_&_housewares" };
		String[] dataFolderSettings = new String[] { "baby", "camera_photo", "cell_phones_service", "video", "beauty" };

		for (int numFolder = 0; numFolder < 5; numFolder++) {
			String filename = "output/" + dataFolderSettings[numFolder] + "/";
			int numSettings = 1;
			int first_count = 1;
			int count = first_count;
			BufferedReader input_doc_file = null;
			BufferedReader input_pos_file = null;
			while (count <= numSettings) {
				// if (count == first_count) {
				readGeneralData(dataFolderSettings, numFolder, count);
				// }
				learn = new Learn();
				learn.initPhi(ReadData.beta[0]);

				Constants.readSetting("output/" + dataFolderSettings[numFolder] + "/settings" + count + ".txt");

				int iter_train = 1;
				String folder = filename + Constants.output_folder;
				System.out.println("folder : " + folder);
				// labels_predict
				ArrayList<Integer> labels_predict;
				int numLearn = 1;
				for (int i = 0; i < iter_train; i++) {
					long startTime = System.nanoTime();

					labels_predict = new ArrayList<Integer>();
					System.out.println("iterator: " + (i + 1));

					// READ FILE
					input_doc_file = new BufferedReader(new FileReader(Constants.DOC_FILE));
					input_pos_file = new BufferedReader(new FileReader(Constants.POS_FILE));

					ReadData.lockRead = false;
					int minibatch = 1;
					File file = new File(filename + Constants.output_folder);
					if (!file.exists())
						file.mkdir();

					Constants.output_folder = folder + "lan" + (i + 1) + "/";

					File file2 = new File(Constants.output_folder);
					if (!file2.exists())
						file2.mkdir();

					while (true) {
						double pt = Math.pow((Constants.tau0 + numLearn), -1 * Constants.kappa);

						ReadData.init();
						// READ ONLY THIS MINIBATCH
						ReadData.read_Bag_Of_Sentence(input_doc_file, input_pos_file);
						System.out.println("Reading BagOfSentence file for minibatch " + minibatch
								+ ", Size this minibatch is : " + ReadData.all_Doc.size());

						int[] labels_batch = new int[ReadData.all_Doc.size()];
						/*
						 * labels_batch: labels of documents in minibatch, from
						 * value of PI
						 */
						labels_batch = learn.learnMiniBatch(pt);

						// if it's last iterator, save predicted labels
						// if (i == iter_train - 1) {
						for (int j = 0; j < ReadData.all_Doc.size(); j++) {
							labels_predict.add(labels_batch[j]);
						}
						// }

						numLearn += 1;
						// Finish reading all document
						if (ReadData.lockRead == true) {
							break;
						}
						minibatch++;
					}
					write_file(learn, numLearn);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime); // divide by 1000000
															// to get
					write_result(Constants.output_folder, labels_predict, learn, duration);

					input_doc_file.close();
				}
				count += 1;
			}
		}
		System.out.println("NUMBER WORD ASSIGNED TO SENTI-TOPIC : " + learn.word_has_sentiment);
		System.out.println("NUMBER WORD ASSIGNED TO NON-SENTI-TOPIC : " + learn.word_has_not_sentiment);
	}
}

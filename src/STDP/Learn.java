package STDP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Map.Entry;

import constants.Constants;
import readdata.ReadData;

public class Learn {
	public static double[][][] phi;
	public static double[][][] lambda;
	public double[][] pi;

	public double[][][] theta;
	public static ArrayList<ArrayList<Integer>> Z, S;
	public static int[][] N_p = new int[5][2];
	public static IntegerMatrix C_ds;
	public static IntegerMatrix C_st;
	public static IntegerMatrix[] C_dst;
	public static IntegerMatrix[] C_stw;

	public static IntegerMatrix C_ds_mean;
	public static IntegerMatrix C_st_mean;
	public static IntegerMatrix[] C_dst_mean;
	public static IntegerMatrix[] C_stw_mean;
	public static IntegerMatrix[] S_dns_mean;
	public static IntegerMatrix[] Z_dnt_mean;
	public static IntegerMatrix[] F_n_mean;
	public static IntegerMatrix F_n;

	public static int word_has_sentiment = 0;
	public static int word_has_not_sentiment = 0;

	public static double[][] delta = new double[2][5];

	public static int sampleCorrect = 0;
	public static int sampleInCorrect = 0;
	public static int sampleCorrect_topic = 0;
	public static int sampleInCorrect_topic = 0;
	public static int sampleCorrect_senti_topic = 0;
	public static int sampleInCorrect_senti_topic = 0;

	public static int V, numDoc;

	public static int groupOfPos(String postag) {
		int group;
		if (postag.substring(0, 1).equals("J")) {
			group = 0;
		} else if (postag.substring(0, 1).equals("R")) {
			group = 1;
		} else if (postag.substring(0, 1).equals("V")) {
			group = 2;
		} else if (postag.substring(0, 1).equals("N")) {
			group = 3;
		} else {
			group = 4;
		}
		return group;
	}

	public static double[] make_Cumprobs(int numberSeg) {
		double[] cumprobs = new double[numberSeg];
		double pro = 0;
		for (int i = 0; i < numberSeg; i++) {
			cumprobs[i] = 1.0 / (double) numberSeg;
			pro += 1.0 / (double) numberSeg;
		}
		return cumprobs;
	};

	public static int maxIndex(double[] proArray) {
		int index = 0;
		double maxValue = proArray[0];
		for (int i = 1; i < proArray.length; i++) {
			if (maxValue < proArray[i]) {
				index = i;
				maxValue = proArray[i];
			}
		}
		return index;
	}

	public static int[] maxIndex(double[][] proTable) {
		int[] index = new int[2];
		double maxValue = proTable[0][0];
		index[0] = 0;
		index[1] = 1;
		for (int i = 0; i < proTable.length; i++) {
			for (int j = 0; j < proTable[i].length; j++) {
				if (maxValue < proTable[i][j]) {
					maxValue = proTable[i][j];
					index[0] = i;
					index[1] = j;
				}
			}
		}
		return index;
	}

	public static int binarySearch(double uni_rv, double[] proArray) {
		double[] cumprobs = new double[proArray.length];
		cumprobs[0] = 0;
		for (int i = 1; i < cumprobs.length; i++) {
			cumprobs[i] += cumprobs[i - 1] + proArray[i - 1];
		}

		int index = 0;
		int rc_start = 0;
		int rc_stop = cumprobs.length;
		while (rc_start < rc_stop - 1) {
			int rc_mid = (rc_start + rc_stop) / 2;
			if (cumprobs[rc_mid] <= uni_rv) {
				rc_start = rc_mid;
			} else {
				rc_stop = rc_mid;
			}
		}
		index = rc_start;

		return index;
	}

	public static int[] searchElement(double uni_rv, double[][] proTable) {
		int[] index = new int[2];
		index[0] = 0;
		index[1] = 0;
		double sum = 0;
		int u = 0;
		int v = 0;
		int n = proTable.length;
		for (int i = 0; i < n; i++) {
			int m = proTable[i].length;
			for (int j = 0; j < m; j++) {
				if (sum <= uni_rv && sum + proTable[i][j] > uni_rv) {
					index[0] = i;
					index[1] = j;
					return index;
				}
				sum += proTable[i][j];
			}
		}
		return index;
	}

	public static double make_uniform() {
		Random random = new Random();
		double randomValue = random.nextDouble();
		return randomValue;
	}

	public static void initSZ() {
		S = new ArrayList<ArrayList<Integer>>();
		Z = new ArrayList<ArrayList<Integer>>();
		C_ds = new IntegerMatrix(numDoc, Constants.NUM_SENTIMENTS + 1);
		C_st = new IntegerMatrix(Constants.NUM_SENTIMENTS + 1, Constants.NUM_ASPECTS);
		C_dst = new IntegerMatrix[numDoc];
		C_stw = new IntegerMatrix[Constants.NUM_SENTIMENTS + 1];

		C_ds_mean = new IntegerMatrix(numDoc, Constants.NUM_SENTIMENTS + 1);
		C_st_mean = new IntegerMatrix(Constants.NUM_SENTIMENTS + 1, Constants.NUM_ASPECTS);
		C_dst_mean = new IntegerMatrix[numDoc];
		C_stw_mean = new IntegerMatrix[Constants.NUM_SENTIMENTS + 1];
		S_dns_mean = new IntegerMatrix[numDoc];
		Z_dnt_mean = new IntegerMatrix[numDoc];
		F_n_mean = new IntegerMatrix[numDoc];
		for (int i = 0; i < numDoc; i++) {
			C_dst[i] = new IntegerMatrix(Constants.NUM_SENTIMENTS + 1, Constants.NUM_ASPECTS);
			C_dst_mean[i] = new IntegerMatrix(Constants.NUM_SENTIMENTS + 1, Constants.NUM_ASPECTS);
			int numWordInDoc = ReadData.numWordInDoc.get(i);
			S_dns_mean[i] = new IntegerMatrix(numWordInDoc, Constants.NUM_SENTIMENTS + 1);
			Z_dnt_mean[i] = new IntegerMatrix(numWordInDoc, Constants.NUM_ASPECTS);
			F_n_mean[i] = new IntegerMatrix(numWordInDoc, 2);
		}
		for (int i = 0; i < Constants.NUM_SENTIMENTS + 1; i++) {
			C_stw[i] = new IntegerMatrix(Constants.NUM_ASPECTS, ReadData.all_Word.size());
			C_stw_mean[i] = new IntegerMatrix(Constants.NUM_ASPECTS, ReadData.all_Word.size());
		}

		F_n = new IntegerMatrix(V, 2);

		// Init S,Z by uniform distribution
		for (int i = 0; i < numDoc; i++) {
			S.add(new ArrayList<Integer>());
			Z.add(new ArrayList<Integer>());
			int numSentence = ReadData.all_Doc.get(i).size();
			int indexWord = 0;
			for (int j = 0; j < numSentence; j++) {
				for (int w = 0; w < ReadData.all_Doc.get(i).get(j).size(); w++) {
					int iWordlist = ReadData.all_Doc.get(i).get(j).get(w);
					String postag = ReadData.all_Pos.get(i).get(j).get(w);
					int group = groupOfPos(postag);

					// random Sentiment and Topic for each Word
					double[] cumprobs = make_Cumprobs(Constants.NUM_SENTIMENTS + 1);
					int SInit = binarySearch(make_uniform(), cumprobs);
					cumprobs = make_Cumprobs(Constants.NUM_ASPECTS);
					int ZInit = binarySearch(make_uniform(), cumprobs);
					// assign SInit and ZInt for word iWordList in document i
					S.get(i).add(SInit);
					Z.get(i).add(ZInit);
					C_ds.incValue(i, SInit);
					C_st.incValue(SInit, ZInit);
					C_dst[i].incValue(SInit, ZInit);
					C_stw[SInit].incValue(ZInit, iWordlist);
					int senti_topic = 1;
					if (SInit == Constants.NUM_SENTIMENTS) {
						senti_topic = 0;
					}
					N_p[group][senti_topic] += 1;
					indexWord += 1;
				}
			}
		}
	}

	public void burn_in() throws IOException {
		// Burn-in phase
		for (int sim = 0; sim < Constants.burn_in + Constants.sampling; sim++) {
			word_has_sentiment = 0;
			word_has_not_sentiment = 0;
			for (int i = 0; i < numDoc; i++) {
				// i: document Index
				int wordIndex = 0;
				int numSent = ReadData.all_Doc.get(i).size();
				for (int j = 0; j < numSent; j++) {
					// j : sentence Index
					int numWord = ReadData.all_Doc.get(i).get(j).size();
					for (int w = 0; w < numWord; w++) {
						// params int sim, int docIndex, int wordIndex, int
						// iWordList, String postag

						// Sampling for a word in a doc
						sample(sim, i, wordIndex, ReadData.all_Doc.get(i).get(j).get(w),
								ReadData.all_Pos.get(i).get(j).get(w));
						wordIndex += 1;
					}
				}
			}

		}
	}

	public static double[][] normalization(double[][] proTable) {
		double[][] newProTable = new double[Constants.NUM_SENTIMENTS + 1][Constants.NUM_ASPECTS];
		double sumI = 0;
		double sumII = 0;
		for (int i = 0; i < Constants.NUM_SENTIMENTS; i++) {
			for (int j = 0; j < Constants.NUM_ASPECTS; j++) {
				sumI += proTable[i][j];
			}
		}
		for (int j = 0; j < Constants.NUM_ASPECTS; j++) {
			sumII += proTable[Constants.NUM_SENTIMENTS][j];
		}
		for (int i = 0; i < Constants.NUM_SENTIMENTS; i++) {
			for (int j = 0; j < Constants.NUM_ASPECTS; j++) {
				newProTable[i][j] = proTable[i][j] / (double) sumI;
			}
		}
		for (int j = 0; j < Constants.NUM_ASPECTS; j++) {
			newProTable[Constants.NUM_SENTIMENTS][j] = proTable[Constants.NUM_SENTIMENTS][j] / (double) sumII;
		}
		return newProTable;
	}

	public static double[][] makeProTable(int docIndex, int wordIndex, int iWordList, String postag) {
		double[][] proTable = new double[Constants.NUM_SENTIMENTS + 1][Constants.NUM_ASPECTS];
		int group = groupOfPos(postag);
		// senti-topic = 0: false
		// senti-topic = 1: true
		int senti_topic;
		int sumC_ds = 0;
		for (int i = 0; i < Constants.NUM_SENTIMENTS; i++) {
			sumC_ds += C_ds.getValue(docIndex, i);
		}

		// senti-topic
		for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++) {
			for (int t = 0; t < Constants.NUM_ASPECTS; t++) {
				double partI = (Constants.alpha + C_dst[docIndex].getValue(s, t))
						/ (double) (Constants.NUM_ASPECTS * Constants.alpha + C_ds.getValue(docIndex, s));

				// Calc Sum(beta[s][w']) by w'
				double partII = phi[s][t][iWordList];

				// partIII: only sentiment-topic
				double partIII = (Constants.gamma + C_ds.getValue(docIndex, s))
						/ (double) (sumC_ds + Constants.NUM_SENTIMENTS * Constants.gamma);
				if (s < Constants.NUM_SENTIMENTS) {
					senti_topic = 1;
				} else {
					senti_topic = 0;
				}

				double partIV = (N_p[group][senti_topic] + delta[senti_topic][group])
						/ (double) (10 + N_p[group][0] + N_p[group][1]);
				proTable[s][t] = partI * partII * partIV;
				if (s < Constants.NUM_SENTIMENTS) {
					proTable[s][t] *= partIII;
				}
			}
		}
		return proTable;

	}

	public static double SUM(double[][] matrix_2D) {
		double sum = 0;
		for (int i = 0; i < matrix_2D.length; i++) {
			for (int j = 0; j < matrix_2D[i].length; j++) {
				sum += matrix_2D[i][j];
			}
		}
		return sum;
	}

	public static double SUM(double[] array) {
		double sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return sum;
	}

	public static void sample(int sim, int docIndex, int wordIndex, int iWordList, String postag) throws IOException {
		int count = 0;
		int group = groupOfPos(postag);
		int oldS = S.get(docIndex).get(wordIndex);
		int oldZ = Z.get(docIndex).get(wordIndex);
		C_ds.decValue(docIndex, oldS);
		C_st.decValue(oldS, oldZ);
		C_dst[docIndex].decValue(oldS, oldZ);
		C_stw[oldS].decValue(oldZ, iWordList);
		int senti_topic = 1;
		if (oldS == Constants.NUM_SENTIMENTS) {
			senti_topic = 0;
		}
		N_p[group][senti_topic] -= 1;

		/*
		 * Tạo ma trận xác suất
		 * 
		 */
		double[][] proTable = makeProTable(docIndex, wordIndex, iWordList, postag);
		// chọn S và Z từ ma trận proTable
		int[] index = searchElement(SUM(proTable) * make_uniform(), proTable);
		int SInit = index[0];
		int ZInit = index[1];
		// System.out.println("S : " + SInit);

		S.get(docIndex).set(wordIndex, SInit);
		Z.get(docIndex).set(wordIndex, ZInit);
		C_ds.incValue(docIndex, SInit);
		C_st.incValue(SInit, ZInit);
		C_dst[docIndex].incValue(SInit, ZInit);
		C_stw[SInit].incValue(ZInit, iWordList);
		senti_topic = 1;
		if (SInit == Constants.NUM_SENTIMENTS) {
			senti_topic = 0;
		}
		N_p[group][senti_topic] += 1;

		if (senti_topic == 1) {
			word_has_sentiment += 1;
		} else {
			word_has_not_sentiment += 1;
		}

		if (sim >= Constants.burn_in) {
			C_ds_mean.incValue(docIndex, SInit);
			C_st_mean.incValue(SInit, ZInit);
			C_dst_mean[docIndex].incValue(SInit, ZInit);
			C_stw_mean[SInit].incValue(ZInit, iWordList);

			S_dns_mean[docIndex].incValue(wordIndex, SInit);
			Z_dnt_mean[docIndex].incValue(wordIndex, ZInit);

			// F_n_mean[docIndex].
			if (SInit == Constants.NUM_SENTIMENTS) {
				F_n_mean[docIndex].incValue(wordIndex, 0);
			} else {
				F_n_mean[docIndex].incValue(wordIndex, 1);
			}

		}

	}

	public int[] calcPi(double pt) throws IOException {
		double numSampling = Constants.sampling;
		double[] lambda_;
		double[][][] lambda = new double[Constants.NUM_SENTIMENTS + 1][Constants.NUM_ASPECTS][V];

		for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++) {
			for (int t = 0; t < Constants.NUM_ASPECTS; t++) {
				lambda_ = new double[V];
				double sumLambda = 0;
				for (int v = 0; v < V; v++) {
					lambda_[v] = Constants.k * ReadData.beta[s][v];
				}
				for (int d = 0; d < numDoc; d++) {
					int numSent = ReadData.all_Doc.get(d).size();
					int wordIndex = 0;
					for (int m = 0; m < numSent; m++) {
						int numWord = ReadData.all_Doc.get(d).get(m).size();
						for (int w = 0; w < numWord; w++) {
							int iWordList = ReadData.all_Doc.get(d).get(m).get(w);
							if (s == Constants.NUM_SENTIMENTS) {
								lambda_[iWordList] += F_n_mean[d].getValue(wordIndex, 0)
										* S_dns_mean[d].getValue(wordIndex, s) * Z_dnt_mean[d].getValue(wordIndex, t)
										/ (double) (numSampling * numSampling * numSampling);

							} else {
								lambda_[iWordList] += F_n_mean[d].getValue(wordIndex, 1)
										* S_dns_mean[d].getValue(wordIndex, s) * Z_dnt_mean[d].getValue(wordIndex, t)
										/ (double) (numSampling * numSampling * numSampling);
							}
							wordIndex += 1;
						}
					}
				}

				// Normalization LAMBDA
				for (int v = 0; v < V; v++) {
					sumLambda += lambda_[v];
				}

				for (int v = 0; v < V; v++) {
					lambda_[v] /= sumLambda;
					lambda[s][t][v] = lambda_[v];
				}
			}
		}

		theta = new double[numDoc][Constants.NUM_SENTIMENTS][Constants.NUM_ASPECTS];
		pi = new double[numDoc][Constants.NUM_SENTIMENTS];
		for (int i = 0; i < numDoc; i++) {
			theta[i] = new double[Constants.NUM_SENTIMENTS + 1][Constants.NUM_ASPECTS];
			pi[i] = new double[Constants.NUM_SENTIMENTS];
			for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++) {
				theta[i][s] = new double[Constants.NUM_ASPECTS];
			}
		}
		int[] labels_batch = new int[ReadData.all_Doc.size()];

		// calculator PI and THETA for each document

		for (int i = 0; i < numDoc; i++) {
			// calc PI
			double sumC_d_mean = 0;
			for (int s__ = 0; s__ < Constants.NUM_SENTIMENTS; s__++) {
				sumC_d_mean += C_ds_mean.getValue(i, s__);
			}
			double mauso = Constants.NUM_SENTIMENTS * Constants.gamma + sumC_d_mean / (double) (Constants.sampling);
			for (int s = 0; s < Constants.NUM_SENTIMENTS; s++) {
				double tuso = Constants.gamma + C_ds_mean.getValue(i, s) / (double) (Constants.sampling);
				pi[i][s] = tuso / (double) mauso;
			}
			if (pi[i][0] > pi[i][1]) {
				labels_batch[i] = 1;
			} else {
				labels_batch[i] = -1;
			}
		}
		// calc phi
		for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++) {
			for (int t = 0; t < Constants.NUM_ASPECTS; t++) {
				for (int v = 0; v < V; v++) {
					phi[s][t][v] = (1 - pt) * phi[s][t][v] + pt * lambda[s][t][v];
				}
			}
		}

		for (int docIndex = 0; docIndex < numDoc; docIndex++) {
			for (int t = 0; t < Constants.NUM_ASPECTS; t++) {
				for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++) {
					double tuso = Constants.alpha + C_dst_mean[docIndex].getValue(s, t) / (double) (Constants.sampling);
					double mauso = Constants.alpha * Constants.NUM_ASPECTS
							+ C_ds_mean.getValue(docIndex, s) / (double) (Constants.sampling);
					theta[docIndex][s][t] = tuso / (double) mauso;
				}
			}
		}
		return labels_batch;

	}

	public void initPhi(double[] pos_beta) throws Exception {
		phi = new double[Constants.NUM_SENTIMENTS + 1][Constants.NUM_ASPECTS][V];
		Random r = new Random();
		double same_type_upper = Constants.SAME_TYPE * 1e1;
		double neutral_lower = Constants.NEUTRAL * 1e-2;
		double diff_type_lower = Constants.DIFFERENT_TYPE * 1e-10;
		double[][][] initLambda = new double[Constants.NUM_SENTIMENTS + 1][Constants.NUM_ASPECTS][V];
		for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++)
			for (int t = 0; t < Constants.NUM_ASPECTS; t++)
				for (int j = 0; j < pos_beta.length; j++) {
					initLambda[s][t][j] = neutral_lower + (Constants.NEUTRAL - neutral_lower) * r.nextDouble();
				}
		for (int t = 0; t < Constants.NUM_ASPECTS; t++)
			for (int j = 0; j < pos_beta.length; j++) {
				if (pos_beta[j] == Constants.SAME_TYPE) {
					initLambda[0][t][j] = Constants.SAME_TYPE
							+ (same_type_upper - Constants.SAME_TYPE) * r.nextDouble();
					initLambda[1][t][j] = diff_type_lower
							+ (Constants.DIFFERENT_TYPE - diff_type_lower) * r.nextDouble();
				} else if (pos_beta[j] == Constants.DIFFERENT_TYPE) {
					initLambda[1][t][j] = Constants.SAME_TYPE
							+ (same_type_upper - Constants.SAME_TYPE) * r.nextDouble();
					initLambda[0][t][j] = diff_type_lower
							+ (Constants.DIFFERENT_TYPE - diff_type_lower) * r.nextDouble();
				}
			}
		double[][] sumV = new double[Constants.NUM_SENTIMENTS + 1][Constants.NUM_ASPECTS];
		for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++)
			for (int t = 0; t < Constants.NUM_ASPECTS; t++)
				for (int v = 0; v < this.V; v++) {
					sumV[s][t] += initLambda[s][t][v];
				}
		for (int s = 0; s < Constants.NUM_SENTIMENTS + 1; s++)
			for (int t = 0; t < Constants.NUM_ASPECTS; t++)
				for (int v = 0; v < this.V; v++) {
					this.phi[s][t][v] = initLambda[s][t][v] / sumV[s][t];
				}
	}

	public static void initDelta() {
		delta[1][0] = 0.9;
		delta[1][1] = 0.9;
		delta[1][2] = 0.7;
		delta[1][3] = 0.9;
		delta[1][4] = 0.1;
		for (int i = 0; i < 5; i++) {
			delta[0][i] = 1 - delta[1][i];
		}
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 2; j++) {
				delta[j][i] *= 10;
			}
		}
	}

	public int[] learnMiniBatch(double pt) throws IOException {
		numDoc = ReadData.all_Doc.size();
		initSZ();
		burn_in();
		int[] label = calcPi(pt);
		return label;
	}

	public Learn() throws Exception {
		V = ReadData.all_Word.size();
		initDelta();
	}

	public static void main(String[] argv) throws Exception {
		Learn learn = new Learn();
		System.out.println(delta[0][0]);
	}
}

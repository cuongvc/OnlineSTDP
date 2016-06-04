package STDP;

import java.util.ArrayList;

import readdata.ReadData;

public class Calc_accuracy {
	public static double accuracy(ArrayList<Integer> labels_predict) {
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
			a[0] = a[1] = a[2] = -1;
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
}

package constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

public class Constants {
	public static double NEUTRAL;
	public static double SAME_TYPE;
	public static double DIFFERENT_TYPE;
	public static int NUM_SENTIMENTS;
	public static int NUM_ASPECTS;
	public static int NUM_DOC;
	public static int MINIBATCH_SIZE;
	public static double SPLIT_RATE;
	public static double alpha;
	public static String output_folder;
	public static String path;
	public static String DOC_FILE;
	public static String POS_FILE;
	public static String WORD_LIST_FILE;
	public static String[] SENTIMENT_FILE;
	public static String LABEL_FILE;
	public static String DOC_FILE_TEST;
	public static String LABEL_FILE_TEST;
	public static String PERPLEXITY_FILE;
	public static int k;
	public static double tau0;
	public static double kappa;
	public static double burn_in;
	public static double sampling;
	public static double gamma;

	public static void readSetting(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
		String line = "";
		Hashtable<String, String> para = new Hashtable<String, String>();
		while ((line = reader.readLine()) != null) {
			String[] outersplit = line.split("\\s+");
			para.put(outersplit[0], outersplit[2]);
		}
		NEUTRAL = Double.parseDouble(para.get("NEUTRAL"));
		SAME_TYPE = Double.parseDouble(para.get("SAME_TYPE"));
		SPLIT_RATE = Double.parseDouble(para.get("SPLIT_RATE"));
		DIFFERENT_TYPE = Double.parseDouble(para.get("DIFFERENT_TYPE"));
		NUM_SENTIMENTS = Integer.parseInt(para.get("NUM_SENTIMENTS"));
		NUM_ASPECTS = Integer.parseInt(para.get("NUM_ASPECTS"));
		MINIBATCH_SIZE = Integer.parseInt(para.get("MINIBATCH_SIZE"));
		output_folder = para.get("output_folder");
		path = para.get("path");
		DOC_FILE = path + para.get("DOC_FILE");
		POS_FILE = path + para.get("POS_FILE");
		WORD_LIST_FILE = path + para.get("WORD_LIST_FILE");
		SENTIMENT_FILE = new String[] { path + para.get("POSITIVE_FILE"), path + para.get("NEGATIVE_FILE") };
		LABEL_FILE = path + para.get("LABEL_FILE");
		DOC_FILE_TEST = path + para.get("DOC_FILE_TEST");
		LABEL_FILE_TEST = path + para.get("LABEL_FILE_TEST");
		PERPLEXITY_FILE = path + para.get("PERPLEXITY_FILE");
		k = Integer.parseInt(para.get("k"));
		tau0 = Double.parseDouble(para.get("tau0"));
		kappa = Double.parseDouble(para.get("kappa"));
		burn_in = Double.parseDouble(para.get("burn_in"));
		sampling = Double.parseDouble(para.get("sampling"));
		gamma = Double.parseDouble(para.get("gamma"));
		String[] sAlpha = para.get("alpha").split(",");
		alpha = Double.parseDouble(sAlpha[0]);

		reader.close();
	}
}

package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class DocUtil {
	public static Hashtable<Integer, Integer>[] split_doc(Hashtable<Integer, Integer> bag_of_w, double split_rate) {
		if (split_rate <= 0 || split_rate >=1) {
			System.out.println("Split rate must be between 0 and 1");
			return null;
		}
		Hashtable<Integer, Integer>[] train_and_test = new Hashtable[2];
		for (int i = 0;i < 2; i++) {
			train_and_test[i] = new Hashtable<Integer, Integer>();
		}
		ArrayList<Integer> word_list = new ArrayList<Integer>();
		for (Map.Entry<Integer, Integer> word: bag_of_w.entrySet()) {
			int word_id = word.getKey();
			int word_freq = word.getValue();
			for (int i = 0; i < word_freq; i++) word_list.add(word_id);
		}
		Collections.shuffle(word_list);
		int list_size = word_list.size();
		int split_index = (int)(split_rate * list_size);
		List<Integer> train_list = word_list.subList(0, split_index);
		List<Integer> test_list = word_list.subList(split_index, list_size);
		train_and_test[0] = list_to_map(train_list);
		train_and_test[1] = list_to_map(test_list);
		return train_and_test;
	}
	public static Hashtable<Integer, Integer> list_to_map(List<Integer> word_list){
		Hashtable<Integer, Integer> bag_of_w = new Hashtable<Integer, Integer>();
		for (Integer word: word_list) {
			if (!bag_of_w.containsKey(word)) bag_of_w.put(word, 1);
			else {
				int word_freq = bag_of_w.get(word);
				bag_of_w.remove(word);
				bag_of_w.put(word, word_freq + 1);
			}
		}
		return bag_of_w;
	}
	public static Hashtable<Integer, Integer> bag_of_sent_to_bag_of_word(ArrayList<Hashtable<Integer, Integer>> doc){
		Hashtable<Integer, Integer> bag_of_word = new Hashtable<Integer, Integer>();
		for (Hashtable<Integer, Integer> sent:doc) {
			for (Map.Entry<Integer, Integer> word_entry: sent.entrySet()) {
				int word_id = word_entry.getKey();
				int word_freq = word_entry.getValue();
				if (!bag_of_word.containsKey(word_id)) bag_of_word.put(word_id, word_freq);
				else {
					int freq = bag_of_word.get(word_id);
					freq += word_freq;
					bag_of_word.remove(word_id);
					bag_of_word.put(word_id, freq);
				}
			}
		}
		return bag_of_word;
	}
	public static ArrayList<Hashtable<Integer, Integer>>[] create_perplexity_set(ArrayList<ArrayList<Hashtable<Integer,Integer>>> perplex_docs, double split_rate) {
		int size = perplex_docs.size();
		Collections.shuffle(perplex_docs);
		ArrayList<Hashtable<Integer, Integer>>[] perplexity_set = new ArrayList[2];
		for (int i = 0; i < 2; i++) {
			perplexity_set[i] = new ArrayList<Hashtable<Integer, Integer>>();
		}
		Hashtable<Integer, Integer>[] current_bag_of_w = new Hashtable[2];
		for (int i = 0;i < 2; i++) {
			current_bag_of_w[i] = new Hashtable<Integer, Integer>();
		}
		for (int i = 0; i < size; i++) {
			ArrayList<Hashtable<Integer, Integer>> current_doc = perplex_docs.get(i);
			Hashtable<Integer, Integer> bag_of_w = bag_of_sent_to_bag_of_word(current_doc);
			current_bag_of_w = split_doc(bag_of_w, split_rate);
			perplexity_set[0].add(current_bag_of_w[0]);
			perplexity_set[1].add(current_bag_of_w[1]);
		}
		return perplexity_set;
	}
}

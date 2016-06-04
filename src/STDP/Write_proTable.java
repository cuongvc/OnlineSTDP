package STDP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import constants.Constants;

public class Write_proTable {
	public static void writeFile(String filename, double[][] matrix_2D) throws IOException {
		// System.out.println("Writing Pi of learner" + this.indexLeaner);
		File folder = new File(Constants.output_folder + "/protable/");
		if (!folder.exists())
			folder.mkdir();

		File f = new File(Constants.output_folder + "/protable/" + filename + ".csv");

		boolean check = false;
		if (!f.exists()) {
			f.createNewFile();
			check = true;
		}
		PrintWriter out = new PrintWriter(new FileWriter(f));
		for (int i = 0; i < matrix_2D.length; i++) {
			for (int s = 0; s < matrix_2D[i].length; s++) {
				out.print("," + matrix_2D[i][s]);
			}
			out.println();
		}

		out.println();
		out.close();

	}

}

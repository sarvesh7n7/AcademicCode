package ml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class Main extends DataSetReader {

	void runID3(String dataset, String inputPartition, String outputPartition) {
		readDataSet(dataset);
		readParition(inputPartition);
		double maxFvalue = 0;
		String maxFvalueKey = null;
		int maxFValueAttrib = -1;
		for (String key : partitions.keySet()) {
			int totalElements = partitions.get(key).size();
			double targetEntropy = calculateTargetAttribEntropy(key);
			double maxGain = 0;
			int maxGainColumn = -1;
			// Calculating the entropy for the attributes other than target
			for (int i = 0; i < column - 1; i++) {
				double entropy = 0;
				// looping for the possible values between 0-2
				for (int j = 0; j < 3; j++) {
					double probability = (double) getCount(key, i, j)
							/ totalElements;
					int zerosInTarget = getCount(key, 0, i, j);
					int onesInTarget = getCount(key, 1, i, j);
					entropy += probability
							* calculateEntropy(zerosInTarget, onesInTarget);
				}
				double gain = targetEntropy - entropy;
				if (gain >= maxGain) {
					maxGain = gain;
					maxGainColumn = i;
				}
				System.out.println("Key=" + key + ", col=" + i + ", Entropy="
						+ entropy);
			}
			double fvalue = maxGain * ((double) totalElements / rows);
			System.out.println("F=" + fvalue);

			if (fvalue >= maxFvalue) {
				maxFvalue = fvalue;
				maxFvalueKey = key;
				maxFValueAttrib = maxGainColumn;
			}
		}
		System.out.println("Max F value=" + maxFvalue + " from " + maxFvalueKey
				+ ", Attrib:" + maxFValueAttrib);
		dumpOutputToFile(maxFvalueKey, maxFValueAttrib, outputPartition);
	}

	void dumpOutputToFile(String maxFvalueKey, int maxFValueAttrib,
			String outputPartition) {
		try {
			File file = new File(outputPartition);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (String key : partitions.keySet()) {
				if (key.equals(maxFvalueKey)) {
					Integer partitionValues[] = ((ArrayList<Integer>) partitions
							.get(maxFvalueKey)).toArray(new Integer[0]);
					for (int i = 0; i < 3; i++) {
						String temp = "";
						temp = maxFvalueKey + i + " ";
						boolean flag = false;
						for (int j = 0; j < partitionValues.length; j++) {
							if (dataset[partitionValues[j] - 1][maxFValueAttrib] == i) {
								temp = temp + partitionValues[j] + " ";
								flag = true;
							}
						}
						if (flag) {
							System.out.println(temp);
							bw.write(temp + "\n");
						}
					}

				} else {
					String temp = "";
					temp = key + " ";
					ArrayList<Integer> partition = partitions.get(key);
					for (int i = 0; i < partition.size(); i++)
						temp = temp + partition.get(i) + " ";
					System.out.println(temp);
					bw.write(temp + "\n");
				}

			}
			bw.close();
			System.out
					.println("Dumped the output into file " + outputPartition);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out
					.println("Options <datasetFile> <partitions> <outputPartitions>");
			System.exit(0);
		}
		new Main().runID3(args[0], args[1], args[2]);
	}

}

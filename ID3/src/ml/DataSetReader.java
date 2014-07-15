package ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataSetReader {

	protected String dataSetFile = null;
	protected String partitionFile = null;
	private BufferedReader br = null;
	protected int rows = 0;
	protected int column = 0;
	protected int dataset[][];
	protected Map<String, ArrayList<Integer>> partitions;

	public DataSetReader() {
		partitions = new LinkedHashMap<String, ArrayList<Integer>>();
	}

	void readDataSet(String datasetFile) {
		if (datasetFile == null) {
			System.out.println("Input file is null! ");
			return;
		}
		String lineValues[] = null;
		try {
			File inputFile = new File(datasetFile);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					inputFile)));

			String firstLine = null;

			firstLine = br.readLine();
			if (firstLine == null) {
				System.out.println("Invalid Input File Format! ");
			}
			lineValues = firstLine.trim().split(" ");
			// Reading the number of rows and columns
			if (lineValues[0] != null)
				rows = Integer.parseInt(lineValues[0]);
			if (lineValues[1] != null)
				column = Integer.parseInt(lineValues[1]);
			System.out.println("Rows= " + rows + "Column=" + column);

			if (rows <= 0 || column <= 0) {
				System.out.println("Invalid input of rows or column!");
				return;
			}
			// Allocating memory to dataset
			dataset = new int[rows + 1][column + 1];
			String dataLine = null;
			int entryCount = 0;
			while ((dataLine = br.readLine()) != null) {
				lineValues = dataLine.trim().split(" ");
				for (int i = 0; i < lineValues.length; i++) {
					if (lineValues[i] != null)
						dataset[entryCount][i] = Integer
								.parseInt(lineValues[i]);
					else
						System.out
								.println("Found the invalid/null value on line number="
										+ entryCount);
				}
				entryCount++;
			}

			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < column; j++)
					System.out.print(dataset[i][j] + " ");
				System.out.println();
			}
			br.close();
		} catch (Exception E) {
			E.printStackTrace();
		}

	}

	void readParition(String partitionFile) {
		if (partitionFile == null) {
			System.out.println("Partition file is null!");
			return;
		}

		String lineValues[] = null;

		try {
			File inputFile = new File(partitionFile);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					inputFile)));
			int entryCount = 0;
			String dataLine = null;
			while ((dataLine = br.readLine()) != null) {
				lineValues = dataLine.trim().split(" ");
				ArrayList<Integer> array = new ArrayList<Integer>();
				for (int i = 1; i < lineValues.length; i++)
					if (lineValues[i] != null) {
						Integer temp = Integer.parseInt(lineValues[i]);
						if (temp != null)
							array.add(temp);
						partitions.put(lineValues[0], array);
					} else {
						System.out
								.println("Found the invalid/null value on line number="
										+ entryCount);
					}
				entryCount++;
			}

			System.out.println("\nPrinting Partitions: ");
			for (String key : partitions.keySet()) {
				System.out.println(key + " : " + partitions.get(key));
			}

			br.close();
		} catch (Exception E) {
			E.printStackTrace();
		}

	}

	double calculateEntropy(int a, int b) {
		if (a == 0 || b == 0)
			return 0;
		double entropy = 0;
		double sum = a + b;
		entropy = (a / sum) * Math.log(sum / a) / Math.log(2) + (b / sum)
				* Math.log(sum / b) / Math.log(2);
		return entropy;
	}

	double calculateTargetAttribEntropy(String key) {
		if (key == null)
			return 0;
		Integer partitionValues[] = ((ArrayList<Integer>) partitions.get(key))
				.toArray(new Integer[0]);
		int zeros = 0, ones = 0;

		for (Integer val : partitionValues) {
			if (dataset[val - 1][column - 1] == 0)
				zeros++;
			else if (dataset[val - 1][column - 1] == 1)
				ones++;
			else
				System.out
						.println("Seeing the values in the target attribute are not binary! ");
		}
		double entropy = calculateEntropy(zeros, ones);
		return entropy;
	}

	int getCount(String key, int zeroOrOne, int col, int attributeValue) {
		Integer partitionValues[] = ((ArrayList<Integer>) partitions.get(key))
				.toArray(new Integer[0]);
		int count = 0;
		for (Integer val : partitionValues) {
			if (dataset[val - 1][col] == attributeValue
					&& dataset[val - 1][column - 1] == zeroOrOne) {
				count++;
			}
		}
		return count;
	}

	int getCount(String key, int col, int attributeValue) {
		Integer partitionValues[] = ((ArrayList<Integer>) partitions.get(key))
				.toArray(new Integer[0]);
		int count = 0;
		for (Integer val : partitionValues) {
			if (dataset[val - 1][col] == attributeValue) {
				count++;
			}
		}
		return count;
	}

	public static void main(String[] args) {
		// Test Run.
		DataSetReader reader = new DataSetReader();
		reader.readDataSet("dataset.txt");
		reader.readParition("outputpartition.txt");
		System.out.println("entropy="
				+ reader.calculateTargetAttribEntropy("2"));
	}

}

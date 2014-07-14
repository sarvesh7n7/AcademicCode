package com.aos.two;

import java.io.*;
import java.util.HashMap;
import java.io.IOException; 

public class TestClass 
{
	public static int initialValue = 1000;
	public static int totalNode;
	
	static String Inputdetails;
	static String[] NodeVectors;
	static String[] LLS;
	static String[] LLR;
	static String[] NodeLLS;
	static String[] NodeLLR;
	static int numberOfNodes;
	static HashMap<Integer, Integer> RecoveryLLRMap;
	static HashMap<Integer, Integer> RecoveryLLSMap;
	
	static HashMap<Integer, HashMap<Integer, Integer>> VerifyLLRMap = new HashMap<Integer, HashMap<Integer, Integer>>();
	static HashMap<Integer, HashMap<Integer, Integer>> VerifyLLSMap = new HashMap<Integer, HashMap<Integer, Integer>>();
	static HashMap<Integer,Integer> VerifyiValue = new HashMap<Integer,Integer>();
	static HashMap<Integer, String[]> neighborNodeIDs;
	
	static int iValue = 0;
	public static HashMap<Integer, Integer> getLLS(int ThisNodeId)
	{
		RecoveryLLSMap = new HashMap<Integer, Integer>();
		File currentDirectory = new File(new File(".").getAbsolutePath());
		File file = new File(currentDirectory + File.separator + ThisNodeId + ".txt");
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
		{
			
			while((Inputdetails = br.readLine())!=null)
			{
				NodeVectors = Inputdetails.split("::");
				
				
				String LLSValue = NodeVectors[3].trim();
				NodeLLS = LLSValue.substring(1,NodeVectors[3].trim().length()-1).split(",");
				
				for(int index = 0; index<NodeLLS.length;index++)
				{
					LLS = NodeLLS[index].split("=");
					RecoveryLLSMap.put(Integer.parseInt(LLS[0].trim()),Integer.parseInt(LLS[1].trim()));
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//System.out.println(RecoveryLLSMap);
		return RecoveryLLSMap;
	}
	
	public static int validateiValue(int totalNode)
	{
		iValue = 0;
		for(int i=0; i <=totalNode-1; i++)
		{
			iValue = iValue + VerifyiValue.get(i);
		} 
		System.out.println("IVALUE: "+ iValue);
		return iValue;
	}
	
	public static HashMap<Integer, Integer> getLLR(int ThisNodeId)
	{
		
		RecoveryLLRMap = new HashMap<Integer, Integer>();
		File currentDirectory = new File(new File(".").getAbsolutePath());
		File file = new File(currentDirectory + File.separator + ThisNodeId + ".txt");
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
		{
			
			while((Inputdetails = br.readLine())!=null)
			{
				NodeVectors = Inputdetails.split("::");	
				
				String LLRValue = NodeVectors[1].trim();
				NodeLLR = LLRValue.substring(1,NodeVectors[1].trim().length()-1).split(",");
				
				for(int index = 0; index<NodeLLR.length;index++)
				{
					LLR = NodeLLR[index].split("=");
					RecoveryLLRMap.put(Integer.parseInt(LLR[0].trim()),Integer.parseInt(LLR[1].trim()));
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//System.out.println(RecoveryLLRMap);
		return RecoveryLLRMap;
	}
	
	public static int getiValue(int ThisNodeId)
	{
		
		File currentDirectory = new File(new File(".").getAbsolutePath());
		File file = new File(currentDirectory + File.separator + ThisNodeId + ".txt");
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
		{
			
			while((Inputdetails = br.readLine())!=null)
			{
				NodeVectors = Inputdetails.split("::");
				
				iValue = Integer.parseInt(NodeVectors[0]);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return iValue;
	}
	
	public static void main(String[] args) throws IOException
	{
	
		System.out.println("Enter total number of nodes: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		totalNode = Integer.parseInt(br.readLine());
		
		System.out.println("*** Reading data of " + totalNode + " log files ***");
		
		for(int i=0; i <=totalNode-1; i++)
		{
			VerifyLLSMap.put(i, getLLS(i)); 
			VerifyLLRMap.put(i, getLLR(i));
			VerifyiValue.put(i,getiValue(i));
		}
		
		//Total value for all Processes	
		int totalValue = totalNode * initialValue;
		
		if(totalValue >= validateiValue(totalNode))
			System.out.println("Value is consistent after the checkpoint algorthim");
		else
			System.out.println("Value is not consistent for the checkpoints");
		
		// Reading configuration file for testing
		ReadConfig();
		
		// validating consistency by reading final permanent checkpoints
		if(validateConsistency() == true)
			System.out.println("All checkpoints are consistent!");
		else
			System.out.println("All checkpoints are not consistent!");
				
	} 
	
	public static boolean validateConsistency()
	{
		HashMap<Integer, Integer> tempLLS = new HashMap<Integer,Integer>();
		
		String tempNebors[] = null;
		for(int i= 0; i< VerifyLLRMap.size(); i++)
		{
			tempLLS = VerifyLLSMap.get(i);
			//System.out.println("TempLLS ===> "+tempLLS);
			tempNebors = neighborNodeIDs.get(i);
			for(int j =0; j < tempNebors.length; j++)
			{
				System.out.println("tempLLS.get(neighborNodeIDs.get(j)) = "+tempLLS.get(Integer.parseInt(tempNebors[j]))+"   VerifyLLRMap.get(tempNebors[j]).get(i) = "+VerifyLLRMap.get(Integer.parseInt(tempNebors[j])).get(i));
				if(tempLLS.get(Integer.parseInt(tempNebors[j])) != VerifyLLRMap.get(Integer.parseInt(tempNebors[j])).get(i))
				{
					return false;
				}
				//return 1;
			}
		}
		
		return true;
		
	}

	
	public static void ReadConfig() throws IOException
	{
		neighborNodeIDs = new HashMap<Integer, String[]>();
		BufferedReader configReader = new BufferedReader(new FileReader("Configuration_L.txt"));
		String readingLine = null;
		int[] nodeID = null;


		int node_count = 0;
		while((readingLine = configReader.readLine()) != null)
		{
			if(!readingLine.startsWith("#"))
			{
				if(!readingLine.isEmpty())
				{
					String subLine = readingLine.substring(0, readingLine.indexOf("#"));

					if(subLine.trim().length()==1)
					{
						numberOfNodes = Integer.parseInt(subLine.trim());
						nodeID = new int[numberOfNodes];
					}
					else
					{
						String[] lineElements = subLine.split(" ");
						nodeID[node_count] = Integer.parseInt(lineElements[0]);
						neighborNodeIDs.put(node_count, lineElements[3].split("\\|"));
						node_count++;
					}

				}
			}
		}
		configReader.close();
		
		/* Printing of Node ID and it's neighbors
		for(int i=0; i < numberOfNodes-1; i++)
		{
			System.out.println(nodeID[i]);
			String temp[] = neighborNodeIDs.get(i);
			for(int j=0; j<temp.length; j++)
				System.out.print("\t"+temp[j]);

			System.out.println();
		}*/
	}

}
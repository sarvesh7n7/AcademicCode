package com.aos.two;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;


public class ReadCheckpointFile {
	
	static HashMap<Integer, Integer> RecoveryLLRMap = new HashMap<Integer, Integer>();
	static HashMap<Integer, Integer> RecoveryLLSMap = new HashMap<Integer, Integer>();
	
	static int iValue = 0;
	static String Inputdetails;
	static String[] NodeVectors;
	static String[] LLS;
	static String[] LLR;
	static String[] NodeLLS;
	static String[] NodeLLR;
	
	
	/*public static HashMap<Integer, Integer> getLLR(int ThisNodeId)
	{
		
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
		return RecoveryLLRMap;
		
	}*/
	
	public static HashMap<Integer, Integer> getLLS(int ThisNodeId)
	{
		
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
		
		return RecoveryLLSMap;
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
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return iValue;
	}
}

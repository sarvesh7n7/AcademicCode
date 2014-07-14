package com.aos.two;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class TentativeChkpt {
	
	public static int iValue = -1;
	public static HashMap<Integer, Integer> tLLR = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> tFLS = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> tLLS = new HashMap<Integer, Integer>();

	
	public static void storeTentativeData(int initialValue, HashMap<Integer, Integer> LLR, HashMap<Integer, Integer> FLS, HashMap<Integer, Integer> LLS)
	{
		iValue = initialValue;
		tLLR = LLR;
		tFLS = FLS;
		tLLS = LLS;
		
		File currentDirectory = new File(new File(".").getAbsolutePath());
		//System.out.println("*** Current Dir ****"+currentDirectory);
    	File file = new File(currentDirectory + File.separator + MainNode.THIS_nodeID + "tc.txt");
    	
    	try 
		{
			if (!file.exists()) 
				file.createNewFile();
		
			PrintWriter printWriter = new PrintWriter(new FileWriter(file,true));
			
			printWriter.println(TentativeChkpt.iValue+"::"+TentativeChkpt.tLLR+"::"+TentativeChkpt.tFLS+"::"+TentativeChkpt.tLLS);
			
			printWriter.close();
			
			//System.out.println("Commit successful !!!!!!!!!!!!!!!!!!!");
			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void resetTentativeHmaps()
	{
		int zero = 0;
		String temp[] = NodeData.neighborNodeIDs.get(MainNode.THIS_nodeID);
		for(int i = 0; i < temp.length; i++)
		{
			TentativeChkpt.tFLS.put(Integer.parseInt(temp[i]), zero);
			TentativeChkpt.tLLS.put(Integer.parseInt(temp[i]), zero);
			TentativeChkpt.tLLR.put(Integer.parseInt(temp[i]), zero);
		}
	}
}

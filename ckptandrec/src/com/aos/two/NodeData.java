package com.aos.two;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

public class NodeData 
{
	public static Object lock = new Object();
	
	public static HashMap<Integer, String[]> neighborNodeIDs = new HashMap<Integer, String[]>();
	
	public static HashMap<Integer, Integer> LLR = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> FLS = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> LLS = new HashMap<Integer, Integer>();	
	
	public static int numberOfNodes;
	public static int[] nodeID;
	public static String[] hostIP;
	public static int[] portNO;
	public static int[] chkptInterval;
	public static int[] recovInterval;
	
	public static volatile int inTheChkpt = -1;
	public static boolean willingToChkpt = true;
	
	public static boolean willingToRec = true;
	
	public static volatile int inTheRecov = -1;
	
	public static volatile int cohortSize = -1;
	public static boolean permanentCkptTaken = false;
	public static boolean permanentRecTaken = false;
	public static boolean needofckpt = false;
	public static boolean makeCkptPermanent = true;
	public static boolean makeRecPermanent = true;
	protected static int i=0;
	public static ArrayList<String> ckTemp = new ArrayList<String>();
	
	public static Object cohortsync = new Object();
	public static Object recNeborsync = new Object();
	public static volatile int recNeborSize = -1;
	public static int permChkptCounter = 0;
	private NodeData()
	{}
	
	public static void storeNoOfNodes(int numberOfNodes)
	{
		NodeData.numberOfNodes = numberOfNodes;
	}
	
	public static void storeData(int[] nodeID, String[] hostIP, int[] portNO, HashMap<Integer, String[]> neighborNodeIDs,  int[] chkptInterval,  int[] recovInterval) 
	{
		NodeData.nodeID = nodeID;
		NodeData.hostIP = hostIP;
		NodeData.portNO = portNO;
		NodeData.neighborNodeIDs = neighborNodeIDs;
		NodeData.chkptInterval = chkptInterval;
		NodeData.recovInterval = recovInterval;
		i++;
	}
	
	public static ArrayList<Integer> getCohorts(int nodeID, int initiator, int receivedFrom)
	{
		String temp[] = NodeData.neighborNodeIDs.get(nodeID);
		
		ArrayList<Integer> cohorts = new ArrayList<Integer>();
				
		int neborID = 0;
		for(int i=0;i < temp.length;i++)
		{
			neborID = Integer.parseInt(temp[i].trim());

			if((neborID != initiator) && (neborID != receivedFrom))
			{
				if(NodeData.LLR.get(neborID) != 0)
				{
					cohorts.add(neborID);
				}
			}			
		}
		System.out.println("cohorts size in function:: "+cohorts.size());
		System.out.println("cohorts values:"+cohorts);
		return cohorts;
	}
	
	
	public static ArrayList<Integer> getRecNeighbors(int nodeID, int initiator, int receivedFrom)
	{
		String temp[] = NodeData.neighborNodeIDs.get(nodeID);
		for(int i=0;i<temp.length;i++)
		{
			System.out.println("----------"+temp[i]);
		}
		
		ArrayList<Integer> recNebors = new ArrayList<Integer>();		
		
		int neborID = 0;
		for(int i=0; i<temp.length; i++)
		{
			neborID = Integer.parseInt(temp[i].trim());

			if((neborID != initiator) && (neborID != receivedFrom))
			{
				recNebors.add(neborID);
			}			
		}
		
		System.out.println("recover neighbour size in function:: "+recNebors.size());
		System.out.println("recover neighbour values:"+recNebors);
		return recNebors;
	}
	
	
	
	public static void sendMessage(SctpChannel sctpChannel, String msg, int streamNum)
	{
		 try 
		 {
			 System.out.println("in SendMsg{} "+msg );
			ByteBuffer byteBuf = ByteBuffer.allocate(1024); 
			byteBuf.clear();
			byteBuf.put(new byte[1024]);
	        byteBuf.clear();
	        		          
			//int x = rand.nextInt(3);
	        		            
	        byte [] message = msg.getBytes();
	        MessageInfo messageInfo = MessageInfo.createOutgoing(null, streamNum); 
	       
	        byteBuf.put(message);
	        byteBuf.flip();        
	        
			sctpChannel.send(byteBuf, messageInfo);
			
			byteBuf.clear();
	        byteBuf.put(new byte[1024]);
	        byteBuf.clear();
		 } 
		 catch (Exception e) 
		 {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
       
            
	}
	
	public static void resetCurrentHmaps()
	{
		int zero = 0;
		String temp[] = NodeData.neighborNodeIDs.get(MainNode.THIS_nodeID);
		for(int i = 0; i < temp.length; i++)
		{
			NodeData.FLS.put(Integer.parseInt(temp[i]), zero);
			NodeData.LLS.put(Integer.parseInt(temp[i]), zero);
			NodeData.LLR.put(Integer.parseInt(temp[i]), zero);
		}
	}
	
	public static void makePermanent()
	{
		File currentDirectory = new File(new File(".").getAbsolutePath());
		//System.out.println("*** Current Dir ****"+currentDirectory);
    	File file = new File(currentDirectory + File.separator + MainNode.THIS_nodeID + ".txt");
    	File file1 = new File(currentDirectory + File.separator + MainNode.THIS_nodeID + "pa.txt");
    	
    	try 
		{
			if (!file.exists()) 
				file.createNewFile();
		
			PrintWriter printWriter = new PrintWriter(new FileWriter(file));
			
			printWriter.println(TentativeChkpt.iValue+"::"+TentativeChkpt.tLLR+"::"+TentativeChkpt.tFLS+"::"+TentativeChkpt.tLLS);
			
			printWriter.close();
			
			//doing this shit for making permanent checkpoint rocords!
			
			if (!file1.exists()) 
				file1.createNewFile();
		
			permChkptCounter++;
			PrintWriter printWriter1 = new PrintWriter(new FileWriter(file1,true));
			
			printWriter1.println("CHKPTID"+permChkptCounter+"::"+TentativeChkpt.iValue+"::"+TentativeChkpt.tLLR+"::"+TentativeChkpt.tFLS+"::"+TentativeChkpt.tLLS);
			
			printWriter1.close();
			
			//System.out.println("Commit successful !!!!!!!!!!!!!!!!!!!");
			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void makePermanentRec(int iValue, HashMap<Integer, Integer> LLS)
	{
		MainNode.initialValue = iValue;
		MainNode.uniLLS = LLS;
		
		File currentDirectory = new File(new File(".").getAbsolutePath());
		//System.out.println("*** Current Dir ****"+currentDirectory);

    	File file1 = new File(currentDirectory + File.separator + MainNode.THIS_nodeID + "tr.txt");
    	
    	try 
		{
		
			if (!file1.exists()) 
				file1.createNewFile();
		
			permChkptCounter++;
			PrintWriter printWriter1 = new PrintWriter(new FileWriter(file1,true));
			
			printWriter1.println("RECID"+permChkptCounter+"::"+TentativeChkpt.iValue+"::"+TentativeChkpt.tLLR+"::"+TentativeChkpt.tFLS+"::"+TentativeChkpt.tLLS);
			
			printWriter1.close();
			
			//System.out.println("Commit successful !!!!!!!!!!!!!!!!!!!");
			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

}

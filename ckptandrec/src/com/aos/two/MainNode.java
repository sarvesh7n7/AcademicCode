package com.aos.two;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.io.IOException; 

import com.sun.nio.sctp.SctpChannel; 
import com.sun.nio.sctp.SctpServerChannel;

public class MainNode 
{
	//public static int THIS_nodeID;
	public static int THIS_nodeID = 0;
	public static int initialValue = 1000;
	public static Object obj = new Object();
	
	public static Object sendInterrupt = new Object();
	
	public static Object chkptInterrupt = new Object();
	public static Object nextinstnotify = new Object();
	public static Object chkptPermanentInterrupt = new Object();
	
	public static Object recInterrupt = new Object();
	public static Object nextinstnotify2 = new Object();
	public static Object recPermanentInterrupt = new Object();
	
	public static int countOfStoredChannels = 0;
	
	public static HashMap<Integer, Integer>  uniFLS = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> uniLLS = new HashMap<Integer, Integer>();

	public static void main(String[] args) throws IOException
	{
		// Reading Configuration File and Storing The Data Globally into NodeData.java Class
		BufferedReader configReader = new BufferedReader(new FileReader("Configuration_L.txt"));
		String readingLine = null;
		String[] hostIP = null;
		int[] nodeID = null; 
		int[] portNO = null;
		int[] chkptInterval = null;
		int[] recovInterval = null;
		HashMap<Integer, String[]> neighborNodeIDs = new HashMap<Integer, String[]>();


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
						NodeData.numberOfNodes = Integer.parseInt(subLine.trim());

						hostIP = new String[NodeData.numberOfNodes];
						nodeID = new int[NodeData.numberOfNodes];
						portNO = new int[NodeData.numberOfNodes];
						chkptInterval = new int[NodeData.numberOfNodes];
						recovInterval = new int[NodeData.numberOfNodes];
					}
					else
					{
						String[] lineElements = subLine.split(" ");
						nodeID[node_count] = Integer.parseInt(lineElements[0]);
						hostIP[node_count] = lineElements[1];
						portNO[node_count] = Integer.parseInt(lineElements[2]);
						chkptInterval[node_count] = Integer.parseInt(lineElements[4]);
						recovInterval[node_count] = Integer.parseInt(lineElements[5]);
						neighborNodeIDs.put(node_count, lineElements[3].split("\\|"));

						node_count++;
					}

				}
			}
		}
		configReader.close();
		NodeData.storeData(nodeID, hostIP, portNO, neighborNodeIDs, chkptInterval, recovInterval);
		//System.out.println("Enter your node id: ");
		//ufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		THIS_nodeID =  Integer.parseInt(args[0]);
				//Integer.parseInt(br.readLine());
				//Integer.parseInt(args[0]);
				//
		System.out.println("*** Data at Node "+THIS_nodeID+" ***");
		for(int i=0; i < NodeData.numberOfNodes; i++)
		{
			System.out.println(nodeID[i]+"\t"+hostIP[i]+"\t"+portNO[i]);
			String temp[] = NodeData.neighborNodeIDs.get(i);
			for(int j=0; j<temp.length; j++)
				System.out.print("\t"+temp[j]);

			System.out.println();
		}


		String THIS_hostIP = NodeData.hostIP[THIS_nodeID];
		int THIS_portNo = NodeData.portNO[THIS_nodeID];

		// Creating socket address for this node
		SocketAddress serverSocketAddress = new InetSocketAddress(THIS_hostIP,THIS_portNo); 
		// Binding with the created socket address
		SctpServerChannel sctpServerChannel =  SctpServerChannel.open().bind(serverSocketAddress);

		// Starting the node for receiving connection
		AcceptClass aClass = new AcceptClass(sctpServerChannel);
		Thread acceptThread = new Thread(aClass);
		acceptThread.start();
		System.out.println("\n*** Server having Node ID:" +THIS_nodeID+" Started Successfully at Host: "+THIS_hostIP+" and Port: "+THIS_portNo+" ***\n");

		//System.out.println("Randomly Generated Initial Timestamp: "+timestamp);

		// Connecting with another nodes having lower ID than THIS node's ID
		String temp[] = NodeData.neighborNodeIDs.get(THIS_nodeID);
		for(int i=0; i<temp.length; i++)
		{
			int index = Integer.parseInt(temp[i]);
			if(!(NodeData.hostIP[index].equals(THIS_hostIP)) && THIS_portNo != NodeData.portNO[index] && index < THIS_nodeID)
			{
				SocketAddress socketAddress = new InetSocketAddress(NodeData.hostIP[index], NodeData.portNO[index]);
				SctpChannel sctpChannel = SctpChannel.open(); //(socketAddress, 1 ,1 ); 
				sctpChannel.connect(socketAddress, 3, 3);

				// Storing channel for further communication
				SctpChannels.storeChannel(index, sctpChannel);
				countOfStoredChannels++;
			}
		}

		NodeData.resetCurrentHmaps();
		
		int zero = 0;
		for(int i = 0; i < temp.length; i++)
		{
			MainNode.uniFLS.put(Integer.parseInt(temp[i]), zero);
			MainNode.uniLLS.put(Integer.parseInt(temp[i]), zero);
			//MainNode.uniLLR.put(Integer.parseInt(temp[i]), zero);
		}


		// A sleep time of 200 milli-seconds so that number of stored channels will get updated
		//br.readLine();
		try 
		{
			Thread.sleep(8000);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}

		// Starting receiving threads on all the stored sctpChannels
		String temp2[] = NodeData.neighborNodeIDs.get(THIS_nodeID);

		SendThread sThread = new SendThread();
		Thread st = new Thread(sThread);

		for(int i=0; i<temp2.length; i++)
		{
			SctpChannel tempSctpCh = SctpChannels.getSctpChannel(Integer.parseInt(temp2[i]));
			ReceiveThread rThread = new ReceiveThread(tempSctpCh, st);
			new Thread(rThread).start();
		}

		st.start();
		
		/*ChkptDaemon cDaemon = new ChkptDaemon(NodeData.chkptInterval[THIS_nodeID], st);
		Thread cDThread = new Thread(cDaemon);
		cDThread.start();
		RecovDaemon rDaemon = new RecovDaemon(NodeData.recovInterval[THIS_nodeID], st);
		Thread rDThread = new Thread(rDaemon);
		
		if(THIS_nodeID==5)
		{		
			rDThread.start();
		}*/
		
		
		if(THIS_nodeID==1)
		{
			for(int i=0;i<1;i++)
			{				
				try {
					Thread.sleep(NodeData.chkptInterval[THIS_nodeID]);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				
							
				if(NodeData.inTheChkpt == -1 && NodeData.inTheRecov == -1)
				{
					CheckpointThread chkpt = new CheckpointThread(st);
					Thread chkptThread = new Thread(chkpt);
					chkptThread.start();
				}
			}
		}
		
		/*if(THIS_nodeID==1)
		{
			for(int i=0;i<1;i++)
			{				
				try {
					Thread.sleep(NodeData.recovInterval[THIS_nodeID]);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				
				if(NodeData.inTheChkpt == -1 && NodeData.inTheRecov == -1)
				{
					RecoveryThread recth = new RecoveryThread(st);
					Thread recThread = new Thread(recth);
					recThread.start();
				}
			}
		}
		
		if(THIS_nodeID==1)
				{
					for(int i=0;i<1;i++)
					{				
						try {
						//	Thread.sleep(NodeData.chkptInterval[THIS_nodeID]);
							Thread.sleep(4000);
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						}
						
									
						if(NodeData.inTheChkpt == -1 && NodeData.inTheRecov == -1)
						{
							CheckpointThread chkpt = new CheckpointThread(st);
							Thread chkptThread = new Thread(chkpt);
							chkptThread.start();
						}
					}
				}
	*/
	/*	try 
		{
			st.join();
			cDThread.join();
			rDThread.join();
		} 
		catch (InterruptedException e) 
		{
			
			e.printStackTrace();
		}
		*/
	
		
	} 
}
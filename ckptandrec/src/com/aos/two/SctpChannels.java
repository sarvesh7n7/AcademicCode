package com.aos.two;

import java.util.HashMap;

import com.sun.nio.sctp.SctpChannel;

public class SctpChannels 
{
	public SctpChannels() 
	{} 
	
	public static SctpChannels ssc = null;
	//public static SctpChannel[] sctpChannels = new SctpChannel[NodeData.numberOfNodes-1];
	
	public static HashMap<SctpChannel, Integer> sctpChannelsChKey = new HashMap<SctpChannel, Integer>();
	public static HashMap<Integer, SctpChannel> sctpChannelsIdKey = new HashMap<Integer, SctpChannel>();
	
	//public static int i = 0;
	
	public static SctpChannels getInstance()
	{
		if(ssc==null)
		{
			ssc = new SctpChannels();
		}
		return ssc;
	}
	
	public static void storeChannel(int nodeID, SctpChannel sctpCh)
	{
		sctpChannelsChKey.put(sctpCh, nodeID);
		sctpChannelsIdKey.put(nodeID, sctpCh);
		//sctpChannels[i] = sctpCh;
		//i++;
		System.out.println("\nChannel Stored from Node "+nodeID);
		
	}
	
	public static int getNumberOfChannelsStored()
	{
		return sctpChannelsChKey.size();
	}
	
	public static SctpChannel getSctpChannel(int index)
	{
		return sctpChannelsIdKey.get(index);
	}
	
	public static int getNodeId(SctpChannel sctpCh)
	{
		return sctpChannelsChKey.get(sctpCh);
	}
}
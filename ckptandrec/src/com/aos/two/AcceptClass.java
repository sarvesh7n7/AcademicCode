/* AcceptClass Thread: A listening thread of the node
*/

package com.aos.two;

import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

public class AcceptClass implements Runnable
{
	SctpServerChannel sctpServerChannel;
	SctpChannel sctpChannel;
	String temp[] = NodeData.neighborNodeIDs.get(MainNode.THIS_nodeID);
	
	//private static int i = 0;
	
	public AcceptClass(SctpServerChannel sctpServerChannel)
	{
		this.sctpServerChannel = sctpServerChannel;
	}
	
	public void run()
	{
		try
		{
			
    		while((sctpChannel = sctpServerChannel.accept()) != null)
            {
            	 //Storing a sctpChannel
    			 
    			 int index = Integer.parseInt(temp[MainNode.countOfStoredChannels]);
            	 SctpChannels.storeChannel(index, sctpChannel);
            	 MainNode.countOfStoredChannels++;
            	 Thread.sleep(20);
            }
    		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

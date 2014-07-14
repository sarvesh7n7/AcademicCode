package com.aos.two;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

public class SendThread implements Runnable
{
	SctpChannel sctpChannel; 
	ByteBuffer byteBuf = ByteBuffer.allocate(1024); 
	String msg = null;
	String temp[] = null;

	public void run() 
	{
		
		while(true)
		{

			temp = NodeData.neighborNodeIDs.get(MainNode.THIS_nodeID);
			Random rand = new Random();
			int index = rand.nextInt(temp.length);

			int neighborNodeId = Integer.parseInt(temp[index]);

			int value = rand.nextInt(20);

			int checkFLS = NodeData.FLS.get(neighborNodeId);
			int checkUniLLS = MainNode.uniLLS.get(neighborNodeId);			

			try 
			{
				Thread.sleep(300);
			} 
			catch (InterruptedException e1) 
			{
				e1.printStackTrace();
			}

			if(checkFLS == 0)
			{
				if(checkUniLLS == 0)
				{
					MainNode.uniFLS.put(neighborNodeId, 1);
					MainNode.uniLLS.put(neighborNodeId, 1);

					NodeData.FLS.put(neighborNodeId, 1);
					NodeData.LLS.put(neighborNodeId, 1);				
				}
				else
				{
					MainNode.uniLLS.put(neighborNodeId, checkUniLLS + 1);
					NodeData.FLS.put(neighborNodeId, checkUniLLS + 1);					
					NodeData.LLS.put(neighborNodeId, checkUniLLS + 1);
				}

			}
			else
			{
				MainNode.uniLLS.put(neighborNodeId, MainNode.uniLLS.get(neighborNodeId)+1);
				NodeData.LLS.put(neighborNodeId, MainNode.uniLLS.get(neighborNodeId));				
			}

			synchronized(MainNode.obj)
			{
				MainNode.initialValue = MainNode.initialValue - value;
			}	
			
			msg = NodeData.LLS.get(neighborNodeId) + "::" +value;
			System.out.println("In send thread, Msg sending to "+neighborNodeId+" "+msg);
			sctpChannel = SctpChannels.getSctpChannel(neighborNodeId);
			
			if(sctpChannel.equals(null))
			{
				System.out.println("WTF!!!!!");
			}					
			else
			{
				byteBuf.clear();
				byteBuf.put(new byte[1024]);
				byteBuf.clear();

				byte [] message = msg.getBytes();
				MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0); 

				byteBuf.put(message);
				byteBuf.flip();

				try {
					sctpChannel.send(byteBuf, messageInfo);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				byteBuf.clear();
				byteBuf.put(new byte[1024]);
				byteBuf.clear();
			}	
		}
	}

}

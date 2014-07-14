package com.aos.two;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.nio.sctp.SctpChannel;

public class RecoveryThread implements Runnable 
{
	CheckpointThread ckthread = null;
	SctpChannel sctpChannel = null; 
	ByteBuffer byteBuf = ByteBuffer.allocate(1024); 
	String msg = null;
	Thread sendThread = new Thread();	
	int initiator = -1; 
	int receivedFrom = -1;
	int LLSFromRecvdFrom = -1;


	RecoveryThread(Thread sendThread)
	{
		this.sendThread = sendThread;
		this.initiator = MainNode.THIS_nodeID;
		NodeData.inTheRecov = MainNode.THIS_nodeID;
	}

	RecoveryThread(Thread sendThread, int initiator, int receivedFrom, int LLSFromRecvdFrom)
	{
		this.sendThread = sendThread;
		this.initiator = initiator;
		this.receivedFrom = receivedFrom;
		this.LLSFromRecvdFrom = LLSFromRecvdFrom;
	}

	@SuppressWarnings("deprecation")

	public void run()
	{

		try 
		{
			sendThread.suspend();
			System.out.println("Interrupting the sending thread to take the recovery #$#$#$#$#");
			ArrayList<Integer> recNebors = new ArrayList<Integer>();
			recNebors = NodeData.getRecNeighbors(MainNode.THIS_nodeID, initiator, receivedFrom);

			synchronized (NodeData.recNeborsync) 
			{
				NodeData.recNeborSize = recNebors.size();
			}

			//send the take checkpoint message to cohorts. TC = Take Checkpoint
			//the initiator
			if(NodeData.inTheRecov == MainNode.THIS_nodeID)		//initiator 
			{

				HashMap<Integer, Integer> LLS = new HashMap<Integer, Integer>();
				LLS = ReadCheckpointFile.getLLS(MainNode.THIS_nodeID);

				int iValue = ReadCheckpointFile.getiValue(MainNode.THIS_nodeID);

				System.out.println("LLLLLLSSSSSSS: "+LLS+"   "+iValue);

				for(int i = 0; i < recNebors.size(); i++)
				{

					int nodeId = recNebors.get(i);
					msg = "TR::"+MainNode.THIS_nodeID+"::"+MainNode.THIS_nodeID+"::"+LLS.get(nodeId);
					sctpChannel = SctpChannels.getSctpChannel(nodeId);
					NodeData.sendMessage(sctpChannel, msg, 2);

				}

				//wait for its cohorts to receive the reply
				System.out.println("In REC: i am waiting for TR reply with size "+NodeData.recNeborSize);
				synchronized (MainNode.recInterrupt) 
				{
					try 
					{
						MainNode.recInterrupt.wait();
					} 
					catch (Exception e)
					{				
						e.printStackTrace();
					}
				}
				System.out.println("In REC: i got all the TR replies...");


				//inform my cohorts to make permanent or discard it. 
				if(NodeData.willingToRec == true)
				{
					NodeData.makePermanentRec(iValue, LLS);
					for(int i = 0; i < recNebors.size(); i++)
					{

						int nodeId = recNebors.get(i);
						msg = "REC::"+MainNode.THIS_nodeID+"::"+initiator;
						sctpChannel = SctpChannels.getSctpChannel(nodeId);
						NodeData.sendMessage(sctpChannel, msg, 2);

					}
				}
				else if (NodeData.willingToRec == false)
				{
					System.out.println("Received: willingToRec = false");
					for(int i = 0; i < recNebors.size(); i++)
					{

						int nodeId = recNebors.get(i);
						msg = "DREC::"+MainNode.THIS_nodeID+"::"+initiator;
						sctpChannel = SctpChannels.getSctpChannel(nodeId);
						NodeData.sendMessage(sctpChannel, msg, 2);

					}
				}
			}

			else 
			{
				System.out.println("nodeata.LLR.get(receivedfROm): "+NodeData.LLR.get(receivedFrom));
				System.out.println("LLSFROMRECVDFROM: "+LLSFromRecvdFrom);

				HashMap<Integer, Integer> LLS = new HashMap<Integer, Integer>();
				LLS = ReadCheckpointFile.getLLS(MainNode.THIS_nodeID);

				int iValue = ReadCheckpointFile.getiValue(MainNode.THIS_nodeID);

				System.out.println("LLLLLLSSSSSSS: "+LLS+"   "+iValue);
				if(NodeData.LLR.get(receivedFrom) > LLSFromRecvdFrom)
				{		

					System.out.println("i am in the if condition..");
					if(recNebors.size()==0)
					{				
						if(MainNode.THIS_nodeID != initiator)
						{
							sctpChannel = SctpChannels.getSctpChannel(receivedFrom);
							msg = "WTR::"+MainNode.THIS_nodeID+"::"+initiator+"";		      		                
							NodeData.sendMessage(sctpChannel, msg, 2);
						}

						// need to do something more here. need to wait for checkpoint here. 
						// wait for the request here dont forget.

						synchronized (MainNode.recPermanentInterrupt) 
						{
							try 
							{
								MainNode.recPermanentInterrupt.wait();
							} 
							catch (Exception e)
							{				
								e.printStackTrace();
							}
						}
					}
					//i have cohorts
					else
					{	
						for(int i=0; i<recNebors.size(); i++)
						{
							if(recNebors.get(i)!= initiator)
							{
								int nodeId = recNebors.get(i);
								sctpChannel = SctpChannels.getSctpChannel(nodeId);
								msg = "TR::"+MainNode.THIS_nodeID+"::"+initiator+"::"+ LLS.get(nodeId);
								NodeData.sendMessage(sctpChannel, msg, 2);
							}
						}

						//waiting for reply. 
						System.out.println("i am waiting for reply to my TR.. with size::"+recNebors.size());

						synchronized (MainNode.recInterrupt) 
						{
							try 
							{
								MainNode.recInterrupt.wait();
							} 
							catch (Exception e)
							{				
								e.printStackTrace();
							}
						}

						System.out.println("i got all the replies...");

						if(NodeData.willingToChkpt==true)
						{
							sctpChannel = SctpChannels.getSctpChannel(receivedFrom);
							msg = "WTR::"+MainNode.THIS_nodeID+"::"+initiator;		      		                
							NodeData.sendMessage(sctpChannel, msg, 2);

						}

						if(NodeData.willingToChkpt==false)
						{
							sctpChannel = SctpChannels.getSctpChannel(receivedFrom);
							msg = "NWTR::"+MainNode.THIS_nodeID+"::"+initiator;		      		                
							NodeData.sendMessage(sctpChannel, msg, 2);		
						}

						//waiting for decision ckpt
						synchronized (MainNode.recPermanentInterrupt) 
						{
							try 
							{
								MainNode.recPermanentInterrupt.wait();
							} 
							catch (Exception e)
							{				
								e.printStackTrace();
							}
						} 		

						//write the logic of sending the permanent message or discard permanent message to cohorts. 
						if(NodeData.makeRecPermanent == true)
						{

							for(int i=0;i<recNebors.size();i++)
							{
								if(recNebors.get(i)!= initiator)
								{
									int nodeId = recNebors.get(i);
									msg = "REC::"+MainNode.THIS_nodeID+"::"+initiator;
									sctpChannel = SctpChannels.getSctpChannel(nodeId);
									NodeData.sendMessage(sctpChannel, msg, 2);								
								}
							}
						}
						else if (NodeData.makeRecPermanent == false)
						{
							for(int i=0;i<recNebors.size();i++)
							{
								if(recNebors.get(i)!= initiator)
								{
									int nodeId = recNebors.get(i);
									msg = "DREC::"+MainNode.THIS_nodeID+"::"+initiator;
									sctpChannel = SctpChannels.getSctpChannel(nodeId);
									NodeData.sendMessage(sctpChannel, msg, 2);		
								}
							}
						}
					}
				}//end of condition else

				//i dont need to recover just send i am willing to do so. the make permament message will be sent afterwards 
				//which will be ignored. 
				else if(recNebors.size()==0)
				{
					//just send the wtc message
					sctpChannel = SctpChannels.getSctpChannel(receivedFrom);
					msg = "WTR::"+MainNode.THIS_nodeID+"::"+initiator;		      		                
					NodeData.sendMessage(sctpChannel, msg, 2);
				}
				
				else
				{
					sctpChannel = SctpChannels.getSctpChannel(receivedFrom);
					msg = "WTR::"+MainNode.THIS_nodeID+"::"+initiator;		      		                
					NodeData.sendMessage(sctpChannel, msg, 2);
				}
			}

			//doing cleanup!
			NodeData.inTheRecov = -1;
			NodeData.willingToRec = true;
			NodeData.recNeborSize = -1;
			NodeData.permanentRecTaken = false;
			System.out.println(" Recovery Done !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "+MainNode.initialValue);
			sendThread.resume();
		} 
		catch (Exception e)
		{			
			e.printStackTrace();
		}
	}

}
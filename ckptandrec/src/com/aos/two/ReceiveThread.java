package com.aos.two;

import java.nio.ByteBuffer;
import java.util.HashMap;


import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

public class ReceiveThread implements Runnable
{
	SctpChannel sctpChannel; 

	final ByteBuffer byteBuf = ByteBuffer.allocate(1024); 
	String msg = null;
	int value;
	Thread sendThread = null;

	public ReceiveThread(SctpChannel sctpChannel, Thread sendThread)
	{
		this.sctpChannel = sctpChannel;
		this.sendThread = sendThread;
	}

	public void run()
	{
		while(true)
		{
			try
			{	       
				MessageInfo messageInfo = sctpChannel.receive(byteBuf , null, null);
				int streamNo = messageInfo.streamNumber();
				String msg = new String (byteBuf.array()).trim();
				int nodeID = SctpChannels.getNodeId(sctpChannel);

				//Application Messages - Stream 000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
				if(!msg.equals(null) && streamNo == 0) // Stream Num 0 for Application Msgs
				{

					System.out.println("In receive thread, Msg received from "+nodeID+" "+msg+" over stream "+streamNo);

					String tempMsg[] = msg.split("::");

					NodeData.LLR.put(nodeID, Integer.parseInt(tempMsg[0]));
					value = Integer.parseInt(tempMsg[1]); 
					synchronized(MainNode.obj)
					{
						MainNode.initialValue = MainNode.initialValue + value;
					}
					byteBuf.clear();
					byteBuf.put(new byte[1024]);
					byteBuf.clear();		           
				}

				//Checkpoint Messages - Stream  1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111
				if(!msg.equals(null) && streamNo == 1) // Stream Num 1 for Checkpoint Requests
				{
					System.out.println("In receive thread, Msg received from "+nodeID+" "+msg+" over stream "+streamNo);
					System.out.println("INTHECKPT: "+NodeData.inTheChkpt);
					//int nodeID = SctpChannels.getNodeId(sctpChannel);

					String temp[] = msg.split("::");
					String msgType = temp[0];

					if(msgType.equals("TC"))
					{
						int receivedFrom = Integer.parseInt(temp[1]);
						int initiator = Integer.parseInt(temp[2]);
						int LLRFromI = Integer.parseInt(temp[3]);

						if(NodeData.inTheRecov == -1 && NodeData.inTheChkpt==-1)
						{

							NodeData.inTheChkpt = initiator;
							CheckpointThread chkpt = new CheckpointThread(sendThread, initiator, receivedFrom, LLRFromI);
							Thread chkptThread = new Thread(chkpt);
							chkptThread.start();
						}
						else
						{
							if(initiator == NodeData.inTheChkpt)
							{
								// reply willingTochktpt YES
								String msgToSend = "WTC::"+MainNode.THIS_nodeID;

								NodeData.sendMessage(sctpChannel, msgToSend, 1);			            		
							}
							else
							{
								// reject the request since it is already inTheChkpt
								String msgToSend = "NWTC::"+MainNode.THIS_nodeID;

								NodeData.sendMessage(sctpChannel, msgToSend, 1); 
							}
						}
					}

					if(msgType.equals("WTC"))
					{
						synchronized (NodeData.cohortsync) 
						{
							NodeData.cohortSize--;
						}
						if(NodeData.cohortSize==0)
						{
							//send message 

							synchronized (MainNode.chkptInterrupt) 
							{
								try 
								{
									MainNode.chkptInterrupt.notify();
								} 
								catch (Exception e)
								{				
									e.printStackTrace();
								}
							}

						}

					}

					if(msgType.equals("NWTC"))
					{
						synchronized (NodeData.cohortsync) 
						{
							NodeData.cohortSize--;
						}
						NodeData.willingToChkpt = false;
						if(NodeData.cohortSize==0)
						{
							synchronized (MainNode.chkptInterrupt) 
							{
								try 
								{
									MainNode.chkptInterrupt.notify();
								} 
								catch (Exception e)
								{				
									e.printStackTrace();
								}
							}        
						}
					}
					if(msgType.equals("MCP") )

					{
						//logic to notify that i've taken the checkpoint and he can send message to other cohorts.

						int initiator = Integer.parseInt(temp[2]);

						if(NodeData.permanentCkptTaken == false  && initiator == NodeData.inTheChkpt)
						{
							NodeData.makePermanent();
							NodeData.resetCurrentHmaps();
							synchronized (MainNode.chkptPermanentInterrupt) 
							{
								try 
								{
									NodeData.makeCkptPermanent = true;
									MainNode.chkptPermanentInterrupt.notify();
								} 
								catch (Exception e)
								{				
									e.printStackTrace();
								}
							} 
							NodeData.permanentCkptTaken = true;

						}
					}


					if(msgType.equals("DMCP"))
					{
						int initiator = Integer.parseInt(temp[2]);
						if(initiator == NodeData.inTheChkpt)
						{
							TentativeChkpt.resetTentativeHmaps();
						}

						synchronized (MainNode.chkptPermanentInterrupt) 
						{
							try 
							{
								NodeData.makeCkptPermanent = false;
								MainNode.chkptPermanentInterrupt.notify();
							} 
							catch (Exception e)
							{				
								e.printStackTrace();
							}
						} 
					}

					byteBuf.clear();
					byteBuf.put(new byte[1024]);
					byteBuf.clear();		           
				}


				//Recovery Messages Stream 222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222
				if(!msg.equals(null) && streamNo == 2) // Stream Num 2 for Recovery Msgs
				{

					System.out.println("In receive thred, Msg received from "+nodeID+": "+msg+" over stream "+streamNo);
					System.out.println("INTHERECOV: "+NodeData.inTheRecov);
					//int nodeID = SctpChannels.getNodeId(sctpChannel);

					String temp[] = msg.split("::");
					String msgType = temp[0];

					if(msgType.equals("TR"))
					{
						int receivedFrom = Integer.parseInt(temp[1]);
						int initiator = Integer.parseInt(temp[2]);
						int LLSFromRecvdFrom = Integer.parseInt(temp[3]);

						if(NodeData.inTheRecov == -1 && NodeData.inTheChkpt == -1)
						{

							NodeData.inTheRecov = initiator;
							RecoveryThread recov = new RecoveryThread(sendThread, initiator, receivedFrom, LLSFromRecvdFrom);
							Thread recovThread = new Thread(recov);
							recovThread.start();
						}
						else
						{
							if(NodeData.inTheRecov == initiator)
							{
								String msgToSend = "WTR::"+MainNode.THIS_nodeID;

								NodeData.sendMessage(sctpChannel, msgToSend, 2);	
							}
							else
							{
								// reject the request since it is already inTheRecov
								String msgToSend = "NWTR::"+MainNode.THIS_nodeID;
								NodeData.sendMessage(sctpChannel, msgToSend, 2); 
							}
						}
					}

					if(msgType.equals("WTR"))
					{
						synchronized (NodeData.recNeborsync) 
						{
							NodeData.recNeborSize--;
						}

						if(NodeData.recNeborSize==0)
						{
							//send message 

							synchronized (MainNode.recInterrupt) 
							{
								try 
								{
									MainNode.recInterrupt.notify();
								} 
								catch (Exception e)
								{				
									e.printStackTrace();
								}
							}

						}

					}

					if(msgType.equals("NWTR"))
					{
						synchronized (NodeData.recNeborsync) 
						{
							NodeData.recNeborSize--;
						}
						NodeData.willingToRec = false;
						if(NodeData.recNeborSize==0)
						{
							synchronized (MainNode.recInterrupt) 
							{
								try 
								{
									MainNode.recInterrupt.notify();
								} 
								catch (Exception e)
								{				
									e.printStackTrace();
								}
							}        
						}
					}


					if(msgType.equals("REC") )

					{
						//logic to notify that ive taken the checkpoint and he can send message to other cohorts.

						int initiator = Integer.parseInt(temp[2]);
						//int receivedFrom = Integer.parseInt(temp[1]);

						if( NodeData.permanentRecTaken == false  && initiator == NodeData.inTheRecov )
						{
							// get values f iValue and LLS from shukla's work
							HashMap<Integer, Integer> LLS = new HashMap<Integer, Integer>();
							LLS = ReadCheckpointFile.getLLS(MainNode.THIS_nodeID);
							int iValue = ReadCheckpointFile.getiValue(MainNode.THIS_nodeID);

							NodeData.makePermanentRec(iValue, LLS);
							//sNodeData.resetCurrentHmaps();
							synchronized (MainNode.recPermanentInterrupt) 
							{
								try 
								{
									NodeData.makeRecPermanent = true;
									MainNode.recPermanentInterrupt.notify();
								} 
								catch (Exception e)
								{				
									e.printStackTrace();
								}
							} 
							NodeData.permanentRecTaken = true;

						}
					}


					if(msgType.equals("DREC"))
					{
						int initiator = Integer.parseInt(temp[2]);
						if(initiator == NodeData.inTheRecov)
						{
							synchronized (MainNode.recPermanentInterrupt) 
							{
								try 
								{
									NodeData.makeRecPermanent = false;
									MainNode.recPermanentInterrupt.notify();
								} 
								catch (Exception e)
								{				
									e.printStackTrace();
								}
							} 
						}
					}		
					byteBuf.clear();
					byteBuf.put(new byte[1024]);
					byteBuf.clear();		           
				}
			} 
			catch (Exception e) 
			{ 
				e.printStackTrace(); 
				break;
			}
		}
	}

}

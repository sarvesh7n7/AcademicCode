package com.aos.two;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import com.sun.nio.sctp.SctpChannel;

public class CheckpointThread implements Runnable 
{
	CheckpointThread ckthread = null;
	SctpChannel sctpChannel; 
    ByteBuffer byteBuf = ByteBuffer.allocate(1024); 
    String msg = null;
	Thread sendThread = new Thread();	
	int initiator = -1;
	int receivedFrom = -1;
	int LLRFromI = -1;
	
	
	CheckpointThread(Thread sendThread)
	{
		this.sendThread = sendThread;
		this.initiator = MainNode.THIS_nodeID;
		NodeData.inTheChkpt = MainNode.THIS_nodeID;
	}
	
	CheckpointThread(Thread sendThread, int initiator, int receivedFrom, int LLRFromI)
	{
		this.sendThread = sendThread;
		this.initiator = initiator;
		this.receivedFrom = receivedFrom;
		this.LLRFromI = LLRFromI;
	}
	
	@SuppressWarnings("deprecation")
	public void run()
	{		
		try 
		{
			sendThread.suspend();
			System.out.println("Interrupting the sending thread to take the checkpoint");
			
			
			//the checkpoint algorithm will be implemented here.
			ArrayList<Integer> cohorts = new ArrayList<Integer>();
			cohorts = NodeData.getCohorts(MainNode.THIS_nodeID, initiator, receivedFrom);
			synchronized (NodeData.cohortsync) 
			{
				NodeData.cohortSize = cohorts.size();
			}
						
			//send the take checkpoint message to cohorts. TC = Take Checkpoint
			//if i am the initiator
			if(NodeData.inTheChkpt==MainNode.THIS_nodeID)		//initiator 
			{
				TentativeChkpt.storeTentativeData(MainNode.initialValue, NodeData.LLR, NodeData.FLS, NodeData.LLS);
				
				//i dont have any cohorts i just need to take checkpoint
				if(cohorts.size()==0)
				{	
					NodeData.makePermanent();
				}
				
				//oh cool. i have some cohorts. lets ask them to take the checkpoint.
				else
				{	
					for(int i=0;i<cohorts.size();i++)
					{
						if(cohorts.get(i)!= initiator) 
						{
							System.out.println("sending initiator::"+initiator);
							sctpChannel = SctpChannels.getSctpChannel(cohorts.get(i));
							
				            msg = "TC::"+MainNode.THIS_nodeID+"::"+initiator+"::"+NodeData.LLR.get(cohorts.get(i));		      		            
				           
				            NodeData.sendMessage(sctpChannel,msg,1);
						}
					}
			
					//wait for its cohorts to receive the reply
					System.out.println("i am waiting for reply..");
					
					synchronized (MainNode.chkptInterrupt) 
					{
						try 
						{
							MainNode.chkptInterrupt.wait();
						} 
						catch (Exception e)
						{				
							e.printStackTrace();
						}
					}
					System.out.println("i got all the replies...");
					
					//inform my cohorts to make this permanent or discard it. 
					if(NodeData.willingToChkpt == true)
					{
						
						NodeData.makePermanent();
						for(int i=0;i<cohorts.size();i++)
						{
							if(cohorts.get(i)!= initiator)
							{
								sctpChannel = SctpChannels.getSctpChannel(cohorts.get(i));
								
								msg = "MCP::"+MainNode.THIS_nodeID+"::"+initiator;		      		            
					           
					            NodeData.sendMessage(sctpChannel,msg,1);
							}
						}
					}
					else if (NodeData.willingToChkpt == false)
					{
						TentativeChkpt.resetTentativeHmaps();
					
						for(int i=0;i<cohorts.size();i++)
						{
							if(cohorts.get(i)!= initiator)
							{
								sctpChannel = SctpChannels.getSctpChannel(cohorts.get(i));
								
								msg = "DMCP::"+MainNode.THIS_nodeID+"::"+initiator;		      		            
					           
					            NodeData.sendMessage(sctpChannel,msg,1);
							}
						}
					}
					
				}
			}			
			//if i am one of the other nodes
			else 
			{
				//first check the condition. 
				NodeData.needofckpt = true;
				if(LLRFromI >= NodeData.FLS.get(receivedFrom) && NodeData.FLS.get(receivedFrom) > 0 )
				{
					//i do need to take the checkpoint
					TentativeChkpt.storeTentativeData(MainNode.initialValue, NodeData.LLR, NodeData.FLS, NodeData.LLS);

					if(cohorts.size()==0)
					{				
						if(MainNode.THIS_nodeID != initiator)
						{
							sctpChannel = SctpChannels.getSctpChannel(receivedFrom);
							msg = "WTC::"+MainNode.THIS_nodeID+"::"+initiator;		      		                
							NodeData.sendMessage(sctpChannel, msg, 1);
						}
							
						
						// need to do something more here. need to wait for checkpoint here. 
						// wait for the request here dont forget.
						//if(NodeData.cohortSize !=0 )
						{
							synchronized (MainNode.chkptPermanentInterrupt) 
		        			{
		        				try 
		        				{
		        					MainNode.chkptPermanentInterrupt.wait();
		        				} 
		        				catch (Exception e)
		        				{				
		        					e.printStackTrace();
		        				}
		        			}
						}
					}
					//i have cohorts
					//informing my cohorts to take the checkpoint. 
					else
					{	
						for(int i=0;i<cohorts.size();i++)
						{
							if(cohorts.get(i)!= initiator)
							{
								sctpChannel = SctpChannels.getSctpChannel(cohorts.get(i));

								msg = "TC::"+MainNode.THIS_nodeID+"::"+initiator+"::"+ NodeData.LLR.get(cohorts.get(i));		      		            

								NodeData.sendMessage(sctpChannel, msg, 1);
							}
						}

						//waiting for this shit faces to give me reply. 
						System.out.println("i am waiting for reply.. with cohort size::"+NodeData.cohortSize);
						//if(NodeData.cohortSize != 0 )
						{
							synchronized (MainNode.chkptInterrupt) 
							{
								try 
								{
									MainNode.chkptInterrupt.wait();
								} 
								catch (Exception e)
								{				
									e.printStackTrace();
								}
							}

						}
						
						System.out.println("i got all the replies...");
						
						if(NodeData.willingToChkpt==true)
						{
							sctpChannel = SctpChannels.getSctpChannel(receivedFrom);
							msg = "WTC::"+MainNode.THIS_nodeID+"::"+initiator;		      		                
							NodeData.sendMessage(sctpChannel, msg, 1);

						}
						
					
						if(NodeData.willingToChkpt==false)
						{
							sctpChannel = SctpChannels.getSctpChannel(receivedFrom);
							msg = "NWTC::"+MainNode.THIS_nodeID+"::"+initiator;		      		                
							NodeData.sendMessage(sctpChannel, msg, 1);		
						}
						
						//waiting to send me decision ckpt
						synchronized (MainNode.chkptPermanentInterrupt) 
	        			{
	        				try 
	        				{
	        					MainNode.chkptPermanentInterrupt.wait();
	        				} 
	        				catch (Exception e)
	        				{				
	        					e.printStackTrace();
	        				}
	        			} 		
						
						//write the logic of sending the permanent message or discard permanent message to cohorts. 
						
						if(NodeData.makeCkptPermanent == true)
						{
							for(int i=0;i<cohorts.size();i++)
							{
								if(cohorts.get(i)!= initiator)
								{
									sctpChannel = SctpChannels.getSctpChannel(cohorts.get(i));
									System.out.println("sending the MCP message from checkpoint thread");
									msg = "MCP::"+MainNode.THIS_nodeID+"::"+initiator;		      		            
						           
						            NodeData.sendMessage(sctpChannel,msg,1);
								}
							}
						}
						else if (NodeData.makeCkptPermanent == false)
						{
							TentativeChkpt.resetTentativeHmaps();
							for(int i=0;i<cohorts.size();i++)
							{
								if(cohorts.get(i)!= initiator)
								{
									System.out.println("sending the DMCP message from checkpoint thread");
									sctpChannel = SctpChannels.getSctpChannel(cohorts.get(i));
									
									msg = "DMCP::"+MainNode.THIS_nodeID+"::"+initiator;		      		            
						           
						            NodeData.sendMessage(sctpChannel,msg,1);
								}
							}
						}
					}
					
					
				}//end of condition else
				
				
				//i dont need to take the checkpoint the condition is not satisfied so dont need to take ckpt but inform
				else if(cohorts.size()==0)
				{
					//just send the wtc message
					sctpChannel = SctpChannels.getSctpChannel(receivedFrom);
					msg = "WTC::"+MainNode.THIS_nodeID+"::"+initiator;		      		                
					NodeData.sendMessage(sctpChannel, msg, 1);
				}
				
				else
				{
					sctpChannel = SctpChannels.getSctpChannel(receivedFrom);
					msg = "WTC::"+MainNode.THIS_nodeID+"::"+initiator;		      		                
					NodeData.sendMessage(sctpChannel, msg, 1);
				}
			}		
		
			//doing my cleanup!
			//saying that i am not in any checkpointing algorithm now and there is no initator.
			NodeData.inTheChkpt = -1;
			NodeData.willingToChkpt = true;
			NodeData.cohortSize = -1;
			sendThread.resume();
			NodeData.permanentCkptTaken = false;
			NodeData.needofckpt = false;
		} 
		catch (Exception e)
		{			
			e.printStackTrace();
		}	
	}

}
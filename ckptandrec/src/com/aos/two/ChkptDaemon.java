package com.aos.two;

public class ChkptDaemon implements Runnable  {

	public int interval = -1;
	public Thread sendThread = null;
	ChkptDaemon(int interval, Thread sendThread)
	{
		this.interval = interval;
		this.sendThread = sendThread;
	}
	
	public void run()
	{
		try
		{
			for(int i=0;i<3;i++)
			{				
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) 
				{					
					e.printStackTrace();
				}
				
							
				if(NodeData.inTheChkpt == -1 && NodeData.inTheRecov == -1)
				{
					CheckpointThread chkpt = new CheckpointThread(sendThread);
					Thread chkptThread = new Thread(chkpt);
					chkptThread.start();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

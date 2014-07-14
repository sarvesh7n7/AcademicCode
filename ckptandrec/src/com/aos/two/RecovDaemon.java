package com.aos.two;

public class RecovDaemon implements Runnable {
	
	public int interval = -1;
	public Thread sendThread = null;
	
	RecovDaemon(int interval, Thread sendThread)
	{
		this.interval = interval;
		this.sendThread = sendThread;
	}
	
	public void run()
	{
		try
		{
			for(int i=0;i<1;i++)
			{				
				try 
				{
					Thread.sleep(interval);
				} catch (InterruptedException e) 
				{					
					e.printStackTrace();
				}				
							
				if(NodeData.inTheChkpt == -1 && NodeData.inTheRecov == -1)
				{
					RecoveryThread recth = new RecoveryThread(sendThread);
					Thread recThread = new Thread(recth);
					recThread.start();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}

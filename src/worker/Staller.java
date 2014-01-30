package worker;

/** Halts the current thread until it is notified */
public class Staller 
{
	private boolean doneWaiting = false;
	public Staller()
	{
		
	}
	public void stall()
	{
		doneWaiting = false;
		synchronized(this)
		{
			while(!doneWaiting)
			{
				try
				{
					wait();
					doneWaiting = true;
				}
				catch(InterruptedException ignore) {}
			}
		}
	}
}

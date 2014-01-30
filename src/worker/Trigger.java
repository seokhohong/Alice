package worker;

/** Will wait until notification, at which point the Triggerable will run */
public class Trigger extends Thread 
{
	private boolean triggered = false;
	private Triggerable triggerable;
	public Trigger(Triggerable triggerable)
	{
		super(triggerable.toString()+" Trigger");
		this.triggerable = triggerable;
	}
	@Override
	public void run()
	{
		while(true)
		{
			triggered = false;
			synchronized(this)
			{
				while(!triggered)
				{
					try
					{
						wait();
						triggered = true;
						new DoTrigger().start();
					}
					catch(InterruptedException ignore) {}
				}
			}
		}
	}
	public synchronized void trigger()
	{
		notifyAll();
	}
	private class DoTrigger extends Thread
	{
		@Override
		public void run()
		{
			triggerable.trigger();
		}
	}
}

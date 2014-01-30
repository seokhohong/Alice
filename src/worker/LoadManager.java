package worker;

import java.util.HashSet;
import java.util.Set;

import macro.Macro;

/** Ensures only one profile updater at a time (to leave the rest of the Drivers to do other work) */
public class LoadManager extends Thread
{
	private Set<LimitedFrequencyOperation> updaters = new HashSet<LimitedFrequencyOperation>();
	
	public LoadManager(Set<LimitedFrequencyOperation> operations)
	{
		updaters = operations;
	}
	@Override
	public void run()
	{
		while(true)
		{
			boolean didWork = false;
			for(LimitedFrequencyOperation updater : updaters)
			{
				if(updater.shouldUpdate())
				{
					didWork = true;
					new Worker(updater).start();
					Macro.sleep(30000);
				}
			}
			if(!didWork)
			{
				Macro.sleep(1000);
			}
		}
	}
	private class Worker extends Thread
	{
		private LimitedFrequencyOperation operation;
		private Worker(LimitedFrequencyOperation operation)
		{
			this.operation = operation;
		}
		@Override
		public void run()
		{
			operation.doWork();
		}
	}
}

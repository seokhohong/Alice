package worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import macro.Macro;

public class Multitasker 
{
	private static final int MAX_THREADS = 16;
	private static final int POLL_TIME = 120; //should be plenty of time
	
	private	ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
	private int pollTime = POLL_TIME;
	
	public Multitasker()
	{
		
	}
	public Multitasker(int numThreads)
	{
		executor = Executors.newFixedThreadPool(numThreads);
	}
	public Multitasker(int numThreads, int pollSeconds)
	{
		executor = Executors.newFixedThreadPool(numThreads);
		pollTime = pollSeconds;
	}
	
	public void load(Runnable runnable)
	{
		while(true)
		{
			try
			{
				executor.execute(runnable);
				break;
			}
			catch(RejectedExecutionException e)
			{
				Macro.sleep(500);
			}
		}
	}
	/** Waits until all threads are complete, or they take too long*/
	public void done()
	{
		executor.shutdown();
		try 
		{
			executor.awaitTermination(pollTime, TimeUnit.SECONDS);
		} 
		catch (InterruptedException ignored) {}
	}
}

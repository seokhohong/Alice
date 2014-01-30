package jane;

import worker.LimitedFrequencyOperation;

public class Jane implements LimitedFrequencyOperation
{	
	public static void main(String[] args)
	{
		new Jane().doWork();
	}

	private HistoryDatabase historyDatabase;
	private long lastModified = 0L; 
	
	public Jane(HistoryDatabase historyDatabase)
	{
		this.historyDatabase = historyDatabase;
	}
	private Jane()
	{
		historyDatabase = new HistoryDatabase();
	}
	@Override
	public synchronized void doWork()
	{
		new Worker().run();
	}
	class Worker extends Thread
	{
		@Override
		public void run()
		{
			update();
		}
	}
	private void update()
	{
		lastModified = System.currentTimeMillis();
		historyDatabase.updateSequentially();
	}
	@Override
	public boolean shouldUpdate() 
	{
		return System.currentTimeMillis() - lastModified > 1000 * 60 * 5;
	}
}

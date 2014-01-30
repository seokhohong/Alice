package jane;

import macro.Macro;

public class JaneRunner extends Thread 
{
	private HistoryDatabase hDatabase;
	public JaneRunner(HistoryDatabase hDatabase)
	{
		this.hDatabase = hDatabase;
	}
	@Override
	public void run()
	{
		while(true)
		{
			Macro.sleep(100000);
			new Jane(hDatabase);
		}
	}
}

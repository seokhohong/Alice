package bryan;

import jane.WatchPageScraper;

public class UnwatchScraper extends WatchPageScraper 
{
	private WatchList watchList;
	protected UnwatchScraper(String name, int offset, WatchList watchList) 
	{
		super(name, offset);
		this.watchList = watchList;
	}

	@Override
	protected synchronized void processWatcher(String name) 
	{
		watchList.add(name);
	}

}

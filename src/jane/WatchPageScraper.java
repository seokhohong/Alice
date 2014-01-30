package jane;

import java.util.List;

import bryan.WatchList;
import scraper.WebScraper;
import data.Artist;

public abstract class WatchPageScraper extends Thread
{
	private int offset;
	private String name;
	protected WatchPageScraper(String name, int offset)
	{
		this.name = name;
		this.offset = offset;
	}
	@Override
	public void run()
	{
		//A list of watchers in the correct order
		List<String> watchers = new WatchPageParser(WebScraper.get(getUrl(), false)).getList();
		for(String watcher : watchers)
		{
			processWatcher(watcher);
		}
	}
	public String getUrl()
	{
		return new Artist(name).getWatchesURL(offset * WatchList.getWatchersPer());
	}
	protected abstract void processWatcher(String name);
}
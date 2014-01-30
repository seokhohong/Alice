package bryan;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import data.Artist;
import scraper.WebScraper;
import worker.Multitasker;

public class WatchList 
{
	private static final int WATCHERS_PER = 100;					public static final int getWatchersPer() { return WATCHERS_PER; }
	
	private String name;
	private Set<String> watchers = new HashSet<String>();			public Collection<String> getList() { return watchers; }
	
	public WatchList(String name)
	{
		this.name = name;
		fullUpdate();
	}
	
	public void add(String watcher)
	{
		synchronized(watchers)
		{
			watchers.add(watcher);
		}
	}
	
	/** Refreshes the whole WatchList */
	public void fullUpdate()
	{
		String pageSource = WebScraper.get(new Artist(name).getHomeURL());
		int numWatchers = parseNumWatchers(pageSource);
		do
		{
			watchers.clear();
			Multitasker verifier = new Multitasker();
			for(int a = 0; a <= numWatchers / WATCHERS_PER; a++)
			{
				synchronized(verifier)
				{
					verifier.load(new UnwatchScraper(name, a, this));
				}
			}
			verifier.done();
		} 
		while(watchers.size() != numWatchers);
	}
	private static final String WATCHER_START_TAG = "<strong>";
	private int parseNumWatchers(String pageSource)
	{
		int watcherEnd = pageSource.indexOf(" </strong>Watchers");
		if(watcherEnd == -1)
		{
			watcherEnd = pageSource.indexOf(" </strong> Watchers");
		}
		int watcherStart = pageSource.lastIndexOf(WATCHER_START_TAG, watcherEnd) + WATCHER_START_TAG.length();
		int numWatchers = 0;
		try
		{
			numWatchers = Integer.parseInt(pageSource.substring(watcherStart, watcherEnd).replace(",", ""));
		}
		catch(Exception ignored ) {}
		return numWatchers;
	}
}

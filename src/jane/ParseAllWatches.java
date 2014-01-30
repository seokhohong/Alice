package jane;

import bryan.WatchList;
import scraper.WebScraper;
import worker.Multitasker;
import data.Artist;

public abstract class ParseAllWatches
{	
	private PayerHistory payerHistory;
	private Multitasker historyVerifier;
	
	ParseAllWatches(PayerHistory payerHistory, Multitasker historyVerifier)
	{
		this.payerHistory = payerHistory;
		this.historyVerifier = historyVerifier;
	}
	
	synchronized void run()
	{
		String pageSource = WebScraper.get(new Artist(payerHistory.getName()).getHomeURL());
		int numWatchers = parseNumWatchers(pageSource);
		for(int a = 0; a <= numWatchers / WatchList.getWatchersPer(); a++)
		{
			synchronized(historyVerifier)
			{
				historyVerifier.load(loadScraper(payerHistory, a));
			}
		}
	}
	private static final String WATCHER_START_TAG = "<strong>";
	private int parseNumWatchers(String pageSource)
	{
		int watcherEnd = pageSource.indexOf(" </strong>Watchers");
		int watcherStart = pageSource.lastIndexOf(WATCHER_START_TAG, watcherEnd) + WATCHER_START_TAG.length();
		return Integer.parseInt(pageSource.substring(watcherStart, watcherEnd).replace(",", ""));
	}
	protected abstract WatchPageScraper loadScraper(PayerHistory payerHistory, int offset);
}

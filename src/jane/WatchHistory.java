package jane;

import worker.Multitasker;

public class WatchHistory extends ParseAllWatches 
{
	WatchHistory(PayerHistory payerHistory, Multitasker historyVerifier) 
	{
		super(payerHistory, historyVerifier);
	}

	@Override
	protected WatchPageScraper loadScraper(PayerHistory payerHistory, int offset) 
	{
		return new WatchHistoryScraper(payerHistory, offset);
	}

}

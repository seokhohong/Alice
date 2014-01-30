package scraper;

import data.Currency;
import data.Earner;
import data.EarnerDatabase;
import data.Payer;
import data.PayerDatabase;
import jane.HistoryDatabase;
import jane.WatchPageScraper;

public class WatchWorkScraper extends WatchPageScraper
{
	private PayerDatabase pDatabase;
	private EarnerDatabase eDatabase;
	private HistoryDatabase hDatabase;
	
	private Payer payer;
	
	public WatchWorkScraper(PayerDatabase pDatabase, EarnerDatabase eDatabase, HistoryDatabase hDatabase, Payer payer)
	{
		super(payer.getName(), 0); //first page only
		this.pDatabase = pDatabase;
		this.eDatabase = eDatabase;
		this.hDatabase = hDatabase;
		this.payer = payer;
	}

	@Override
	protected void processWatcher(String name) 
	{
		hDatabase.foundTransaction(Currency.WATCH, payer.getName(), name);
		for(Earner earner : eDatabase.getEarners())
		{
			System.out.println("Attempting to record Watch from "+earner.getName()+" to "+payer.getName());
			if(payer.accepts(Currency.WATCH) && earner.addWatchFrom(payer))
			{
				pDatabase.hasReceived(payer.getName(), Currency.WATCH);
			}
		}
	}
}

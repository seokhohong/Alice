package scraper;

import org.openqa.selenium.UnhandledAlertException;

import data.Currency;
import data.Earner;
import data.EarnerDatabase;
import data.Payer;
import data.PayerDatabase;


public class WatchScraper extends Thread 
{
	private PayerDatabase pDatabase;
	private EarnerDatabase eDatabase;
	
	private Payer payer;
	
	public WatchScraper(PayerDatabase pDatabase, EarnerDatabase eDatabase, Payer payer)
	{
		this.pDatabase = pDatabase;
		this.eDatabase = eDatabase;
		this.payer = payer;
	}
	
	@Override
	public void run()
	{
		String pageSource = WebScraper.get(payer.getWatchesURL());
		parseWatches(pageSource, payer);
	}
	private synchronized void parseWatches(String pageSource, Payer payer) throws UnhandledAlertException
	{
		synchronized(eDatabase)
		{
			for(Earner earner : eDatabase.getEarners())
			{
				if(pageSource.contains(surroundName(earner.getName())))
				{
					System.out.println("Attempting to record Watch from "+earner.getName()+" to "+payer.getName());
					if(payer.accepts(Currency.WATCH) && earner.addWatchFrom(payer))
					{
						pDatabase.hasReceived(payer.getName(), Currency.WATCH);
					}
				}
			}
		}
	}
	//Make sure we don't pick out substrings or something
	private String surroundName(String name)
	{
		return "http://"+name+".";
	}
}

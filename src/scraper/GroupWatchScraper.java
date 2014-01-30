package scraper;

import org.openqa.selenium.UnhandledAlertException;

import data.Currency;
import data.Earner;
import data.EarnerDatabase;
import data.Group;
import data.GroupDatabase;


public class GroupWatchScraper extends Thread 
{
	private GroupDatabase gDatabase;
	private EarnerDatabase eDatabase;
	
	private Group group;
	
	public GroupWatchScraper(GroupDatabase gDatabase, EarnerDatabase eDatabase, Group group)
	{
		this.gDatabase = gDatabase;
		this.eDatabase = eDatabase;
		this.group = group;
	}
	
	@Override
	public void run()
	{
		String pageSource = WebScraper.get(group.getWatchesURL(0)); //first page
		parseWatches(pageSource, group);
	}
	private synchronized void parseWatches(String pageSource, Group group) throws UnhandledAlertException
	{
		for(Earner earner : eDatabase.getEarners())
		{
			if(pageSource.contains(earner.getName()))
			{
				System.out.println("Attempting to record Watch from "+earner.getName()+" to "+group.getName());
				if(group.accepts(Currency.WATCH) && earner.addWatchFrom(group))
				{
					gDatabase.hasReceived(group.getName(), Currency.WATCH);
				}
			}
		}
	}
}

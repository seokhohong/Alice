package scraper;

import main.Listener;
import data.Critique;
import data.Currency;
import data.Earner;
import data.EarnerDatabase;
import data.Payer;
import data.PayerDatabase;
import data.RecordKeeper;

public class CritiqueScraper extends Thread 
{
	private PayerDatabase pDatabase;
	private EarnerDatabase eDatabase;
	private Critique critique;
	private RecordKeeper recorder;
	private Listener listener;
	
	public CritiqueScraper(PayerDatabase pDatabase, EarnerDatabase eDatabase, Critique critique, RecordKeeper recorder, Listener listener)
	{
		this.pDatabase = pDatabase;
		this.eDatabase = eDatabase;
		this.critique = critique;
		this.recorder = recorder;
		this.listener = listener;
	}
	
	@Override
	public void run()
	{
		Earner earner = eDatabase.get(critique.getEarnerName());
		Payer payer = pDatabase.get(critique.getDeviant());
		if(earner == null || payer == null)
		{
			return;
		}
		String pageSource = WebScraper.get(critique.URL());
		if(deviantDeletes(pageSource))
		{
			pDatabase.removeCritique(critique);
		}
		if(deviantAccepts(pageSource) || critique.pastDue())
		{
			System.out.println("Accepted "+critique);
			recorder.add(Currency.CRITIQUE);
			try {
				eDatabase.addCritique(critique);
			} catch (Exception e)
			{
				listener.catchException(e);
			}
			pDatabase.acceptCritique(critique);
		}
	}
	
	private static final String ACCEPT_TAG = "<div class=\"artist_feedback\">The Artist thought this was FAIR</div>";
	private static final String EXIST_TAG = "Devious Rating";
	private boolean deviantAccepts(String pageSource)
	{
		return pageSource.contains(ACCEPT_TAG);
	}
	private boolean deviantDeletes(String pageSource)
	{
		return !pageSource.contains(EXIST_TAG);
	}
}

package jane;

import scraper.WebScraper;
import utils.StringOps;
import worker.Multitasker;
import data.Artist;

public class LlamaHistory
{
	private static final int INIT_PAGE = 10;
	private static final int LLAMAS_PER = 5;			public static int getLlamasPer() { return LLAMAS_PER; }
	private static final int MAX_LLAMAS = 112000;
	
	private PayerHistory payerHistory;
	private Multitasker historyVerifier;
	
	LlamaHistory(PayerHistory payerHistory, Multitasker historyVerifier)
	{
		this.payerHistory = payerHistory;
		this.historyVerifier = historyVerifier;
	}
	
	synchronized void run()
	{
		String pageSource = WebScraper.get(new Artist(payerHistory.getName()).getLlamasURL());
		int numLlamas = Math.max(parseNumLlamas(pageSource), MAX_LLAMAS);
		for(int a = INIT_PAGE; a <= numLlamas / LLAMAS_PER; a++)
		{
			synchronized(historyVerifier)
			{
				historyVerifier.load(new LlamaHistoryScraper(payerHistory, a));
			}
		}
	}
	private static final String BADGES_SENT_END = " Badges sent, ";
	private int parseNumLlamas(String pageSource)
	{
		int numSent = 0;
		int numReceived = 0;
		try
		{
			numSent = Integer.parseInt(StringOps.textBetween(pageSource, "<div class=\"c zebraheader\">", BADGES_SENT_END).replace(",", ""));
		}
		catch(Exception noLlamas) {}
		try
		{
			numReceived = Integer.parseInt(StringOps.textBetween(pageSource, BADGES_SENT_END, " Badges received").replace(",", ""));
		}
		catch(Exception noLlamas) {}
		//System.out.println(payerHistory.getName()+" has "+(numSent+numReceived) + " llamas");
		return numSent + numReceived;
	}
	
}

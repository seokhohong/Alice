package scraper;

import java.util.ArrayList;
import java.util.Date;

import utils.StringOps;
import data.Earner;
import data.Critique;
import data.PayerDatabase;

/** Reads the critiques off of a particular earner's page */
public class CritiqueListScraper extends Thread
{
	private Earner earner;
	private int offset;
	private PayerDatabase pDatabase;
	
	public CritiqueListScraper(Earner earner, PayerDatabase pDatabase, int offset)
	{
		this.earner = earner;
		this.pDatabase = pDatabase;
		this.offset = offset;
	}
	
	@Override
	public void run()
	{
		String pageSource = WebScraper.get(earner.getCritiqueListURL(offset));
		ArrayList<Integer> critiqueIds = parseCritiqueIds(pageSource);
		ArrayList<Integer> artworkIds = parseArtworkIds(pageSource);
		ArrayList<String> deviants = parseDeviants(pageSource);
		ArrayList<String> artworkUrls = parseArtUrls(pageSource);
		for(int a = deviants.size(); a --> 0; )
		{
			if(pDatabase.has(deviants.get(a)) && pDatabase.get(deviants.get(a)).wantsCritiqueOn(artworkIds.get(a)))
			{
				//Date this critique with the time we found it
				Critique thisCritique = new Critique(deviants.get(a), earner.getName(), artworkIds.get(a), critiqueIds.get(a), artworkUrls.get(a), new Date());
				if(earner.submitCritique(thisCritique))
				{
					pDatabase.submitCritique(deviants.get(a), thisCritique);
				}
			}
		}
	}
	
	private static final String URL_TAG = "\" class=\"t\"";
	private static final String URL_START = "href=\"";
	private ArrayList<String> parseArtUrls(String pageSource)
	{
		ArrayList<String> urls = new ArrayList<String>();
		int index = 0;
		while((index = pageSource.indexOf(URL_TAG, index)) != -1)
		{
			urls.add(pageSource.substring(pageSource.lastIndexOf(URL_START, index) + URL_START.length(), index));
			index++;
		}
		return urls;
	}
	
	private static final String ID_TAG = "collect_rid=\"1:";
	private static final String ID_END = "\"";
	
	private ArrayList<Integer> parseArtworkIds(String pageSource)
	{
		ArrayList<Integer> ids = new ArrayList<Integer>();
		int index = 0;
		while((index = pageSource.indexOf(ID_TAG, index)) != -1)
		{
			String artNumber = StringOps.textBetween(pageSource, ID_TAG, ID_END, index);
			ids.add(Integer.parseInt(artNumber));
			index ++;
		}
		return ids;
	}
	
	private static final String VIEW_CRITIQUE_TAG = "\">VIEW CRITIQUE</a>";
	private static final String VIEW_CRITIQUE_START = "/";
	
	private ArrayList<Integer> parseCritiqueIds(String pageSource)
	{
		ArrayList<Integer> ids = new ArrayList<Integer>();
		int index = 0;
		while((index = pageSource.indexOf(VIEW_CRITIQUE_TAG, index)) != -1)
		{
			String artNumber = pageSource.substring(pageSource.lastIndexOf(VIEW_CRITIQUE_START, index) + VIEW_CRITIQUE_START.length(), index);
			ids.add(Integer.parseInt(artNumber));
			index ++;
		}
		return ids;
	}
	
	private static final String USERNAME_TAG = " username=\"";
	private static final String USERNAME_END = "\"";
	
	private ArrayList<String> parseDeviants(String pageSource)
	{
		ArrayList<String> deviants = new ArrayList<String>();
		int index = 0;
		while((index = pageSource.indexOf(USERNAME_TAG, index)) != -1)
		{
			deviants.add(StringOps.textBetween(pageSource, USERNAME_TAG, USERNAME_END, index).toLowerCase());
			index ++;
		}
		return deviants;
	}
}

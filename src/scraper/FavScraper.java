package scraper;

import utils.StringOps;
import macro.Timer;
import data.Currency;
import data.Earner;
import data.Payer;
import data.PayerDatabase;

public class FavScraper extends Thread
{
	private PayerDatabase pDatabase;
	private Earner earner;
	private int offset;
	
	public FavScraper(PayerDatabase pDatabase, Earner earner, int offset)
	{
		this.pDatabase = pDatabase;
		this.earner = earner;
		this.offset = offset;
	}
	@Override
	public void run()
	{
		parseFavs(earner);
	}
	private static final String CHOP_TEXT = "gallery_pager";
	private static final String ID_TAG = "username=\"";
	private static final String END_ID_TAG = "\" ";
	
	private static final String NO_FAVS = "This collection has no items yet!";
	
	private void parseFavs(Earner earner)
	{
		String pageSource = WebScraper.get(earner.getFavsURL(offset));
		String text = chopFavs(pageSource);
			
		if(text.contains(NO_FAVS))
		{
			return;
		}

		int index = 0;
		while((index = text.indexOf(ID_TAG, index)) > -1)
		{
			String artist = StringOps.textBetween(text, ID_TAG, END_ID_TAG, index);
			processFav(artist.toString().toLowerCase(), earner);
			index ++;
		}
	}
	private void processFav(String artist, Earner earner)
	{
		if(pDatabase.has(artist))
		{
			Payer payer = pDatabase.get(artist);
			if(payer.accepts(Currency.FAV))
			{
				System.out.println("Attempting to record Fav from "+earner.getName()+" to "+payer.getName());
				if(earner.addFavFrom(payer))
				{
					pDatabase.hasReceived(artist, Currency.FAV);
				}
			}
		}
	}
	private String chopFavs(String pageSource)
	{
		Timer maxPageSearch = new Timer(5000);
		while(true)
		{
			if(pageSource.indexOf(CHOP_TEXT) != -1)
			{
				pageSource = pageSource.substring(pageSource.indexOf(CHOP_TEXT));
				return pageSource;
			}
			if(maxPageSearch.hasExpired())
			{
				System.err.println("Failed to properly read page source for Favorites");
				System.exit(1);
			}
		}
	}
}

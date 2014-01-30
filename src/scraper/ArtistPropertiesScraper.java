package scraper;

import utils.StringOps;
import data.Artist;
import data.Earner;

public class ArtistPropertiesScraper extends Thread
{
	//Debugging
	public static void main(String[] args)
	{
		//new ArtistPropertiesScraper(new Earner("LudwigGBeilschmidt")).start();
	}
	
	private Artist artist;
	
	public ArtistPropertiesScraper(Artist artist)
	{
		this.artist = artist;
	}
	
	private static final String DONATION_BUTTON = "class=\"smbutton smbutton-red donate disabledbutton\"";
	private static final String DEV_END = " </strong> Deviations</span>";
	private static final String COMMENTS_END = " </strong> Comments</span>";
	private static final String PAGEVIEWS_END = " </strong> Pageviews</span>";
	private static final String DEV_TIME = "<div>Deviant for ";
	private static final String STRONG = "<strong>";
	private static final String CLOSE_DIV = "</div>";
	
	private static final String MONTH = "Month";
	private static final String YEAR = "Year";
	
	private static final int MIN_DEVS = 3;
	private static final int MIN_COMMENTS = 50;
	private static final int MIN_PAGEVIEWS = 100;
	private static final int LOTTA_DEVS = 15;
	
	@Override
	public void run()
	{
		String pageSource = WebScraper.get(artist.getHomeURL());
		lookForDonation(pageSource);
		if(checkDeviations(pageSource))
		{
			return;
		}
		checkComments(pageSource);
		checkPageViews(pageSource);
		checkDeviantTime(pageSource);
	}
	private void checkDeviantTime(String pageSource)
	{
		String timeTag = StringOps.textBetween(pageSource, DEV_TIME, CLOSE_DIV);
		if(!(timeTag.contains(MONTH) || timeTag.contains(YEAR)))
		{
			System.err.println(artist+" is too new");
			artist.markCannotEarn();
		}
	}
	//Returns true if this person has enough deviations to be cleared
	private boolean checkDeviations(String pageSource)
	{
		int index = pageSource.indexOf(DEV_END);
		int numDeviations = 0;
		try
		{
			numDeviations = Integer.parseInt(pageSource.substring(pageSource.lastIndexOf(STRONG, index) + STRONG.length(), index).replaceAll(",",""));
			if(numDeviations < MIN_DEVS)
			{
				System.err.println(artist+" has too few deviations");
				artist.markCannotEarn();
			}
		} 
		catch(StringIndexOutOfBoundsException e) //handle this better, this occurs with 1 deviation (no 's')
		{
			//System.err.println(artist+" has too few deviations");
			//artist.markCannotEarn();
		}
		return numDeviations > LOTTA_DEVS;
	}
	private void checkComments(String pageSource)
	{
		int index = pageSource.indexOf(COMMENTS_END);
		try
		{
			int numComments = Integer.parseInt(pageSource.substring(pageSource.lastIndexOf(STRONG, index) + STRONG.length(), index).replaceAll(",",""));
			if(numComments < MIN_COMMENTS)
			{
				tooFewComments(artist);
			}
		} 
		catch(Exception e)
		{
			//tooFewComments(artist);
		}
	}
	private void tooFewComments(Artist artist)
	{
		System.err.println(artist+" has too few comments");
		artist.markCannotEarn();
	}
	private void checkPageViews(String pageSource)
	{
		try
		{
			int index = pageSource.indexOf(PAGEVIEWS_END);
			int numViews = Integer.parseInt(pageSource.substring(pageSource.lastIndexOf(STRONG, index) + STRONG.length(), index).replaceAll(",",""));
			if(numViews < MIN_PAGEVIEWS)
			{
				tooFewViews(artist);
			}
		}
		catch(Exception e)
		{
			//tooFewViews(artist);
		}
	}
	private void tooFewViews(Artist artist)
	{
		System.err.println(artist+" has too few pageviews");
		artist.markCannotEarn();
	}
	private void lookForDonation(String pageSource)
	{
		if(!pageSource.contains(DONATION_BUTTON))
		{
			System.err.println(artist+" has no donation widget");
			artist.cannotFindDonation();
		}
	}
}

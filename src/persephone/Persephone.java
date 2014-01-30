package persephone;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import scraper.WebScraper;
import utils.StringOps;
import data.Artist;
import driver.DriverPool;
import event.PaymentEvents;
import utils.Write;

public class Persephone 
{
	public static void main(String[] args)
	{
		new Persephone().go();
	}
	private static final int NUM_PERSEPHONES = 5;
	private static final int NUM_GIFTERS = 2;
	private static final int NUM_GIFTS = 50;
	
	private DriverPool driverPool = new DriverPool(NUM_GIFTERS, new Artist("datrade"), "minderbender");
	private PaymentEvents paymentEvents = new PaymentEvents(driverPool, null);
	//private ProxyScraper proxyScraper = new ProxyScraper(new ProxyPool());

	private void go()
	{
		for(int a = NUM_PERSEPHONES ; a --> 0; )
		{
			new PersephoneGifter().start();
		}
	}
	
	private int numGifts = 0;
	private ArrayList<String> giftInfo = new ArrayList<String>();
	private synchronized void addedGift(String info)
	{
		System.out.println(numGifts+" gifts given");
		numGifts++;
		giftInfo.add(info);
		if(numGifts >= NUM_GIFTS)
		{
			Write.toFile(new File("data/gifts.txt"), giftInfo);
		}
	}
	
	class PersephoneGifter extends Thread
	{
		@Override
		public void run()
		{
			Set<String> nameSet = new HashSet<String>();
			while(numGifts < 100)
			{
				String pageSource = WebScraper.get("http://www.deviantart.com/random/deviant", false);
				String name = StringOps.textBetween(pageSource, "<title>", " ");
				System.out.println("Checking "+name);
				if(!nameSet.contains(name))
				{
					parsePage(pageSource);
					nameSet.add(name);
				}
			}
		}
	}
	
	private void parsePage(String pageSource)
	{
		String name = parseAccount(pageSource);
		int pageviews = 0;
		try
		{
			pageviews = Integer.parseInt(parseBackwards(pageSource, " </strong> Pageviews</span>", "<span class=\"tighttt\"><strong>").replace(",", ""));
		} catch(Exception noPageviews) {}
		int comments = 0;
		try
		{
			comments = Integer.parseInt(parseBackwards(pageSource, " </strong> Comments</span>", "<span class=\"tighttt\"><strong>").replace(",", ""));
		} catch(Exception noComments) {}
		
		int deviations = 0;
		try
		{
			deviations = Integer.parseInt(parseBackwards(pageSource, " </strong> Deviations</span>", "<span class=\"tighttt\"><strong>").replace(",", ""));
		} catch(Exception noComments) {}
		int numMonths = parseDuration(pageSource);
		boolean hasPremium = !pageSource.contains("Needs Premium Membership");
		int donationAmt = parseDonationAmt(pageSource);
		if(donationAmt > -1)
		{
			String info = name+":Views "+pageviews+":DonationAmt "+donationAmt+":HasPremium "+hasPremium+":Comments "+comments+":Deviations "+deviations+":Months "+numMonths;
			if(numMonths > 0 && pageviews > 1000 && comments > 100 && deviations > 20 && donationAmt < 500)
			{
				System.out.println(info);
				paymentEvents.addPayment(new Artist(name), 1, "Hope you have a fabulous day! :heart:");
				addedGift(info);
			}
			else
			{
				System.err.println(info);
			}
		}
	}
	private String parseAccount(String pageSource)
	{
		return StringOps.textBetween(pageSource, "<title>", " ");
	}
	private int parseDonationAmt(String pageSource)
	{
		int index = pageSource.indexOf(" <div class=\"cfill ctile cround");
		if(index != -1)
		{
			index = pageSource.indexOf(" / ", index);
			int front = pageSource.lastIndexOf(">", index);
			return Integer.parseInt(pageSource.substring(front + ">".length(), index).replace(",", ""));
		}
		return -1;
	}
	private int parseDuration(String pageSource)
	{
		String tag = "<div>Deviant for ";
		int index = pageSource.indexOf(tag) + tag.length();
		String timeUnit = pageSource.substring(index, index + 10);
		if(timeUnit.contains("Month"))
		{
			return Integer.parseInt(StringOps.textBetween(pageSource, "<div>Deviant for ", "Month").trim());
		}
		else if(timeUnit.contains("Year"))
		{
			return Integer.parseInt(StringOps.textBetween(pageSource, "<div>Deviant for ", "Year").trim()) * 12;
		}
		else return 0;
	}
	private String parseBackwards(String pageSource, String backTag, String frontTag)
	{
		int index = pageSource.indexOf(backTag);
		int front = pageSource.lastIndexOf(frontTag, index);
		return pageSource.substring(front + frontTag.length(), index);
	}
}

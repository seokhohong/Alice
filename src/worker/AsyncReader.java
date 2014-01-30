package worker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import scraper.CommentsScraper;
import scraper.ProfileComment;
import scraper.WebScraper;
import data.Artist;
import driver.DriverPool;
import macro.Macro;

/** Asynchronously reads comments on a page and notifies when the bot needs to move forward */
public class AsyncReader extends Thread
{
	private static final boolean COMMENT = false;
	private static final int REPOLL_GAP = 5000;
	
	private static final Date NONE = null;
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss aa", Locale.US);
	
	private static final String COMMENT_DATE_TAG = "<span class=\"cc-time\"><a title=\"";
	private static final String PAYMENT_DATE_TAG = "<span class=\"entry-count\" title=\"";
	private static final String END_DATE = "\"";

	private Date lastProcessedComment = NONE;
	private Date lastProcessedTransaction = new Date(0);
	
	private Artist artist;
	private DriverPool pool;
	private Object notifyOfComment;
	private Trigger notifyOfTransaction;
	
	private ArrayList<ProfileComment> profileComments = new ArrayList<ProfileComment>();

	//Notify staller when there is a comment, notify the transactionDriver trigger with transaction notifications
	public AsyncReader(Artist artist, DriverPool pool, Object notifyOfComment, Trigger notifyOfTransaction)
	{
		super("AsyncReader");
		this.artist = artist;
		this.pool = pool;
		this.notifyOfComment = notifyOfComment;
		this.notifyOfTransaction = notifyOfTransaction;
	}
	public synchronized ArrayList<ProfileComment> getComments()
	{
		return profileComments;
	}
	public synchronized void setComments(ArrayList<ProfileComment> comments)
	{
		profileComments = comments;
	}
	/** Notifies and updates comments every time new ones are available. Runs asynchronously */
	@Override
	public void run()
	{
		while(true)
		{	
			String pageSource = COMMENT ? WebScraper.get(artist.getHomeURL()) : WebScraper.get(artist.getHomeURL(), false);
			//String pageSource = WebScraper.get("http://datrade.deviantart.com/?offset=80");
			if(COMMENT && pool != null)
			{
				System.out.println("NumDrivers in Pool "+pool.numDrivers()+" Occupation: "+pool.occupationString());
			}
			
			setComments(new CommentsScraper().get(pageSource, COMMENT));
			
			Date latestCommentDate = getMostRecentDate(pageSource, COMMENT_DATE_TAG);
			Date latestTransactionDate = getMostRecentDate(pageSource, PAYMENT_DATE_TAG);
			
			boolean firstRun = (lastProcessedComment == null);
			boolean foundMoreRecentComment = (lastProcessedComment != null && latestCommentDate != null && latestCommentDate.after(lastProcessedComment));
			boolean foundMoreRecentTransaction = (lastProcessedTransaction != null && latestTransactionDate != null && latestTransactionDate.after(lastProcessedTransaction));
			
			if(foundMoreRecentComment || firstRun)
			{
				foundComment(latestCommentDate);
			}
			if(foundMoreRecentTransaction)
			{
				foundTransaction(latestTransactionDate);
			}
			Macro.sleep(REPOLL_GAP);
		}
	}
	private void foundComment(Date latestDate)
	{
		lastProcessedComment = latestDate;
		if(notifyOfComment != null)
		{
			synchronized(notifyOfComment)
			{
				notifyOfComment.notifyAll();
			}
		}	
	}
	private void foundTransaction(Date latestDate)
	{				
		System.out.println("Found Transaction");
		if(notifyOfTransaction != null)
		{
			notifyOfTransaction.trigger();
		}
		lastProcessedTransaction = latestDate;
	}
	/** Returns the most recent date on the pageSource, searching by the tag */
	private Date getMostRecentDate(String pageSource, String tag)
	{
		ArrayList<Date> dates = new ArrayList<Date>();
		try
		{
			addDatesByTag(dates, pageSource, tag);
			Collections.sort(dates);
			Collections.reverse(dates);
		} catch(ParseException ignored) {}
		if(dates.isEmpty())
		{
			return new Date();
		}
		return dates.get(0);
	}
	private void addDatesByTag(ArrayList<Date> dates, String pageSource, String tag) throws ParseException
	{
		int index = 0;
		while((index = pageSource.indexOf(tag, index)) != -1)
		{
			dates.add(parseDate(pageSource.substring(index + tag.length(), pageSource.indexOf(END_DATE, index + tag.length()))));
			index ++;
		}
	}
	private Date parseDate(String dateString) throws ParseException
	{
		return DATE_FORMATTER.parse(dateString);
	}
}

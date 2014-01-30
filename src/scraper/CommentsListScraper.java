package scraper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import data.Comment;
import data.CommentsHistory;
import data.Currency;
import data.Earner;
import data.PayerDatabase;
import data.ToppersDatabase;
import utils.StringOps;
import worker.Multitasker;

public class CommentsListScraper extends Thread
{
	public static void main(String[] args)
	{
		//PayerDatabase pDatabase = new PayerDatabase(null, null, null);
		//new CommentsListScraper(pDatabase, new Earner("animelovers21"), new Multitasker(5)).start();
		//new CommentsListScraper(null, null, null).new EarnerCommentsScraper("http://comments.deviantart.com/1/398100848/3218244099", "bloomingrosexeniia", new Date()).start();
	}
	
	private static final String USER_COMMENTS = "User Comments";
	private static final String URL_TAG = "<span class=\"main\"><a href=\"";
	private static final String URL_END = "\">Comment</a>";
	private static final String BY_TAG = " by ";
	private static final String NAME_TAG = "/\" >";
	private static final String NAME_END = "</a>";
	private static final String TIME_TAG = "<br />on ";
	private static final String TIME_END = "</span>";
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss aa", Locale.US);
	
	private static final CommentsHistory commentsHistory = new CommentsHistory();
	
	private PayerDatabase pDatabase;
	private Earner givingEarner;
	private Multitasker mainVerifier;
	private ToppersDatabase topDatabase;
	
	public CommentsListScraper(PayerDatabase pDatabase, ToppersDatabase topDatabase, Earner givingEarner, Multitasker mainVerifier)
	{
		this.pDatabase = pDatabase;
		this.givingEarner = givingEarner;
		this.mainVerifier = mainVerifier;
		this.topDatabase = topDatabase;
	}
	
	@Override
	public void run()
	{
		String source = WebScraper.get(givingEarner.getActivityURL());
		if(source.indexOf(USER_COMMENTS) == -1)
		{
			givingEarner.activityHidden();
			return;
		}
		source = source.substring(0, source.indexOf(USER_COMMENTS));
		int index = 0;
		while((index = source.indexOf(URL_TAG, index)) != -1)
		{
			String commentUrl = StringOps.textBetween(source, URL_TAG, URL_END, index);
			String receiver = StringOps.textBetween(source, NAME_TAG, NAME_END, source.indexOf(BY_TAG, index)).toLowerCase();
			String time = StringOps.textBetween(source, TIME_TAG, TIME_END, index);
			Date date = new Date(); //now
			try
			{
				date = DATE_FORMATTER.parse(time);
			} 
			catch(ParseException e) 
			{
				e.printStackTrace();
			}
			if(!receiver.equals(givingEarner.getName()) && pDatabase.has(receiver) && pDatabase.get(receiver).accepts(Currency.COMMENT))
			{
				System.out.println("Receiver "+receiver);
				givingEarner.attemptedComments();
				mainVerifier.load(new EarnerCommentsScraper(commentUrl, receiver, date));
			}
			index++;
		}
	}
	class EarnerCommentsScraper extends Thread
	{
		private static final String COMMENT_TEXT_TAG = "<div class=\"text text-ii\">";
		private static final String COMMENT_DETAILS_TAG = "/1/";
		private static final String DETAILS_DELIM = "/";
		
		private static final int MIN_WORD_COUNT = 25; //number of spaces
		private static final int MIN_UNIQUE = 15;
		private static final int MIN_LENGTH = 75;
		
		private String commentUrl;
		private String receiver;
		private Date date;
		
		EarnerCommentsScraper(String commentUrl, String receiver, Date date)
		{
			this.commentUrl = commentUrl;
			this.receiver = receiver;
			this.date = date;
		}
		@Override
		public void run()
		{
			String source = WebScraper.get(commentUrl);
			String comment = extractComment(source);
			//String comment = StringOps.textBetween(source, COMMENT_TEXT_TAG, COMMENT_END);
			comment = killHtml(comment).toLowerCase();
			int numWords = getCharFreq(comment, ' ') + 1;
			int numAlphabetChars = uniqueAlphabet(comment);
			int numCharsTotal = comment.length();
			System.out.println("Comment "+comment);
			/*
			System.out.println("NumWords "+numWords);
			System.out.println("NumAlphabet "+numAlphabetChars);
			System.out.println("NumCharsTotal "+numCharsTotal);
			*/
			if(numWords >= MIN_WORD_COUNT &&
					numAlphabetChars >= MIN_UNIQUE &&
					numCharsTotal >= MIN_LENGTH)
			{
				acceptComment(comment, date);
			}
			else
			{
				rejectComment(comment, date);
			}
		}
		private static final String DIV_TAG = "<div>";
		private static final String END_DIV_TAG = "</div>";
		private String extractComment(String pageSource)
		{
			int startIndex = pageSource.indexOf(COMMENT_TEXT_TAG) + COMMENT_TEXT_TAG.length();
			int divDepth = 1;
			for(int a = startIndex; a < pageSource.length(); a++)
			{	
				if(pageSource.indexOf(DIV_TAG, a) == a)
				{
					divDepth++;
				}
				if(divDepth == 0)
				{
					return pageSource.substring(startIndex, a).replace(DIV_TAG, "").replace(END_DIV_TAG, "");
				}
				if(pageSource.indexOf(END_DIV_TAG, a) == a)
				{
					divDepth --;
				}
			}
			return "";
		}
		private void acceptComment(String commentString, Date date)
		{
			Comment comment = new Comment(receiver, givingEarner.getName(), getArtworkId(commentUrl), commentString, date.getTime());
			if(givingEarner.addComment(comment, commentsHistory))
			{
				topDatabase.addComment(comment);
				pDatabase.hasReceived(receiver, Currency.COMMENT);
			}
		}
		private void rejectComment(String commentString, Date date)
		{
			Comment comment = new Comment(receiver, givingEarner.getName(), getArtworkId(commentUrl), commentString, date.getTime());
			givingEarner.rejectComment(comment);
		}
		private int getArtworkId(String commentUrl)
		{
			//#/# formatting at the end of url
			return Integer.parseInt(StringOps.textBetween(commentUrl, COMMENT_DETAILS_TAG, DETAILS_DELIM));
		}
		private int getCharFreq(String comment, char c)
		{
			int numChars = 0;
			for(int a = 0; a < comment.length(); a++)
			{
				if(comment.charAt(a) == c)
				{
					numChars ++;
				}
			}
			return numChars;
		}
		//Number of unique chars in string
		public int uniqueAlphabet(String comment)
		{
			Set<Character> chars = new HashSet<Character>();
			for(int a = comment.length(); a --> 0; )
			{
				if(Character.isAlphabetic(comment.charAt(a)))
				{
					chars.add(comment.charAt(a));
				}
			}
			return chars.size();
		}
		private static final String A_TAG = "<a ";
		private static final String A_CLOSE = "</a>";
		private static final String IMG_TAG = "<img src";
		private static final String IMG_CLOSE = "\"/>";
		public String killHtml(String comment)
		{
			StringBuilder builder = new StringBuilder();
			int bracketDepth = 0;
			for(int a = 0; a < comment.length(); a++)
			{	
				if(comment.indexOf(IMG_TAG, a) == a || comment.indexOf(A_TAG, a) == a)
				{
					bracketDepth++;
				}
				if(bracketDepth == 0)
				{
					builder.append(comment.charAt(a));
				}
				if(comment.indexOf(IMG_CLOSE, a) == a || comment.indexOf(A_CLOSE, a) == a)
				{
					a += IMG_CLOSE.length();
					bracketDepth--;
				}
			}
			return builder.toString().trim();
		}
	}
}

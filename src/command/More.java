package command;

import jane.HistoryDatabase;

import java.util.HashMap;
import java.util.Map;

import scraper.ProfileComment;
import data.Currency;
import data.Earner;
import data.EarnerDatabase;
import data.Payer;
import data.PayerDatabase;
import data.Group;
import data.GroupDatabase;
import event.Reply;
import event.ReplyEvents;

public class More extends AliceCommand 
{
	private static final int NUM_INFO_ALL = 10; //NUM_INFO_ALL number of links per category if all is specified
	private static final int NUM_INFO_SPECIFIC = 30; //If specifically specified, this number of links
	
	private ReplyEvents replyEvents;
	private PayerDatabase pDatabase;
	private EarnerDatabase eDatabase;
	private HistoryDatabase hDatabase;
	private GroupDatabase gDatabase;
	
	public More(ProfileComment profileComment, ReplyEvents replyEvents, PayerDatabase pDatabase, EarnerDatabase eDatabase, HistoryDatabase hDatabase, GroupDatabase gDatabase) 
	{
		super(profileComment);
		this.replyEvents = replyEvents;
		this.pDatabase = pDatabase;
		this.eDatabase = eDatabase;
		this.hDatabase = hDatabase;
		this.gDatabase = gDatabase;
	}
	
	public static boolean matchesTag(String message)
	{
		return message.toLowerCase().contains("#more");
	}

	@Override
	public void execute()
	{
		Earner earner = eDatabase.get(getArtistName());
		replyEvents.addReply(new Reply(getCommentUrl(), getWorkInfo(earner)));
	}
	private String getWorkInfo(Earner earner)
	{
		StringBuilder comment = new StringBuilder();
		boolean wantsAll = true;
		Map<Currency, Boolean> wantsSpecific = new HashMap<Currency, Boolean>();
		for(Currency c : Currency.values())
		{
			for(String param : getParams())
			{
				if(!param.trim().equals(""))
				{
					boolean contains = param.contains(c.getCommand());
					if(contains)
					{
						wantsAll = false;
					}
					wantsSpecific.put(c, contains);
				}
			}
		}
		if(wantsAll)
		{
			informAll(earner, comment);
		}
		else
		{
			if(wantsSpecific.get(Currency.WATCH)) informWatches(earner, comment, NUM_INFO_SPECIFIC);
			if(wantsSpecific.get(Currency.LLAMA)) informLlamas(earner, comment, NUM_INFO_SPECIFIC);
			if(wantsSpecific.get(Currency.FAV)) informFavs(earner, comment, NUM_INFO_SPECIFIC);
			if(wantsSpecific.get(Currency.CRITIQUE)) informCritiques(earner, comment, NUM_INFO_SPECIFIC);
			if(wantsSpecific.get(Currency.ENROLLMENT)) informEnrollment(earner, comment, NUM_INFO_SPECIFIC);
			if(wantsSpecific.get(Currency.COMMENT)) informComments(earner, comment, NUM_INFO_SPECIFIC);
		}
		comment.append("\n");
		comment.append(":note:TIP: Use Control+Click to open multiple tabs at once for faster earning.\n\n");
		comment.append("Please note that Alice did not check work! If you wish to receive credit for anything you have done, please leave a new comment.");
		return comment.toString();
	}
	/** Returns the work still available for this particular earner */
	private void informAll(Earner earner, StringBuilder comment)
	{
		informWatches(earner, comment, NUM_INFO_ALL);
		informLlamas(earner, comment, NUM_INFO_ALL);
		informFavs(earner, comment, NUM_INFO_ALL);
		informCritiques(earner, comment, NUM_INFO_ALL);
		informEnrollment(earner, comment, NUM_INFO_ALL);
		informComments(earner, comment, NUM_INFO_ALL);
	}
	private void informWatches(Earner earner, StringBuilder comment, int numEntries)
	{
		StringBuilder tempInfo = new StringBuilder();
		tempInfo.append("\nRemaining Watches:\n");
		int numLinks = 0;
		for(Payer payer : pDatabase.sortedPayers())
		{
			if(payer.accepts(Currency.WATCH) && !earner.watches(payer.getName()))
			{
				if(!hDatabase.transactionExists(Currency.WATCH, payer.getName(), earner.getName()) && !payer.getName().equals(earner.getName()))
				{
					tempInfo.append(payer.watchLink(payer.getName())+" ");
					numLinks ++;
					if(numLinks == numEntries) break;
				}
			}
		}
		for(Group group : gDatabase.sortedGroups())
		{
			if(group.accepts(Currency.WATCH) && !earner.watches(group.getName()))
			{
				tempInfo.append(group.getLink(group.getName()) + " ");
				numLinks++;
				if(numLinks == numEntries) break;
			}
		}
		tempInfo.append("\n");
		if(numLinks == 0)
		{
			comment.append("\nYou've Watched Everyone!\n");
		}
		else
		{
			comment.append(tempInfo);
		}
	}
	private void informLlamas(Earner earner, StringBuilder comment, int numEntries)
	{
		StringBuilder tempInfo = new StringBuilder();
		tempInfo.append("\nRemaining Llamas:\n");
		int numLinks = 0;
		for(Payer payer : pDatabase.sortedPayers())
		{
			if(payer.accepts(Currency.LLAMA) && !earner.llamaed(payer.getName()))
			{
				if(!hDatabase.transactionExists(Currency.LLAMA, payer.getName(), earner.getName()) && !payer.getName().equals(earner.getName()))
				{
					tempInfo.append(payer.llamaLink(payer.getName())+" ");
					numLinks ++;
					if(numLinks == numEntries) break;
				}
			}
		}
		tempInfo.append("\n");
		if(numLinks == 0)
		{
			comment.append("\nYou've Llama'd Everyone!\n");
		}
		else
		{
			comment.append(tempInfo);
		}
	}
	private void informEnrollment(Earner earner, StringBuilder comment, int numEntries)
	{
		StringBuilder tempInfo = new StringBuilder();
		tempInfo.append("\nRemaining Groups:\n");
		int numLinks = 0;
		for(Group group : gDatabase.sortedGroups())
		{
			if(group.accepts(Currency.ENROLLMENT) && !earner.enrolled(group.getName()))
			{
				tempInfo.append(group.getLink(group.getName())+" ");
				numLinks ++;
				if(numLinks == numEntries) break;
			}
		}
		tempInfo.append("\n");
		if(numLinks == 0)
		{
			comment.append("\nYou've Enrolled in all Groups!\n");
		}
		else
		{
			comment.append(tempInfo);
		}
	}
	private void informFavs(Earner earner, StringBuilder comment, int numEntries)
	{
		StringBuilder tempInfo = new StringBuilder();
		tempInfo.append("\nRemaining Favs:\n");
		int numLinks = 0;
		for(Payer payer : pDatabase.sortedPayers())
		{
			int numFavs = earner.numFavsOn(payer.getName());
			if(payer.accepts(Currency.FAV) && numFavs < Earner.getMaxFavs())
			{
				tempInfo.append(payer.favLink(payer.getName())+"("+ Integer.toString(Earner.getMaxFavs() - numFavs)+") ");
				numLinks ++;
				if(numLinks == numEntries) break;
			}
		}
		tempInfo.append("\n");
		if(numLinks == 0)
		{
			comment.append("\nYou've Fav'd Everyone!\n");
		}
		else
		{
			comment.append(tempInfo);
		}
	}
	private void informComments(Earner earner, StringBuilder comment, int numEntries)
	{
		StringBuilder tempInfo = new StringBuilder();
		tempInfo.append("\nGive Comments To:\n");
		int numLinks = 0;
		for(Payer payer : pDatabase.sortedPayers())
		{
			if(payer.accepts(Currency.COMMENT))
			{
				tempInfo.append(payer.favLink(payer.getName())+" ");
				numLinks ++;
				if(numLinks == numEntries) break;
			}
		}
		tempInfo.append("\n");
		if(numLinks == 0)
		{
			comment.append("\nNobody wants Comments!\n");
		}
		else
		{
			comment.append(tempInfo);
		}
	}
	private void informCritiques(Earner earner, StringBuilder comment, int numEntries)
	{
		StringBuilder tempInfo = new StringBuilder();
		tempInfo.append("\nRemaining Critiques:\n");
		int numLinks = 0;
		enoughLinks:
		for(Payer payer : pDatabase.sortedPayers())
		{
			if(payer.accepts(Currency.CRITIQUE) && !payer.getArtworks().isEmpty() && !payer.getName().equals(earner.getName()))
			{
				int numAvailable = 0;
				for(String artwork : payer.getArtworks())
				{
					//Emergency check
					if(artwork.length() > 9)
					{
						artwork = artwork.substring(0, 9);
					}
					if(!earner.hasCritiqued(artworkToID(artwork)))
					{
						numAvailable ++;
					}
				}
				if(numAvailable > 0)
				{
					tempInfo.append("<a href=\"http://datrade.deviantart.com/#843371291\" target=\"_blank\">"+payer.getName()+"</a>("+numAvailable+") ");
					numLinks++;
				}
				if(numLinks == numEntries) break enoughLinks;
			}
		}
		tempInfo.append("\n");
		if(numLinks == 0)
		{
			comment.append("\nYou've Critiqued Everyone!\n");
		}
		else
		{
			comment.append(tempInfo);
		}
	}
	//Turn :thumb#: into #
	private int artworkToID(String artwork)
	{
		return Integer.parseInt(artwork.substring(":thumb".length(), artwork.length() - 1));
	}
}

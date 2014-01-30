package command;

import jane.HistoryDatabase;
import scraper.ProfileComment;
import data.EarnerDatabase;
import data.PayerDatabase;
import data.GroupDatabase;
import event.ReplyEvents;

public class CommandFactory 
{
	private ReplyEvents replyEvents;
	private PayerDatabase pDatabase;
	private EarnerDatabase eDatabase;
	private HistoryDatabase hDatabase;
	private GroupDatabase gDatabase;
	
	//Takes all parameters necessary for all commands
	public CommandFactory(
			ReplyEvents replyEvents, 
			PayerDatabase pDatabase, 
			EarnerDatabase eDatabase,
			HistoryDatabase hDatabase,
			GroupDatabase gDatabase)
	{
		this.replyEvents = replyEvents;
		this.pDatabase = pDatabase;
		this.eDatabase = eDatabase;
		this.hDatabase = hDatabase;
		this.gDatabase = gDatabase;
	}
	
	/** Takes a raw message (do not lowercase) */
	public AliceCommand parse(ProfileComment profileComment)
	{
		String message = profileComment.getMessage();
		if(More.matchesTag(message))
		{
			return new More(profileComment, replyEvents, pDatabase, eDatabase, hDatabase, gDatabase);
		}
		else if(Stats.matchesTag(message))
		{
			return new Stats(profileComment, replyEvents, pDatabase, eDatabase);
		}
		else if(Refund.matchesTag(message))
		{
			return new Refund(profileComment, replyEvents, pDatabase);
		}
		/*else if(FavLink.matchesTag(message))
		{
			return new FavLink(profileComment, replyEvents, pDatabase);
		}*/
		else if(Prefs.matchesTag(message))
		{
			return new Prefs(profileComment, replyEvents, pDatabase);
		}
		else if(CommentsOn.matchesTag(message))
		{
			return new CommentsOn(profileComment, eDatabase);
		}
		else if(CommentsOff.matchesTag(message))
		{
			return new CommentsOff(profileComment, eDatabase);
		}
		return new AcceptEarner(profileComment, eDatabase);
	}
}

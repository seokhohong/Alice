package command;

import scraper.ProfileComment;
import data.EarnerDatabase;
import data.PayerDatabase;
import event.Reply;
import event.ReplyEvents;

public class Stats extends AliceCommand 
{
	private ReplyEvents replyEvents;
	private PayerDatabase pDatabase;
	private EarnerDatabase eDatabase;
	
	public Stats(ProfileComment profileComment, ReplyEvents replyEvents, PayerDatabase pDatabase, EarnerDatabase eDatabase) 
	{
		super(profileComment);
		this.replyEvents = replyEvents;
		this.pDatabase = pDatabase;
		this.eDatabase = eDatabase;
	}
	
	public static boolean matchesTag(String message)
	{
		return message.toLowerCase().contains("#stats");
	}

	@Override
	public void execute() 
	{
		replyEvents.addReply(new Reply(getCommentUrl(), compileStats()));
	}
	private String compileStats()
	{
		StringBuilder builder = new StringBuilder();
		if(pDatabase.has(getArtistName()))
		{
			builder.append(pDatabase.get(getArtistName()).getStats());
		}
		builder.append("\n\n");
		builder.append(eDatabase.get(getArtistName()).getStats());
		return builder.toString();
	}
}

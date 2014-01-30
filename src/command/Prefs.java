package command;

import scraper.ProfileComment;
import data.Payer;
import data.PayerDatabase;
import event.Reply;
import event.ReplyEvents;

public class Prefs extends AliceCommand
{
	private ReplyEvents replyEvents;
	private PayerDatabase pDatabase;
	
	public Prefs(ProfileComment profileComment, ReplyEvents replyEvents, PayerDatabase pDatabase) 
	{
		super(profileComment);
		this.replyEvents = replyEvents;
		this.pDatabase = pDatabase;
	}
	
	public static boolean matchesTag(String message)
	{
		return message.toLowerCase().contains("#prefs");
	}

	@Override
	public void execute() 
	{
		if(pDatabase.has(getArtistName()))
		{
			Payer payer = pDatabase.get(getArtistName());
			payer.setPreferences(concatenateParams());
			payer.updateArtwork(concatenateParams());
			replyEvents.addReply(new Reply(getCommentUrl(), buildAcceptanceList(payer)));
		}
		else
		{
			replyEvents.addReply(new Reply(getCommentUrl(), "You don't have any points with Alice!"));
		}
	}
	private String buildAcceptanceList(Payer payer)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("You now accept: ");
		builder.append(payer.acceptanceList());
		builder.append("!");
		return builder.toString();
	}
	private String concatenateParams()
	{
		StringBuilder builder = new StringBuilder();
		for(String param : getParams())
		{
			builder.append(param);
		}
		return builder.toString();
	}
}

package command;

import scraper.ProfileComment;
import data.Currency;
import data.Payer;
import data.PayerDatabase;
import event.Reply;
import event.ReplyEvents;

public class FavLink extends AliceCommand 
{
	private static final String RESET = "reset";
	
	private ReplyEvents replyEvents;
	private PayerDatabase pDatabase;
	
	public FavLink(ProfileComment profileComment, ReplyEvents replyEvents, PayerDatabase pDatabase) 
	{
		super(profileComment);
		this.replyEvents = replyEvents;
		this.pDatabase = pDatabase;
	}
	
	public static boolean matchesTag(String message)
	{
		return message.toLowerCase().contains("#favlink");
	}

	@Override
	public void execute() 
	{
		if(pDatabase.has(getArtistName()))
		{
			Payer favLinkPayer = pDatabase.get(getArtistName());
			if(favLinkPayer.accepts(Currency.FAV))
			{
				if(getParams().size() >= 1)
				{
					if(getParams().get(0).toLowerCase().contains(RESET))
					{
						favLinkPayer.setFavLink(null);
						replyEvents.addReply(new Reply(getCommentUrl(), "Favorites link reset to default."));
					}
					else
					{
						favLinkPayer.setFavLink(getParams().get(1));
						replyEvents.addReply(new Reply(getCommentUrl(), "Favorites link set to "+getParams().get(1)));
					}
				}
				else
				{
					replyEvents.addReply(new Reply(getCommentUrl(), "Incorrect Parameters"));
				}
			}
			else
			{
				replyEvents.addReply(new Reply(getCommentUrl(), "You don't accept Favs!"));
			}
		}
		else
		{
			replyEvents.addReply(new Reply(getCommentUrl(), "You don't have any points with Alice!"));
		}
	}
}

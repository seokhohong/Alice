package command;

import scraper.ProfileComment;
import data.Payer;
import data.PayerDatabase;
import event.Reply;
import event.ReplyEvents;

public class Refund extends AliceCommand 
{
	private ReplyEvents replyEvents;
	private PayerDatabase pDatabase;
	
	public Refund(ProfileComment profileComment, ReplyEvents replyEvents, PayerDatabase pDatabase) 
	{
		super(profileComment);
		this.replyEvents = replyEvents;
		this.pDatabase = pDatabase;
	}
	
	public static boolean matchesTag(String message)
	{
		return message.toLowerCase().contains("#refund");
	}

	@Override
	public void execute() 
	{
		if(pDatabase.has(getArtistName()))
		{
			Payer refundingPayer = pDatabase.get(getArtistName());
			refundingPayer.giveRefund(); //for replying to the comment, not necessarily point redemption
			replyEvents.addReply(new Reply(getCommentUrl(), refundingPayer.getRefundReply()));
			pDatabase.refund(refundingPayer);
		}
		else
		{
			replyEvents.addReply(new Reply(getCommentUrl(), "You don't have any points with Alice!"));
		}
	}
}

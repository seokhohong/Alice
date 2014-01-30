package worker;

import java.util.Collection;

import scraper.EarnerVerifier;
import scraper.ProfileComment;
import data.Earner;
import data.EarnerDatabase;
import data.GroupDatabase;
import data.PayerDatabase;
import data.ToppersDatabase;
import event.Reply;
import event.ReplyEvents;

public class EarnerWorkChecker
{
	private ReplyEvents replyEvents;
	private PayerDatabase pDatabase;
	private EarnerDatabase eDatabase;
	private GroupDatabase gDatabase;
	private ToppersDatabase topDatabase;
	
	public EarnerWorkChecker(ReplyEvents replyEvents, PayerDatabase pDatabase, EarnerDatabase eDatabase, GroupDatabase gDatabase, ToppersDatabase topDatabase)
	{
		this.replyEvents = replyEvents;
		this.pDatabase = pDatabase;
		this.eDatabase = eDatabase;
		this.gDatabase = gDatabase;
		this.topDatabase = topDatabase;
	}

	public synchronized void run(Collection<ProfileComment> earnerComments)
	{
		new Worker(earnerComments).run();
	}
	
	class Worker extends Thread
	{
		private Collection<ProfileComment> earnerComments;
		
		Worker(Collection<ProfileComment> earnerComments)
		{
			this.earnerComments = earnerComments;
		}
		@Override
		public void run()
		{
			doWork(earnerComments);
		}
	}

	private void doWork(Collection<ProfileComment> earnerComments)
	{
		synchronized(eDatabase.getEarners()) //make earners are taken out or added in (individual earners can be modified)
		{
			new EarnerVerifier(pDatabase, eDatabase, gDatabase, topDatabase); //do earner work
			for(ProfileComment earnerComment : earnerComments)
			{
				assert(eDatabase.has(earnerComment.getArtistName()));
				Earner earner = eDatabase.get(earnerComment.getArtistName());
				replyEvents.addReply(new Reply(earnerComment.getUrl(), earner.getReplyMessage(earnerComment.getCapitalizedName())));
			}
			earnerComments.clear();
			eDatabase.payEarners();
		}
	}
}

package worker;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import command.CommandFactory;
import data.EarnerDatabase;
import data.GroupDatabase;
import data.PayerDatabase;
import data.ToppersDatabase;
import event.ReplyEvents;
import scraper.ProfileComment;

public class CommentsManager 
{
	private static final int RETRY_COMMENT = 1000 * 45; //after this number of millis, clear a comment to be tried again.
	private CommandFactory commandFactory;
	private EarnerWorkChecker workChecker;
	
	private Map<ProfileComment, Long> alreadyRead = new HashMap<ProfileComment, Long>();
	
	public CommentsManager(
			ReplyEvents replyEvents, 
			PayerDatabase pDatabase, 
			EarnerDatabase eDatabase, 
			GroupDatabase gDatabase,
			ToppersDatabase topDatabase,
			CommandFactory commandFactory) throws IOException, ParseException
	{
		this.commandFactory = commandFactory;
		workChecker = new EarnerWorkChecker(replyEvents, pDatabase, eDatabase, gDatabase, topDatabase);
	}
	public synchronized void work(ArrayList<ProfileComment> comments) throws IOException, ParseException
	{
		//to make sure we don't launch two responder threads to one comment
		comments.removeAll(alreadyRead.keySet());
		//updateAlreadyRead();
		if(!comments.isEmpty())
		{
			for(ProfileComment comment : comments)
			{
				System.out.println("Began Execution of "+comment);
				alreadyRead.put(comment, System.currentTimeMillis());
				commandFactory.parse(comment).execute();
			}
			workChecker.run(comments);
		}
		updateAlreadyRead();
	}
	private synchronized void updateAlreadyRead()
	{
		Iterator<ProfileComment> iter = alreadyRead.keySet().iterator();
		while(iter.hasNext())
		{
			ProfileComment thisComment = iter.next();
			if(System.currentTimeMillis() - alreadyRead.get(thisComment) > RETRY_COMMENT)
			{
				System.out.println("Opening up Comment: "+thisComment);
				iter.remove();
			}
		}
		if(!alreadyRead.isEmpty())
		{
			System.out.println("Comments waiting to be opened "+alreadyRead.size());
		}
	}
}

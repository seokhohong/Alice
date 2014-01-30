package command;

import scraper.ProfileComment;
import data.Currency;
import data.EarnerDatabase;

public class CommentsOn extends AliceCommand 
{
	private EarnerDatabase eDatabase;
	
	public CommentsOn(ProfileComment profileComment, EarnerDatabase eDatabase) 
	{
		super(profileComment);
		this.eDatabase = eDatabase;
	}
	
	public static boolean matchesTag(String message)
	{
		return message.toLowerCase().contains("#commentsoff");
	}

	@Override
	public void execute() 
	{
		eDatabase.get(getArtistName()).setPreferences(Currency.COMMENT, false);
	}
}

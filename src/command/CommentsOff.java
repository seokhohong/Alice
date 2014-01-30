package command;

import scraper.ProfileComment;
import data.Currency;
import data.EarnerDatabase;

public class CommentsOff extends AliceCommand 
{
	private EarnerDatabase eDatabase;
	
	public CommentsOff(ProfileComment profileComment, EarnerDatabase eDatabase) 
	{
		super(profileComment);
		this.eDatabase = eDatabase;
	}
	
	public static boolean matchesTag(String message)
	{
		return message.toLowerCase().contains("#commentson");
	}

	@Override
	public void execute() 
	{
		eDatabase.get(getArtistName()).setPreferences(Currency.COMMENT, true);
	}
}

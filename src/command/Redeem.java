package command;

import scraper.ProfileComment;
import data.EarnerDatabase;

public class Redeem extends AliceCommand 
{
	private EarnerDatabase eDatabase;
	
	public Redeem(ProfileComment profileComment, EarnerDatabase eDatabase) 
	{
		super(profileComment);
		this.eDatabase = eDatabase;
	}
	
	public static boolean matchesTag(String message)
	{
		return message.toLowerCase().contains("#redeem");
	}

	@Override
	public void execute() 
	{
		//eDatabase.redeem(getArtistName());
	}
}

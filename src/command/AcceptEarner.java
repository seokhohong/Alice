package command;

import scraper.ProfileComment;
import data.EarnerDatabase;

public class AcceptEarner extends AliceCommand 
{
	private EarnerDatabase eDatabase;
	
	public AcceptEarner(ProfileComment profileComment, EarnerDatabase eDatabase) 
	{
		super(profileComment);
		this.eDatabase = eDatabase;
	}

	@Override
	public void execute() 
	{
		eDatabase.add(getArtistName());
	}
}

package command;

import java.util.ArrayList;

import scraper.ProfileComment;

public abstract class AliceCommand 
{	
	private static final String CMD_TAG = "#";
	private static final String PARAM_DELIM = " ";
	
	private ArrayList<String> params;		protected ArrayList<String> getParams() { return params; }
	private String commentUrl;				protected String getCommentUrl() { return commentUrl; }
	private String artistName;				protected String getArtistName() { return artistName; }
	
	public AliceCommand(ProfileComment profileComment)
	{
		this.params = getParams(profileComment.getMessage());
		this.commentUrl = profileComment.getUrl();
		this.artistName = profileComment.getArtistName();
	}
	private static ArrayList<String> getParams(String message)
	{
		String[] params = message.split(PARAM_DELIM);
		ArrayList<String> paramList = new ArrayList<String>(); 
		for(String param : params)
		{
			if(!param.isEmpty() && !param.contains(CMD_TAG))
			{
				paramList.add(param);
			}
		}
		return paramList;
	}
	public abstract void execute();

}

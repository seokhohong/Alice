package scraper;

public class ProfileComment 
{
	private String artistName;		public String getArtistName() { return artistName; }
	private String commentUrl;		public String getUrl() { return commentUrl; }
	private String message;			public String getMessage() { return message; }
	private String capitalizedName;	public String getCapitalizedName() { return capitalizedName; }
	
	ProfileComment(String capitalizedName, String message, String commentUrl)
	{
		this.artistName = capitalizedName.toLowerCase();
		this.message = message.toLowerCase();
		this.commentUrl = commentUrl;
		this.capitalizedName = capitalizedName;
	}
	@Override
	public String toString()
	{
		return artistName+" "+message+" @ "+commentUrl;
	}
	@Override
	public int hashCode()
	{
		return commentUrl.hashCode();
	}
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof ProfileComment)
		{
			ProfileComment otherComment = (ProfileComment) o;
			return commentUrl.equals(otherComment.commentUrl);
		}
		return false;
	}
}

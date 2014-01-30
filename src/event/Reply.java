package event;

public class Reply 
{
	private String message;		public String getMessage() { return message; }
	private String url;			public String getUrl() { return url; }
	
	public Reply(String url, String message)
	{
		this.message = message;
		this.url = url;
	}
	@Override
	public String toString()
	{
		return message+" at "+url;
	}
	@Override
	public int hashCode()
	{
		return url.hashCode();
	}
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Reply)
		{
			Reply otherReply = (Reply) o;
			return otherReply.url.equals(url);
		}
		return false;
	}
}

package data;

import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;

/** Data about a Comment, gathered from a profile's activity list */
public class Comment
{
	private static final String DELIM = "`";
	private static final int TOO_SIMILAR = 10;
	
	private String receiver;			public String getReceiver() { return receiver; }
	private String commenter;			public String getCommenter() { return commenter; }
	private int artworkId;				public int getArtworkId() { return artworkId; }
	private String comment;				public String getMessage()  { return comment; }
	private long time;
	
	public Comment(String receiver, String commenter, int artworkId, String comment, long time)
	{
		this.receiver = receiver;
		this.commenter = commenter;
		this.artworkId = artworkId;
		this.comment = comment;
		this.time = time;
	}
	
	public static Comment parse(String toParse) throws ParseException
	{
		String[] split = toParse.split(DELIM);
		return new Comment(split[0], split[1], Integer.parseInt(split[2]), split[3], Long.parseLong(split[4]));
	}
	/** Uses Levenshtein edit distance to determine whether one comment is too similar to another */
	public boolean tooSimilar(Comment otherComment)
	{
		return StringUtils.getLevenshteinDistance(this.comment, otherComment.comment) < TOO_SIMILAR;
	}
	public long timeDifference(Comment otherComment)
	{
		return Math.abs(time - otherComment.time);
	}
	
	@Override
	public String toString()
	{
		return receiver + DELIM + commenter + DELIM + artworkId + DELIM + comment + DELIM + time;
	}
	
}

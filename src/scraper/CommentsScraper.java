package scraper;

import java.io.File;
import java.util.ArrayList;

import utils.StringOps;
import utils.Write;

public class CommentsScraper 
{
	public static void main(String[] args)
	{
		Write.toFile(new File("commentsData.txt"), WebScraper.get("http://datrade.deviantart.com/"));
		for(ProfileComment comment : new CommentsScraper().get(WebScraper.get("http://datrade.deviantart.com/"), true))
		{
			System.out.println(comment);
		}
	}
	private static final String NAME_TAG = ".deviantart.com/\" >";
	private static final String NAME_TAG_END = "</a><span class=\"user-symbol";
	//private static final String NAME_TAG_FIRST = "<span class=\"cc-name\">";
	//private static final int GAP = 1;
	//private static final String NAME_TAG_SECOND = "<a class=\"u\" href=\"http://";
	private static final String COMMENT_TAG = "<div class=\"text text-ii\">";
	private static final String COMMENT_END = "</div>";
	private static final String COMMENTS_URL_PREFIX = "http://comments.deviantart.com/4/";
	private static final String START_COMMENTS = "<span class=\"cclabel\">Add a Comment:</span>";
	
	private boolean nonSelective = false;
	
	public CommentsScraper()
	{

	}
	/** If true, reads comments that have been replied too */
	public CommentsScraper(boolean nonSelective)
	{
		this.nonSelective = nonSelective;
	}
	public ArrayList<ProfileComment> get(String pageSource, boolean comment)
	{
		ArrayList<ProfileComment> comments = new ArrayList<ProfileComment>();
		parse(pageSource.substring(pageSource.indexOf(START_COMMENTS)), comments);
		if(comment)
		{
			System.out.println("Found "+comments.size() + " unreplied comments");
		}
		return comments;
	}
	private int getCloseDiv(String pageSource, int startIndex)
	{
		int index = startIndex;
		int divDepth = 0;
		while(index < pageSource.length())
		{
			int nextOpenDiv = pageSource.indexOf("<div ", index);
			int nextCloseDiv = pageSource.indexOf("</div>", index);
			if(nextOpenDiv < nextCloseDiv)
			{
				divDepth ++;
				index = nextOpenDiv + 1; 
			}
			else
			{
				divDepth --;
				index = nextCloseDiv + 1;
			}
			//Complete the open and closing dividers for the nest
			if(divDepth < 0)
			{
				return index;
			}
		}
		//Bad stuff
		return -1;
	}
	private void parse(String pageSource, ArrayList<ProfileComment> comments)
	{
		ProfileComment waiting = null;
		int index = 0;
		while(true)
		{
			int nextNest = pageSource.indexOf("nest", index);
			index = pageSource.indexOf(NAME_TAG, index);
			if(index == -1) //no more comments
			{
				if(waiting != null)
				{
					//The last comment will not appear to have a reply from the profile page, thus Alice will try to respond to it.
					//Enable if backlogged.
					//comments.add(waiting); //so add it to comments to be replied to
				}
				break;
			}
			//there's a reply so skip it, or there are no nests, in which case all comments are up for replying
			if(index > nextNest && nextNest != -1 && !nonSelective) 
			{
				index = getCloseDiv(pageSource, nextNest);
				if(index == -1)
				{
					break;
				}
				waiting = null; //and clear out waiter
				continue;
			}
			else //non-nest comment found
			{
				if(waiting != null)
				{
					comments.add(waiting); //so add it to comments to be replied to
				}
			}
			//found a comment
			//if(pageSource.indexOf(NAME_TAG, index) < index + NAME_TAG_END.length() + 30)
			{
				String capitalizedName = StringOps.textBetween(pageSource, NAME_TAG, NAME_TAG_END, index);
				String message = StringOps.textBetween(pageSource, COMMENT_TAG, COMMENT_END, index);
				String url = COMMENTS_URL_PREFIX+StringOps.textBetween(pageSource, "href=\""+COMMENTS_URL_PREFIX, "\">", index);
				waiting = new ProfileComment(capitalizedName, message, url);
			}
			index++;
		}
	}
}

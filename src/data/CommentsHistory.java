package data;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;

import utils.Read;
import utils.Write;

/** Records every comment that goes through Alice. Not designed well since it's probably temporary */
public class CommentsHistory 
{
	private static final File COMMENTS_HISTORY_FILE = new File("data//commentsHistory.txt");	//raw comments log
	private static final File COMMENTS_EVENTS_FILE = new File("data//commentsEvents.txt");		//notable events with the comments system
	
	private ArrayList<Comment> history = new ArrayList<Comment>();
	private ArrayList<String> events = new ArrayList<String>(); 
	
	public CommentsHistory()
	{
		//load();
	}
	public void addComment(Comment comment)
	{
		history.add(comment);
		save(); //for now
	}
	public void addEvent(String event)
	{
		events.add(event);
		save();
	}
	private void load()
	{
		events = Read.fromFile(COMMENTS_EVENTS_FILE);
		ArrayList<String> comments = Read.fromFile(COMMENTS_HISTORY_FILE);
		for(String comment : comments)
		{
			try
			{
				history.add(Comment.parse(comment));
			} catch(ParseException ignore) {}
		}
	}
	private void save()
	{
		Write.toFile(COMMENTS_EVENTS_FILE, events);
		ArrayList<String> comments = new ArrayList<String>();
		for(Comment comment : history)
		{
			comments.add(comment.toString());
		}
		Write.toFile(COMMENTS_HISTORY_FILE, comments);
	}
}

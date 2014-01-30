package jane;

import java.util.ArrayList;

import utils.StringOps;

public class WatchPageParser 
{
	private String pageSource;
	public WatchPageParser(String pageSource)
	{
		this.pageSource = pageSource;
	}
	private static final String WATCH_TAG = "target='_blank'>"; 
	private static final String WATCH_END = "</a></li>";
	public ArrayList<String> getList()
	{
		ArrayList<String> listOfWatchers = new ArrayList<String>();
		int index = 0;
		while((index = pageSource.indexOf(WATCH_TAG, index)) != -1)
		{
			listOfWatchers.add(StringOps.textBetween(pageSource, WATCH_TAG, WATCH_END, index));
			index++;
		}
		return listOfWatchers;
	}
	
}

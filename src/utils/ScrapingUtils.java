package utils;

import java.util.ArrayList;
import java.util.List;

public class ScrapingUtils 
{
	/** Returns the string between start and end, ensuring that it is the first start/end pair working backwards from index */
	public static String lastTextBetween(String pageSource, String start, String end, int offset)
	{
		int startIndex = pageSource.lastIndexOf(start, offset) + start.length();
		if(pageSource.indexOf(end, startIndex + 1) >= startIndex)
		{
			return pageSource.substring(startIndex, pageSource.indexOf(end, startIndex + 1));
		}
		return null;
	}
	/** Searches base string for the first start token, and grabs the string between that and the next occurring end token */
	public static String textBetween(String base, String start, String end)
	{
		return textBetween(base, start, end, 0);
	}
	/** Searches pageSource string for the first start token, starting at an offset, and grabs the string between that and the next occurring end token */
	public static String textBetween(String pageSource, String start, String end, int offset)
	{
		int startIndex = pageSource.indexOf(start, offset) + start.length();
		if(pageSource.indexOf(end, startIndex + 1) >= startIndex)
		{
			return pageSource.substring(startIndex, pageSource.indexOf(end, startIndex + 1));
		}
		return null;
	}
	/** Returns all indices at which tag exists in pageSource */
	public static List<Integer> findAll(String pageSource, String tag)
	{
		List<Integer> indices = new ArrayList<>();
		int index = 0;
		while((index = pageSource.indexOf(tag, index)) != -1)
		{
			indices.add(index);
			index++;
		}
		return indices;
	}
}

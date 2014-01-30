package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class StringOps 
{
	//Number formatting (Could use Formatter)
	public static String roundDown2(double d) 
	{
	    return Double.toString((long) (d * 1e2) / 1e2);
	}
	public static String roundDown1(double d) 
	{
	    return Double.toString((long) (d * 1e1) / 1e1);
	}
	/** Searches base string for the first start token, and grabs the string between that and the next occurring end token */
	public static String textBetween(String base, String start, String end)
	{
		return textBetween(base, start, end, 0);
	}
	/** Searches base string for the first start token, starting at an offset, and grabs the string between that and the next occurring end token */
	public static String textBetween(String base, String start, String end, int offset)
	{
		int startIndex = base.indexOf(start, offset) + start.length();
		if(base.indexOf(end, startIndex + 1) >= startIndex)
		{
			return base.substring(startIndex, base.indexOf(end, startIndex + 1));
		}
		return null;
	}
	public static String capitalizeFirstLetterOf(String word)
	{
		return Character.toUpperCase(word.charAt(0)) + word.substring(1);
	}
	public static String listUp(ArrayList<String> components)
	{
		switch(components.size()) //cannot be empty
		{
			case 0: return "nothing";
			case 1: return components.get(0);
			case 2: return components.get(0)+" and "+components.get(1);
			default:
			{
				StringBuilder builder = new StringBuilder();
				for(int a = 0; a < components.size() - 1; a++)
				{
					builder.append(components.get(a)+", ");
				}
				builder.append("and "+components.get(components.size() - 1));
				return builder.toString();
			}
		}
	}
	
	private static final int BUTTON_WIDTH_IN_NBSP = 11;
	public static String buttonWidthInNBSP()
	{
		return "<img src=\"http://www.simplydevio.us/b.png\">";
	}
	public static String assembleDisplay(File file, Map<String, String> replace) throws IOException
	{
		BufferedReader buffReader = new BufferedReader(new FileReader(file));
		StringBuilder longLine = new StringBuilder();
		String line;
		while((line = buffReader.readLine())!=null)
		{
			boolean echo = true; //whether to write the line read from the file
			for(String replaceKey : replace.keySet())
			{
				if(line.trim().contains(replaceKey))
				{
					longLine.append(replace.get(replaceKey));
					echo = false;
				}
			}
			if(echo)
			{
				longLine.append(line+"\n");
			}
		}
		buffReader.close();
		return longLine.toString(); 
	}
}

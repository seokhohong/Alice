package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Prefs 
{
	private static final String DELIM = ":";
	
	private Map<String, String> prefs = new HashMap<String, String>();
	
	public Prefs(String file)
	{
		parse(Read.fromFile(file));
	}
	private void parse(ArrayList<String> data)
	{
		for(String line : data)
		{
			String[] split = line.split(DELIM);
			prefs.put(split[0].trim(), split[1].trim());
		}
	}
	public String get(String key) throws NoSuchPreferenceException
	{
		if(!prefs.containsKey(key))
		{
			throw new NoSuchPreferenceException(key);
		}
		return prefs.get(key);
	}
	@SuppressWarnings("serial")
	public class NoSuchPreferenceException extends Exception
	{
		NoSuchPreferenceException(String key)
		{
			super("Could not read preference: "+key+"!");
		}
	}
}

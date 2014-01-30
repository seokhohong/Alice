package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/** Used instead of Java's Serialization to make sure final files are readable */
public class MySerializer 
{
	private static final String COMPONENT_DELIM = ";";
	private static final String MAP_DELIM = ",";
	
	private static final String NULL_STRING = "(NULL)";

	private static void writeEndComponent(BufferedWriter writer) throws IOException
	{
		try
		{
			writer.write(COMPONENT_DELIM);
			writer.newLine();
		}
		catch(IOException e) { e.printStackTrace(); }
	}
	public static void readMapStringDate(BufferedReader reader, Map<String, Date> map) throws ParseException, IOException
	{
		String line;
		while((line = readLine(reader)) != null)
		{
			String[] split = line.split(MAP_DELIM);
			map.put(split[0], new SimpleDateFormat().parse(split[1]));
		}
	}
	public static void readMapStringInteger(BufferedReader reader, Map<String, Integer> map) throws NumberFormatException, IOException
	{
		String line;
		while((line = readLine(reader)) != null)
		{
			String[] split = line.split(MAP_DELIM);
			map.put(split[0], Integer.parseInt(split[1]));
		}
	}
	public static void readMapStringLong(BufferedReader reader, Map<String, Long> map) throws NumberFormatException, IOException
	{
		String line;
		while((line = readLine(reader)) != null)
		{
			String[] split = line.split(MAP_DELIM);
			map.put(split[0], Long.parseLong(split[1]));
		}
	}

	public static <K, V> void write(BufferedWriter writer, Map<K, V> map) throws IOException
	{
		for(Object key : map.keySet())
		{
			writer.write(key+MAP_DELIM+map.get(key));
			writer.newLine();
		}
		writeEndComponent(writer);
	}
	public static <T> void write(BufferedWriter writer, Queue<T> queue) throws IOException
	{
		Iterator<T> iter = queue.iterator();
		while(iter.hasNext())
		{
			T nextElem = iter.next();
			writer.write(nextElem.toString());
			writer.newLine();
		}
	}
	public static void readQueueString(BufferedReader reader, Queue<String> queue) throws IOException
	{
		String line;
		while((line = readLine(reader)) != null)
		{
			queue.add(line);
		}
	}
	public static void readSetInteger(BufferedReader reader, Set<Integer> set) throws IOException
	{
		String line;
		while((line = readLine(reader)) != null)
		{
			set.add(Integer.parseInt(line));
		}
	}
	public static void read(BufferedReader reader, Set<String> set) throws IOException
	{
		String line;
		while((line = readLine(reader)) != null)
		{
			set.add(line);
		}
	}
	public static <T> void write(BufferedWriter writer, Collection<T> set) throws IOException
	{
		for(Object obj : set)
		{
			writer.write(obj.toString());
			writer.newLine();
		}
		writeEndComponent(writer);
	}
	//Not to be used internally
	public static String read(BufferedReader reader) throws IOException
	{
		String line = reader.readLine();
		if(line == null || line.equals(NULL_STRING))
		{
			return null;
		}
		else
		{
			return line;
		}
	}
	private static boolean isEnd(String line)
	{
		return line.equals(COMPONENT_DELIM);
	}
	//This read line terminates on the end of a components, as well as EOF
	private static String readLine(BufferedReader reader) throws IOException
	{
		String line = reader.readLine();
		if(line == null || isEnd(line)) 
		{
			return null;
		}
		return line;
	}
	public static void write(BufferedWriter writer, Object obj) throws IOException
	{
		if(obj == null)
		{
			writer.write(NULL_STRING);
		}
		else
		{
			writer.write(obj.toString());
		}
		writer.newLine();
	}
	public static void write(BufferedWriter writer, String s) throws IOException
	{
		writer.write(s);
		writer.newLine();
	}
}

package sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utils.StringOps;

public class Blob 
{
	/*
	 * Compression and Decompression details:
	 * 
	 * Values are separated by the DELIM variable (which should be an unused character in the ASCII key set) 
	 * The entire blob is prefixed and suffixed by the DELIM variable
	 */
	private static final String DELIM = "~"; //cannot use this char in strings
	public static <T> String compress(Set<T> set)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(DELIM);
		for(T elem : set)
		{
			builder.append(elem + DELIM);
		}
		return builder.toString();
	}
	public static Set<String> decompress(String blob)
	{
		Set<String> elems = new HashSet<String>();
		int index = 0;
		while((index = blob.indexOf(DELIM, index)) != -1)
		{
			if(index == blob.length() - 1) //make sure we're not trying to go off the end of the string
			{
				break;
			}
			elems.add(StringOps.textBetween(blob, DELIM, DELIM, index));
			index ++;
		}
		return elems;
	}
	public static Set<Integer> decompressIntSet(String blob)
	{
		Set<Integer> elems = new HashSet<>();
		for(String stringElem : decompress(blob))
		{
			elems.add(Integer.parseInt(stringElem));
		}
		return elems;
	}
	public static <K, V> String compress(Map<K, V> map)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(DELIM);
		for(K key : map.keySet())
		{
			builder.append(key+DELIM+map.get(key)+DELIM);
		}
		return builder.toString();
	}
	public static Map<String, Integer> decompressStringIntMap(String blob)
	{
		Map<String, Integer> map = new HashMap<String, Integer>();
		boolean readKey = true;
		String storedKey = "";
		int index = 0;
		while((index = blob.indexOf(DELIM, index)) != -1)
		{
			if(index == blob.length() - 1)
			{
				break;
			}
			String elem = StringOps.textBetween(blob, DELIM, DELIM, index);
			if(readKey)
			{
				storedKey = elem;
			}
			else
			{
				map.put(storedKey, Integer.parseInt(elem));
			}
			readKey = !readKey; //alternate
			index++;
		}
		return map;
	}
}

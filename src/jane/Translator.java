package jane;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utils.MySerializer;
import utils.Read;

public class Translator 
{
	public static void main(String[] args)
	{
		new Translator().go();
	}
	
	private Map<String, Set<String>> watchers = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> llamas = new HashMap<String, Set<String>>();
	
	private void go()
	{
		ArrayList<String> lines = Read.fromFile(new File("data\\historyData.txt"));
		lines.addAll(Read.fromFile(new File("data\\historyData2.txt")));
		for(String line : lines)
		{
			String[] split = line.split(" ");
			if(split[1].equals("Llamaed"))
			{
				putLlama(split[0], split[2]);
			}
			else
			{
				putWatch(split[0], split[2]);
			}
		}
		writeToFile();
	}
	private void putLlama(String earner, String payer)
	{
		if(!llamas.containsKey(payer))
		{
			llamas.put(payer, new HashSet<String>());
		}
		llamas.get(payer).add(earner);
	}
	private void putWatch(String earner, String payer)
	{
		if(!watchers.containsKey(payer))
		{
			watchers.put(payer, new HashSet<String>());
		}
		watchers.get(payer).add(earner);
	}
	private void writeToFile()
	{
		for(String payer : llamas.keySet()) //either set should work for this short program
		{
			if(!watchers.containsKey(payer))
			{
				watchers.put(payer, new HashSet<String>());
			}
			try
			{
				BufferedWriter buff = new BufferedWriter(new FileWriter(new File("data\\history\\"+payer+".hist")));
				MySerializer.write(buff, System.currentTimeMillis());
				MySerializer.write(buff, watchers.get(payer));
				MySerializer.write(buff, llamas.get(payer));
				buff.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}

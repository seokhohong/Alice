package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import utils.MyIO;
import utils.MySerializer;

/** Keeps track of what payers (historically) have paid in */
public class PayerHistory implements MyIO
{
	private static final File ALL_PAYERS_FILE = new File("data\\allPayers.txt");
	private Set<String> foreverPayers = new HashSet<String>();
	
	public PayerHistory()
	{
		load(ALL_PAYERS_FILE);
	}
	
	public synchronized void save() throws IOException
	{
		save(ALL_PAYERS_FILE);
	}
	
	public synchronized void addPayer(String name)
	{
		foreverPayers.add(name);
	}
	public synchronized boolean hasPayer(String name)
	{
		return foreverPayers.contains(name);
	}
	
	@Override
	public synchronized void save(File f) throws IOException 
	{
		BufferedWriter buff = new BufferedWriter(new FileWriter(f));
		MySerializer.write(buff, foreverPayers);
		buff.close();
		
	}
	@Override
	public synchronized void load(File f) 
	{
		try
		{
			BufferedReader buff = new BufferedReader(new FileReader(f));
			MySerializer.read(buff, foreverPayers);
			buff.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}

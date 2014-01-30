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
public class GroupHistory implements MyIO
{
	private static final File ALL_GROUPS_FILE = new File("data\\allGroups.txt");
	private Set<String> foreverGroups = new HashSet<String>();
	
	public GroupHistory()
	{
		load(ALL_GROUPS_FILE);
	}
	
	public synchronized void save()
	{
		save(ALL_GROUPS_FILE);
	}
	
	public synchronized void addGroup(String name)
	{
		foreverGroups.add(name);
	}
	public synchronized boolean hasGroup(String name)
	{
		return foreverGroups.contains(name);
	}
	
	@Override
	public synchronized void save(File f)
	{
		try
		{
			BufferedWriter buff = new BufferedWriter(new FileWriter(f));
			MySerializer.write(buff, foreverGroups);
			buff.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	@Override
	public synchronized void load(File f)
	{
		try
		{
			BufferedReader buff = new BufferedReader(new FileReader(f));
			MySerializer.read(buff, foreverGroups);
			buff.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	} 

}

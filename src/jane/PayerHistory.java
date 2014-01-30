package jane;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import bryan.WatchList;
import data.Currency;
import utils.FileOps;
import utils.MySerializer;
import worker.Multitasker;

public class PayerHistory 
{
	private static final long UPDATE_INTERVAL = 1000 * 60 * 60 * 24 * 2;
	private static final File HISTORY_DIR = new File("data//history//");
	private static final String EXT = ".hist";

	private String name;
	private long lastUpdated;
	private Set<String> watchers = new HashSet<String>();
	private Set<String> llamas = new HashSet<String>(); //received
	
	private boolean needsUpdating = false;
	
	/** Returns the set of Strings that are in the history but not in the watchlist */
	public Set<String> getUnwatchers(WatchList list)
	{
		Set<String> watchersCopy = new HashSet<String>();
		watchersCopy.addAll(watchers);
		watchersCopy.removeAll(list.getList());
		return watchersCopy;
	}
	
	/** Doesn't really matter whether this is secured from race conditions */
	public boolean hasTransactionFrom(Currency currency, String earner)
	{
		switch(currency)
		{
		case WATCH : return watchers.contains(earner);
		case LLAMA : return llamas.contains(earner);
		default : return false;
		}
	}
	void addTransaction(Currency currency, String earner)
	{
		switch(currency)
		{
		case WATCH : synchronized(watchers) { watchers.add(earner); } break;
		case LLAMA : synchronized(llamas) { llamas.add(earner); } break;
		default : return;
		}
	}
	
	static PayerHistory getInstance(String name)
	{
		for(File file : HISTORY_DIR.listFiles())
		{
			if(FileOps.fileBelongsTo(file, name))
			{
				return new PayerHistory(file, name);
			}
		}
		return new PayerHistory(name);
	}
	
	private PayerHistory(String name)
	{
		this.name = name;
		checkUpdate();
	}
	private PayerHistory(File file, String name)
	{
		this.name = name;
		load(file);
		checkUpdate();
	}
	private void checkUpdate()
	{
		Random random = new Random();
		double alteration = 1d + random.nextDouble() / 5d; //just some randomness so not everybody updates at the same time
		if(System.currentTimeMillis() - lastUpdated > UPDATE_INTERVAL * alteration)
		{
			needsUpdating = true;
			lastUpdated = System.currentTimeMillis();
		}
	}
	
	private static final int LONG_TIME = 600; //in seconds
	synchronized void update()
	{
		if(needsUpdating)
		{
			Multitasker verifier = new Multitasker(5, LONG_TIME);
			Set<LlamaHistory> llamaHistories = new HashSet<LlamaHistory>();
			Set<ParseAllWatches> watchHistories = new HashSet<ParseAllWatches>();
			LlamaHistory llamaHistory = new LlamaHistory(this, verifier);
			llamaHistory.run();
			llamaHistories.add(llamaHistory);
			WatchHistory watchHistory = new WatchHistory(this, verifier);
			watchHistory.run();
			watchHistories.add(watchHistory);
			verifier.done();
			System.out.println("Updated "+name);
			save();
			needsUpdating = false;
		}
	}
	
	private synchronized void load(File file)
	{
		try
		{
			BufferedReader buff = new BufferedReader(new FileReader(file));
			lastUpdated = Long.parseLong(MySerializer.read(buff));
			MySerializer.read(buff, watchers);
			MySerializer.read(buff, llamas);
			buff.close();
		} 
		catch(Exception e)
		{
			System.err.println("Failed to completely load history for "+name);
		}
	}
	void save()
	{
		save(new File(HISTORY_DIR + "\\" + name + EXT));
	}
	private synchronized void save(File file)
	{
		try
		{
			BufferedWriter buff = new BufferedWriter(new FileWriter(file));
			MySerializer.write(buff, lastUpdated);
			MySerializer.write(buff, watchers);
			MySerializer.write(buff, llamas);
			buff.close();
		}
		catch(Exception ignored)
		{
			
		}
	}
	
	public String getName()
	{
		return name;
	}
	
}

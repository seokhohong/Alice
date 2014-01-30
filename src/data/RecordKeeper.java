package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import utils.MyIO;
import utils.MySerializer;

public class RecordKeeper implements MyIO
{

	private File recordsFile;
	private File historyFile;
	
	private int numPayers;			public int getNumPayers() { return numPayers; } 
									synchronized void addPayer() { setModified(); numPayers ++; }
									
	private int numEarners;			public int getNumEarners() { return numEarners; }
									synchronized void addEarner() { setModified(); numEarners ++; }
									
	private int totalPaid;			public int getTotalPaid() { return totalPaid; }
									public synchronized void addTotalPaid(int amt) { setModified(); totalPaid += amt; }
									
	private int numRefers;			public synchronized void addRefer() { setModified(); numRefers ++; }
	private int numGifts;			public synchronized void addGift() { setModified(); numGifts ++; }
									
	private Map<Currency, Integer> totalCounts = new HashMap<Currency, Integer>();
	
									
	private boolean modified = true;	private void setModified() { modified = true; }
										public void clearModified() { modified = false; }
										public boolean getModified() { return modified; }
									
	private static final int SAVE_EVERY = 10; //for history
	
	private ArrayList<String> history = new ArrayList<String>();
	public enum Event
	{
		PAYMENT_IN("PAYIN: "),
		PAYMENT_OUT("PAYOUT: "),
		ERROR("ERROR: "),
		JOURNAL("JOURNAL: ");
		private String message;
		private Event(String message)
		{
			this.message = message;
		}
	}
									
	public RecordKeeper(String recordsFilename, String historyFilename) throws IOException
	{
		recordsFile = new File(recordsFilename);
		historyFile = new File(historyFilename);
		load();
	}
	
	public synchronized void load() throws IOException
	{
		load(recordsFile);
	}
	
	public synchronized void save() throws BackupException, IOException
	{
		save(recordsFile);
	}
	public synchronized void add(Currency currency)
	{
		setModified();
		totalCounts.put(currency, totalCounts.get(currency) + 1);
	}
	public synchronized int get(Currency currency)
	{
		if(!totalCounts.containsKey(currency))
		{
			totalCounts.put(currency, 0);
		}
		return totalCounts.get(currency);
	}
	
	public synchronized void addEvent(Event event, String description)
	{
		addEvent(event, description, new Date());
	}
	
	public synchronized void addEvent(Event event, String description, Date date)
	{
		history.add(event.message + " "+ description +" " + date);
		if(history.size() > SAVE_EVERY)
		{
			saveHistory();
		}
	}
	
	private void saveHistory()
	{
		try
		{
			BufferedWriter buff = new BufferedWriter(new FileWriter(historyFile, true));
			for(String event : history)
			{
				buff.write(event);
				buff.newLine();
			}
			history.clear();
			buff.close();
			
		}
		catch(IOException e)
		{
			
		}
	}
	
	@Override
	public synchronized String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("\n");
		builder.append("<b>" + numPayers + "</b> Deviants have received <b>" + 
					get(Currency.WATCH) + "</b> watches, <b>" + 
					get(Currency.LLAMA) + "</b> llamas, <b>" + 
					get(Currency.FAV) + "</b> favorites, <b>" + 
					get(Currency.COMMENT) + "</b> comments, <b>" + 
					get(Currency.CRITIQUE) + " </b> critiques, and <b>"+
					get(Currency.ENROLLMENT) + "</b> enrollments. \n\n");
		builder.append("<b>" + numEarners + "</b> Deviants have earned <b>"+totalPaid+"</b> points.");
		return builder.toString();
	}
	
	@Override
	public synchronized void save(File f) throws IOException 
	{
		BufferedWriter buff = new BufferedWriter(new FileWriter(f));
		MySerializer.write(buff, Integer.toString(numPayers));
		MySerializer.write(buff, Integer.toString(numEarners));
		MySerializer.write(buff, Integer.toString(totalPaid));
		MySerializer.write(buff, Integer.toString(get(Currency.WATCH)));
		MySerializer.write(buff, Integer.toString(get(Currency.LLAMA)));
		MySerializer.write(buff, Integer.toString(get(Currency.FAV)));
		MySerializer.write(buff, Integer.toString(get(Currency.CRITIQUE)));
		MySerializer.write(buff, Integer.toString(get(Currency.COMMENT)));
		MySerializer.write(buff, Integer.toString(get(Currency.ENROLLMENT)));
		MySerializer.write(buff, Integer.toString(numRefers));
		MySerializer.write(buff, Integer.toString(numGifts));
		buff.close();
	}

	@Override
	public synchronized void load(File f) throws IOException 
	{
		BufferedReader buff = new BufferedReader(new FileReader(f));
		try
		{	
			numPayers = Integer.parseInt(MySerializer.read(buff));
			numEarners = Integer.parseInt(MySerializer.read(buff));
			totalPaid = Integer.parseInt(MySerializer.read(buff));
			totalCounts.put(Currency.WATCH, Integer.parseInt(MySerializer.read(buff)));
			totalCounts.put(Currency.LLAMA, Integer.parseInt(MySerializer.read(buff)));
			totalCounts.put(Currency.FAV, Integer.parseInt(MySerializer.read(buff)));
			totalCounts.put(Currency.CRITIQUE, Integer.parseInt(MySerializer.read(buff)));
			totalCounts.put(Currency.COMMENT, Integer.parseInt(MySerializer.read(buff)));
			totalCounts.put(Currency.ENROLLMENT, Integer.parseInt(MySerializer.read(buff)));
			numRefers = Integer.parseInt(MySerializer.read(buff));
			numGifts = Integer.parseInt(MySerializer.read(buff));
		}
		catch(NumberFormatException e)
		{
			
		}
		buff.close();
	}
	
}

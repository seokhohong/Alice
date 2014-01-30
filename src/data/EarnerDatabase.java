package data;

import java.io.IOException;
import java.sql.ResultSet;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;

import sql.SQLDatabase;
import worker.NoteWriter;
import event.PaymentEvents;

public class EarnerDatabase 
{
	static final String TABLE = "earners";
	
	private static final double CRITIQUE_VALUE = 3.0d;
	
	private HashMap<String, Earner> earners = new HashMap<String, Earner>();
	
	private PaymentEvents paymentEvents;
	private NoteWriter noteWriter;
	private RecordKeeper recorder;
	private SQLDatabase database;
	
	public EarnerDatabase(SQLDatabase database, PaymentEvents paymentEvents, NoteWriter noteWriter, RecordKeeper recorder)
	{
		this.database = database;
		this.paymentEvents = paymentEvents;
		this.noteWriter = noteWriter;
		this.recorder = recorder;
	}
	
	/** Adds an Earner by name without symbol */
	public void add(String name)
	{
		synchronized(earners)
		{
			if(!earners.containsKey(name))	//If new earner
			{
				//just for recording
				if(!earnerInDatabase(name))
				{
					recorder.addEarner();
				}
				earners.put(name, new Earner(database, name));
			}
		}
	}
	
	public boolean earnerInDatabase(String name)
	{
		ResultSet rs = database.executeQuery("select * from "+EarnerDatabase.TABLE+" where "+Earner.COL_NAME+"=\""+name+"\"");
		try
		{
			return rs.next();
		} catch(Exception ignored) {}
		//not reached
		return false;
	}

	public boolean isEmpty()
	{
		synchronized(earners)
		{
			return earners.isEmpty();
		}
	}
	public Collection<Earner> getEarners()
	{
		synchronized(earners)
		{
			return earners.values();
		}
	}
	public boolean has(String earner)
	{
		synchronized(earners)
		{
			return earners.containsKey(earner);
		}
	}
	/** Provides the the safe way to get an earner as it does a contains check on the earner set. Will create Earner if there isn't one already */
	public Earner get(String name)
	{
		Earner earner = null;
		synchronized(earners)
		{
			if(earners.containsKey(name))
			{
				return earners.get(name);
			}
			if(earner == null) //don't have earner
			{
				add(name);
				earner = earners.get(name);
			}
		}
		return earner;
	}
	public void processTransaction(Transaction transaction)
	{
		String origin = transaction.getOrigin().toLowerCase();
		Earner earner = get(origin);
		earner.acceptTransaction(transaction, noteWriter);
	}
	/** Earner is paid for the critique */
	public void addCritique(Critique critique) throws IOException, ParseException
	{
		Earner earner = get(critique.getEarnerName());
		synchronized(earner)
		{
			if(!earner.isBanned())
			{
				paymentEvents.addPayment(get(critique.getEarnerName()), (int) CRITIQUE_VALUE, "Critique Approved on Deviation:\n" + critique.getArtworkURL());
				get(critique.getEarnerName()).save();
			}
		}
	}
	/** Each earner currently in the earners list goes through payment processing. Clears the list afterwards for a fresh batch */
	public void payEarners()
	{
		synchronized(earners)
		{
			for(Earner earner : getEarners())
			{
				earner.pay(paymentEvents);
			}
			earners.clear();
		}
	}
}

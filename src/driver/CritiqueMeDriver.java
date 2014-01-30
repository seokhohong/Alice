package driver;

import java.io.IOException;

import worker.Triggerable;
import main.Listener;
import data.Artist;
import data.PayerDatabase;

public class CritiqueMeDriver extends ProfileUpdateDriver implements Triggerable
{
	private PayerDatabase pDatabase;
	
	public CritiqueMeDriver(Artist artist, DriverPool pool, PayerDatabase pDatabase, Listener listener) throws IOException
	{
		super(artist, pool, "Critique Me", listener);
		this.pDatabase = pDatabase;
	}
	@Override
	public synchronized void trigger()
	{
		doWork();
	}
	@Override
	public String toString()
	{
		return CritiqueMeDriver.class.getName();
	}
	@Override
	public int minUpdateDelay()
	{
		return 1000 * 60 * 5;
	}
	@Override
	String getDisplayText() throws IOException 
	{
		return pDatabase.getCritiqueMe();
	}
}

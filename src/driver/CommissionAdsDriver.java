package driver;

import java.io.IOException;

import worker.Triggerable;
import main.Listener;
import data.Artist;
import data.PayerDatabase;

public class CommissionAdsDriver extends ProfileUpdateDriver implements Triggerable
{	
	private PayerDatabase pDatabase;
	
	public CommissionAdsDriver(Artist artist, DriverPool pool, PayerDatabase pDatabase, Listener listener) throws IOException
	{
		super(artist, pool, "Open Commissions", listener);
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
		return CommissionAdsDriver.class.getName();
	}
	@Override
	public int minUpdateDelay()
	{
		return 1000 * 60 * 5;
	}
	@Override
	public String getDisplayText() throws IOException
	{
		return pDatabase.commissionAds();
	}
}

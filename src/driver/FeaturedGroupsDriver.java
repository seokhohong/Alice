package driver;

import java.io.IOException;

import main.Alice;
import main.Listener;
import data.GroupDatabase;
import worker.Triggerable;

public class FeaturedGroupsDriver extends ProfileUpdateDriver implements Triggerable
{
	private GroupDatabase gDatabase;
	
	public FeaturedGroupsDriver(Alice alice, DriverPool pool, GroupDatabase gDatabase, Listener listener) throws IOException
	{
		super(alice, pool, "Featured Groups", listener);
		this.gDatabase = gDatabase;
	}
	@Override
	public synchronized void trigger()
	{
		doWork();
	}
	@Override
	public String toString()
	{
		return FeaturedGroupsDriver.class.getName();
	}
	@Override
	public int minUpdateDelay()
	{
		return 1000 * 60 * 5;
	}
	@Override
	public String getDisplayText() throws IOException
	{
		return gDatabase.getFeaturePage();
	}
}

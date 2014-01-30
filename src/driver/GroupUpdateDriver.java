package driver;

import main.Listener;

import data.Artist;

public abstract class GroupUpdateDriver extends ProfileUpdateDriver
{	
	public GroupUpdateDriver(Artist artist, DriverPool pool, String widgetTitle, Listener listener)
	{
		super(artist, pool, widgetTitle, listener);
	}
	@Override
	int getEditOffset()
	{
		return 2;
	}
}

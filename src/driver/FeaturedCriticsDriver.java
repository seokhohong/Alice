package driver;

import java.io.IOException;

import main.Listener;
import data.Artist;
import data.ToppersDatabase;

public class FeaturedCriticsDriver extends ProfileUpdateDriver
{
	private ToppersDatabase cDatabase;

	public FeaturedCriticsDriver(Artist artist, DriverPool pool, ToppersDatabase cDatabase, Listener listener) throws IOException
	{
		super(artist, pool, "Critiquers", listener);
		this.cDatabase = cDatabase;
	}
	@Override
	public String toString()
	{
		return FeaturedCriticsDriver.class.getName();
	}
	@Override
	public int minUpdateDelay()
	{
		return 1000 * 60 * 30;
	}
	@Override
	public String getDisplayText() throws IOException
	{
		return cDatabase.getDisplay();
	}
}

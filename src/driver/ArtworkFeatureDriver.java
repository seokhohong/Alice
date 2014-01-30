package driver;

import java.io.IOException;

import main.Listener;
import data.Artist;
import data.PayerDatabase;

public class ArtworkFeatureDriver extends ProfileUpdateDriver
{	
	private PayerDatabase pDatabase;

	public ArtworkFeatureDriver(Artist artist, DriverPool pool, PayerDatabase pDatabase, Listener listener) throws IOException
	{
		super(artist, pool, "Featured Deviations", listener);
		this.pDatabase = pDatabase;
	}
	@Override
	public String toString()
	{
		return ArtworkFeatureDriver.class.getName();
	}
	@Override
	public int minUpdateDelay()
	{
		return 1000 * 60 * 5;
	}
	@Override
	public String getDisplayText() throws IOException
	{
		return pDatabase.artworkFeatures();
	}
}

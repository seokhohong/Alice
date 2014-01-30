package driver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import utils.StringOps;
import main.Listener;
import data.Artist;
import data.RecordKeeper;

public class AliceStatsDriver extends ProfileUpdateDriver
{
	
	public static void main(String[] args)
	{
		new AliceStatsDriver(new Artist("datrade"), new DriverPool(1, new Artist("datrade"), "minderbender"), null, null).doWork();
	}
	
	private static final File WIDGET_FILE = new File("data\\aboutAlice.txt");
	
	private RecordKeeper recorder;
	
	public AliceStatsDriver(Artist artist, DriverPool pool, RecordKeeper recorder, Listener listener)
	{
		super(artist, pool, "- About Alice -", listener);
		this.recorder = recorder;
	}
	@Override
	public String toString()
	{
		return AliceStatsDriver.class.getName();
	}
	@Override
	public int minUpdateDelay()
	{
		return 1000 * 60 * 30;
	}
	@Override
	String getDisplayText() throws IOException 
	{
		Map<String, String> replace = new HashMap<String, String>();
		replace.put("PutStatsHere", recorder.toString());
		return StringOps.assembleDisplay(WIDGET_FILE, replace);
	}
	int getEditOffset()
	{
		return 2;
	}
}

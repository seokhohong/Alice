package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public abstract class TimedFeature
{
	//private static final File FOLDER = new File("data\\feature\\");
	//private static final String EXT = ".feature";
	static final String DELIM = "--TimedFeatureDelim--";
	static final long WEEK_IN_MILLIS = 1000 * 60 * 60 * 24 * 7L;
	private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24L;
	
	private long creation;
	private long duration;
	//private String filename;
	protected String username;
	
	public TimedFeature(int payment, String username)
	{
		creation = System.currentTimeMillis();
		this.duration = calculateDuration(payment);
		this.username = username;
		//this.filename = generateFilename();
	}
	//For loading
	//TimedFeature() {}
	public TimedFeature(String denseString)
	{
		String[] split = denseString.split(DELIM);
		username = split[0];
		creation = Long.parseLong(split[1]);
		duration = Long.parseLong(split[2]);
	}
	
	abstract String getFeatureType();
	abstract long calculateDuration(int payment);
	abstract String displayString(FeatureCategory category); //returns empty string if nothing is appropriate
	
	String display(FeatureCategory category)
	{
		String displayString = displayString(category);
		if(displayString != "")
		{
			return displayString;
		}
		return "";
	}
	
	String daysRemaining()
	{
		int days = (int) ((duration - (System.currentTimeMillis() - creation)) / DAY_IN_MILLIS);
		switch(days)
		{
		case 0: return "<small>(<1 Day Remaining)</small>";
		case 1: return "<small>(1 Day Remaining)</small>";
		default: return "<small>("+days+" Days Remaining)</small>";
		}
	}
	
	public static TimedFeature getInstance(int payment, String username, String wholeMessage)
	{
		if(CommissionFeature.foundTag(wholeMessage))
		{
			return new CommissionFeature(payment, username, wholeMessage);
		}
		return null;
	}
	
	public static boolean foundTag(String message, String[] markers)
	{
		for(String marker : markers)
		{
			if(message.toLowerCase().contains(marker))
			{
				return true;
			}
		}
		return false;
	}
	/*
	public static TimedFeature fromFile(String filename)
	{
		TimedFeature feature = load(new File(filename));
		feature.filename = filename;
		if(!feature.stillActive())
		{			
			feature = null;
		}
		return feature;
	}
	*/
	@Override
	public String toString()
	{
		return username + DELIM + creation + DELIM + duration;
	}
	/*
	private String generateFilename()
	{
		while(true) //until a valid username is found
		{
			String fileusername = FOLDER + "\\" + Long.toString(System.currentTimeMillis()) + EXT;
			if(!new File(fileusername).exists())
			{
				return fileusername;
			}
		}
	}
	*/
	/** Returns if there is still time available on this feature. Deletes the file if there is not*/
	boolean stillActive()
	{
		boolean active = System.currentTimeMillis() - creation < duration;
		//if(!active)
		{
			//new File(filename).delete();
		}
		return active;
	}
	/*
	private static TimedFeature load(File file)
	{
		try
		{
			BufferedReader buff = new BufferedReader(new FileReader(file));
			TimedFeature feature = null;
			String featureClass = buff.readLine();
			if(featureClass.equals(CommissionFeature.typeName()))
			{
				feature = new CommissionFeature();
			}
			feature.username = buff.readLine();
			feature.creation = Long.parseLong(buff.readLine());
			feature.duration = Long.parseLong(buff.readLine());
			feature.load(buff);
			buff.close();
			return feature;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	*/
	/** Overload to read additional information 
	 * @throws IOException */
	protected void load(BufferedReader buff) throws IOException
	{
		
	}
	/*
	private void save(File file) throws IOException
	{
		BufferedWriter buff = new BufferedWriter(new FileWriter(file));
		MySerializer.write(buff, getFeatureType());
		MySerializer.write(buff, username);
		MySerializer.write(buff, creation);
		MySerializer.write(buff, duration);
		save(buff);
		buff.close();
	}
	*/
	/** Overload to save additional information */
	protected void save(BufferedWriter buff) throws IOException
	{
		
	}
}

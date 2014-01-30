package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import utils.MySerializer;

public class CommissionFeature extends TimedFeature 
{
	private static final String TYPE_NAME = "Commission Feature";				public static String typeName() { return TYPE_NAME; }
	private static final FeatureCategory CATEGORY = FeatureCategory.COMMISSION;
	private static final int COST_PER_WEEK = 20;
	
	private String contents;
	private String thumb;
	
	/** Duration of feature and the entire message. Name is just in case they do not provide a thumb*/
	CommissionFeature(int payment, String name, String message)
	{
		super(payment, name);
		parse(message, name);
	}
	public CommissionFeature(String denseString)
	{
		super(denseString);
		String[] split = denseString.split(DELIM);
		contents = split[3];
		thumb = split[4];
	}
	
	private static final String[] MARKERS = { "commissions", "comissions", "commisions", "comisions", "commission", "comission", "commision", "comision" };
	public static boolean foundTag(String message)
	{
		return TimedFeature.foundTag(message, MARKERS);
	}
	
	private static final String THUMB_TAG = ":thumb";
	private static final String THUMB_END = ":";
	private static final int NO_THUMB = -1;
	
	//Retrieves data from message
	private void parse(String message, String name)
	{
		contents = message;
		for(String marker : MARKERS)
		{
			if(contents.contains(marker))
			{
				contents = contents.replaceFirst(marker, "");
				break;
			}
		}
		extractThumb(name);
	}
	
	//Cuts out the thumb from the contents and saves it separately
	private void extractThumb(String name)
	{
		int thumbIndex = contents.indexOf(THUMB_TAG);
		if(thumbIndex != NO_THUMB)
		{
			int thumbEnd = contents.indexOf(THUMB_END, thumbIndex + 1) + THUMB_END.length();
			thumb = contents.substring(thumbIndex, thumbEnd);
			contents = contents.replace(thumb, "").trim();
		}
		else
		{
			thumb = "<a href=\""+name+".deviantart.com/\">Profile</a>";
		}
	}
	
	@Override
	public String displayString(FeatureCategory category)
	{
		if(CATEGORY == category)
		{
			return ":icon"+username+": "+thumb+"\n"+contents+" "+daysRemaining();
		}
		return "";
	}
	@Override
	public String toString()
	{
		return super.toString() + DELIM + contents + DELIM + thumb;
	}
	@Override
	long calculateDuration(int payment)
	{
		return TimedFeature.WEEK_IN_MILLIS * payment / COST_PER_WEEK;
	}
	/*
	@Override
	protected void load(BufferedReader buff) throws IOException
	{
		contents = buff.readLine();
		thumb = buff.readLine();
	}
	@Override
	protected void save(BufferedWriter buff) throws IOException
	{
		MySerializer.write(buff, contents);
		MySerializer.write(buff, thumb);
	}
	
	*/
	@Override
	String getFeatureType() 
	{
		return TYPE_NAME;
	}
}

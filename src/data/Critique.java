package data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Data about a Critique, gathered from a profile's list */
public class Critique
{
	private static final long AUTO_ACCEPT = 1000 * 60 * 60 * 24 * 2; //accepts automatically after this much time has passed since submission
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat();
	private static final String DELIM = "`";
	
	private String dev;					public String getDeviant() { return dev; }
	private String earnerName;			public String getEarnerName() { return earnerName; }
	private int artworkId;				public int getArtworkId() { return artworkId; }
	private int critiqueId;				public int getCritiqueId() { return critiqueId; }
	private String artworkURL;			public String getArtworkURL() { return artworkURL; }
	private String dateOfCritique;
	
	public Critique(String dev, String earnerName, int artworkId, int critiqueId, String artworkURL, Date dateOfCritique)
	{
		this.dev = dev;
		this.earnerName = earnerName;
		this.artworkId = artworkId;
		this.critiqueId = critiqueId;
		this.artworkURL = artworkURL;
		this.dateOfCritique = DATE_FORMAT.format(dateOfCritique);
	}
	
	public static Critique parse(String toParse)
	{
		String[] split = toParse.split(DELIM);
		Critique critique = null;
		try
		{
			critique = new Critique(split[0], split[1], Integer.parseInt(split[2]), Integer.parseInt(split[3]), split[4], DATE_FORMAT.parse(split[5]));
		}
		catch(ParseException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		return critique; 
	}
	
	public String URL()
	{
		return "http://"+earnerName+".deviantart.com/critique/"+critiqueId+"/";
	}
	
	public boolean pastDue()
	{
		try {
			return System.currentTimeMillis() - DATE_FORMAT.parse(dateOfCritique).getTime() > AUTO_ACCEPT;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		return dev + DELIM + earnerName + DELIM + artworkId + DELIM +critiqueId + DELIM + artworkURL + DELIM + dateOfCritique;
	}
	
}

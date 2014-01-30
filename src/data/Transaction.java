package data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import sql.SQLDatabase;
import sql.SQLUtils;
import utils.StringOps;

public class Transaction 
{
	private static final String DELIMITER = "aoehudcagdcbnetr"; //hopefully nobody types this in
	private static final String TABLE = "transactions";
	
	private static final int NUM_DATA_ENTRIES = 6;
	
	private static Map<Integer, Double> bonuses = new HashMap<Integer, Double>();
	static
	{
		bonuses.put(100, 1.1);
		bonuses.put(500, 1.2);
	}
	
	private String[] data = new String[NUM_DATA_ENTRIES];
	
	public String getDate() { return data[0]; }
	public String getTime() { return data[1]; }
	public String getOrigin() { return data[2]; }
	public String getDest() { return data[3]; }
	public String getMessage() { return data[4]; }
	public String getFirstWord() { return data[4].split(" ")[0]; }
	public int getAmount() { return Integer.parseInt(data[5]); }
	
	private String origString;
	
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MMMM dd, yyyyhh:mmaa", Locale.US);
	
	//Worst-case scenario
	private static String[] DEFAULT_ENTRY = {"Aug 7, 2013", "12:26pm", "dATrade", "pemdara", "", "-1"};

	/** Takes a line from the file, this has nothing to do with web-scraping */
	public Transaction(String line)
	{
		data = line.split(DELIMITER);
		if(data.length > NUM_DATA_ENTRIES) //terrible, worst case scenario, shouldn't ever happen
		{
			data = DEFAULT_ENTRY;
		}
		this.origString = line;
	}
	public Transaction(String date, String time, String origin, String dest, String message, int amount)
	{
		data[0] = new String(date);
		data[1] = new String(time);
		data[2] = new String(origin);
		data[3] = new String(dest);
		data[4] = new String(message);
		data[5] = Integer.toString(amount);
		origString = createOrigString();
	}
	private String createOrigString()
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 0; a < data.length; a++)
		{
			builder.append(data[a]);
			if(a != data.length - 1)
			{
				builder.append(DELIMITER);
			}
		}
		return builder.toString();
	}
	public boolean after(Transaction t)
	{
		return getFullDate().after(t.getFullDate());
	}

	public Date getFullDate()
	{
		try 
		{
			return DATE_FORMATTER.parse(getDate() + getTime());
		} 
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return new Date();
	}
	private static final double PENALTY = 0.75;
	static int calculateRefund(int points)
	{
		Integer greatestThreshold = -1;
		for(Integer threshold : bonuses.keySet())
		{
			if(points >= threshold / 2 && threshold > greatestThreshold)
			{
				greatestThreshold = threshold;
			}
		}
		if(greatestThreshold != -1)
		{
			return (int) (points / bonuses.get(greatestThreshold) * PENALTY);
		}
		return (int) (points * PENALTY);
	}
	
	static double applyBonuses(double amount)
	{
		Integer greatestThreshold = -1;
		for(Integer threshold : bonuses.keySet())
		{
			if(amount >= threshold && threshold > greatestThreshold)
			{
				greatestThreshold = threshold;
			}
		}
		if(greatestThreshold != -1)
		{
			return amount * bonuses.get(greatestThreshold);
		}
		return amount;
	}
	
	String extractDevName()
	{
		return StringOps.textBetween(getMessage().toLowerCase(), ":dev", ":");
	}
	
	public void saveInto(SQLDatabase database)
	{
		//database.executeUpdate(sqlString());
	}
	private String sqlString()
	{
		return "insert into "+TABLE+" values("
				+ "\""+SQLUtils.format(getDate()) + "\","
				+ "\""+SQLUtils.format(getTime()) + "\","
				+ "\""+SQLUtils.format(getOrigin()) + "\","
				+ "\""+SQLUtils.format(getMessage()) + "\","
				+ getAmount() + ");";
	}
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Transaction)
		{
			Transaction t = (Transaction) o;
			for(int a = 0; a < data.length; a++)
			{
				if(!t.data[a].toString().equals(data[a].toString()))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
	@Override
	public int hashCode()
	{
		//Should be enough
		return getDate().hashCode() * getTime().hashCode() * getOrigin().hashCode() * Integer.toString(getAmount()).hashCode();
	}
	@Override
	public String toString()
	{
		return origString;
	}
}

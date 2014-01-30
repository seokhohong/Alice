package data;

import java.util.ArrayList;
import java.util.Map;

import utils.StringOps;

public enum Currency 
{
	WATCH("watch", "watches", 1d, 0.25d, " watches"),
	LLAMA("llama", "llamas", 0.25d, 0.15d, " llamas"),
	FAV("fav", "favs", 0.2d, 0.1d, " favs"),
	COMMENT("comment", "comments", 1d, 1.0d, " comments"),
	CRITIQUE("critique", "critiques", 2, 3, " critiques (some additional critiques may be pending your approval)"),
	ENROLLMENT("enrollment", "enrollments", 0.5d, 0.3d, " enrollments");
	
	public static final double PENALTY = -1d; //for unwatching, unenrolling, etc.
	
	//also represents singular
	private String infoCommand;				public String getCommand() { return infoCommand; }
	private String plural;					public String getPlural() { return plural; }
	private double cost;					public double getCost() { return cost; }
	private double payout;					public double getPayout() { return payout; }
	private String receivedPlural;			public String getReceivedPlural() { return receivedPlural; } 
	
	private Currency(String infoCommand, String plural, double cost, double payout, String receivedPlural)
	{
		this.infoCommand = infoCommand;
		this.plural = plural;
		this.cost = cost;
		this.payout = payout;
		this.receivedPlural = receivedPlural;
	}
	
	public static String assembleList(Map<Currency, Integer> quantities)
	{
		ArrayList<String> components = new ArrayList<String>();
		for(Currency c : values())
		{
			if(quantities.containsKey(c) && quantities.get(c) > 0)
			{
				components.add(c.count(quantities.get(c)));
			}
		}
		return StringOps.listUp(components);
	}
	
	public String count(int num)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(num);
		builder.append(" ");
		builder.append(StringOps.capitalizeFirstLetterOf(((num == 1) ? infoCommand : plural)));
		return builder.toString();
	}
	@Override
	public String toString()
	{
		return infoCommand;
	}
}

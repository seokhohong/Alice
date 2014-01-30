package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import utils.MySerializer;
import utils.StringOps;
import worker.NoteWriter;

public class Group implements Comparable<Group>, Refundable
{	
	private static final File GROUPS_FOLDER = new File("data\\group\\");
	private static final String EXT = ".grp";
	private static final String GROUP_TAG = "group";		public static String getTag() { return GROUP_TAG; }
	
	private static final String WATCH_PREF_TAG = "watch";
	private static final String ENROLLMENT_PREF_TAG = "enrollment";
	
	private static Map<Currency, String> TAG = new HashMap<Currency, String>();
	private static Map<Currency, Boolean> DEFAULT_ACCEPTS = new HashMap<Currency, Boolean>();
	static
	{
		TAG.put(Currency.WATCH, WATCH_PREF_TAG);
		TAG.put(Currency.ENROLLMENT, ENROLLMENT_PREF_TAG);
		DEFAULT_ACCEPTS.put(Currency.COMMENT, false);
		DEFAULT_ACCEPTS.put(Currency.LLAMA, false);
		DEFAULT_ACCEPTS.put(Currency.CRITIQUE, false);
		DEFAULT_ACCEPTS.put(Currency.FAV, false);
		DEFAULT_ACCEPTS.put(Currency.WATCH, true);
		DEFAULT_ACCEPTS.put(Currency.ENROLLMENT, true);
	}
	
	private Map<Currency, Integer> received = new HashMap<Currency, Integer>();
	private Map<Currency, Boolean> accepts = new HashMap<Currency, Boolean>();
	
	private String name;					@Override
	public String getName() { return name; }
	private String description;				public String getDescription() { return description; }
	private double points;					double getPoints() { return points; }
	
	private boolean modified = false;		private void modified() { modified = true; }
											boolean getModified() { return modified; }
											void clearModified() { modified = false; }
	
	public Group(String name, String description)
	{
		this.name = name;
		this.description = description;
		initData();
	}
	private void initData()
	{
		received.put(Currency.WATCH, 0);
		received.put(Currency.ENROLLMENT, 0);
	}
	private void setDefaultAcceptance()
	{
		for(Currency tag : TAG.keySet())
		{
			accepts.put(tag, DEFAULT_ACCEPTS.get(tag));
		}
	}

	public Group(File file)
	{
		load(file);
	}
	
	/** Won't change frequently enough to be worth sync */
	public boolean accepts(Currency currency)
	{
		if(!accepts.containsKey(currency))
		{
			accepts.put(currency, false);
		}
		return accepts.get(currency);
	}
	public synchronized int numReceived(Currency currency)
	{
		if(!received.containsKey(currency))
		{
			received.put(currency, 0);
		}
		return received.get(currency);
	}
	
	static String parseDescription(String message)
	{
		int firstQuote = message.indexOf("\"");
		int lastQuote = message.lastIndexOf("\"");
		if(firstQuote != -1 && lastQuote != firstQuote) //there is a proper description
		{
			//offset by length of quote
			return message.substring(firstQuote + 1, lastQuote);
		}
		else
		{
			return "";
		}
	}
	
	public synchronized void acceptTransaction(Transaction transaction, NoteWriter noteWriter)
	{
		double amount = transaction.getAmount();
		System.out.println("Adding "+amount+" points to "+transaction.getOrigin()+"'s Group");
		amount = Transaction.applyBonuses(amount);
		points += amount;
		modified();
		setPreferences(transaction.getMessage());	//and update preferences
		noteWriter.send(name, "Donation Received!", thankYouMessage());
		save();
	}
	private void setPreferences(String message)
	{
		message.replace(parseDescription(message), "");
		for(Currency currency : TAG.keySet())
		{
			accepts.put(currency, message.contains(TAG.get(currency)));
		}
		if(cantBuyMore()) //meaning the message says they want none
		{
			setDefaultAcceptance();
		}
	}
	public boolean cantBuyMore()
	{
		for(Currency currency : TAG.keySet())
		{
			if(points < currency.getCost())
			{
				accepts.put(currency, false);
			}
		}
		return !accepts.get(Currency.WATCH) && !accepts.get(Currency.ENROLLMENT);
	}
	private String thankYouMessage()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Thank you for donating! :heart: The group's current balance is ");
		builder.append(StringOps.roundDown2(points));
		builder.append(" :points: .\n\n");
		builder.append("The group currently accepts: ");
		builder.append(acceptanceList());
		builder.append(".\n\n");
		//builder.append("Try the following commands with Alice: #refund, #stats\n\n");
		builder.append(" - automated by Program Alice");
		return builder.toString();
	}
	public String getWatchesURL(int offset)
	{
		return "http://www."+name+".deviantart.com/friends/?offset="+offset;
	}
	public String getMembersURL()
	{
		return "http://www."+name+".deviantart.com/modals/memberlist/";
	}
	public String acceptanceList()
	{
		StringBuilder builder = new StringBuilder();
		ArrayList<String> accepts = new ArrayList<String>();
		for(Currency currency : TAG.keySet())
		{
			if(accepts(currency))
			{
				accepts.add(StringOps.capitalizeFirstLetterOf(currency.getPlural()));
			}
		}
		builder.append(StringOps.listUp(accepts));
		return builder.toString();
	}
	
	synchronized void hasReceived(Currency currency)
	{
		if(accepts(currency))
		{
			System.out.println(name + " Received " + currency.toString());
			received.put(currency, numReceived(currency) + 1);
			points -= currency.getCost();
			modified();
		}
	}
	@Override
	public void refund(Currency currency)
	{
		received.put(currency, numReceived(currency) - 1);
		points += currency.getCost();
		modified();
	}
	
	private String buildReceivedString()
	{
		ArrayList<String> perType = new ArrayList<String>(); //builds the string for each receivable type of item
		for(Currency type : TAG.keySet())
		{
			int count = numReceived(type);
			switch(count)
			{
			case 0: break;
			case 1: perType.add(count+" "+type.toString()); break; //a tiny problem is left if the person received only one critique
			default : perType.add(count+" "+type.getPlural()); break;
			}
		}
		return StringOps.listUp(perType);
	}
	
	public synchronized String getFinishedMessage()
	{
		StringBuilder comment = new StringBuilder();
		comment.append("Hi there~! :D ");
		comment.append("The group has received "+buildReceivedString()+"! \n\nIf you would like to be featured again, please make another donation. :happybounce: \n\n - automated by Program Alice ");
		return comment.toString();
	}
	
	void save()
	{
		try
		{
			save(new File(GROUPS_FOLDER + "\\" + name + EXT));
		} catch(IOException ohWell) {}
	}
	private synchronized void save(File f) throws IOException
	{
		BufferedWriter buff = new BufferedWriter(new FileWriter(f));
		MySerializer.write(buff, name);
		MySerializer.write(buff, Double.toString(points));
		MySerializer.write(buff, description);
		MySerializer.write(buff, Integer.toString(numReceived(Currency.WATCH)));
		MySerializer.write(buff, Integer.toString(numReceived(Currency.ENROLLMENT)));
		MySerializer.write(buff, Boolean.toString(accepts(Currency.WATCH)));
		MySerializer.write(buff, Boolean.toString(accepts(Currency.ENROLLMENT)));
		buff.close();
	}
	private void load(File file)
	{
		try
		{
			BufferedReader buff = new BufferedReader(new FileReader(file));
			try
			{
				name = MySerializer.read(buff);
				points = Float.parseFloat(MySerializer.read(buff));
				description = MySerializer.read(buff);
				received.put(Currency.WATCH, Integer.parseInt(MySerializer.read(buff)));
				received.put(Currency.ENROLLMENT, Integer.parseInt(MySerializer.read(buff)));
				accepts.put(Currency.WATCH, Boolean.parseBoolean(MySerializer.read(buff)));
				accepts.put(Currency.ENROLLMENT, Boolean.parseBoolean(MySerializer.read(buff)));
			}
			catch(NullPointerException | NumberFormatException e)
			{
				System.err.println("Could not read everything, defaulting some values for "+name);
			}
			buff.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public int compareTo(Group otherGroup) 
	{
		if(otherGroup.points > points)
		{
			return 1;
		}
		else if(otherGroup.points < points)
		{
			return -1;
		}
		return 0;
	}
	
	public String getLink(String tag)
	{
		return "<a href=\"http://" + name + ".deviantart.com\" target=\"_blank\" class=\"smbutton smbutton-white\"><span class=\"post\">" + tag + "</span></a>";
	}
	
	public synchronized String getWidgetText()
	{
		boolean acceptsWatches = accepts(Currency.WATCH);
		boolean acceptsEnrollments = accepts(Currency.ENROLLMENT);
		if(!acceptsWatches && !acceptsEnrollments)
		{
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append(":icon");
		builder.append(name);
		builder.append(":");
		if(acceptsWatches)
		{
			builder.append("&nbsp;"+getLink(":+devwatch: Watch"));
		}
		else
		{
			builder.append(StringOps.buttonWidthInNBSP());
		}
		if(acceptsEnrollments)
		{
			builder.append("&nbsp;"+getLink("<img src=\"http://fc08.deviantart.net/fs70/f/2013/258/4/a/join_by_datrade-d6mh3qc.png\"> Join"));
		}
		else
		{
			builder.append(StringOps.buttonWidthInNBSP());
		}
		builder.append("&nbsp;<sub>(");
		builder.append(StringOps.roundDown1(points));
		builder.append(")</sub> \n\n");
		builder.append(description);
		builder.append("<br>");
		
		return builder.toString();
	}
}

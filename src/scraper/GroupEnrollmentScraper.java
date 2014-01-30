package scraper;

import java.io.IOException;
import java.text.ParseException;
import data.Currency;
import data.Earner;
import data.EarnerDatabase;
import data.Group;
import data.GroupDatabase;

public class GroupEnrollmentScraper extends Thread
{
	public static void main(String[] args) throws IOException, ParseException
	{
		//new GroupEnrollmentScraper(new EarnerDatabase(null, null, null), new GroupDatabase(null, null), new Group("dragons-organisation", "")).start();
	}
	private EarnerDatabase eDatabase;
	private GroupDatabase gDatabase;
	private Group group;
	
	public GroupEnrollmentScraper(EarnerDatabase eDatabase, GroupDatabase gDatabase, Group group)
	{
		this.eDatabase = eDatabase;
		this.gDatabase = gDatabase;
		this.group = group;
	}
	@Override
	public void run()
	{
		String pageSource = WebScraper.get(group.getMembersURL());
		searchPage(pageSource);
	}
	
	//private static final String NAME_TAG = "alt=\":icon";
	//private static final String NAME_END = ":";
	
	private void searchPage(String pageSource)
	{
		for(Earner earner : eDatabase.getEarners())
		{
			if(pageSource.contains(appendEnds(earner)))
			{
				System.out.println("Attempting to record Enrollment from "+earner.getName()+" to "+group.getName());
				if(earner.addEnrollmentFrom(group))
				{
					gDatabase.hasReceived(group.getName(), Currency.ENROLLMENT);
				}
			}
		}
	}
	//Appends characters to the front and back to make sure we are looking for a unique tag in the pagesource
	private static String appendEnds(Earner earner)
	{
		return "http://"+earner.getName() + ".";
	}

	/*
	private void parseGroups(String pageSource)
	{
		Set<String> groups = new HashSet<String>();
		int index = 0;
		while((index = pageSource.indexOf(NAME_TAG, index))>-1)
		{
			String groupName = StringOps.textBetween(pageSource, NAME_TAG, NAME_END, index);
			processGroup(groupName, earner);
			groups.add(groupName);
			index++;
		}
		earner.checkUnenroll(groups);
	}
	private void processGroup(String groupName, Earner earner)
	{
		if(gDatabase.has(groupName) && gDatabase.get(groupName).accepts(Currency.ENROLLMENT))
		{
			System.out.println("Attempting to record Enrollment from "+earner.getName()+" to "+groupName);
			if(earner.addEnrollmentFrom(gDatabase.get(groupName)))
			{
				gDatabase.hasReceived(groupName, Currency.ENROLLMENT);
			}
		}
	}
	*/
}

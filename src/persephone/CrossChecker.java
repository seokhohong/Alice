package persephone;

import java.io.File;

import utils.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CrossChecker 
{
	public static void main(String[] args)
	{
		new CrossChecker().go();
	}
	
	private void go()
	{
		ArrayList<String> history = Read.fromFile(new File("data\\history.txt"));
		ArrayList<String> gifts = Read.fromFile(new File("data\\gifts.txt"));
		Set<String> userHistory = parseUsers(history);
		Set<String> gifted = parseGifts(gifts);
		System.out.println(gifted.size());
		//gifted.removeAll(userHistory);
		for(String gift : gifted)
		{
			if(userHistory.contains(gift))
			{
				System.out.println(gift);
			}
		}
	}
	private Set<String> parseUsers(ArrayList<String> history)
	{
		Set<String> users = new HashSet<String>();
		for(String line : history)
		{
			int toIndex = line.indexOf(" to ");
			users.add(StringOps.textBetween(line, " to "," ", toIndex));
		}
		return users;
	}
	private Set<String> parseGifts(ArrayList<String> gifts)
	{
		Set<String> gifted = new HashSet<String>();
		for(String line : gifts)
		{
			gifted.add(line.substring(0, line.indexOf(":")));
		}
		return gifted;
	}
}

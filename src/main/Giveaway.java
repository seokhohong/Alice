package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import utils.Read;

public class Giveaway 
{
	public static void main(String[] args)
	{
		new Giveaway().go();
	}
	private void go()
	{
		ArrayList<Set<String>> ithNames = new ArrayList<>();
		for(int a = 2; a < 8; a ++)
		{
			ArrayList<String> giveaway = Read.fromFile(new File("data/giveaway"+a+".txt"));
			Set<String> athNames = new HashSet<String>();
			parse(giveaway, athNames);
			ithNames.add(athNames);
		}
		for(int a = ithNames.size() - 1; a --> 0; )
		{
			System.out.println(ithNames.get(ithNames.size() - 1).size());
			ithNames.get(ithNames.size() - 1).removeAll(ithNames.get(a));
		}
	}
	private void parse(ArrayList<String> lines, Set<String> names)
	{
		for(String line : lines)
		{
			if(line.contains(". "))
			{
				names.add(line.substring(line.indexOf(". ") + 2));
			}
		}
	}
}

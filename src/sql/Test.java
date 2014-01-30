package sql;

import data.Earner;
import data.Payer;

public class Test 
{
	SQLDatabase database = new SQLDatabase("localhost", "root", "'a;,oq123", "deviantart");
	public static void main(String[] args)
	{
		new Test().go();
	}
	private void go()
	{
		Earner animelovers = new Earner(database, "animelovers21");
		System.out.println(animelovers.getSQLSave());
		animelovers.addPoints(1.0);
		System.out.println(animelovers.getSQLSave());
		animelovers.save();
		animelovers = new Earner(database, "animelovers21");
		System.out.println(animelovers.getSQLSave());
	}
}

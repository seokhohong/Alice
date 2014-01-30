package sql;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import data.Currency;
import data.Earner;
import data.Payer;
import sql.SQLDatabase;
import utils.MySerializer;

public class Main 
{
	public static void main(String[] args)
	{
		new Main().go();
	}
	private void go()
	{
		/*
		SQLDatabase database = new SQLDatabase("localhost", "root", "'a;,oq123", "deviantart");
		
		database.executeUpdate("drop table payers");
		Payer.makeTable(database);
		for(File payer : new File("data\\payer").listFiles())
		{
			database.executeUpdate(new Payer(payer).getSQLSave());
			System.out.println("Databased "+payer.getName());
		}
		*/
		//database.backup(new File("C:\\backup\\backup.sql"));
		
		//database.executeUpdate("drop table payers");
		//database.executeUpdate("drop table earners");
		/*
		Payer.makeTable(database);
		Earner.makeTable(database);
		for(File payer : new File("data\\payer").listFiles())
		{
			database.executeUpdate(new Payer(payer).getSQLSave());
			System.out.println("Databased "+payer.getName());
		}
		for(File earner : new File("data\\earner").listFiles())
		{
			database.executeUpdate(new Earner(earner).getSQLSave());
			System.out.println("Databased "+earner.getName());
		}
		*/
		/*
		Earner simply = null;
		try {
			simply = new Earner(new File("simplysilent.earner"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		/*
		database.executeUpdate("create table earners("
				+ COL_NAME + " varchar(64),"
				+ COL_WATCHES + " int,"
				+ COL_LLAMAS + " int,"
				+ COL_FAVS + " int,"
				+ COL_ENROLLMENTS + " int,"
				+ COL_COMMENTS + " int,"
				+ COL_POINTS + " double,"
				+ COL_BANNED + " int," //boolean (0, 1)
				+ COL_WATCH_HIST + " mediumblob,"
				+ COL_LLAMA_HIST + " mediumblob,"
				+ COL_FAV_HIST + " mediumblob,"
				+ COL_COMMENT_HIST + " mediumblob,"
				+ COL_CRITIQUE_HIST + " mediumblob,"
				+ COL_ENROLL_HIST + " mediumblob,"
				+ COL_UNENROLL + " int,"
				+ COL_UNWATCH + " int);");
		*/		
		//System.out.println(simply.getSQLSave());
		//database.executeUpdate(simply.getSQLSave());
		//System.out.println(new Earner(database, "simplysilent").getSQLSave());
		//database.backup(new File("C:\\backup\\backup.sql"));
		//database.executeUpdate("drop table earners");
	}
}

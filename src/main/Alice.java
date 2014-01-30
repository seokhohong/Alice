package main;

import inject.Inject;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import scraper.AcceptanceVerifier;
import sql.SQLDatabase;
import jane.HistoryDatabase;
import jane.JaneRunner;
import command.CommandFactory;

import worker.AsyncReader;
import worker.CommentsManager;
import worker.LimitedFrequencyOperation;
import worker.LoadManager;
import worker.NoteWriter;
import worker.Staller;
import data.Artist;
import data.Backup;
import data.GiveawayDatabase;
import data.ToppersDatabase;
import data.EarnerDatabase;
import data.GroupDatabase;
import data.PayerDatabase;
import data.RecordKeeper;
import data.TransactionDatabase;
import driver.*;
import event.PaymentEvents;
import event.ReplyEvents;

public class Alice extends Artist
{
	
	private static final String TRANSACTIONS_FILENAME = "data\\transactions.txt";
	private static final String HISTORY_FILENAME = "data\\history.txt";
	private static final String RECORDS_FILENAME = "data\\records.txt";
	private static final String CRITIQUES_FILENAME = "data\\toppers.txt";
	private static final String BACKUP_FILENAME = "backup.txt";
	
	private static final String NOTIFY_DEST = "2096408363@vtext.com";
	
	private static final String SQL_SERVER = "localhost";
	private static final String SQL_USERNAME = "root";
	
	private static final int NUM_DRIVERS = 4;
	
	private Alice(String name)
	{
		super(name);
	}
	
	@Override
	public String getName() { return name; }
	
	/** Main */
	public static void main(String[] args)
	{
		new Alice(ACCOUNT).go();
	}
	
	private void go()
	{
		DriverPool drivers = new DriverPool(NUM_DRIVERS, this, PASSWORD);
		NoteWriter noteWriter = new NoteWriter(drivers); //used to write notes
		Listener listener = new AliceListener(ALICE_EMAIL, ALICE_PASSWORD, NOTIFY_DEST, noteWriter); //catches exceptions thrown in threads
		((AliceListener) listener).start();
		
		try
		{
			RecordKeeper recorder = new RecordKeeper(RECORDS_FILENAME, HISTORY_FILENAME);
			ReplyEvents replyEvents = new ReplyEvents(drivers);
			PaymentEvents paymentEvents = new PaymentEvents(drivers, recorder);
			SQLDatabase database = new SQLDatabase(SQL_SERVER, SQL_USERNAME, SQL_PASSWORD, SQL_DATABASE);
			database.backup(new File("C:\\backup\\backup.sql"));
			PayerDatabase pDatabase = new PayerDatabase(database, paymentEvents, noteWriter, recorder);
			EarnerDatabase eDatabase = new EarnerDatabase(database, paymentEvents, noteWriter, recorder);
			GroupDatabase gDatabase = new GroupDatabase(noteWriter, recorder);
			GiveawayDatabase gaDatabase = new GiveawayDatabase(paymentEvents, noteWriter);
			TransactionDatabase tDatabase = new TransactionDatabase(TRANSACTIONS_FILENAME, pDatabase, eDatabase, gDatabase, gaDatabase, paymentEvents, noteWriter, recorder, ACCOUNT);
			ToppersDatabase topDatabase = new ToppersDatabase(CRITIQUES_FILENAME);
			HistoryDatabase hDatabase = new HistoryDatabase();
			
			//Inject Code Here
			//Inject.transaction(gaDatabase, pDatabase);
			
			MarkCritiqueDriver markCritiqueDriver = new MarkCritiqueDriver(drivers, pDatabase, topDatabase);
			
			//Triggers so that these actions *can* be activated on an instant's notice
			TransactionDriver transactionDriver = new TransactionDriver(drivers, tDatabase);
			
			FeaturedDeviantsDriver featuredDeviantsDriver = new FeaturedDeviantsDriver(this, drivers, pDatabase, listener);
			
			CritiqueMeDriver critiqueMeDriver = new CritiqueMeDriver(this, drivers, pDatabase, listener);
			
			CommentCornerDriver commentCornerDriver = new CommentCornerDriver(new Artist("theexhibition"), drivers, pDatabase, noteWriter, listener);
			
			CommandFactory commandFactory = new CommandFactory(replyEvents, pDatabase, eDatabase, hDatabase, gDatabase);
			CommentsManager commentsManager = new CommentsManager(replyEvents, pDatabase, eDatabase, gDatabase, topDatabase, commandFactory);
			
			new JaneRunner(hDatabase).start();
			
			//Profile widget updating is costly and can occupy drivers for long amounts of time, starving other tasks. 
			//LoadManager takes care of this, running one thing at once
			Set<LimitedFrequencyOperation> operations = new HashSet<LimitedFrequencyOperation>();
			
			operations.add(featuredDeviantsDriver);
			
			operations.add(new AliceStatsDriver(this, drivers, recorder, listener));
			
			//Trigger transactionTrigger = new Trigger(transactionDriver);
			//loadManager.add(new ClosedArtworkChecker(this, noteWriter, pDatabase));
			operations.add(new FeaturedCriticsDriver(this, drivers, topDatabase, listener));
			operations.add(critiqueMeDriver);
			operations.add(new AcceptanceVerifier(pDatabase, eDatabase, recorder, listener));
			operations.add(new FeaturedGroupsDriver(this, drivers, gDatabase, listener));
			
			//operations.add(new Bryan(pDatabase, eDatabase, gDatabase, hDatabase));
			
			operations.add(transactionDriver);
			
			operations.add(commentCornerDriver);
			
			new LoadManager(operations).start();
			
			Staller staller = new Staller();
			AsyncReader reader = new AsyncReader(this, drivers, staller, null); //staller will be notified
			reader.start();
	
			while(true)
			{	
				staller.stall(); //waits for a comment
				System.out.println("Stall Complete");
				
				commentsManager.work(reader.getComments());

				markCritiqueDriver.run();

				topDatabase.save();
				pDatabase.save();
				gDatabase.save();
				recorder.save();
				new Backup(BACKUP_FILENAME);
			}
		}
		catch(Exception e)
		{
			listener.catchException(e);
		}
	}
}

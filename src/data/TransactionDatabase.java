package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import event.PaymentEvents;
import scraper.WebScraper;
import sql.SQLDatabase;
import worker.NoteWriter;
import macro.Macro;

public class TransactionDatabase 
{
	private static final int MIN_STARTING_POINTS = 1; //Minimum number of points for a new payer to be enrolled
	private static final int MEMORY_SIZE = 100;
	
	private File transactionsFile;
	
	private ArrayList<Transaction> transactions = new ArrayList<Transaction>();

	private PayerDatabase pDatabase;
	private EarnerDatabase eDatabase;
	private GroupDatabase gDatabase;
	private RecordKeeper recorder;
	private GiveawayDatabase gaDatabase;
	private String myAccount;
	
	public TransactionDatabase(
			String filename, 
			PayerDatabase pDatabase, 
			EarnerDatabase eDatabase, 
			GroupDatabase gDatabase,
			GiveawayDatabase gaDatabase,
			PaymentEvents paymentEvents,
			NoteWriter noteWriter,
			RecordKeeper recorder, 
			String myAccount)
	{
		transactionsFile = new File(filename);
		this.pDatabase = pDatabase;
		this.eDatabase = eDatabase;
		this.gDatabase = gDatabase;
		this.recorder = recorder;
		this.myAccount = myAccount;
		this.gaDatabase = gaDatabase;
		readTransactions();
	}
	
	public void add(Transaction transaction)
	{
		if(transactions.contains(transaction))
		{
			return;
		}
		transactions.add(transaction);
		if(isValidTransaction(transaction, myAccount))
		{
			String message = transaction.getMessage();
			if(targetsGroup(message))
			{
				String groupName = gDatabase.parseGroupName(message);
				if(groupName != null)
				{
					gDatabase.processTransaction(transaction);
				}
			}
			else if(gaDatabase.processTransaction(transaction))
			{
				//function processes the transaction if its accepted
			}
			else
			{
				pDatabase.processTransaction(transaction);
			}
			recorder.addEvent(RecordKeeper.Event.PAYMENT_IN, transaction.toString(), transaction.getFullDate());
		}
	}
	private boolean targetsGroup(String message)
	{
		if(message.split(" ")[0].toLowerCase().contains(Group.getTag()))
		{
			String groupName = gDatabase.parseGroupName(message);
			if(groupName != null)
			{
				return WebScraper.isValid(new Artist(groupName).getHomeURL());
			}
		}
		return false;
	}
	
	private boolean isValidTransaction(Transaction transaction, String myAccount)
	{
		String origin = transaction.getOrigin().toString().toLowerCase();
		if(!origin.isEmpty() && !origin.equals(myAccount))
		{
			int amount = transaction.getAmount();
			if(amount >= MIN_STARTING_POINTS) //handles negative amounts
			{
				return true;
			}
		}
		return false;
	}
	
	public void save()
	{
		Macro.sleep(100);
		ArrayList<Transaction> updatedTransactions = new ArrayList<Transaction>();
		try
		{
			BufferedWriter buff = new BufferedWriter(new FileWriter(transactionsFile));
			for(int a = Math.max(0, transactions.size() - MEMORY_SIZE) ; a < transactions.size() ; a ++)
			{
				updatedTransactions.add(transactions.get(a));
				buff.write(transactions.get(a).toString());
				buff.newLine();
			}
			buff.close();
		}
		catch(IOException e)
		{
			System.err.println("Failed to write Transactions");
			System.exit(1);
		}
		transactions = updatedTransactions; //kept to a limit in size
	}
	private void readTransactions()
	{
		try
		{
			BufferedReader buff = new BufferedReader(new FileReader(transactionsFile));
			String line;
			while((line = buff.readLine()) != null)
			{
				transactions.add(new Transaction(line));
			}
			buff.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}

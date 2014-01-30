package driver;

import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnhandledAlertException;

import utils.StringOps;
import worker.LimitedFrequencyOperation;
import worker.Triggerable;
import data.Artist;
import data.Transaction;
import data.TransactionDatabase;
import macro.Macro;

public class TransactionDriver implements LimitedFrequencyOperation, Triggerable
{	
	public static void main(String[] args)
	{
		new TransactionDriver(new DriverPool(1, new Artist("datrade"), "minderbender"), null).doWork();
	}
	private TransactionDatabase tDatabase;
	private DriverPool pool;
	
	private long lastModified = 0L; 
	
	public TransactionDriver(DriverPool pool, TransactionDatabase tDatabase)
	{
		this.pool = pool;
		this.tDatabase = tDatabase;
	}
	
	private static final String TRANSACTIONS_PAGE = "http://www.deviantart.com/account/points/";
	//private static final String TRANSACTIONS_PAGE = "http://www.deviantart.com/account/points/?offset=180";
	
	private static final String TRANSACTION_LABEL = "<tr class=";
	private static final String NEW_LINE = "\\n";
	
	@Override
	public boolean shouldUpdate()
	{
		return System.currentTimeMillis() - lastModified > 1000 * 60 * 1; //at least a minute to weed out false repeaters
	}
	
	@Override
	public synchronized void doWork()
	{
		LoginDriver driver = pool.get("TransactionDriver");
		String myAccount = driver.getAccount();
		while(true)
		{
			try
			{
				driver.get(TRANSACTIONS_PAGE);
				break;
			}
			catch(UnhandledAlertException e)
			{
				try
				{
					driver.switchTo().alert().dismiss();
				} 
				catch(NoAlertPresentException alertError)
				{
					break;
				}
			}
		}
		Macro.sleep(100);
		String pageSource = driver.getPageSource();
		pool.put(driver);
		parsePageSource(pageSource, myAccount);
		tDatabase.save();
		lastModified = System.currentTimeMillis();
	}
	private void parsePageSource(String pageSource, String myAccount)
	{
		int index = 0;
		while((index = pageSource.indexOf(TRANSACTION_LABEL, index))>-1)
		{
			String transactionString = StringOps.textBetween(pageSource, TRANSACTION_LABEL, TRANSACTION_LABEL, index);
			if(transactionString == null)
			{
				break;
			}
			String[] splitByLine = transactionString.split(NEW_LINE);
			if(!splitByLine[3].contains("Made Order with"))
			{
				//System.out.println(parseTransaction(splitByLine));
				try
				{
					parseTransaction(splitByLine);
				}
				catch(Exception e)
				{
					parseTransaction(splitByLine);
				}
				tDatabase.add(parseTransaction(splitByLine));
			}
			index ++; //chop the string
		}
	}
	private static final String DATE_TAG = "<td nowrap=\"nowrap\" align=\"center\">";
	private static final String DATE_END = "</td>";
	private static final String TIME_TAG = "<td nowrap=\"nowrap\" align=\"center\">";
	private static final String TIME_END = "</td>";
	private static final String MEMO_TAG = "<strong>Memo:</strong> ";
	private static final String MEMO_END = "</div>";
	private static final String CREDIT_TAG = "class=\"order-amount credit\">";
	private static final String DEBIT_TAG = "class=\"order-amount debit\">";
	private static final String AMOUNT_END = "</td>";
	
	private Transaction parseTransaction(String[] splitByLine)
	{
		String date = StringOps.textBetween(splitByLine[1], DATE_TAG, DATE_END);
		String time = StringOps.textBetween(splitByLine[2], TIME_TAG, TIME_END);
		String NAME_TAG = "<a href=\"http://";
		String NAME_END = ".deviantart.com";
		String origin = StringOps.textBetween(splitByLine[3], NAME_TAG, NAME_END);//splitByLine[3].trim().split("<")[2].split(">")[1].trim();
		String dest = StringOps.textBetween(splitByLine[3].split(" to ")[1], NAME_TAG, NAME_END);//.trim().split("<")[4].split(">")[1].trim();
		String message = "";
		if(StringOps.textBetween(splitByLine[4], MEMO_TAG, MEMO_END) != null)
		{
			message = StringOps.textBetween(splitByLine[4], MEMO_TAG, MEMO_END).trim().replace("<br />", "");
		}
		else
		{
			splitByLine[5] = splitByLine[4]; //missing memo
		}
		String amount;
		if(splitByLine[5].contains(CREDIT_TAG))
		{
			amount = StringOps.textBetween(splitByLine[5], CREDIT_TAG, AMOUNT_END);
		}
		else
		{
			amount = StringOps.textBetween(splitByLine[5], DEBIT_TAG, AMOUNT_END);
		}
		return new Transaction(date, time, origin, dest, message, Integer.parseInt(amount));
	}
	
	@Override
	public String toString()
	{
		return TransactionDriver.class.getName();
	}

	@Override
	public void trigger() 
	{
		doWork();
	}
}

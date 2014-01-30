package bryan;

import jane.HistoryDatabase;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import utils.Append;
import worker.LimitedFrequencyOperation;
import data.Currency;
import data.EarnerDatabase;
import data.GroupDatabase;
import data.PayerDatabase;
import data.Refundable;

/** Not threading-optimized for speed */
public class Bryan implements LimitedFrequencyOperation
{	
	public static void main(String[] main)
	{
		//new Bryan(new PayerDatabase(null, null, null), new EarnerDatabase(null, null, null), new GroupDatabase(null, null), new HistoryDatabase()).doWork();
	}
	private PayerDatabase pDatabase;
	private GroupDatabase gDatabase;
	private EarnerDatabase eDatabase;
	private HistoryDatabase hDatabase;
	
	private long lastModified = 0L; 
	
	public Bryan(PayerDatabase pDatabase, EarnerDatabase eDatabase, GroupDatabase gDatabase, HistoryDatabase hDatabase)
	{
		this.pDatabase = pDatabase;
		this.eDatabase = eDatabase;
		this.gDatabase = gDatabase;
		this.hDatabase = hDatabase;
	}
	@Override
	public void doWork()
	{
		lastModified = System.currentTimeMillis();
		Set<Refundable> allRefundables = new HashSet<Refundable>();
		allRefundables.addAll(pDatabase.getPayers());
		//allRefundables.addAll(gDatabase.getGroups());
		for(Refundable refundable : allRefundables)
		{
			Set<String> missingWatchers = hDatabase.missingWatchers(refundable.getName(), new WatchList(refundable.getName()));
			for(String missing : missingWatchers)
			{
				missing = missing.toLowerCase();
				if(eDatabase.earnerInDatabase(missing))
				{
					if(eDatabase.get(missing).watches(refundable.getName()))
					{
						//System.out.println(missing+" unwatched "+refundable.getName());
						refundable.refund(Currency.WATCH);
						eDatabase.get(missing).markUnwatch(refundable.getName());
						Append.toFile(new File("data\\unwatchers.txt"), missing+" withdrew from "+refundable.getName());
					}
				}
			}
		}
	}
	@Override
	public boolean shouldUpdate() 
	{
		return System.currentTimeMillis() - lastModified > 1000 * 60 * 30;
	}
}

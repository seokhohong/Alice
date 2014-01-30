package scraper;

import java.util.ArrayList;
import java.util.Iterator;

import worker.LimitedFrequencyOperation;
import worker.Multitasker;
import main.Listener;
import data.Critique;
import data.EarnerDatabase;
import data.Payer;
import data.PayerDatabase;
import data.RecordKeeper;

public class AcceptanceVerifier extends Multitasker implements LimitedFrequencyOperation
{
	private PayerDatabase pDatabase;
	private EarnerDatabase eDatabase;
	private RecordKeeper recorder;
	private Listener listener;
	
	private long lastModified = 0L;
	
	public AcceptanceVerifier(PayerDatabase pDatabase, EarnerDatabase eDatabase, RecordKeeper recorder, Listener listener)
	{
		this.pDatabase = pDatabase;
		this.eDatabase = eDatabase;
		this.recorder = recorder;
		this.listener = listener;
	}
	
	@Override
	public boolean shouldUpdate()
	{
		boolean shouldUpdate = System.currentTimeMillis() - lastModified > 1000 * 60 * 1;
		lastModified = System.currentTimeMillis();
		return shouldUpdate;
	}

	@Override
	public void doWork()
	{
		for(Payer payer : pDatabase.getPayers())
		{
			ArrayList<Critique> pendings = new ArrayList<Critique>();
			pendings.addAll(payer.getPendingCritiques());
			for(Critique pending : pendings)
			{
				//check the artwork page to see if the critique was accepted
				//System.out.println("Checking status of "+pending);
				CritiqueScraper newScraperThread = new CritiqueScraper(pDatabase, eDatabase, pending, recorder, listener);
				load(newScraperThread);
			}
		}
	}
}

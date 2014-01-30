package scraper;

import worker.Multitasker;
import data.Currency;
import data.Earner;
import data.EarnerDatabase;
import data.Group;
import data.GroupDatabase;
import data.Payer;
import data.PayerDatabase;
import data.ToppersDatabase;

public class EarnerVerifier
{
	private PayerDatabase pDatabase;
	private EarnerDatabase eDatabase;
	private GroupDatabase gDatabase;
	private ToppersDatabase topDatabase;
	
	public EarnerVerifier(PayerDatabase pDatabase, EarnerDatabase eDatabase, GroupDatabase gDatabase, ToppersDatabase topDatabase)
	{
		this.pDatabase = pDatabase;
		this.eDatabase = eDatabase;
		this.gDatabase = gDatabase;
		this.topDatabase = topDatabase;
		run();
	}
	
	private static final int FAVS_PER_PAGE = 120;
	private static final int MAX_FAV_SEARCH = 240;
	private static final int LLAMA_SEARCH = 50;
	private static final int LLAMAS_PER_PAGE = 5;
	
	public void run()
	{
		runInitScan();
		runScan();
		System.out.println("Finished all Verifier threads");
	}
	
	private void runInitScan()
	{
		Multitasker propsVerifier = new Multitasker();
		loadPropertiesThreads(propsVerifier);
		propsVerifier.done();
	}
	
	private void runScan()
	{
		Multitasker verifier = new Multitasker(17);
		Multitasker listVerifier = new Multitasker(10);
		
		loadCommentHistoryThreads(verifier, listVerifier);
		loadFavThreads(verifier);
		loadCritiqueThreads(verifier);
		loadWatchThreads(verifier);
		loadLlamaThreads(verifier);
		loadEnrollmentThreads(verifier);
		loadGroupWatchThreads(verifier);
		listVerifier.done();
		verifier.done();
	}
	
	private void loadPropertiesThreads(Multitasker verifier)
	{
		for(Earner earner : eDatabase.getEarners())
		{
			verifier.load(new ArtistPropertiesScraper(earner));
		}
	}
	
	private static final int CRITIQUES_TO_SEARCH = 30;
	private static final int CRITIQUES_PER_PAGE = 6;
	private void loadCritiqueThreads(Multitasker verifier)
	{
		for(Earner earner : eDatabase.getEarners())
		{
			for(int a = 0; a < CRITIQUES_TO_SEARCH; a+= CRITIQUES_PER_PAGE)
			{
				verifier.load(new CritiqueListScraper(earner, pDatabase, a));
			}
		}
	}
	private void loadCommentHistoryThreads(Multitasker mainVerifier, Multitasker listVerifier)
	{
		for(Earner earner : eDatabase.getEarners())
		{
			listVerifier.load(new CommentsListScraper(pDatabase, topDatabase, earner, mainVerifier));
		}
	}
	private void loadWatchThreads(Multitasker verifier)
	{
		if(eDatabase.isEmpty())
		{
			return;
		}
		for(Payer payer : pDatabase.getPayers())
		{
			if(payer.accepts(Currency.WATCH))
			{
				verifier.load(new WatchScraper(pDatabase, eDatabase, payer));
			}
		}
	}
	private void loadLlamaThreads(Multitasker verifier)
	{
		for(Earner earner : eDatabase.getEarners())
		{
			for(int offset = 0; offset < LLAMA_SEARCH; offset+=LLAMAS_PER_PAGE)
			{
				verifier.load(new LlamaWorkScraper(pDatabase, earner, offset));
			}
		}
	}
	
	private void loadFavThreads(Multitasker verifier)
	{
		for(Earner earner : eDatabase.getEarners())
		{
			for(int offset = 0; offset < MAX_FAV_SEARCH; offset+=FAVS_PER_PAGE)
			{
				verifier.load(new FavScraper(pDatabase, earner, offset));
			}
		}
	}
	
	private void loadEnrollmentThreads(Multitasker verifier)
	{
		for(Group group : gDatabase.getGroups())
		{
			verifier.load(new GroupEnrollmentScraper(eDatabase, gDatabase, group));
		}
	}
	
	private void loadGroupWatchThreads(Multitasker verifier)
	{
		for(Group group : gDatabase.getGroups())
		{
			//verifier.load(new GroupWatchScraper(gDatabase, eDatabase, group));
		}
	}
}

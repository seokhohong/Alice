package scraper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import utils.StringOps;
import utils.Write;
import worker.LimitedFrequencyOperation;
import worker.Multitasker;
import worker.NoteWriter;
import data.Artist;
import data.PayerDatabase;

/** Checks if the critiques displayed on the profile page are open to be critiqued */
public class ClosedArtworkChecker implements LimitedFrequencyOperation 
{
	public static void main(String[] args)
	{
		//new ClosedArtworkChecker(new Artist("datrade"), null, null).new CritiqueAvailableChecker("http://bloomingrosexeniia.deviantart.com/art/Mother-Of-Dragons-393006676").start();
		//new ClosedArtworkChecker(new Artist("datrade"), null, null).new CritiqueAvailableChecker("http://razielmb.deviantart.com/art/Lady-Dragon-378848667").start();
		new ClosedArtworkChecker(new Artist("datrade"), null, null).new CritiqueAvailableChecker("http://xinfinitivelove.deviantart.com/art/WIP-FMD-Entry-manip-402097535").start();
		//new ClosedArtworkChecker(new Artist("datrade"), null, null).doWork();
	}
	private static final String CRITIQUE_SECTION_HEADER = "Critique Me";
	private static final String CRITIQUE_SECTION_END = "icon i26";
	private static final String LINK_TAG = "href=\"";
	private static final String LINK_END = "\"";
	private static final String ART_LINK_INDICATOR = "/art/";
	private static final String NAME_TAG = "http://";
	private static final String NAME_END = ".";
	private static final String ID_DIVIDER = "-";
	
	private Artist alice;
	private NoteWriter noteWriter;
	private PayerDatabase pDatabase;
	
	private Map<String, ArrayList<Integer>> closedCritiques = new HashMap<String, ArrayList<Integer>>();
	private long lastUpdateTime = 0L;
	
	public ClosedArtworkChecker(Artist alice, NoteWriter noteWriter, PayerDatabase pDatabase)
	{
		this.alice = alice;
		this.noteWriter = noteWriter;
		this.pDatabase = pDatabase;
	}
	@Override
	public boolean shouldUpdate()
	{
		return System.currentTimeMillis() - lastUpdateTime > 1000 * 60 * 60;
	}
	@Override
	public synchronized void doWork()
	{
		lastUpdateTime = System.currentTimeMillis();
		Multitasker critiqueVerifier = new Multitasker();
		String profile = WebScraper.get(alice.getHomeURL());
		String critiqueMe = StringOps.textBetween(profile, CRITIQUE_SECTION_HEADER, CRITIQUE_SECTION_END);
		int index = 0;
		while((index = critiqueMe.indexOf(LINK_TAG, index)) != -1)
		{
			String url = StringOps.textBetween(critiqueMe, LINK_TAG, LINK_END, index);
			if(url.contains(ART_LINK_INDICATOR))
			{
				critiqueVerifier.load(new CritiqueAvailableChecker(url));
			}
			index++;
		}
		critiqueVerifier.done();
		handleClosedCritiques();
	}
	private void handleClosedCritiques()
	{
		for(String name : closedCritiques.keySet())
		{
			System.out.println("Notifying "+name+" of closed Critique Artworks");
			if(pDatabase.has(name))
			{
				pDatabase.get(name).removeArtworks(closedCritiques.get(name));
				noteWriter.send(name, "Your Artworks for Critiquing", warningString());
			}
		}
	}
	private String warningString()
	{
		return "One or more of your artworks are not open for critiques! Please open and resubmit them.\n\n - automated by Program Alice";
	}
	class CritiqueAvailableChecker extends Thread
	{
		String url;
		CritiqueAvailableChecker(String url)
		{
			this.url = url;
		}
		@Override
		public void run()
		{
			String checkArtwork = WebScraper.get(url);
			Write.toFile(new File("critiqueSource.txt"), checkArtwork);
			boolean loadedProperly = checkArtwork.contains("Add a Comment:");
			boolean isOpen = checkArtwork.contains("The Artist has requested Critique on this Artwork");
			if(loadedProperly && !isOpen)
			{
				addClosedCritique(url);
			}
		}
		private synchronized void addClosedCritique(String url)
		{
			int artworkId = 0;
			try
			{
				artworkId = Integer.parseInt(url.substring(url.lastIndexOf(ID_DIVIDER) + ID_DIVIDER.length()));
			} catch(NumberFormatException thisShouldNotHappen) { return; }
			String name = StringOps.textBetween(url, NAME_TAG, NAME_END);
			
			if(!closedCritiques.containsKey(name))
			{
				System.out.println(url+" is closed");
				closedCritiques.put(name, new ArrayList<Integer>());
			}
			closedCritiques.get(name).add(artworkId);
		}
	}
}

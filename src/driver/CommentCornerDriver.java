package driver;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import macro.Macro;
import main.Listener;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import data.Artist;
import data.Currency;
import data.PayerDatabase;
import scraper.GalleryScraper;
import scraper.GalleryScraper.Artwork;
import utils.StringOps;
import worker.NoteWriter;

public class CommentCornerDriver extends GroupUpdateDriver
{
	public static void main(String[] args)
	{
		/*
		try {
			System.out.println(new CommentCornerDriver(new Artist("theexhibition"), new DriverPool(1, new Artist("datrade"), "minderbender"), null, null, null).getDisplayText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		new CommentCornerDriver(new Artist("theexhibition"), new DriverPool(1, new Artist("datrade"), "minderbender"), null, null, null).doWork();
	}
	
	private static final String MSG_HEADER = "Gallery Submission to ";
	private static final String GROUP_MSG = "http://theexhibition.deviantart.com/messages/?log_type=1&instigator_module_type=0&instigator_roleid=3723823&instigator_username=&bpp_status=3&display_order=desc";
	private static final String GROUP_FAVS = "http://theexhibition.deviantart.com/favourites/";
	private static final String FOLDER_TAG = "<a href=\"http://theexhibition.deviantart.com/favourites/";
	private static final String NAME_TAG = "username=\"";
	private static final String THUMB_TAG = "collect_rid=\"1:";
	private static final String COMMENTS_CATEGORY = "Comments Corner";
	
	private static final List<String> CATEGORIES = Arrays.asList("literature", "photography", "traditional", "digitalart", "manga");
	
	private static final File WIDGET_FILE = new File("data\\commentsCorner.txt");
	
	private PayerDatabase pDatabase;
	private NoteWriter noteWriter;
	
	public CommentCornerDriver(Artist artist, DriverPool pool, PayerDatabase pDatabase, NoteWriter noteWriter, Listener listener)
	{
		super(artist, pool, "Comments Corner", listener);
		this.pDatabase = pDatabase;
		this.noteWriter = noteWriter;
	}

	@Override
	public synchronized void doWork() 
	{
		//acceptProposals();
		super.doWork();
	}
	
	@Override
	String getDisplayText() throws IOException 
	{
		Map<String, List<Artwork>> works = new GalleryScraper("http://theexhibition.deviantart.com/gallery/?set=46878004").getAll();
		prune(works);
		Map<String, String> replace = new HashMap<String, String>();
		replace.put("PutDigitalHere", appendCategory(works, "digitalart"));
		replace.put("PutTraditionalHere", appendCategory(works, "traditional"));
		replace.put("PutPhotographyHere", appendCategory(works, "photography"));
		replace.put("PutLiteratureHere", appendCategory(works, "literature"));
		replace.put("PutMangaHere", appendCategory(works, "manga"));
		replace.put("PutOtherHere", appendCategory(works, "Other"));
		return StringOps.assembleDisplay(WIDGET_FILE, replace);
	}
	
	//Remove extra artists and artworks
	private void prune(Map<String, List<Artwork>> works)
	{
		Iterator<String> iter = works.keySet().iterator();
		while(iter.hasNext())
		{
			String artist = iter.next();
			if(!pDatabase.has(artist) || !pDatabase.get(artist).accepts(Currency.COMMENT))
			{
				iter.remove();
			}
		}
	}
	
	private String appendCategory(Map<String, List<Artwork>> works, String category)
	{
		StringBuilder builder = new StringBuilder();
		for(List<Artwork> value : works.values())
		{
			for(Artwork artwork : value)
			{
				if(artwork.getCategory().equals(category))
				{
					builder.append(":thumb"+artwork.getThumb()+":");
				}
				else if(category.equals("Other") && !CATEGORIES.contains(artwork.getCategory()))
				{
					builder.append(":thumb"+artwork.getThumb()+":");
				}
			}
		}
		return builder.toString();
	}

	@Override
	public int minUpdateDelay() 
	{
		return 1000 * 60;
	}
	
	private void acceptProposals()
	{
		LoginDriver driver = pool.get("Accept Proposals");
		Set<RejectedEntry> rejects = new HashSet<>();
		while(true)
		{
			driver.get(GROUP_MSG);
			driver.navigate().refresh();
			List<WebElement> nameElems = driver.fillCssElements("span.tt-w > small > span.username-with-symbol");
			List<WebElement> commentLinks = driver.findElements(By.cssSelector("div.vote-float > a.a"));
			List<WebElement> titleElems = driver.findElements(By.className("mcb-line"));
			List<WebElement> category = driver.findElements(By.cssSelector("span.mcb-line > strong"));
			boolean didWork = false;
			
			for(int a = 0; a < Math.min(titleElems.size(), nameElems.size()); a ++)
			{
				String title = titleElems.get(a).getText();
				String artistName = nameElems.get(a).getText().toLowerCase();
				if(title.contains(MSG_HEADER) && category.get(a).getText().equals(COMMENTS_CATEGORY))
				{
					commentLinks.get(a).click();
					processSingleArtwork(driver, title, artistName, rejects);
					didWork = true; //there was something to do
					Macro.sleep(1000);
					break; //next one
				}
			}
			if(!didWork) //nothing more to do
			{
				informRejects(rejects);
				break;
			}
		}
		pool.put(driver);
	}
	private void processSingleArtwork(LoginDriver driver, String title, String artistName, Set<RejectedEntry> rejects)
	{
		List<WebElement> thumbElems = new ArrayList<WebElement>();
		while(thumbElems.isEmpty())
		{
			thumbElems.addAll(driver.findElements(By.cssSelector("span.shadow > a.thumb")));
		}
		String href = thumbElems.get(0).getAttribute("href");
		String artworkTitle = thumbElems.get(0).getAttribute("title");
		//chop the by-line on the artwork title
		artworkTitle = artworkTitle.substring(0, artworkTitle.indexOf(" by "));
		String thumb = href.substring(href.lastIndexOf("-") + 1);
		String category = title.substring(MSG_HEADER.length());
		String submitterName = driver.fillCssElements("span.mcb-ts > span.username-with-symbol > a.username").get(0).getText().toLowerCase();
		if(submitterName.equals(artistName))
		{
			if(pDatabase.has(artistName) && pDatabase.get(artistName).accepts(Currency.COMMENT))
			{
				driver.findElement(By.cssSelector("div.vote-float > a.gmvoteyes")).click();
			}
			else
			{
				//reject and inform
				driver.findElement(By.cssSelector("div.vote-float > a.gmvoteno")).click();
				rejects.add(new RejectedEntry(submitterName, artworkTitle));
			}
		}
		else
		{
			//just reject
			driver.findElement(By.cssSelector("div.vote-float > a.gmvoteno")).click();
		}
	}
	private void informRejects(Set<RejectedEntry> rejects)
	{
		Map<String, List<String>> rejectMap = new HashMap<>();
		for(RejectedEntry entry : rejects)
		{
			if(rejectMap.containsKey(entry.artist))
			{
				rejectMap.get(entry.artist).add(entry.artwork);
			}
			else
			{
				List<String> rejectedArtworks = new ArrayList<>();
				rejectedArtworks.add(entry.artwork);
				rejectMap.put(entry.artist, rejectedArtworks);
			}
		}
		for(String rejectedArtist : rejectMap.keySet())
		{
			String submission = rejectMap.get(rejectedArtist).size() == 1 ? "submission was" : "submissions were";
			noteWriter.send(rejectedArtist, "Your "+submission+" rejected!", rejectionMessage());
		}
	}
	private String rejectionMessage()
	{
		return "I'm sorry, but submissions to the Favorites folder are only for having your artwork featured on :devtheexhibition:'s front page for comments.\n\n"+
		"In order to have your artwork featured to receive comments, you must first donate points to :devdatrade:. For more information, please refer to the <a href=\"http://fav.me/d6hdkvu\">Comment Guidelines</a> journal.";
	}
	class RejectedEntry
	{
		private String artist;
		private String artwork;
		private RejectedEntry(String artist, String artwork)
		{
			this.artist = artist;
			this.artwork = artwork;
		}
	}
	/*
	private void clearOut()
	{
		String pageSource = WebScraper.get("http://theexhibition.deviantart.com/favourites/");
		//parse out all folder links
		List<Integer> indices = ScrapingUtils.findAll(pageSource, FOLDER_TAG);
		Set<String> folderLinks = new HashSet<>(); //there are duplicate links on the page
		for(int index : indices)
		{
			folderLinks.add(ScrapingUtils.textBetween(pageSource, FOLDER_TAG, "\"", index));
		}
		for(String link : folderLinks)
		{
			System.out.println(link);
		}
	}
	private void clearFolder(int linkNum, int offset)
	{
		String pageSource = WebScraper.get("http://theexhibition.deviantart.com/favourites/?set="+linkNum+"&offset="+offset);
		for(int index : ScrapingUtils.findAll(pageSource, NAME_TAG))
		{
			System.out.println("Artist "+ScrapingUtils.textBetween(pageSource, "username=\"", "\"", index));
		}
	}
	private void cleanArtists()
	{
		LoginDriver driver = pool.get("Clean Artists");
		for(String url : urlsToClean)
		{
			driver.get(url);

			String pageSource = driver.getPageSource();
			List<Integer> indices = ScrapingUtils.findAll(pageSource, NAME_TAG);
			
			List<WebElement> allShadows = new ArrayList<>();
			while(allShadows.isEmpty())
			{
				allShadows.addAll(driver.findElements(By.cssSelector("div.folderview-art > div > div > span > span > span.shadow")));
			}

			for(int a = indices.size(); a --> 0; )
			{
				//If I'm supposed to clean out this artist
				if(artistsToClean.contains(ScrapingUtils.textBetween(pageSource, "username=\"", "\"", indices.get(a))))
				{
					WebElement target = driver.findElement(By.xpath("(//a[contains(text(),'Menu')])["+a+"]"));
					Actions actions = new Actions(driver);
					actions.moveToElement(allShadows.get(a - 1));
					//actions.dragAndDrop(allShadows.get(a), allShadows.get(a));
					actions.moveToElement(target);
					actions.perform();
					target.click();
					String js = "arguments[0].style.height='auto'; arguments[0].style.visibility='visible';";
					JavascriptExecutor jsE = (JavascriptExecutor) driver;
					jsE.executeScript(js, target);
					target.click();
					System.out.println();
					//System.out.println();
					
					//Selenium selenium = new WebDriverBackedSelenium(driver, url);
					//selenium.clickAt("xpath=(//a[contains(text(),'Menu')])["+a+"]", "0,0");
					
					Actions actions = new Actions(driver);
					actions.moveToElement(allShadows.get(a));
					Macro.sleep(500);
					target.click();
					
				}
			}
		}
		pool.put(driver);
	}
	*/
}

class ArtworkEntry implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8905964597181558072L;
	private String artist;			public String getArtist() { return artist; }
	//just a number					
	private String thumb;  			public String getThumb()  { return thumb; }
	private String category;		public String getCategory() { return category; }
	//artwork name
	private String artwork;			public String getArtwork() { return artwork; }
	ArtworkEntry(String artist, String thumb, String category, String artwork)
	{
		this.artist = artist;
		this.thumb = thumb;
		this.category = category;
		this.artwork = artwork;
	}
}
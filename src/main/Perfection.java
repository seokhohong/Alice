package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import macro.Macro;
import scraper.WebScraper;
import utils.StringOps;
import data.Earner;
import driver.LoginDriver;

public class Perfection 
{
	public static void main(String[] args)
	{
		new Perfection().scrapeGroups();
	}
	
	private void scrapeGroups()
	{
		String pageSource = WebScraper.get("http://datrade.deviantart.com/mygroups/");
		System.out.println(pageSource);
		int index = 0;
		String NAME_TAG = "<a target=\"_self\" href=\"http://";
		while((index = pageSource.indexOf(NAME_TAG, index)) != -1)
		{
			String groupName = StringOps.textBetween(pageSource, NAME_TAG, ".", index);
			System.out.println(groupName);
			index ++;
		}

	}
	private void openProxyUrl()
	{
		try
		{
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("202.51.102.34", 80));
			HttpURLConnection connection = (HttpURLConnection)new URL("http://datrade.deviantart.com/friends/").openConnection(proxy);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-type", "text/xml");
			connection.setRequestProperty("Accept", "text/xml, application/xml");
			connection.setRequestMethod("POST");
			System.out.println("Connecting");
			connection.connect();
			System.out.println("Connected");
			BufferedReader in = new BufferedReader(
					new InputStreamReader((InputStream) connection.getContent()));
			System.out.println("Retrieved Contents");
			String inputLine = "";
			StringBuilder builder = new StringBuilder(inputLine);
	
			while ((inputLine = in.readLine()) != null)
			{
				System.out.println(inputLine);
				builder.append(inputLine);
			}
			System.out.println(builder.toString());
		} 
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private void getProxies()
	{
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("http://hidemyass.com/proxy-list/");
		//WebElement webPage = driver.findElement(By.id("subpagebgtabs"));
		WebElement body = driver.findElement(By.cssSelector("body"));
		body.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		body.sendKeys(Keys.chord(Keys.CONTROL, "c"));
		ArrayList<Proxy> proxies = parseProxies(Macro.getClipboardContents());
		
		//webPage.sendKeys();
	}
	private ArrayList<Proxy> parseProxies(String pageSource)
	{
		ArrayList<Proxy> proxies = new ArrayList<Proxy>();
		pageSource = StringOps.textBetween(pageSource, "Connection time", "Previous");
	 	int index = 0;
	 	while((index = pageSource.indexOf(".", index)) != -1)
	 	{
	 		String ip = pageSource.substring(index - 3, index + 11).trim();
	 		String port = StringOps.textBetween(pageSource, ip, "flag");
	 		index += ip.length();
	 		String typeString = pageSource.substring(index, pageSource.indexOf(".", index));
	 		//use only HTTP proxies
	 		if(typeString.contains("HTTP"))
	 		{
	 			System.out.println(ip+":"+port);
	 			proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, Integer.parseInt(port))));
	 		}
	 	}
		return proxies;
	}
	/*
	private void verifyCritiquable()
	{
		String profile = WebScraper.get("http://datrade.deviantart.com/");
		String critiqueMe = StringOps.textBetween(profile, "Critique Me", "icon i26");
		int index = 0;
		while((index = critiqueMe.indexOf("href=\"", index)) != -1)
		{
			String url = StringOps.textBetween(critiqueMe, "href=\"", "\"", index);
			if(url.contains("/art/"))
			{
				new CritiqueAvailableChecker(url).start();
			}
			index++;
		}
	}
	class CritiqueAvailableChecker extends Thread
	{
		String url;
		CritiqueAvailableChecker(String url)
		{
			this.url = url;
		}
		public void run()
		{
			String checkArtwork = WebScraper.get(url);
			boolean isOpen = checkArtwork.contains("The Artist has requested Critique on this Artwork");
			System.out.println(url+" is Open: "+isOpen);
		}
	}
	private void replyIsolatedComment()
	{
		LoginDriver driver = null;
		try
		{
			driver = new LoginDriver(new Artist("animelovers21"), "qazwsx1");	
		} catch(Exception e) {}
		driver.get("http://comments.deviantart.com/4/22627381/3208093512");
		//driver.findElement(By.cssSelector("h2.c")).click();
		//driver.blast("testing", By.xpath("//*[contains(@id, 'w')]"));
		driver.blast("testing", By.id("commentbody"));
		driver.findElement(By.id("commentbody")).click();
		//driver.findElement(By.cssSelector("span.post")).click();
	}
	private void replyOnProfile()
	{
		LoginDriver driver = null;
		Artist artist = new Artist("animelovers21");
		ArrayList<String> source = new ArrayList<String>();
		
		try
		{
			//driver = new LoginDriver(artist, "qazwsx1");	
		} catch(Exception e) {}
		//driver.get(artist.getHomeURL());
		//source.add(driver.getPageSource());
		//Write.toFile(new File("source.txt"), source);
		//source.clear();
		String scraped = WebScraper.get(artist.getHomeURL());
		source.add(scraped);
		Write.toFile(new File("sourceScrape.txt"), source);
		int index = 0;
		String nameTagFirst = "<span class=\"cc-name\">";
		int gap = 1;
		String nameTagSecond = "<a class=\"u\" href=\"http://";
		String commentTag = "<div class=\"text text-ii\">";
		String commentsUrlPrefix = "http://comments.deviantart.com/4/";
		//System.out.println(scraped);
		int numReplies = 0;
		while((index = scraped.indexOf(nameTagFirst, index))!=-1)
		{
			if(scraped.indexOf(nameTagSecond, index) == index + nameTagFirst.length() + gap)
			{
				if(scraped.substring(index - 1000, index).indexOf("nest")==-1)
				{
					System.out.println(StringOps.textBetween(scraped, nameTagSecond, ".", index));
					System.out.println("\t"+StringOps.textBetween(scraped, commentTag, "</div>", index));
					//int commentUrlIndex = scraped.indexOf("href=\"http://comments.deviantart.com/4", index) + 6;
					System.out.println(commentsUrlPrefix+StringOps.textBetween(scraped, "href=\""+commentsUrlPrefix, "\">", index));
				}
				else
				{
					System.out.println("(Reply)");
				}
				numReplies++;
			}
			index++;
		}
		/*
		while(true)
		{
			List<WebElement> elements = driver.findElements(By.linkText("Reply"));
			if(elements.size() >= numReplies)
			{
				System.out.println("FoundAll");
			}
		}
		//
	}
	private void replyComment()
	{
		LoginDriver driver = null;
		try
		{
			driver = new LoginDriver(new Artist("datrade"), "minderbender");	
		} catch(Exception e) {}
		driver.get("http://www.deviantart.com/messages/#view=comments%3A4%3A27788959");
		while(true)
		{
			List<WebElement> replies = findReplies(driver);
			replies.get(0).click();
			By textArea = By.xpath("//*[contains(@id, '-w')]");
			driver.waitFor(textArea);
			driver.blast(big(), textArea);
			Macro.sleep(2000);
			driver.navigate().refresh();
		}
	}
	public String big()
	{
		StringBuilder builder = new StringBuilder();
		for(int a = 1000 ; a --> 0 ;)
		{
			builder.append("testing");
		}
		return builder.toString();
	}
	public List<WebElement> findReplies(LoginDriver driver)
	{
		List<WebElement> replies = new ArrayList<WebElement>();
		Timer maxReplyTimer = new Timer(2000);
		while(replies.isEmpty() && maxReplyTimer.stillWaiting())
		{
			try
			{
				replies = driver.findElements(By.linkText("Reply"));
				Macro.sleep(100);
			} 
			catch(NoSuchElementException e) {}
		}
		return replies;
	}
	private void writeNote()
	{
		String NOTE_URL = "http://www.deviantart.com/messages/notes/#1_0";
		By SUBJECT_ELEM = By.xpath("//div[@id='notes']/table/tbody/tr/td[2]/table/tbody/tr/td[2]/div[3]/div/div[2]/div/div/form/div/div[2]/input");
		
		By NEW_NOTE_ELEM = By.linkText("Create New Note");
		LoginDriver driver = null;
		try
		{
			driver = new LoginDriver(new Earner("datrade"), "minderbender");
		} catch(Exception e) {}
		while(true)
		{
			driver.get(NOTE_URL);
			try
			{
				WebElement newNote = driver.waitFor(NEW_NOTE_ELEM);
				newNote.click();
			} catch(Exception e) {  }
			Macro.sleep(2000);
			for(int a = 20; a --> 0; )
			{
				driver.blast("testing", By.xpath("//input["+a+"]"));
			}
			WebElement subjectElem = driver.findElement(SUBJECT_ELEM);
			subjectElem.clear();
			subjectElem.click();
			Macro.typeIntoBrowser("subject", subjectElem);
			//driver.blast("testing", By.xpath("//*[contains(@id, 'l_')]"));
			Macro.sleep(2000);
		}
	}
	*/
	private void markCritique()
	{
		try
		{
			//DriverPool pool = new DriverPool(1, new Earner("datrade"), "minderbender");
			//new ReplyDriver(pool, new Reply("http://immakai1.deviantart.com/critique/846506495/", "testing")).start();
			/*
			LoginDriver driver = new LoginDriver(new Earner("datrade"), "minderbender");
			driver.get("http://pritthish.deviantart.com/critique/845650627/");
			By writerBy = By.xpath("//*[contains(@id, 'w')]");
			while(true)
			{
				driver.waitFor(By.id("commentbody")).click();
				driver.waitFor(writerBy);
				driver.blastObfuscated("test", writerBy);
				Macro.sleep(1000);
				driver.navigate().refresh();
			}
			*/
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

package driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import macro.Macro;
import macro.Timer;
import main.Listener;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import scraper.WebScraper;
import worker.LimitedFrequencyOperation;
import data.Artist;

public abstract class ProfileUpdateDriver implements LimitedFrequencyOperation
{
	private static final By EDIT_LINK = By.linkText("Edit this Widget");
	private static final By TEXT_AREA = By.name("customtext");
	private static final By SAVE_BUTTON = By.linkText("Save");
	
	protected Artist artist;
	protected DriverPool pool;
	protected Listener listener;
	
	private String widgetTitle;
	private long lastUpdateTime = 0L;
	
	public ProfileUpdateDriver(Artist artist, DriverPool pool, String widgetTitle, Listener listener)
	{
		this.widgetTitle = widgetTitle;
		this.artist = artist;
		this.pool = pool;
	}
	@Override
	public boolean shouldUpdate()
	{
		return System.currentTimeMillis() - lastUpdateTime > minUpdateDelay();
	}
	abstract String getDisplayText() throws IOException;
	public abstract int minUpdateDelay();
	
	@Override
	public synchronized void doWork()
	{
		LoginDriver driver = pool.get(toString());
		lastUpdateTime = System.currentTimeMillis();
		try
		{
			openWidget(driver);
			WebElement textArea = driver.waitFor(TEXT_AREA);
			textArea.clear();
			//Copy and paste text
			Macro.pasteIntoBrowser(getDisplayText(), textArea);
		} 
		catch(Exception e) 
		{
			pool.put(driver);
			return; 
		}
		
		driver.waitFor(SAVE_BUTTON).click();
		Macro.sleep(1000);
		pool.put(driver);
	}
	private static final String BUTTON_TAG = "name=\"gmi-GMFrame_Gruser\"";
	private By getEditWidgetElem(String widgetTitle)
	{
		String pageSource = WebScraper.get(artist.getHomeURL(), false);
		int titleIndex = pageSource.indexOf(widgetTitle);
		int buttonIndex = 0;
		int numButtons = 1;
		while((buttonIndex = pageSource.indexOf(BUTTON_TAG, buttonIndex)) < titleIndex)
		{
			buttonIndex++;
			numButtons++;
		}
		return By.xpath("(//a[contains(text(),'Edit')])["+numButtons+"]");
	}
	private static final int MAX_OPEN_WIDGET_TIME = 60000;
	private void openWidget(LoginDriver driver) throws Exception
	{
		while(true)
		{
			Timer timer = new Timer(MAX_OPEN_WIDGET_TIME);
			try
			{
				driver.get(artist.getHomeURL());
				List<WebElement> widgetTitles = new ArrayList<>();
				while(widgetTitles.isEmpty())
				{
					widgetTitles = driver.findElements(By.cssSelector("div > div > div > div > h2"));
				}
				for(int a = widgetTitles.size(); a --> 0; )
				{
					if(widgetTitles.get(a).getText().equals(widgetTitle))
					{
						driver.waitFor(By.xpath("(//a[contains(text(),'Edit')])["+(a + getEditOffset())+"]")).click();
						break;
					}
				}
				WebElement editLink = driver.waitFor(EDIT_LINK);
				if(editLink == null)
				{
					continue;
				}
				editLink.click();
				break;
			}
			catch(Exception e) 
			{
				e.printStackTrace();
			}
			if(timer.hasExpired())
			{
				throw new Exception("Couldn't open Widget");
			}
		}
	}
	//Returns the offset of the edit buttons
	int getEditOffset()
	{
		return 1;
	}
}

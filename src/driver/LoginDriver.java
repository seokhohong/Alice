package driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import macro.Macro;
import macro.Timer;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.*;

import data.Artist;

public class LoginDriver extends FirefoxDriver
{
	private static final String LOGIN_URL = "https://www.deviantart.com/users/login";
	private static final By LOGIN_USERNAME = By.id("login_username");
	private static final By LOGIN_PASSWORD = By.id("login_password");
	private static final By LOG_IN = By.xpath("//input[@value='Log In']");
	
	private Artist artist;				public String getAccount() { return artist.getName(); }
	private String password;
	
	public LoginDriver(Artist artist, String password) throws Exception
	{
		super(buildProfile());
		this.artist = artist;
		this.password = password;
		logIn();
	}
	public LoginDriver(LoginDriver copyMe) throws Exception
	{
		super(buildProfile());
		artist = copyMe.artist;
		password = copyMe.password;
		logIn();
	}
	@Override
	public void get(String url)
	{
		while(true)
		{
			try
			{
				Timer timer = new Timer(LITTLE_LESS_THAN_TIMEOUT);
				super.get(url);
				if(timer.hasExpired())
				{
					navigate().refresh();
					continue;
				}
				String source = getPageSource().toLowerCase();
				if(!source.contains("the server encountered an internal error."))
				{
					return;
				}
				if(!source.contains("Server Too Busy"))
				{
					return;
				}
				else
				{
					navigate().refresh();
				}
			}
			catch(UnhandledAlertException killAlert)
			{
				try
				{
					switchTo().alert().dismiss();
				} catch(NoAlertPresentException ignore) {}
			}
			catch(Exception tryAgain) {}
		}
	}
	/** Types the text into all elements found by the specified searcher*/
	public void blast(String text, By by)
	{
		ArrayList<By> bys = new ArrayList<By>();
		bys.add(by);
		blast(text, bys);
	}
	
	public void blastObfuscated(String comment, By by)
	{
		Timer waitChange = new Timer(5000);
		boolean typed = false;
		while(waitChange.stillWaiting() && !typed)
		{
			List<WebElement> elems = findElements(by);
			for(WebElement elem : elems)
			{
				try
				{
					elem.sendKeys(" ");
					elem.clear();
					Macro.typeIntoBrowser(comment, elem);
					typed = true;
				}
				catch(WebDriverException e) {}
			}
		}
	}
	
	//Removes duplicate elements
	private HashSet<WebElement> removeDuplicates(ArrayList<By> bys)
	{
		HashSet<WebElement> elements = new HashSet<WebElement>(); //remove duplicates
		for(By by : bys)
		{
			for(WebElement elem : findElements(by))
			{
				elements.add(elem);
			}
		}
		return elements;
	}
	/** Types the text into all elements (with a short wait in between each) specified by any of the marked criteria */
	public void blastCarefully(String text, ArrayList<By> bys)
	{
		int a = 0;
		for(WebElement elem : removeDuplicates(bys))
		{
			a++;
			try
			{
				System.out.println("Elem Text:" + elem.getText());
				if(elem.getText().isEmpty())
				{
					Macro.typeIntoBrowser(text, elem);
					Macro.sleep(200);
				}
			}
			catch(WebDriverException e) {}
		}
		System.out.println("Blasted "+a+" Elements");
	}
	/** Types the text into all elements specified by any of the marked criteria */
	public void blast(String text, ArrayList<By> bys)
	{
		for(WebElement elem : removeDuplicates(bys))
		{
			try
			{
				if(elem.getText().isEmpty())
				{
					Macro.typeIntoBrowser(text, elem);
				}
			}
			catch(WebDriverException e) {}
		}
	}
	public WebElement waitFor(By by)
	{
		Timer elemTimer = new Timer(ELEMENT_WAIT);
		while(elemTimer.stillWaiting())
		{
			try
			{
				WebElement elem = findElement(by);
				if(elem.isDisplayed() && elem.isEnabled())
				{
					return elem;
				}
			}
			catch(ElementNotVisibleException | NoSuchElementException | StaleElementReferenceException e)
			{

			}
			catch(UnhandledAlertException e)
			{
				try
				{
					switchTo().alert().dismiss();
				}
				catch(NoAlertPresentException a) //Really don't know
				{}
			}
			Macro.sleep(300);
		}
		return null;
	}
	private static int ELEMENT_WAIT = 15000;
	/** Busywaits the driver until the element specified is found, enabled, and available for interaction 
	 * @throws Exception */
	public WebElement waitFor(By by, int index)
	{
		Timer elemTimer = new Timer(ELEMENT_WAIT);
		while(elemTimer.stillWaiting())
		{
			try
			{
				List<WebElement> elems = findElements(by);
				if(elems.size() > index) //can see enough elements
				{
					if(elems.get(index).isDisplayed() && elems.get(index).isEnabled())
					{
						return elems.get(index);
					}
				}
			}
			catch(ElementNotVisibleException | NoSuchElementException | StaleElementReferenceException e)
			{

			}
			catch(UnhandledAlertException e)
			{
				try
				{
					switchTo().alert().dismiss();
				}
				catch(NoAlertPresentException a) //Really don't know
				{}
			}
			Macro.sleep(300);
		}
		return null;
	}
	
	/** Waits until there are css elements matching the cssSelector string. Note: will try for up to 10 seconds */
	public List<WebElement> fillCssElements(String cssSelector)
	{
		List<WebElement> elems = new ArrayList<>();
		Timer timer = new Timer(10000);
		while(elems.isEmpty() && timer.stillWaiting())
		{
			elems = findElements(By.cssSelector(cssSelector));
		}
		return elems;
	}
	
	private static FirefoxProfile buildProfile() throws IOException
	{
		FirefoxProfile ffp = new FirefoxProfile();
		ffp.setPreference("permissions.default.image", 2);
		ffp.setPreference("dom.ipc.plugins.enabled.libflashplayer.so", "false");
		return ffp;
	}

	private static final int LITTLE_LESS_THAN_TIMEOUT = 25000;
	private void logIn() throws Exception
	{
		manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
		get(LOGIN_URL);
		WebElement usernameElem = waitFor(LOGIN_USERNAME);
		usernameElem.sendKeys(artist.getName());
		WebElement passwordElem = findElement(LOGIN_PASSWORD);
		passwordElem.sendKeys(password);
		WebElement form = findElement(LOG_IN);
		form.submit();
		System.out.println("Logged in");
	}
}

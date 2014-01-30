package exhibition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import macro.Macro;
import macro.Timer;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import utils.StringOps;

public class Cynthia 
{
	private static final String LOGIN_URL = "https://www.deviantart.com/users/login";
	private static final By LOGIN_USERNAME = By.id("login_username");
	private static final By LOGIN_PASSWORD = By.id("login_password");
	private static final By LOG_IN = By.xpath("//input[@value='Log In']");
	
	private List<HiddenDriver> pool = new ArrayList<HiddenDriver>();
	private InvitedDatabase invited = new InvitedDatabase();
	private Random random = new Random();
	
	public static void main(String[] args)
	{
		new Cynthia().go();
	}
	private void go()
	{
		pool.add(new HiddenDriver("datrade", "minderbender"));
		pool.add(new HiddenDriver("animelovers21", "qazwsx1"));
		pool.add(new HiddenDriver("pemdara", "qazwsx1"));
		pool.add(new HiddenDriver("indemniss", "qazwsx1"));
		pool.add(new HiddenDriver("fabellaa", "qazwsx1"));
		pool.add(new HiddenDriver("solvatio", "qazwsx1"));
		pool.add(new HiddenDriver("eleganns", "qazwsx1"));
		pool.add(new HiddenDriver("adulcis", "qazwsx1"));
		for(HiddenDriver driver : pool)
		{
			new RandomInvite(driver).start();
		}
	}
	class RandomInvite extends Thread
	{
		private HiddenDriver driver;
		public RandomInvite(HiddenDriver driver)
		{
			this.driver = driver;
		}
		@Override
		public void run()
		{
			while(true)
			{
				driver.get("http://www.deviantart.com/random/deviant");
				String name = StringOps.textBetween(driver.getCurrentUrl(), "http://", ".");
				if(!invited.hasInvited(name))
				{
					invite(driver, name);
				}
				Macro.sleep(10000);
			}
		}
	}
	private static FirefoxProfile buildProfile()
	{
		FirefoxProfile ffp = new FirefoxProfile();
		ffp.setPreference("permissions.default.image", 2);
		ffp.setPreference("dom.ipc.plugins.enabled.libflashplayer.so", "false");
		return ffp;
	}
	class HiddenDriver extends FirefoxDriver
	{
		private String user;
		private String password;
		public HiddenDriver(String user, String password)
		{
			//super(buildProfile());
			this.user = user;
			this.password = password;
			logIn();
		}
		private void logIn()
		{
			get(LOGIN_URL);
			WebElement usernameElem = waitFor(LOGIN_USERNAME);
			usernameElem.sendKeys(user);
			WebElement passwordElem = findElement(LOGIN_PASSWORD);
			passwordElem.sendKeys(password);
			WebElement form = findElement(LOG_IN);
			form.submit();
			System.out.println("Logged in");
		}
		private WebElement waitFor(By by)
		{
			int index = 0;
			Timer elemTimer = new Timer(15000);
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
			}
			return null;
		}
	}
	private void invite(HiddenDriver driver, String name)
	{
		System.out.println("Inviting "+name);
		driver.get("http://theexhibition.deviantart.com/");
		while(true)
		{
			try
			{
				driver.findElement(By.linkText("Invite")).click();
				driver.waitFor(By.xpath("(//input[@name='username'])[2]")).sendKeys(name);
				driver.findElement(By.linkText("Send")).click();
				Macro.sleep(1000);
				if(driver.findElement(By.linkText("Back")) != null)
				{
					System.out.println("Successfully Invited "+name);
					invited.addInvited(name);
				}
				break;
			}
			catch(Exception ignored)
			{
				driver.get("http://theexhibition.deviantart.com/");
			}
		}
	}
	/*
	class Inviter extends Thread
	{
		private String name;
		private Inviter(String name)
		{
			this.name = name;
		}
		@Override
		public void run()
		{
			DriverPool pool = pools.get(random.nextInt(pools.size()));
			LoginDriver driver = pool.get("Inviter");
			System.out.println("Inviting "+name);
			driver.get("http://theexhibition.deviantart.com/");
			driver.findElement(By.linkText("Invite")).click();
			driver.waitFor(By.xpath("(//input[@name='username'])[2]")).sendKeys(name);
			driver.findElement(By.linkText("Send")).click();
			pool.put(driver);
		}
	}
	*/
}

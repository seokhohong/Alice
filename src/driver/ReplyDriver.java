package driver;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import data.Artist;
import macro.Macro;
import macro.Timer;
import event.Reply;

public class ReplyDriver extends Thread
{
	public static void main(String[] args)
	{
		DriverPool pool = new DriverPool(1, new Artist("datrade"), "minderbender");
		new ReplyDriver(pool, new Reply("http://comments.deviantart.com/4/27788959/3261152328", "test")).start();
	}
	private DriverPool pool;
	private Reply reply;
	public ReplyDriver(DriverPool pool, Reply reply)
	{
		this.pool = pool;
		this.reply = reply;
	}
	@Override
	public void run()
	{
		LoginDriver driver = pool.get("ReplyDriver");
		System.out.println("ReplyDriver for "+reply.getUrl()+" has been processed and is proceeding");
		driver.get(reply.getUrl());
		Timer timer = new Timer(120000);
		while(!timer.hasExpired())
		{
			try
			{
				if(driver.getPageSource().contains("No comments have been added yet."))
				{
					try
					{
						WebElement commentBody = driver.findElement(By.id("commentbody"));
						commentBody.sendKeys(Keys.TAB + reply.getMessage());
						driver.waitFor(By.cssSelector("span.post")).click();
					}
					catch(NullPointerException e) {}
					Macro.sleep(5000);
					driver.navigate().refresh();
				}
				else
				{
					break;
				}
			}
			catch(Exception e)
			{
				System.out.println("Trying again");
				e.printStackTrace();
				driver.navigate().refresh();
				Macro.sleep(2000);
			}
		}
		pool.put(driver);
	}
}

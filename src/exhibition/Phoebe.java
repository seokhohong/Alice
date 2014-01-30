package exhibition;

import java.util.List;
import java.util.ArrayList;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import data.Artist;
import driver.DriverPool;
import driver.LoginDriver;
import utils.StringOps;

public class Phoebe 
{
	private DriverPool pool = new DriverPool(1, new Artist("animelovers21"), "qazwsx1");
	private InvitedDatabase invited = new InvitedDatabase();
	private LoginDriver driver = pool.get("Phoebe");
	
	public static void main(String[] args)
	{
		new Phoebe().go();
	}
	private void go()
	{
		driver.get("http://theexhibition.deviantart.com/gallery/");
		while(true)
		{
			try
			{
				driver.findElement(By.partialLinkText("Submit to this")).click();
				driver.findElement(By.partialLinkText("Contribute ")).click();
			}
			catch(NullPointerException e)
			{
				
			}
			break;
		}
		driver.waitFor(By.partialLinkText("All of deviantART")).click();
		driver.waitFor(By.cssSelector("i.thumb-select-3")).click();
		while(true)
		{
			selectThumbs();
			driver.findElement(By.cssSelector("span.ok-label")).click();
			driver.findElement(By.linkText("Next Page")).click();
			try
			{
				Thread.sleep(10000);
			} catch(InterruptedException ignored) {}
		}
	}
	private void selectThumbs()
	{
		List<WebElement> allShadows = new ArrayList<>();
		List<WebElement> nameElems = new ArrayList<>();
		while(allShadows.isEmpty())
		{
			allShadows.addAll(driver.findElements(By.cssSelector("div.search-results > div.stream > div > span > span.shadow")));
			nameElems.addAll(driver.findElements(By.cssSelector("div.search-results > div.stream > div > span > a.t")));
			try
			{
				Thread.sleep(500);
			} catch(InterruptedException ignored) {}
		}
		List<String> names = new ArrayList<>();
		for(WebElement name : nameElems)
		{
			names.add(StringOps.textBetween(name.getAttribute("title"), " by ", ","));
		}
		int numInvited = 0;
		for(int a = 0; a < allShadows.size() ; a ++)
		{
			if(!invited.hasInvited(names.get(a)))
			{
				System.out.println("Inviting "+names.get(a));
				allShadows.get(a).click();
				invited.addInvited(names.get(a));
				numInvited ++;
			}
		}
		/*
		if(numInvited < 5) //reached far back
		{
			System.exit(0);
		}
		*/
		System.out.println();
	}
}

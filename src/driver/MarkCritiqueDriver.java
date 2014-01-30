package driver;

import java.util.ArrayList;

import macro.Macro;
import macro.Timer;
import main.Personality;

import org.openqa.selenium.By;

import data.ToppersDatabase;
import data.Critique;
import data.Payer;
import data.PayerDatabase;

public class MarkCritiqueDriver 
{
	private static final By WRITER_BY = By.xpath("//*[contains(@id, 'w')]");
	private DriverPool pool;
	private PayerDatabase pDatabase;
	private ToppersDatabase cDatabase;
	
	private static final By POST = By.cssSelector("span.post");
	private final String critiqueMarker;
	
	public MarkCritiqueDriver(DriverPool pool, PayerDatabase pDatabase, ToppersDatabase cDatabase)
	{
		this.pool = pool;
		this.pDatabase = pDatabase;
		this.cDatabase = cDatabase;
		Personality personality = new Personality();
		critiqueMarker = personality.randomGreeting() + " " + personality.randomEmote() + " This critique has been submitted through :devdatrade: and is currently pending approval by the artist. If you feel that the critique is unfair or to your dissatisfaction, please read through the <a href=\"http://fav.me/d6hdkqd\">Critique Guidelines</a> and send us a note within 48 hours to receive a refund of your points.";
	}
	
	public synchronized void run()
	{
		new Worker().start();
	}
	
	class Worker extends Thread
	{
		@Override
		public void run()
		{
			goMark();
		}
	}
	
	private void goMark()
	{
		synchronized(pDatabase.getPayers())
		{
			for(Payer payer : pDatabase.getPayers())
			{
				ArrayList<MarkAction> markerThreads = new ArrayList<MarkAction>();
				for(Critique critique : payer.getFreshCritiques())
				{
					cDatabase.addCritique(critique);
					MarkAction newAction = new MarkAction(pool, critique);
					newAction.start();
					markerThreads.add(newAction);
				}
				//Once all action threads are complete for this particular payer, clear out FreshCritiques
				payer.flushMarked();
			}
		}
	}
	class MarkAction extends Thread
	{
		private Critique critique;
		private MarkAction(DriverPool pool, Critique critique)
		{
			this.critique = critique;
		}
		@Override
		public void run()
		{
			Timer timer = new Timer(30000);
			while(timer.stillWaiting())
			{
				LoginDriver driver = pool.get("Critique Marker");
				try
				{
					
					driver.get(critique.URL());
					driver.waitFor(By.id("commentbody")).click();
					driver.waitFor(WRITER_BY);
					driver.blastObfuscated(critiqueMarker, WRITER_BY);
					Macro.sleep(500);
					driver.waitFor(POST).click();
					pool.put(driver);
					break;
				}
				catch(Exception tryAgain)
				{
					
				}
				pool.put(driver);
			}

		}
	}
}

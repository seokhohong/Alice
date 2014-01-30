package driver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import macro.Macro;
import macro.Timer;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebElement;

import data.Artist;
import data.RecordKeeper;
import data.RecordKeeper.Event;
import event.Payment;

public class PayingDriver 
{
	
	public static void main(String[] args)
	{
		new PayingDriver(new DriverPool(1, new Artist("datrade"), "minderbender").get("PayingDriver"), null).pay(new Payment(new Artist("CandyManEditor"), 1, "testing"));
	}
	
	private LoginDriver driver;
	private RecordKeeper recorder;
	
	public PayingDriver(LoginDriver driver, RecordKeeper recorder)
	{
		this.driver = driver;
		this.recorder = recorder;
	}
	
	private static final By DONATE = By.className("donate");
	private static final By IFRAMES = By.tagName("iframe");
	private static final By NUM_POINTS = By.id("num_points");
	private static final By SUBMIT = By.xpath("//div[4]/input");
	
	private static final int NUM_ATTEMPTS = 2;
	
	public void pay(Payment payment)
	{
		int attempts = 0;
		while(attempts < NUM_ATTEMPTS)
		{
			attempts++;
			try
			{
				int numFrames = findFrames(payment.getArtist());
				List<WebElement> donates = driver.findElements(DONATE);
				for(WebElement donate : donates)
				{
					Timer tryTimer = new Timer(60000);
					while(tryTimer.stillWaiting())
					{
						try
						{
							donate.click();
							payArtist(payment, numFrames);
							return;
						}
						catch(NoSuchElementException | ElementNotVisibleException e)
						{
							break; //next element
						}
					}
				}
			}
			catch(PaymentFailedException tryAgain)
			{
				System.out.println("PaymentFailed");
			}
		}
	}
	private int findFrames(Artist artist)
	{
		while(true)
		{
			try
			{
				driver.get(artist.getHomeURL());
				return driver.findElements(IFRAMES).size();
			}
			catch(UnhandledAlertException e)
			{
				try
				{
					driver.switchTo().alert().dismiss();
				}
				catch(NoAlertPresentException noAlert)
				{
					
				}
			}
		}
	}
	private static final int IFRAME_TIME = 4000;
	private void payArtist(Payment payment, int initFrames) throws PaymentFailedException
	{
		Timer frameFinder = new Timer(IFRAME_TIME);
		List<WebElement> iframes = new ArrayList<WebElement>();
		while(frameFinder.stillWaiting())
		{
			 iframes = driver.findElements(IFRAMES);
			 if(iframes.size() != initFrames)
			 {
				 break;
			 }
		}
		
	    try
	    {
	    	driver.switchTo().frame(iframes.get(iframes.size()-1)); //get the last iframe
	    	WebElement pointsBox = driver.waitFor(NUM_POINTS);
	    	pointsBox.clear(); //it will throw null exception
	    	pointsBox.sendKeys(Integer.toString(payment.getAmount()));
	    
		    WebElement memo = driver.findElement(By.name("memo"));
		    memo.clear();
		    Macro.sleep(200);
		    Macro.typeIntoBrowser(payment.getMessage(), memo);
		    
		    driver.findElement(SUBMIT).click();
	    	driver.waitFor(By.linkText("Okay")).click();
	    } 
	    catch(Exception paymentFailed) { throw new PaymentFailedException(); }
	    recordPayment(payment);
	}
	private void recordPayment(Payment payment)
	{
		if(recorder != null)
		{
			recorder.addTotalPaid(payment.getAmount());
			if(payment.getAmount() > 0)
			{
				recorder.addEvent(Event.PAYMENT_OUT, payment.toString(), new Date());
			}
		}
	}
	private class PaymentFailedException extends Exception 
	{
		private static final long serialVersionUID = 1L;	
	}
}

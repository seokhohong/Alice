package worker;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import data.Artist;
import driver.DriverPool;
import driver.LoginDriver;
import macro.Macro;
import macro.Timer;

public class NoteWriter 
{
	public static void main(String[] args)
	{
		new NoteWriter(new DriverPool(1, new Artist("datrade"), "minderbender")).send("animelovers21", "testing", "test message");
	}
	
	private static final String NOTE_URL = "http://www.deviantart.com/messages/notes/#1_0";
	
	private static final By NEW_NOTE_ELEM = By.linkText("Create New Note");
	private static final By SUBJECT_ELEM = By.xpath("//div[@id='notes']/table/tbody/tr/td[2]/table/tbody/tr/td[2]/div[3]/div/div[2]/div/div/form/div/div[2]/input");
	
	private static final By MSG_BODY_ELEM = By.id("notebody");
	private static final By SUBMIT_ELEM = By.xpath("//div[5]/a[2]/span");
	
	private static final int NOTE_TIMER = 60000;
	
	private DriverPool pool;
	
	public NoteWriter(DriverPool pool)
	{
		this.pool = pool;
	}
	public void send(String recipient, String subject, String message)
	{
		new Write(recipient, subject, message).start();
	}
	private class Write extends Thread
	{
		private String recipient;
		private String subject;
		private String message;
		private Write(String recipient, String subject, String message)
		{
			this.recipient = recipient;
			this.subject = subject;
			this.message = message;
		}
		@Override
		public void run()
		{
			LoginDriver driver = pool.get("Note Writer");
			Timer noteTimer = new Timer(NOTE_TIMER);
			driver.get(NOTE_URL);
			while(true) //some initial error handling
			{
				if(noteTimer.hasExpired())
				{
					System.out.println("Failed to send note to "+recipient);
					return;
				}
				try
				{
					WebElement newNote = driver.waitFor(NEW_NOTE_ELEM);
					newNote.click();
					break;
				} catch(Exception retry) { driver.navigate().refresh(); }
			}
			
			List<WebElement> inputs = driver.fillCssElements("div#recipient-textareas > input");
			for(WebElement input : inputs)
			{
				if(input.isDisplayed())
				{
					input.sendKeys(recipient);
				}
			}
			WebElement subjectElem = driver.findElement(SUBJECT_ELEM);
			subjectElem.clear();
			subjectElem.sendKeys(subject);
			
			WebElement msgBodyElem = driver.findElement(MSG_BODY_ELEM);
			msgBodyElem.click();
			Macro.pasteIntoBrowser(message, msgBodyElem);

			//Finished
			driver.findElement(SUBMIT_ELEM).click();	
			
			pool.put(driver);
		}
	}
}

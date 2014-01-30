package macro;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class Macro 
{
	private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	public static void sleep(int millis)
	{
		try 
		{
			Thread.sleep(millis);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	//Uses the clipboard to copy and paste text into the browser
	public static synchronized void typeIntoBrowser(String s, WebElement elem)
	{
		elem.sendKeys(s);
	}
	public static synchronized void pasteIntoBrowser(String s, WebElement elem)
	{
		Macro.copyToClipboard(s);
		Macro.pasteClipboard(elem);
		Macro.clearClipboard();
	}
	private static void copyToClipboard(String s)
	{
		StringSelection sers = new StringSelection(s);
		clipboard.setContents(sers, null); 
	}	
	private static void clearClipboard()
	{
		StringSelection sers = new StringSelection("");
		clipboard.setContents(sers, null); 
	}
	private static void pasteClipboard(WebElement elem)
	{
		elem.sendKeys(Keys.CONTROL, "v");
	}
	/** For abnormal use */
	public synchronized static String getClipboardContents()
	{
		//Many thanks to code from: http://www.javapractices.com/topic/TopicAction.do?Id=82
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		String result = "";
	    if(hasTransferableText)
	    {
	    	try 
	    	{
	    		result = (String)contents.getTransferData(DataFlavor.stringFlavor);
	    	}
	    	catch(Exception ex)
	    	{
	    		//highly unlikely since we are using a standard DataFlavor
	    		System.out.println(ex);
	    		ex.printStackTrace();
	    	}
	    }
	    return result;
	}
}

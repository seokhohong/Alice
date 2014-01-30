package htmldriver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class HtmlDriverTesting 
{
	public static void main(String[] args)
	{
		new HtmlDriverTesting().go();
	}
	private void go()
	{
		WebDriver driver = new HtmlUnitDriver();
		driver.get("http://www.google.com");
		System.out.println();
	}
}

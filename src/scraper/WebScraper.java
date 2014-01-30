package scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import data.Artist;
import macro.Macro;

public class WebScraper
{
	public static void main(String[] args) throws IOException
	{
		get("http://suicune-chic.deviantart.com/critique/877769008/");
	}
	public static boolean isValid(String webURL)
	{
		try
		{
			URL url = new URL(webURL);
			new BufferedReader(new InputStreamReader(url.openStream()));
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
	}
	public static String get(String webURL)
	{
		return get(webURL, true);
	}
	private static final int MAX_ATTEMPTS = 5;
	public static String get(String webURL, boolean display) throws ScrapingError
	{
		if(webURL.contains("suicune-chic")) //TODO: remove this from db... somehow 
		{
			System.out.println();
		}
		int attempts = 0;
		while(attempts < MAX_ATTEMPTS)
		{
			attempts ++;
			try
			{
				if(display)
				{
					System.out.println("Scraping "+webURL);
				}
				URL url = new URL(webURL);
				BufferedReader in = new BufferedReader(
						new InputStreamReader(url.openStream()));
		
				String inputLine = "";
				StringBuilder builder = new StringBuilder(inputLine);
		
				while ((inputLine = in.readLine()) != null)
				{
					builder.append(inputLine);
				}
				
				in.close();
				return builder.toString();
			}
			catch(IOException e)
			{
				//e.printStackTrace();
				System.err.println("Cannot Scrape "+webURL);
				
				Macro.sleep(1000);
			}
		}
		return "";
	}
}

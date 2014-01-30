package jane;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import macro.Timer;
import scraper.ScrapingError;

/** Scrape via proxy */
public class ProxyScraper 
{
	private ProxyPool pool;
	public ProxyScraper(ProxyPool pool)
	{
		this.pool = pool;
	}
	String get(String webURL) throws ScrapingError
	{
		String inputLine = "";
		StringBuilder builder = new StringBuilder(inputLine);
		while(true)
		{
			Proxy proxy = pool.get();
			try
			{
				HttpURLConnection connection = (HttpURLConnection)new URL(webURL).openConnection(proxy);
				connection.connect();
				Timer timer = new Timer(120000);
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while((inputLine = in.readLine()) != null)
				{
					if(timer.hasExpired())
					{
						pool.put(proxy, ProxyPool.Quality.BAD);
						continue;
					}
					builder.append(inputLine);
				}
				pool.put(proxy, ProxyPool.Quality.GOOD);
				break;
			}
			catch(Exception e)
			{
				//System.out.println(e.getMessage());
			}
			pool.put(proxy, ProxyPool.Quality.BAD);
		}
		return builder.toString();
	}
}

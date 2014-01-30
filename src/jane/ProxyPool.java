package jane;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;

import macro.Macro;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import utils.StringOps;

public class ProxyPool 
{
	private static final int LIMIT_CONNECTIONS_PER = 5; 
	private static final int NUM_PROXY_PAGES = 13;
	private static final int TOO_MANY_STRIKES = 100; //some proxies may not be so hot
	
	//doesn't have to be logged in
	private FirefoxDriver driver = new FirefoxDriver();
	
	//Maps proxies to strikes
	private Map<Proxy, Integer> proxies = new HashMap<Proxy, Integer>();
	private Semaphore available = new Semaphore(0);
	
	public ProxyPool()
	{
		getProxies();
	}
	public synchronized Proxy get()
	{
		try
		{
			available.acquire();
		} catch(InterruptedException go) {}
		synchronized(proxies)
		{
			Proxy proxy = null;
			while(proxy == null)
			{
				proxy = getRandomProxy();
			}
			return proxy;
		}
	}
	private Proxy getRandomProxy()
	{
		int rnd = new Random().nextInt(proxies.size());
		int index = 0;
		for(Proxy proxy : proxies.keySet())
		{
			index++;
			if(index == rnd)
			{
				return proxy;
			}
		}
		//not reached
		return null;
	}
	public void put(Proxy proxy, Quality quality)
	{
		available.release();
		synchronized(proxies)
		{
			if(quality == Quality.BAD)
			{
				if(proxies.containsKey(proxy))
				{
					proxies.put(proxy, proxies.get(proxy) + 1);
					if(proxies.get(proxy) > TOO_MANY_STRIKES)
					{
						proxies.remove(proxy);
						System.out.println("Removing Poor Proxy "+proxies.size()+" Permits "+available.availablePermits());
						try {
							available.acquire(LIMIT_CONNECTIONS_PER);
						} catch (InterruptedException ignored) {}
					}
				}
			}
			else
			{
				//remove a strike
				proxies.put(proxy, proxies.get(proxy) - 1);
			}
		}
	}
	private void put(int num)
	{
		available.release(num);
	}
	enum Quality
	{
		GOOD,
		BAD;
	}
	private static final String PROXY_SOURCE = "http://hidemyass.com/proxy-list/";
	private static final By SELECT_ALL = By.cssSelector("body");
	private static final String CTRL_A = Keys.chord(Keys.CONTROL, "a");
	private static final String CTRL_C = Keys.chord(Keys.CONTROL, "c");
	
	//Scrapes proxies off a page
	class ProxyScraper extends Thread
	{
		@Override
		public synchronized void run()
		{
			for(int offset = 0; offset < NUM_PROXY_PAGES; offset ++)
			{
				while(true)
				{
					try
					{
						driver.get(PROXY_SOURCE + ((offset == 0) ? "" : offset));
						WebElement body = driver.findElement(SELECT_ALL);
						body.sendKeys(CTRL_A);
						body.sendKeys(CTRL_C);
						Collection<Proxy> foundProxies = parseProxies(Macro.getClipboardContents());
						synchronized(proxies)
						{
							for(Proxy proxy : foundProxies)
							{
								proxies.put(proxy, 0);
							}
						}
						synchronized(available)
						{
							put(foundProxies.size() * LIMIT_CONNECTIONS_PER);
						}
						break;
					} 
					catch(Exception tryAgain)
					{
						Macro.sleep(5000);
					}
				}
				Macro.sleep(5000);
			}
		}
	}
	
	private void getProxies()
	{
		new ProxyScraper().start();
	}
	
	private ArrayList<Proxy> parseProxies(String pageSource)
	{
		ArrayList<Proxy> proxies = new ArrayList<Proxy>();
		pageSource = StringOps.textBetween(pageSource, "Connection time", "Previous");
	 	int index = 0;
	 	while((index = pageSource.indexOf(".", index)) != -1)
	 	{
	 		String ip = pageSource.substring(index - 3, pageSource.indexOf("\t", index)).trim();
	 		String port = StringOps.textBetween(pageSource, ip, "flag").trim();
	 		index += ip.length();
	 		int endOfType = pageSource.indexOf(".", index);
	 		if(endOfType == -1)
	 		{
	 			endOfType = pageSource.length();
	 		}
	 		String typeString = pageSource.substring(index, endOfType);
	 		//use only HTTP proxies
	 		if(typeString.contains("HTTP") && typeString.contains("HTTPS"))
	 		{
	 			proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, Integer.parseInt(port))));
	 		}
	 	}
		return proxies;
	}
}

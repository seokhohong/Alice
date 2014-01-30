package driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import data.Artist;
import macro.Macro;

//Copied from Javadoc example of the semaphore
public class DriverPool 
{
	private final int numAvailable;			public int capacity() { return numAvailable; }
	private final Semaphore available;		public int numDrivers() { return available.availablePermits(); }
	private Artist account;
	private String password;   
	   
	public DriverPool(int numDrivers, Artist account, String password)
	{
		this.account = account;
		this.password = password;
		numAvailable = numDrivers;
		items = new LoginDriver[numAvailable];
		used = new boolean[numAvailable];
		users = new String[numAvailable];
		Arrays.fill(used, true);
		available = new Semaphore(numAvailable, true);
		createDrivers();
	}
	   
	private void createDrivers()
	{
		ArrayList<MakeDriver> makeDrivers = new ArrayList<MakeDriver>();
		try {
			available.acquire(numAvailable);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		  
		for(int a = numAvailable; a --> 0; )
		{
			MakeDriver mkDriver = new MakeDriver(a);
			mkDriver.start();
			makeDrivers.add(mkDriver);
		} 
	}
	   
	class MakeDriver extends Thread
	{
		private int index; //index of item array
		private MakeDriver(int index)
		{
			this.index = index;
		}
		@Override
		public void run()
		{
			LoginDriver newDriver = null;
			while(true)
			{
				try
				{
					newDriver = new LoginDriver(account, password);
					break;
				}
				catch(Exception e)
				{
					e.printStackTrace();
					Macro.sleep(1000);
				}
			}
			items[index] = newDriver;
			used[index] = false;
			available.release();
		}
	}

	public LoginDriver get(String from) 
	{
		LoginDriver driver = null;
		try
		{
			available.acquire();
			driver = getNextAvailableDriver(from);
			System.out.println(Thread.currentThread()+" from "+from+" acquired a Driver: "+driver+". Total "+available.availablePermits()+" Remaining");
		}
		catch(InterruptedException e) {}
		return driver;
	}

	public void put(LoginDriver x) 
	{
		if(markAsUnused(x))
		{
			available.release();
			System.out.println(Thread.currentThread() +" released Driver "+x+". Total "+available.availablePermits());
		}
	}
	
	public String occupationString()
	{
		StringBuilder builder = new StringBuilder();
		for(String user : users)
		{
			builder.append(user + " ");
		}
		return builder.toString();
	}


	// Not a particularly efficient data structure
	private LoginDriver[] items;
	private boolean[] used;
	private String[] users;

	private synchronized LoginDriver getNextAvailableDriver(String user) {
		for (int i = 0; i < numAvailable; ++i) {
			if (!used[i]) {
				used[i] = true;
				users[i] = user;
				return items[i];
			}
		}
		return null; // not reached
	}

	private synchronized boolean markAsUnused(LoginDriver item) {
		for (int i = 0; i < numAvailable; ++i) {
			if (item == items[i]) {
				if (used[i]) {
					used[i] = false;
					users[i] = null;
					return true;
				} else
					return false;
				}
			}
			return false;
		}
	 }
package event;

import java.util.HashSet;
import java.util.Set;

import driver.DriverPool;
import driver.ReplyDriver;

/** Remembers which comments have been responded to. */
public class ReplyEvents 
{
	private DriverPool pool;
	
	private Set<String> commentUrls = new HashSet<>(); //wont ever need to respond to a comment twice
	
	public ReplyEvents(DriverPool pool)
	{
		this.pool = pool;
	}
	
	public synchronized void addReply(Reply reply)
	{
		if(!commentUrls.contains(reply.getUrl()))
		{
			commentUrls.add(reply.getUrl());
			new LaunchReply(reply).start();
		}
	}
	class LaunchReply extends Thread
	{
		private Reply reply;
		LaunchReply(Reply reply)
		{
			this.reply = reply;
		}
		@Override
		public void run()
		{
			ReplyDriver driver = new ReplyDriver(pool, reply);
			driver.start();
			try 
			{
				driver.join();
			}
			catch (InterruptedException ignored) {}
			clearComment(reply);
		}
	}
	synchronized void clearComment(Reply reply)
	{
		commentUrls.remove(reply.getUrl());
	}
}

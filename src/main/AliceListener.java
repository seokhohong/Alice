package main;

import java.util.Date;

import utils.Email;
import worker.NoteWriter;

public class AliceListener extends Thread implements Listener 
{
	private String email;
	private String emailPassword;
	private String destination;
	private NoteWriter noteWriter;
	
	private Exception crashingException = null;		
	
	public AliceListener(String email, String emailPassword, String destination, NoteWriter noteWriter)
	{
		super("AliceListener");
		this.email = email;
		this.emailPassword = emailPassword;
		this.destination = destination;
		this.noteWriter = noteWriter;
	}
	@Override
	public void catchException(Exception e) 
	{
		crashingException = e;
		synchronized(this)
		{
			notify();
		}
	}
	
	@Override
	public void run()
	{
		while(crashingException == null)
		{
			try
			{
				synchronized(this)
				{
					wait();
				}
			}
			catch(InterruptedException ignore) {}
		}
		sendCrashReport();
	}
	private void sendCrashReport()
	{
		System.out.println("Alice Crashed :(");
		crashingException.printStackTrace();
		Email.send(email, emailPassword, destination, crashReport());
		//noteWriter.send("simplysilent", "Alice Needs Help", crashReport());
	}
	private String crashReport()
	{
		String report = "Alice Crashed at "+new Date()+"\n";
		for(StackTraceElement stElem : crashingException.getStackTrace())
		{
			report += stElem.toString()+"\n";
		}
		return report;
	}
}

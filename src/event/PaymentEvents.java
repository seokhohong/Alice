package event;

import data.Artist;
import data.RecordKeeper;
import driver.DriverPool;
import driver.LoginDriver;
import driver.PayingDriver;

public class PaymentEvents 
{
	private DriverPool pool;
	private RecordKeeper recorder;
	
	public PaymentEvents(DriverPool pool, RecordKeeper recorder)
	{
		this.pool = pool;
		this.recorder = recorder;
	}
	
	public synchronized void addPayment(Artist artist, int amount, String message)
	{
		Payment payment = new Payment(artist, amount, message);
		new PayAction(payment).start();
	}
	
	private class PayAction extends Thread
	{
		private Payment payment;
		private PayAction(Payment payment)
		{
			this.payment = payment;
		}
		@Override
		public void run()
		{
			LoginDriver driver = pool.get("PaymentEvent");
			new PayingDriver(driver, recorder).pay(payment);
			pool.put(driver);
		}
	}
}

package data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import event.PaymentEvents;
import utils.Append;
import worker.NoteWriter;

public class GiveawayDatabase 
{
	private static final File GIVEAWAY_FILE = new File("data\\giveaway.txt");
	
	private static final String THUMBS_TAG = ":thumb";
	private static final String THUMBS_END = ":";
	
	private static final int COST_PER_THUMB = 50;
	
	private PaymentEvents paymentEvents;
	private NoteWriter noteWriter;
	
	public GiveawayDatabase(PaymentEvents paymentEvents, NoteWriter noteWriter)
	{
		this.paymentEvents = paymentEvents;
		this.noteWriter = noteWriter;
	}
	
	/** Returns if this transaction was accepted as a giveaway transaction  */
	public synchronized boolean processTransaction(Transaction transaction)
	{
		if(transaction.getFirstWord().toLowerCase().contains("give"))
		{
			int paymentAmount = transaction.getAmount();
			if(paymentAmount >= COST_PER_THUMB)
			{
				noteWriter.send(transaction.getOrigin(), "Donation Received!", "You have thumbs that will be featured in the next giveaway: "+getThumbList(parseThumbs(transaction.getMessage(), paymentAmount))+" \n\n -Automated by Program Alice");
			}
			else
			{
				paymentEvents.addPayment(new Artist(transaction.getOrigin()), paymentAmount, "You donated an insufficient amount to be featured in our giveaway. Please donate at least "+COST_PER_THUMB+" points!");
			}
			save(transaction.getOrigin(), transaction.getMessage(), paymentAmount);
			return true;
		}
		else
		{
			return false;
		}
	}

	//Parses only as many thumbs as they paid for
	private List<String> parseThumbs(String message, int paymentAmount)
	{
		List<String> artworks = new ArrayList<String>();
		int index = 0;
		while((index = message.indexOf(THUMBS_TAG, index)) != -1)
		{
			artworks.add(message.substring(index, message.indexOf(THUMBS_END, index + 1)) + THUMBS_END);
			index ++;
			if(artworks.size() > paymentAmount / COST_PER_THUMB) break;
		}
		return artworks;
	}
	private String getThumbList(List<String> thumbs)
	{
		StringBuilder builder = new StringBuilder();
		for(String thumb : thumbs)
		{
			builder.append(" "+thumb);
		}
		return builder.toString();
	}
	private void save(String deviant, String message, int paymentAmount)
	{
		Append.toFile(GIVEAWAY_FILE, deviant + " " + getThumbList(parseThumbs(message, paymentAmount)));
	}

}

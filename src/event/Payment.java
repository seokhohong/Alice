package event;

import data.Artist;

public class Payment
{
	private Artist artist;		public Artist getArtist() { return artist; }		
	private int amount;			public int getAmount() { return amount; }
	private String message;		public String getMessage() { return message; }
	
	public Payment(Artist artist, int amount, String message)
	{
		this.artist = artist;
		this.amount = amount;
		this.message = message;
	}
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("Paid ");
		builder.append(Integer.toString(amount));
		builder.append(" to ");
		builder.append(artist);
		builder.append(" with message :");
		builder.append(message);
		return builder.toString();
	}
}

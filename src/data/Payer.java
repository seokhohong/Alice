package data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import event.PaymentEvents;
import scraper.WebScraper;
import sql.Blob;
import sql.SQLDatabase;
import sql.SQLUtils;
import utils.MySerializer;
import utils.StringOps;
import worker.NoteWriter;

public class Payer extends Artist implements Comparable<Payer>, Refundable
{
	static final String COL_NAME = "COL_NAME";
	private static final String COL_POINTS = "COL_POINTS";
	private static final String COL_RECEIVED_LLAMAS = "COL_RECEIVED_LLAMAS";
	private static final String COL_RECEIVED_FAVS = "COL_RECEIVED_FAVS";
	private static final String COL_RECEIVED_WATCHES = "COL_RECEIVED_WATCHES";
	private static final String COL_RECEIVED_CRITIQUES = "COL_RECEIVED_CRITIQUES";
	private static final String COL_RECEIVED_COMMENTS = "COL_RECEIVED_COMMENTS";
	private static final String COL_ACCEPTS_WATCHES = "COL_ACCEPTS_WATCHES";
	private static final String COL_ACCEPTS_LLAMAS = "COL_ACCEPTS_LLAMAS";
	private static final String COL_ACCEPTS_FAVS = "COL_ACCEPTS_FAVS";
	private static final String COL_ACCEPTS_CRITIQUES = "COL_ACCEPTS_CRITIQUES";
	private static final String COL_ACCEPTS_COMMENTS = "COL_ACCEPTS_COMMENTS";
	private static final String COL_LAST_PAYMENT = "COL_LAST_PAYMENT";
	private static final String COL_LLAMA_URL = "COL_LLAMA_URL";
	private static final String COL_ARTWORKS = "COL_ARTWORKS";
	private static final String COL_PENDING_CRITIQUES = "COL_PENDING_CRITIQUES";
	private static final String COL_CRITIQUE_HISTORY = "COL_CRITIQUE_HISTORY";
	private static final String COL_TIMED_FEATURES = "COL_TIMED_FEATURES";

	private static final int MAX_ARTWORKS = 5;
	
	private static final String WATCH_PREF_TAG = "watch";
	private static final String LLAMA_PREF_TAG = "llama";
	private static final String FAV_PREF_TAG = "fav";
	private static final String COMMENT_PREF_TAG = "comment";
	
	private static final String ALT_ARTWORK_TAG = "deviationid=\"";
	private static final String ARTWORK_TAG = ":thumb";
	private static final String ARTWORK_END = ":";
	
	private Set<Critique> freshCritiques = new HashSet<Critique>(); //just now received
	private Set<Critique> pendingCritiques = new HashSet<Critique>(); //critiques awaiting approval
	private Set<Critique> acceptedCritiques = new HashSet<Critique>();

	private Set<Integer> critiqueHistory = new HashSet<Integer>(); //by id
	
	private static final Set<Currency> currencies = new HashSet<Currency>();
	private static final Map<String, Currency> tag = new HashMap<String, Currency>();
	private static final Map<Currency, Boolean> defaultAcceptance = new HashMap<Currency, Boolean>();
	private static final Map<Currency, String> receivedPlural = new HashMap<Currency, String>();
	static
	{
		currencies.add(Currency.WATCH);
		currencies.add(Currency.LLAMA);
		currencies.add(Currency.FAV);
		currencies.add(Currency.CRITIQUE);
		currencies.add(Currency.COMMENT);
		tag.put(WATCH_PREF_TAG, Currency.WATCH);
		tag.put(LLAMA_PREF_TAG, Currency.LLAMA);
		tag.put(FAV_PREF_TAG, Currency.FAV);
		tag.put(ARTWORK_TAG, Currency.CRITIQUE);
		tag.put(ALT_ARTWORK_TAG, Currency.CRITIQUE);
		tag.put(COMMENT_PREF_TAG, Currency.COMMENT);
		defaultAcceptance.put(Currency.WATCH, true);
		defaultAcceptance.put(Currency.LLAMA, true);
		defaultAcceptance.put(Currency.FAV, true);
		defaultAcceptance.put(Currency.CRITIQUE, false);
		defaultAcceptance.put(Currency.COMMENT, false);
		receivedPlural.put(Currency.WATCH, " watches");
		receivedPlural.put(Currency.LLAMA, " llamas");
		receivedPlural.put(Currency.FAV, " faves");
		receivedPlural.put(Currency.CRITIQUE, " critiques (some additional critiques may be pending your approval)");
		receivedPlural.put(Currency.COMMENT, " comments");
	}
	
	/* Remember to change load and save when adding new variables!*/
	private float points;
	private Map<Currency, Integer> receivedCounts = new HashMap<Currency, Integer>();
	
	//stored as thumbnails
	private Set<String> artworks = new HashSet<String>(); 		public synchronized boolean hasArtwork() { return !artworks.isEmpty(); }
																public synchronized Set<String> getArtworks() { return artworks; }
	
	//Used to keep track of time of last payment
	private Transaction lastPayment;
	
	private Map<Currency, Boolean> acceptsCurrency = new HashMap<Currency, Boolean>();
	
	private boolean beingRefunded = false;	public synchronized void giveRefund() { beingRefunded = true; }
											public boolean waitingForRefund() { return beingRefunded; }
											public synchronized void processRefund() { if(points >= 1) points = 0; }
											//Does not need to clear refund because payer file will be deleted
	
	public static final int NO_URL = -1;
	private int llamaUrl = NO_URL;
	
	private String favLink = "";			public void setFavLink(String favLink) { this.favLink = favLink; } 
	
	//Whether the payer has been modified since last savings
	public boolean modifiedFlag = false;	public synchronized boolean hasBeenModified() { return modifiedFlag; }
											private void setModified() { modifiedFlag = true; }
											public synchronized void clearModified() { modifiedFlag = false; }
											
	public ArrayList<TimedFeature> timedFeatures = new ArrayList<TimedFeature>();			public boolean hasTimedFeatures() { return !timedFeatures.isEmpty(); }
	
	private SQLDatabase database;
	
	public Payer(File file)
	{
		super(file);
		load(file);
	}
	public Payer(SQLDatabase database, String name)
	{
		super(name);
		this.database = database;
		loadDB(database);
	}
											
	private synchronized void initData()
	{
		points = 0f;
		for(Currency item : defaultAcceptance.keySet())
		{
			receivedCounts.put(item, 0);
			acceptsCurrency.put(item, false);
		}
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	/** Use only for testing */
	@Deprecated
	public void addPoints(double p)
	{
		System.out.println("Adding "+p+" points to "+name);
		points += p;
		setModified();
	}
	/** Won't change frequently enough to be worth sync */
	public boolean accepts(Currency currency)
	{
		if(!acceptsCurrency.containsKey(currency))
		{
			acceptsCurrency.put(currency, false);
		}
		return acceptsCurrency.get(currency);
	}
	public synchronized int numReceived(Currency currency)
	{
		if(!receivedCounts.containsKey(currency))
		{
			receivedCounts.put(currency, 0);
		}
		return receivedCounts.get(currency);
	}
	private double applyReferral(Transaction transaction, PaymentEvents paymentEvents, RecordKeeper recorder, double amount)
	{
		if(transaction.getMessage().toLowerCase().contains("ref") && !transaction.extractDevName().equals(name))
		{
			System.out.println("Processing Referral");
			String referrer = transaction.extractDevName();
			paymentEvents.addPayment(new Artist(referrer), (int) ((double) amount * 0.2d), successfulReferralMessage());
			recorder.addRefer();
			return (double) amount * 1.05d;
		}
		return amount;
	}
	private String successfulReferralMessage()
	{
		return "You referred "+name+"!";
	}
	//Updates the payer's values by the transaction
	private synchronized void processTransaction(Transaction transaction, PaymentEvents paymentEvents, RecordKeeper recorder, boolean isNewPayer)
	{
		double amount = transaction.getAmount();
		if(paymentEvents != null && isNewPayer) //null if there can't be a referral made (i.e. gift)
		{
			amount = applyReferral(transaction, paymentEvents, recorder, amount);
		}
		amount = Transaction.applyBonuses(amount);
		points += amount;

		lastPayment = transaction;
		if(!updateFeatures(lastPayment.getMessage(), amount))
		{
			setPreferences(lastPayment.getMessage());	//and update preferences
			updateArtwork(lastPayment.getMessage());	//add artworks if any
		}

	}
	public synchronized void acceptGift(Transaction transaction, RecordKeeper recorder, NoteWriter noteWriter)
	{
		processTransaction(transaction, null, null, false);
		System.out.println("Adding "+transaction.getAmount()+" points to "+name);
		noteWriter.send(transaction.getOrigin().toLowerCase(), "Gift Sent!", giftSentMessage());
		noteWriter.send(name, "Someone Gifted You!", giftReceivedMessage(transaction.getOrigin().toLowerCase()));
		recorder.addGift();
		save();
	}
	private String giftSentMessage()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(":dev"+name+": has received your gift!\n\n");
		builder.append(" - automated by Program Alice");
		return builder.toString();
	}
	String giftReceivedMessage(String gifter)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(":dev"+gifter +": has donated for you! :heart: Your current balance is ");
		builder.append(getPoints());
		builder.append(" :points: .\n\n");
		builder.append("You currently accept: ");
		builder.append(acceptanceList());
		builder.append(".\n\n");
		builder.append(" - automated by Program Alice");
		return builder.toString();
	}
	public synchronized void acceptTransaction(Transaction transaction, PaymentEvents paymentEvents, NoteWriter noteWriter, RecordKeeper recorder, boolean isNewPayer)
	{
		processTransaction(transaction, paymentEvents, recorder, isNewPayer);
		sendDonationReceivedMessage(noteWriter);
		System.out.println("Adding "+transaction.getAmount()+" points to "+transaction.getOrigin());
		save();
	}
	void sendDonationReceivedMessage(NoteWriter noteWriter)
	{
		noteWriter.send(name, "Donation Received!", thankYouMessage());
	}
	String thankYouMessage()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Thank you for donating! :heart: Your current balance is ");
		builder.append(points);
		builder.append(" :points: .\n\n");
		builder.append("You currently accept ");
		builder.append(acceptanceList());
		builder.append(". \n\nTo change these options, type #prefs in a new comment on my profile and include the new list of options you would like to receive.");
		builder.append(" For more information, refer to my <a href=\"http://fav.me/d6hbehb\">Donating Points</a> journal.\n\n");
		if(accepts(Currency.COMMENT))
		{
			builder.append("\n\n");
		}
		//appendFeatures(builder); //disabled 
		builder.append("- automated by Program Alice");
		return builder.toString();
	}
	/** Provides just the raw list */
	public String acceptanceList()
	{
		StringBuilder builder = new StringBuilder();
		ArrayList<String> accepts = new ArrayList<String>();
		for(Currency currency : currencies)
		{
			if(accepts(currency))
			{
				accepts.add(StringOps.capitalizeFirstLetterOf(currency.getPlural()));
			}
		}
		builder.append(StringOps.listUp(accepts));
		return builder.toString();
	}
	/** */
	private void appendFeatures(StringBuilder builder)
	{
		if(hasTimedFeatures())
		{
			builder.append("Your Features:\n");
			for(TimedFeature feature : timedFeatures)
			{
				builder.append(feature.getFeatureType()+" "+feature.daysRemaining()+"\n");
			}
			builder.append("\n");
		}
	}
	synchronized int calculateRefund()
	{
		return Transaction.calculateRefund((int) points);
	}
	public synchronized float getPoints()
	{
		return points;
	}
	/** Determines if the current settings leave the payer with nothing to accept*/
	public synchronized boolean acceptsNone()
	{
		boolean acceptsNone = true;
		for(Currency item : currencies)
		{
			if(accepts(item))
			{
				acceptsNone = false;
			}
		}
		return acceptsNone;
	}
	public synchronized boolean cantBuyMore()
	{
		if(acceptsNone() && !hasTimedFeatures())
		{
			setAcceptanceToDefaults();
		}
		for(Currency item : currencies)
		{
			if(points < item.getCost())
			{
				acceptsCurrency.put(item, false);
			}
		}
		return acceptsNone() && !hasTimedFeatures();
	}
	
	public synchronized void setPreferences(String prefs)
	{
		prefs = prefs.toLowerCase();
		if(prefs.contains(WATCH_PREF_TAG) 
				|| prefs.contains(LLAMA_PREF_TAG) 
				|| prefs.contains(FAV_PREF_TAG) 
				|| prefs.contains(ARTWORK_TAG)
				|| prefs.contains(ALT_ARTWORK_TAG)
				|| prefs.contains(COMMENT_PREF_TAG))
		{
			for(Currency currency : currencies)
			{
				acceptsCurrency.put(currency, false);
			}
			for(String prefTag : tag.keySet())
			{
				if(prefs.contains(prefTag))
				{
					acceptsCurrency.put(tag.get(prefTag), true);
					System.out.println("Accepts "+tag.get(prefTag));
				}
			}
		}
		else
		{
			setAcceptanceToDefaults();
		}
		setModified();
	}
	private void setAcceptanceToDefaults()
	{
		for(Currency currency : currencies)
		{
			if(defaultAcceptance.containsKey(currency))
			{
				acceptsCurrency.put(currency, defaultAcceptance.get(currency));
			}
		}
		System.out.println("Accepts Default");
		save();
	}
	/** Does not work for Critiques or Comments */
	synchronized void hasReceived(Currency currency)
	{
		if(currency == Currency.CRITIQUE) return;
		System.out.println(name + " Received " + currency.toString());
		receivedCounts.put(currency, numReceived(currency) + 1);
		points -= currency.getCost();
		setModified();
	}
	@Override
	public void refund(Currency currency)
	{
		receivedCounts.put(currency, numReceived(currency) - 1);
		points += currency.getCost();
		setModified();
	}
	synchronized void removeCritique(Critique critique)
	{
		System.out.println("Removing from deletion "+critique);
		pendingCritiques.remove(critique);
		setModified();
	}
	synchronized void acceptCritique(Critique critique)
	{
		receivedCounts.put(Currency.CRITIQUE, numReceived(Currency.CRITIQUE) + 1);
		points -= Currency.CRITIQUE.getCost();
		acceptedCritiques.add(critique);
		pendingCritiques.remove(critique);
		setModified();
	}

	private static final String LLAMA_TAG = "Llamas are awesome!";
	private static final String GMI_END = "\"";
	private static final String GMI_ID = "gmi-id=\"";
	private void scrapeLlamaUrl()
	{
		if(llamaUrl == NO_URL)
		{
			String text = WebScraper.get(getLlamasURL());
			int nearIndex = text.indexOf(LLAMA_TAG);
			int gmiId = text.indexOf(GMI_ID, nearIndex) + GMI_ID.length();
			try
			{
				llamaUrl = Integer.parseInt(text.substring(gmiId, text.indexOf(GMI_END, gmiId)));
			}
			catch(NumberFormatException e)
			{
				System.out.println(name+" has no Llama");
			}
			save();
		}
	}
	public synchronized String getWidgetText()
	{
		boolean acceptsWatches = accepts(Currency.WATCH);
		boolean acceptsLlamas = accepts(Currency.LLAMA);
		boolean acceptsFavs = accepts(Currency.FAV);
		boolean acceptsComments = accepts(Currency.COMMENT);
		if(!acceptsWatches && !acceptsLlamas && !acceptsFavs && !acceptsComments)
		{
			return "";
		}
		scrapeLlamaUrl();
		StringBuilder builder = new StringBuilder();
		builder.append(":icon");
		builder.append(name);
		builder.append(":");
		if(acceptsWatches)
		{
			builder.append("&nbsp;<a href=\"http://");
			builder.append(name);
			builder.append(".deviantart.com\" target=\"_blank\" class=\"smbutton smbutton-white\"><span class=\"post\">:+devwatch: Watch</span></a>");
		}
		else
		{
			builder.append(StringOps.buttonWidthInNBSP());
		}
		if(acceptsLlamas)
		{
			builder.append("&nbsp;<a href=\"http://");
			builder.append(name);
			builder.append(".deviantart.com/badges/");
			builder.append(llamaUrl);
			builder.append("/\" target=\"_blank\" class=\"smbutton smbutton-white\"><span class=\"post\"><img src=\"http://fc06.deviantart.net/fs70/f/2013/245/0/1/llama_by_datrade-d6ktd98.png\"> Llama</span></a>");
		}
		else
		{
			builder.append(StringOps.buttonWidthInNBSP());
		}
		if(acceptsFavs)
		{
			builder.append("&nbsp;<a href=\"http://");
			builder.append(name);
			builder.append(".deviantart.com/gallery/\" target=\"_blank\" class=\"smbutton smbutton-white\"><span class=\"post\">:+fav: Fav</span></a>");
		}
		else
		{
			builder.append(StringOps.buttonWidthInNBSP());
		}
		if(acceptsComments)
		{
			builder.append("&nbsp;<a href=\"http://");
			builder.append(name);
			builder.append(".deviantart.com/gallery/\" target=\"_blank\" class=\"smbutton smbutton-white\"><span class=\"post\"><img src=\"http://fc09.deviantart.net/fs70/f/2013/252/7/f/comment_by_datrade-d6lqayu.png\"> Comment</span></a>");
		}
		else
		{
			builder.append(StringOps.buttonWidthInNBSP());
		}
		builder.append("&nbsp;<sub>(");
		builder.append(StringOps.roundDown1(points));
		builder.append(")</sub> <br>");
		
		return builder.toString();
	}
	
	public String watchLink(String tag)
	{
		return "<a href=\"http://" + name + ".deviantart.com\" target=\"_blank\">" + tag + "</a>";
	}
	public String llamaLink(String tag)
	{
		return "<a href=\"http://" + name + ".deviantart.com/badges/"+llamaUrl+"/\" target=\"_blank\">"+ tag +"</a>";
	}
	public String favLink(String tag)
	{
		return "<a href=\"http://" + name + ".deviantart.com/gallery/\" target=\"_blank\">" + tag + "</a>";
	}
	
	public synchronized String getFinishedMessage()
	{
		StringBuilder comment = new StringBuilder();
		comment.append("Hey, :icon" + name + ": :aww: ");
		addReasonForFinishing(comment);
		comment.append("You've received "+buildReceivedString()+"! \n\n - automated by Program Alice ");
		return comment.toString();
	}
	private void addReasonForFinishing(StringBuilder comment)
	{
		if(beingRefunded)
		{
			comment.append("Your refund was processed successfully!\n\n");
		}
		else
		{
			comment.append("Just a notice that your credit has now run out.\n\n");
		}
	}
	public synchronized String getRefundReply()
	{
		StringBuilder message = new StringBuilder();
		if(points >= 1)
		{
			message.append("Your refund will be processed shortly!");
		}
		else
		{
			message.append("You don't have any points to refund!");
		}
		return message.toString();
	}
	public String getRefundProcessed()
	{
		return "Your refund has been processed!";
	}
	
	private String buildReceivedString()
	{
		ArrayList<String> perType = new ArrayList<String>(); //builds the string for each receivable type of item
		for(Currency type : currencies)
		{
			int count = numReceived(type);
			switch(count)
			{
			case 0: break;
			case 1: perType.add(count+" "+type.toString()); break; //a tiny problem is left if the person received only one critique
			default : perType.add(count+" "+receivedPlural.get(type)); break;
			}
		}
		return StringOps.listUp(perType);
	}
	public synchronized String getStats()
	{
		StringBuilder message = new StringBuilder();
		message.append("Your current balance with Alice is: "+StringOps.roundDown2(points)+" :points:\n\n");
		message.append("During the time you have been featured, you have received:\n");
		message.append(Currency.assembleList(receivedCounts));
		message.append("\n\n");
		message.append("You currently accept "+acceptanceList()+".");
		return message.toString();
	}
	
	@Override
	public int compareTo(Payer otherPayer) 
	{
		if(otherPayer.points > points)
		{
			return 1;
		}
		else if(otherPayer.points < points)
		{
			return -1;
		}
		return 0;
	}
	
	synchronized ArrayList<String> getFeatures(FeatureCategory category)
	{
		ArrayList<String> features = new ArrayList<String>();
		for(TimedFeature feature : timedFeatures)
		{
			features.add("<div class=\"drawPlz\"><div class=\"topbar c pp\">"+feature.display(category)+"</div></div>\n");
		}
		return features;
	}
	//Returns whether there was a new feature
	private boolean updateFeatures(String message, double amount)
	{
		//Disabled
		/*
		TimedFeature newFeature = TimedFeature.getInstance((int) amount, name, message);
		if(newFeature != null)
		{
			timedFeatures.add(newFeature);
			points -= amount;
			return true;
		}
		*/
		return false;
	}
	
	String getCritiqueString()
	{
		if(points < Currency.CRITIQUE.getCost())
		{
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append(":dev");
		builder.append(name);
		builder.append(": ");
		builder.append("<small>(");
		builder.append(StringOps.roundDown2(points));
		builder.append(")</small>\n");
		if(!artworks.isEmpty())
		{
			for(String artwork : artworks)
			{
				builder.append(artwork);
			}
		}
		else
		{
			builder.append(galleryLink("Gallery"));
		}
		builder.append("\n");
		return builder.toString();
	}
	
	public boolean wantsCritiqueOn(int id)
	{
		return artworks.contains(toArtworkString(id));// || artworks.isEmpty();
	}
	
	private String toArtworkString(int id)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(":thumb");
		builder.append(id);
		builder.append(":");
		return builder.toString();
	}
	
	public void updateArtwork(String message)
	{
		Set<String> artworks = parseArtwork(message);
		if(!artworks.isEmpty())
		{
			this.artworks = artworks; //replace old artworks with new ones
		}
	}
	public void removeArtworks(ArrayList<Integer> closedArtworks)
	{
		for(int closed : closedArtworks)
		{
			artworks.remove(toArtworkString(closed));
		}
		save();
	}
	private static final String EXPANDED_THUMBS_TAG = "deviationid:\"";
	private static final String EXPANDED_THUMBS_END = "\"";
	private Set<String> parseArtwork(String message)
	{
		Set<String> artworks = new HashSet<String>();
		int index = 0;
		while((index = message.indexOf(ARTWORK_TAG, index)) != -1)
		{
			artworks.add(message.substring(index, message.indexOf(ARTWORK_END, index + 1)) + ARTWORK_END);
			index ++;
			if(artworks.size() > MAX_ARTWORKS) break;
		}
		//For #prefs
		while((index = message.indexOf(EXPANDED_THUMBS_TAG, index)) != -1)
		{
			artworks.add(message.substring(index, message.indexOf(EXPANDED_THUMBS_END, index + 1)) + EXPANDED_THUMBS_END);
			index ++;
			if(artworks.size() > MAX_ARTWORKS) break;
		}
		return artworks;
	}
	
	public synchronized boolean submitCritique(Critique critique)
	{
		if(accepts(Currency.CRITIQUE) && !critiqueHistory.contains(critique.getCritiqueId()))
		{
			System.out.println("Received "+critique);
			points -= Currency.CRITIQUE.getCost();
			setModified();
			freshCritiques.add(critique);
			critiqueHistory.add(critique.getCritiqueId());
			return true;
		}
		return false;
	}
	public synchronized Set<Critique> getFreshCritiques()
	{
		return freshCritiques;
	}
	public synchronized Set<Critique> getPendingCritiques()
	{
		return pendingCritiques;
	}
	public synchronized void flushMarked()
	{
		pendingCritiques.addAll(freshCritiques);
		freshCritiques.clear();
	}
	public synchronized void checkTimedFeatures()
	{
		Iterator<TimedFeature> iter = timedFeatures.iterator();
		while(iter.hasNext())
		{
			TimedFeature feature = iter.next();
			if(!feature.stillActive())
			{
				iter.remove();
				setModified();
			}
		}
	}
	private Set<String> saveTimedFeatures()
	{
		Set<String> zippedFeatures = new HashSet<String>();
		for(TimedFeature feature : timedFeatures)
		{
			zippedFeatures.add(feature.toString());
		}
		return zippedFeatures;
	}
	private void readTimedFeatures(Set<String> timedFeatureStrings)
	{
		for(String featureString : timedFeatureStrings)
		{
			timedFeatures.add(new CommissionFeature(featureString));
		}
	}
	
	public static void makeTable(SQLDatabase database)
	{
		database.executeUpdate("create table payers("
				+ COL_NAME + " varchar(64),"
				+ COL_POINTS + " int,"
				+ COL_RECEIVED_WATCHES + " int,"
				+ COL_RECEIVED_LLAMAS + " int,"
				+ COL_RECEIVED_FAVS + " int,"
				+ COL_RECEIVED_CRITIQUES + " int,"
				+ COL_RECEIVED_COMMENTS + " int,"
				+ COL_ACCEPTS_WATCHES + " int," //boolean (0, 1)
				+ COL_ACCEPTS_LLAMAS + " int," 
				+ COL_ACCEPTS_FAVS + " int,"
				+ COL_ACCEPTS_CRITIQUES + " int,"
				+ COL_ACCEPTS_COMMENTS + " int,"
				+ COL_LAST_PAYMENT + " varchar(1024),"
				+ COL_LLAMA_URL + " int,"
				+ COL_ARTWORKS + " blob,"
				+ COL_PENDING_CRITIQUES + " blob,"
				+ COL_TIMED_FEATURES + " blob,"
				+ COL_CRITIQUE_HISTORY + " blob);");
	}
	
	private void loadDB(SQLDatabase database)
	{
		synchronized(database)
		{
			ResultSet rs = database.executeQuery("select * from "+PayerDatabase.TABLE+" where "+COL_NAME+"=\""+name+"\"");
			try
			{
				if(rs.next()) //only one entry expected
				{
					//Remove cast after legacy data is gone
					points = (float) rs.getDouble(COL_POINTS);
					receivedCounts.put(Currency.WATCH, rs.getInt(COL_RECEIVED_WATCHES));
					receivedCounts.put(Currency.LLAMA, rs.getInt(COL_RECEIVED_LLAMAS));
					receivedCounts.put(Currency.FAV, rs.getInt(COL_RECEIVED_FAVS));
					receivedCounts.put(Currency.CRITIQUE, rs.getInt(COL_RECEIVED_CRITIQUES));
					receivedCounts.put(Currency.COMMENT, rs.getInt(COL_RECEIVED_COMMENTS));
					acceptsCurrency.put(Currency.WATCH, rs.getInt(COL_ACCEPTS_WATCHES) == 1);
					acceptsCurrency.put(Currency.LLAMA, rs.getInt(COL_ACCEPTS_LLAMAS) == 1);
					acceptsCurrency.put(Currency.FAV, rs.getInt(COL_ACCEPTS_FAVS) == 1);
					acceptsCurrency.put(Currency.CRITIQUE, rs.getInt(COL_ACCEPTS_CRITIQUES) == 1);
					acceptsCurrency.put(Currency.COMMENT, rs.getInt(COL_ACCEPTS_COMMENTS) == 1);
					lastPayment = new Transaction(rs.getString(COL_LAST_PAYMENT));
					llamaUrl = rs.getInt(COL_LLAMA_URL);
					artworks = Blob.decompress(rs.getString(COL_ARTWORKS));
					critiqueHistory = Blob.decompressIntSet(rs.getString(COL_CRITIQUE_HISTORY));
					readTimedFeatures(Blob.decompress(rs.getString(COL_TIMED_FEATURES)));
					pendingCritiques = readCritiques(Blob.decompress(rs.getString(COL_PENDING_CRITIQUES)));
				}
				else
				{
					//Initialize
					initData();
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void save()
	{
		database.executeUpdate("delete from "+PayerDatabase.TABLE+" where "+COL_NAME+" = "+SQLUtils.format(name));
		database.executeUpdate(getSQLSave());
	}
	
	/** MAKE SURE THINGS(MAPS) ARE SORTED IF ORDER MATTERS */
	public String getSQLSave()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("insert into "+PayerDatabase.TABLE+" values("
				+ SQLUtils.format(name) +","
				+ points + ","
				+ numReceived(Currency.WATCH) + ","
				+ numReceived(Currency.LLAMA) + ","
				+ numReceived(Currency.FAV) + ","
				+ numReceived(Currency.CRITIQUE) + ","
				+ numReceived(Currency.COMMENT) + ","
				+ (accepts(Currency.WATCH) ? 1 : 0) + ","
				+ (accepts(Currency.LLAMA) ? 1 : 0) + ","
				+ (accepts(Currency.FAV) ? 1 : 0) + ","
				+ (accepts(Currency.CRITIQUE) ? 1 : 0) + ","
				+ (accepts(Currency.COMMENT) ? 1 : 0) + ","
				+ SQLUtils.format(lastPayment.toString().replace("'", "''")) +","
				+ llamaUrl +","
				+ SQLUtils.format(Blob.compress(artworks)) + ","
				+ SQLUtils.format(Blob.compress(pendingCritiques)) + ","
				+ SQLUtils.format(Blob.compress(saveTimedFeatures())) + ","
				+ SQLUtils.format(Blob.compress(critiqueHistory)) + ");");
		return builder.toString();
	}
	/** MAKE SURE THINGS(MAPS) ARE SORTED IF ORDER MATTERS */
	/*
	public synchronized void save(File f) throws IOException
	{
		BufferedWriter buff = new BufferedWriter(new FileWriter(f));
		MySerializer.write(buff, name);
		MySerializer.write(buff, Float.toString(points));
		MySerializer.write(buff, Integer.toString(numReceived(Currency.LLAMA)));
		MySerializer.write(buff, Integer.toString(numReceived(Currency.FAV)));
		MySerializer.write(buff, Integer.toString(numReceived(Currency.WATCH)));
		MySerializer.write(buff, lastPayment.toString());
		MySerializer.write(buff, Boolean.toString(accepts(Currency.WATCH)));
		MySerializer.write(buff, Boolean.toString(accepts(Currency.LLAMA)));
		MySerializer.write(buff, Boolean.toString(accepts(Currency.FAV)));
		MySerializer.write(buff, Integer.toString(llamaUrl));
		MySerializer.write(buff, Integer.toString(numReceived(Currency.CRITIQUE)));
		MySerializer.write(buff, artworks);
		MySerializer.write(buff, pendingCritiques);
		MySerializer.write(buff, critiqueHistory);
		MySerializer.write(buff, accepts(Currency.CRITIQUE));
		saveTimedFeatures(buff);
		MySerializer.write(buff, Integer.toString(numReceived(Currency.COMMENT)));
		MySerializer.write(buff, Boolean.toString(accepts(Currency.COMMENT)));
		MySerializer.write(buff, favLink == null ? "" : favLink);
		buff.close();
	}
	*/
	
	public synchronized void load(File f)
	{
		try
		{
			BufferedReader buff = new BufferedReader(new FileReader(f));
			try
			{
				name = MySerializer.read(buff);
				points = Float.parseFloat(MySerializer.read(buff));
				receivedCounts.put(Currency.LLAMA, Integer.parseInt(MySerializer.read(buff)));
				receivedCounts.put(Currency.FAV, Integer.parseInt(MySerializer.read(buff)));
				receivedCounts.put(Currency.WATCH, Integer.parseInt(MySerializer.read(buff)));
				lastPayment = new Transaction(MySerializer.read(buff));
				acceptsCurrency.put(Currency.WATCH, Boolean.parseBoolean(MySerializer.read(buff)));
				acceptsCurrency.put(Currency.LLAMA, Boolean.parseBoolean(MySerializer.read(buff)));
				acceptsCurrency.put(Currency.FAV, Boolean.parseBoolean(MySerializer.read(buff)));
				llamaUrl = Integer.parseInt(MySerializer.read(buff));
				receivedCounts.put(Currency.CRITIQUE, Integer.parseInt(MySerializer.read(buff)));
				MySerializer.read(buff, artworks);
				readSetCritique(buff, pendingCritiques);
				MySerializer.readSetInteger(buff, critiqueHistory);
				acceptsCurrency.put(Currency.CRITIQUE, Boolean.parseBoolean(MySerializer.read(buff)));
				Set<String> timedFeatureFiles = new HashSet<String>();
				MySerializer.read(buff, timedFeatureFiles);
				readTimedFeatures(timedFeatureFiles);
				receivedCounts.put(Currency.COMMENT, Integer.parseInt(MySerializer.read(buff)));
				acceptsCurrency.put(Currency.COMMENT, Boolean.parseBoolean(MySerializer.read(buff)));
				favLink = MySerializer.read(buff);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.err.println("Could not read everything, defaulting some values for "+name);
			}
			buff.close();
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private static Set<Critique> readCritiques(Set<String> rawCritiques)
	{
		Set<Critique> critiques = new HashSet<>();
		for(String raw : rawCritiques)
		{
			critiques.add(Critique.parse(raw));
		}
		return critiques;
	}
	private static void readSetCritique(BufferedReader reader, Set<Critique> set) throws IOException, ParseException
	{
		String line;
		while((line = readLine(reader)) != null)
		{
			set.add(Critique.parse(line));
		}
	}
	
	private static final String COMPONENT_DELIM = ";";
	private static boolean isEnd(String line)
	{
		return line.equals(COMPONENT_DELIM);
	}
	//This read line terminates on the end of a components, as well as EOF
	private static String readLine(BufferedReader reader) throws IOException
	{
		String line = reader.readLine();
		if(isEnd(line)) 
		{
			return null;
		}
		return line;
	}
	@Override
	public String toString()
	{
		return name;
	}
}

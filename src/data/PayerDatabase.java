package data;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import event.PaymentEvents;
import sql.SQLDatabase;
import sql.SQLUtils;
import utils.StringOps;
import worker.NoteWriter;

public class PayerDatabase
{
	static final String TABLE = "payers";

	private static final File WIDGET_FILE = new File("data\\deviantsWidget.txt");
	private static final File CRITIQUABLE_FILE = new File("data\\critiquesWidget.txt");
	private static final File COMMISSION_ADS_FILE = new File("data\\commissionAds.txt");
	private static final File ARTWORK_FEATURES_FILE = new File("data\\artworkFeatures.txt");
	
	private PayerHistory pHistory = new PayerHistory();
	
	private HashMap<String, Payer> payers = new HashMap<String, Payer>();
	
	private SQLDatabase database;
	private PaymentEvents paymentEvents;
	private NoteWriter noteWriter;
	private RecordKeeper recorder;
	
	//Whether the database was modified (since last clearing)
	private boolean modifiedFlag = false;		public synchronized boolean getModified() { return modifiedFlag; }
												public synchronized void clearModified() { modifiedFlag = false; }
												private synchronized void modified() { modifiedFlag = true; }
	
	public PayerDatabase(SQLDatabase database, PaymentEvents paymentEvents, NoteWriter noteWriter, RecordKeeper recorder)
	{
		this.database = database;
		this.paymentEvents = paymentEvents;
		this.noteWriter = noteWriter;
		this.recorder = recorder;
		load();
	}
	
	private synchronized void load()
	{
		ResultSet allPayers = database.executeQuery("select * from "+TABLE);
		Set<String> payerNames = new HashSet<>();
		try
		{
			while(allPayers.next())
			{
				payerNames.add(allPayers.getString(Payer.COL_NAME));
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		for(String payer : payerNames)
		{
			loadPayer(payer);
		}
	}
	//returns whether to delete this file
	private boolean loadPayer(String name)
	{
        Payer p = new Payer(database, name);
        add(p);
        boolean deleteMe = shouldRemove(p.getName());
        if(deleteMe)
        {
        	removePayer(p);
        }
        return deleteMe;
	}
	public synchronized void save() throws IOException
	{
		synchronized(payers)
		{
			for(String payerName : payers.keySet())
			{
				Payer payer = payers.get(payerName);
				payer.checkTimedFeatures();
				if(payer.hasBeenModified())
				{
					System.out.println("Saving "+payerName);
					payer.save();
					payer.clearModified();
				}
			}
		}
		pHistory.save();
	}

	public synchronized void processTransaction(Transaction transaction)
	{
		transaction.saveInto(database);
		String payerName = transaction.getOrigin().toLowerCase();
		modified();
		boolean isGift = transaction.getMessage().toLowerCase().contains("gift") 
				&& transaction.extractDevName() != null
				&& !transaction.extractDevName().equals(payerName); //no self gifting
		if(isGift)
		{
			payerName = transaction.extractDevName();
		}
		boolean isNewPayer = false;
		if(!payerName.contains("<img"))
		{
			if(!has(payerName)) //idk how database got corrupted
			{
				System.out.println("Adding Payer "+payerName);
				if(!pHistory.hasPayer(payerName)) //must be new payer
				{
					recorder.addPayer(); //to be worth recording
					isNewPayer = true;
				}
				pHistory.addPayer(payerName);
				add(payerName);
			}
			if(isGift)
			{
				get(payerName).acceptGift(transaction, recorder, noteWriter);
			}
			else
			{
				get(payerName).acceptTransaction(transaction, paymentEvents, noteWriter, recorder, isNewPayer);
			}
			//
		}

	}
	
	public String getCritiqueMe() throws IOException
	{
		Map<String, String> replace = new HashMap<String, String>();
		replace.put("PutDeviantsHere", critiquablePayers());
		return StringOps.assembleDisplay(CRITIQUABLE_FILE, replace);
	}
	//Returns a list of all payers who accept critiques
	private synchronized ArrayList<Payer> getCritiquable()
	{
		ArrayList<Payer> deviants = new ArrayList<Payer>();
		for(Payer p : payers.values())
		{
			if(p.accepts(Currency.CRITIQUE) && p.hasArtwork())
			{
				deviants.add(p);
			}
		}
		Collections.sort(deviants);
		return deviants;
	}

	private String critiquablePayers()
	{
		StringBuilder builder = new StringBuilder();
		ArrayList<Payer> deviants = getCritiquable();
		if(deviants.isEmpty())
		{
			builder.append("(None Currently)");
		}
		for(Payer dev : deviants)
		{
			builder.append(dev.getCritiqueString());
		}
		return builder.toString();
	}

	public String commissionAds() throws IOException
	{
		Map<String, String> replace = new HashMap<String, String>();
		ArrayList<String> featuresList = featuresList(FeatureCategory.COMMISSION);
		int cutoff = featuresList.size() / 2;
		replace.put("PutCommissionsHere1", appendFeatures(featuresList.subList(0, cutoff)));
		replace.put("PutCommissionsHere2", appendFeatures(featuresList.subList(cutoff, featuresList.size())));
		return StringOps.assembleDisplay(COMMISSION_ADS_FILE, replace);
	}
	
	public String artworkFeatures() throws IOException
	{
		Map<String, String> replace = new HashMap<String, String>();
		//replace.put("PutArtworksHere", appendFeatures(FeatureCategory.ARTWORK));
		return StringOps.assembleDisplay(ARTWORK_FEATURES_FILE, replace);
	}
	
	private String appendFeatures(List<String> features)
	{
		StringBuilder builder = new StringBuilder();
		for(String feature : features)
		{
			builder.append(feature);
		}
		return builder.toString();
	}
	private ArrayList<String> featuresList(FeatureCategory category)
	{
		ArrayList<String> features = new ArrayList<String>();
		for(Payer payer : payers.values())
		{
			if(payer.hasTimedFeatures())
			{
				features.addAll(payer.getFeatures(category));
			}
		}
		return features;
	}
	
	public String getDonatePage() throws IOException
	{
		Map<String, String> replace = new HashMap<String, String>();
		replace.put("PutPayerHere", appendPayers());
		return StringOps.assembleDisplay(WIDGET_FILE, replace);
	}
	private String appendPayers()
	{
		StringBuilder builder = new StringBuilder();
		boolean addedPayer = false;
		for(Payer payer : sortedPayers())
		{
			if(payer.getPoints() > 0)
			{
				builder.append(payer.getWidgetText());
				addedPayer = true;
			}
		}
		if(!addedPayer)
		{
			builder.append("(None Currently)");
		}
		return builder.toString();
	}
	public ArrayList<Payer> sortedPayers()
	{
		ArrayList<Payer> sortedPayers = new ArrayList<Payer>();
		for(Payer payer : payers.values())
		{
			sortedPayers.add(payer);
		}
		Collections.sort(sortedPayers);
		return sortedPayers;
	}
	/** Adds an Payer by name without symbol */
	public void add(String name)
	{
		synchronized(payers)
		{
			if(!has(name))
			{
				System.out.println("Making new Payer for "+name);
				payers.put(name, new Payer(database, name));
			}
		}
	}
	private void add(Payer p)
	{
		synchronized(payers)
		{
			if(!has(p.getName()))
			{
				payers.put(p.getName(), p);
			}
		}
	}
	
	public Collection<Payer> getPayers()
	{
		synchronized(payers)
		{
			return payers.values();
		}
	}

	public Payer get(String name)
	{
		return payers.get(name);
	}
	public boolean has(String payer)
	{
		return payers.containsKey(payer);
	}
	
	private boolean shouldRemove(String payerName)
	{
		synchronized(payers)
		{
			Payer payer = payers.get(payerName);
			if(payer.cantBuyMore())
			{
				removePayer(payer);
				return true;
			}
		}
		return false;
	}
	
	//Removes payer from the system and database
	private void removePayer(Payer payer)
	{
		System.out.println(payer.getName()+" cannot afford anything more");
    	database.executeUpdate("delete from "+TABLE+" where "+Payer.COL_NAME+" = "+SQLUtils.format(payer.getName()));
		payers.remove(payer.getName());
		noteWriter.send(payer.getName(), "Thanks!", payer.getFinishedMessage());
	}

	/** Does not work for Critiques */
	public void hasReceived(String payerName, Currency currency)
	{
		if(currency == Currency.CRITIQUE) return;
		if(has(payerName))
		{
			modified();
			recorder.add(currency);
			payers.get(payerName).hasReceived(currency);
			shouldRemove(payerName);
		}
	}
	/** Payer takes the critique into the pending stage */
	public void submitCritique(String payerName, Critique critique)
	{
		if(has(payerName) && payers.get(payerName).submitCritique(critique))
		{
			modified();
		}
	}
	/** Payer has approved the critique */
	public void acceptCritique(Critique critique)
	{
		recorder.add(Currency.CRITIQUE);
		if(has(critique.getDeviant()))
		{
			payers.get(critique.getDeviant()).acceptCritique(critique);
			shouldRemove(critique.getDeviant());
			modified();
		}
	}
	/** Payer has deleted the deviation */
	public void removeCritique(Critique critique)
	{
		if(has(critique.getDeviant()))
		{
			payers.get(critique.getDeviant()).removeCritique(critique);
			modified();
		}
	}
	public void refund(Payer refundingPayer)
	{
		synchronized(refundingPayer)
		{
			assert(has(refundingPayer.getName()));
			if(refundingPayer.getPoints() >= 1)
			{
				paymentEvents.addPayment(refundingPayer, refundingPayer.calculateRefund(), refundingPayer.getRefundProcessed());
				refundingPayer.processRefund();
				shouldRemove(refundingPayer.getName());
				modified();
			}
		}
	}
}

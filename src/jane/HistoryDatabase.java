package jane;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bryan.WatchList;
import data.Currency;

public class HistoryDatabase 
{
	private PayerReferences payerReferences;
	
	private Map<String, PayerHistory> histories = new HashMap<String, PayerHistory>();
	
	public HistoryDatabase()
	{
		try
		{
			payerReferences = new PayerReferences();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		load();
	}
	public Set<String> missingWatchers(String payer, WatchList watchList)
	{
		if(!histories.containsKey(payer))
		{
			return new HashSet<String>();
		}
		return histories.get(payer).getUnwatchers(watchList);
	}
 	public void foundTransaction(Currency currency, String payer, String earner)
	{
		if(histories.containsKey(payer))
		{
			histories.get(payer).addTransaction(currency, earner);
		}
	}
	public boolean transactionExists(Currency currency, String payer, String earner)
	{
		if(!histories.containsKey(payer))
		{
			return false;
		}
		return histories.get(payer).hasTransactionFrom(currency, earner);
	}
	private void load()
	{
		for(String payer : payerReferences.getPayers())
		{
			histories.put(payer, PayerHistory.getInstance(payer));
		}
	}
	void updateSequentially()
	{
		for(String history : histories.keySet())
		{
			histories.get(history).update();
		}
	}
}

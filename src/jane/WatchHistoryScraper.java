package jane;

import data.Currency;

public class WatchHistoryScraper extends WatchPageScraper
{
	private PayerHistory payerHistory;
	WatchHistoryScraper(PayerHistory payerHistory, int offset) 
	{
		super(payerHistory.getName(), offset);
		this.payerHistory = payerHistory;
	}
	@Override
	protected void processWatcher(String name)
	{
		payerHistory.addTransaction(Currency.WATCH, name);
	}
}

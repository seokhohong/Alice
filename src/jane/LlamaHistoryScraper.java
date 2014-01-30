package jane;

import data.Currency;

public class LlamaHistoryScraper extends LlamaPageScraper 
{
	private PayerHistory payerHistory;
	LlamaHistoryScraper(PayerHistory payerHistory, int offset) 
	{
		super(payerHistory.getName(), offset);
		this.payerHistory = payerHistory;
	}

	@Override
	protected void processLlama(String name)
	{
		payerHistory.addTransaction(Currency.LLAMA, name);
	}
}

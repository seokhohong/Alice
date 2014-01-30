package scraper;

import utils.StringOps;
import data.Currency;
import data.Earner;
import data.PayerDatabase;

/** Cannot inherit from LlamaPageScraper because this works in the reverse direction, searching earner pages rather than payer pages*/
public class LlamaWorkScraper extends Thread
{
	public static void main(String[] args)
	{
		//new LlamaWorkScraper(null, new Earner("lunagal1"), 0).start();
	}
	private PayerDatabase pDatabase;
	
	private int offset;
	private Earner earner;
	
	public LlamaWorkScraper(PayerDatabase pDatabase, Earner earner, int offset)
	{
		this.pDatabase = pDatabase;
		this.earner = earner;
		this.offset = offset;
	}
	@Override
	public void run()
	{
		String pageSource = WebScraper.get(earner.getLlamasURL(offset));
		parseLlamas(pageSource, earner);
	}
	
	private static final String LLAMA_TAG = "Gave a  <strong>Llama Badge</strong> to ";
	private static final String NARROW_IN = ".deviantart.com/\" >";
	private static final String NARROW_IN_END = "</a><span class=";

	private void parseLlamas(String pageSource, Earner earner)
	{
		int index = 0;
		while((index = pageSource.indexOf(LLAMA_TAG, index))>-1)
		{
			String payerName = StringOps.textBetween(pageSource, NARROW_IN, NARROW_IN_END, index).toLowerCase();
			processLlama(payerName, earner);
			index++;
		}
	}
	private void processLlama(String payerName, Earner earner)
	{
		if(pDatabase.has(payerName) && pDatabase.get(payerName).accepts(Currency.LLAMA))
		{
			System.out.println("Attempting to record Llama from "+earner.getName()+" to "+payerName);
			if(earner.addLlamaFrom(pDatabase.get(payerName)))
			{
				pDatabase.hasReceived(payerName, Currency.LLAMA);
			}
		}
	}
}

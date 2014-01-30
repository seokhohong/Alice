package jane;

import scraper.WebScraper;
import utils.StringOps;
import data.Artist;

public abstract class LlamaPageScraper extends Thread
{
	private String name;
	private int offset;
	protected LlamaPageScraper(String name, int offset)
	{
		this.name = name;
		this.offset = offset;
	}
	@Override
	public void run()
	{
		parseLlamas(WebScraper.get(getUrl(), false));
	}
	public String getUrl()
	{
		return new Artist(name).getLlamasURL(offset * LlamaHistory.getLlamasPer());
	}
	private static final String LLAMA_TAG = "Received a  <strong>Llama Badge</strong> from ";
	private static final String LLAMA_NARROW = "<a class=\"u\" href=\"http://";
	private static final String LLAMA_END = ".";
	private void parseLlamas(String pageSource)
	{
		int index = 0;
		while((index = pageSource.indexOf(LLAMA_TAG, index)) != -1)
		{
			String name = StringOps.textBetween(pageSource, LLAMA_NARROW, LLAMA_END, index);
			processLlama(name);
			index ++ ;
		}
	}
	protected abstract void processLlama(String name);
}

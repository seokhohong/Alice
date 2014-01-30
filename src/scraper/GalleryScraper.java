package scraper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.ScrapingUtils;
import worker.Multitasker;

public class GalleryScraper 
{
	public static void main(String[] args)
	{
		Map<String, List<Artwork>> works = new GalleryScraper("http://theexhibition.deviantart.com/gallery/?set=46697784").getAll();
		for(String artist : works.keySet())
		{
			System.out.println("For artist "+artist);
			for(Artwork artwork : works.get(artist))
			{
				System.out.println("\t"+artwork.getThumb()+" "+artwork.getCategory());
			}
		}
	}
	private static final String THUMB_TAG = "collect_rid=\"1:";
	private static final String USER_TAG = "username=\"";
	private static final String CATEGORY_TAG = "category=\"";
	
	private static final int NUM_PER_PAGE = 24;
	
	private String url;
	private Map<String, List<Artwork>> artworks = new HashMap<>();
	
	public GalleryScraper(String url)
	{
		this.url = url;
	}
	public Map<String, List<Artwork>> getAll()
	{
		int numPages = scrapeNumPages();
		Multitasker multitasker = new Multitasker();
		for(int a = numPages ; a --> 0; )
		{
			multitasker.load(new PageScraper(url+"&offset="+a * NUM_PER_PAGE));
		}
		multitasker.done();
		return artworks;
	}
	private int scrapeNumPages()
	{
		String pageSource = WebScraper.get(url);
		int index = pageSource.indexOf("</a></li><li class=\"next\">");
		int start = pageSource.lastIndexOf(">", index);
		String num = pageSource.substring(start + 1, index);
		if(num.equals("Previous"))
		{
			return 1;
		}
		return Integer.parseInt(num);
	}
	private synchronized void addArtwork(String artist, String artwork, String category)
	{
		if(artworks.containsKey(artist))
		{
			artworks.get(artist).add(new Artwork(artwork, category));
		}
		else
		{
			List<Artwork> herArtworks = new ArrayList<>();
			herArtworks.add(new Artwork(artwork, category));
			artworks.put(artist, herArtworks);
		}
	}
	public class Artwork
	{
		private String thumb;			public String getThumb() { return thumb; }
		private String category;		public String getCategory() { return category; }
		Artwork(String thumb, String category)
		{
			this.thumb = thumb;
			this.category = category;
		}
	}
	private class PageScraper extends Thread
	{
		private String pageUrl;
		private PageScraper(String pageUrl)
		{
			this.pageUrl = pageUrl;
		}
		@Override
		public void run()
		{
			String pageSource = WebScraper.get(pageUrl);
			for(int index : ScrapingUtils.findAll(pageSource, THUMB_TAG))
			{
				String thumb = ScrapingUtils.textBetween(pageSource, THUMB_TAG, "\"", index);
				String artist = ScrapingUtils.textBetween(pageSource, USER_TAG, "\"", index).toLowerCase();
				String category = ScrapingUtils.textBetween(pageSource, CATEGORY_TAG, "/", index);
				addArtwork(artist, thumb, category);
			}
		}
	}
}

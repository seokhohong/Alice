package main;

import data.Artist;
import scraper.ArtistPropertiesScraper;

public class ScrapeArtist 
{
	public static void main(String[] args)
	{
		new ScrapeArtist().go();
	}
	private void go()
	{
		new ArtistPropertiesScraper(new Artist("SpecialNature")).start();
	}
}

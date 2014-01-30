package data;

import java.io.File;

public class Artist 
{
	protected String name;							public String getName() { return name; }
	
	boolean canEarn = true;							public void markCannotEarn() { canEarn = false; }
	
	boolean cannotFindDonationWidget = false;		public boolean hasDonationWidget() { return !cannotFindDonationWidget; }
													public void cannotFindDonation() { cannotFindDonationWidget = true; }
													public boolean donationFound() { return cannotFindDonationWidget; }
	
	/** Override this */
	protected Artist(File file)
	{
		
	}
													
	public Artist(String name)
	{
		this.name = name.toLowerCase();
	}
	
	public String getHomeURL()
	{
		return "http://www."+name+".deviantart.com/";
	}
	
	public String getLlamasURL()
	{
		return getLlamasURL(0);
	}
	
	public String getLlamasURL(int offset)
	{
		return "http://www."+name+".deviantart.com/badges/?offset="+offset;
	}
	
	public String getFavsURL(int offset)
	{
		return "http://www."+name+".deviantart.com/favourites/?catpath=/&offset="+offset;
	}
	
	public String getWatchesURL()
	{
		return getWatchesURL(0);
	}
	
	public String getWatchesURL(int offset)
	{
		return "http://www."+name+".deviantart.com/modals/watchers/?offset="+offset;
	}
	
	public String getMyGroupsURL()
	{
		return "http://www."+name+".deviantart.com/mygroups/";
	}
	
	public String getCritiqueListURL(int offset)
	{
		return "http://www."+name+".deviantart.com/critique/?offset="+offset;	
	}
	
	public String getActivityURL()
	{
		return "http://www."+name+".deviantart.com/activity/";
	}
	
	public String galleryLink(String text)
	{
		return " <a href=\"www." + name + ".deviantart.com/gallery/\" target=\"_blank\" >" + text + "</a>";
	}
	
	public boolean isWrittenIn(File file)
	{
		return file.getName().substring(0, file.getName().indexOf(".")).equals(name);
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
}

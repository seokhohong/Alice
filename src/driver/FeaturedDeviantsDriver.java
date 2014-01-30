package driver;

import java.io.IOException;

import main.Alice;
import main.Listener;
import data.PayerDatabase;

public class FeaturedDeviantsDriver extends ProfileUpdateDriver
{
	//private static final By EDIT_WIDGET_ELEM = By.cssSelector("a.gmbutton2.gmbutton2chaos");
	
	private PayerDatabase pDatabase;
	
	public FeaturedDeviantsDriver(Alice alice, DriverPool pool, PayerDatabase pDatabase, Listener listener) throws IOException
	{
		super(alice, pool, "deviantID", listener);
		this.pDatabase = pDatabase;
	}
	@Override
	public String toString()
	{
		return FeaturedDeviantsDriver.class.getName();
	}
	@Override
	public int minUpdateDelay()
	{
		return 1000 * 60 * 2;
	}
	@Override
	public String getDisplayText() throws IOException
	{
		return pDatabase.getDonatePage();
	}
	//Terrible fix
	int getEditOffset()
	{
		return 1;
	}
}

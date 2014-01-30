package jane;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;

/** Separates Jane from the PayerDatabase class */
public class PayerReferences 
{
	private static final File PAYERS_FOLDER = new File("data\\payer\\");
	private Collection<String> payers = new HashSet<String>();		public Collection<String> getPayers() { return payers; }

	public PayerReferences() throws IOException, ParseException
	{
		loadPayers();
	}
	private void loadPayers() throws IOException, ParseException
	{
		for(File file : PAYERS_FOLDER.listFiles()) 
		{
		    if(file.isFile()) 
		    { 
		    	payers.add(file.getName().split("\\.")[0]);
		    }
		} 
	}
}

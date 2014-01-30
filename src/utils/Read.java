package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import macro.Macro;

//Static class
public class Read 
{
	private Read() { throw new AssertionError("Do Not Instantiate Me"); }
	/** Reads from a file and returns the contents in an ArrayList<String>. Returns an empty ArrayList<String> if there is no file */
	public static ArrayList<String> fromFile(String filename, boolean readBlanks)
	{
		return fromFile(new File(filename), readBlanks);
	}
	public static ArrayList<String> fromFile(File f, boolean readBlanks)
	{
		ArrayList<String> strings = new ArrayList<String>();
		if(!f.exists())
		{
			return strings;
		}
		while(true)
		{
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line;
				while((line = reader.readLine())!=null)
				{
					//ignore comments and newlines
					if(!line.startsWith("#") && (!line.isEmpty() || readBlanks))
					{
						strings.add(line);
					}
				}
				reader.close();
				break;
			}
			catch(IOException e)
			{
				System.err.println("Error reading file "+f.getName()+".... attempting Again");
				Macro.sleep(1000);
			}
		}
		return strings;
	}
	public static ArrayList<String> fromFile(File f)
	{
		return fromFile(f, false);
	}
	public static ArrayList<String> fromFile(String filename)
	{
		return fromFile(filename, false);
	}
}

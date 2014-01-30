package jane;

import java.io.File;
import java.util.ArrayList;

import utils.*;

public class Decapitalizer 
{
	public static void main(String[] args)
	{
		new Decapitalizer().go();
	}
	private void go()
	{
		for(File file : new File("data\\history\\").listFiles())
		{
			ArrayList<String> lines = Read.fromFile(file);
			ArrayList<String> decap = new ArrayList<String>();
			for(String line : lines)
			{
				decap.add(line);
			}
			Write.toFile(file, decap);
		}
	}
}

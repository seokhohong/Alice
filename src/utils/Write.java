package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Write 
{
	public static void toFile(File file, String line)
	{
		try
		{
			BufferedWriter buff = new BufferedWriter(new FileWriter(file));
			buff.write(line);
			buff.close();
		} 
		catch(IOException e)
		{
			
		}
	}
	public static void toFile(File file, ArrayList<String> lines)
	{
		try
		{
			BufferedWriter buff = new BufferedWriter(new FileWriter(file));
			for(String line : lines)
			{
				buff.write(line);
				buff.newLine();
			}
			buff.close();
		} 
		catch(IOException e)
		{
			
		}
	}
}

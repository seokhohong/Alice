package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Append 
{
	public static void toFile(String filename, String line)
	{
		toFile(new File(filename), line);
	}
	public static void toFile(File file, String line)
	{
		try
		{
			BufferedWriter buff = new BufferedWriter(new FileWriter(file, true));
			buff.write(line);
			buff.newLine();
			buff.close();
		} 
		catch(IOException e)
		{
			
		}
	}
	public static void toFile(String filename, List<String> lines)
	{
		toFile(new File(filename), lines);
	}
	public static void toFile(File file, List<String> lines)
	{
		try
		{
			BufferedWriter buff = new BufferedWriter(new FileWriter(file, true));
			for(String line : lines)
			{
				buff.write(line);
			}
			buff.newLine();
			buff.close();
		} 
		catch(IOException e)
		{
			
		}
	}
}

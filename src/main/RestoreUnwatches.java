package main;

import java.io.File;
import java.util.ArrayList;

import utils.Read;

public class RestoreUnwatches 
{
	public static void main(String[] args)
	{
		new RestoreUnwatches().go();
	}
	private void go()
	{
		ArrayList<String> lines = Read.fromFile(new File("data\\misuzu.txt"));
		System.out.println(lines.size());
		
	}
}

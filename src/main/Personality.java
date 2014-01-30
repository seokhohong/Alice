package main;

import java.util.Random;

public class Personality 
{
	public Personality()
	{
		
	}
	
	private static String[] greetings = {"Hey there, ", "Hi, ", "Hey, ", "Thanks for your time, ", "Welcome back, "};
	private static String[] emotes = { ":D", ":happybounce:", ":huggle:", ":giggle:", ":dance:", ":meow:", ":dummy:", ":la:", ":squee:", ":aww:"};
	
	public String randomGreeting()
	{
		return randomArrayElem(greetings);
	}
	
	public String randomEmote()
	{
		return randomArrayElem(emotes);
	}
	
	private static String randomArrayElem(String[] arr)
	{
		Random rnd = new Random();
		return arr[rnd.nextInt(arr.length)];
	}
}

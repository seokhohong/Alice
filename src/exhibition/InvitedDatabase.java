package exhibition;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import utils.Append;
import utils.Read;

public class InvitedDatabase 
{
	private static final File FILE = new File("data\\invitedDatabase.txt");
	private Set<String> invited = new HashSet<>();
	InvitedDatabase()
	{
		load();
	}
	synchronized boolean hasInvited(String name)
	{
		return invited.contains(name);
	}
	synchronized void addInvited(String name)
	{
		invited.add(name);
		Append.toFile(FILE, name);
	}
	private void load()
	{
		invited.addAll(Read.fromFile(FILE));
	}
}

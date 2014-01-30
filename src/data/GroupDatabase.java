package data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import utils.StringOps;
import worker.NoteWriter;

public class GroupDatabase 
{
	private static final File GROUPS_FOLDER = new File("data\\group\\");
	private static final File WIDGET_FILE = new File("data\\groupsWidget.txt");
	
	private Map<String, Group> groups = new HashMap<String, Group>();
	private GroupHistory gHistory = new GroupHistory();
	
	private NoteWriter noteWriter;
	private RecordKeeper recorder;
	
	public GroupDatabase(NoteWriter noteWriter, RecordKeeper recorder)
	{
		this.noteWriter = noteWriter;
		this.recorder = recorder;
		load();
	}
	
	private void load()
	{
		ArrayList<File> filesToDelete = new ArrayList<File>();
		for(File file : GROUPS_FOLDER.listFiles()) 
		{
		    if(file.isFile()) 
		    { 
		    	if(loadGroup(file))
	    		{
		    		System.out.println("Deleting "+file);
	            	filesToDelete.add(file);
	    		}
		    }
		} 
		for(File f : filesToDelete)
		{
			f.delete();
		}
	}
	
	/** Adds a Group by name and description */
	public void add(String name, String description)
	{
		synchronized(groups)
		{
			if(!has(name))
			{
				System.out.println("Making new Group for "+name);
				groups.put(name, new Group(name, description));
			}
		}
	}
	
	private void add(Group group)
	{
		synchronized(groups)
		{
			if(!has(group.getName()))
			{
				groups.put(group.getName(), group);
			}
		}
	}
	
	public void clear()
	{
		synchronized(groups)
		{
			groups.clear();
		}
	}
	
	public Collection<Group> getGroups()
	{
		synchronized(groups)
		{
			return groups.values();
		}
	}

	public Group get(String name)
	{
		return groups.get(name);
	}
	public boolean has(String group)
	{
		return groups.containsKey(group);
	}
	
	//returns whether to delete this file
	private boolean loadGroup(File file)
	{
        Group group = new Group(file);
        add(group);
        boolean deleteMe = shouldRemove(group.getName());
        if(deleteMe)
        {
        	System.out.println("Deleting "+group.getName());
        	file.delete();
        }
        return deleteMe;
	}
	
	public String getFeaturePage() throws IOException
	{
		Map<String, String> replace = new HashMap<String, String>();
		replace.put("PutGroupsHere", appendGroups());
		return StringOps.assembleDisplay(WIDGET_FILE, replace);
	}
	
	private String appendGroups()
	{
		StringBuilder builder = new StringBuilder();
		boolean addedGroup = false;
		for(Group group : sortedGroups())
		{
			if(group.getPoints() > 0)
			{
				builder.append(group.getWidgetText()+"\n\n");
				addedGroup = true;
			}
		}
		if(!addedGroup)
		{
			builder.append("(None Currently)");
		}
		return builder.toString();
	}
	public ArrayList<Group> sortedGroups()
	{
		ArrayList<Group> sortedGroups = new ArrayList<Group>();
		for(Group group : groups.values())
		{
			sortedGroups.add(group);
		}
		Collections.sort(sortedGroups);
		return sortedGroups;
	}
	
	public synchronized void processTransaction(Transaction transaction)
	{
		addGroup(transaction);
		acceptTransaction(transaction);
	}
	//Looks for the first word that isn't the group tag (use this to guarantee higher up that there is, in fact, something else too)
	public String parseGroupName(String message)
	{
		String[] split = purgeNewlines(message).toLowerCase().split(" ");
		for(int a = 0; a < split.length; a++)
		{
			if(!split[a].equals("group"))
			{
				return split[a];
			}
		}
		//not reached
		return null;
	}
	private String purgeNewlines(String message)
	{
		return message.replace("<br>", " ").replace("</br>", " ");
	}

	private void addGroup(Transaction transaction)
	{
		String message = transaction.getMessage();
		String[] split = message.split(" ");
		String groupName = split[1].toLowerCase();
		String groupDescription = Group.parseDescription(message);
		if(!has(groupName))
		{
			System.out.println("Adding Group "+groupName);
			if(!gHistory.hasGroup(groupName)) //must be new group
			{
				recorder.addPayer(); //to be worth recording
			}
			gHistory.addGroup(groupName);
			add(groupName, groupDescription);
		}
	}
	
	//amount may be modified because of bonuses
	private void acceptTransaction(Transaction transaction)
	{
		System.out.println("Useful Transaction Found: "+transaction.toString());
		Group group = get(parseGroupName(transaction.getMessage()));
		group.acceptTransaction(transaction, noteWriter);
	}
	
	public boolean shouldRemove(String payerName)
	{
		synchronized(groups)
		{
			Group group = groups.get(payerName);
			if(group.cantBuyMore())
			{
				System.out.println(group.getName()+" cannot afford anything more");
				groups.remove(group.getName());
				deleteFile(group);
				noteWriter.send(group.getName(), "Thanks!", group.getFinishedMessage());
				return true;
			}
		}
		return false;
	}
	
	public synchronized void hasReceived(String groupName, Currency currency)
	{
		if(has(groupName))
		{
			recorder.add(currency);
			groups.get(groupName).hasReceived(currency);
			shouldRemove(groupName);
		}
	}
	
	private void deleteFile(Group group)
	{
		for(File file : GROUPS_FOLDER.listFiles()) 
		{
		    if(file.isFile() && fileBelongsTo(file, group)) 
		    { 
		    	System.out.println("Deleting "+group);
		    	file.delete();
		    }
		}
	}
	
	private boolean fileBelongsTo(File file, Group group)
	{
		return file.getName().substring(0, file.getName().indexOf(".")).equals(group.getName());
	}
	
	public synchronized void save() throws IOException
	{
		synchronized(groups)
		{
			for(String groupName : groups.keySet())
			{
				Group group = groups.get(groupName);
				if(group.getModified())
				{
					System.out.println("Saving "+groupName);
					group.save();
					group.clearModified();
				}
			}
		}
		gHistory.save();
	}
	
	
}

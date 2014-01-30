package data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.MyIO;
import utils.MySerializer;

/** Keeps track of critics to display */
public class ToppersDatabase implements MyIO
{
	private static final String DELIM = "--SubmissionDelim--";
	private static final int NUM_TOP = 14;
	
	private static final File CRITIC_WIDGET_FILE = new File("data\\featuredCriticsWidget.txt");;
	
	private File dataFile;
	
	private boolean modified = true;		private void setModified() { modified = true; }
											public synchronized void clearModified() { modified = false; }
											public synchronized boolean getModified() { return modified; }
	
	//NOT a good data structure, but used it to make serialization easier
	//Hashing Critic to time of submission
	private List<Submission> critics = new ArrayList<>();
	private List<Submission> commenters = new ArrayList<>();
	
	public ToppersDatabase(String dataFilename) throws IOException, ParseException
	{
		this.dataFile = new File(dataFilename);
		load(dataFile);
	}
	
	public synchronized void save() throws IOException
	{
		save(dataFile);
	}
	
	/** Should be added only once (currently in MarkCritiqueDriver) */
	public synchronized void addCritique(Critique critique)
	{
		critics.add(new Submission(critique.getEarnerName()));
		setModified();
	}
	/** Should be added only once (currently in MarkCritiqueDriver) */
	public synchronized void addComment(Comment comment)
	{
		commenters.add(new Submission(comment.getCommenter()));
		setModified();
	}
	
	public synchronized String getDisplay() throws IOException
	{
		BufferedReader buffReader = new BufferedReader(new FileReader(CRITIC_WIDGET_FILE));
		StringBuilder longLine = new StringBuilder();
		String line;
		while((line = buffReader.readLine())!=null)
		{
			if(line.contains("TopCritics"))
			{
				longLine.append(getTop(critics));
			}
			else if(line.contains("TopCommenters"))
			{
				longLine.append(getTop(commenters));
			}
			else
			{
				longLine.append(line+"\n");
			}
		}
		buffReader.close();
		return longLine.toString(); 
	}
	
	private String getTop(List<Submission> submissions)
	{
		StringBuilder builder = new StringBuilder();
		Map<String, SubmissionRecord> topCounts = new HashMap<>();
		Iterator<Submission> iter = submissions.iterator();
		while(iter.hasNext())
		{
			Submission submission = iter.next();
			if(submission.isOutdated())
			{
				iter.remove();
				continue;
			}
			if(topCounts.containsKey(submission.name))
			{
				topCounts.get(submission.name).addSubmission();
			}
			else
			{
				topCounts.put(submission.name, new SubmissionRecord(submission.name));
			}
		}
		List<SubmissionRecord> sortedRecords = new ArrayList<>();
		sortedRecords.addAll(topCounts.values());
		Collections.sort(sortedRecords);
		for(int a = 0; a < Math.min(topCounts.size(), NUM_TOP); a++)
		{
			builder.append(":icon"+sortedRecords.get(a).name+": ("+sortedRecords.get(a).numSubmissions+")");
		}
		return builder.toString();
	}
	
	
	@Override
	public synchronized void save(File f) throws IOException 
	{
		BufferedWriter buff = new BufferedWriter(new FileWriter(f));
		MySerializer.write(buff, critics);
		MySerializer.write(buff, commenters);
		buff.close();
	}

	@Override
	public synchronized void load(File f) throws IOException, ParseException 
	{
		BufferedReader buff = new BufferedReader(new FileReader(f));
		try
		{	
			readSubmissionList(buff, critics);
			readSubmissionList(buff, commenters);
		}
		catch(NumberFormatException e)
		{
			
		}
		buff.close();
	}
	
	private void readSubmissionList(BufferedReader buff, List<Submission> submissions) throws IOException
	{
		Set<String> rawStrings = new HashSet<>();
		MySerializer.read(buff, rawStrings);
		for(String raw : rawStrings)
		{
			submissions.add(getSubmission(raw));
		}
	}
	
	private Submission getSubmission(String rawString)
	{
		String[] split = rawString.split(DELIM);
		Submission submission = new Submission(split[0]);
		submission.creationTime = Long.parseLong(split[1]);
		return submission;
	}
	private class Submission
	{
		private static final long VALID_FOR = 1000 * 60 * 60 * 24 * 7;

		private long creationTime = System.currentTimeMillis();
		private String name;
		private Submission(String name)
		{
			this.name = name;
		}

		private boolean isOutdated()
		{
			return System.currentTimeMillis() - creationTime > VALID_FOR;
		}
		
		@Override
		public String toString()
		{
			return name + DELIM + creationTime;
		}
	}
	private class SubmissionRecord implements Comparable<SubmissionRecord>
	{
		private int numSubmissions = 1;
		private String name;
		private SubmissionRecord(String name)
		{
			this.name = name;
		}
		private void addSubmission()
		{
			numSubmissions ++;
		}
		@Override
		public int compareTo(SubmissionRecord o) 
		{
			return o.numSubmissions - numSubmissions;
		}
		
	}
}

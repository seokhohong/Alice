package data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import utils.Read;

//Could be rewritten using MyIO
public class Backup 
{
	
	//Most info is in this file
	private File backupFile;
	
	private static final int BACKUP_GAP = 1000 * 60 * 60 * 24; //in milliseconds
	
	/* File format:
	 * 
	 * (Address of Alice's Backup folder)
	 * (Time of last Backup, in format "Aug 8, 2013")
	 * (First to copy (should be directory, haven't implemented for files))
	 * (Second folder to copy)
	 * etc...
	 */
	private static final int BACKUP_DEST_INDEX = 0;
	private static final int LAST_BACKUP_TIME_INDEX = 1;
	private static final int BACKUP_FILES_START_INDEX = 2;
	
	private static final int MIN_FIELDS = 3;
	
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss aa", Locale.US);
	
	public Backup(String backupFilename) throws BackupException
	{
		backupFile = new File(backupFilename);
		considerBackup();
	}
	private void considerBackup() throws BackupException
	{
		if(!backupFile.exists())
		{
			createBackup();
			throw new BackupException("Had to create backup.txt");
		}
		ArrayList<String> lines = Read.fromFile(backupFile);
		if(lines.size() < MIN_FIELDS)
		{
			throw new BackupException("backup.txt does not have enough information");
		}
		Date lastBackupDate = getLastBackupDate(lines);
		if(new Date().getTime() - lastBackupDate.getTime() > BACKUP_GAP)
		{
			System.out.println("Running Backup");
			backupFiles(lines);
		}
	}
	
	private void backupFiles(ArrayList<String> lines) throws BackupException
	{
		for(int a = lines.size(); a --> BACKUP_FILES_START_INDEX; )
		{
			try
			{
				File toCopy = new File(lines.get(a));
				File destDir = new File(getBackupLocation(lines)+"\\"+toCopy.getName());
				destDir.mkdir();
				if(toCopy.isDirectory())
				{
					FileUtils.copyDirectory(toCopy, destDir);
				}
				else
				{
					System.err.println("Only built to handle directories: cannot copy "+toCopy.getName());
				}
			}
			catch(IOException e)
			{
				throw new BackupException("Error Making Backups");
			}
		}
		lines.set(LAST_BACKUP_TIME_INDEX, DATE_FORMATTER.format(new Date()));
		writeBackupRecord(lines);
	}
	
	private void writeBackupRecord(ArrayList<String> lines) throws BackupException
	{
		try
		{
			BufferedWriter buff = new BufferedWriter(new FileWriter(backupFile));
			for(String line : lines)
			{
				buff.write(line);
				buff.newLine();
			}
			buff.close();
		}
		catch(IOException e)
		{
			throw new BackupException("Could not save date");
		}
	}
	
	private static String getBackupLocation(ArrayList<String> lines)
	{
		return lines.get(BACKUP_DEST_INDEX);
	}
	
	private static Date getLastBackupDate(ArrayList<String> lines) throws BackupException
	{
		try
		{
			return DATE_FORMATTER.parse(lines.get(LAST_BACKUP_TIME_INDEX));
		}
		catch(ParseException p)
		{
			throw new BackupException("Invalid date in backup.txt");
		}
	}
	//If there isn't one
	private void createBackup()
	{
		try
		{
			BufferedWriter buff = new BufferedWriter(new FileWriter(backupFile));
			buff.write("C:\\");
			buff.newLine();
			buff.write(new Date().toString());
			buff.newLine();
			buff.close();
		}
		catch(IOException e)
		{
			
		}
	}	
}
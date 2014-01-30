package sql;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

import utils.Write;

public class SQLDatabase 
{
	static
	{
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
		} 
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	private Connection con;
	private Statement statement;
	private String username;
	private String password;
	private String databaseName;
	
	/** Connects to a database server (use "localhost" for local server) and opens the specified database */
	public SQLDatabase(String databaseServer, String username, String password, String databaseName)
	{			
		try
		{
			this.username = username;
			this.password = password;
			this.databaseName = databaseName;
			con = DriverManager.getConnection(
					"jdbc:mysql://" + databaseServer,
					username,
					password);
			statement = con.createStatement();
			statement.executeQuery("USE " + databaseName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//Shutdown hook code from: http://stackoverflow.com/questions/5824049/running-a-method-when-closing-the-program
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() 
		{
	        public void run()
	        {
	        	try
	        	{
	        		con.close(); //make sure to close the connection
	        	}
	        	catch(Exception e)
	        	{
	        		e.printStackTrace();
	        	}
	        }
	    }, "Shutdown MySQL thread"));
	}
	/** Makes a backup of the database to the specified file (full path)
	 * 
	 * Implementation Note: Creates a batch file and executes the script from there. Obviously this is slower
	 * than simply running Runtime.exec, but there is a whole suite of guards that have to be used with any
	 * call for the exec method, and this avoids having to re-implement all of it. 
	 * 
	 * */
	public void backup(File fullPath)
	{
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("cd "+fullPath.getParentFile()); //change to parent directory of destination
		commands.add(mySQLDump(fullPath.getName()));
		String batchFilename = fullPath.getParentFile()+"\\backup.bat";
		Write.toFile(new File(batchFilename), commands);
		try 
		{
			BatchExecutor.run(batchFilename);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	//Runs mySQLDump
	private String mySQLDump(String backupFile)
	{
		return "mysqldump -u "+username+" -p"+password+" "+databaseName+" > "+backupFile;
	}
	public synchronized void executeUpdate(String request)
	{
		try
		{
			statement.executeUpdate(request);
			//statement.executeUpdate(request.replace('\'', ' '));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	public synchronized ResultSet executeQuery(String request)
	{
		try
		{
			return statement.executeQuery(request);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//not reached
		return null;
	}
}

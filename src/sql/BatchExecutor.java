package sql;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

public class BatchExecutor 
{
	/**
	 * 
	 * Runs a batch file
	 * 
	 * @param filename
	 * @return		: Returns an ArrayList<String> representing stdout
	 * @throws IOException 
	 * @throws ExecuteException 
	 */
	public static ArrayList<String> run(String filename) throws ExecuteException, IOException
	{
		CommandLine cmdLine = new CommandLine(filename);

		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

		ExecuteWatchdog watchdog = new ExecuteWatchdog(10 * 1000); //kills runaway/stalled processes
		Executor executor = new DefaultExecutor();
		
		LogStream stdout = new LogStream();
		PumpStreamHandler psh = new PumpStreamHandler(stdout);
		
		executor.setExitValue(1);
		executor.setStreamHandler(psh);
		executor.setWatchdog(watchdog);
		executor.execute(cmdLine, resultHandler);

		// some time later the result handler callback was invoked so we
		// can safely request the exit value
		try 
		{
			resultHandler.waitFor();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		return stdout.getLines();
	}
}

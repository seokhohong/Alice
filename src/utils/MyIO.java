package utils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/** Essentially the same as Serializable 
 * 
 *	Open and close the BufferedReader/Writer
 *	Use MySerializer to read and write the object 
 * 
 */
public interface MyIO 
{
	public void save(File f) throws IOException;
	public void load(File f) throws IOException, ParseException;
}

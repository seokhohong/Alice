package sql;

public class SQLUtils 
{
	/** Puts single quotes around the string, and replaces quotes with */
	public static String format(String s)
	{
		s.replace("'", "''");
		s.replace("\\", "\\\\");
		return "'"+s+"'";
	}
}

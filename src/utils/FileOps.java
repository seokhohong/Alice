package utils;

import java.io.File;

public class FileOps 
{
	public static boolean fileBelongsTo(File file, String name)
	{
		return getSimpleName(file).equals(name);
	}
	public static String getSimpleName(File file)
	{
		return file.getName().substring(0, file.getName().indexOf("."));
	}
}

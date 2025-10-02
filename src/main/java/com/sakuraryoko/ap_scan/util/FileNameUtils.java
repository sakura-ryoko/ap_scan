package com.sakuraryoko.ap_scan.util;

/**
 * Cloned from MaLiLib
 */
public class FileNameUtils
{
	/**
	 * @return The file name extension (if any) after the last dot.
	 *         If there are no dots, or there are no characters before or after the last dot,
	 *         then an empty string is returned.
	 */
	public static String getFileNameExtension(String name)
	{
		int i = name.lastIndexOf(".");
		return i > 0 && name.length() > i + 1 ? name.substring(i + 1) : "";
	}

	/**
	 * @return The file name without the extension and the dot (if any).
	 *         The last dot and anything after it is removed.
	 */
	public static String getFileNameWithoutExtension(String name)
	{
		int i = name.lastIndexOf(".");
		return i > 0 ? name.substring(0, i) : name;
	}
}

package com.sakuraryoko.ap_scan.data;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.audio.AudioFile;

public class DirectoryData
{
	public static final AudioFileFilter AUDIO_FILE_FILTER	= new AudioFileFilter();

	public static void readAudioFileListFromPath(Path dir)
	{
		DataManager.getInstance().getPathList().clear();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, AUDIO_FILE_FILTER))
		{
			for (Path entry : stream)
			{
				String cleanName = entry.getFileName().toString().replace(".mp3", "").replace(".wav", "");
				DataManager.getInstance().getPathList().add(new AudioFile(cleanName, "Directory Listing"));
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("DataManager#readAudioFileListFromPath(): Exception reading file: '{}'; {}", dir.toAbsolutePath().toString(), err.getLocalizedMessage());
		}
	}

	public static class AudioFileFilter implements DirectoryStream.Filter<Path>
	{
		@Override
		public boolean accept(Path entry) throws IOException
		{
			try
			{
				if (Files.isRegularFile(entry))
				{
					String file = entry.getFileName().toString();

					return (file.endsWith(".mp3") || file.endsWith(".wav"));
				}
			}
			catch (Exception err)
			{
				throw new IOException(err.getMessage());
			}

			return false;
		}
	}
}

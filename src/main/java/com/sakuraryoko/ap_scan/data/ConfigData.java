package com.sakuraryoko.ap_scan.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.JsonParser;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.audio.AudioFileList;

public class ConfigData
{
	public static void readAudioFileListFromJson()
	{
		Path file = DataManager.getInstance().getAudioConfigFile();

		ApScan.debugLog("ConfigData#readAudioFileListFromJson(): Reading Audio Config JSON (If present)");

		if (Files.exists(file) && Files.isReadable(file))
		{
			try
			{
				AudioFileList list = null;

				if (DataManager.getInstance().getAudioConfigVersion() == 2)
				{
					list = AudioFileList.fromJsonV2(JsonParser.parseString(Files.readString(file)));
				}
				else if (DataManager.getInstance().getAudioConfigVersion() == 1)
				{
					list = AudioFileList.fromJsonV1(JsonParser.parseString(Files.readString(file)));
				}

				if (list != null)
				{
					DataManager.getInstance().setConfigList(list);
				}
				else
				{
					ApScan.LOGGER.error("DataManager#readAudioFileListFromJson(): Error reading file: '{}'; Unsupported File version!", file.toAbsolutePath().toString());
				}
			}
			catch (IOException err)
			{
				ApScan.LOGGER.error("DataManager#readAudioFileListFromJson(): Exception reading file: '{}'; {}", file.toAbsolutePath().toString(), err.getLocalizedMessage());
			}
		}
		else
		{
			ApScan.LOGGER.error("DataManager#readAudioFileListFromJson(): File: '{}' not found!", file.toAbsolutePath().toString());
		}
	}
}

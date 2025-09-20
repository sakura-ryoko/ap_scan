package com.sakuraryoko.ap_scan.event;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.data.ConfigData;
import com.sakuraryoko.ap_scan.data.DataManager;
import com.sakuraryoko.ap_scan.data.DirectoryData;

public class ProcessEvents
{
	public static void onPostArguments()
	{
	}

	public static void onCaptureWorldPath()
	{
		DirectoryData.readAudioFileListFromPath(DataManager.getInstance().getAudioPath());
		ConfigData.readAudioFileListFromJson();
	}

	public static void onShutdown()
	{
		ApScan.LOGGER.error("CONFIG LIST -->");
		DataManager.getInstance().getConfigList().dump();
		ApScan.LOGGER.error("PATH LIST -->");
		DataManager.getInstance().getPathList().dump();
		ApScan.LOGGER.error("WORLD LIST -->");
		DataManager.getInstance().getWorldList().dump();
	}
}

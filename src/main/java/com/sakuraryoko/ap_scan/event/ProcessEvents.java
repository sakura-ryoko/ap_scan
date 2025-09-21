package com.sakuraryoko.ap_scan.event;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.data.ConfigData;
import com.sakuraryoko.ap_scan.data.DataManager;
import com.sakuraryoko.ap_scan.data.DirectoryData;
import com.sakuraryoko.ap_scan.data.PlayerData;
import com.sakuraryoko.ap_scan.report.UnusedFilesReport;

public class ProcessEvents
{
	public static void onPostArguments()
	{
	}

	public static void onCaptureWorldPath()
	{
		ConfigData.readAudioFileListFromJson();
		DirectoryData.readAudioFileListFromPath(DataManager.getInstance().getAudioPath());
		PlayerData.readAudioFileListFromPath(DataManager.getInstance().getPlayerDataPath());
	}

	public static void onShutdown()
	{
		ApScan.LOGGER.error("CONFIG LIST -->");
		DataManager.getInstance().getConfigList().dump();
		ApScan.LOGGER.error("PATH LIST -->");
		DataManager.getInstance().getPathList().dump();
		ApScan.LOGGER.error("WORLD LIST -->");
		DataManager.getInstance().getWorldList().dump();

		ApScan.LOGGER.info("*** Exporting Reports ***");
		UnusedFilesReport.getInstance().runReport(
				DataManager.getInstance().getReportsPath(), DataManager.getInstance().getReportName()
		);
	}
}

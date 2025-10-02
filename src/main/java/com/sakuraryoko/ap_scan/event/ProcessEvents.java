package com.sakuraryoko.ap_scan.event;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.Reference;
import com.sakuraryoko.ap_scan.data.ConfigData;
import com.sakuraryoko.ap_scan.data.DataManager;
import com.sakuraryoko.ap_scan.data.DirectoryData;
import com.sakuraryoko.ap_scan.data.PlayerData;
import com.sakuraryoko.ap_scan.report.FileLocationsReport;
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
		if (Reference.DEBUG)
		{
			ApScan.LOGGER.error("CONFIG LIST -->");
			DataManager.getInstance().getConfigList().dump();
			ApScan.LOGGER.error("PATH LIST -->");
			DataManager.getInstance().getPathList().dump();
			ApScan.LOGGER.error("WORLD LIST -->");
			DataManager.getInstance().getWorldList().dump();
			ApScan.LOGGER.error("LOCATIONS LIST -->");
			DataManager.getInstance().getLocationsList().dump();
		}

		ApScan.LOGGER.info("*** Exporting Reports ***");
		UnusedFilesReport.getInstance().runReport(
				DataManager.getInstance().getReportsPath(), DataManager.getInstance().getReportName()
		);
		FileLocationsReport.getInstance().runReport(
				DataManager.getInstance().getReportsPath(), DataManager.getInstance().getReportName()
		);

		if (DataManager.getInstance().shouldRelocateUnused())
		{
			ApScan.LOGGER.info("*** Relocating Any unused files ***");
			UnusedFilesReport.getInstance().relocateAllUnusedFiles(
					DataManager.getInstance().getAudioPath(), DataManager.getInstance().getAudioUnusedPath()
			);
		}
	}
}

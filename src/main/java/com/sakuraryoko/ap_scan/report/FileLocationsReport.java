package com.sakuraryoko.ap_scan.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.audio.AudioDataLocation;
import com.sakuraryoko.ap_scan.audio.LocationsList;
import com.sakuraryoko.ap_scan.data.DataManager;

public class FileLocationsReport
{
	private static final FileLocationsReport INSTANCE = new FileLocationsReport();
	public static FileLocationsReport getInstance() {return INSTANCE;}

	private final String REPORT_SUFFIX = "Locations";

	private FileLocationsReport() {}

	public void runReport(Path dir, String name)
	{
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
		String nowRfc = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now());
		String nowMC = fmt.format(ZonedDateTime.now());
		Path file = dir.resolve(name + "-" + REPORT_SUFFIX + "_" + nowMC + ".txt");

		if (!Files.isDirectory(dir))
		{
			try
			{
				Files.createDirectory(dir);
			}
			catch (IOException err)
			{
				ApScan.LOGGER.error("runReport: Exception creating directory '{}'; {}", dir.toAbsolutePath().toString(), err.getLocalizedMessage());
				return;
			}
		}

		if (Files.exists(file))
		{
			try
			{
				Files.delete(file);
			}
			catch (IOException err)
			{
				ApScan.LOGGER.error("runReport: Exception deleting file '{}'; {}", file.toAbsolutePath().toString(), err.getLocalizedMessage());
				return;
			}
		}

		if (DataManager.getInstance().getLocationsList().isEmpty())
		{
			ApScan.LOGGER.error("runReport: Locations list is empty!  Cancelling report generation.");
			return;
		}

		this.runFileLocationsReport(file, name, nowRfc);
	}

	private void runFileLocationsReport(Path file, String name, String now)
	{
		LocationsList list = DataManager.getInstance().getLocationsList();
		List<String> results = new ArrayList<>();
		int total = 0;

		results.add(name + " (" + now + ") Audio Data Locations report");
		results.add("========================================================================================================================");

		for (int i = 0; i < list.size(); i++)
		{
			AudioDataLocation entry = list.get(i);

			if (entry != null)
			{
				results.add(String.format("[%04d] [%s], [%s: %s]", i, entry.id(), entry.type().toString(), entry.desc()));
				total++;
			}
		}

		results.add(String.format("[TOTAL: %04d]", total));
		this.writeAllLines(file, results);
	}

	private void writeAllLines(Path file, List<String> lines)
	{
		try
		{
			Files.write(file, lines);
			ApScan.LOGGER.warn("FileLocations: Wrote report file '{}'", file.toAbsolutePath().toString());
		}
		catch (IOException err)
		{
			ApScan.LOGGER.error("writeAllLines: Exception writing report file '{}'; {}", file.toAbsolutePath().toString(), err.getLocalizedMessage());
		}
	}
}

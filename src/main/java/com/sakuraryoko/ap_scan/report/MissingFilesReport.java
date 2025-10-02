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
import com.sakuraryoko.ap_scan.audio.AudioFile;
import com.sakuraryoko.ap_scan.audio.AudioFileList;
import com.sakuraryoko.ap_scan.data.DataManager;

public class MissingFilesReport
{
	private static final MissingFilesReport INSTANCE = new MissingFilesReport();
	public static MissingFilesReport getInstance() { return INSTANCE; }

	private final String FILES_SUFFIX = "MissingFiles";
	private final String CONFIG_SUFFIX = "MissingConfig";

	private MissingFilesReport() { }

	public void runReport(Path dir, String name)
	{
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
		String nowRfc = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now());
		String nowMC = fmt.format(ZonedDateTime.now());
		Path file1 = dir.resolve(name + "-" + FILES_SUFFIX + "_" + nowMC + ".txt");
		Path file2 = dir.resolve(name + "-" + CONFIG_SUFFIX + "_" + nowMC + ".txt");

		if (DataManager.getInstance().getWorldList().isEmpty())
		{
			ApScan.LOGGER.error("runReport: Exception; World List is empty!");
			return;
		}

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

		if (Files.exists(file1))
		{
			try
			{
				Files.delete(file1);
			}
			catch (IOException err)
			{
				ApScan.LOGGER.error("runReport: Exception deleting file1 '{}'; {}", file1.toAbsolutePath().toString(), err.getLocalizedMessage());
				return;
			}
		}

		if (Files.exists(file2))
		{
			try
			{
				Files.delete(file2);
			}
			catch (IOException err)
			{
				ApScan.LOGGER.error("runReport: Exception deleting file2 '{}'; {}", file2.toAbsolutePath().toString(), err.getLocalizedMessage());
				return;
			}
		}

		if (!DataManager.getInstance().getPathList().isEmpty())
		{
			this.runFromPathReport(file1, name, nowRfc);
		}

		if (!DataManager.getInstance().getConfigList().isEmpty())
		{
			this.runFromConfigReport(file1, name, nowRfc);
		}
	}

	private void runFromPathReport(Path file, String name, String now)
	{
		AudioFileList world = DataManager.getInstance().getWorldList();
		List<String> results = new ArrayList<>();
		int count = 0;
		int total = 0;
		int locTotal = 0;

		results.add(name + " (" + now + ") Directory files missing report");
		results.add("========================================================================================================================");

		for (int i = 0; i < world.size(); i++)
		{
			AudioFile entry = world.get(i);

			if (entry != null && DataManager.getInstance().getPathList().getById(entry.id()) == null)
			{
				List<AudioDataLocation> locations = DataManager.getInstance().getLocationsList().getById(entry.id());

				if (locations != null)
				{
					for (int j = 0; j < locations.size(); j++)
					{
						AudioDataLocation eachLoc = locations.get(j);

						results.add(
								String.format("[%04d/%04d] [%s/'%s'] Located: [%s: %s]", i, j,
								              entry.id(), entry.name(),
								              eachLoc.type().name(), eachLoc.desc())
						);
						locTotal++;
					}
				}
				else
				{
					results.add(
							String.format("[%04d/0000] [%s/'%s'] Located: [UNKNOWN]", i,
							              entry.id(), entry.name())
					);
				}

				count++;
			}

			total++;
		}

		results.add(String.format("[TOTAL: %04d / MISSING: %04d / LOCATIONS: %04d]", total, count, locTotal));
		this.writeAllLines(file, results);
	}

	private void runFromConfigReport(Path file, String name, String now)
	{
		AudioFileList world = DataManager.getInstance().getWorldList();
		List<String> results = new ArrayList<>();
		int count = 0;
		int total = 0;
		int locTotal = 0;

		results.add(name + " (" + now + ") Configured files missing report");
		results.add("========================================================================================================================");

		for (int i = 0; i < world.size(); i++)
		{
			AudioFile entry = world.get(i);

			if (entry != null && DataManager.getInstance().getConfigList().getById(entry.id()) == null)
			{
				List<AudioDataLocation> locations = DataManager.getInstance().getLocationsList().getById(entry.id());

				if (locations != null)
				{
					for (int j = 0; j < locations.size(); j++)
					{
						AudioDataLocation eachLoc = locations.get(j);

						results.add(
								String.format("[%04d/%04d] [%s/'%s'] Located: [%s: %s]", i, j,
								              entry.id(), entry.name(),
								              eachLoc.type().name(), eachLoc.desc())
						);
						locTotal++;
					}
				}
				else
				{
					results.add(
							String.format("[%04d/0000] [%s/'%s'] Located: [UNKNOWN]", i,
							              entry.id(), entry.name())
					);
				}

				count++;
			}

			total++;
		}

		results.add(String.format("[TOTAL: %04d / MISSING: %04d / LOCATIONS: %04d]", total, count, locTotal));
		this.writeAllLines(file, results);
	}

	private void writeAllLines(Path file, List<String> lines)
	{
		try
		{
			Files.write(file, lines);
			ApScan.LOGGER.warn("MissingFilesReport: Wrote report file '{}'", file.toAbsolutePath().toString());
		}
		catch (IOException err)
		{
			ApScan.LOGGER.error("MissingFilesReport: Exception writing report file '{}'; {}", file.toAbsolutePath().toString(), err.getLocalizedMessage());
		}
	}
}

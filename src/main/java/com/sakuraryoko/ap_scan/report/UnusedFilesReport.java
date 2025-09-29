package com.sakuraryoko.ap_scan.report;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.audio.AudioFile;
import com.sakuraryoko.ap_scan.audio.AudioFileList;
import com.sakuraryoko.ap_scan.data.DataManager;

public class UnusedFilesReport
{
	private static final UnusedFilesReport INSTANCE = new UnusedFilesReport();

	public static UnusedFilesReport getInstance() {return INSTANCE;}

	private final String CONFIG_SUFFIX = "ConfigUnused";
	private final String PATH_SUFFIX = "DirectoryUnused";

	private final AudioFileList unusedFiles;

	public UnusedFilesReport()
	{
		this.unusedFiles = new AudioFileList();
	}

	public void runReport(Path dir, String name)
	{
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
		String nowRfc = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now());
		String nowMC = fmt.format(ZonedDateTime.now());
		Path file1 = dir.resolve(name + "-" + CONFIG_SUFFIX + "_" + nowMC + ".txt");
		Path file2 = dir.resolve(name + "-" + PATH_SUFFIX + "_" + nowMC + ".txt");

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

		if (DataManager.getInstance().getWorldList().isEmpty())
		{
			ApScan.LOGGER.error("runReport: World list is empty!  Cancelling report generation.");
			return;
		}

		if (!DataManager.getInstance().getConfigList().isEmpty())
		{
			this.runFromConfigReport(file1, name, nowRfc);
		}
		if (!DataManager.getInstance().getPathList().isEmpty())
		{
			this.runFromDirectoryReport(file2, name, nowRfc);
		}
	}

	private void runFromConfigReport(Path file, String name, String now)
	{
		AudioFileList config = DataManager.getInstance().getConfigList();
		List<String> results = new ArrayList<>();
		int count = 0;
		int total = 0;

		results.add(name + " (" + now + ") Configured files unused report");
		results.add("========================================================================================================================");

		for (int i = 0; i < config.size(); i++)
		{
			AudioFile entry = config.get(i);

			if (entry != null && DataManager.getInstance().getWorldList().getById(entry.id()) == null)
			{
				results.add(String.format("[%04d] [%s] name: '%s'", i, entry.id(), entry.name()));
				count++;
			}

			total++;
		}

		results.add(String.format("[TOTAL: %04d / EXTRA: %04d]", total, count));
		this.writeAllLines(file, results);
	}

	private void runFromDirectoryReport(Path file, String name, String now)
	{
		AudioFileList config = DataManager.getInstance().getPathList();
		List<String> results = new ArrayList<>();
		int count = 0;
		int total = 0;

		results.add(name + " (" + now + ") Directory files unused report");
		results.add("========================================================================================================================");

		for (int i = 0; i < config.size(); i++)
		{
			AudioFile entry = config.get(i);

			if (entry != null && DataManager.getInstance().getWorldList().getById(entry.id()) == null)
			{
				results.add(String.format("[%04d] [%s] name: '%s'", i, entry.id(), entry.name()));
				this.unusedFiles.add(entry);
				count++;
			}

			total++;
		}

		results.add(String.format("[TOTAL: %04d / EXTRA: %04d]", total, count));
		this.writeAllLines(file, results);
	}

	private void writeAllLines(Path file, List<String> lines)
	{
		try
		{
			Files.write(file, lines);
		}
		catch (IOException err)
		{
			ApScan.LOGGER.error("writeAllLines: Exception writing report file '{}'; {}", file.toAbsolutePath().toString(), err.getLocalizedMessage());
		}
	}

	public void relocateAllUnusedFiles(Path fromDir, Path toDir)
	{
        if (!Files.isDirectory(fromDir))
        {
            ApScan.LOGGER.error("relocateAllUnusedFiles: Exception; Source directory '{}' does not exist,", fromDir.toAbsolutePath().toString());
            return;
        }

		if (!Files.isDirectory(toDir))
		{
			try
			{
				Files.createDirectory(toDir);
			}
			catch (IOException err)
			{
				ApScan.LOGGER.error("relocateAllUnusedFiles: Exception creating destination directory '{}'; {}", toDir.toAbsolutePath().toString(), err.getLocalizedMessage());
				return;
			}
		}

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(fromDir))
		{
			int count = 0;

			for (Path file : stream)
			{
				AudioFile audio = this.unusedFiles.getById(this.getNameWithoutExtension(file));

				if (audio != null)
				{
					Path target = toDir.resolve(file.getFileName());
					ApScan.debugLog("UnusedFilesReport#relocateAllUnusedFiles(): Moving unused file '{}' -> '{}'", file.getFileName().toString(), target.toAbsolutePath().toString());
					Files.move(file, target);
					count++;
				}
			}

			ApScan.LOGGER.warn("relocateAllUnusedFiles: Relocated [{}] file(s) to '{}'", count, toDir.toAbsolutePath().toString());
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("relocateAllUnusedFiles: Exception relocating files to destination directory '{}'; {}", toDir.toAbsolutePath().toString(), err.getLocalizedMessage());
		}
	}

	private String getNameWithoutExtension(Path file)
	{
		String s = file.getFileName().toString();
		String separator = FileSystems.getDefault().getSeparator();
		String filename;

		int lastSeparatorIndex = s.lastIndexOf(separator);

		if (lastSeparatorIndex == -1)
		{
			filename = s;
		}
		else
		{
			filename = s.substring(lastSeparatorIndex + 1);
		}

		// Remove the extension.
		int extensionIndex = filename.lastIndexOf(".");
		if (extensionIndex == -1)
		{
			return filename;
		}

		return filename.substring(0, extensionIndex);
	}
}

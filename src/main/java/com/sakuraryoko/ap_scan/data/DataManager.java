package com.sakuraryoko.ap_scan.data;

import java.nio.file.Path;

import net.minecraft.registry.DynamicRegistryManager;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.audio.AudioFileList;

public class DataManager
{
	private static final DataManager INSTANCE = new DataManager();
	public static DataManager getInstance() { return INSTANCE; }

	public static final String ROOT_DEFAULT 				= ".";
	public static final String WORLD_DEFAULT 				= "world";
	public static final String AUDIO_PLAYER_DATA			= "audio_player_data";
	public static final String AUDIO_PLAYER_CONFIG			= "file-name-mappings";

	private DynamicRegistryManager registry;
	private Path rootPath;
	private Path worldPath;
	private Path audioPath;

	private AudioFileList pathList;
	private AudioFileList configList;
	private AudioFileList worldList;

	public DataManager()
	{
		Path dir = Path.of(ROOT_DEFAULT);
		this.rootPath = dir;
		this.updateWorldPath(dir.resolve(WORLD_DEFAULT), false);
		this.registry = DynamicRegistryManager.EMPTY;

		this.pathList = new AudioFileList();
		this.configList = new AudioFileList();
		this.worldList = new AudioFileList();
	}

	public Path getRootPath()
	{
		return this.rootPath;
	}

	public Path getWorldPath()
	{
		return this.worldPath;
	}

	public Path getAudioPath()
	{
		return this.audioPath;
	}

	public Path getAudioConfigFile()
	{
		return this.audioPath.resolve(AUDIO_PLAYER_CONFIG+".json");
	}

	public void updateRootPath(Path dir, boolean debugOk)
	{
		if (debugOk)
		{
			ApScan.debugLog("Root path captured: '{}'", dir.toAbsolutePath().toString());
		}

		this.rootPath = dir.normalize();
		this.updateWorldPath(this.rootPath.resolve(WORLD_DEFAULT), debugOk);
	}

	public void updateWorldPath(Path dir, boolean debugOk)
	{
		if (debugOk)
		{
			ApScan.debugLog("World path captured: '{}'", dir.toAbsolutePath().toString());
		}

		this.worldPath = dir.normalize();
		this.audioPath = this.worldPath.resolve(AUDIO_PLAYER_DATA);
	}

	public DynamicRegistryManager getRegistry() { return this.registry; }

	public void setRegistry(DynamicRegistryManager registry)
	{
		this.registry = registry;
	}

	public AudioFileList getPathList() { return this.pathList; }

	public AudioFileList getConfigList() { return this.configList; }

	public AudioFileList getWorldList() { return this.worldList; }

	public void setConfigList(AudioFileList list)
	{
		this.configList.clear();
		this.configList = list;
	}
}

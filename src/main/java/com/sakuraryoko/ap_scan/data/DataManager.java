package com.sakuraryoko.ap_scan.data;

import java.nio.file.Path;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.WorldSavePath;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.Reference;
import com.sakuraryoko.ap_scan.audio.AudioFileList;
import com.sakuraryoko.ap_scan.audio.LocationsList;

public class DataManager
{
	private static final DataManager INSTANCE = new DataManager();
	public static DataManager getInstance() { return INSTANCE; }

	/**
	 * Runtime Arguments
	 */
	public static final String RUN_REPORTS_PARAM			= "--runReports";
	public static final String REPORT_NAME_PARAM			= "--reportName";
	public static final String STOP_SERVER_PARAM			= "--stopServer";
	public static final String DEFLATE_LEVEL_PARAM			= "--deflateLevel9";
	public static final String RELOCATE_UNUSED_PARAM		= "--relocateUnused";
    public static final String DISABLE_LIGHTMAP_PRUNE_PARAM = "--noPruneLightmap";
	public static final String FORCE_UPGRADE_PARAM          = "--forceUpgrade";
	public static final String ERASE_CACHE_PARAM            = "--eraseCache";
	public static final String RECREATE_REGION_FILES_PARAM  = "--recreateRegionFiles";

	/**
	 * Default settings (Non-testing these should be false)
	 */
	private boolean forceUpgrade = true;
	private boolean eraseCache = true;
	private boolean recreateRegionFiles = true;
	private boolean stopServer = true;
	private boolean runReports = true;
	private boolean adjustDeflateLevel = true;
	private boolean relocateUnused = true;
    private boolean disableLightmapPruning = false;

	/**
	 * Default Directory Paths
	 */
	public static final String ROOT_DEFAULT 				= ".";
	public static final String WORLD_DEFAULT 				= "world";
	public static final String AUDIO_PLAYER_DATA			= "audio_player_data";
	public static final String AUDIO_PLAYER_UNUSED			= "audio_player_unused";
	public static final String AUDIO_PLAYER_CONFIG			= "file-name-mappings";
	public static final String REPORTS_FOLDER				= "audioplayer_reports";

	/**
	 * Internal Use Only
	 */
	private DynamicRegistryManager registry;
	private Path rootPath;
	private Path worldPath;
	private Path audioPath;
	private Path audioUnusedPath;
	private Path playerDataPath;
	private Path reportsPath;
	private String reportName;

	private final LocationsList locationsList;
	private final AudioFileList pathList;
	private final AudioFileList worldList;
	private AudioFileList configList;

	public DataManager()
	{
		Path dir = Path.of(ROOT_DEFAULT);
		this.rootPath = dir;
		this.updateWorldPath(dir.resolve(WORLD_DEFAULT), false);
		this.updateReportsPath(null);
		this.registry = DynamicRegistryManager.EMPTY;
		this.reportName = Reference.MOD_ID+"-Report";
		this.locationsList = new LocationsList();
		this.pathList = new AudioFileList();
		this.configList = new AudioFileList();
		this.worldList = new AudioFileList();
	}

	public Path getRootPath() { return this.rootPath; }

	public Path getWorldPath() { return this.worldPath; }

	public Path getAudioPath() { return this.audioPath; }

	public Path getAudioUnusedPath() { return this.audioUnusedPath; }

	public Path getAudioConfigFile() { return this.audioPath.resolve(AUDIO_PLAYER_CONFIG+".json"); }

	public Path getPlayerDataPath() { return this.playerDataPath; }

	public Path getReportsPath() { return this.reportsPath; }

	public void updateRootPath(Path dir, boolean debugOk)
	{
		dir = dir.normalize();

		if (debugOk)
		{
			ApScan.debugLog("Root path captured: '{}'", dir.toAbsolutePath().toString());
		}

		this.rootPath = dir;
		this.updateReportsPath(REPORTS_FOLDER);
		this.updateWorldPath(dir.resolve(WORLD_DEFAULT), false);
	}

	public void updateWorldPath(Path dir, boolean debugOk)
	{
		dir = dir.normalize();

		if (debugOk)
		{
			ApScan.debugLog("World path captured: '{}'", dir.toAbsolutePath().toString());
		}

		this.worldPath = dir;
		this.audioPath = dir.resolve(AUDIO_PLAYER_DATA);
		this.audioUnusedPath = dir.resolve(AUDIO_PLAYER_UNUSED);
		this.playerDataPath = dir.resolve(WorldSavePath.PLAYERDATA.getRelativePath());
	}

	public void updateReportsPath(String dir)
	{
		if (dir != null && !dir.isEmpty())
		{
			this.reportsPath = this.getRootPath().resolve(dir).normalize();
		}
		else
		{
			this.reportsPath = this.getRootPath().resolve(REPORTS_FOLDER).normalize();
		}
	}

	public DynamicRegistryManager getRegistry() { return this.registry; }

	public void setRegistry(DynamicRegistryManager registry) { this.registry = registry; }

	public LocationsList getLocationsList() { return this.locationsList; }

	public AudioFileList getPathList() { return this.pathList; }

	public AudioFileList getConfigList() { return this.configList; }

	public AudioFileList getWorldList() { return this.worldList; }

	public void setConfigList(AudioFileList list)
	{
		this.configList.clear();
		this.configList = list;
	}

	public void setReportName(String name) { this.reportName = name; }

	public String getReportName()
	{
		if (this.reportName.isEmpty())
		{
			return Reference.MOD_ID;
		}

		return this.reportName;
	}

	public void toggleRunReports() { this.runReports = !this.runReports; }

	public void toggleStopServer() { this.stopServer = !this.stopServer; }

	public void toggleDeflate() { this.adjustDeflateLevel = !this.adjustDeflateLevel; }

	public void toggleRelocateUnused() { this.relocateUnused = !this.relocateUnused; }

    public void toggleDisableLightmapPrune() { this.disableLightmapPruning = !this.disableLightmapPruning; }

    public boolean shouldRunReports() { return this.runReports; }

	public boolean shouldStopServer() { return this.stopServer; }

	public boolean shouldAdjustDeflateLevel() { return this.adjustDeflateLevel; }

	public boolean shouldRelocateUnused() { return this.relocateUnused; }

    public boolean shouldDisableLightmapPrune() { return this.disableLightmapPruning; }

	public boolean shouldForceUpgrade() { return this.forceUpgrade; }

	public boolean shouldEraseCache() { return this.eraseCache; }

	public boolean shouldRecreateRegionFiles() { return this.recreateRegionFiles; }
}

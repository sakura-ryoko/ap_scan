package com.sakuraryoko.ap_scan.mixin;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.slf4j.Logger;

import com.mojang.datafixers.DataFixer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.Main;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sakuraryoko.ap_scan.Reference;
import com.sakuraryoko.ap_scan.data.DataManager;
import com.sakuraryoko.ap_scan.event.ProcessEvents;

@Mixin(Main.class)
public class MixinMain
{
	@Shadow @Final private static Logger LOGGER;
	@Unique private static final String logPrefix = "("+Reference.MOD_ID+")";

	@ModifyVariable(method = "main", at = @At("HEAD"),
					argsOnly = true, name = "args")
	private static String[] ap_scan$onLaunchServer(String[] args)
	{
		if (!DataManager.getInstance().shouldRunTasks()) { return args; }
		boolean hasForceUpgrade = false;
		boolean hasEraseCache = false;
		boolean hasRecreateRegionFiles = false;
		boolean hasRunReports = false;
		boolean hasStopServer = false;
		boolean hasDeflateLevel = false;
		boolean hasReportName = false;
		boolean hasRelocateUnused = false;
        boolean hasDisableLightmapPrune = false;

		for (int i = 0; i < args.length; i++)
		{
			String entry = args[i];

			if (entry.equalsIgnoreCase(DataManager.FORCE_UPGRADE_PARAM))
			{
				hasForceUpgrade = true;
			}
			else if (entry.equalsIgnoreCase(DataManager.ERASE_CACHE_PARAM))
			{
				hasEraseCache = true;
			}
			else if (entry.equalsIgnoreCase(DataManager.RECREATE_REGION_FILES_PARAM))
			{
				hasRecreateRegionFiles = true;
			}
			else if (entry.equalsIgnoreCase(DataManager.RUN_REPORTS_PARAM))
			{
				DataManager.getInstance().toggleRunReports();
				hasRunReports = true;
			}
			else if (entry.equalsIgnoreCase(DataManager.STOP_SERVER_PARAM))
			{
				DataManager.getInstance().toggleStopServer();
				hasStopServer = true;
			}
			else if (entry.equalsIgnoreCase(DataManager.DEFLATE_LEVEL_PARAM))
			{
				DataManager.getInstance().toggleDeflate();
				hasDeflateLevel = true;
			}
			else if (entry.equalsIgnoreCase(DataManager.RELOCATE_UNUSED_PARAM))
			{
				DataManager.getInstance().toggleRelocateUnused();
				hasRelocateUnused = true;
			}
            else if (entry.equalsIgnoreCase(DataManager.DISABLE_LIGHTMAP_PRUNE_PARAM))
            {
                DataManager.getInstance().toggleDisableLightmapPrune();
                hasDisableLightmapPrune = true;
            }
			else if (entry.equalsIgnoreCase(DataManager.REPORT_NAME_PARAM))
			{
				if (args.length > (i + 1))
				{
					DataManager.getInstance().setReportName(args[++i]);
				}
				else
				{
					LOGGER.error("{} [MAIN] -- Report Name Param exception; Out of bounds.", logPrefix);
				}

				hasReportName = true;
			}
		}

		if (hasForceUpgrade || hasRecreateRegionFiles)
		{
			if (DataManager.getInstance().shouldRunReports())
			{
				if (Reference.DEBUG)
				{
					LOGGER.info("{} [MAIN] -- No changed required.", logPrefix);
				}

				ProcessEvents.onPostArguments();
			}

			return args;
		}
		else
		{
			if (DataManager.getInstance().shouldRunReports())
			{
				List<String> list = new java.util.ArrayList<>(Arrays.stream(args).toList());

				if (DataManager.getInstance().shouldForceUpgrade())
				{
					list.add(DataManager.FORCE_UPGRADE_PARAM);
				}

				if (DataManager.getInstance().shouldRecreateRegionFiles())
				{
					list.add(DataManager.RECREATE_REGION_FILES_PARAM);
				}

				if (!hasEraseCache && DataManager.getInstance().shouldEraseCache())
				{
					list.add(DataManager.ERASE_CACHE_PARAM);
				}

				if (hasStopServer)
				{
					list.remove(DataManager.STOP_SERVER_PARAM);
				}
				if (hasRunReports)
				{
					list.remove(DataManager.RUN_REPORTS_PARAM);
				}
				if (hasReportName)
				{
					list.remove(DataManager.REPORT_NAME_PARAM);
				}
				if (hasDeflateLevel)
				{
					list.remove(DataManager.DEFLATE_LEVEL_PARAM);
				}
				if (hasRelocateUnused)
				{
					list.remove(DataManager.RELOCATE_UNUSED_PARAM);
				}
                if (hasDisableLightmapPrune)
                {
                    list.remove(DataManager.DISABLE_LIGHTMAP_PRUNE_PARAM);
                }

				if (Reference.DEBUG)
				{
					LOGGER.warn("{} [MAIN] -- Arguments appended.", logPrefix);
				}
				ProcessEvents.onPostArguments();

				return list.toArray(new String[0]);
			}
			else
			{
				return args;
			}
		}
	}

	@Redirect(method = "main",
			  at = @At(value = "INVOKE",
					   target = "Lnet/minecraft/world/level/storage/LevelStorageSource;createDefault(Ljava/nio/file/Path;)Lnet/minecraft/world/level/storage/LevelStorageSource;"))
	private static LevelStorageSource ap_scan$onCaptureRootPath(Path path)
	{
		if (DataManager.getInstance().shouldRunTasks() &&
			DataManager.getInstance().shouldRunReports())
		{
			DataManager.getInstance().updateRootPath(path, true);
		}
		return LevelStorageSource.createDefault(path);
	}

	@Redirect(method = "main",
			  at = @At(value = "INVOKE",
					 target = "Lnet/minecraft/world/level/storage/LevelStorageSource;validateAndCreateAccess(Ljava/lang/String;)Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;"))
	private static LevelStorageSource.LevelStorageAccess ap_scan$onCaptureWorldPath(LevelStorageSource instance, String directoryName)
	{
		if (DataManager.getInstance().shouldRunTasks() && DataManager.getInstance().shouldRunReports())
		{
			try
			{
				DataManager.getInstance().updateWorldPath(instance.getBaseDir().resolve(directoryName), true);
				ProcessEvents.onCaptureWorldPath();
				return instance.validateAndCreateAccess(directoryName);
			}
			catch (Exception err)
			{
				LOGGER.error("{} [MAIN] -- LevelStorage.Session failed to be captured.", logPrefix);
				System.exit(1);
			}
		}

		try
		{
			return instance.validateAndCreateAccess(directoryName);
		}
		catch (Exception err)
		{
			LOGGER.error("{} [MAIN] -- Vanilla Exception; {}", logPrefix, err.getLocalizedMessage());
			System.exit(1);
		}

		return null;
	}

	@Inject(method = "main",
			at = @At(value = "INVOKE",
					 target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;saveDataTag(Lnet/minecraft/world/level/storage/WorldData;)V"),
			cancellable = true
	)
	private static void ap_scan$onLaunchCancel(String[] args, CallbackInfo ci)
	{
		if (DataManager.getInstance().shouldRunTasks() &&
			DataManager.getInstance().shouldRunReports())
		{
			if (Reference.DEBUG)
			{
				LOGGER.error("{} [MAIN] -- TASK COMPLETE.", logPrefix);
			}
			ProcessEvents.onShutdown();

			if (DataManager.getInstance().shouldStopServer())
			{
				ci.cancel();
				System.exit(0);
			}
		}
	}

	@Inject(method = "forceUpgrade", at = @At("HEAD"))
	private static void ap_scan$onCaptureImmutable(LevelStorageSource.LevelStorageAccess storageSource,
	                                               DataFixer fixerUpper, boolean eraseCache, BooleanSupplier isRunning,
	                                               RegistryAccess registryAccess, boolean recreateRegionFiles,
	                                               CallbackInfo ci)
	{
		DataManager.getInstance().setRegistry(registryAccess);
	}
}

package com.sakuraryoko.ap_scan.mixin;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.slf4j.Logger;

import com.mojang.datafixers.DataFixer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.Main;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
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
	@Unique private static final String logPrefix = "("+ Reference.MOD_ID+")";

	@ModifyVariable(method = "main", at = @At("HEAD"), argsOnly = true)
	private static String[] ap_scan$onLaunchServer(String[] value)
	{
		boolean hasForceUpgrade = false;
		boolean hasEraseCache = false;
		boolean hasRecreateRegionFiles = false;
		boolean hasRunReports = false;
		boolean hasStopServer = false;
		boolean hasDeflateLevel = false;
		boolean hasReportName = false;
		boolean hasRelocateUnused = false;
        boolean hasDisableLightmapPrune = false;

		for (int i = 0; i < value.length; i++)
		{
			String entry = value[i];

			if (entry.equalsIgnoreCase("--forceUpgrade"))
			{
				hasForceUpgrade = true;
			}
			else if (entry.equalsIgnoreCase("--eraseCache"))
			{
				hasEraseCache = true;
			}
			else if (entry.equalsIgnoreCase("--recreateRegionFiles"))
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
				if (value.length > (i + 1))
				{
					DataManager.getInstance().setReportName(value[++i]);
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

			return value;
		}
		else
		{
			if (DataManager.getInstance().shouldRunReports())
			{
				List<String> list = new java.util.ArrayList<>(Arrays.stream(value).toList());

				list.add("--recreateRegionFiles");

				if (!hasEraseCache)
				{
					list.add("--eraseCache");
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
				return value;
			}
		}
	}

	@Redirect(method = "main",
			  at = @At(value = "INVOKE",
					   target = "Lnet/minecraft/world/level/storage/LevelStorage;create(Ljava/nio/file/Path;)Lnet/minecraft/world/level/storage/LevelStorage;"))
	private static LevelStorage ap_scan$onCaptureRootPath(Path path)
	{
		if (DataManager.getInstance().shouldRunReports())
		{
			DataManager.getInstance().updateRootPath(path, true);

		}
		return LevelStorage.create(path);
	}

	@Redirect(method = "main",
			  at = @At(value = "INVOKE",
					 target = "Lnet/minecraft/world/level/storage/LevelStorage;createSession(Ljava/lang/String;)Lnet/minecraft/world/level/storage/LevelStorage$Session;"))
	private static LevelStorage.Session ap_scan$onCaptureWorldPath(LevelStorage instance, String directoryName)
	{
		if (DataManager.getInstance().shouldRunReports())
		{
			try
			{
				DataManager.getInstance().updateWorldPath(instance.getSavesDirectory().resolve(directoryName), true);
				ProcessEvents.onCaptureWorldPath();
				return instance.createSession(directoryName);
			}
			catch (Exception err)
			{
				LOGGER.error("{} [MAIN] -- LevelStorage.Session failed to be captured.", logPrefix);
				System.exit(1);
			}
		}

		try
		{
			return instance.createSession(directoryName);
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
					 target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;backupLevelDataFile(Lnet/minecraft/registry/DynamicRegistryManager;Lnet/minecraft/world/SaveProperties;)V"), cancellable = true)
	private static void ap_scan$onLaunchCancel(String[] args, CallbackInfo ci)
	{
		if (DataManager.getInstance().shouldRunReports())
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

	@Inject(method = "forceUpgradeWorld", at = @At("HEAD"))
	private static void ap_scan$onCaptureImmutable(LevelStorage.Session session, SaveProperties saveProperties, DataFixer dataFixer, boolean eraseCache, BooleanSupplier continueCheck, DynamicRegistryManager registries, boolean recreateRegionFiles, CallbackInfo ci)
	{
		if (DataManager.getInstance().shouldRunReports())
		{
			DataManager.getInstance().setRegistry(registries);
		}
	}
}

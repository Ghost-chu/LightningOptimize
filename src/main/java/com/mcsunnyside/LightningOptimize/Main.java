package com.mcsunnyside.LightningOptimize;


import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.mcsunnyside.LightningOptimize.Module.AntiWaterFall;
import com.mcsunnyside.LightningOptimize.Module.ChunkAutoUnloader;
import com.mcsunnyside.LightningOptimize.Module.ChunksPreloader;
import com.mcsunnyside.LightningOptimize.Module.DoublePlayerProtect;
import com.mcsunnyside.LightningOptimize.Module.ItemCleaner;
import com.mcsunnyside.LightningOptimize.Module.NoCrowdEntity;
import com.mcsunnyside.LightningOptimize.Module.SmartDisableAI;
import com.mcsunnyside.LightningOptimize.Utils.MsgUtil;
import com.mcsunnyside.LightningOptimize.Utils.Util;
public class Main extends JavaPlugin {
	
	static public Main instance;
	NoCrowdEntity noCrowdEntity;
	static public ConfigurationSection modules;
	
	public long bootTime = 0; // Public bootTime
	public FileConfiguration config;
	private ItemCleaner itemCleaner;
	private ChunkAutoUnloader chunkAutoUnloader;
	private SmartDisableAI smartDisableAI;
	private ChunksPreloader chunksPreloader;
	private AntiWaterFall antiWaterFall;
	private DoublePlayerProtect doublePlayerProtect;
	@Override
	public void onEnable() {
		instance = this; //Keep at first line FOREVER!
		MsgUtil.info("Loader","Plugin Launcher","Initializing...");
		long bootStartTime = System.currentTimeMillis();
		MsgUtil.info("Loader","Plugin Launcher","Checking...");
		boolean checkResult = BootChecker.checkEnv(); // <--- You can check Env in there.
		if(!checkResult) {
			MsgUtil.log("FATAL >> Environment check failed, Plugin will disabled...", Level.SEVERE);;
			Bukkit.getPluginManager().disablePlugin(this);
			return; //Stop booting up
		}
		saveDefaultConfig();
		Util.parseColours(getConfig());
		//Init moduless
		config = getConfig();
		long bootTime = System.currentTimeMillis()-bootStartTime;
		MsgUtil.info("Loader","Plugin Launcher","Finished! ("+bootTime+"ms)");
	}
	
	@Override
	public void onDisable() {
	}
	
	public void initModules() {
		// <--- Registering your moudules in there! See NoCrowedEntity.java for Module standard demo!
		modules = config.createSection("moudules");
		if(modules.getBoolean("NoCrowedEntity.enable"))
			noCrowdEntity = new NoCrowdEntity(this);
		if(modules.getBoolean("ItemCleaner.enable"))
			itemCleaner = new ItemCleaner(this);
		if(modules.getBoolean("ChunkAutoUnloader.enable"))
			chunkAutoUnloader = new ChunkAutoUnloader(this);
		if(modules.getBoolean("SmartDisableAI.enable"))
			smartDisableAI = new SmartDisableAI(this);
		if(modules.getBoolean("ChunksPreloader.enable"))
			chunksPreloader = new ChunksPreloader(this);
		if(modules.getBoolean("AntiWaterFall.enable"))
			antiWaterFall = new AntiWaterFall(this);
		if(modules.getBoolean("AntiWaterFall.enable"))
			antiWaterFall = new AntiWaterFall(this);
		if(modules.getBoolean("DoublePlayerProtect.enable"))
			doublePlayerProtect = new DoublePlayerProtect(this);
	}
	public void uninitModules() {
		// <--- Registering your moudules in there! See NoCrowedEntity.java for Module standard demo!
		modules = config.createSection("moudules");
		if(modules.getBoolean("NoCrowedEntity.enable"))
			noCrowdEntity.uninit();
		if(modules.getBoolean("ItemCleaner.enable"))
			itemCleaner.uninit();
		if(modules.getBoolean("ChunkAutoUnloader.enable"))
			chunkAutoUnloader.uninit();
		if(modules.getBoolean("SmartDisableAI.enable"))
			smartDisableAI.uninit();
		if(modules.getBoolean("ChunksPreloader.enable"))
			chunksPreloader.uninit();
		if(modules.getBoolean("AntiWaterFall.enable"))
			antiWaterFall.uninit();
		if(modules.getBoolean("DoublePlayerProtect.enable"))
			doublePlayerProtect.uninit();
	}
	public void reloadPlugin() {
		MsgUtil.info("Loader","Plugin Launcher","Reloading...");
		Bukkit.getPluginManager().disablePlugin(this);
		Bukkit.getPluginManager().enablePlugin(this);
		MsgUtil.info("Loader","Plugin Launcher","Reloaded...");
	}
	@Override
	public FileConfiguration getConfig() {
		return config;
	}
	public static Main getInstance() {
		return instance;
	}

}

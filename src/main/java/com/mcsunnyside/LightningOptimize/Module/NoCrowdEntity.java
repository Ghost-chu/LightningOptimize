package com.mcsunnyside.LightningOptimize.Module;

import java.util.List;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import com.mcsunnyside.LightningOptimize.Main;
import com.mcsunnyside.LightningOptimize.Utils.MsgUtil;
import com.mcsunnyside.LightningOptimize.Utils.Util;


public class NoCrowdEntity implements Listener {
	public Main plugin;
	private int limits = 25; // You should first init thesethings for default values.
	private List<String> entity_BlackList;
	private List<String> world_BlackList;
	private ConfigurationSection config;
	boolean uninited = false;
	@SuppressWarnings("unchecked")
	public NoCrowdEntity(Main plugin) {
		// You need call Util.setTime to get loading time.
		MsgUtil.info("Moudles",this.getClass().getName(),"Loading...");
		UUID timeUUID = Util.setTimer();
		this.plugin = plugin;
		// Registering your events
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		// Get yourself configSection. DO NOT DO LIKE getConfig.getString("Modules.NoCrowdEntity.limits") STUPID THINGS!
		config = Main.modules.createSection("NoCrowdEntity");
		this.limits = config.getInt("limits");
		this.world_BlackList = (List<String>)config.getList("world_blacklist");
		this.entity_BlackList = (List<String>)config.getList("entity_blacklist");
		// Don't forget this! You will got mess when reload your module!
		uninited = false;
		// Print out load time.
		MsgUtil.info("Moudles",this.getClass().getName(),"Completed ("+Util.endTimer(timeUUID)+"ms)");
	}
	public void uninit() {
		// You need call Util.setTime to get unloading time.
		MsgUtil.info("Moudles",this.getClass().getName(),"Unloading...");
		// Cleam all datas.
		UUID timeUUID = Util.setTimer();
		// Don't forget this! You will got mess when reload your module!
		uninited=true;
		entity_BlackList.clear();
		world_BlackList.clear();
		config=null;
		limits=0;
		// Print out unload time.
		MsgUtil.info("Moudles",this.getClass().getName(),"Unloaded ("+Util.endTimer(timeUUID)+"ms)");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkLoad(ChunkLoadEvent e) {
		if(uninited)return; // You need check and return checking, because your module already uninited! But Bukkit doen't think so.
		crowedCheck(e.getChunk());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnLoad(ChunkUnloadEvent e) {
		if(uninited)return; // You need check and return checking, because your module already uninited! But Bukkit doen't think so.
		crowedCheck(e.getChunk());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldUnLoad(WorldUnloadEvent e) {
		if(uninited)return; // You need check and return checking, because your module already uninited! But Bukkit doen't think so.
		for (Chunk chunk : e.getWorld().getLoadedChunks()) {
			crowedCheck(chunk);
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldLoad(WorldLoadEvent e) {
		if(uninited)return; // You need check and return checking, because your module already uninited! But Bukkit doen't think so.
		for (Chunk chunk : e.getWorld().getLoadedChunks()) {
			crowedCheck(chunk);
		}
	}

	public void crowedCheck(Chunk chunk) {
		if(uninited)return; // You need check and return checking, because your module already uninited! But Bukkit doen't think so.
		Util.debugLog("Moudles >> NoCrowdEntity >> Check crowed chunk: " + chunk.toString());
		if (world_BlackList.contains(chunk.getWorld().getName()))
			return;
		Entity[] entitys = chunk.getEntities();
		if (entitys.length <= limits)
			return;
		int need_remove = entitys.length - limits;
		int removed = 0;
		for (Entity entity : entitys) {
			if (entity_BlackList.contains(entity.getType().name()))
				return;
			Util.debugLog("Moudles >> NoCrowdEntity >> Check crowed chunk >> Removed: " + entity.toString()); // Use debugLog to print out debug NOT directly use Logger!
			entity.remove();
			removed++;
			if (removed >= need_remove)
				break;

		}
	}
	
}

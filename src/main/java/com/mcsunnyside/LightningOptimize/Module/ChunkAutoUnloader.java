package com.mcsunnyside.LightningOptimize.Module;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.mcsunnyside.LightningOptimize.Main;
import com.mcsunnyside.LightningOptimize.Utils.MsgUtil;
import com.mcsunnyside.LightningOptimize.Utils.Util;

public class ChunkAutoUnloader implements Listener {
	Main plugin;
	private ConfigurationSection config;
	private List<String> world_BlackList;
	private boolean unload_NoPlayer = true;
	private boolean unload_OutOfMemory = true;
	private int unload_OutOfMemory_limit = 64;
	private BukkitTask task;
	boolean uninited = false;

	@SuppressWarnings("unchecked")
	public ChunkAutoUnloader(Main plugin) {
		MsgUtil.info("Moudles",this.getClass().getName(),"Loading...");
		UUID timeUUID = Util.setTimer();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
		config = Main.modules.createSection("ChunkAutoUnloader");
		this.world_BlackList = (List<String>)config.getList("world_blacklist");
		this.unload_NoPlayer = config.getBoolean("unloadtype.NoPlayer");
		this.unload_OutOfMemory = config.getBoolean("unloadtype.OutOfMemory.enable");
		this.unload_OutOfMemory_limit = config.getInt("unloadtype.OutOfMemory.limit");
		uninited=false;
		MsgUtil.info("Moudles",this.getClass().getName(),"Completed ("+Util.endTimer(timeUUID)+"ms)");
		cronTask();
	}
	public void uninit() {
		MsgUtil.info("Moudles",this.getClass().getName(),"Unloading...");
		UUID timeUUID = Util.setTimer();
		task.cancel();
		uninited=true;
		world_BlackList.clear();
		config=null;
		MsgUtil.info("Moudles",this.getClass().getName(),"Unloaded ("+Util.endTimer(timeUUID)+"ms)");
	}
	public void cronTask() {
		task = new BukkitRunnable() {
			@Override
			public void run() {
				if(uninited)return;
				List<World> worlds = Bukkit.getWorlds();
				// Remove blacklisted worlds.
				for (World world : worlds) {
					if (world_BlackList.contains(world.getName()))
						return;
					// ============OOF UNLOADER============
					if (unload_OutOfMemory) {
						Runtime javaRuntime = Runtime.getRuntime();
						// Unit:MB
						long trueFreeMemory = ((javaRuntime.maxMemory() - javaRuntime.totalMemory()) / 1024) / 1024;
						if (trueFreeMemory < unload_OutOfMemory_limit) {
							Chunk[] chunks = world.getLoadedChunks();
							for (Chunk chunk : chunks) {
								Entity[] entitys = chunk.getEntities();
								boolean havePlayer = false;
								for (Entity entity : entitys) {
									if (entity.getType() == EntityType.PLAYER) {
										havePlayer = true;
										break;
									}
								}
								if (!havePlayer)
									chunk.unload(true);
							}
						}

					}
					// ====================================
					// ============NOP UNLOADER============
					if(unload_NoPlayer) {
						Chunk[] chunks = world.getLoadedChunks();
						for (Chunk chunk : chunks) {
							Entity[] entitys = chunk.getEntities();
							boolean havePlayer = false;
							for (Entity entity : entitys) {
								if (entity.getType() == EntityType.PLAYER) {
									havePlayer = true;
									break;
								}
							}
							if (!havePlayer)
								chunk.unload(true);
						}	
					}
					// ====================================
				}

			}
		}.runTaskTimer(plugin, 200, 200);
	}

}

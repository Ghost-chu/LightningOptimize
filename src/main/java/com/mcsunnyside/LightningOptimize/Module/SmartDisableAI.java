package com.mcsunnyside.LightningOptimize.Module;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;


import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.mcsunnyside.LightningOptimize.Main;
import com.mcsunnyside.LightningOptimize.Utils.MsgUtil;
import com.mcsunnyside.LightningOptimize.Utils.Util;

public class SmartDisableAI implements Listener {
	public Main plugin;
	private double limits = 14.99;
	private List<String> entity_BlackList;
	private List<String> world_BlackList;
	private ConfigurationSection config;
	private BukkitTask task;
	private int time = 600;
	private List<List<String>> aimap;
	private boolean tag = true;
	private String tagname = "Disabled AI";
	private YamlConfiguration smartaidata;
	private File smartaiFile;
	@SuppressWarnings("unchecked")
	public SmartDisableAI(Main plugin) {
		MsgUtil.info("Moudles",this.getClass().getName(),"Loading...");
		UUID timeUUID = Util.setTimer();
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		config = Main.modules.createSection("SmartDisableAI");
		this.limits = config.getInt("limits");
		this.world_BlackList = (List<String>)config.getList("world_blacklist");
		this.entity_BlackList = (List<String>)config.getList("entity_blacklist");
		this.time = config.getInt("time");
		this.tag = config.getBoolean("tag");
		this.tagname = config.getString("tagname");
		aimap = new ArrayList<List<String>>();
		smartaiFile = new File(plugin.getDataFolder(), "smartai.dat");
		if (!smartaiFile.exists()) {
			plugin.saveResource("smartai.dat", true);
		}
		// Store it
		smartaidata = YamlConfiguration.loadConfiguration(smartaiFile);
		smartaidata.options().copyDefaults(true);
		YamlConfiguration smartaii18nYAML = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("smartai.ai")));
		smartaidata.setDefaults(smartaii18nYAML);
		
		if(smartaidata.getInt("version")==0) {
			smartaidata.set("version", 1);
		}
		//Print language copyright infomation
		try {
			smartaidata.save(smartaiFile);
		} catch (IOException e) {
			e.printStackTrace();
			plugin.getLogger().log(Level.WARNING, "Could not load/save tasks from smartai.dat. Skipping.");
		}
		MsgUtil.info("Moudles",this.getClass().getName(),"Completed ("+Util.endTimer(timeUUID)+"ms)");
		restoreTask();
		cronTask();
		
	}

	private void restoreTask() {
		task = new BukkitRunnable() {
			@Override
			public void run() {
				@SuppressWarnings("unchecked")
				List<List<?>> tasking = (List<List<?>>) smartaidata.getList("waiting");
				if (!tasking.isEmpty())
					MsgUtil.info(
							"Moudles",this.getClass().getName(),"Trying restore not finished work(Server accident shutdown?)...");
				UUID timerUUID = Util.setTimer();
				for (List<?> list : tasking) {
					@SuppressWarnings("unchecked")
					List<String> work = (List<String>) list;
					UUID entityUUID = UUID.fromString(work.get(0));
					World world = Bukkit.getWorld(work.get(1));
					int chunkX = Integer.parseInt(work.get(2));
					int chunkZ = Integer.parseInt(work.get(3));
					world.getChunkAt(chunkX, chunkZ).load(true);
					Entity entity = Bukkit.getEntity(entityUUID);
					enableAI(entity);
					world.getChunkAt(chunkX, chunkZ).unload(true);
				}
				MsgUtil.info("Moudles",this.getClass().getName(),"Completed (" + Util.endTimer(timerUUID) + "ms)");
			}
		}.runTaskLater(plugin, 1); // Make sure only run code after server running.

	}
	public void uninit() {
		MsgUtil.info("Moudles",this.getClass().getName(),"Unloading...");
		UUID timeUUID = Util.setTimer();
		saveDat();
		restoreTask();
		entity_BlackList.clear();
		world_BlackList.clear();
		aimap.clear();
		smartaidata=null;
		config=null;
		smartaiFile=null;
		task.cancel();
		MsgUtil.info("Moudles",this.getClass().getName(),"Unloaded ("+Util.endTimer(timeUUID)+"ms)");
	}

	private void cronTask() {
		task = new BukkitRunnable() {
			@Override
			public void run() {
				switchAI();

			}
		}.runTaskTimer(plugin, time, time);
	}

	private void switchAI() {
		Double tps = Util.getTPS();
		if(limits>=tps) {
			for (List<String> list : aimap) {
				UUID entityUUID = UUID.fromString(list.get(0));
				World world = Bukkit.getWorld(list.get(1));
				int chunkX = Integer.parseInt(list.get(2));
				int chunkZ = Integer.parseInt(list.get(3));
				boolean loaded = world.getChunkAt(chunkX, chunkZ).isLoaded();
				if(!loaded)
					world.getChunkAt(chunkX, chunkZ).load(true);
				Entity entity = Bukkit.getEntity(entityUUID);
				enableAI(entity);
				if(!loaded)
					world.getChunkAt(chunkX, chunkZ).unload(true);
			}
			
		}else {
			// Disable AI
			List<World> worlds = Bukkit.getWorlds();
			List<World> enableWorlds = new ArrayList<World>();
			for (World world : worlds) {
				if (!world_BlackList.contains(world.getName()))
					enableWorlds.add(world);
			}
			List<Entity> entitys = new ArrayList<>();
			for (World world : enableWorlds) {
				for (Entity entity : world.getEntities()) {
					if ((entity instanceof LivingEntity)&&!(entity_BlackList.contains(entity.getType().name())))
						entitys.add(entity);
				}
			}
			for (Entity entity : entitys) {
				disableAI(entity);
			}
			saveDat();
		}
	}
	private void saveDat() {
		smartaidata.set("waiting", aimap);
		try {
			smartaidata.save(smartaiFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
	private void onChunkUnload(ChunkUnloadEvent e) {
		for (Entity entity : e.getChunk().getEntities()) {
			if((entity instanceof LivingEntity)||!(entity_BlackList.contains(entity.getType().name())))
				enableAI(entity);
		}
	}
	@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
	private void onWorldUnload(WorldUnloadEvent e) {

		for (Chunk chunk : e.getWorld().getLoadedChunks()) {
			for (Entity entity : chunk.getEntities()) {
				if((entity instanceof LivingEntity)||!(entity_BlackList.contains(entity.getType().name())))
					enableAI(entity);
			}
		}
		saveDat();
		
	}

	private void disableAI(Entity entity) {
		LivingEntity mob = (LivingEntity) entity;
		if(tag) {
			mob.setCustomName(tagname);
			mob.setCustomNameVisible(true);
		}
		mob.setAI(false);
		List<String> data = new ArrayList<>();
		data.add(mob.getUniqueId().toString());
		data.add(mob.getLocation().getChunk().getWorld().getName());
		data.add(String.valueOf(mob.getLocation().getChunk().getX()));
		data.add(String.valueOf(mob.getLocation().getChunk().getZ()));
		aimap.add(data);
	}

	private void enableAI(Entity entity) {
		LivingEntity mob = (LivingEntity) entity;
		if(mob.getCustomName()==tagname) {
			mob.setCustomName(null);
			mob.setCustomNameVisible(false);
		}
		mob.setAI(true);
	}
}

package com.mcsunnyside.LightningOptimize.Module;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;

import com.mcsunnyside.LightningOptimize.Main;
import com.mcsunnyside.LightningOptimize.Utils.MsgUtil;
import com.mcsunnyside.LightningOptimize.Utils.Util;

public class ItemCleaner implements Listener {
	Main plugin;
	private ConfigurationSection config;
	private List<String> item_BlackList;
	private List<String> world_BlackList;
	private Map<Chunk, UUID> death_chunks;
	boolean uninited = false;
	@SuppressWarnings("unchecked")
	public ItemCleaner(Main plugin) {
		MsgUtil.info("Moudles",this.getClass().getName(),"Loading...");
		UUID timeUUID = Util.setTimer();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
		config = Main.modules.createSection("ItemCleaner");
		this.world_BlackList = (List<String>)config.getList("world_blacklist");
		this.item_BlackList = (List<String>)config.getList("item_blacklist");
		uninited=false;
		MsgUtil.info("Moudles",this.getClass().getName(),"Completed ("+Util.endTimer(timeUUID)+"ms)");
	}
	public void uninit() {
		MsgUtil.info("Moudles",this.getClass().getName(),"Unloading...");
		UUID timeUUID = Util.setTimer();
		uninited=true;
		config=null;
		item_BlackList.clear();
		world_BlackList.clear();
		death_chunks.clear();
		MsgUtil.info("Moudles",this.getClass().getName(),"Unloaded ("+Util.endTimer(timeUUID)+"ms)");
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkLoad(ChunkLoadEvent e) {
		if(uninited)return;
		itemsCheck(e.getChunk());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnLoad(ChunkUnloadEvent e) {
		if(uninited)return;
		itemsCheck(e.getChunk());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldUnLoad(WorldUnloadEvent e) {
		if(uninited)return;
		for (Chunk chunk : e.getWorld().getLoadedChunks()) {
			itemsCheck(chunk);
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldLoad(WorldLoadEvent e) {
		if(uninited)return;
		for (Chunk chunk : e.getWorld().getLoadedChunks()) {
			itemsCheck(chunk);
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent e) {
		if(uninited)return;
		death_chunks.put(e.getEntity().getLocation().getChunk(),e.getEntity().getUniqueId());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent e) {
		if(uninited)return;
		for (Entry<Chunk, UUID> entry : death_chunks.entrySet()) {
			if(e.getPlayer().getUniqueId().equals(entry.getValue())) {
				death_chunks.remove(entry.getKey());
				break;
			}
		}
		
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPickup(EntityPickupItemEvent e) {
		if(uninited)return;
		if(e.getEntityType()!=EntityType.PLAYER)
			return;
		Player player = (Player)e.getEntity();
		if(death_chunks.containsKey(player.getLocation().getChunk()))
			death_chunks.remove(player.getLocation().getChunk());
	}
	
	public void itemsCheck(Chunk chunk) {
		if(uninited)return;
		if(death_chunks.containsKey(chunk))
			return;
		if(world_BlackList.contains(chunk.getWorld().getName()))
			return;
		Entity[] entity = chunk.getEntities();
		for (Entity entity2 : entity) {
			if(entity2.getType()!=EntityType.DROPPED_ITEM)
				return;
			Item item = (Item)entity2;
			ItemStack itemStack = item.getItemStack();
			if(item_BlackList.contains(itemStack.getType().name()))
				return;
			entity2.remove();
		}
	}
}

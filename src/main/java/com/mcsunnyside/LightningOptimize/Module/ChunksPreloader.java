package com.mcsunnyside.LightningOptimize.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import com.mcsunnyside.LightningOptimize.Main;
import com.mcsunnyside.LightningOptimize.Utils.MsgUtil;
import com.mcsunnyside.LightningOptimize.Utils.Util;

public class ChunksPreloader implements Listener {
	List<UUID> ignorePlayer = new ArrayList<>();
	Main plugin;
	boolean uninited = false;
	public ChunksPreloader(Main plugin) {
		MsgUtil.info("Moudles",this.getClass().getName(),"Loading...");
		UUID timeUUID = Util.setTimer();
		this.plugin =plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		uninited = false;
		MsgUtil.info("Moudles",this.getClass().getName(),"Completed ("+Util.endTimer(timeUUID)+"ms)");

	}
	public void uninit() {
		MsgUtil.info("Moudles",this.getClass().getName(),"Unloading...");
		UUID timeUUID = Util.setTimer();
		ignorePlayer.clear();
		uninited =true;
		MsgUtil.info("Moudles",this.getClass().getName(),"Unloaded ("+Util.endTimer(timeUUID)+"ms)");
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if(uninited)return;
		if (ignorePlayer.contains(e.getPlayer().getUniqueId()))
			return;
		TeleportCause cause = e.getCause();
		Location target = e.getTo();
		if (target.getChunk().isLoaded()) // Already loaded, juse send to there.
			return;
		e.setCancelled(true); // First cancel event.
		new BukkitRunnable() {
			@Override
			public void run() {
				// Load chunk
				target.getChunk().load(true);
				ignorePlayer.add(e.getPlayer().getUniqueId());
				e.getPlayer().teleport(target, cause);
				ignorePlayer.remove(e.getPlayer().getUniqueId());

			}
		}.runTask(plugin);
	}
}

package com.mcsunnyside.LightningOptimize.Module;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import com.mcsunnyside.LightningOptimize.Main;
import com.mcsunnyside.LightningOptimize.Utils.MsgUtil;
import com.mcsunnyside.LightningOptimize.Utils.Util;

public class DoublePlayerProtect implements Listener{
	private ConfigurationSection config;

	public DoublePlayerProtect(Main plugin) {
		MsgUtil.info("Moudles",this.getClass().getName(),"Loading...");
		UUID timeUUID = Util.setTimer();
		config = Main.modules.createSection("DoublePlayerProtect");
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		MsgUtil.info("Moudles",this.getClass().getName(),"Completed ("+Util.endTimer(timeUUID)+"ms)");

	}
	@EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
	public void PlayerPreLoginEvent(AsyncPlayerPreLoginEvent e) {
		String playerName = e.getName();
		UUID playerUUID = e.getUniqueId();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if(playerName.equals(player.getName())||playerUUID.toString().equals(e.getUniqueId().toString())) {
				e.setKickMessage(config.getString("moudules.DoublePlayerProtect.message"));
				e.setLoginResult(Result.KICK_OTHER);
				return;
			}
		}
	}
	public void uninit() {
		MsgUtil.info("Moudles",this.getClass().getName(),"Unloading...");
		UUID timeUUID = Util.setTimer();
		config=null;
		MsgUtil.info("Moudles",this.getClass().getName(),"Unloaded ("+Util.endTimer(timeUUID)+"ms)");
	}
}

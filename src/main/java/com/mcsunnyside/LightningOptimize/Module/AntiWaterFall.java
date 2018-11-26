package com.mcsunnyside.LightningOptimize.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import com.mcsunnyside.LightningOptimize.Main;
import com.mcsunnyside.LightningOptimize.Utils.MsgUtil;
import com.mcsunnyside.LightningOptimize.Utils.Util;

public class AntiWaterFall implements Listener {
	List<UUID> ignorePlayer = new ArrayList<>();
	Main plugin;
	int lava_limit = 6;
	int lava_nether_limit = 10;
	int water_limit = 10;
	int other_limit = 10;
	boolean uninited = false;
	private List<String> world_BlackList;
	private ConfigurationSection config;
	public AntiWaterFall(Main plugin) {
		MsgUtil.info("Moudles >> "+this.getClass().getName()+" >> Loading...");
		UUID timeUUID = Util.setTimer();
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		config = Main.modules.createSection("AntiWaterFall");
		lava_limit = config.getInt("lava_limit");
		water_limit = config.getInt("water_limit");
		lava_nether_limit = config.getInt("lava_nether_limit");
		other_limit = config.getInt("other_limit");
		world_BlackList = config.getStringList("world_blacklist");
		uninited = false;
		MsgUtil.info("Moudles >> "+this.getClass().getName()+" >> Completed ("+Util.endTimer(timeUUID)+"ms)");
	}
	public void uninit() {
		MsgUtil.info("Moudles >> "+this.getClass().getName()+" >> Unloading...");
		UUID timeUUID = Util.setTimer();
		MsgUtil.info("Moudles >> "+this.getClass().getName()+" >> Unloaded ("+Util.endTimer(timeUUID)+"ms)");
		uninited = true;
	}
	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void antiWaterfall (BlockFromToEvent e) {
		if(uninited)return;
		if((e.getBlock().getType()!=Material.WATER)&&(e.getBlock().getType()!=Material.LAVA)&&!e.getBlock().isLiquid())
			return;
		if(world_BlackList.contains(e.getBlock().getLocation().getWorld().getName()))
			return;
		//Check!
		int x = e.getToBlock().getLocation().getBlockX();
		int old_y = e.getBlock().getLocation().getBlockY();
		int y = e.getToBlock().getLocation().getBlockX();
		int z = e.getToBlock().getLocation().getBlockX();
		World world = e.getToBlock().getWorld();
		boolean antiGravity = false;
		if(( y - old_y ) <0 )
			antiGravity=true;
		//Holy shit, @Isaac Newton, hey hey hey, wake up! look this holy fuck shit. Oh my fucking god.
		
		ArrayList<Material> sample = new ArrayList<>();
		switch(e.getBlock().getType()) {
		case WATER:
			sample = getSample(world, x, y, z, antiGravity, water_limit);
			break;
		case LAVA:
			if(e.getToBlock().getWorld().getEnvironment()==Environment.NETHER) {
				sample = getSample(world, x, y, z, antiGravity, lava_nether_limit);
			}else {
				sample = getSample(world, x, y, z, antiGravity, lava_limit);
			}
			break;
		default:
			// MOD?
			sample = getSample(world, x, y, z, antiGravity, other_limit);
			break;
		}
		boolean haveSomethingNoAIR = false;
		for (Material type : sample) {
			if(type!=Material.AIR) {
				haveSomethingNoAIR=true;
				break;
			}
		}
		if(haveSomethingNoAIR) {
			return;
		}else {
			e.setCancelled(true);
		}
		
	}
	public ArrayList<Material> getSample(World world,int x ,int y,int z ,boolean antiGravity, int checkLength){
		ArrayList<Material> sample = new ArrayList<>();
		for (int i = 0; i < checkLength; i++) {
			if(!antiGravity) {
				if(y-i<=0) {
					sample.add(Material.BEDROCK);
					return sample;
				}
				sample.add(world.getBlockAt(x, y-i, z).getType());
			}
		}
		return sample;
	}
}

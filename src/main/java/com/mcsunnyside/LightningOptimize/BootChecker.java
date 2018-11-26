package com.mcsunnyside.LightningOptimize;

import java.util.logging.Level;

import com.mcsunnyside.LightningOptimize.Utils.MsgUtil;

public class BootChecker {
	public static Main plugin = Main.instance;
	public static boolean checkEnv() {
		if(!checkSpigot())
			return false;
		
		
		return true;
	}
	private static boolean checkSpigot() {
		try {
			plugin.getServer().spigot();
		}catch (Exception e) {
			MsgUtil.log("FATAL >> You must running LightningOptimize under Spigot/Spigot's fork!", Level.SEVERE);
			return false;
		}
		return true;
	}
}

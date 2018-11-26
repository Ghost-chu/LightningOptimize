package com.mcsunnyside.LightningOptimize.Utils;

import java.util.logging.Level;

import com.mcsunnyside.LightningOptimize.Main;

public class MsgUtil {
	public static Main plugin = Main.instance;
	
	public static void info(String text) {
		log(text, Level.INFO);
	}
	public static void log(String text, Level level) {
		plugin.getLogger().log(level, text);
	}
}

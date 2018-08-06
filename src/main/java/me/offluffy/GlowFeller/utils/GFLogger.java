package me.offluffy.GlowFeller.utils;

import me.offluffy.GlowFeller.GlowFeller;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class GFLogger {
	private GlowFeller gf;
	public GFLogger(GlowFeller gf) { this.gf = gf; }
	private void log(String level, boolean def, ChatColor clr, String msg) {
		if (level.equals("severe") || gf.getConfig().getBoolean("logging-settings.log-" + level, def)) {
			Bukkit.getConsoleSender().sendMessage(clr + "[" + gf.getDescription().getName() + "] " + msg);
		}
	}
	public void custom(String msg) { Bukkit.getConsoleSender().sendMessage(msg); }
	public void info(String msg) { log("info", true, ChatColor.AQUA, msg); }
	public void debug(String msg) { log("debug", false, ChatColor.LIGHT_PURPLE, msg); }
	public void warning(String msg) { log("warning", true, ChatColor.GOLD, msg); }
	public void severe(String msg) { log("severe", true, ChatColor.RED ,msg); }
}

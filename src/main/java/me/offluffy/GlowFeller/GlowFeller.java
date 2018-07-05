package me.offluffy.GlowFeller;

import me.offluffy.GlowFeller.listeners.ArrowHitGlowstoneListener;
import me.offluffy.GlowFeller.listeners.ProjListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GlowFeller extends JavaPlugin {
	public PluginManager pm;
	public void onEnable() {
		pm = Bukkit.getPluginManager();
		pm.registerEvents(new ProjListener(), this);
		pm.registerEvents(new ArrowHitGlowstoneListener(), this);
	}
}

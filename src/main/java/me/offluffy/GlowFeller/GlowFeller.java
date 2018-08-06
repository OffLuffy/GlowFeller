package me.offluffy.GlowFeller;

import me.offluffy.GlowFeller.commands.GlowFellerReload;
import me.offluffy.GlowFeller.listeners.ArrowHitGlowstoneListener;
import me.offluffy.GlowFeller.listeners.ProjListener;
import me.offluffy.GlowFeller.utils.GFLogger;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class GlowFeller extends JavaPlugin {
	public GFLogger logger;
	private Metrics metrics;
	private ArrowHitGlowstoneListener ahg;
	public void onEnable() {
		logger = new GFLogger(this);
		getServer().getPluginManager().registerEvents(new ProjListener(this), this);
		ahg = new ArrowHitGlowstoneListener(this);
		getServer().getPluginManager().registerEvents(ahg, this);
		getServer().getPluginCommand("glowfellerreload").setExecutor(new GlowFellerReload(this));
		saveDefaultConfig();
		reload();
		banner();
	}

	public boolean reload() {
		boolean noErrors = true;
		reloadConfig();
		if (getConfig().getBoolean("enable-metrics", true)) { metrics = new Metrics(this); } else { metrics = null; }
		if (!ahg.reload()) { noErrors = false; }
		return noErrors;
	}

	private void banner() {
		logger.custom(ChatColor.YELLOW + "   __" +  ChatColor.GOLD + "  __");
		logger.custom(ChatColor.YELLOW + "  /__" +  ChatColor.GOLD + " |_      " + ChatColor.YELLOW + getDescription().getName() + ChatColor.GOLD + " v" + getDescription().getVersion());
		logger.custom(ChatColor.YELLOW + "  \\_|" + ChatColor.GOLD + " |       " + ChatColor.DARK_GRAY + "Running on " + Bukkit.getVersion() + " " + Bukkit.getBukkitVersion());
		logger.custom("");
	}
}

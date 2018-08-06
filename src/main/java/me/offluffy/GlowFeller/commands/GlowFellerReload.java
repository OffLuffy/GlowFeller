package me.offluffy.GlowFeller.commands;

import me.offluffy.GlowFeller.GlowFeller;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GlowFellerReload implements CommandExecutor {
	private GlowFeller gf;
	public GlowFellerReload(GlowFeller gf) { this.gf = gf; }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("glowfeller.reload")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return true;
		}
		boolean noErrors = gf.reload();
		sender.sendMessage(ChatColor.AQUA + "Reloaded " + gf.getDescription().getName() + " v" + gf.getDescription().getVersion());
		if (!noErrors) {
			sender.sendMessage(ChatColor.GRAY + "There were errors in the config. Check the console for information!");
		}
		return true;
	}
}

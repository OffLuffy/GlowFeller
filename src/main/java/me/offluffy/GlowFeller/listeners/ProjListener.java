package me.offluffy.GlowFeller.listeners;

import me.offluffy.GlowFeller.GlowFeller;
import me.offluffy.GlowFeller.events.ArrowHitGlowstoneEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;

public class ProjListener implements Listener {

	private GlowFeller gf;
	public ProjListener(GlowFeller gf) { this.gf = gf; }

	@EventHandler
	public void projHit(ProjectileHitEvent e) {
		Projectile proj = e.getEntity();
		ProjectileSource ps = proj.getShooter();
		World w = proj.getWorld();

		if (!(proj instanceof Arrow) || !(ps instanceof Player) || w.getEnvironment() != Environment.NETHER) return;

		Arrow a = (Arrow) proj;
		Player p = (Player) ps;
		BlockIterator bi = new BlockIterator(w, a.getLocation().toVector(), a.getVelocity().normalize(), 0, 4);
		Block hit = null;
		while (bi.hasNext()) {
			hit = bi.next();
			if (hit.getType() != Material.AIR) { break; }
		}

		if (hit != null && hit.getType() == Material.GLOWSTONE) {
			ArrowHitGlowstoneEvent ahge = new ArrowHitGlowstoneEvent(hit, a);
			Bukkit.getPluginManager().callEvent(ahge);
		}
	}
}

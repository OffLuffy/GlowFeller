package net.teamcarbon.glowfeller.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ArrowHitGlowstoneEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private Block hit;
	private Arrow arrow;
	public ArrowHitGlowstoneEvent(Block hit, Arrow arrow)
	{
		this.hit = hit;
		this.arrow = arrow;
	}
	public Block getHitBlock() { return hit; }
	public Arrow getArrow() { return arrow; }
	public boolean isCancelled() { return cancelled; }
	public void setCancelled(boolean b) { cancelled = b; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}

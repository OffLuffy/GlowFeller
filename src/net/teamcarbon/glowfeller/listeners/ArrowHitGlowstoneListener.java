package net.teamcarbon.glowfeller.listeners;

import net.teamcarbon.glowfeller.events.ArrowHitGlowstoneEvent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArrowHitGlowstoneListener implements Listener {

	private final BlockFace[] FACES = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
			BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
	private final Material[] NTYPES = {Material.NETHERRACK, Material.QUARTZ_ORE,
			Material.NETHER_BRICK, Material.NETHER_BRICK_STAIRS, Material.SOUL_SAND};

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void arrowHitGlowstone(ArrowHitGlowstoneEvent e) {
		Block hit = e.getHitBlock();
		e.getArrow().remove();
		hit.breakNaturally();
		e.getArrow().getWorld().playSound(hit.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
		if (!connectedTo(hit, Material.GLOWSTONE)) return;

		// Map of lists. Each list corresponds to a group of glowstone connected to each face of the hit block
		// These may be null if there is no glowstone on that face.
		HashMap<BlockFace, List<Block>> blockLists = new HashMap<>();

		// Iterate over each block face of the hit block
		OUTER:
		for (BlockFace face : FACES) {
			Block rel = e.getHitBlock().getRelative(face);
			// Check that the starter block on this face isn't already stored in another list
			for (BlockFace f : FACES)
				if (blockLists.containsKey(f) && blockLists.get(f).contains(rel))
					continue OUTER;
			// Store the glowstone blocks attached to this face of the hit block (but not the hit block itself)
			List<Block> list = iterateGlowstone(rel, hit, 0, null);
			if (list.contains(hit)) list.remove(hit);
			blockLists.put(face, list);
		}

		// Whether or not to remove the arrow (if it makes any glowstone fall)
		boolean removeArrow = false;

		// Now lets iterate over the stored lists of blocks to check if they're attached to nether-blocks
		OUTER2:
		for (BlockFace face : blockLists.keySet()) {
			// Make sure the block lists map has a list for this block face
			if (blockLists.containsKey(face) && blockLists.get(face) != null) {
				// Iterate over each block in this list
				for (Block block : blockLists.get(face))
					// If it's connected to a nether-block, back out to the outer loop and check the next list
					if (connectedTo(block, NTYPES))
						continue OUTER2;


				// No blocks in the list are connected to nether blocks by this point assumedly
				for (Block block : blockLists.get(face)) {
					// Final double-check that we're only affecting glowstone
					if (block.getType() == Material.GLOWSTONE) {
						// Set the block to air and spawn a falling glowstone
						block.setType(Material.AIR);
						e.getArrow().getWorld().spawnFallingBlock(block.getLocation(), Material.GLOWSTONE, (byte) 0x0);
						removeArrow = true;
					}
				}
			}
		}

		// Finally, remove the arrow if it made any glowstone drop
		// to prevent floating arrows or falling arrows from breaking more glowstone
		if (removeArrow) e.getArrow().remove();
	}

	/**
	 * Checks if any block of the specified types are u/d/n/e/s/w relative to the source block
	 * @param source Source block to check relative blocks of
	 * @param types The types of blocks to check for around the Source
	 * @return Returns true if any block of the specified types was found connected to the source block
	 */
	private boolean connectedTo(Block source, Material ... types) {
		for (BlockFace face : FACES) {
			for (Material m : types) {
				if (source.getRelative(face).getType() == m) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Builds a List of attached glowstone blocks attached to the source, recursively.
	 * @param source The source glowstone blocks.
	 * @param depth The iteration depth, used to terminate the loop if iterating too deep.
	 * @param iterated List of Blocks already iterated over, to prevent iterating over them again
	 * @return Returns a List of blocks attached to the source glowstone block
	 */
	private List<Block> iterateGlowstone(Block source, Block ignore, int depth, List<Block> iterated) {
		iterated = (iterated == null) ? new ArrayList<>() : iterated;
		List<Block> blocks = new ArrayList<>();
		if (depth > 30 || source.getType() != Material.GLOWSTONE || iterated.contains(source)) return blocks;
		iterated.add(source);
		blocks.add(source);
		for (BlockFace face : FACES) {
			Block rel = source.getRelative(face);
			if (rel != ignore) {
				List<Block> list = iterateGlowstone(rel, source, depth + 1, iterated);
				for (Block b : list) if (!blocks.contains(b)) blocks.addAll(list);
			}
		}
		if (depth == 0) iterated.clear();
		return blocks;
	}
}

package me.offluffy.GlowFeller.listeners;

import me.offluffy.GlowFeller.GlowFeller;
import me.offluffy.GlowFeller.events.ArrowHitGlowstoneEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ArrowHitGlowstoneListener implements Listener {

	private static Material resolveMat(String m) { try { return Material.valueOf(m); } catch (Exception e) { return null; } }
	private static PotionEffectType resolvePotionType(String p) {
		p = p.replace(" ", "").replace("_", "").replace("-", "");
		for (PotionEffectType pet : PotionEffectType.values()) {
			if (pet == null) continue;
			String pn = pet.getName().replace("_", "");
			if (pn.equalsIgnoreCase(p)) return pet;
		}
		return null;
	}

	private static final BlockFace[] FACES = {
			BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
			BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
	};

	private Set<Material> NETHER_TYPES = new HashSet<>();
	private Set<PotionEffectType> TIP_TYPES = new HashSet<>();
	private Set<World> NO_USE = new HashSet<>();

	private static GlowFeller gf;
	public ArrowHitGlowstoneListener(GlowFeller gf) { ArrowHitGlowstoneListener.gf = gf; }


	@EventHandler(ignoreCancelled = true)
	public void arrowHitGlowstone(ArrowHitGlowstoneEvent e) {
		if (NO_USE.contains(e.getHitBlock().getWorld())) { e.setCancelled(true); return; }
		Block hit = e.getHitBlock();
		Arrow arrow = e.getArrow();
		if (dropCheck(arrow)) {
			hit.setType(Material.AIR);
			hit.getWorld().dropItem(hit.getLocation(), new ItemStack(Material.GLOWSTONE));
		} else { hit.breakNaturally(); }
		e.getArrow().remove();
		e.getArrow().getWorld().playSound(hit.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
		if (!gf.getConfig().getBoolean("use-gravity", true) || !connectedTo(hit, Material.GLOWSTONE)) return;

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
			list.remove(hit);
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
					if (connectedTo(block, NETHER_TYPES))
						continue OUTER2;


				// No blocks in the list are connected to nether blocks by this point assumedly
				for (Block block : blockLists.get(face)) {
					// Final double-check that we're only affecting glowstone
					if (block.getType() == Material.GLOWSTONE) {
						// Set the block to air and spawn a falling glowstone
						e.getArrow().getWorld().spawnFallingBlock(block.getLocation().clone().add(0.5, 0.0, 0.5), block.getState().getData().clone());
						block.setType(Material.AIR);
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
	private boolean connectedTo(Block source, Set<Material> types) {
		for (Material m : types) { if (connectedTo(source, m)) return true; }
		return false;
	}

	/**
	 * Checks if a block of the specified types are u/d/n/e/s/w relative to the source block
	 * @param source Source block to check relative blocks of
	 * @param type The type of block to check for around the Source
	 * @return Returns true if a block of the specified type was found connected to the source block
	 */
	private boolean connectedTo(Block source, Material type) {
		for (BlockFace face : FACES) {
			if (source.getRelative(face).getType() == type) {
				return true;
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
		if (depth > gf.getConfig().getInt("search-iterations", 30) || source.getType() != Material.GLOWSTONE || iterated.contains(source)) return blocks;
		iterated.add(source);
		blocks.add(source);
		for (BlockFace face : FACES) {
			Block rel = source.getRelative(face);
			if (rel != ignore) {
				List<Block> list = iterateGlowstone(rel, source, ++depth, iterated);
				for (Block b : list) if (!blocks.contains(b)) blocks.addAll(list);
			}
		}
		if (depth == 0) iterated.clear();
		return blocks;
	}

	private boolean dropCheck(Arrow arrow) {
		return gf.getConfig().getBoolean("all-drops-blocks", false) || spectralDropCheck(arrow) || tippedDropCheck(arrow);
	}
	private boolean spectralDropCheck(Arrow arrow) {
		return gf.getConfig().getBoolean("spectral-drops-blocks", false) && arrow instanceof SpectralArrow;
	}
	private boolean tippedDropCheck(Arrow arrow) {
		if (!gf.getConfig().getBoolean("tipped-drops-blocks", false)) return false;
		if (!(arrow instanceof TippedArrow)) return false;
		TippedArrow ta = (TippedArrow) arrow;
		return TIP_TYPES.contains(ta.getBasePotionData().getType().getEffectType());
	}

	private void addNetherType(Material m) { if (m != null) NETHER_TYPES.add(m); }

	public boolean reload() {
		boolean noErrors = true;

		NETHER_TYPES = new HashSet<>();
		TIP_TYPES = new HashSet<>();
		NO_USE = new HashSet<>();

		// These don't exist after 1.12.2
		addNetherType(resolveMat("QUARTZ_ORE"));

		// These don't exist until 1.13
		addNetherType(resolveMat("NETHER_QUARTZ_ORE"));
		addNetherType(resolveMat("NETHER_BRICK_SLAB"));
		addNetherType(resolveMat("RED_NETHER_BRICKS"));

		// These should be safe
		addNetherType(resolveMat("NETHERRACK"));
		addNetherType(resolveMat("NETHER_BRICK"));
		addNetherType(resolveMat("NETHER_BRICK_STAIRS"));
		addNetherType(resolveMat("SOUL_SAND"));
		addNetherType(resolveMat("NETHER_WART_BLOCK"));
		addNetherType(resolveMat("OBSIDIAN"));

		gf.logger.debug("=====[ Config settings ]==========");
		gf.logger.debug("Search iteration depth set to " + gf.getConfig().getInt("search-iterations", 50));

		gf.logger.debug("Discovered " + NETHER_TYPES.size() + " nether-type blocks:");
		StringJoiner types = new StringJoiner(", ");
		for (Material type : NETHER_TYPES) { types.add(type.name()); }
		gf.logger.debug(types.toString());

		List<String> tippedTypeNames = gf.getConfig().getStringList("tipped-arrow-types");
		for (String tippedTypeName : tippedTypeNames) {
			PotionEffectType pet = resolvePotionType(tippedTypeName);
			if (pet != null) { TIP_TYPES.add(pet); } else {
				noErrors = false;
				gf.logger.warning("Invalid PotionEffectType found in GlowFeller config 'tipped-arrow-types': " + tippedTypeName);
				gf.logger.warning("Make sure it's spelled correctly and that it's available in your version!");
			}
		}

		gf.logger.debug("All arrows " + (gf.getConfig().getBoolean("all-drops-blocks", false) ? "will" : "won't") + " drop blocks");
		gf.logger.debug("Spectral arrows " + (gf.getConfig().getBoolean("spectral-drops-blocks", false) ? "will" : "won't") + " drop blocks");

		if (gf.getConfig().getBoolean("tipped-drops-blocks", false)) {
			gf.logger.debug("Discovered " + TIP_TYPES.size() + " tipped-arrow types that will drop blocks:");
			types = new StringJoiner(", ");
			for (PotionEffectType type : TIP_TYPES) { types.add(type.getName()); }
			gf.logger.debug(types.toString());
		} else {
			gf.logger.debug("Tipped arrows won't drop blocks");
		}

		if (gf.getConfig().isList("ignore-worlds")) {
			for (String s : gf.getConfig().getStringList("ignore-worlds")) {
				World w = Bukkit.getWorld(s);
				if (w == null) {
					noErrors = false;
					gf.logger.warning("Found a World that doesn't exist in the GlowFeller config 'ignore-worlds': " + s);
				} else if (w.getEnvironment() != World.Environment.NETHER) {
					noErrors = false;
					gf.logger.warning("Found a non-Nether World in the GlowFeller config 'ignore-worlds': " + s + ", only Nether environments are supported");
				} else {
					NO_USE.add(w);
				}
			}
		} else if (gf.getConfig().contains("ignore-worlds")) {
			noErrors = false;
			gf.logger.warning("'ignore-worlds' in GlowFeller's config should be a list");
		}
		if (NO_USE.size() > 0) {
			gf.logger.debug("Discovered  " + NO_USE.size() + " ignored Nether worlds:");
			types = new StringJoiner(", ");
			for (World world : NO_USE) {
				types.add(world.getName());
			}
			gf.logger.debug(types.toString());
		} else { gf.logger.debug("No Nether worlds are being ignored"); }

		return noErrors;
	}
}

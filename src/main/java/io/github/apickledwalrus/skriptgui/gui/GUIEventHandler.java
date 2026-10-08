package io.github.apickledwalrus.skriptgui.gui;

import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class GUIEventHandler {

	/**
	 * To be used to control whether this event handler processes an event.
	 */
	private boolean paused;

	/**
	 * To be used to control whether this event handler processes an event for a specific player.
	 */
	private final List<Player> pausedFor = new ArrayList<>();

	/**
	 * Resumes handling of events for this handler.
	 */
	public void resume() {
		paused = false;
	}

	/**
	 * Resumes handling of events for this handler for the given player.
	 */
	public void resume(Player player) {
		pausedFor.remove(player);
	}

	/**
	 * Pauses handling of events for this handler.
	 */
	public void pause() {
		paused = true;
	}

	/**
	 * Pauses handling of events for this handler for the given player.
	 */
	public void pause(Player player) {
		pausedFor.add(player);
	}

	/**
	 * @return Whether this event handler is processing events.
	 * True if not processing, false if processing.
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * @param player The player to check for.
	 * @return Whether this event handler is processing events for the given player.
	 * If event processing is globally disabled, this method will reflect that.
	 * True if not processing, false if processing.
	 */
	public boolean isPaused(Player player) {
		return isPaused() || pausedFor.contains(player);
	}

	public abstract void onClick(InventoryPreClickEvent e);
	public abstract void onDrag(InventoryPreClickEvent e);
	public abstract void onOpen(InventoryOpenEvent e);
	public abstract void onClose(InventoryCloseEvent e);
	public abstract void onChange(Player player, ItemStack[] before);

}

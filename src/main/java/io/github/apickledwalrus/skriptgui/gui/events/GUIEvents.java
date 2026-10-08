package io.github.apickledwalrus.skriptgui.gui.events;

import io.github.apickledwalrus.skriptgui.SkriptGUI;
import io.github.apickledwalrus.skriptgui.gui.GUI;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class GUIEvents {

	public static void register(EventNode<Event> node) {
		EventNode<InventoryEvent> child = EventNode.type("skript-gui-minestom-inventory", EventFilter.INVENTORY);
		child.addListener(InventoryPreClickEvent.class, event -> {
			if (event.getClick() instanceof Click.Drag drag) {
				GUI gui = SkriptGUI.getGUIManager().getGUI(event.getInventory());
				if (gui != null) {
					// Check if any slots in the actual GUI were changed. We don't care if only the player's inventory was changed.
					int lastSlotIndex = gui.getInventory().getSize() - 1;
					for (int slot : drag.slots()) {
						if (slot <= lastSlotIndex) { // A slot in the actual GUI was interacted with
							gui.getEventHandler().onDrag(event);
							break;
						}
					}
				}
				return;
			}

			Player player = event.getPlayer();
			// Process this event if it's cancelled ONLY if the clicker is in Spectator Mode
			if (player.getGameMode() != GameMode.SPECTATOR && event.isCancelled()) {
				return;
			}

			// Don't handle this event if it's from an unsupported click type
			switch (event.getClick()) {
				/*case WINDOW_BORDER_RIGHT:
				case WINDOW_BORDER_LEFT:*/
				case Click.Middle ignored:
					return;
                default:
            }

			// No inventory was clicked
			AbstractInventory clickedInventory = event.getInventory();

            // Don't handle this event if there isn't a matching GUI for it
			GUI gui = SkriptGUI.getGUIManager().getGUI(event.getInventory());
			if (gui == null) {
				return;
			}

			// Don't process unknown clicks for safety reasons - cancel them to prevent unwanted GUI changes
			//if (event.getClick() == ClickType.UNKNOWN) {
			if (event.getSlot() == -999) {
				event.setCancelled(true);
				return;
			}

			// Don't handle this event if the clicked inventory is the bottom inventory, as we want users to be able to interact with their inventory
			// However, there are some cases where interaction with the bottom inventory may cause changes to the top inventory
			// Because of this, we will cancel the event for some click types
			if (clickedInventory.equals(player.getInventory())) {
				switch (event.getClick()) {
					case Click.RightShift ignored -> handleShiftMove(event, gui);
					case Click.LeftShift ignored -> handleShiftMove(event, gui);
					case Click.Double ignored -> {
						// Only cancel if this will cause a change to the GUI itself
						// We are checking if our GUI contains an item that could be merged with the event item
						// If that item is mergeable but it isn't stealable, we will cancel the event now
						Inventory guiInventory = gui.getInventory();
						int size = guiInventory.getSize();
						ItemStack cursor = player.getInventory().getCursorItem();
						for (int slot = 0; slot < size; slot++) {
							ItemStack item = guiInventory.getItemStack(slot);
							if (!item.material().equals(Material.AIR) && item.isSimilar(cursor) && !gui.isRemovable(gui.convert(slot))) {
								event.setCancelled(true);
								break;
							}
						}
						return;
					}
                    default -> {
                        return;
                    }
                }
			}

			gui.getEventHandler().onClick(event);
		});

		child.addListener(InventoryPreClickEvent.class, event -> {
			if (event.isCancelled()) return;
			Player player = event.getPlayer();
			AbstractInventory openInventory = player.getOpenInventory();
			if (openInventory == null) return;
			GUI gui = SkriptGUI.getGUIManager().getGUI(openInventory);
			if (gui == null || !gui.hasChangeListeners()) return;
			ItemStack[] before = gui.getInventory().getItemStacks();
			player.scheduleNextTick(entity -> gui.getEventHandler().onChange(player, before));
		});

		child.addListener(InventoryOpenEvent.class, event -> {
			GUI gui = SkriptGUI.getGUIManager().getGUI(event.getInventory());
			if (gui != null) {
				gui.getEventHandler().onOpen(event);
			}
		});
		child.addListener(InventoryCloseEvent.class, event -> {
			GUI gui = SkriptGUI.getGUIManager().getGUI(event.getInventory());
			if (gui != null) {
				gui.getEventHandler().onClose(event);
			}
		});

		node.addChild(child);
	}

	private static void handleShiftMove(InventoryPreClickEvent event, GUI gui) {

		ItemStack clicked = event.getClickedItem();
		Inventory guiInventory = gui.getInventory();

		if (!contains(guiInventory, clicked.material())) {
			int firstEmpty = firstEmpty(guiInventory);
			if (firstEmpty != -1 && gui.isRemovable(gui.convert(firstEmpty))) { // Safe to be moved into the GUI
				return;
			}
		}

		int size = guiInventory.getSize();

		for (int slot = 0; slot < size; slot++) {
			ItemStack item = guiInventory.getItemStack(slot);
			if (item.material() != Material.AIR && item.isSimilar(clicked)) {
				if (!gui.isRemovable(gui.convert(slot))) {
					if (item.amount() == 64) { // It wouldn't be able to combine
						continue;
					}
					event.setCancelled(true);
					return;
				}

				if (item.amount() + clicked.amount() <= 64) { // This will only modify a modifiable slot
					return;
				}
			}
		}

		int firstEmpty = firstEmpty(guiInventory);
		if (firstEmpty != -1 && gui.isRemovable(gui.convert(firstEmpty))) { // Safe to be moved into the GUI
			return;
		}

		event.setCancelled(true);
	}

	private static boolean contains(Inventory inventory, Material type) {
		for (ItemStack i : inventory.getItemStacks()) {
			if (i.material().equals(type)) return true;
		}
		return false;
	}

	private static int firstEmpty(Inventory inventory) {
		ItemStack[] stacks = inventory.getItemStacks();
		for (int i = 0; i < stacks.length; i++) {
			if (stacks[i].material().equals(Material.AIR)) return i;
		}
		return -1;
	}

}

package io.github.apickledwalrus.skriptgui.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.wrapper.InventoryPreClickWrapper;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import io.github.apickledwalrus.skriptgui.SkriptGUI;
import io.github.apickledwalrus.skriptgui.gui.GUI;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.function.Consumer;

@Name("GUI Slot Change")
@Description({
		"A section for executing code after a player changes a slot, whether by clicking, shift clicking, dragging or double clicking.",
		"It runs on the tick after the change, so the slot already holds its new item.",
		"For GUIs with a shape where multiple slots are represented by a single character, the section runs when any of those slots change."
})
@Examples({
		"create a gui with virtual chest 3 row inventory named \"My GUI\" and shape \"xxxxxxxxx\", \"x-------x\" and \"xxxxxxxxx\"",
		"	run when slot 1 changes:",
		"		send \"You changed slot 1\" to player",
		"	run when slot \"-\" changes:",
		"		send \"You changed an interior slot\" to player"
})
@Since("1.4.0")
public class SecSlotChange extends Section {

	static {
		Skript.registerSection(SecSlotChange.class,
				"run when [gui] slot[s] %integers/strings% change[s]",
				"run when [gui] slot[s] %integers/strings% (is|are) changed",
				"run on change of [gui] slot[s] %integers/strings%"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Trigger trigger;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Object> slots;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		if (!getParser().isCurrentSection(SecCreateGUI.class)) {
			Skript.error("You can't listen for changes of a slot outside of a GUI creation or editing section.");
			return false;
		}

		trigger = loadCode(sectionNode, "inventory click", InventoryPreClickWrapper.class);
		slots = (Expression<Object>) exprs[0];

		return true;
	}

	@Override
	@Nullable
	public TriggerItem walk(Event e) {
		GUI gui = SkriptGUI.getGUIManager().getGUI(e);
		if (gui == null) return walk(e, false);

		Object variables = Variables.copyLocalVariables(e);
		Consumer<InventoryPreClickWrapper> onChange = event -> {
			if (variables != null) {
				Variables.setLocalVariables(event, variables);
			}
			trigger.execute(event);
		};

		for (Object slot : slots.getArray(e)) {
			Character converted = gui.convert(slot);
			GUI.SlotData slotData = gui.getSlotData(converted);
			if (slotData == null) {
				gui.setItem(converted, null, false, null);
				slotData = gui.getSlotData(converted);
				if (slotData == null) continue;
			}
			slotData.setRunOnChange(onChange);
		}

		return walk(e, false);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		boolean single = slots.isSingle();
		return "run when gui slot" + (single ? " " : "s ") + slots.toString(e, debug) + " change" + (single ? "s" : "");
	}

}

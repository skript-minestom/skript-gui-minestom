package io.github.apickledwalrus.skriptgui.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.github.apickledwalrus.skriptgui.gui.GUI;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Lock/Unlock GUI")
@Description("Locks or unlocks a GUI, which controls whether its items without actions can be removed or changed.")
@Examples("unlock the player's gui")
@Since("1.4.0")
public class EffLock extends Effect {

	static {
		Skript.registerEffect(EffLock.class,
				"(lock|:unlock) [gui[s]] %guiinventorys%",
				"allow items to be (removed|changed) from %guiinventorys%",
				"(disallow|prevent) items [from] being (removed|changed) from %guiinventorys%"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<GUI> guis;
	private boolean removable;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		guis = (Expression<GUI>) exprs[0];
		removable = parseResult.hasTag("unlock") || matchedPattern == 1;
		return true;
	}

	@Override
	protected void execute(Event e) {
		for (GUI gui : guis.getArray(e)) {
			gui.setRemovable(removable);
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (removable ? "unlock " : "lock ") + guis.toString(e, debug);
	}

}

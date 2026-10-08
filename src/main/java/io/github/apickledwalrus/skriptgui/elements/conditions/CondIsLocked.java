package io.github.apickledwalrus.skriptgui.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.github.apickledwalrus.skriptgui.gui.GUI;

@Name("Is GUI Locked")
@Description("Whether a GUI is locked or unlocked, which controls whether its items without actions can be removed or changed.")
@Examples({
		"if the player's gui is locked:",
		"	send \"You cannot remove items from this GUI!\" to the player"
})
@Since("1.4.0")
public class CondIsLocked extends PropertyCondition<GUI> {

	static {
		String[] bePatterns = getPatterns(PropertyType.BE, "(:locked|unlocked)", "guiinventorys");
		for (int i = 0; i < bePatterns.length; i++) {
			bePatterns[i] = "[gui[s]] " + bePatterns[i];
		}
		Skript.registerCondition(CondIsLocked.class,
				bePatterns[0],
				bePatterns[1],
				"%guiinventorys% allow[s] items to be (removed|changed)",
				"%guiinventorys% (disallow|prevent)[s] items [from] being (removed|changed)"
		);
	}

	private boolean locked;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		locked = parseResult.hasTag("locked") || matchedPattern == 3;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(GUI gui) {
		return gui.isRemovable() != locked;
	}

	@Override
	protected String getPropertyName() {
		return locked ? "locked" : "unlocked";
	}

}

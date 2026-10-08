package io.github.apickledwalrus.skriptgui.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.github.apickledwalrus.skriptgui.gui.GUI;
import net.minestom.server.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Anvil Text Input")
@Description({
		"The text a player has typed into the rename field of an anvil GUI.",
		"Without a player, it is the most recent text typed into that GUI by anyone.",
		"The rename field only accepts text while the first slot holds an item."
})
@Examples({
		"command /getinput:",
		"	trigger:",
		"		create a gui with virtual anvil inventory named \"Enter Input\":",
		"			make gui slot 0 with barrier named \"Enter Input\"",
		"			make gui slot 2 with air:",
		"				set {_input} to the anvil text input of the gui for player",
		"				close the player's inventory",
		"				send \"You entered %{_input}%\" to player",
		"		open last gui to player"
})
@Since("1.4.0")
public class ExprAnvilInput extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprAnvilInput.class, String.class, ExpressionType.PROPERTY,
				"[the] anvil [text] input of %guiinventory% [(for|from) %-player%]",
				"%guiinventory%'[s] anvil [text] input [(for|from) %-player%]"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<GUI> gui;
	@Nullable
	private Expression<Player> player;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		gui = (Expression<GUI>) exprs[0];
		player = (Expression<Player>) exprs[1];
		return true;
	}

	@Override
	protected String @Nullable [] get(Event e) {
		GUI gui = this.gui.getSingle(e);
		if (gui == null) return new String[0];
		Player player = this.player != null ? this.player.getSingle(e) : null;
		if (this.player != null && player == null) return new String[0];
		String input = gui.getAnvilInput(player);
		return input != null ? new String[]{input} : new String[0];
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the anvil text input of " + gui.toString(e, debug) + (player != null ? " for " + player.toString(e, debug) : "");
	}

}

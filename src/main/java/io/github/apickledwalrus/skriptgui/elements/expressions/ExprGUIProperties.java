package io.github.apickledwalrus.skriptgui.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.ComponentWrapper;
import ch.njol.skript.util.InventoryType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.github.apickledwalrus.skriptgui.gui.GUI;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import static ch.njol.skript.effects.EffOpenInventory.getDefaultTitle;

@Name("GUI Properties")
@Description("Different properties of a GUI. They can be modified.")
@Examples({
		"edit gui last gui:",
		"\tset the name of the edited gui to \"New GUI Name!\"",
		"\tset the rows of the edited gui to 3 # Sets the number of rows to 3 (if possible)",
		"\tset the shape of the edited gui to \"xxxxxxxxx\", \"x-------x\", and \"xxxxxxxxx\"",
		"\tset the lock status of the edited gui to false # Players can take items from this GUI now"
})
@Since("1.0.0, 1.3 (rework, support outside of edit sections)")
public class ExprGUIProperties extends SimplePropertyExpression<GUI, Object> {

	static {
		register(ExprGUIProperties.class, Object.class, "(0¦[skript-]gui name[s]|1¦([skript-]gui size[s]|rows)|2¦shape[s]|3¦lock status[es])", "guiinventorys");
	}

	private static final int NAME = 0, ROWS = 1, SHAPE = 2, LOCK_STATUS = 3;
	private int property;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		property = parseResult.mark;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Object convert(GUI gui) {
        return switch (property) {
            case NAME -> gui.getName();
            case ROWS -> gui.getInventory().getSize() / 9; // We return rows
            case SHAPE -> gui.getRawShape();
            case LOCK_STATUS -> !gui.isRemovable(); // Not removable = locked
            default -> null;
        };
    }

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET) {
			switch (property) {
				case NAME:
					return CollectionUtils.array(ComponentWrapper.class, Component.class);
				case ROWS:
					return CollectionUtils.array(Number.class);
				case SHAPE:
					return CollectionUtils.array(String[].class);
				case LOCK_STATUS:
					return CollectionUtils.array(Boolean.class);
			}
		}
		return null;
	}

	@Override
	public void change(Event e, Object @Nullable [] delta, ChangeMode mode) {
		if ((mode != ChangeMode.SET && mode != ChangeMode.RESET) || (mode == ChangeMode.SET && delta == null)) {
			return;
		}
		for (GUI gui : getExpr().getArray(e)) {
			switch (mode) {
				case SET:
					switch (property) {
						case NAME:
							gui.setName(delta[0] instanceof ComponentWrapper wrapper ? wrapper.getComponent() : (Component) delta[0]);
							break;
						case ROWS:
							gui.setSize(((Number) delta[0]).intValue() * 9);
							break;
						case SHAPE:
							String[] newShape = new String[delta.length];
							for (int i = 0; i < delta.length; i++) {
								if (!(delta[i] instanceof String)) {
									return;
								}
								newShape[i] = (String) delta[i];
							}
							gui.setShape(newShape);
							break;
						case LOCK_STATUS:
							gui.setRemovable(!(boolean) delta[0]);
							break;
					}
					break;
				case RESET:
					switch (property) {
						case NAME:
                            //noinspection DataFlowIssue
                            gui.setName(getDefaultTitle(InventoryType.of(gui.getInventory().getInventoryType())));
							break;
						case ROWS:
							gui.setSize(Math.max(gui.getInventory().getInnerSize()/9, 1));
							break;
						case SHAPE:
							gui.resetShape();
							break;
						case LOCK_STATUS:
							gui.setRemovable(false);
							break;
					}
					break;
				default:
					assert false;
			}
		}
	}

	@Override
	public Class<?> getReturnType() {
        return switch (property) {
            case NAME, SHAPE -> Component.class;
            case ROWS -> Number.class;
            case LOCK_STATUS -> Boolean.class;
            default -> Object.class;
        };
	}

	@Override
	protected String getPropertyName() {
        return switch (property) {
            case NAME -> "name";
            case ROWS -> "size";
            case SHAPE -> "shape";
            case LOCK_STATUS -> "lock status";
            default -> "property";
        };
	}

}

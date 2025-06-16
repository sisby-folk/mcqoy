package dev.sisby.mcqoy.controller;

import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
import net.minecraft.client.gui.DrawContext;

public class EntryControllerElement<T> extends ControllerWidget<EntryController<T>> {
	private final AbstractWidget keyWidget;
	private final AbstractWidget valueWidget;

	public EntryControllerElement(EntryController<T> control, YACLScreen screen, Dimension<Integer> dim, AbstractWidget keyWidget, AbstractWidget valueWidget) {
		super(control, screen, dim);
		this.keyWidget = keyWidget;
		this.valueWidget = valueWidget;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return keyWidget.mouseClicked(mouseX, mouseY, button) || valueWidget.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return keyWidget.keyPressed(keyCode, scanCode, modifiers) || valueWidget.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
		keyWidget.render(graphics, mouseX, mouseY, delta);
		valueWidget.render(graphics, mouseX, mouseY, delta);
	}

	@Override
	public void setDimension(Dimension<Integer> dim) {
		super.setDimension(dim);
		if (valueWidget instanceof TickBoxController.TickBoxControllerElement) {
			this.keyWidget.setDimension(dim.withWidth(dim.width() - 20));
			this.valueWidget.setDimension(dim.withWidth(20).moved(dim.width() - 20, 0));
		} else {
			this.keyWidget.setDimension(dim.withWidth(dim.width() / 2));
			this.valueWidget.setDimension(dim.withWidth(dim.width() / 2).moved(dim.width() / 2, 0));
		}
	}

	@Override
	protected int getHoveredControlWidth() {
		return getUnhoveredControlWidth();
	}
}

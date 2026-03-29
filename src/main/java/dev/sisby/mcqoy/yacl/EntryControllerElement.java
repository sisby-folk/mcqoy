package dev.sisby.mcqoy.yacl;

import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class EntryControllerElement<T> extends ControllerWidget<EntryController<T>> {
	private final AbstractWidget keyWidget;
	private final AbstractWidget valueWidget;

	public EntryControllerElement(EntryController<T> control, YACLScreen screen, Dimension<Integer> dim, AbstractWidget keyWidget, AbstractWidget valueWidget) {
		super(control, screen, dim);
		this.keyWidget = keyWidget;
		this.valueWidget = valueWidget;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
		return keyWidget.mouseClicked(mouseButtonEvent, doubleClick) || valueWidget.mouseClicked(mouseButtonEvent, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		return keyWidget.keyPressed(keyEvent) || valueWidget.keyPressed(keyEvent);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		keyWidget.extractRenderState(graphics, mouseX, mouseY, delta);
		valueWidget.extractRenderState(graphics, mouseX, mouseY, delta);
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

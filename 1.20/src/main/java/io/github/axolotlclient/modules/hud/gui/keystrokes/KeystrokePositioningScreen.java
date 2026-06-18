/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import io.github.axolotlclient.modules.hud.snapping.SnappingHelper;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeystrokePositioningScreen extends Screen {
	private final Screen parent;
	private final KeystrokeHud hud;
	private KeystrokeHud.Keystroke focused;
	private final KeystrokeHud.Keystroke editing;
	private static final long MOVE_CURSOR = GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_ALL_CURSOR);
	private static final long DEFAULT_CURSOR = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
	private long currentCursor;

	public KeystrokePositioningScreen(Screen parent, KeystrokeHud hud, KeystrokeHud.Keystroke focused) {
		super(Text.translatable("keystrokes.stroke.move"));
		this.parent = parent;
		this.hud = hud;
		if (hud.keystrokes == null) {
			hud.setKeystrokes();
		}
		this.editing = focused;
		mouseDown = false;
	}

	public KeystrokePositioningScreen(Screen parent, KeystrokeHud hud) {
		this(parent, hud, null);
	}

	private DrawPosition offset = null;
	private boolean mouseDown;
	private SnappingHelper snap;

	@Override
	protected void init() {
		addDrawableChild(ButtonWidget.builder(CommonTexts.BACK, b -> closeScreen()).position(width / 2 - 75, height - 50 + 22).width(150).build());
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("hud.snapping").append(": ")
				.append(Text.translatable(HudManager.getInstance().isSnappingEnabled() ? "options.on" : "options.off")),
			buttonWidget -> {
				HudManager.getInstance().toggleSnapping();
				buttonWidget.setMessage(Text.translatable("hud.snapping").append(": ")
					.append(Text.translatable(HudManager.getInstance().isSnappingEnabled() ? "options.on" : "options.off")));
			}).positionAndSize(width / 2 - 50, height - 50, 100, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		guiGraphics.getMatrices().push();
		guiGraphics.getMatrices().translate(0, 0, -300);
		super.renderBackground(guiGraphics);
		HudManager.getInstance().renderPlaceholder(guiGraphics, partialTick);
		guiGraphics.getMatrices().pop();
		guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		Optional<KeystrokeHud.Keystroke> hovered = Optional.empty();
		if (editing != null) {
			drawStroke(guiGraphics, mouseX, mouseY, editing);
			if (getScaledRenderPos(editing).isMouseOver(mouseX, mouseY)) {
				hovered = Optional.of(editing);
			}
		} else {
			for (KeystrokeHud.Keystroke k : hud.keystrokes) {
				var pos = Optional.of(getScaledRenderPos(k));
				if (pos.get().isMouseOver(mouseX, mouseY)) {
					hovered = Optional.of(k);
				}
				drawStroke(guiGraphics, mouseX, mouseY, k);
			}
		}
		setCursor(hovered.isPresent() ? MOVE_CURSOR : DEFAULT_CURSOR);
		if (mouseDown && snap != null) {
			snap.renderSnaps(guiGraphics);
		}
	}

	private void setCursor(long cursor) {
		if (cursor > 0 && cursor != currentCursor) {
			currentCursor = cursor;
			GLFW.glfwSetCursor(MinecraftClient.getInstance().getWindow().getHandle(), cursor);
		}
	}

	private void drawStroke(GuiGraphics guiGraphics, int mouseX, int mouseY, KeystrokeHud.Keystroke s) {
		var rect = getScaledRenderPos(s);
		if (rect.isMouseOver(mouseX, mouseY)) {
			DrawUtil.fillRect(guiGraphics, rect, ClientColors.SELECTOR_BLUE.withAlpha(100));
		} else {
			DrawUtil.fillRect(guiGraphics, rect, ClientColors.WHITE.withAlpha(50));
		}
		guiGraphics.getMatrices().push();
		guiGraphics.getMatrices().scale(hud.getScale(), hud.getScale(), 1);
		s.render(guiGraphics);
		guiGraphics.getMatrices().pop();
		DrawUtil.outlineRect(guiGraphics, rect, Colors.BLACK);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		var value = super.mouseClicked(mouseX, mouseY, button);
		if (button == 0) {
			Optional<KeystrokeHud.Keystroke> entry = Optional.empty();
			Optional<Rectangle> pos = Optional.empty();
			if (editing == null) {
				for (KeystrokeHud.Keystroke k : hud.keystrokes) {
					pos = Optional.of(getScaledRenderPos(k));
					if (pos.get().isMouseOver(mouseX, mouseY)) {
						entry = Optional.of(k);
						break;
					}
				}
			} else {
				pos = Optional.of(getScaledRenderPos(editing));
				if (pos.get().isMouseOver(mouseX, mouseY)) {
					entry = Optional.of(editing);
				}
			}
			if (entry.isPresent()) {
				focused = entry.get();
				mouseDown = true;
				var rect = pos.get();
				offset = new DrawPosition((int) Math.round(mouseX - rect.x()),
					(int) Math.round(mouseY - rect.y()));
				updateSnapState();
				return true;
			} else {
				focused = null;
			}
		} else if (button == 1 && editing == null) {
			Optional<KeystrokeHud.Keystroke> entry = Optional.empty();
			for (KeystrokeHud.Keystroke k : hud.keystrokes) {
				var pos = getScaledRenderPos(k);
				if (pos.isMouseOver(mouseX, mouseY)) {
					entry = Optional.of(k);
					break;
				}
			}
			entry.ifPresent(stroke -> client.setScreen(new ConfigureKeyBindScreen(this, hud, stroke, false)));
		}
		return value;
	}

	@Override
	public void closeScreen() {
		client.setScreen(parent);
		hud.saveKeystrokes();
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (focused != null) {
			hud.saveKeystrokes();
		}
		snap = null;
		mouseDown = false;
		focused = null;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (focused != null && mouseDown) {
			focused.setX((int) Math.round((mouseX - offset.x()) / hud.getScale()));
			focused.setY((int) Math.round((mouseY - offset.y()) / hud.getScale()));
			if (snap != null) {
				Optional<Integer> snapX, snapY;
				var rect = getScaledRenderPos(focused);
				snap.setCurrent(rect);
				if ((snapX = snap.getCurrentXSnap()).isPresent()) {
					focused.setX(Math.round(snapX.get() / hud.getScale()));
				}
				if ((snapY = snap.getCurrentYSnap()).isPresent()) {
					focused.setY(Math.round(snapY.get() / hud.getScale()));
				}
			}
			return true;
		}
		return false;
	}

	private Rectangle getScaledRenderPos(KeystrokeHud.Keystroke stroke) {
		return stroke.getRenderPosition().scale(hud.getScale());
	}

	private List<Rectangle> getAllBounds() {
		return Stream.concat(HudManager.getInstance().getAllBounds().stream(), hud.keystrokes.stream().map(this::getScaledRenderPos)).toList();
	}

	private void updateSnapState() {
		if (HudManager.getInstance().isSnappingEnabled() && focused != null) {
			snap = new SnappingHelper(getAllBounds(), getScaledRenderPos(focused));
		} else if (snap != null) {
			snap = null;
		}
	}

	@Override
	public void removed() {
		setCursor(DEFAULT_CURSOR);
	}
}

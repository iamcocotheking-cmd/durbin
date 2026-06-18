/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.AxolotlClientConfig;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.ConfigStyles;
import io.github.axolotlclient.bridge.impl.AxoRenderContextImpl;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import io.github.axolotlclient.modules.hud.gui.component.Positionable;
import io.github.axolotlclient.modules.hud.snapping.SnappingHelper;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.util.CursorType;
import io.github.axolotlclient.util.CursorTypes;
import io.github.axolotlclient.util.MathUtil;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.resource.language.I18n;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class HudEditScreen extends Screen {

	private final Screen parent;
	private HudEntry current;
	private DrawPosition offset = null;
	private boolean mouseDown;
	private SnappingHelper snap;
	private ModificationMode pendingMode = ModificationMode.NONE;
	private ModificationMode mode = ModificationMode.NONE;
	private int mouseX, mouseY;

	public HudEditScreen() {
		this(null);
	}

	public HudEditScreen(Screen parent) {
		super();
		updateSnapState();
		mouseDown = false;
		this.parent = parent;
	}

	private void updateSnapState() {
		if (HudManager.getInstance().isSnappingEnabled() && current != null) {
			var bounds = SnappingHelper.getNonDependentEntries(current, HudManager.getInstance().getMoveableEntries())
				.stream()
				.map(Positionable::getTrueBounds)
				.collect(Collectors.toCollection(ArrayList::new));
			bounds.remove(current.getTrueBounds());
			current.getDependenciesX().keySet().forEach(e -> bounds.remove(e.getTrueBounds()));
			current.getDependenciesY().keySet().forEach(e -> bounds.remove(e.getTrueBounds()));
			snap = new SnappingHelper(bounds, current.getTrueBounds());
		} else if (snap != null) {
			snap = null;
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		if (Minecraft.getInstance().world != null)
			fillGradient(0, 0, width, height, 0xB0100E0E, 0x46212020);
		else {
			renderBackground(0);
		}

		super.render(mouseX, mouseY, delta);
		GlStateManager.enableTexture();

		Optional<HudEntry> entry;
		if (current != null && mode != ModificationMode.NONE) {
			current.setHovered(true);
			entry = Optional.of(current);
		} else {
			entry = HudManager.getInstance().getEntryXY(mouseX, mouseY);
			entry.ifPresent(abstractHudEntry -> abstractHudEntry.setHovered(true));
		}
		var graphics = AxoRenderContextImpl.getInstance();
		if (mouseDown && snap != null && HudManagerCommon.getInstance().hudLinkCreationEnabled.get()) {
			snap.renderHighlights(graphics, current);
		}
		HudManager.getInstance().renderPlaceholder(graphics, delta);
		if (entry.isPresent()) {
			var bounds = entry.get().getTrueBounds();
			if (mode == ModificationMode.NONE && bounds.isMouseOver(mouseX, mouseY)) {
				var supportsScaling = entry.get().supportsScaling();
				var tolerance = HudManagerCommon.HUD_RESCALE_GRAB_TOLERANCE;
				var toleranceSquared = tolerance * tolerance;
				var pending = ModificationMode.MOVE;
				if (supportsScaling) {
					if (MathUtil.distSq(mouseX, mouseY, bounds.x(), bounds.y()) < toleranceSquared) {
						// top-left
						pending = ModificationMode.TOP_LEFT;
					} else if (MathUtil.distSq(mouseX, mouseY, bounds.xEnd(), bounds.yEnd()) < toleranceSquared) {
						// bottom-right
						pending = ModificationMode.BOTTOM_RIGHT;
					} else if (MathUtil.distSq(mouseX, mouseY, bounds.x(), bounds.yEnd()) < toleranceSquared) {
						// bottom-left
						pending = ModificationMode.BOTTOM_LEFT;
					} else if (MathUtil.distSq(mouseX, mouseY, bounds.xEnd(), bounds.y()) < toleranceSquared) {
						// top-right
						pending = ModificationMode.TOP_RIGHT;
					}
				}
				pending.type.select();
				this.pendingMode = pending;
			}
		} else if (current == null) {
			CursorType.DEFAULT.select();
			pendingMode = ModificationMode.NONE;
			mode = ModificationMode.NONE;
		}
		if (mouseDown && snap != null) {
			snap.renderSnaps(graphics);
		}
		// bweh
		if (mouseDown && (this.mouseX != mouseX || this.mouseY != mouseY)) {
			handleDrag(mouseX, mouseY);
		}
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		Optional<HudEntry> entry = HudManager.getInstance().getEntryXY(mouseX, mouseY);
		if (button == 0) {
			mouseDown = true;
			if (entry.isPresent()) {
				current = entry.get();
				offset = new DrawPosition(mouseX - current.getTruePos().x(),
					mouseY - current.getTruePos().y());
				if (pendingMode == ModificationMode.MOVE) {
					updateSnapState();
				}
				mode = pendingMode;
			} else {
				mode = ModificationMode.NONE;
				current = null;
			}
		} else if (button == 1) {
			entry.ifPresent(hudEntry -> {
				Screen screen = ConfigStyles.createScreen(this, hudEntry.getCategory());
				mode = ModificationMode.NONE;
				pendingMode = ModificationMode.NONE;
				CursorTypes.ARROW.select();
				Minecraft.getInstance().openScreen(screen);
			});
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int button) {
		if (current != null) {
			AxolotlClientConfig.getInstance().getConfigManager(current.getCategory()).save();
		}

		current = null;
		snap = null;
		mouseDown = false;
		mode = ModificationMode.NONE;
		pendingMode.type.select();
		super.mouseReleased(mouseX, mouseY, button);
	}

	private void handleDrag(int mouseX, int mouseY) {
		if (current != null && mouseDown) {
			current.clearBoundsDependencies();
			if (mode == ModificationMode.MOVE) {
				current.setPos(mouseX - offset.x() + current.offsetTrueWidth(), mouseY - offset.y() + current.offsetTrueHeight());
				if (snap != null) {
					Collection<HudEntry> entries = null;
					Optional<Integer> snapX = snap.getCurrentXSnap(), snapY = snap.getCurrentYSnap();
					if (snapX.isPresent() || snapY.isPresent()) {
						entries = HudManagerCommon.getInstance().getMoveableEntries();
						entries.remove(current);
						entries.removeIf(e -> e.dependsOnX(current).isPresent() || e.dependsOnY(current).isPresent());
					}
					snap.setCurrent(current.getTrueBounds());
					if (snapX.isPresent()) {
						current.setX(snapX.get() + current.offsetTrueWidth());
						if (HudManagerCommon.getInstance().hudLinkCreationEnabled.get()) {
							snap.getXTouching(entries, current).forEach(c -> {
								c.getLeft().removeBoundsDependencyX(current);
								current.addBoundsDependency(c.getLeft(), c.getRight());
							});
						}
					}
					if (snapY.isPresent()) {
						current.setY(snapY.get() + current.offsetTrueHeight());
						if (HudManagerCommon.getInstance().hudLinkCreationEnabled.get()) {
							snap.getYTouching(entries, current).forEach(c -> {
								c.getLeft().removeBoundsDependencyY(current);
								current.addBoundsDependency(c.getLeft(), c.getRight());
							});
						}
					}
					HudManagerCommon.getInstance().saveHudDependencyLinks();
				}
			} else {
				var bounds = current.getTrueBounds();
				int newWidth, newHeight;
				if (mode == ModificationMode.TOP_LEFT) {
					// top-left corner
					newWidth = mouseX - bounds.xEnd();
					newHeight = mouseY - bounds.yEnd();
				} else if (mode == ModificationMode.BOTTOM_LEFT) {
					// bottom-left corner
					newWidth = mouseX - bounds.xEnd();
					newHeight = mouseY - bounds.y();
				} else if (mode == ModificationMode.TOP_RIGHT) {
					// top-right corner
					newWidth = mouseX - bounds.x();
					newHeight = mouseY - bounds.yEnd();
				} else if (mode == ModificationMode.BOTTOM_RIGHT) {
					// bottom-right corner
					newWidth = mouseX - bounds.x();
					newHeight = mouseY - bounds.y();
				} else {
					newWidth = bounds.width();
					newHeight = bounds.height();
				}
				float newScale = current.getScale() * Math.max((float) Math.abs(newWidth) / bounds.width(), (float) Math.abs(newHeight) / bounds.height());
				current.setScale(Math.max(0.1f, newScale));
				if (mode == ModificationMode.TOP_LEFT) {
					// top-left corner
					current.setPos(bounds.xEnd() - current.getTrueWidth(), bounds.yEnd() - current.getTrueHeight());
				} else if (mode == ModificationMode.BOTTOM_LEFT) {
					// bottom-left corner
					current.setX(bounds.xEnd() - current.getTrueWidth());
				} else if (mode == ModificationMode.TOP_RIGHT) {
					// top-right corner
					current.setY(bounds.yEnd() - current.getTrueHeight());
				}
			}
			if (current.tickable()) {
				current.tick();
			}
		}
	}

	@Override
	public void removed() {
		super.removed();
		pendingMode = ModificationMode.NONE;
		CursorTypes.ARROW.select();
		mode = ModificationMode.NONE;
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		switch (button.id) {
			case 3:
				HudManager.getInstance().toggleSnapping();
				button.message = I18n.translate("hud.snapping") + ": "
					+ I18n.translate(HudManager.getInstance().isSnappingEnabled() ? "options.on" : "options.off");
				break;
			case 1:
				Screen screen = ConfigStyles.createScreen(this, AxolotlClient.getInstance().getConfigManager().getRoot());
				Minecraft.getInstance().openScreen(screen);
				break;
			case 0:
				Minecraft.getInstance().openScreen(parent);
				break;
			case 2:
				Minecraft.getInstance().openScreen(null);
				break;
		}
	}

	@Override
	public void init() {
		mode = ModificationMode.NONE;
		this.buttons.add(new ButtonWidget(3, width / 2 - 50, height / 2 + 12, 100, 20,
			I18n.translate("hud.snapping") + ": " + I18n.translate(HudManager.getInstance().isSnappingEnabled() ? "options.on" : "options.off")));

		this.buttons.add(
			new ButtonWidget(1, width / 2 - 75, height / 2 - 10, 150, 20, I18n.translate("hud.clientOptions")));

		if (parent != null)
			buttons.add(new ButtonWidget(0, width / 2 - 75, height - 50 + 22, 150, 20, I18n.translate("back")));
		else
			buttons.add(new ButtonWidget(2, width / 2 - 75, height - 50 + 22, 150, 20, I18n.translate("close")));
	}

	@RequiredArgsConstructor
	private enum ModificationMode {
		NONE(CursorType.DEFAULT),
		MOVE(CursorTypes.RESIZE_ALL),
		TOP_LEFT(CursorTypes.RESIZE_NWSE),
		TOP_RIGHT(CursorTypes.RESIZE_NESW),
		BOTTOM_LEFT(CursorTypes.RESIZE_NESW),
		BOTTOM_RIGHT(CursorTypes.RESIZE_NWSE);
		private final CursorType type;
	}
}

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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.Comparator;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.ClientColors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class ScoreboardHud extends TextHudEntry {

	public static final Identifier ID = Identifier.of("kronhud", "scoreboardhud");
	public static final ScoreboardObjective placeholder = Util.make(() -> {
		Scoreboard placeScore = new Scoreboard();
		ScoreboardObjective objective = placeScore.addObjective("placeholder", ScoreboardCriterion.DUMMY,
			Text.literal("Scoreboard"), ScoreboardCriterion.RenderType.INTEGER, false, StyledNumberFormat.RED);
		ScoreAccess dark = placeScore.getOrCreateScore(ScoreHolder.of("DarkKronicle"), objective);
		dark.setScore(8780);

		ScoreAccess moeh = placeScore.getOrCreateScore(ScoreHolder.of("moehreag"), objective);
		moeh.setScore(743);

		ScoreAccess kode = placeScore.getOrCreateScore(ScoreHolder.of("TheKodeToad"), objective);
		kode.setScore(2948);

		placeScore.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, objective);
		return objective;
	});

	private final ColorOption backgroundColor = new ColorOption("backgroundcolor", new Color(0x4C000000));
	private final ColorOption topColor = new ColorOption("topbackgroundcolor", new Color(0x66000000));
	private final IntegerOption topPadding = new IntegerOption("toppadding", 0, 0, 4);
	private final BooleanOption scores = new BooleanOption("scores", true);
	private final ColorOption scoreColor = new ColorOption("scorecolor", new Color(0xFFFF5555));
	private final IntegerOption textAlpha = new IntegerOption("text_alpha", 255, 0, 255);
	private final MinecraftClient client = (MinecraftClient) super.client;

	public ScoreboardHud() {
		super(200, 146, true);
	}

	@Override
	public void render(AxoRenderContext context, float delta) {
		final var graphics = (GuiGraphics) context;
		graphics.getMatrices().push();
		scale(graphics);
		renderComponent(graphics, delta);
		graphics.getMatrices().pop();
	}

	@Override
	public void renderComponent(AxoRenderContext context, float delta) {
		final var graphics = (GuiGraphics) context;
		//noinspection DataFlowIssue
		Scoreboard scoreboard = this.client.world.getScoreboard();
		ScoreboardObjective scoreboardObjective = null;
		//noinspection DataFlowIssue
		Team team = scoreboard.getPlayerTeam(this.client.player.getProfileName());
		if (team != null) {
			int t = team.getColor().getColorIndex();
			if (t >= 0) {
				scoreboardObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.BY_ID.apply(3 + t));
			}
		}

		ScoreboardObjective scoreboardObjective2 = scoreboardObjective != null ? scoreboardObjective
			: scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
		if (scoreboardObjective2 != null) {
			this.renderScoreboardSidebar(graphics, scoreboardObjective2, false);
		}
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext graphics, float delta) {
		renderScoreboardSidebar((GuiGraphics) graphics, placeholder, true);
	}

	// Abusing this could break some stuff/could allow for unfair advantages. The goal is not to do this, so it won't
	// show any more information than it would have in vanilla.
	private void renderScoreboardSidebar(GuiGraphics graphics, ScoreboardObjective objective, boolean placeholder) {
		var font = client.textRenderer;
		var scoreboard = objective.getScoreboard();
		var numberFormat = objective.getNumberFormatOverrideOrElse(StyledNumberFormat.RED);

		@Environment(EnvType.CLIENT)
		record DisplayEntry(Text name, Text score, int scoreWidth) {
		}

		DisplayEntry[] entries = scoreboard.getEntriesForObjective(objective).stream()
			.filter(entry -> !entry.isHidden())
			.sorted(Comparator.comparing(ScoreboardEntry::value).reversed()
				.thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER))
			.limit(15L)
			.map(entry -> {
				var team = scoreboard.getPlayerTeam(entry.owner());
				var value = entry.getNumber(numberFormat);
				return new DisplayEntry(Team.decorateName(team, entry.getDisplay()), value, font.getWidth(value));
			}).toArray(DisplayEntry[]::new);
		var title = objective.getDisplayName();
		int titleWidth = font.getWidth(title) + 2;
		int maxWidth = titleWidth;
		int textOffset = font.getWidth(": ");

		for (DisplayEntry lv : entries) {
			maxWidth = Math.max(maxWidth, font.getWidth(lv.name) + (lv.scoreWidth > 0 && scores.get() ?
				textOffset + lv.scoreWidth : 0));
		}

		maxWidth += 3;
		int entryCount = entries.length;
		int mainHeight = entryCount * font.fontHeight;

		int newHeight = mainHeight + 10 + topPadding.get() * 2;

		boolean updated = false;
		if (newHeight + 1 != getContentHeight()) {
			setContentHeight(newHeight + 1);
			updated = true;
		}
		if (maxWidth + 1 != getContentWidth()) {
			setContentWidth(maxWidth + 1);
			updated = true;
		}
		if (updated) {
			onBoundsUpdate();
		}

		Rectangle bounds = getContentBounds();

		int yEnd = bounds.y() + bounds.height();
		int textX = bounds.x() + 2;
		int xEnd = bounds.x() + bounds.width() - 1;
		int titleEnd = yEnd - mainHeight;
		var bgBounds = getBounds();
		var maxRounding = Math.min(Math.min(font.fontHeight + topPadding.get() * 2 + backgroundPadding.get(), titleEnd - 1 - bgBounds.y()), xEnd - textX - 3) / 2f;
		float rounding = Math.min(maxRounding, backgroundRounding.get());
		if (!placeholder) {
			if (background.get()) {
				if (roundBackground.get()) {
					graphics.axolotlclient_rendering$roundedRectVarying(bgBounds.x(), bgBounds.y(), bgBounds.xEnd(), titleEnd - 1,
						topColor.get().toInt(), rounding, 0, 0, rounding);
					graphics.axolotlclient_rendering$roundedRectVarying(bgBounds.x(), titleEnd - 1, bgBounds.xEnd(), bgBounds.yEnd(),
						backgroundColor.get().toInt(), 0, rounding, rounding, 0);
				} else {
					graphics.fill(bgBounds.x(), bgBounds.y(), bgBounds.xEnd(), titleEnd - 1, topColor.get().toInt());
					graphics.fill(bgBounds.x(), titleEnd - 1, bgBounds.xEnd(), bgBounds.yEnd(), backgroundColor.get().toInt());
				}
			}
		} else {
			graphics.br$fillRect(bgBounds.x()+1, bgBounds.y()+1, bgBounds.width()-2, titleEnd - 1 - bgBounds.y()-1, ClientColors.DARK_GRAY.withAlpha(100));
		}
		graphics.drawText(font, title, textX + maxWidth / 2 - titleWidth / 2, titleEnd - font.fontHeight - topPadding.get(),
			ClientColors.ARGB.color(textAlpha.get(), -1), shadow.get());

		for (int v = 0; v < entryCount; v++) {
			DisplayEntry entry = entries[v];
			int y = yEnd - (entryCount - v) * font.fontHeight;
			graphics.drawText(font, entry.name, textX, y, ClientColors.ARGB.color(textAlpha.get(), -1), shadow.get());
			if (scores.get()) {
				graphics.drawText(font, entry.score, xEnd - entry.scoreWidth, y, scoreColor.get().toInt(),
					shadow.get());
			}
		}

		if (!placeholder) {
			if (outline.get() && outlineColor.get().getAlpha() > 0) {
				if (roundBackground.get()) {
					graphics.axolotlclient_rendering$outlineRoundedRect(bgBounds.x(), bgBounds.y(), bgBounds.xEnd(), bgBounds.yEnd(), outlineColor.get().toInt(), rounding, 0.5f);
				} else {
					DrawUtil.outlineRect(graphics, bgBounds, outlineColor.get());
				}
			}
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.set(options.indexOf(super.backgroundColor), backgroundColor);
		options.add(hide);
		options.add(topColor);
		options.add(scores);
		options.add(scoreColor);
		options.add(topPadding);
		options.remove(textColor);
		options.add(textAlpha);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	protected AnchorPoint getDefaultAnchor() {
		return AnchorPoint.MIDDLE_RIGHT;
	}

	@Override
	public double getDefaultX() {
		return 1.0;
	}

	@Override
	public double getDefaultY() {
		return 0.5;
	}
}

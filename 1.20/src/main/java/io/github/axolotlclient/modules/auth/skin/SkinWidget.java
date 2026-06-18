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

package io.github.axolotlclient.modules.auth.skin;

import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.modules.auth.Account;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.auth.MSApi;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ElementPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.GuiNavigationEvent;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.CommonTexts;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class SkinWidget extends ClickableWidget {
	private static final float MODEL_HEIGHT = 2.125F;
	private static final float FIT_SCALE = 0.97F;
	private static final float ROTATION_SENSITIVITY = 2.5F;
	private static final float DEFAULT_ROTATION_X = -5.0F;
	private static final float DEFAULT_ROTATION_Y = 30.0F;
	private static final float ROTATION_X_LIMIT = 50.0F;
	private float rotationX = DEFAULT_ROTATION_X;
	@Setter
	private float rotationY = DEFAULT_ROTATION_Y;
	@Getter
	@Setter
	private Skin skin;
	@Getter
	@Setter
	private Cape cape;
	private final Account owner;
	private boolean noCape, noCapeActive;

	public SkinWidget(int width, int height, Skin skin, @Nullable Cape cape, Account owner) {
		super(0, 0, width, height, CommonTexts.EMPTY);
		this.skin = skin;
		this.cape = cape;
		this.owner = owner;
	}

	public SkinWidget(int width, int height, Skin skin, Account owner) {
		this(width, height, skin, null, owner);
	}

	public void noCape(boolean noCapeActive) {
		noCape = true;
		this.noCapeActive = noCapeActive;
	}

	@Override
	protected void drawWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		float scale = FIT_SCALE * this.getHeight() / MODEL_HEIGHT;
		float pivotY = -1.0625F;

		SkinManager skinManager = Auth.getInstance().getSkinManager();
		AxoIdentifier skinRl = skinManager.loadSkin(skin);
		boolean classic = skin.classicVariant();
		var capeRl = cape == null ? null : skinManager.loadCape(cape);

		SkinRenderer.render(guiGraphics, classic, (Identifier) skinRl, (Identifier) capeRl, this.rotationX, this.rotationY, pivotY, this.getX(), this.getY(), this.getX() + getWidth(), this.getY() + getHeight(), scale);
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
		this.rotationX = MathHelper.clamp(this.rotationX - (float) dragY * ROTATION_SENSITIVITY, -ROTATION_X_LIMIT, ROTATION_X_LIMIT);
		this.rotationY += (float) dragX * ROTATION_SENSITIVITY;
	}

	@Override
	public void playDownSound(SoundManager handler) {
	}

	@Override
	protected void updateNarration(NarrationMessageBuilder builder) {

	}

	@Override
	public @Nullable ElementPath nextFocusPath(GuiNavigationEvent event) {
		return null;
	}

	public boolean isEquipped() {
		return noCape ? noCapeActive : (cape != null ? cape.active() : skin != null && skin.active());
	}

	public CompletableFuture<MSApi.MCProfile> equip() {
		var msApi = Auth.getInstance().getMsApi();
		if (noCape) {
			return msApi.hideCape(owner);
		}
		if (cape != null) {
			return cape.equip(msApi, owner);
		}
		if (skin != null) {
			return skin.equip(msApi, owner);
		}
		return msApi.resetSkin(owner);
	}

	public Asset getFocusedAsset() {
		return noCape ? null : cape != null ? cape : skin;
	}
}

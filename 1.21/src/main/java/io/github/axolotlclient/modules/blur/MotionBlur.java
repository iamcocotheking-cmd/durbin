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

package io.github.axolotlclient.modules.blur;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.FloatOption;
import io.github.axolotlclient.modules.AbstractModule;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

public class MotionBlur extends AbstractModule {

	@Getter
	private static final MotionBlur Instance = new MotionBlur();
	public final BooleanOption enabled = new BooleanOption("enabled", false);
	public final FloatOption strength = new FloatOption("strength", 50F, 1F, 99F);
	public final BooleanOption inGuis = new BooleanOption("inGuis", false);
	public final OptionCategory category = OptionCategory.create("motionBlur");
	private final Identifier shaderLocation = Identifier.parse("minecraft:shaders/post/motion_blur.json");
	private final MinecraftClient client = MinecraftClient.getInstance();
	public ShaderEffect shader;

	private static float getBlur() {
		return MotionBlur.getInstance().strength.get() / 100F;
	}

	@Override
	public void init() {
		category.add(enabled, strength, inGuis);

		AxolotlClient.config().rendering.add(category);
		AxolotlClient.runtimeResources.put(shaderLocation, new MotionBlurShader());
	}

	public void load() {
		if (shader != null) {
			shader.close();
		}

		try {
			shader = new ShaderEffect(client.getTextureManager(), client.getResourceManager(),
				client.getFramebuffer(), shaderLocation);
			shader.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
		} catch (JsonSyntaxException | IOException e) {
			AxolotlClientCommon.getInstance().getLogger().error("Could not load motion blur: ", e);
		}
	}

	public void onUpdate(float delta) {
		if (shader != null) {
			shader.setUniform("BlendFactor", getBlur());
			shader.render(delta);
		}
	}

	private static class MotionBlurShader extends Resource {

		public MotionBlurShader() {
			super(MinecraftClient.getInstance().getDefaultResourcePack(), () -> IOUtils.toInputStream("""
				{
					"targets": [
						"swap",
						"previous"
					],
					"passes": [
						{
							"name": "motion_blur",
							"intarget": "minecraft:main",
							"outtarget": "swap",
							"auxtargets": [
								{
									"name": "PrevSampler",
									"id":"previous"
								}
							],
							"uniforms": [
								{
									"name": "BlendFactor",
									"values": [ 0.3 ]
								}
							]
						},
						{
							"name": "blit",
							"intarget": "swap",
							"outtarget": "previous"
						},
						{
							"name": "blit",
							"intarget": "swap",
							"outtarget": "minecraft:main"
						}
					]
				}
				""",
				"utf-8"));
		}
	}
}

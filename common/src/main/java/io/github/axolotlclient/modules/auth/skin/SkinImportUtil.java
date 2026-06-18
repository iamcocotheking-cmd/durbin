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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class SkinImportUtil {
	public static CompletableFuture<List<Path>> openImportSkinDialog() {
		return CompletableFuture.supplyAsync(() -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				var pointers = stack.pointers(stack.UTF8("*.png"));
				@SuppressWarnings("DataFlowIssue") var result = TinyFileDialogs.tinyfd_openFileDialog("Import Skins",
					FabricLoader.getInstance().getGameDir().toString(), pointers, null, true);
				if (result != null) {
					return Arrays.stream(result.split("\\|"))
						.map(Path::of).toList();
				}
				return List.of();
			}
		});
	}
}

Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors

This file is part of AxolotlClient.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

For more information, see the LICENSE file.

#year_selection file

package io.github.axolotlclient.durbin;

import net.minecraft.client.Minecraft;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class DurbinPanoramaPackInstaller {
    private static final String PACK_FILE = "Durbin_Panorama_Pack.zip";
    private static final String PACK_ID = "file/" + PACK_FILE;
    private static boolean installed;

    private DurbinPanoramaPackInstaller() {
    }

    public static void install() {
        if (installed) {
            return;
        }
        installed = true;

        try {
            Minecraft minecraft = Minecraft.getInstance();
            Path gameDir = minecraft.gameDirectory.toPath();
            Path packDir = gameDir.resolve("resourcepacks");
            Files.createDirectories(packDir);

            Path target = packDir.resolve(PACK_FILE);
            try (InputStream in = DurbinPanoramaPackInstaller.class.getResourceAsStream("/assets/axolotlclient/resourcepacks/" + PACK_FILE)) {
                if (in != null) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            enablePackInOptions(gameDir.resolve("options.txt"));
        } catch (Throwable ignored) {
        }
    }

    private static void enablePackInOptions(Path options) {
        try {
            List<String> lines = Files.exists(options)
                    ? Files.readAllLines(options, StandardCharsets.UTF_8)
                    : new ArrayList<>();

            boolean foundResourcePacks = false;
            boolean foundIncompatible = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line.startsWith("resourcePacks:")) {
                    foundResourcePacks = true;
                    if (!line.contains(PACK_ID)) {
                        lines.set(i, addPackToListLine("resourcePacks:", line, PACK_ID));
                    }
                } else if (line.startsWith("incompatibleResourcePacks:")) {
                    foundIncompatible = true;
                    if (!line.contains(PACK_ID)) {
                        lines.set(i, addPackToListLine("incompatibleResourcePacks:", line, PACK_ID));
                    }
                }
            }

            if (!foundResourcePacks) {
                lines.add("resourcePacks:[\"vanilla\",\"" + PACK_ID + "\"]");
            }
            if (!foundIncompatible) {
                lines.add("incompatibleResourcePacks:[\"" + PACK_ID + "\"]");
            }

            Files.write(options, lines, StandardCharsets.UTF_8);
        } catch (Throwable ignored) {
        }
    }

    private static String addPackToListLine(String key, String line, String packId) {
        String value = line.substring(key.length()).trim();
        if (value.length() < 2 || !value.startsWith("[") || !value.endsWith("]")) {
            return key + "[\"vanilla\",\"" + packId + "\"]";
        }

        String inside = value.substring(1, value.length() - 1).trim();
        if (inside.isEmpty()) {
            return key + "[\"" + packId + "\"]";
        }

        return key + "[" + inside + ",\"" + packId + "\"]";
    }
}

/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.util;

import org.lwjgl.glfw.GLFW;

@SuppressWarnings("unused")
public final class CursorTypes {
	public static final CursorType ARROW = CursorType.createStandardCursor(GLFW.GLFW_ARROW_CURSOR, "arrow", CursorType.DEFAULT);
	public static final CursorType IBEAM = CursorType.createStandardCursor(GLFW.GLFW_IBEAM_CURSOR, "ibeam", CursorType.DEFAULT);
	public static final CursorType CROSSHAIR = CursorType.createStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR, "crosshair", CursorType.DEFAULT);
	public static final CursorType POINTING_HAND = CursorType.createStandardCursor(GLFW.GLFW_POINTING_HAND_CURSOR, "pointing_hand", CursorType.DEFAULT);
	public static final CursorType RESIZE_NS = CursorType.createStandardCursor(GLFW.GLFW_RESIZE_NS_CURSOR, "resize_ns", CursorType.DEFAULT);
	public static final CursorType RESIZE_EW = CursorType.createStandardCursor(GLFW.GLFW_RESIZE_EW_CURSOR, "resize_ew", CursorType.DEFAULT);
	public static final CursorType RESIZE_ALL = CursorType.createStandardCursor(GLFW.GLFW_RESIZE_ALL_CURSOR, "resize_all", CursorType.DEFAULT);
	public static final CursorType NOT_ALLOWED = CursorType.createStandardCursor(GLFW.GLFW_NOT_ALLOWED_CURSOR, "not_allowed", CursorType.DEFAULT);
	public static final CursorType RESIZE_NWSE = CursorType.createStandardCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR, "resize_nwse", POINTING_HAND);
	public static final CursorType RESIZE_NESW = CursorType.createStandardCursor(GLFW.GLFW_RESIZE_NESW_CURSOR, "resize_nesw", POINTING_HAND);
}

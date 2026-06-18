# Durbin Client

Durbin Client is a legal AxolotlClient LGPL-3.0 fork with Durbin branding and a custom simple black transparent UI.

## What this version does

- Keeps AxolotlClient's real HUD system and HUD editor.
- Replaces the pause-menu badge button with a Durbin GUI.
- Durbin GUI has: Mods, HUD Editor, Profiles, About.
- Mods tab toggles actual Axolotl HUD entries from `HudManager`, not fake Durbin-only modules.
- HUD Editor tab opens AxolotlClient's real `HudEditScreen`.

## Credits

This project includes AxolotlClient code by moehreag and contributors under LGPL-3.0.
Do not remove LICENSE or THIRD_PARTY_NOTICES.md.

## Archivo Black font

Durbin GUI text is wired to use `durbinclient:archivo_black`.
Before building, add your own font file:

`1.21.11/src/main/resources/assets/durbinclient/font/archivo_black.ttf`

The generated zip does not include the TTF font file.


## Durbin fix
- Right Shift now opens Durbin custom GUI instead of Axolotl HUD editor.
- UI theme is black transparent, not white.
- Custom Archivo font hook removed because missing TTF makes Minecraft render square boxes. Default Minecraft font restored for readable text.

DURBIN CLIENT - FIREBASE NAMETAGS

What was added:
- Right Shift GUI now has a Nametags tab.
- Client downloads nametags from Firebase Realtime Database using the REST JSON URL.
- Client saves local cache at: .minecraft/config/durbin-nametags-cache.json
- If the player is offline, the mod keeps using the local cache.

Setup:
1. Run Minecraft once with the mod.
2. Open .minecraft/config/durbin-nametags.properties
3. Paste your Firebase Realtime Database URL:
   firebase_url=https://YOUR_PROJECT_ID-default-rtdb.firebaseio.com/nametags.json
4. Press Right Shift > Nametags > Sync Now.

Example Firebase JSON:

{
  "COSA": [
    { "text": "Owner Line", "color": "#4D7CFF" },
    { "text": "Durbin Line", "color": "#33DD66" },
    { "text": "[PORTALBD]", "color": "#FF3333" }
  ],
  "TrenchTheKid": {
    "enabled": true,
    "lines": [
      { "text": "Another Line 2", "color": "#5578FF" },
      { "text": "Another Line 1", "color": "#44DD55" },
      { "text": "[StarMode]", "color": "#DDDDDD" }
    ]
  }
}

Notes:
- Usernames are matched case-insensitive.
- Max 6 custom lines per player.
- You can use Minecraft color codes too, like "&cRed Text".
- Client-side only: other players do not need the mod to see their tag on your screen.

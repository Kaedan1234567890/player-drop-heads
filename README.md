# Chill Zone Player Heads 26.3 — Alpha 1 (source only)

This is a deliberately small **server-side** Minecraft 26.3 Fabric adaptation of the MIT CustomPlayerHeads project uploaded by the user. It is **not** an unmodified or fully equivalent port of the original mod. Original inspiration/source: https://github.com/onebeastchris/customplayerheads

## What this test source implements
- On **player-versus-player death**, drop one vanilla head of the victim.
- Head name: `<player>'s Head`.
- Item lore: killer Minecraft username; X/Y/Z death coordinates; dimension ID; date/time in America/Toronto.
- Uses the victim's original game profile for skin; requires a valid resolved profile for full skin fidelity.
- No client mod required, no datapack, no new permissions.

## Not included in this initial port
- Original `/getskull` and Bedrock texture lookup commands; original HOCON settings.
- Non-player deaths; configurable drop rates; duplicate-drop integration with other mods.
- Guaranteed Minecraft 26.3 compilation: this source has NOT been compiled in this environment; test GitHub build before deploying.

## How to build
Create a GitHub repository containing the **contents** of this directory (not an extra enclosing folder). Actions > Build Chill Zone Player Heads 26.3 > Run workflow. Download the artifact produced in a successful run. Install ONLY the normal compiled .jar, not the sources jar, in your server's mods directory. Stop server and back up first. Do not use another head-dropping mod simultaneously.

## Developer note
The game uses official Mojang mappings in 26.3, not the old Yarn 1.20.6 mappings. If the GitHub build fails, use the full error output to adapt a specific API call before testing in-game. Do not install source ZIP in mods.

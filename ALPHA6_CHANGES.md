# Player Heads Alpha 6 — delayed death drop

- PvP victim head is now queued for 15 server ticks (0.75 seconds at 20 TPS) after the death callback.
- Spawned head has an extra 20-tick pickup delay; the dead Carpet fake player should no longer grab its own dropped head.
- Preserves the exact victim profile, killer, Overworld/Nether/End, date and nonitalic lore. No coordinates.
- **Source not compiled or tested in Minecraft; evaluate on a private server before replacing the live JAR.**

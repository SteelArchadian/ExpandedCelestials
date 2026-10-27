<p align="center"><img src=".github/assets/banner.png" alt="ExpandedCelestials" width="100%"></p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/Loader-NeoForge-F0323F?style=flat-square" alt="NeoForge">
  <img src="https://img.shields.io/badge/Side-Client%20%26%20server-4C8BF5?style=flat-square" alt="Side">
  <img src="https://img.shields.io/badge/License-LGPL--3.0--only-555555?style=flat-square" alt="LGPL-3.0-only">
</p>

**Expanded Celestials** adds 19 new moon events to **[Enhanced Celestials 2](https://modrinth.com/mod/enhanced-celestials-2-core)** by Corgi Taco, for Minecraft 1.21.1 with NeoForge.

> **Requires** the official **Enhanced Celestials 2: Core** (2.0.3.3 or newer) and its libraries **CorgiLib** and **Data Anchor**. This add-on contains none of Enhanced Celestials' code or files, and isn't made or endorsed by Corgi Taco.

## The moons

| Moon | What happens |
|---|---|
| Big Moon | Lower gravity: jumps go higher, items and mobs fall slower |
| Tiny Moon | Higher gravity: everything falls heavier |
| Miner Moon | Haste I for faster mining |
| Prospector's Moon | Mining ore gives extra XP and sometimes extra drops |
| Blood Moon | More hostile mobs spawn |
| Eclipse | Nether creatures replace the usual night monsters |
| Pink Moon | No new hostile mobs spawn |
| Pillager Moon | Pillagers and illagers replace the usual night monsters |
| Ender Moon | Endermen and endermites take over, and the night feels like the End |
| Harvest Moon | Crops grow faster and can give extra produce |
| Blue Moon | Luck +2 for better loot |
| Green Moon | Slimes spawn in more places |
| Tidal Moon | Fish bite sooner and there are more fish |
| Hunter's Moon | Wildlife spawns at night in bigger groups |
| Howling Moon | Wolves are more common |
| Shroom Moon | Mushrooms and fungi spread faster |
| Storm Moon | Thunderstorms with extra lightning that can't set blocks on fire |
| Meteor Shower Moon | Rare stone and raw ore fragments land on grass and snow |
| Wanderer's Moon | Traders show up more often and may sell two extra goods |

## Commands

`/ecplus status`, `/ecplus test <moon>`, `/ecplus reload`, `/ecplus refreshForecast`. Singleplayer owners can use them without cheats; on servers they need operator permission.

## Settings

- `config/expandedcelestials-common.toml`: `extraMoons`, `moonsInRain`, `quietJoinMessages` (all on by default).
- `config/expandedcelestials.json`: tuning for each moon.

## Bugs and ideas

Please post them in **[Issues](https://github.com/SteelArchadian/ExpandedCelestials/issues)**. That's where every report gets read and tracked, rather than in CurseForge or Modrinth comments.

## Building

Put the jars listed in `libs/README.txt` into `libs/` (Enhanced Celestials 2 Core and its libraries; not included here), then:

```
./gradlew build
```

## Credits and license

Enhanced Celestials 2 is by Corgi Taco (All Rights Reserved) and is not included in this repository or the mod.

Licensed under the GNU Lesser General Public License v3.0 only: see [LICENSE](LICENSE) (with the GPL text it builds on in [COPYING](COPYING)). Made for the Foundation modpack, with help from Claude.

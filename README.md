# Block Randomizer × Accurate Block Placement

A lightweight, client-side **Fabric 1.20.1** mod that randomly selects block items from your hotbar after each successful main-hand placement.

**Accurate Block Placement is optional.** Without it, randomization works at Minecraft's normal placement speed. With Accurate Block Placement **1.2.1**, automatic slot changes update its held-item tracking so you can keep holding right-click while building quickly.

## Download and install

Download [Hotbar Randomizer 1.0.1 for Fabric 1.20.1](downloads/hotbar-randomizer-1.0.1+mc1.20.1.jar) using GitHub's download button.

1. Install Fabric Loader, [Fabric API](https://modrinth.com/mod/fabric-api), and [Cloth Config 11](https://modrinth.com/mod/cloth-config) for Minecraft 1.20.1.
2. Close Minecraft and copy the JAR into your instance's `mods` folder.
3. Launch Minecraft and press **P** to toggle randomization. Rebind it in Controls → Key Binds → Hotbar Randomizer.

[Mod Menu](https://modrinth.com/mod/modmenu) is optional and provides the settings screen. Install [Accurate Block Placement Reborn 1.2.1](https://modrinth.com/mod/accurate-block-placement-reborn/version/1.2.1) if you want its faster held-right-click placement. No server installation is needed.

## Behavior

- Only hotbar slots 1–9 are considered. Tools, weapons, buckets, empty slots, and other non-block items are excluded.
- All block categories are included initially, including torches, flowers, sand and containers.
- Enabling immediately selects an eligible block. Each successful main-hand placement selects the next slot.
- Failed placements and opening containers do not randomize. Offhand placement does not change your hotbar.
- Starts OFF each session and switches off when you disconnect or run out of eligible blocks.
- Equal probability per eligible hotbar slot, regardless of stack size. Duplicate stacks add weight to their block type.
- By default, avoids repeating the previous block type when another eligible type exists. Disable this option for unrestricted randomness.

## Settings

Mods → Hotbar Randomizer → Configure. Toggle categories for ordinary blocks; plants/flowers/crops; torches; falling blocks; block entities such as containers and machines; slabs/stairs/fences/walls/panes; doors/trapdoors; and leaves. Add specific excluded item IDs, such as `minecraft:tnt`.

Blocks matching multiple categories must pass all of them. Modded blocks are classified by their base classes, with unmatched blocks in Ordinary / other blocks. Settings are saved to `config/hotbar-randomizer.json`; the active on/off state is session-only.

## Build from source

Requires a **JDK 17 or 21** and an internet connection for the first build. The Gradle wrapper downloads the build tools and dependencies automatically.

```sh
./gradlew build
```

Windows:

```powershell
.\gradlew.bat build
```

The installable JAR appears in `build/libs/hotbar-randomizer-1.0.1+mc1.20.1.jar`. Do not install the `-sources.jar` file. `build` runs the selection tests. GitHub Actions also builds and uploads artifacts for pushes and pull requests.

## Compatibility and validation

Targets Minecraft 1.20.1, Java 17+, Fabric, Cloth Config 11, and optional Mod Menu 7. Accurate Block Placement integration targets version 1.2.1; its internal field may differ in other versions.

Automatic switches happen after the placement packet is sent, allowing the next interaction to synchronize the selected slot. No extra placement cooldown is introduced. Existing placement rules still apply: flowers need suitable ground, torches need support, and some block types do not support ABP's fast placement. Failed placement keeps the current block selected. Client prediction cannot immediately detect a server rejecting placement.

The standalone Gradle build verifies that only declared mixin classes live in the reserved mixin package and that entrypoints remain outside it. It passes, including 42,003 selections covering empty and depleted slots, single blocks, duplicate types, avoiding repeats, and probability distribution. Java 17 class versions, generated Fabric mixin mappings, placement target signatures and ABP's tracking field were checked. **Live gameplay testing remains necessary**, both with and without ABP, including sustained right-click, creative/survival, depleted stacks and category exclusions.

MIT licensed. Local instance-specific references and backups are kept outside the published source under ignored `local-reference/` when present.

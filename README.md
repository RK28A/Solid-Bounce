# Solid-Bounce

A **Forge 1.20.1** port of [LiquidBounce](https://github.com/CCBlueX/LiquidBounce) (nextgen).

> Solid-Bounce is a Minecraft utility/"hacked" client, ported from the Fabric-based LiquidBounce
> nextgen codebase to the **Minecraft Forge** mod loader on **Minecraft 1.20.1**.

---

## Status

This is an **in-progress port**, built up feature by feature. It is **not** an automated 1:1
translation of the whole LiquidBounce source — that is impossible, because upstream LiquidBounce
targets a much newer Minecraft (26.x) on Fabric. Instead the port:

1. Uses **LiquidBounce nextgen `v0.1.0`** (MC 1.20.4, the oldest/cleanest nextgen release) as the
   architecture and feature reference.
2. Re-implements everything against **Forge 1.20.1** with the **Kotlin For Forge** language adapter
   and **Mojang official mappings**.
3. Re-wires every event from Fabric hooks/mixins onto the **Forge event bus** (`ForgeEventBridge`).
4. Re-adds later LiquidBounce modules that are compatible with 1.20.1 as the port progresses.

### What already works (foundation — session 1)

- Core architecture ported from upstream (loader-agnostic):
  - Event system: `Event`, `Listenable`, `EventManager`, coroutine `Sequence`s.
  - Module system: `Module`, `ModuleManager`, `Category`.
  - Config/value system: `Configurable`, `Value`, `RangedValue`, `ChoiceConfigurable` — same
    module-facing declaration style as upstream (`boolean(...)`, `int(...)`, `.listen { }`, `by`).
- **Forge event bridge** dispatching: client tick, world render, GUI overlay render, key & mouse
  input, attack, chat send, world (dis)connect, and living-fall.
- Chat command handler (prefix `.`): `.t <module>`, `.bind <module> <key>`, `.list`, `.panic`.
- HUD: client watermark + top-right module ArrayList.
- **All 156 modules** of the LiquidBounce v0.1.0 set are present, registered, toggleable and
  listed (8 categories: Combat, Movement, Player, Render, World, Exploit, Misc, Fun).
  - Fully wired now (Forge events / packets / rotations): AutoClicker, AutoWeapon,
    SuperKnockback, KillAura, Criticals, Velocity, Aimbot, Sprint, Sneak, Speed, Fly, AirJump,
    HighJump, AutoRespawn, NoFall, AntiAFK, AutoWalk, Blink, FullBright, ESP, ItemESP, AutoTool,
    Spammer.
  - The remaining modules are registered **scaffolds** — they toggle and expose options, and
    each documents the dedicated mixin/util still needed to complete its deep behavior
    (movement/collision internals, render hooks, interaction/container utils, packet transforms).
  - Purely offensive server-attack modules (ServerCrasher, Disabler, Kick, Damage) are registered
    as **inert placeholders with no attack/DoS payload**.

> Nothing here is compile-verified yet — the build sandbox blocks the Forge/Sponge Maven repos.
> Build locally with JDK 17; report errors and they get fixed on the compiling base.

### Roadmap (next sessions)

- Activate the **Mixin pipeline** (MixinGradle + AP + reobf refmap) for events Forge cannot provide
  natively (packet interception, movement internals, timer, item-use cooldown, block shapes, ...).
- Port the **RotationManager**, target/aim utilities and combat modules (KillAura, Velocity,
  Criticals, ...).
- Port movement modules (Fly, Speed, Scaffold, Step, ...).
- Port render modules (Tracers, Nametags, StorageESP, HUD theme system, ...).
- Port a proper **ClickGUI**.
- Config persistence (JSON) and per-module settings save/load.
- Continue toward the full LiquidBounce module set.

---

## Building

> **Requires JDK 17** (Forge 1.20.1). The Gradle toolchain is pinned to Java 17.

```bash
./gradlew build          # produces build/libs/solidbounce-<version>.jar
./gradlew runClient      # launch a dev client with the mod loaded
```

The jar goes in your Forge 1.20.1 `mods/` folder. **Kotlin For Forge** is required at runtime and
is declared as a dependency in `mods.toml` (Gradle pulls it into dev automatically; for a normal
install, add the Kotlin For Forge mod to `mods/` as well).

Third-party helper mods (ViaFabric/ViaForge, Sodium/Embeddium/optimizers, etc.) are intentionally
**not** bundled — add whatever you want yourself, as requested.

### Note on the build environment

The project was scaffolded in an environment whose network policy blocks the Forge/Sponge/Parchment
Maven repositories, so a full Gradle build could not be run there. Build locally with JDK 17.

---

## Usage

- Toggle modules by key bind (set with `.bind <module> <key>`) or via chat: `.t sprint`.
- `.list` shows all modules; `.panic` disables everything.
- Default command prefix is `.`.

---

## License & attribution

Solid-Bounce is **free software under the GNU GPLv3** (see [`LICENSE`](LICENSE)), because it is a
derivative work of LiquidBounce.

- Original work © 2015–2024 **CCBlueX** — <https://github.com/CCBlueX/LiquidBounce>
- Forge port modifications © 2025 Solid-Bounce contributors.

This project is not affiliated with or endorsed by CCBlueX or Mojang/Microsoft.

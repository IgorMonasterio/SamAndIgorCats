# SamAndIgorCats

Minecraft mod for **Forge 1.20.1** (Forge 47.4+, Java 17, Mojang official mappings).
Mod id `samcats`, package `com.igormonasterio.samcats`, version `0.0.5-alpha`, MIT, author Igor Monasterio.

What it adds:

- **21 unique cats**, one of each per world. Each has its own coat (texture), body size and meow pitch.
  They arrive one by one, in random order. They never despawn and come back
  one Minecraft day after dying. Spawn eggs make extra copies that the world does not track.
  **Each cat has a personality** (see "Personalities" below), and sneak + right click with an empty hand
  strokes a family cat.
- **Cardboard box**: placed, cats walk in and sit; worn on the head while sneaking, monsters ignore you
  (except bosses and the Warden), nearby cats join you, and 3+ cats heal you.
- **Laser pointer**: hold right-click to project a red dot up to 48 blocks away; tamed cats chase and pounce on it.
- An advancement tab that works as a cat album (one advancement per cat, plus "The Whole Family").

## Rules (always)

1. **Igor is the only visible author.** Before the first commit in a fresh clone run:
   `git config user.name "Igor Monasterio" && git config user.email "129518645+IgorMonasterio@users.noreply.github.com"`.
   No `Co-Authored-By`, no "Generated with ..." lines and no mention of AI in commits, PRs, code or docs.
2. **Everything in the repo is written in English** (code, comments, README, commit messages, PRs).
3. **Never change the mod id (`samcats`), the cat registry ids, or the package (`com.igormonasterio.samcats`).**
   Existing worlds depend on them. The same goes for the other registry ids (`cardboard_box`, `laser_pointer`,
   `<cat>_spawn_egg`) and for the SavedData name and NBT keys (see below).
4. **Cat textures, lang files, spawn egg models and the cat advancements are generated** by
   `tools/gen_cat_textures.py` and `tools/gen_cat_data.py`. To change them, edit the script and regenerate;
   never hand-edit the outputs.
5. **`./gradlew build` must work before anything else.** If you change Java, compile again before committing.
6. **One pull request per task**, with a short description of what changed and why.

## Building

Requires JDK 17 (the Gradle toolchain asks for 17; the foojay resolver can download one if it's missing).

```
./gradlew build
```

The jar ends up in `build/libs/SamAndIgorCats-<version>.jar` (reobfuscated by `reobfJar`).
Useful tasks: `./gradlew runClient`, `./gradlew runServer` (working dir `run/`, git-ignored).

The first build needs network access to `maven.minecraftforge.net`, `piston-meta.mojang.com`,
`piston-data.mojang.com`, `libraries.minecraft.net`, Maven Central and the Gradle plugin portal (plus
`api.foojay.io` if JDK 17 isn't installed, and `resources.download.minecraft.net` for `runClient` assets). In sandboxes that block those hosts the
build can't run locally; rely on CI.

`gradlew` must keep its executable bit in git (`git update-index --chmod=+x gradlew`), or CI fails with
"Permission denied".

### CI

`.github/workflows/build.yml`:

- Builds on every push to `main`, on pull requests and on manual runs, and uploads the jar as the
  `SamAndIgorCats` artifact.
- On a pushed tag `v*.*.*` it also attaches the jar to the GitHub release for that tag (creating the release
  if it doesn't exist). Bump `mod_version` in `gradle.properties` before tagging, since the jar name comes
  from it.

## Layout

```
src/main/java/com/igormonasterio/samcats/
  SamCats.java            @Mod entry point: registers everything, gives every cat type vanilla cat attributes
  ModRegistry.java        DeferredRegisters: one EntityType + ForgeSpawnEggItem per cat, box block/item,
                          laser item, creative tab
  CommonEvents.java       Forge-bus events: adds laser/box goals to every Cat (vanilla too), box stealth
                          (cancel/clear monster targets), purring regen, spawner tick, laser cleanup on logout
  NaruPowers.java         Forge-bus events for Naru: nine lives (LivingDeathEvent), roar (LivingHurtEvent),
                          night vision for her owner
  BoxStealth.java         isBoxed / isHidden / canBeFooled rules
  LaserTracker.java       Server-side map player UUID -> current laser dot (dimension, position, game time)
  entity/
    CatProfile(s).java    The list of cats: id, scale, voice pitch, spawn egg colours. Source of truth in Java.
    UniqueCat.java        Base class for family cats: persistent, custom name, voice pitch, vanilla kittens,
                          reports death to the spawner
    NaruEntity.java       Adds ChaseIvyGoal; `chasing` / `provoked` flags; save and roar cooldowns
    IvyEntity.java        Flees from a chasing Naru, hides in a box (HideInBoxGoal), teases Naru (TeaseNaruGoal)
    personality/          Personalities (which goals each cat gets, reactions to a stroke) and one goal per
                          behaviour: FollowLeader, BegForFish, StayClose, Shy, Grumpy, Play, Nap, Pose,
                          SitOnSoft, Curious, Steal, Greet, RainShelter, BringGift, Court
    goal/                 LaserChaseGoal, JoinBoxGoal (cat joins a boxed player), SitInBoxGoal (placed box),
                          ChaseIvyGoal, HideInBoxGoal, TeaseNaruGoal
  world/
    CatSpawner.java       Runs from ServerTickEvent every 200 ticks: picks a ready cat and spawns it near a
                          random player (any dimension) online > 20 s; handles deaths (return after 24000 ticks)
    WorldCatsData.java    SavedData on the overworld: which UUID is the "real" copy of each cat
  block/CardboardBoxBlock Open box (thin floor collision, pathfindable). CLOSED=true is only used to render a
                          boxed player.
  item/                   CardboardBoxItem (BlockItem + Equipable on HEAD), LaserPointerItem (use-tick raycast)
  client/                 ClientEvents (renderer registration; boxed player drawn as a closed box),
                          UniqueCatRenderer (vanilla CatRenderer with per-cat texture and scale),
                          SamCatModel (CatModel exposing the head), CrownLayer (Naru), El Negrito's EyesLayer
src/main/resources/
  assets/samcats/         lang (generated), models, blockstates, textures (entity/ generated except ivy.png)
  data/samcats/           advancements (cats/ generated; naru_and_ivy.json hand-written), recipes, loot table
tools/
  gen_cat_textures.py     Cat coats from the vanilla cat textures
  gen_cat_data.py         Lang entries, spawn egg models, cat album advancements
  gen_extra_textures.py   textures/entity/crown.png and el_negrito_eyes.png
```

### Registry

Everything is registered through `DeferredRegister`s in `ModRegistry`, driven by `CatProfiles.ALL`.
For each profile: entity type `samcats:<id>` (hitbox scaled by `scale`, `MobCategory.CREATURE`) and
`samcats:<id>_spawn_egg`. The factory picks `NaruEntity` / `IvyEntity` for those two ids and `UniqueCat`
for the rest. Attributes come from `Cat.createAttributes()` in `SamCats.onAttributes`.

Cat registry ids (do not change): `naru`, `ivy`, `batman`, `bonzo`, `calcetin`, `cheeto`, `dolores`,
`el_abuelo`, `el_bebe`, `sin_nombre`, `el_negrito`, `itlerina`, `kalessi`, `la_trico`, `lince`, `mia`,
`noah`, `nube`, `oliver`, `stripey`, `valentino`.

### Entities and goals

- `UniqueCat extends Cat`. `finalizeSpawn` sets the custom name to the translatable entity name and makes it
  persistent; `removeWhenFarAway` is false. Breeding produces plain vanilla kittens. `die()` calls
  `CatSpawner.onDeath` (only if the death wasn't cancelled); Forge's `onAddedToWorld` / `onRemovedFromWorld`
  call `CatSpawner.onLoaded` / `onUnloaded`.
- `CommonEvents.onEntityJoin` adds three goals to **every** `Cat` on the server: `LaserChaseGoal` (priority 1,
  tamed cats only), `JoinBoxGoal` (2) and `SitInBoxGoal` (5). All of them respect `isOrderedToSit`.
- `onEntityJoin` also adds, to every `Cat`: an `AvoidEntityGoal` (7) against Bonzo (not to Bonzo himself) and
  `CourtGoal` (5) (not to Naru, Bonzo or Stripey). Kalessi gets no `SitInBoxGoal`.
- Naru gets `ChaseIvyGoal` (8; starts at once when Ivy provoked her; if Ivy is in a box she gives up after
  60 ticks). Ivy gets `HideInBoxGoal` (3), an `AvoidEntityGoal` (4) against a chasing Naru and `TeaseNaruGoal` (7).

### Personalities

`UniqueCat.registerGoals` calls `Personalities.addGoals`, which adds each cat's goals by id. Vanilla cat
priorities, for reference: 1 float/panic, 2 sit when ordered, 3 relax on owner, 4 tempt, 5 lie on bed,
6 follow owner, 7 sit on block, 8 leap, 9 attack, 10 breed, 11 stroll, 12 look at player.

| Cat | What | How |
|---|---|---|
| Naru | Crown, court, night vision, roar, nine lives, Luck on stroke | `CrownLayer`, `CourtGoal`, `NaruPowers` (save once per 24000 ticks, saved as `SamcatsLastSave`; roar every 600 ticks) |
| Ivy | Teases Naru, hides in a box | `TeaseNaruGoal`, `HideInBoxGoal`, `IvyEntity.isHiddenInBox` |
| Batman | Untamed strays follow her | `FollowLeaderGoal` (10) on `CatProfiles.STRAYS` |
| Bonzo | Begs when you hold fish; others step aside | `BegForFishGoal` (4); avoid goal in `onEntityJoin` |
| Dolores, Mía | Stay by the owner; heal a hurt owner | `StayCloseGoal` (5); regen in `CommonEvents.onPlayerTick` |
| Itlerina | Runs from players who aren't sneaking | `ShyGoal` (3); vanilla player-avoid removed in `UniqueCat.reassessTameGoals` |
| Stripey | Avoids other cats | `AvoidEntityGoal<Cat>` (9) |
| El gato sin nombre | Refuses name tags | `CommonEvents.onInteract` |
| El Negrito | Glowing eyes | `EyesLayer` with `el_negrito_eyes.png` |
| Lince | Hunts rabbits and chickens, brings a gift | target goals (2), `BringGiftGoal` (4), gift set in `CommonEvents.onDeath` |
| El Bebé | Chases items and cats, pounces | `PlayGoal` (8) |
| El Abuelo | Slow, naps a lot | movement speed 0.22 in `SamCats.onAttributes`, `NapGoal` (9) |
| Noah | Naps belly-up; extra hearts if stroked lying | `NapGoal` (9), `Personalities.onPetted` |
| Oliver | Hisses and backs off from sprinting players unless stroked in the last 10 min | `GrumpyGoal` (3), `SamcatsCalmUntil` |
| Valentino | Poses after being looked at for 3 s | `PoseGoal` (4) |
| Kalessi | Sits only on beds and carpets | `SitOnSoftGoal` (7); vanilla `CatSitOnBlockGoal` removed |
| Cheeto | Follows players from afar while untamed | `CuriousGoal` (5); vanilla player-avoid removed |
| Calcetín | Steals items lying around (age > 100), returns them when stroked | `StealGoal` (6), `SamcatsStash` / `SamcatsStashHome`, dropped on death |
| La Trico | Greets every newcomer, visits other cats | `GreetGoal` (6); `CatSpawner.spawn` calls `greet` |
| Nube | Slow fall, no fall damage; shelters from rain | `UniqueCat.aiStep` / `causeFallDamage`, `RainShelterGoal` (4) |

Stroke = sneak + right click with an empty main hand (`CommonEvents.onInteract`, cancels the vanilla sit toggle).

### Spawner and SavedData

- `WorldCatsData` (`samcats_cats.dat` in the overworld data folder) keeps `alive: id -> UUID`,
  `returnAt: id -> game time`, `lastSeen: id -> dimension + block pos`, `previous: id -> UUID` (the last tracked
  cat after it died or got lost) and `nextNewcomer`. A cat is "ready" when it has no UUID and `returnAt` has passed.
- `CatSpawner.tick` (overworld game time, every 200 ticks): one random ready cat (Naru and Ivy included, no
  special order) spawns near a random player in any dimension once `nextNewcomer` has passed (2-4 min between
  newcomers; the first one 20-30 s after the player joins).
  Spots are searched in a 10-20 block ring, near the player's height first, then on the surface (except in
  dimensions with a ceiling, like the Nether).
- `onDeath` only reacts if the dying cat's UUID is the tracked one (egg copies don't count): it clears the UUID,
  sets `returnAt = now + 24000` and broadcasts the "ran away" message.
- Reconciliation, so a cat never goes missing for good or gets duplicated after a crash or another mod:
  - Every check, `track` looks each tracked UUID up in all levels and updates `lastSeen`. If it isn't found but
    the entities of the 5x5 chunks around `lastSeen` are loaded, for 2 checks in a row, the cat is lost:
    it's marked as gone with `returnAt = now`, so it comes back soon.
  - `onUnloaded`: a tracked cat removed as `DISCARDED` (deleted without dying) is lost right away; one saved
    with its chunk updates `lastSeen` to its exact position.
  - `onLoaded`: if the cat's UUID is `previous` and nothing else is tracked for that id, it's taken back as the
    real one (e.g. the data was saved but the chunk wasn't before a crash).
- NBT format is world data: tag `Cats` -> `<id>` -> `UUID`, `Return`, `Previous`, `SeenDim`, `SeenPos`;
  `NextNewcomer`. `load` also migrates the 0.0.1 keys `Naru`, `Ivy`, `NaruReturn`, `IvyReturn`. Keep it backwards
  compatible.

### Client

- `ClientEvents.ModBus` registers a `UniqueCatRenderer` per cat (texture `textures/entity/<id>.png`, model and
  shadow scaled by `scale`).
- `ClientEvents.ForgeBus.onRenderPlayer` cancels the player render when `BoxStealth.isBoxed` and draws the
  `closed=true` box block instead (no name tag).
- Client classes are only referenced from `client/`, behind `Dist.CLIENT` subscribers. Keep common code free of
  client imports or the dedicated server will crash.

### Box and laser

- Box stealth (`BoxStealth`): hidden = wearing the box on the head, crouching, not spectator, and no attack in
  the last 60 ticks. `LivingChangeTargetEvent` is cancelled for hidden players, and every 10 ticks mobs already
  targeting a hidden player drop the target (and the brain `ATTACK_TARGET` memory). Bosses (`forge:bosses`) and
  the Warden are never fooled. Every 40 ticks a boxed player with 3+ cats within 2.5 blocks gets Regeneration.
- Laser: `LaserPointerItem.onUseTick` raycasts 48 blocks on the server, sends a dust particle and updates
  `LaserTracker`. Dots expire by age (cats use max 3 ticks to start, 10 to keep chasing), so a missed
  `releaseUsing` is harmless. Logout clears the player's dot.

## Regenerating coats and data

Never edit the outputs by hand (rule 4). Edit the script, run it from the repo root, and commit script and
outputs together.

```
python3 tools/gen_cat_data.py
```

Rewrites `assets/samcats/lang/en_us.json` and `es_es.json` (entity names, spawn egg names, "ran away" message,
cat album advancement strings), `models/item/<id>_spawn_egg.json` and `data/samcats/advancements/cats/*.json`.
Lang keys the script doesn't own (tooltips, block/item names, `samcats.msg.appeared`, the `naru_and_ivy`
advancement strings) are kept as they are; to change one of those, move it into the script first.

```
pip install Pillow
VANILLA_CAT_TEXTURES=/path/to/cat/ python3 tools/gen_cat_textures.py
```

Needs the vanilla 1.20.1 cat textures (`assets/minecraft/textures/entity/cat/` extracted from the client jar;
they are not in the repo). Default path is `vanilla/cat/` relative to the current directory. Writes
`assets/samcats/textures/entity/<id>.png` for every cat **except Ivy**, whose coat was made by hand and must
not be overwritten or deleted. Block and item textures are hand-made too.

### Adding a cat

1. Add a `CatProfile` to `CatProfiles.ALL` (new id, never reuse or rename an old one).
2. Add it to `CATS` in `tools/gen_cat_data.py` (EN and ES names).
3. Add a `make()` branch and a `CATS` entry in `tools/gen_cat_textures.py`.
4. Run both scripts, update the cat count in `README.md` and in `mod_description` (`gradle.properties`),
   build, and test in game.

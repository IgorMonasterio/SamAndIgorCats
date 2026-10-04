# SamAndIgorCats

A Minecraft Forge mod about cats: a family of **21 unique cats**, each with its own coat, size and voice, plus a **cardboard box** to hide in and a **laser pointer** to drive them crazy.

**Minecraft 1.20.1 · Forge 47.4+** · Alpha

## The cats

There is **only one of each cat per world**. Naru and Ivy turn up first, together, near a player who has been online for a little while. The rest arrive one by one every few minutes, with a message in chat. They never despawn, and if one dies it comes back the next Minecraft day.

Naru, Ivy, Batman, Bonzo, Calcetín, Cheeto, Dolores, El Abuelo, El Bebé, Nameless, El Negrito, Itlerina pero con Hache, Kalessi, La Trico, Lince, Mía, Noah, Nube, Oliver, Stripey and Valentino.

- Tame them like any cat (raw cod or salmon, sneaking, while they come to you).
- **Naru chases Ivy** every now and then, and Ivy always runs. Tame both for a secret advancement.
- Some are bigger, some are tiny, and some have very particular meows.
- The **SamAndIgorCats advancement tab** is the cat album: one entry per cat, filled in as you tame them. Tame all 21 to complete *The Whole Family*.

## Cardboard box

```
Paper  ·      Paper
Paper  Paper  Paper
```

- **On the ground:** cats can't resist it. They walk in and sit for a while.
- **On your head** (right-click in the air): sneak and you become a box. Monsters stop targeting you and lose track of you if they were chasing. Hit something and the box stops hiding you for 3 seconds. Bosses and the Warden are not fooled.
- Cats nearby come and hide with you. With 3 or more, their purring heals you.

## Laser pointer

```
·      ·         Red dye
·      Redstone  ·
Iron   ·         ·
```

Hold right-click to point a red dot up to 48 blocks away. Every tamed cat nearby chases it and pounces. Creepers are scared of cats, so it doubles as creeper control; cats also press pressure plates.

## Commands

All cats have a spawn egg in the **SamAndIgorCats** creative tab. Eggs make extra copies that don't count as the world's own cat.

```
/summon samcats:naru
```

## Building

Requires JDK 17.

```
./gradlew build
```

The jar ends up in `build/libs/`. The cat coats are generated from the vanilla cat textures with `tools/gen_cat_textures.py`, and the language files, spawn egg models and advancements with `tools/gen_cat_data.py`.

## Author

Igor Monasterio

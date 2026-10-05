# SamAndIgorCats

A Minecraft Forge mod about cats: a family of **21 unique cats**, each with its own coat, size and voice, plus a **cardboard box** to hide in and a **laser pointer** to drive them crazy.

**Minecraft 1.20.1 · Forge 47.4+** · Alpha

## The cats

There is **only one of each cat per world**. They turn up one by one, in random order, near a player who has been online for a little while: the first one soon after you join, then a new one every few minutes, with a message in chat. They never despawn, and if one dies it comes back the next Minecraft day.

Naru, Ivy, Batman, Bonzo, Calcetín, Cheeto, Dolores, El Abuelo, El Bebé, El gato sin nombre, El Negrito, Itlerina pero con Hache, Kalessi, La Trico, Lince, Mía, Noah, Nube, Oliver, Stripey and Valentino.

- Tame them like any cat (raw cod or salmon, sneaking, while they come to you).
- **Stroke a cat:** sneak and right-click it with an empty hand. Hearts and purring; some of them react in their own way.
- Some are bigger, some are tiny, and some have very particular meows.
- The **SamAndIgorCats advancement tab** is the cat album: one entry per cat, filled in as you tame them. Tame all 21 to complete *The Whole Family*.

### Every cat has a personality

- **Naru**, the Queen: wears a little golden crown and chases Ivy every now and then. When she sits down, the cats around her sit in a ring, like a royal court. With your tame Naru by your side you see in the dark. If a monster hurts you near her, she lets out a huge hiss that knocks monsters back and sends them running. And once a Minecraft day, if you're about to die near her, **she saves your life**. Stroke her for a bit of Luck.
- **Ivy** always runs from Naru, but sometimes she starts it: she walks up, taps Naru and bolts. If there's a cardboard box nearby, she dives in, and Naru can't get her in there.
- **Batman**, the boss of the street cats: the untamed strays (Calcetín, El Abuelo, El Bebé, El gato sin nombre, El Negrito, La Trico and Lince) follow her around.
- **Bonzo**, the neighbourhood tough guy: hold a fish and he follows you, begging with his broken meow. The other cats step aside when he walks by.
- **Dolores** and **Mía**, the cuddly ones: they stay right by their owner, and their purring heals you when you're hurt.
- **Itlerina pero con Hache** is very shy: she runs from anyone who isn't sneaking.
- **Stripey**, the loner, keeps away from the other cats.
- **El gato sin nombre** won't take a name tag. He's fine without one.
- **El Negrito**: at night, all you can see are his glowing eyes.
- **Lince** hunts rabbits and chickens, and brings his owner a present.
- **El Bebé** is playful: he chases other cats and anything lying on the floor, and pounces.
- **El Abuelo** has seen it all: he's slow and naps a lot.
- **Noah** rolls over for belly rubs. Stroke him while he's lying down.
- **Oliver** looks grumpy: run at him and he hisses... until you stroke him.
- **Valentino** lets you admire him: look at him for a few seconds and he sits and poses, all sparkles.
- **Kalessi** is a princess: she only sits on beds and carpets.
- **Cheeto**, the new friend: before he's tamed, he follows you from a distance.
- **Calcetín** steals things lying on the floor and carries them to his corner. Stroke him and he gives it all back.
- **La Trico**, the neighbourhood gossip, goes to welcome every new cat and visits the others.
- **Nube** floats down gently when she falls, and hides under a roof when it rains.

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

The jar ends up in `build/libs/`. The cat coats are generated from the vanilla cat textures with `tools/gen_cat_textures.py`, Naru's crown and El Negrito's eyes with `tools/gen_extra_textures.py`, and the language files, spawn egg models and advancements with `tools/gen_cat_data.py`.

## Author

Igor Monasterio

## License

MIT. See [LICENSE](LICENSE).

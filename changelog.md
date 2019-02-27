##### Version 1.14-1.0.0
Rewrote all the things

##### Version 1.12.2-0.2.1 - BUILT
- Fixed players getting trapped inside the villager when curing a possessed zombie villager
- Fixed eau de mort being useless for ending possession

##### Version 1.12.2-0.2.0 - BUILT
Rewritten mob possession, hopefully fixing a lot of bugs

General Features:
- Souls are now almost completely intangible regarding other entities

Possession:
- You can now use enderpearls and other short-distance teleportation !
- You can now use grappling hooks and other velocity altering apparatus !
- YOU CAN NOW SPRINT
- Potions are now synchronized properly between the player and its possessed entity 
  (Drinking and curing potions work !)
- Fixed hands appearing in first person when invisible
- Fixed players appearing (comically) sit on their possessed entity in multiplayer
- Fixed being randomly kicked from world when left-clicking
- Fixed possessed entities not respecting the player's attack cooldown
- Fixed the fire overlay appearing in third person
- Fixed possessed entities moving by themselves under obscure circumstances
- Fixed possession breaking with some portals like Aether Legacy's

General bugfixes:
- Fixed the soul shader sometimes appearing when it should not
- Fixed soul players sometimes appearing solid to other players
- Fixed the mod forgetting everything about the player after coming back from the End
- Fixed human flesh being consumed in creative
- Fixed creative flight being removed when regaining corporeality in creative mode

Mod compatibility:
- Made evilcraft and cannibalism fleshes fit for undead consumption

##### Version 1.12.2-0.1.6 - BUILT
Convert old possessed entities to new format from 0.1.5

##### Version 1.12.2-0.1.5 - BUILT
Dissolution's API is distributed as a contained dependency now (potentially means other mods can use it more easily)

Bugfixes:
- Fixed crash when two mods registered entities with the same name
    - Warning: may break old worlds. Skip to 0.1.6 if you can.
- Probably display an error screen instead of crashing when forge is too old

##### Version 1.12.2-0.1.4 - BUILT
Features:
- `skipDeathScreen` is now enabled by default
- Added a neat fade effect when you die, to replace the game over screen (only for remnants)

Hardcore tweaks:
- Dying while you are possessing something makes you die for good
- Bodies cannot be purified
- Basically dissolution only works as a 1-UP in hardcore (although in theory other players can give you their bodies)

Bugfixes:
- Fixed compatibility issue with Sync where newly created shells would have the soul status
- Fixed russian lang file encoding

##### Version 1.12.2-0.1.3 - BUILT

Bugfixes:
- Fixed a bug with using `/setRemnant player false` on a soul
- Remnant players are now properly invisible during possession
- Theoretically fixed a good chunk of synchronization issues
- Fixed possession setting incorporeal effect on all clients
- Fixed possessed entities' loot getting in the soul's inventory

##### Version 1.12.2-0.1.2 - BUILT

Changes:
- Added a blacklist for entities that can be possessed
Bugfixes:
- Fixed player bodies despawning
- Fixed possessed entities hitting themselves with Swing Through Grass
- Fixed server crash with Inspirations

##### Version 1.12.2-0.1.1 - BUILT

Changes:
- Possessed Skeletons fire arrows with the appropriate effects
- Illagers and witches drop human flesh

Bugfixes:
- Fixed purification cauldron connecting to fences (game-breaking bug)
- Fixed possessed entities taking fall damage on ladders

API:
- Dissolution's optional dependencies are not transitive anymore
- Update LadyLib


##### Version 1.12.2-0.1 - BUILT
- Strong / weak soul, now Remnant / Vanilla soul: when joining a world for the first time, the player will be granted the choice between the mod’s death system or the vanilla death system.
- Soul player state: if the player is a Remnant, upon death, his soul leaves his body, allowing a free roaming of the world in order to select a new body to inhabit.
- Undead possession: completely re-written to be the most enjoyable possible, the undead possession system is here to stay. When in soul mode, you will be able to enter any undead monster’s body and control them. This will give you access to their inventories, advantages but also inconveniences. In a more detailed way:
  - Fixed latency when moving around while possessing something
  - Fixed offhand items rendering
  - Fixed bow usage when possessing entities
  - Fixed possessed mobs being unable to mount or dismount other things
  - Fixed possession removal in creative
  - Allowed undead players to go through portals when using possession
  - Allowed possession to work with any undead mob from any mod

New Items

- Human flesh:
  - Can be eaten by a human to restore food
  - Can be eaten by an undead player to regain life and eventually his humanity
  - Can be obtained from:
    - Killing humans (such as human players, villagers, witches and illagers)
    - Crafting via one rotten flesh piece and one ghast tear
    - Putting a ghast tear in a water filled cauldron, and purifying rotten flesh by interacting with it
- Human organs (heart, brain): serving the same use as the human flesh, they are mainly useful for brewing the mod’s potions.
- Obnoxious potion: obtained when brewing human flesh in water bottles, it’s ineffective and acts as a, intermediary potion.
- Eau de mort: obtained when brewing a human brain in obnoxious potions, it can expel Remnant souls out of their body, without destroying it, and alter vanilla souls to make them Remnant.
- Sanguine potions: obtained when brewing a human heart in obnoxious potions, it has no effect for souls other than Remnant, altering them to become vanilla.

Various changes

- API changes : new Forge events for possession
- The air bar is now rendered when possessing something
- Possessed mobs now render arms in first person and full body in the inventory
- Players possessing entities stop losing their experience
- Possessed entities don’t get artificially targeted by mobs normally targetting players anymore
- Added baby zombies immunity to sunlight back by default

Cross-mod interactions

- Added Thaumcraft aspects for every entity and item
- Added Thaumcraft brain to human brain recipe
- Added Inspirations recipe for purified flesh
- Added Inspirations recipes for the mod’s potions

##### Version history end

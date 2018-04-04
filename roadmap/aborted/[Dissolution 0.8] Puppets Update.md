# DISSOLUTION 0.8

## Puppets Update

#### Addition of:

-         New States of Soul:
     -         Puppet master: Having a puppet master soul triggers the mod’s death system for the player. Upon death, the player’s soul leaves the body, and the player has to find a new puppet as a body (possessing undead mobs no longer being a possibility). The puppet master also grants the ability to possess Snowmen and Iron Golems. This is the mod default state of soul proposed the first time the player joins the world.
     -         Lich cursed (previously Strong Soul): Obtaining this state of soul requires having a puppet master soul. Grants the ability to the dead player of possessing undead mobs as a new body.
     -         In the inventory is now displayed the current state of soul of the player. Hovering the logo displays the name and tooltip of the state of soul. If the player has the vanilla death, no logo is shown. Example for a puppet master player and a lich cursed player:

![img](img/sos_indicators.png)

-         
  Rotten flesh conversion: When under the effect of Regeneration, if the player possesses Rotten flesh in his inventory, these will start to convert into human flesh at a rate of one item per heart regenerated.


#### New items:

- Watcher in a soul orb
- Human flesh: Dropped from Villagers and other alive Players (in a human body) (between1 and 3 at death). Restores 2 food icons. Eaten by a puppet master in a clay puppet body, replaces 2 puppet hearts of the health bar into human hearts. Can be cooked.
- Cooked human flesh: Restores 10 food (5 food icons).

#### New blocks:

- Empty spawner: Obtained by breaking a mob spawner. Has no effect when placed.
- Soul lantern: Obtained by throwing a wisp in a soul orb on an empty spawner. Lures undead mobs into the zone (32 blocks effect radius). Gives an empty spawner when mined.
- Watching lantern: Obtained by throwing a watcher in a soul orb on an empty spawner. Makes all undead mobs passive in a 32 blocks effect radius towards players with a SoS. Gives an empty spawner when mined.
- Puppet head: Obtained by collecting a wisp with a log in hand, therefore transforming the log in the head. Similarly to a normal mob skull, can be placed, can be equipped as a helmet that offers a protection similar to a leather cap.

#### New entities:

-         Watcher: If the player possesses any state of soul, watchers may appear in caves below depth 16. They are characterized by the apparition of an eye on the texture of a stone type block. When the block is broken, the watcher gets out of the block and moves like a faerie. If the player doesn’t have the lich cursed SoS, the watcher can be collected like a soul faerie, triggering the lich soul curse dialogue. The player can then accept (or refuse) to exchange its current SoS for the lich cursed SoS. If the player possesses a soul orb in his inventory,instead of consuming the watcher and triggering the dialogue, the watcher will be collected in the soul orb (no matter the current SoS).

- Puppet: Used to regain a body when the player is dead and has a puppet master soul. Uncontrolled by a player, it stays idle and can’t despawn. Immune to poison, regeneration,instant damage and health. Cannot sprint, doesn’t regenerate life, has no food bar. Spawning a puppet requires placing 2 material blocks (same type) and a puppet head on top, similar to how you would spawn a snowman. The puppet mob(except the obsidian puppet), even controlled by a player, always wears a puppet head. Shall that puppet head be taken off (by the player for example),the puppet will instantly die (and drop its head). When a puppet (controlled or not) dies, it has a 25% chance of dropping its head.
- Wooden puppet: 20hp, takes x2 damage from axes, blocks required: planks (any). When killed, drops between 1 and 4 sticks.
- Clay puppet: 20hp, takes x2 damage from shovels, blocks required: clay blocks. When killed, drops between 2 and 8 clay balls. When possessed by a puppet master player, gains the ability to eat human flesh in order to regain a human body. When the clay puppet is full converted into a human body, the player will still have a puppet head in the helmet slot, but can take it off without dying.
- Obsidian puppet: 40hp, constant fire / arrow / explosion immunity, can only be damaged from diamond tools, takes x2 damage from diamond pickaxes. Cannot be obtained /spawned the normal way, spawns only if a charged creeper kills a skeleton wearing a puppet head on an obsidian block. When killed, drops nothing.
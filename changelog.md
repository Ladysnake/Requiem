------------------------------------------------------
Version 1.4.0
------------------------------------------------------
### The Chilly Shades Update
**Additions**
- Added the Wandering Spirit remnant state
    - Wandering spirits are a variant of remnant players that cannot regenerate a player body
    - They can however leave a mob at any time without killing it

**Changes**
- Some mobs get cured into a new mob instead of being assimilated into a player body
    - Zombie villagers get cured into possessable villagers
    - Zombified piglins get cured into piglins
        - Zombified brutes go back to being brutes after the cure
- The `/requiem remnant set` command can now use any ID

**Mod Compatibility**
- When dissociated from a player body, your origin now gets temporarily changed to Vagrant

**Fixes**
- Fixed a random crash at launch with Bedrockify
- Fixed doctor4t's capitalization in fabric.mod.json
- The API jar is once more available on Bintray
- Possibly fixed an issue where player attributes (notably speed) would get funky values when reviving
- Fixed Opus Daemonium items not having the enchantment glint

#### Pandemonium
**Additions**
- Wandering spirits can take over player shells

**Mod Compatibility**
- Player shells now store a player's Origin
    - Origins' graphical effects are visible on the shell

**Fixes**
- Fixed some data being overwritten when merging with a player shell
- Impersonation through body swapping now works more or less as intended
- Players merging with a shell now get properly teleported to the shell's location

------------------------------------------------------
Version 1.3.0
------------------------------------------------------

![Screenshot](https://cdn.discordapp.com/attachments/477596941757317121/785214169690800148/2020-12-06_19.39.30.png)

### The Gloss and Golems Update

**Additions**

- Demons can now possess all golems, and all undead !  
    - Golems are defined by the `requiem:golems` tag, and include iron golems, snow golems, and shulkers  
    - Undeads are defined by the `requiem:undead` tag, and now additionally include phantoms, undead horses, and zoglins  
- **Added the ability for ghosts to go through walls !** Just hug a wall, and you should start softly entering it.  
- Added an attack indicator for ranged abilities of possessed mobs  
- Added custom inventories for possessed mobs  
    - Giants will no longer take the whole inventory screen  
- Added an advancement tab  
- Added a bunch of tags to control possessed mob behaviour  
    - `requiem:armor_users`: allows a mob to wear armor when possessed  
    - `requiem:dichromats`: makes a mob use the `dichromatic` shader vision  
    - `requiem:inventory_carriers`: allows a mob to access the main inventory  
    - `requiem:supercrafters`: gives a portable crafting table to the mob  
    - `requiem:tetrachromats`: makes a mob use the `tetrachromatic` shader vision  

**Changes**

- You can now cure your friend (or even an enemy) who is possessing an undead  
- Added localization for all gamerules in the gamerule screen  
- Ranged abilities can now be fired from way further than 3 blocks  
- The possession icon now only appears for mobs you can possess  
- Replacing too many bones as a Wither Skeleton will change you into a regular skeleton  

**Mod Compatibility**

- Added an Origins screen to choose your soul type! This replaces the first death dialogue.  
  - This screen respects the `requiem:startingRemnantType` gamerule  
- Made Eldritch mobs unable to be possessed  
- Fixed most Origins powers staying activated outside a human body  
- Golems Galore's golems can be possessed by default  
- Better Nether's jungle skeletons can be possessed by default  

**Removals**

- Removed Bart. You served well, but players don't need you anymore.  

**Fixes**

- Piglins no longer aggro ghosts  
- Fixed Ghosts being able to interact with boats and minecarts  
- Fixed crash when trying to possess an invisible mob  

#### Pandemonium

**Additions**

- Added special interaction between evokers and endermen  
- Villagers and piglins now have access to a portable 3x3 crafting screen *(breaks with BetterEnd and BetterNether...) * 
- All mammals now have dichromatic vision  
- Chickens and parrots now have "tetrachromatic" vision  
- Bees now have a custom bee vision  

**Changes**

- Ticking is even more faithful to the possessed entity, eg.  
    - Bees die after stinging  
    - Piglins and hoglins zombify in the overworld  
    - Elder guardians apply their curse (oops, that one is not in your control)  
    - Zombified piglins get a speed and strength boost when they or their friends are hurt  
    - etc.  
- Player shells are back, and they should feel even more like a real player  
    - You once again need a player shell to dissociate from a possessed mob  
    - You can impersonate a player by stealing its shell ! (probably buggy still)  
- Foxes, pandas, and dolphins can now hold items in their "hands"  
- Blazes are more accurate with their fireballs  
- Possessed pandas now stand up when sneaking  
- Guardian beams are now visible in first person  

**Mod Compatibility**
- Players can now teleport to and from the Bumblezone when possessing a bee  

**Fixes**
- Fixed possessed elder guardians not getting the beam ability  
- Vexes you summon as an evoker will now properly assist you in combat  
- Possessed ghasts now do the shooting face when firing  

------------------------------------------------------
Version 1.2.2
------------------------------------------------------
Additions
- Added the `requiem:disableCure` gamerule to disable any form of cure
- Added the ability for other players to cure a possessed undead
- Added descriptions for gamerules in the Edit Gamerules screen

Fixes
- Fixed server crash with Pandemonium
- Fixed changes to the `requiem:showPossessorNameTag` not updating ongoing possessions
- You will no longer share your mob vision with everyone around you when you start possessing a mob with a shader (about time)

------------------------------------------------------
Version 1.2.1
------------------------------------------------------
- Fixed pandemonium crashing at launch  
- Added Portuguese translation (thanks to cominixo!)

------------------------------------------------------
Version 1.2.0
------------------------------------------------------
Updated to 1.16.3

Additions  
- Added the Attrition status effect  
    - Upon losing a body, one level of attrition will be gained  
    - Each level of attrition removes 20% of future bodies' maximum health  
        - Caps at Attrition IV (80%)  
        - If you die in hardcore with Attrition IV, your soul is forever put to rest  
    - Attrition is cleared upon recovering your own human body  

- Added the Humanity enchantment  
    - When a possessed entity wields a weapon with this enchantment, killed mobs start dropping normal "human" player loot  
        - With Level I, mobs start dropping items as if a player killed them  
        - With Level II, mobs start dropping XP as if a player killed them  
    - Books with this enchantment can only be found in Nether chests  

- Added a command selector argument for possessed entities (see readme for usage information)  

- Added a `spawnHelpEnderman` gamerule to toggle the helpful enderman feature

- Abilities from the Origins mod now get disabled when playing as a soul or while possessing a mob  

Bugfixes
- Restored all graphical effects from the 1.14 version

API
- Made access to most Requiem registries a lot more sane  
- Added a `MovementRegistry` and a `SoulbindingRegistry`  
- Moved everything that could be to components

Bugfixes
- Fixed weakness status effect not being cleared clientside after possession cure ends

------------------------------------------------------
Version 1.1.4
------------------------------------------------------
Bug Fixes
- Fixed the game crashing whenever an evoker spawned
- Fixed crash at launch with Health Overlay installed

------------------------------------------------------
Version 1.1.3
------------------------------------------------------
Bug Fixes
- Fixed skeletons being able to consume anything
- Fixed custom gamerules not loading on servers
- Fixed possession when using a trans-dimensional teleport command
- Fixed incompatibility with Shulker Charm disabling soul flight
- Fixed incompatibility with Health Overlay, causing hearts over 20HP to not be colored

------------------------------------------------------
Version 1.1.2
------------------------------------------------------
Additions
- Mummies from The Hallow can now be possessed

Bug Fixes
- Fixed health bar not being right during possession when Health Overlay is installed

------------------------------------------------------
Version 1.1.1
------------------------------------------------------
Additions
- Added some gamerules
    - `requiem:showPossessorNameTag`: if set to `true`, shows the name of the possessor above the head of possessed entities. (default: `false`)
    - `requiem:startingRemnantType`: can be set to `FORCE_REMNANT` or `FORCE_VANILLA` to enforce all players to be respectively a demon or a normal player at the start of the game.
- Added the `requiem:humans` and `requiem:skeletons` entity type tags for server owners and modpack makers to fine tune mob categories
- Added compatibility for mobz

Bug Fixes
- Fixed crash when skeletons attempt to heal using bones
- Fixed ranged attacks crashing on dedicated servers
- Fixed husk resurrection crashing on dedicated servers


------------------------------------------------------
Version 1.1.0
------------------------------------------------------
Updated to 1.15

Additions
- Added the `regular_eater` entity type tag to allow some possessed mobs to use the standard hunger mechanic

Bug Fixes
- Fixed possessed mobs not tagged as "item user" being able to pick up items

------------------------------------------------------
Version 1.0.1
------------------------------------------------------
Bug Fixes
- Possessed mobs now raise their arms when attacking or using a bow
- Fixed most incompatibilities with Immersive Portals

------------------------------------------------------
Version 1.0.0
------------------------------------------------------
Additions
- Rewrote everything
- See the new mod description for the full list of features

Bug Fixes
- Lots

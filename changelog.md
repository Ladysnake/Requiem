------------------------------------------------------
Version 1.3.0
------------------------------------------------------

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

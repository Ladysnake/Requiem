------------------------------------------------------
Version 1.7.8
------------------------------------------------------
**Additions**  
- Added the `Bolt Hoarder` passive mob ability, making mobs like pillagers and piglins use fewer arrows  
- Added the `Ammo Generation` passive mob ability, although it is not assigned to any vanilla mob  
    - If you really want skeletons to have infinite ammo, you can now add them to the relevant tag  

**Changes**  
- New format for datapack mob item overrides ! It's a lot cleaner now. (old ones are still compatible for now)  
- Added raw rabbit to the list of raw meats  

**Mod Compatibility**  
- Biome Makeover: Added compatibility for all mobs
  - Stone golems have the new exclusive "Ammo Generation" passive ability
  - Mushroom traders can only eat mushrooms !
- Added a bunch of random modded food to requiem's tags. I didn't do any of the big food mods so feel free to contribute or poke their authors (nicely, of course).  

**Fixes**
- Fixed the hunger bar being reset every time you left the game while possessing something

------------------------------------------------------
Version 1.7.7
------------------------------------------------------
**Additions**
- Mobs can now give you loot when you possess them for the first time
    - Skeletons, piglins and pillagers now come with some free arrows

**Changes**
- You can now possess a witch converted from a smitten villager
    - Brought over witch abilities and gameplay fixes from Pandemonium

**Mod Compatibility**
- Chime: Fixed crash at launch when Chime was installed
    - Requiem now bundles Chime :>
- Biome Makeover: Fixed witches with attrition replacing all held items with bottles of antidote

**Fixes**
- Fixed a crash when shift clicking fuel out of a furnace
- Fixed item cooldown not starting when eating the last item of a stack of mob food
- Updated a bunch of libraries
    - Fixed a crash with Fabric API <0.34 in some modpacks
    - Fixed creative flight ability being lost when teleporting to another dimension

#### Pandemonium
**Fixes**
- Fixed a crash when standing on blocks with dynamic collision boxes
- Fixed player shells looking in the wrong direction on spawn

------------------------------------------------------
Version 1.7.6
------------------------------------------------------
**Changes**
- Mobs that are added to `inventory_carriers.json` but not to `item_users.json` will now have their entire hotbar locked

**Fixes**
- Fixed endermen not being able to be added to `possessables.json` without pandemonium
- Fixed sinking mobs (eg. zombies) being allowed to swim while possessed
- Fixed possessed mobs being able to equip armor by shift-clicking in the inventory
- Fixed previously possessed mobs losing their residual inventory when converted (eg. zombies drowning)

------------------------------------------------------
Version 1.7.5
------------------------------------------------------
**Changes**
- Mobs will now keep items in their main inventory after possession ends instead of dropping everything
- Added tooltips for the new mob diets

**Fixes**
- Fixed hunger bar resetting when you re-possess a mob
- Mobs will no longer stare at incorporeal players
- Fixed incorporeal players being able to push mobs around

#### Pandemonium
**Pandemonium is now released separately as part of [its own Curseforge project](https://curseforge.com/minecraft/mc-mods/requiem-pandemonium-expansion)**

**Changes**
- Players can now quickly merge with their body when looking at it while possessing a mob

------------------------------------------------------
Version 1.7.4
------------------------------------------------------
**Changes**
- Weapons enchanted with Humanity can now hurt the Enderdragon
- Added custom diets for villager folks
    - Illagers are carnist
    - Villagers are vegetarian
    - Witches eat stuff no one reasonable would
    - Everyone eats bread (bread is good)
- Made a breaking change in the item override data format, hope no one did a datapack yet

**Mod Compatibility**
- Bumblezone: Fixed the "compatibility improvements" actually crashing the game

**Fixes**
- Fixed Requiem flat out not doing anything in Hardcore

------------------------------------------------------
Version 1.7.3
------------------------------------------------------
**Bugfixes**
- Fixed players being unable to connect to multiplayer servers with requiem

#### Pandemonium
**Additions**
- Raider mobs can now ride ravagers ✪ ω ✪

------------------------------------------------------
Version 1.7.2
------------------------------------------------------
#### Pandemonium
**Bugfixes**
- Fixed chunk save failures and the resulting shell duplication from unloading chunks with player shells in them

------------------------------------------------------
Version 1.7.1
------------------------------------------------------
**Bugfixes**
- Fixed crash on dedicated servers
- Fixed mob ability desync under unusual circumstances

------------------------------------------------------
Version 1.7.0
------------------------------------------------------
**Additions**
- Added custom mob item overrides, [configurable through datapacks](https://ladysnake.github.io/wiki/requiem/configuration)
    - Moved some hardcoded item behaviour to this new system
- Piglins can now eat pork, and only pork.
    - They also have a food bar now
    
**Mod Compatibility**
- LuckPerms: The `/requiem` command now has some configured permission nodes
- Optifine: The game will now refuse to launch instead of crashing with Optifabric installed

**Fixes**
- Fixed possession indicator appearing on blacklisted mobs like the wither boss
- Fixed Humanity being capped to level 1
- Fixed invalid `entity_mobility` data file causing possessed mobs to not have adequate movement options

#### Pandemonium
**Additions**
- Added a config option to disable pandemonium's ability to possess every mob
- Foxes can now eat berries to regain some health

------------------------------------------------------
Version 1.6.3
------------------------------------------------------
**Changes**
- Possessed mobs now properly handle block slowdown; eg. spiders are unaffected by cobwebs and withers are unaffected entirely
  (This change isn't noticeable without a datapack or Pandemonium)

**Mod Compatibility**
- AdventureZ/MobZ/Happiness is a Warm Gun: added compatibility for mob possession (*thanks to bartratar!*)
- Haema: blood injector use should once again be forbidden while possessing stuff

#### Pandemonium
**Fixes**
- Fixed crash at launch on dedicated servers
- Fixed sneak interactions flat out never working
- Fixed crash when merging with blank shell (created with /summon)

------------------------------------------------------
Version 1.6.2
------------------------------------------------------
#### Pandemonium
**Mod Compatibility**
- Identity: Fixed a crash when opening the Identity menu

------------------------------------------------------
Version 1.6.1
------------------------------------------------------
#### Pandemonium
- Fixed a crash when merging with a shell that was not riding anything

------------------------------------------------------
Version 1.6.0
------------------------------------------------------
**Additions**
- Added the `requiem:possessionKeepInventory` gamerule to specifically keep your inventory on your soul when possession ends

**Changes**
- Attrition now goes up twice as quickly in hardcore (fewer allowed deaths)
- The `requiem:showPossessorNameTag` no longer changes the nametag serverside
    - This allows you to keep easter egg effects when possessing named mobs with the gamerule

**Mod Compatibility**
- Origins: the `soul_type` layer now has `auto_choose` enabled by default
- Better Graves: graves no longer spawns when possession stops for any reason

**Fixes**
- Fixed armor enchantments on possessed entities
- Fixed mobs still sprinting a player stops possessing them
- Updated Cardinal Components API dependency to fix a crash at launch with latest Fabric API

#### Pandemonium
**Additions**
- Added `/pandemonium shell (create/split/identity)`
    - `create` allows you to make a shell copying a player's data
    - `split` allows you to artificially split a player as if they dissociated themselves
    - `identity set` allows you to change a shell's name and skin
- Added Wither abilities (Wither possession is still denied by default) [Thanks SekoiaTree!]

**Changes**
- Player shells are now actual (fake) players
    - This should fix a bunch of old bugs, you're welcome!
    - This may create new bugs, please report them!
- Rabbits now always jump when walking
- Mounts will be properly transferred when splitting/merging with player shells

**Fixes**
- Endermen's blink ability now plays its sound correctly
- Fixed player shells being incorrectly flagged as dead when loading chunks

------------------------------------------------------
Version 1.5.1
------------------------------------------------------
**Additions**
- Added Romanian translation (thanks AwsAlex !)

**Mod Compatibility**
- Updated Origins compatibility; the Soul Types should be back in the selection screen
- Automated Crafting: Fixed the same crash as last time, but for real
- Fixed requiem breaking Bewitchment's and Origins' climbing hooks
- Fixed some mixin incompatibilities with other mods

**Fixes**
- The /team command should no longer be broken, but for real this time
- Fixed the localization for the banishment opus' tooltip

------------------------------------------------------
Version 1.5.0
------------------------------------------------------
Kudos to SciRave for learning github and java to make a Requiem PR fixing some bugs!

**Additions**
- Added the `"FLOATING"` swim mode for possessed entities, forcing them to stay at the surface of the water
- Added the `"JUMPY"` walk mode for possessed entities, forcing them to jump when moving on land
- While phasing, souls can now see viable hosts around them glowing

**Changes**
- Wandering spirits no longer heal attrition in hardcore
- Hosts can no longer fly with an elytra. (It was buggy, looked weird, and didn't support the wings mod models.)
- The `shouldSinkInWater` movement config field has been superceded by the `"SINKING"` value for `swimMode`
- Being cured in any way (including as a piglin or a villager) now removes attrition
- Undead food now has more delay, to prevent healing too fast
- Removed the desaturation effect near other demons

**Mod Compatibility**
- Automated Crafting: Fixed a crash when using the mod

**Fixes**
- Fixed the dissociation keybind resetting each launch
- Hosts no longer despawn when teleporting long distances
- Dying as a spirit no longer removes attrition
- The team command should no longer be broken
- Entities will correctly go splat when you stop possessing them while falling
- Fixed giant hosts being unable to break blocks
- Fixed possessed mobs forgetting whether they were a baby or not when cured
- Fixed hardcore mode killing you immediately instead of giving you one more chance
- **Fixed other player's possessed entities being rendered twice** (that one's been around for longer than it should)

#### Pandemonium
**Changes**
- Slimes use the new movement settings, resulting in more faithful possession

**Fixes**
- Fixed an invalid translation key for water bottles when possessing a witch
- Fixed ravagers only breaking leaves clientside, leading to desync


------------------------------------------------------
Version 1.4.4
------------------------------------------------------
**Fixes**
- Fixed dedicated servers crashing at launch

------------------------------------------------------
Version 1.4.3
------------------------------------------------------
**Additions**
- Added the Drowned resurrection for players dead by drowning in an ocean
- Added a config file, and the screen that goes with it
- Added a config option to use an alternative rendering mode for incorporeal players in third person

**Changes**
- The zombie resurrection now requires a light level of 8 or lower
- Phasing particles are now only shown to remnant players
- Armor and capes no longer render on incorporeal players

**Mod Compatibility**
- Fixed trinkets and curios buttons appearing on inventory-less mobs

**Fixes**
- Fixed possessed mobs resurrecting as zombies when killed by zombies
- Fixed a random black screen issue when rendering phasing particles
- Fixed souls catching fire despite them not being supposed to
- Fixed mob armor and additional layers (e.g. clothes) disappearing during the zoom animation effect

#### Pandemonium
**Fixes**
- Fixed player shells being targeted by every mob that can be aggressive towards another entity
    - Tangentially fixes a crash when llama would try to target a player shell

------------------------------------------------------
Version 1.4.2
------------------------------------------------------
**Changes**
- The (still unobtainable) skeletal totem now does the totem display effect on use

**Fixes**
- Fixed resurrection flat out not working since 1.4.0

------------------------------------------------------
Version 1.4.1
------------------------------------------------------
**Changes**
- Fire immune mobs no longer show the fire overlay when possessed
- Incorporeal players can no longer get on fire
- Incorporeal players can no longer push away other entities
- Incorporeal players now spawn fewer particles when falling

**Mod Compatibility**
- Haema+Origins: Fixed a bug where becoming incorporeal with Haema's Vampire origin would clear the vampire status forever

**Fixes**
- Fixed players respawning in the Overworld instead of their death dimension
- Fixed naturally spawned zombified piglins reverting to regular piglins instead of cured piglins
- Fixed a desync issue with ability cooldowns
- Fixed wandering spirits not respawning correctly when they died after getting a player body somehow

**API**
- Possibly fixed some remapping issues when depending on Requiem

#### Pandemonium
**Changes**
- Striders can now walk on lava
- The witch attack range has been tweaked to reflect the actual range

**Fixes**
- Fixed witches' instant health never applying

------------------------------------------------------
Version 1.4.0
------------------------------------------------------
### The Chilly Shades Update

**Additions**
- Added the Wandering Spirit remnant state
    - Wandering spirits are a variant of remnant players that cannot regenerate a player body
    - They can however leave a mob at any time without killing it using the Dissociation keybind
    - They also cure attrition on their own after enough time spent possessing a body
        - In hardcore, leaving the body will give you back the attrition
- Added an Opus Daemonium to become a Wandering Spirit
    - Type "Ad Vitam Vagrate" as a sentence into an empty Opus to turn it into an Opus Daemonium of Banishment
- Some mobs now get cured into a new mob instead of being assimilated into a player body
    - Zombie villagers get cured into possessable villagers
    - Zombified piglins get cured into piglins
        - Zombified brutes go back to being brutes after the cure
        - Cured piglins are immune to overworld zombification
- Incorporeal players now have free night vision
- Added the Totem of Skeletonization (currently unobtainable)
    - When you die while holding a Totem of Skeletonization, you revive as a skeleton
    - This effect also applies to possessed mobs

**Changes**
- The `/requiem remnant set` command can now use remnant type identifiers
- Using an Opus Daemonium during the curing process of a possessed mob will now expedite it on top of the usual effect
- You can now dissociate from golems at any time using the Dissociation keybind
- Changed the texture for locked slots while possessing a mob or playing as a free soul
- Functional tags are now organized into folders
- Swapping all bones of a wither skeleton may now leave you with its head
- Golems and undead mobs (except the drowned) now sink in water
- Snow golems can now shoot snowballs through direct attack on top of indirect use
- Tweaked the texture for the ability target indicator

**Mod Compatibility**
- Snow Mercy: All weaponized snow golems can now be possessed. May the festivities start!
- Origins: When dissociated from a player body, your origin now gets temporarily changed to Vagrant
  - The vagrant origin gives absolutely no power, it's just there to tell you why you can't use them
- Origins: Added the Wandering Spirit as an origin (on the origin layer, as it effectively prevents you from using another origin's powers)
- Haema: vampirism is now disabled when vagrant (out of a player body)
- BetterEnd/BetterNether: now marked as conflicting because they break the Supercrafter ability

**Fixes**
- Fixed a random crash at launch with Bedrockify
- Fixed doctor4t's capitalization in fabric.mod.json
- Possibly? fixed an issue where player attributes (notably speed) would get funky values when reviving
- Fixed Opus Daemonium items not having the enchantment glint
- Fixed Attrition not displaying immediately after respawn
- Fixed the Dissociation (ethereal fracture) keybind localization
- Fixed absorption hearts not displaying while possessing an entity
- Fixed armor-less possessed mobs being able to equip armor through right-click
- Fixed attrition flashing out of existence periodically during possession

**API**
- The API jar is once more available on Bintray
- `isSoul`/`setSoul` methods have been renamed to `isVagrant`/`setVagrant`
- A bunch of experimental methods have been added, no forward compatibility guarantee for those

#### Pandemonium
**Additions**
- Wandering spirits can take over player shells

**Changes**
- Vexes no longer have an inventory and armor, only held items
- Player shells will now increment the attrition of the corresponding player when killed

**Mod Compatibility**
- Origins: Player shells now store a player's origin
    - Origins' graphical effects are visible on the shell
- Haema: Player shells now store a player's vampirism status

**Fixes**
- Fixed some data being overwritten when merging with a player shell
- Impersonation through body swapping now works more or less as intended
- Players merging with a shell now get properly teleported to the shell's location
- Fixed piglins and villagers being literally braindead after possession stops
- Fixed ranged attacks not triggering the ability cooldown

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

# Requiem

![](https://raw.githubusercontent.com/Ladysnake/Requiem/fabric/requiem-logo-200x125.png)

![](https://img.shields.io/github/stars/ladysnake/dissolution.svg) ![](https://img.shields.io/github/forks/ladysnake/dissolution.svg) ![](https://jitpack.io/v/ladysnake/dissolution.svg) ![](https://img.shields.io/github/issues/ladysnake/dissolution.svg)


**Table of Contents**

# Introduction

## What is Requiem?
Requiem is a Minecraft (Java Edition) mod aiming at removing the continuity break resulting from death.
In vanilla Minecraft, dying is bland and can be frustrating: you lose all your items and get redirected to a menu where you can choose to reappear at your spawn point or quit the game.
With Requiem installed however, this menu breaking immersion is removed and dying gives place to various gameplay elements.

## Why would you want to use it?
As explained above, Requiem is great for players willing to make death a little different from most games. Additionally, Requiem has other benefits and features that other players can enjoy:
- Explorers wanting to venture far in their world, only to be retained by their death leading back to their respawn point. As such, Requiem allows an infinite journey, so you don't have to walk the same path twice.
- Players searching to change their playstyle by roleplaying an undead, fleeing sunlight, drowning endlessly in the ocean or eventually searching for preys to feast on.
- And players wanting to experience this new Death, while having friends not willing to. The mod allows both death systems, Vanilla and Requiem, to coexist in the same world at the same time, depending on each player's needs.

## Getting started
- Running the mod will require Fabric and Fabric API. For more information on how to use Fabric, [please visit the official website](https://fabricmc.net/). To download Fabric API, [please visit the official Curse Forge page](https://minecraft.curseforge.com/projects/fabric).
- If Fabric is installed on your Minecraft Java copy, [you can download Rquiem from Curse Forge](https://minecraft.curseforge.com/projects/requiem) and copy the jar file obtained, as well as the download Fabric API jar file, in the mods folder located in you Minecraft installation or Fabric Server folder. Please make sure to have the latest version, as we will not accept issues regarding older versions.
- Once the previous steps have been completed, boot up your game, and upon your first death, Requiem will asks you whether you'd like to enable the modified death system or not through an interactive dialogue. Be careful, because after choosing, the dialogue won't be prompted a second time, and you will have to use a special item to change that.

# Wiki
This section lists and explains the different mechanics and items Requiem adds to the game. If you need information about something you did not understand, this is where to look!

## Gameplay mechanics

### First death
Upon dying for the first time in a world, a dialogue will be prompted to the player. In this menu, the player can select whether or not he wants to become a demon, and activate the mod for himself or not. After choosing, the player obtains the "A Devilish Conundrum" and the dialogue will not be prompted anymore.
However, the player can still change their mind by using an Opus Daemonium at a later time.

### Soul state
If the player is a demon and dies, their demon souls splits from the host's and escapes the destroyed body.
In this state, the player can easily fly and navigate the world in search of a new host to possess. The player is granted creative fly and can use the sprint key to navigate in the air (similarly to how a player would swim ) and go through one block tall holes.
To possess a new body and leave the soul state, the player must find an undead mob with an inventory and interact with them (a crimson eye should render upon placing the cursor on an entity). The list of the valid undead Vanilla Minecraft mobs is as follows:
- Skeleton
- Stray
- Wither Skeleton
- Zombie
- Husk
- Zombie Villager
- Zombie Pigman
- Drowned

### Body transformation after death
In some cases, the player's body might transform after death, giving birth to an undead creature. In those few situations, the player's soul is not expelled from the dying body, and keeps full control of it.
The list of events that lead to a body transformation after death is as follows:
- Dying from a Zombie or a Drowned will turn the player into a Zombie
- Dying in lava while being in the Nether will turn the player into a Wither Skeleton
- Dying from suffocation by sand will turn the player into a Husk
- As Vanilla game behaviour guarantees, staying too long underwater as a Zombie will turn the player into a Drowned

### Undead gameplay
After finding a new host or transforming after death, the player is forced to play as an undead creature. Undead players behave very similarly to undead mobs, meaning they both have their strengths and weaknesses. For instance, a Stray player will shoot slowness arrows, but will also burn if exposed to sunlight.
All undead players:
- Are slower than human players
- Do not have hunger, but can sprint nonetheless
- Do not suffocate underwater and cannot swim (unless possessing a Drowned).
- Do not naturally regenerate health. Please read the "Special item uses for Undead" section for information on how to heal in other ways.
- Are ignored by hostile mobs, unless the player attacks them, but will be targeted by golems.
- Do not receive any experience from combat, but will still be able to obtain some from other sources (such as mining, fishing and breeding animals).
- Do not have the strength to swing their sword in a manner to do area damage.
- Receive damage from instant health potion and health from instant damage potions.

### Special item uses for Undead
Depending on the undead possessed, the player is able to use items differently from how he would if he was human.
#### Raw meat ("Zombie Snack")
Raw meat can be eaten by Zombie players (all sub-types included) to regenerate health. A piece of meat regenerates as much health as it would recover food for a human player.
#### Raw fish ("Drowned Delicacy")
Raw fish can eaten by Drowned exclusively, and serves the same purpose raw meat does.
#### Bones ("Bony Prosthesis")
Skeletons (all sub-types included) can replace their damaged bones by new ones. To do so, just use a bone and it will regenerate 4 health points (2 hearts).
#### Milk buckets ("Calcium Goodness")
Skeletons (all sub-types included) can drink milk for extra bone resistance. Drinking a milk bucket grants the player Resistance for 30 seconds.
#### Bows ("Skeletal Efficiency")
Using a bow as a Skeleton (all sub-types included) has a chance to not consume an arrow when shooting. Resulting arrows are not recoverable.
#### Tridents ("Drowned Grip")
Throwing a trident as a Drowned has a chance to keep it in hand and duplicate it. Resulting tridents are not recoverable.

### Becoming human again
To recover their humanity and get rid of their rotting body, an undead player has to go through the purification progress every Zombie Villager must. By getting the Weakness effect, undead players are able to eat Golden Apples. Doing so results in the player turning back into human form.

## Items
Below can be found all the items added by Requiem, their uses and how to obtain them.

### Opus Daemonium
The Opus Daemonium is an item that allows a Player to activate or deactivate Requiem's death mechanics (for himself only), by respectively awakening or destroying a Player's demon soul.
There are 3 variants of Opus Daemoniums:
- Neutral: The base Opus Daemonium to obtain the two other variants. Can be opened to write inside it. If the phrase is a valid incantation, the player can sign it and use 5 experience levels in order to convert the Opus into either a Curse or Cure Opus.
- Curse: Activates the mod's death system when used. Obtained by writing "Ad Vitam Aeternam" inside a neutral Opus Daemonium.
- Cure: Deactivates the mod's death system when used. Obtained by writing "Ad Vitam Mortale" inside a neutral Opus Daemonium.

**Recipe (shapeless):**

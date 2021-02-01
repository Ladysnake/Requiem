# Memento Mori

![](https://raw.githubusercontent.com/Ladysnake/Requiem/master/requiem-logo-200x125.png)

[![Download](https://curse.nikky.moe/api/img/265729?logo)](https://curse.nikky.moe/api/url/265729) ![](https://jitpack.io/v/ladysnake/dissolution.svg) ![](https://img.shields.io/github/issues/ladysnake/requiem.svg)

Memento Mori is a fork of Requiem that adjusts the balancing for a semi-hardcore experince. It also does some bug fixes.

**Changes:**

* Attrition caries through all types of death and the effect decreases by one level every 20 minutes in a human body. No more avoiding the mod by stockpiling on resources.

* Host-specific healing items give Hunger I for 30 seconds, during this period you cannot consume said items.

* Souls with Attrition V and above can no longer possess a body. Basically a hardcore death.

**Fixes:**
* Souls no longer lose attrition when dying from void damage.
* Milk buckets no longer cure attrition levels when human.
* Hosts no longer get deleted when teleporting large distances.


More information on Requiem is available on the Ladysnake Website: https://ladysnake.github.io/wiki/requiem.

## Installation

### For users

- Running the mod will require Fabric and Fabric API. For more information on how to use Fabric, [please visit the official website](https://fabricmc.net/). To download Fabric API, [please visit the official CurseForge page](https://minecraft.curseforge.com/projects/fabric).
- If Fabric is installed on your Minecraft Java copy, you can download Requiem from [CurseForge](https://minecraft.curseforge.com/projects/requiem), [Github Releases](https://github.com/Ladysnake/Requiem/releases), or [Modrinth](https://modrinth.com/mod/requiem). After that, copy the jar file obtained, as well as the download Fabric API jar  file, in the mods folder located in you Minecraft installation or Fabric Server folder. Please make sure to have the latest version, as we will  not accept issues regarding older versions.
- Once the previous steps have been completed, boot up your game, and  upon your first death, Requiem will asks you whether you'd like to  enable the modified death system or not through an interactive dialogue. Be careful, because after choosing, the dialogue won't be prompted a  second time, and you will have to use a special item to change that.

## For developers

If you wish to develop compatibility features for Requiem, binaries are available both on the [Ladysnake bintray](https://bintray.com/ladysnake/mods/requiem/) and Jcenter. You can get it in your workspace with just a few lines in your Gradle buildscript (`build.gradle` file) :

```gradle
repositories {
    maven {
        name = 'Ladysnake Mods'
        url = 'https://dl.bintray.com/ladysnake/mods'
    }
}

dependencies {
    modImplementation "io.github.ladysnake:requiem-api:${requiem_version}"
    modRuntime "io.github.ladysnake:requiem:${requiem_version}"
}
```

You can then add the mod version to your `gradle.properties` file :
```properties
# Requiem mod
requiem_version = 1.x.y
```


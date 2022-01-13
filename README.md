# Requiem

![](https://raw.githubusercontent.com/Ladysnake/Requiem/master/requiem-logo-200x125.png)

[![Download](https://curse.nikky.moe/api/img/265729?logo)](https://curse.nikky.moe/api/url/265729) ![](https://jitpack.io/v/ladysnake/dissolution.svg) ![](https://img.shields.io/github/issues/ladysnake/requiem.svg)

Requiem is a Minecraft (Java Edition) mod aiming at removing the continuity break resulting from death,
as well as adding new mechanics centered around soul dissociation and body possession.

More information is available on the Ladysnake Website: https://ladysnake.github.io/wiki/requiem.

## Installation

### For players

- Running the mod will require Fabric and Fabric API. For more information on how to use Fabric, [please visit the official website](https://fabricmc.net/). To download Fabric API, [please visit the official CurseForge page](https://minecraft.curseforge.com/projects/fabric).
- If Fabric is installed on your Minecraft Java copy, you can download Requiem from [CurseForge](https://minecraft.curseforge.com/projects/requiem), [Github Releases](https://github.com/Ladysnake/Requiem/releases), or [Modrinth](https://modrinth.com/mod/requiem). After that, copy the jar file obtained, as well as the download Fabric API jar  file, in the mods folder located in you Minecraft installation or Fabric Server folder. Please make sure to have the latest version, as we will  not accept issues regarding older versions.
- Once the previous steps have been completed, boot up your game, and  upon your first death, Requiem will asks you whether you'd like to  enable the modified death system or not through an interactive dialogue. Be careful, because after choosing, the dialogue won't be prompted a  second time, and you will have to use a special item to change that.

### For developers

If you wish to develop compatibility features for Requiem, binaries are available both on the [Ladysnake artifactory](https://ladysnake.jfrog.io/artifactory/mods/io/github/ladysnake/requiem/).
You can get it in your workspace with just a few lines in your Gradle buildscript (`build.gradle` file) :

```gradle
repositories {
    maven {
        name = 'Ladysnake Mods'
        url = 'https://ladysnake.jfrog.io/artifactory/mods'
        content {
            includeGroup 'io.github.ladysnake'
            includeGroupByRegex 'io\\.github\\.onyxstudios.*'
            includeGroupByRegex 'dev\\.emi.*'
        }
    }
    maven {
        name = 'Nexus OSS Repository'
        url = 'https://oss.sonatype.org/content/repositories/snapshots'
    }
    maven {
        url = 'https://maven.jamieswhiteshirt.com/libs-release/'
        content {
            includeGroup 'com.jamieswhiteshirt'
        }
    }
}

dependencies {
    modImplementation "io.github.ladysnake:requiem-api:${requiem_version}"
    modRuntimeOnly "io.github.ladysnake:requiem:${requiem_version}"
}
```

You can then add the mod version to your `gradle.properties` file :
```properties
# Requiem mod
requiem_version = 2.x.y
```


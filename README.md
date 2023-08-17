# Requiem

<p align="center">
    <picture>
        <source media="(prefers-color-scheme: dark)" srcset="banners/requiem_logo_light.png" width="250">
        <img src="banners/requiem_logo_200x125.png" width="250">
    </picture> 
</p>

[![Download](https://curse.nikky.moe/api/img/265729?logo)](https://curse.nikky.moe/api/url/265729) ![](https://jitpack.io/v/ladysnake/dissolution.svg) ![](https://img.shields.io/github/issues/ladysnake/requiem.svg)

Requiem is a Minecraft (Java Edition) mod aiming at removing the continuity break resulting from death,
as well as adding new mechanics centered around soul dissociation and body possession.

More information is available on the Ladysnake Website: https://ladysnake.org/wiki/requiem.

## Installation

### For players

-   Running the mod will require Quilt and Quilted Fabric API. For more information on how to use Quilt, [please visit the official website](https://quiltmc.org/). To download Quilted Fabric API, please visit the official [CurseForge](https://www.curseforge.com/minecraft/mc-mods/qsl) or [Modrinth](https://modrinth.com/mod/qsl) page.

-   If Quilt is installed on your Minecraft Java copy, you can download Requiem from [CurseForge](https://minecraft.curseforge.com/projects/requiem), [Github Releases](https://github.com/Ladysnake/Requiem/releases), or [Modrinth](https://modrinth.com/mod/requiem). After that, copy the jar file obtained, as well as the Quilted Fabric API jar file in the mods folder located in you Minecraft installation or your Server's Mods folder. Please make sure to have the latest version, as we will not accept issues regarding older versions.

-   Once the previous steps have been completed, boot up your game, and  upon your first death, Requiem will ask you whether you'd like to  enable the modified death system or not through an interactive dialogue. Be careful, because after choosing, the dialogue won't be prompted a second time, and you will have to use a special item to change that.

### For developers

If you wish to develop compatibility features for Requiem, binaries are available both on the [Ladysnake Maven](https://maven.ladysnake.org). You can get it in your workspace with just a few lines in your Gradle buildscript (`build.gradle` file) :

```gradle
repositories {
    maven {
        name = 'Ladysnake Mods'
        url = 'https://maven.ladysnake.org/releases'
        content {
            includeGroup 'io.github.ladysnake'
            includeGroupByRegex '(dev|io\\.github)\\.onyxstudios.*'
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
    modRuntimeOnly "io.github.ladysnake:requiem-core:${requiem_version}"
    modRuntimeOnly "io.github.ladysnake:requiem:${requiem_version}"
}
```

You can then add the mod version to your `gradle.properties` file :

```properties
# Requiem mod
requiem_version = 2.x.y
```

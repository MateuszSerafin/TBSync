This branch is unmaintained, this was tested on "Space-Tech" modpack it was working. <br>
However there will be some changes going forward.
<img src="https://techblock.pl/storage/img/tblogov2svg.svg" width="40" height="40" align="top">Sync
================
This is an API to synchronize mods between servers, we use it for our modpacks<br>
We also decided to preload player data rather than try loading it after players join <br>
## Recommended Usage
### Synchronization process
#### Loading
>Load world via your plugin/mod -> Call my Managers (Players, Party, World) to load from DB
#### Saving
>Call in my managers local backup -> Call in my managers to save to DB -> Call cleanup for each mod used -> Unload world via your plugin/mod
### Restore from local backup
#### Full
>Load Backup -> Save to DB
#### Partial
>Load from db -> Load backup (with the mod that data loss occured as argument) -> save to DB

## Configuration
>Requires connection to MariaDB, edit config/TBSync.conf and it should be working nothing else is required

## Player
| Mod               | Implementation                                                                                                                                                                                                                                                                                                                                                                 | Can be loaded while player is online                                      |
|-------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| Astral            | Injecting data to research helper it looks like it handles everything (perks, researches, knowledge)                                                                                                                                                                                                                                                                           | Yes                                                                       |
| Flux Networks     | Each player has 3 networks, network ID's cannot duplicate meaning if player has network ID 1 on server A on server B it must also be network ID 1, otherwise I load or unload the network, when network is unloaded it leaves blocks in a state where they show they are disconnected from the network but after loading the network and reloading the chunk it works normally | No, maybe with mod synchronization packet and chunk reload it would work. |
| Cosmetic Armor    | Nothing interesting just loading, unloading data                                                                                                                                                                                                                                                                                                                               | Yes                                                                       |
| Future Pack       | Again nothing interesting loading unloading data                                                                                                                                                                                                                                                                                                                               | Yes                                                                       |
| EnderStorage      | Only private player chests,tanks are synchronized                                                                                                                                                                                                                                                                                                                              | Yes, for tanks the texture bugs while cleaning up                         |
| EnderChests       | Only private player chests are synchronized                                                                                                                                                                                                                                                                                                                                    | Yes                                                                       |
| EnderTanks        | Only private player tanks are synchronized                                                                                                                                                                                                                                                                                                                                     | Yes                                                                       |
| ForgeCapabilities | I change data in player.dat, other way would be to manually write mixin for each mod that uses capability so this is bad solution but it works and is easy                                                                                                                                                                                                                     | Absolutely Not                                                            |

## Party
| Mod      | Implementation                                                                                                                                                                                                                                                                                                    | Can be loaded while players are online |
|----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------|
| FTBTeams | I disabled manual creation of teams, invitations, transfer of ownership kick etc.., we use it with island plugin, so when player creates an island the island plugin handles who is on the island and it adds everyone accordingly. Also only parties are synchronized PlayerTeams wont work and Server teams too | No, would need synchronization packet  |
| FTBQuest | Nothing interesting just loading, saving party UUID                                                                                                                                                                                                                                                               | No, would need synchronization packet  |

## World
>For each world mod make sure your island plugin leaves the same world name on a different server otherwise it won't work, and it would be too much work to adjust world names

| Mod      | Implementation              | Can be loaded while players are online |
|----------|-----------------------------|--------------------------------------|
| XNet     | Simple loading, saving data | Yes |

## Building
>Clone <br>
Add dependencies to libs folder <br>
run reobfShadowJar <br>
.jar should be in build/reobfShadowJar

## Going forward
>This will be rewritten to some kind of commons so it can be used between versions 

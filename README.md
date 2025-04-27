<img src="https://techblock.pl/storage/img/tblogov2svg.svg" width="40" height="40" align="top">Sync
================
This is an API to synchronize mods between servers, we use it for our modpacks <br>
We also decided to preload player data rather than try loading it after players join. <br>

## Recommended Usage
After successful run on our "Space-Tech" modpack. I decided to fully remove loading and saving to database from this side. <br>
You need to write yourself plugin/mod. I recommend to load all player and world and party data before world loads. <br>
There will be a sample usage of this mod. But it won't be for 1.16.5 and I am in process of writing it. <br>

## Configuration
Edit ``TBSync.yaml`` <br>
Configure SQL database if using FluxNetworks. <br>
And configure which mixins you want to load. <br>
At this point you can use it in your mod.

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
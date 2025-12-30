<img src="https://techblock.pl/storage/img/tblogov2svg.svg" width="40" height="40" align="top">Sync
================
Some mods such as Enderstorage, FluxNetworks FTBTeams etc. don't load/save data per world meaning if you run server with nodes and players can load worlds on different sub-servers leading to things not loading fully/correctly/at all requiring players to re-setup things again, quests not working or other weird issues. <br>
I am providing in this repo mixins and API where you can synchronize this stuff. This was tested on our mod packs. <br>
Warning: I am supporting only mods that are required on our modpacks. Additionally, this does nothing without integrating to your case.

## Example Usage
1. Copy Mod to server, start it. Configure ``config/TBSync.yaml``
    ```
    debug: false
    jbdc: "jdbc:mariadb://(ip of server):3306/(database name)?user=(user)&password=(password)&autoReconnect=true&useUnicode=true&characterEncoding=UTF8"
    
    # Each mixin might behave differently read on GitHub what each does
    Enabledmixins:
    enderstorage: false
    fluxnetworks: false
    ftbmixins: false
    xnet: false
    ae2: false
    
    Locales:
    #%s is owner
    FTBTeamNames: "Druzyna Gracza %s"
    FTBonTransferOwnership: "Nie mozesz tego zrobic"
    FTBonLeave: "Musisz opuscic wyspe aby to zrobic"
    FTBonInvite: "Jezeli gracz dolaczy do twojej wyspy zostanie dodany automatycznie"
    FTBonKick: "Jezeli usuniesz gracza z wyspy stanie sie to automatycznie"
    
    privateFluxNetworksPerPlayer: 4
    ```
2. Write mod/plugin that integrates that to your use case. <br>
   In our case we use this [TBConnector](https://github.com/MateuszSerafin/TBConnector/)
3. **Should** be working


## Implementation
### Player
| Mod          | Implementation                                                                                                                                                                                                                       | Can load/save/cleanup when player is already on server | Problems                 |
|--------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|--------------------------|
| EnderStorage | Saving, Loading is fully disabled, My code saves/loads/cleanups only private chests,tanks                                                                                                                                            | Yes                                                    | For tanks, texture bugs. |
| FluxNetworks | Saving, Loading is fully disabled, My code saves/loads/cleanups all networks that the player is owner of. Additionally SQL is required for that each network has UniqueID, meaning for each sub-server it needs to match per player. | No, chunk reload is needed                             | Nothing reported         |

### Party
| Mod       | Implementation                                                                                                                                                                                                                    | Can load/save/cleanup when player is already on server | Problems          |
|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|-------------------|
| FTBTeams  | Saving, Loading is fully disabled, Additionally players cannot transfer teams, kick and add members. In our case we add members to team when someone joins island, kick from island etc. Saving is not required.                  | No                                                     | Nothing reported |
| FTBQuests | Saving, Loading is fully disabled, Loading and Saving is really simple. Players by default are added to "Player Team", there is mixin that fully disables quest rewards unless the team was created by calling my load for team.  | No                             | Nothing reported  |

### World
Make sure to use correct format for worlds when using my code e.g "minecraft:overworld".

| Mod          | Implementation                                                                                       | Can load/save/cleanup when player is already on server             | Problems                                                                                                                                                                                                 |
|--------------|------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| XNet         | Saving, Loading is fully disabled, I load/save data that would be normally done when loading server. | No, chunk reload would be required | When cleaning up you can't see network on Controller which is expected but the network still works and stops when chunk is reloaded. (Cleanup should be used after world unload so not a problem anyway) |


### Misc (data can't be preloaded easily)
| Mod       | Implementation                                                                                                                                                                           | Can load/save/cleanup when player is already on server | Problems                                                                                                                                                                                                                                                                                                                         |
|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AE2 - P2P | P2P uses random number generator to assign ID's to P2P tunnels, When collision occurs P2P tunnel dies. Random number generator is replaced with SQL table that is shared between servers | N/A                                                    | Could be implemented in a better way, Database is only queried once per p2p block and once it has it's ID it stores it in block data, current implementation could be laggy as server will wait for database to finish the query blocking the main server thread. At the same time it's only once per block so it should be fine |

## Building
Clone <br>
Add dependencies to libs folder <br>
run ShadowJar <br>
.jar should be in build/libs
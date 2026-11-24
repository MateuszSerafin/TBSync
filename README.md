TBSync
================
## The Problem
[TechBlock](https://techblock.pl/) is running a proprietary world loading plugin, where an island can be loaded on Node 1, Node 2, Node X <br>
Because mods are mostly written to be used on one server (and multiple nodes were used). There were problems with loading mod specific data for example FTBQuests which saved data somewhere in world folder <br>
TBSync is a "very simple" synchronization layer that uses Mixins to redirect where data is saved and loaded. 

## Current State
This project was a proof of concept. As previously data was synchronized differently (on which players could duplicate items easily) <br>
This has managed to handle around 150 concurrent players without nuking their data and causing lag (aside of one incident where someone deleted one mod, and I was not checking if mods were actually loaded causing Nulls to be inserted and whole DB got nuked) <br>
Currently, this project is not managed by me and got forked into private repository (actively used). 

## Recommendations
This repository is a great starting point as this is a skeleton for mod synchronization between servers that worked <br>
It's not ideal as this is not a full synchronization between servers as it would be extremely difficult, but for a case where players have their own islands it did its job really well <br>
Currently, LLMs even the local ones can manage to decompile mods and find what needs to be synchronized (this is effectively what is done currently). 

Here are my top picks if someone will pick this repo <br>

| Recommendation                   | Description                                                                                                                                                                                                                                                                                                                                                                    |
|----------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Make version control on database | Currently, all data is upserted to DB meaning one bad write breaks players data. I think it's possible quite easily to do inserts (and player data is not so heavy so it would be a problem) effectively allowing to have a version control so that player data can be rolled back if something dies                                                                           |
| Forge Capabilities               | I did not manage to find a way to synchronize capabilities effectively, the "best" way was to just copy player.dat and mirror the file on another server to synchronize the capabilities. The actual problem is that capabilities load data on fly and there is no way to Mixin a preload into it (perhaps LLM can find it but orginally this project was done fully manually) |

Note: [TBConnector](https://github.com/MateuszSerafin/TBConnector) is a Bukkit <-> TBSync bridge, the proprietary plugin at some point used multiverse to load worlds so worth to take a look as an example

## Implementation
### Player
| Mod          | Implementation                                                                                                                                                                                                                       | Can load/save/cleanup when player is already on server | Problems                 |
|--------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|--------------------------|
| EnderStorage | Saving, Loading is fully disabled, My code saves/loads/cleanups only private chests,tanks                                                                                                                                            | Yes                                                    | For tanks, texture bugs. |
| FluxNetworks | Saving, Loading is fully disabled, My code saves/loads/cleanups all networks that the player is owner of. Additionally SQL is required for that each network has UniqueID, meaning for each sub-server it needs to match per player. | No, chunk reload is needed                             | Nothing reported         |

### Party
| Mod       | Implementation                                                                                                                                                                                                                   | Can load/save/cleanup when player is already on server | Problems         |
|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|------------------|
| FTBTeams  | Saving, Loading is fully disabled, Additionally players cannot transfer teams, kick and add members. In our case we add members to team when someone joins island, kick from island etc. Saving is not required.                 | No                                                     | Nothing reported |
| FTBQuests | Saving, Loading is fully disabled, Loading and Saving is really simple. Players by default are added to "Player Team", there is mixin that fully disables quest rewards unless the team was created by calling my load for team. | No                                                     | Nothing reported |

### World
Make sure to use correct format for worlds when using my code e.g "minecraft:overworld".

| Mod  | Implementation                                                                                       | Can load/save/cleanup when player is already on server | Problems                                                                                                                                                                                                 |
|------|------------------------------------------------------------------------------------------------------|--------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| XNet | Saving, Loading is fully disabled, I load/save data that would be normally done when loading server. | No, chunk reload would be required                     | When cleaning up you can't see network on Controller which is expected but the network still works and stops when chunk is reloaded. (Cleanup should be used after world unload so not a problem anyway) |


### Misc (data can't be preloaded easily)
| Mod       | Implementation                                                                                                                                                                           | Can load/save/cleanup when player is already on server | Problems                                                                                                                                                                                                                                                                                                                         |
|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AE2 - P2P | P2P uses random number generator to assign ID's to P2P tunnels, When collision occurs P2P tunnel dies. Random number generator is replaced with SQL table that is shared between servers | N/A                                                    | Could be implemented in a better way, Database is only queried once per p2p block and once it has it's ID it stores it in block data, current implementation could be laggy as server will wait for database to finish the query blocking the main server thread. At the same time it's only once per block so it should be fine |


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

## Building
Clone <br>
Add dependencies to libs folder <br>
run ShadowJar <br>
.jar should be in build/libs
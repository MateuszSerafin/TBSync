package pl.techblock.sync.mixins.futurepack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import futurepack.common.research.PlayerDataLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.logic.mods.duckinterfaces.IFuturePackCustom;
import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Mixin(PlayerDataLoader.class)
public abstract class FuturePackMixin implements IFuturePackCustom {

    @Shadow(remap = false)
    private HashMap<UUID, PlayerDataLoader.CloseableCollection> profileMap;

    @Overwrite(remap = false)
    private void loadResearchFolder(File dir){
        return;
    }


    @Overwrite(remap = false)
    private void saveResearchFile(UUID uuid, PlayerDataLoader.CloseableCollection list) throws FileNotFoundException, IOException {
        return;
    }

    @Override
    public void readCustom(UUID playerUUID, InputStream inputStream) {
        try {
            GZIPInputStream in = new GZIPInputStream(inputStream);
            JsonReader read = new JsonReader(new InputStreamReader(in));
            JsonArray arr = new Gson().fromJson(read, JsonArray.class);
            read.close();
            ArrayList<String> list = new ArrayList<String>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                list.add(arr.get(i).getAsString());
            }
            profileMap.put(playerUUID, new PlayerDataLoader.CloseableCollection(playerUUID, list));
        } catch (Exception e){
            TBSync.getLOGGER().error("A problem with Future Pack mixin read custom probably something with json");
            e.printStackTrace();
        }
    }

    @Override
    @Nullable
    public ByteArrayOutputStream writeCustom(UUID pUUID) {
        try {
            PlayerDataLoader.CloseableCollection collection = profileMap.get(pUUID);
            //no idea i feel like it will happen often
            if (collection == null) {
                return null;
            }

            JsonArray arr = new JsonArray();
            for (String s : collection)//bypass closeable collection to prevent timer reset
            {
                arr.add(new JsonPrimitive(s));
            }

            ByteArrayOutputStream thisIsActualOut = new ByteArrayOutputStream();
            GZIPOutputStream out = new GZIPOutputStream(thisIsActualOut);
            JsonWriter write = new JsonWriter(new OutputStreamWriter(out));
            new Gson().toJson(arr, write);
            write.close();
            out.finish();

            return thisIsActualOut;
        } catch (Exception e){
            TBSync.getLOGGER().error("A problem with Future Pack mixin write custom probably something with json");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void cleanup(UUID playerUUID) {
        profileMap.remove(playerUUID);
    }
}

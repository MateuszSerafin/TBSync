package pl.techblock.sync;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

public class MixinConnector implements IMixinConnector {
    @Override
    public void connect() {
        try {
            File configFile = new File(new File(".") + "/config/TBSync.yaml");

            if (!configFile.exists()) {
                InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("TBSync.yaml");
                Files.copy(resourceAsStream, configFile.toPath());
                resourceAsStream.close();
            }

            Yaml yaml = new Yaml();

            InputStream configStream = new FileInputStream(configFile);

            TBSyncConfig.config = yaml.load(configStream);

            configStream.close();

            System.out.println(TBSyncConfig.config.get("Enabledmixins"));
            Map<String, Boolean> enabledMixins = (Map<String, Boolean>) TBSyncConfig.config.get("Enabledmixins");

            for (Map.Entry<String, Boolean> stringStringEntry : enabledMixins.entrySet()) {
                if (!stringStringEntry.getValue()) continue;
                String mixinConfig = "mixins/" + stringStringEntry.getKey() + ".tbsync.json";
                Mixins.addConfiguration(mixinConfig);
            }

        } catch (Exception e) {
            System.out.println("Unable to load config for TBSync this is catastrophic will crash.");
            e.printStackTrace();
        }
    }
}
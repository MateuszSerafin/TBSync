package pl.techblock.sync;

import net.minecraftforge.common.ForgeConfigSpec;

public class TBSyncConfig {
    public static final ForgeConfigSpec GENERAL_SPEC;

    public static ForgeConfigSpec.ConfigValue<String> JBDCString;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        JBDCString = builder.comment("MariaDB Connection").define("JDBCString", "jdbc:mariadb://(ip of server):3306/(database name)?user=(user)&password=(password)&autoReconnect=true&useUnicode=true&characterEncoding=UTF8");
    }
}

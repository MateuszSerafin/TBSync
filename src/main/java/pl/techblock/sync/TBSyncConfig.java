package pl.techblock.sync;

import java.util.Map;

public class TBSyncConfig {

    public static Boolean debug;

    public static String dataBaseConnection;
    public static Map<String, Boolean> enabledMixins;
    public static Map<String, String> locales;

    //per mod configuration past this point
    public static Integer privateFluxNetworksPerPlayer = 4;
}
package com.ea.enums;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum MapMoHH2 {

//    Modes:
//    2 - Infiltration
//    7 - Team Deathmatch
//    8 - Deathmatch

    PORT_DM("MAP1", "101", "8"),
    PORT_TDM("MAP2", "101", "7"),
    CITY_DM("MAP3", "201", "8"),
    CITY_TDM("MAP4", "201", "7"),
    SEWERS_DM("MAP5", "301", "8"),
    SEWERS_TDM("MAP6", "301", "7"),
    SEWERS_INF("MAP7", "301", "2"),
    VILLAGE_DM("MAP8", "401", "8"),
    VILLAGE_TDM("MAP9", "401", "7"),
    VILLAGE_INF("MAP10", "401", "2"),
    MONASTERY_DM("MAP11", "501", "8"),
    MONASTERY_TDM("MAP12", "501", "7"),
    BASE_DM("MAP13", "601", "8"),
    BASE_TDM("MAP14", "601", "7");

    public final String code;
    public final String decimalId;
    public final String modeId;
    private static final Map<String, List<String>> mapStatId = new HashMap<>();

    static {
        mapStatId.put("0", List.of("101")); // Port
        mapStatId.put("2", List.of("201")); // City
        mapStatId.put("4", List.of("301")); // Sewers
        mapStatId.put("7", List.of("401")); // Village
        mapStatId.put("10", List.of("501")); // Monastery
        mapStatId.put("12", List.of("601")); // Base
    }

    MapMoHH2(String code, String decimalId, String modeId) {
        this.code = code;
        this.decimalId = decimalId;
        this.modeId = modeId;
    }

    public static String getMapStatId(String key) {
        for (Map.Entry<String, List<String>> entry : mapStatId.entrySet()) {
            if (entry.getValue().contains(key)) {
                return entry.getKey();
            }
        }
        return "0";
    }
}
package com.ea.enums;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum MapMoHH {

//    Modes:
//    1 - Domination = Capture and hold
//    2 - Infiltration = Capture The Flag
//    3 - Demolition
//    4 - Hold the Line = King Of the Hill
//    5 - Battle Lines
//    8 - Deathmatch

    HOLLAND_CITY_DOM("MAP11", "211", "1"), // d3
    HOLLAND_CITY_DM("MAP15", "281", "8"), // 119
    HOLLAND_STREET_INF("MAP13", "222", "2"), // de
    HOLLAND_STREET_DM("MAP16", "282", "8"), // 11a
    HOLLAND_CHURCH_DM("MAP17", "283", "8"), // 11b
    HOLLAND_BRIDGE_HTL("MAP14", "245", "4"), // f5
    HOLLAND_BRIDGE_DM("MAP18", "285", "8"), // 11d
    HOLLAND_BRIDGE_NIGHT_DOM( "MAP12", "216", "1"), // d8
    HOLLAND_BRIDGE_NIGHT_DM("MAP19", "286", "8"), // 11e
    ITALY_BEACH_DEM("MAP3", "131", "3"), // 83
    ITALY_BEACH_DM("MAP6", "181", "8"), // b5
    ITALY_AIRFIELD_DEM("MAP4", "132", "3"), // 84
    ITALY_AIRFIELD_DM("MAP7", "182", "8"), // b6
    ITALY_MT_VILLAGE_INF("MAP2", "124", "2"), // 7c
    ITALY_MT_VILLAGE_DM("MAP9", "184", "8"), // b8
    ITALY_MT_VILLAGE_NIGHT_BL("MAP5", "153", "5"), // 99
    ITALY_MT_VILLAGE_NIGHT_DM("MAP8", "183", "8"), // b7
    ITALY_CITY_DOM("MAP1", "115", "1"), // 73
    ITALY_CITY_DM("MAP10", "185", "8"), // b9
    BELGIUM_RIVER_NIGHT_HTL("MAP22", "341", "4"), // 155
    BELGIUM_RIVER_NIGHT_DM("MAP24", "381", "8"), // 17d
    BELGIUM_RANCH_BL("MAP23", "352", "5"), // 160
    BELGIUM_RANCH_DM("MAP25", "382", "8"), // 17e
    BELGIUM_RIVER_DM("MAP26", "383", "8"), // 17f
    BELGIUM_FOREST_INF("MAP20", "324", "2"), // 144
    BELGIUM_FOREST_DM("MAP27", "384", "8"), // 180
    BELGIUM_CASTLE_DEM("MAP21", "336", "3"), // 150
    BELGIUM_CASTLE_DM("MAP28", "386", "8"); // 182

    public final String id;
    public final String key;
    public final String modeId;

    private static final Map<String, List<String>> mapStatId = new HashMap<>();

    static {
        mapStatId.put("1", List.of("153", "183")); // IT Mountain Village Night (Village B)
        mapStatId.put("2", List.of("131", "181")); // IT Beach
        mapStatId.put("3", List.of("132", "182")); // IT Airfield
        mapStatId.put("4", List.of("124", "184")); // IT Mountain Village (Village A)
        mapStatId.put("9", List.of("115", "185")); // IT City
        mapStatId.put("10", List.of("211", "281")); // NL City
        mapStatId.put("11", List.of("216", "286")); // NL Bridge Night (Bridge B)
        mapStatId.put("12", List.of("222", "282")); // NL Street
        mapStatId.put("13", List.of("245", "285")); // NL Bridge (Bridge A)
        mapStatId.put("16", List.of("283")); // NL Church
        mapStatId.put("19", List.of("324", "384")); // BE Forest
        mapStatId.put("20", List.of("336", "386")); // BE Castle
        mapStatId.put("21", List.of("383")); // BE River (River A)
        mapStatId.put("22", List.of("352", "382")); // BE Ranch
        mapStatId.put("25", List.of("341", "381")); // BE River Night (River B)
    }

    MapMoHH(String id, String key, String modeId) {
        this.id = id;
        this.key = key;
        this.modeId = modeId;
    }

    public static String getMapStatId(String key) {
        for (Map.Entry<String, List<String>> entry : mapStatId.entrySet()) {
            if (entry.getValue().contains(key)) {
                return entry.getKey();
            }
        }
        return "1";
    }
}

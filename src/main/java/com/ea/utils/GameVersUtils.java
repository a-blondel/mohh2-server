package com.ea.utils;

import java.util.List;

public class GameVersUtils {

    public static final String VERS_MOHH_PSP_HOST = "PSP/MOHGPS071";

    public static final List<String> VERS_MOHH_PSP = List.of("PSP/MOH07", VERS_MOHH_PSP_HOST);
    public static final List<String> SLUS_MOHH_PSP_PAL = List.of("ULES00557", "ULES00558", "ULES00559", "ULES00560", "ULES00561", "ULES00562", "MOHA10000");
    public static final List<String> SLUS_MOHH_PSP_NTSC = List.of("ULUS10141", "MOHA10000");

    public static final List<String> VERS_MOHH2_PSP = List.of("PSP/MOH08");
    public static final List<String> SLUS_MOHH2_PSP_PAL = List.of("ULES00955", "ULES00956", "ULES00988");
    public static final List<String> SLUS_MOHH2_PSP_NTSC = List.of("ULUS10310");

    public static final List<String> VERS_MOHH2_WII = List.of("WII/MOH08");
    public static final List<String> SLUS_MOHH2_WII_PAL = List.of("RM2X", "RM2P");
    public static final List<String> SLUS_MOHH2_WII_NTSC = List.of("RM2E");


    public static List<String> getRelatedVers(String vers) {
        if (VERS_MOHH_PSP.contains(vers)) {
            return VERS_MOHH_PSP;
        } else if (VERS_MOHH2_PSP.contains(vers)) {
            return VERS_MOHH2_PSP;
        } else if (VERS_MOHH2_WII.contains(vers)) {
            return VERS_MOHH2_WII;
        }
        return null;
    }

    public static List<String> getRelatedSlus(String vers, String slus) {
        if (VERS_MOHH_PSP.contains(vers)) {
            if (SLUS_MOHH_PSP_PAL.contains(slus)) {
                return SLUS_MOHH_PSP_PAL;
            } else if (SLUS_MOHH_PSP_NTSC.contains(slus)) {
                return SLUS_MOHH_PSP_NTSC;
            }
        } else if (VERS_MOHH2_PSP.contains(vers)) {
            if (SLUS_MOHH2_PSP_PAL.contains(slus)) {
                return SLUS_MOHH2_PSP_PAL;
            } else if (SLUS_MOHH2_PSP_NTSC.contains(slus)) {
                return SLUS_MOHH2_PSP_NTSC;
            }
        } else if (VERS_MOHH2_WII.contains(vers)) {
            if (SLUS_MOHH2_WII_PAL.contains(slus)) {
                return SLUS_MOHH2_WII_PAL;
            } else if (SLUS_MOHH2_WII_NTSC.contains(slus)) {
                return SLUS_MOHH2_WII_NTSC;
            }
        }
        return null;
    }

}

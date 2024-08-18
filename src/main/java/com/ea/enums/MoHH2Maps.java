package com.ea.enums;

public enum MoHH2Maps {
    PORT("0"),
    CITY("2"),
    SEWERS("4"),
    VILLAGE("7"),
    MONASTERY("10"),
    BASE("12");

    public final String id;

    MoHH2Maps(String id) {
        this.id = id;
    }
}

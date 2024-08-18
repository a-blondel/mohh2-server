package com.ea.enums;

public enum RankingCategories {
    MY_LEADERBOARD("0"), // MY EA LEADERBOARD
    TOP_100("1"), // EA TOP 100
    WEAPON_LEADERS("2"); // EA WEAPON LEADERS

    public final String id;

    RankingCategories(String id) {
        this.id = id;
    }
}

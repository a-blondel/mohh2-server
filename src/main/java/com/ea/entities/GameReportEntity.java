package com.ea.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "GAME_REPORT")
public class GameReportEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="GAME_ID", nullable=false)
    private GameEntity game;

    @ManyToOne
    @JoinColumn(name="PERSONA_ID", nullable=false)
    private PersonaEntity persona;

    @Column(name="IS_HOST", nullable=false)
    private boolean isHost;

    @Column(name="KILL", nullable=false)
    private int kill;

    @Column(name="DEATH", nullable=false)
    private int death;

    @Column(name="COLTSHOT", nullable=false)
    private int coltShot;

    @Column(name="COLTHIT", nullable=false)
    private int coltHit;

    @Column(name="COLTKILL", nullable=false)
    private int coltKill;

    @Column(name="COLTHEAD", nullable=false)
    private int coltHead;

    @Column(name="TOMSHOT", nullable=false)
    private int tomShot;

    @Column(name="TOMHIT", nullable=false)
    private int tomHit;

    @Column(name="TOMKILL", nullable=false)
    private int tomKill;

    @Column(name="TOMHEAD", nullable=false)
    private int tomHead;

    @Column(name="BARSHOT", nullable=false)
    private int barShot;

    @Column(name="BARHIT", nullable=false)
    private int barHit;

    @Column(name="BARKILL", nullable=false)
    private int barKill;

    @Column(name="BARHEAD", nullable=false)
    private int barHead;

    @Column(name="GARSHOT", nullable=false)
    private int garShot;

    @Column(name="GARHIT", nullable=false)
    private int garHit;

    @Column(name="GARKILL", nullable=false)
    private int garKill;

    @Column(name="GARHEAD", nullable=false)
    private int garHead;

    @Column(name="ENFIELDSHOT", nullable=false)
    private int enfieldShot;

    @Column(name="ENFIELDHIT", nullable=false)
    private int enfieldHit;

    @Column(name="ENFIELDKILL", nullable=false)
    private int enfieldKill;

    @Column(name="ENFIELDHEAD", nullable=false)
    private int enfieldHead;

    @Column(name="SHOTTYSHOT", nullable=false)
    private int shottyShot;

    @Column(name="SHOTTYHIT", nullable=false)
    private int shottyHit;

    @Column(name="SHOTTYKILL", nullable=false)
    private int shottyKill;

    @Column(name="SHOTTYHEAD", nullable=false)
    private int shottyHead;

    @Column(name="BAZSHOT", nullable=false)
    private int bazShot;

    @Column(name="BAZHIT", nullable=false)
    private int bazHit;

    @Column(name="BAZKILL", nullable=false)
    private int bazKill;

    @Column(name="BAZHEAD", nullable=false)
    private int bazHead;

    @Column(name="LUGERSHOT", nullable=false)
    private int lugerShot;

    @Column(name="LUGERHIT", nullable=false)
    private int lugerHit;

    @Column(name="LUGERKILL", nullable=false)
    private int lugerKill;

    @Column(name="LUGERHEAD", nullable=false)
    private int lugerHead;

    @Column(name="MP40SHOT", nullable=false)
    private int mp40Shot;

    @Column(name="MP40HIT", nullable=false)
    private int mp40Hit;

    @Column(name="MP40KILL", nullable=false)
    private int mp40Kill;

    @Column(name="MP40HEAD", nullable=false)
    private int mp40Head;

    @Column(name="MP44SHOT", nullable=false)
    private int mp44Shot;

    @Column(name="MP44HIT", nullable=false)
    private int mp44Hit;

    @Column(name="MP44KILL", nullable=false)
    private int mp44Kill;

    @Column(name="MP44HEAD", nullable=false)
    private int mp44Head;

    @Column(name="KARSHOT", nullable=false)
    private int karShot;

    @Column(name="KARHIT", nullable=false)
    private int karHit;

    @Column(name="KARKILL", nullable=false)
    private int karKill;

    @Column(name="KARHEAD", nullable=false)
    private int karHead;

    @Column(name="GEWRSHOT", nullable=false)
    private int gewrShot;

    @Column(name="GEWRHIT", nullable=false)
    private int gewrHit;

    @Column(name="GEWRKILL", nullable=false)
    private int gewrKill;

    @Column(name="GEWRHEAD", nullable=false)
    private int gewrHead;

    @Column(name="PANZSHOT", nullable=false)
    private int panzShot;

    @Column(name="PANZHIT", nullable=false)
    private int panzHit;

    @Column(name="PANZKILL", nullable=false)
    private int panzKill;

    @Column(name="PANZHEAD", nullable=false)
    private int panzHead;

    @Column(name="GREENTHROW", nullable=false)
    private int greenThrow;

    @Column(name="GRENADEKILL", nullable=false)
    private int grenadeKill;

    @Column(name="MELEEKILL", nullable=false)
    private int meleeKill;

    @Column(name="WIN", nullable=false)
    private int win;

    @Column(name="LOSS", nullable=false)
    private int loss;

    @Column(name="AXIS", nullable=false)
    private int axis;

    @Column(name="ALLIES", nullable=false)
    private int allies;

    @Column(name="DMRND", nullable=false)
    private int dmRnd;

    @Column(name="CTFAXIS", nullable=false)
    private int ctfAxis;

    @Column(name="CTFALLIES", nullable=false)
    private int ctfAllies;

    @Column(name="DEMAXIS", nullable=false)
    private int demAxis;

    @Column(name="DEMALLIES", nullable=false)
    private int demAllies;

    @Column(name="CAPAXIS", nullable=false)
    private int capAxis;

    @Column(name="CAPALLIES", nullable=false)
    private int capAllies;

    @Column(name="KOHAXIS", nullable=false)
    private int kohAxis;

    @Column(name="KOHALLIES", nullable=false)
    private int kohAllies;

    @Column(name="BLAXIS", nullable=false)
    private int blAxis;

    @Column(name="BLALLIES", nullable=false)
    private int blAllies;

    @Column(name="MAP1", nullable=false)
    private int map1;

    @Column(name="MAP2", nullable=false)
    private int map2;

    @Column(name="MAP3", nullable=false)
    private int map3;

    @Column(name="MAP4", nullable=false)
    private int map4;

    @Column(name="MAP5", nullable=false)
    private int map5;

    @Column(name="MAP6", nullable=false)
    private int map6;

    @Column(name="MAP7", nullable=false)
    private int map7;

    @Column(name="MAP8", nullable=false)
    private int map8;

    @Column(name="MAP9", nullable=false)
    private int map9;

    @Column(name="MAP10", nullable=false)
    private int map10;

    @Column(name="MAP11", nullable=false)
    private int map11;

    @Column(name="MAP12", nullable=false)
    private int map12;

    @Column(name="MAP13", nullable=false)
    private int map13;

    @Column(name="MAP14", nullable=false)
    private int map14;

    @Column(name="MAP15", nullable=false)
    private int map15;

    @Column(name="MAP16", nullable=false)
    private int map16;

    @Column(name="MAP17", nullable=false)
    private int map17;

    @Column(name="MAP18", nullable=false)
    private int map18;

    @Column(name="MAP19", nullable=false)
    private int map19;

    @Column(name="MAP20", nullable=false)
    private int map20;

    @Column(name="MAP21", nullable=false)
    private int map21;

    @Column(name="MAP22", nullable=false)
    private int map22;

    @Column(name="MAP23", nullable=false)
    private int map23;

    @Column(name="MAP24", nullable=false)
    private int map24;

    @Column(name="MAP25", nullable=false)
    private int map25;

    @Column(name="MAP26", nullable=false)
    private int map26;

    @Column(name="MAP27", nullable=false)
    private int map27;

    @Column(name="MAP28", nullable=false)
    private int map28;

    @Column(name="PLAYTIME", nullable=false)
    private int playTime;

    @Column(name="SERVTYPE", nullable=false)
    private int servType;

    @Column(name="AUTH")
    private String auth;

    @Column(name="RNK", nullable=false)
    private int rnk;

    @Column(name="START_TIME", nullable=false)
    private LocalDateTime startTime;

    @Column(name="END_TIME")
    private LocalDateTime endTime;

}

package com.ea.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

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

    private boolean isHost;

    private int kills;

    private int deaths;

    private Timestamp startTime;

    private Timestamp endTime;

}

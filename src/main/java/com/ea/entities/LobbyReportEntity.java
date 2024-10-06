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
@Table(name = "LOBBY_REPORT")
public class LobbyReportEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="LOBBY_ID", nullable=false)
    private LobbyEntity lobby;

    @ManyToOne
    @JoinColumn(name="PERSONA_ID", nullable=false)
    private PersonaEntity persona;

    private boolean isHost;

    private int kills;

    private int deaths;

    private Timestamp startTime;

    private Timestamp endTime;

}

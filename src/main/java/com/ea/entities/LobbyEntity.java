package com.ea.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "LOBBY")
public class LobbyEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private boolean userHosted;

    private String name;

    private String params;

    private String sysflags;

    private String pass;

    private int minsize;

    private int maxsize;

    private Timestamp startTime;

    private Timestamp endTime;

    @OneToMany(mappedBy="lobby", fetch = FetchType.EAGER)
    private Set<LobbyReportEntity> lobbyReports;

}

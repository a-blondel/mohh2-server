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
@Table(name = "PERSONA")
public class PersonaEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="ACCOUNT_ID", nullable=false)
    private AccountEntity account;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumn
    private PersonaStatsEntity personaStats;

    private String pers;

    private int rp;

    private Timestamp createdOn;

    private Timestamp deletedOn;

    @OneToMany(mappedBy="persona", fetch = FetchType.EAGER)
    private Set<GameReportEntity> gameReports;

    @OneToMany(mappedBy="persona", fetch = FetchType.EAGER)
    private Set<PersonaConnectionEntity> personaConnections;

}

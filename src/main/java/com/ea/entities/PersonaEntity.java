package com.ea.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
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

    private LocalDateTime createdOn;

    private LocalDateTime deletedOn;

    @OneToMany(mappedBy="persona", fetch = FetchType.EAGER)
    private Set<GameReportEntity> gameReports;

    @OneToMany(mappedBy="persona", fetch = FetchType.EAGER)
    private Set<PersonaConnectionEntity> personaConnections;

}

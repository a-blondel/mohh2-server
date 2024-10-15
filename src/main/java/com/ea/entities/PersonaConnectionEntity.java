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
@Table(name = "PERSONA_CONNECTION")
public class PersonaConnectionEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="PERSONA_ID", nullable=false)
    private PersonaEntity persona;

    private String ip;

    private String vers;

    private String slus;

    private Timestamp startTime;

    private Timestamp endTime;

}

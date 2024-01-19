CREATE TABLE IF NOT EXISTS ACCOUNT (
    ID numeric AUTO_INCREMENT PRIMARY KEY NOT NULL,
    NAME varchar(32) NOT NULL,
    PASS varchar(128) NOT NULL,
    MAIL varchar(128) NOT NULL,
    LOC varchar(10) NOT NULL,
    BORN varchar(8) NOT NULL,
    ZIP varchar(20) NOT NULL,
    GEND varchar(1) NOT NULL,
    SPAM varchar(2) NOT NULL,
    TOS numeric NOT NULL,
    TICK varchar(128) NOT NULL,
    GAMECODE varchar(20) NOT NULL,
    VERS varchar(32) NOT NULL,
    SKU varchar(10) NOT NULL,
    SLUS varchar(10) NOT NULL,
    SDKVERS varchar(20) NOT NULL,
    BUILDDATE varchar(20) NOT NULL,
    CREATED_ON timestamp NOT NULL,
    UPDATED_ON timestamp NULL
);

CREATE TABLE IF NOT EXISTS PERSONA (
    ID numeric AUTO_INCREMENT PRIMARY KEY NOT NULL,
    ACCOUNT_ID numeric NOT NULL,
    PERS varchar(32) NOT NULL,
    RP numeric NOT NULL DEFAULT 5,
    CREATED_ON timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    DELETED_ON timestamp NULL,
    CONSTRAINT FK_PERSONA_ACCOUNT_ID FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNT(ID)
);

CREATE TABLE IF NOT EXISTS PERSONA_STATS (
    PERSONA_ID numeric UNIQUE NOT NULL,
    TOTAL_KILLS numeric NOT NULL DEFAULT 0,
    TOTAL_DEATHS numeric NOT NULL DEFAULT 0,
    TOTAL_HEADSHOTS numeric NOT NULL DEFAULT 0,
    TOTAL_HIT numeric NOT NULL DEFAULT 0,
    TOTAL_MISS numeric NOT NULL DEFAULT 0,
    TIME_ALLIED numeric NOT NULL DEFAULT 0,
    TIME_AXIS numeric NOT NULL DEFAULT 0,
    TIME_PORT numeric NOT NULL DEFAULT 0,
    TIME_CITY numeric NOT NULL DEFAULT 0,
    TIME_SEWERS numeric NOT NULL DEFAULT 0,
    TIME_VILLAGE numeric NOT NULL DEFAULT 0,
    TIME_MONASTERY numeric NOT NULL DEFAULT 0,
    TIME_BASE numeric NOT NULL DEFAULT 0,
    TIME_DM numeric NOT NULL DEFAULT 0,
    TIME_TDM numeric NOT NULL DEFAULT 0,
    TIME_INF numeric NOT NULL DEFAULT 0,
    DM_WINS numeric NOT NULL DEFAULT 0,
    DM_LOSSES numeric NOT NULL DEFAULT 0,
    TDM_WINS numeric NOT NULL DEFAULT 0,
    TDM_LOSSES numeric NOT NULL DEFAULT 0,
    INF_WINS numeric NOT NULL DEFAULT 0,
    INF_LOSSES numeric NOT NULL DEFAULT 0,
    COLT_KILLS numeric NOT NULL DEFAULT 0,
    COLT_HIT numeric NOT NULL DEFAULT 0,
    COLT_MISS numeric NOT NULL DEFAULT 0,
    THOMPSON_KILLS numeric NOT NULL DEFAULT 0,
    THOMPSON_HIT numeric NOT NULL DEFAULT 0,
    THOMPSON_MISS numeric NOT NULL DEFAULT 0,
    BAR_KILLS numeric NOT NULL DEFAULT 0,
    BAR_HIT numeric NOT NULL DEFAULT 0,
    BAR_MISS numeric NOT NULL DEFAULT 0,
    GARAND_KILLS numeric NOT NULL DEFAULT 0,
    GARAND_HIT numeric NOT NULL DEFAULT 0,
    GARAND_MISS numeric NOT NULL DEFAULT 0,
    SPRINGFIELD_KILLS numeric NOT NULL DEFAULT 0,
    SPRINGFIELD_HIT numeric NOT NULL DEFAULT 0,
    SPRINGFIELD_MISS numeric NOT NULL DEFAULT 0,
    SHOTGUN_KILLS numeric NOT NULL DEFAULT 0,
    SHOTGUN_HIT numeric NOT NULL DEFAULT 0,
    SHOTGUN_MISS numeric NOT NULL DEFAULT 0,
    BAZOOKA_KILLS numeric NOT NULL DEFAULT 0,
    BAZOOKA_HIT numeric NOT NULL DEFAULT 0,
    BAZOOKA_MISS numeric NOT NULL DEFAULT 0,
    LUGER_KILLS numeric NOT NULL DEFAULT 0,
    LUGER_HIT numeric NOT NULL DEFAULT 0,
    LUGER_MISS numeric NOT NULL DEFAULT 0,
    MP40_KILLS numeric NOT NULL DEFAULT 0,
    MP40_HIT numeric NOT NULL DEFAULT 0,
    MP40_MISS numeric NOT NULL DEFAULT 0,
    STG44_KILLS numeric NOT NULL DEFAULT 0,
    STG44_HIT numeric NOT NULL DEFAULT 0,
    STG44_MISS numeric NOT NULL DEFAULT 0,
    KARABINER_KILLS numeric NOT NULL DEFAULT 0,
    KARABINER_HIT numeric NOT NULL DEFAULT 0,
    KARABINER_MISS numeric NOT NULL DEFAULT 0,
    GEWEHR_KILLS numeric NOT NULL DEFAULT 0,
    GEWEHR_HIT numeric NOT NULL DEFAULT 0,
    GEWEHR_MISS numeric NOT NULL DEFAULT 0,
    GRENADE_KILLS numeric NOT NULL DEFAULT 0,
    MELEE_KILLS numeric NOT NULL DEFAULT 0,
    CONSTRAINT FK_PERSONA_STATS_PERSONA_ID FOREIGN KEY (PERSONA_ID) REFERENCES PERSONA(ID)
);

CREATE TABLE IF NOT EXISTS PERSONA_CONNECTION (
    ID numeric AUTO_INCREMENT PRIMARY KEY NOT NULL,
    IP varchar(255) NOT NULL,
    PERSONA_ID numeric NULL,
    START_TIME timestamp NOT NULL,
    END_TIME timestamp NULL,
    CONSTRAINT FK_CONNECTIONS_ACCOUNT_ID FOREIGN KEY (PERSONA_ID) REFERENCES PERSONA(ID)
);

CREATE TABLE IF NOT EXISTS LOBBY (
    ID numeric AUTO_INCREMENT PRIMARY KEY NOT NULL,
    NAME varchar(32) NOT NULL,
    PARAMS varchar(255) NOT NULL,
    SYSFLAGS varchar(10) NOT NULL,
    PASS varchar(128) NULL,
    MINSIZE numeric NOT NULL,
    MAXSIZE numeric NOT NULL,
    START_TIME timestamp NOT NULL,
    END_TIME timestamp NULL
);

CREATE TABLE IF NOT EXISTS LOBBY_REPORT (
    ID numeric AUTO_INCREMENT PRIMARY KEY NOT NULL,
    LOBBY_ID numeric NOT NULL,
    PERSONA_ID numeric NOT NULL,
    KILLS numeric NOT NULL DEFAULT 0,
    DEATHS numeric NOT NULL DEFAULT 0,
    START_TIME timestamp NOT NULL,
    END_TIME timestamp NULL,
    CONSTRAINT FK_LOBBY_REPORT_LOBBY_ID FOREIGN KEY (LOBBY_ID) REFERENCES LOBBY(ID),
    CONSTRAINT FK_LOBBY_REPORT_PERSONA_ID FOREIGN KEY (PERSONA_ID) REFERENCES PERSONA(ID)
);

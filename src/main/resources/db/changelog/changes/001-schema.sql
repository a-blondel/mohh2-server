CREATE TABLE IF NOT EXISTS ACCOUNT (
    ID bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    NAME varchar(32) NOT NULL,
    PASS varchar(128) NOT NULL,
    MAIL varchar(128) NOT NULL,
    LOC varchar(10) NOT NULL,
    BORN varchar(8) NOT NULL,
    ZIP varchar(20) NOT NULL,
    GEND varchar(1) NOT NULL,
    SPAM varchar(2) NOT NULL,
    TOS numeric NOT NULL,
    TICK varchar(128) NULL,
    GAMECODE varchar(20) NULL,
    VERS varchar(32) NULL,
    SKU varchar(20) NULL,
    SLUS varchar(10) NULL,
    SDKVERS varchar(20) NULL,
    BUILDDATE varchar(20) NULL,
    CREATED_ON timestamp NOT NULL,
    UPDATED_ON timestamp NULL
);

CREATE TABLE IF NOT EXISTS PERSONA (
    ID bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    ACCOUNT_ID bigint NOT NULL,
    PERS varchar(32) NOT NULL,
    RP numeric NOT NULL DEFAULT 5,
    CREATED_ON timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    DELETED_ON timestamp NULL,
    HOST boolean NOT NULL DEFAULT false,
    CONSTRAINT FK_PERSONA_ACCOUNT_ID FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNT(ID)
);

CREATE TABLE IF NOT EXISTS PERSONA_STATS (
    ID bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    PERSONA_ID bigint NOT NULL,
    VERS varchar(32) NOT NULL,
    SLUS varchar(10) NOT NULL,
    KILL numeric NOT NULL DEFAULT 0,
    DEATH numeric NOT NULL DEFAULT 0,
    SHOT numeric NOT NULL DEFAULT 0,
    HIT numeric NOT NULL DEFAULT 0,
    HEAD numeric NOT NULL DEFAULT 0,
    COLTSHOT numeric NOT NULL DEFAULT 0,
    COLTHIT numeric NOT NULL DEFAULT 0,
    COLTKILL numeric NOT NULL DEFAULT 0,
    COLTHEAD numeric NOT NULL DEFAULT 0,
    TOMSHOT numeric NOT NULL DEFAULT 0,
    TOMHIT numeric NOT NULL DEFAULT 0,
    TOMKILL numeric NOT NULL DEFAULT 0,
    TOMHEAD numeric NOT NULL DEFAULT 0,
    BARSHOT numeric NOT NULL DEFAULT 0,
    BARHIT numeric NOT NULL DEFAULT 0,
    BARKILL numeric NOT NULL DEFAULT 0,
    BARHEAD numeric NOT NULL DEFAULT 0,
    GARSHOT numeric NOT NULL DEFAULT 0,
    GARHIT numeric NOT NULL DEFAULT 0,
    GARKILL numeric NOT NULL DEFAULT 0,
    GARHEAD numeric NOT NULL DEFAULT 0,
    ENFIELDSHOT numeric NOT NULL DEFAULT 0,
    ENFIELDHIT numeric NOT NULL DEFAULT 0,
    ENFIELDKILL numeric NOT NULL DEFAULT 0,
    ENFIELDHEAD numeric NOT NULL DEFAULT 0,
    SHOTTYSHOT numeric NOT NULL DEFAULT 0,
    SHOTTYHIT numeric NOT NULL DEFAULT 0,
    SHOTTYKILL numeric NOT NULL DEFAULT 0,
    SHOTTYHEAD numeric NOT NULL DEFAULT 0,
    BAZSHOT numeric NOT NULL DEFAULT 0,
    BAZHIT numeric NOT NULL DEFAULT 0,
    BAZKILL numeric NOT NULL DEFAULT 0,
    BAZHEAD numeric NOT NULL DEFAULT 0,
    LUGERSHOT numeric NOT NULL DEFAULT 0,
    LUGERHIT numeric NOT NULL DEFAULT 0,
    LUGERKILL numeric NOT NULL DEFAULT 0,
    LUGERHEAD numeric NOT NULL DEFAULT 0,
    MP40SHOT numeric NOT NULL DEFAULT 0,
    MP40HIT numeric NOT NULL DEFAULT 0,
    MP40KILL numeric NOT NULL DEFAULT 0,
    MP40HEAD numeric NOT NULL DEFAULT 0,
    MP44SHOT numeric NOT NULL DEFAULT 0,
    MP44HIT numeric NOT NULL DEFAULT 0,
    MP44KILL numeric NOT NULL DEFAULT 0,
    MP44HEAD numeric NOT NULL DEFAULT 0,
    KARSHOT numeric NOT NULL DEFAULT 0,
    KARHIT numeric NOT NULL DEFAULT 0,
    KARKILL numeric NOT NULL DEFAULT 0,
    KARHEAD numeric NOT NULL DEFAULT 0,
    GEWRSHOT numeric NOT NULL DEFAULT 0,
    GEWRHIT numeric NOT NULL DEFAULT 0,
    GEWRKILL numeric NOT NULL DEFAULT 0,
    GEWRHEAD numeric NOT NULL DEFAULT 0,
    PANZSHOT numeric NOT NULL DEFAULT 0,
    PANZHIT numeric NOT NULL DEFAULT 0,
    PANZKILL numeric NOT NULL DEFAULT 0,
    PANZHEAD numeric NOT NULL DEFAULT 0,
    GREENTHROW numeric NOT NULL DEFAULT 0,
    GRENADEKILL numeric NOT NULL DEFAULT 0,
    MELEEKILL numeric NOT NULL DEFAULT 0,
    WIN numeric NOT NULL DEFAULT 0,
    LOSS numeric NOT NULL DEFAULT 0,
    AXIS numeric NOT NULL DEFAULT 0,
    ALLIES numeric NOT NULL DEFAULT 0,
    DMRND numeric NOT NULL DEFAULT 0,
    CTFAXIS numeric NOT NULL DEFAULT 0,
    CTFALLIES numeric NOT NULL DEFAULT 0,
    DEMAXIS numeric NOT NULL DEFAULT 0,
    DEMALLIES numeric NOT NULL DEFAULT 0,
    CAPAXIS numeric NOT NULL DEFAULT 0,
    CAPALLIES numeric NOT NULL DEFAULT 0,
    KOHAXIS numeric NOT NULL DEFAULT 0,
    KOHALLIES numeric NOT NULL DEFAULT 0,
    BLAXIS numeric NOT NULL DEFAULT 0,
    BLALLIES numeric NOT NULL DEFAULT 0,
    TDMAXIS numeric NOT NULL DEFAULT 0,
    TDMALLIES numeric NOT NULL DEFAULT 0,
    MAP1 numeric NOT NULL DEFAULT 0,
    MAP2 numeric NOT NULL DEFAULT 0,
    MAP3 numeric NOT NULL DEFAULT 0,
    MAP4 numeric NOT NULL DEFAULT 0,
    MAP5 numeric NOT NULL DEFAULT 0,
    MAP6 numeric NOT NULL DEFAULT 0,
    MAP7 numeric NOT NULL DEFAULT 0,
    MAP8 numeric NOT NULL DEFAULT 0,
    MAP9 numeric NOT NULL DEFAULT 0,
    MAP10 numeric NOT NULL DEFAULT 0,
    MAP11 numeric NOT NULL DEFAULT 0,
    MAP12 numeric NOT NULL DEFAULT 0,
    MAP13 numeric NOT NULL DEFAULT 0,
    MAP14 numeric NOT NULL DEFAULT 0,
    MAP15 numeric NOT NULL DEFAULT 0,
    MAP16 numeric NOT NULL DEFAULT 0,
    MAP17 numeric NOT NULL DEFAULT 0,
    MAP18 numeric NOT NULL DEFAULT 0,
    MAP19 numeric NOT NULL DEFAULT 0,
    MAP20 numeric NOT NULL DEFAULT 0,
    MAP21 numeric NOT NULL DEFAULT 0,
    MAP22 numeric NOT NULL DEFAULT 0,
    MAP23 numeric NOT NULL DEFAULT 0,
    MAP24 numeric NOT NULL DEFAULT 0,
    MAP25 numeric NOT NULL DEFAULT 0,
    MAP26 numeric NOT NULL DEFAULT 0,
    MAP27 numeric NOT NULL DEFAULT 0,
    MAP28 numeric NOT NULL DEFAULT 0,
    PLAYTIME numeric NOT NULL DEFAULT 0,
    CONSTRAINT FK_PERSONA_STATS_PERSONA_ID FOREIGN KEY (PERSONA_ID) REFERENCES PERSONA(ID)
);

CREATE TABLE IF NOT EXISTS PERSONA_CONNECTION (
    ID bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    PERSONA_ID bigint NOT NULL,
    ADDRESS varchar(255) NOT NULL,
    VERS varchar(32) NULL,
    SLUS varchar(10) NULL,
    START_TIME timestamp NOT NULL,
    END_TIME timestamp NULL,
    CONSTRAINT FK_CONNECTIONS_ACCOUNT_ID FOREIGN KEY (PERSONA_ID) REFERENCES PERSONA(ID)
);

CREATE TABLE IF NOT EXISTS GAME (
    ID bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    VERS varchar(32) NOT NULL,
    SLUS varchar(10) NOT NULL,
    USER_HOSTED boolean NOT NULL,
    NAME varchar(32) NOT NULL,
    PARAMS varchar(255) NOT NULL,
    SYSFLAGS varchar(10) NOT NULL,
    PASS varchar(128) NULL,
    MINSIZE numeric NOT NULL,
    MAXSIZE numeric NOT NULL,
    START_TIME timestamp NOT NULL,
    END_TIME timestamp NULL
);

CREATE TABLE IF NOT EXISTS GAME_REPORT (
    ID bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    GAME_ID bigint NOT NULL,
    PERSONA_ID bigint NOT NULL,
    IS_HOST boolean NOT NULL,
    KILL numeric NOT NULL DEFAULT 0,
    DEATH numeric NOT NULL DEFAULT 0,
    SHOT numeric NOT NULL DEFAULT 0,
    HIT numeric NOT NULL DEFAULT 0,
    HEAD numeric NOT NULL DEFAULT 0,
    COLTSHOT numeric NOT NULL DEFAULT 0,
    COLTHIT numeric NOT NULL DEFAULT 0,
    COLTKILL numeric NOT NULL DEFAULT 0,
    COLTHEAD numeric NOT NULL DEFAULT 0,
    TOMSHOT numeric NOT NULL DEFAULT 0,
    TOMHIT numeric NOT NULL DEFAULT 0,
    TOMKILL numeric NOT NULL DEFAULT 0,
    TOMHEAD numeric NOT NULL DEFAULT 0,
    BARSHOT numeric NOT NULL DEFAULT 0,
    BARHIT numeric NOT NULL DEFAULT 0,
    BARKILL numeric NOT NULL DEFAULT 0,
    BARHEAD numeric NOT NULL DEFAULT 0,
    GARSHOT numeric NOT NULL DEFAULT 0,
    GARHIT numeric NOT NULL DEFAULT 0,
    GARKILL numeric NOT NULL DEFAULT 0,
    GARHEAD numeric NOT NULL DEFAULT 0,
    ENFIELDSHOT numeric NOT NULL DEFAULT 0,
    ENFIELDHIT numeric NOT NULL DEFAULT 0,
    ENFIELDKILL numeric NOT NULL DEFAULT 0,
    ENFIELDHEAD numeric NOT NULL DEFAULT 0,
    SHOTTYSHOT numeric NOT NULL DEFAULT 0,
    SHOTTYHIT numeric NOT NULL DEFAULT 0,
    SHOTTYKILL numeric NOT NULL DEFAULT 0,
    SHOTTYHEAD numeric NOT NULL DEFAULT 0,
    BAZSHOT numeric NOT NULL DEFAULT 0,
    BAZHIT numeric NOT NULL DEFAULT 0,
    BAZKILL numeric NOT NULL DEFAULT 0,
    BAZHEAD numeric NOT NULL DEFAULT 0,
    LUGERSHOT numeric NOT NULL DEFAULT 0,
    LUGERHIT numeric NOT NULL DEFAULT 0,
    LUGERKILL numeric NOT NULL DEFAULT 0,
    LUGERHEAD numeric NOT NULL DEFAULT 0,
    MP40SHOT numeric NOT NULL DEFAULT 0,
    MP40HIT numeric NOT NULL DEFAULT 0,
    MP40KILL numeric NOT NULL DEFAULT 0,
    MP40HEAD numeric NOT NULL DEFAULT 0,
    MP44SHOT numeric NOT NULL DEFAULT 0,
    MP44HIT numeric NOT NULL DEFAULT 0,
    MP44KILL numeric NOT NULL DEFAULT 0,
    MP44HEAD numeric NOT NULL DEFAULT 0,
    KARSHOT numeric NOT NULL DEFAULT 0,
    KARHIT numeric NOT NULL DEFAULT 0,
    KARKILL numeric NOT NULL DEFAULT 0,
    KARHEAD numeric NOT NULL DEFAULT 0,
    GEWRSHOT numeric NOT NULL DEFAULT 0,
    GEWRHIT numeric NOT NULL DEFAULT 0,
    GEWRKILL numeric NOT NULL DEFAULT 0,
    GEWRHEAD numeric NOT NULL DEFAULT 0,
    PANZSHOT numeric NOT NULL DEFAULT 0,
    PANZHIT numeric NOT NULL DEFAULT 0,
    PANZKILL numeric NOT NULL DEFAULT 0,
    PANZHEAD numeric NOT NULL DEFAULT 0,
    GREENTHROW numeric NOT NULL DEFAULT 0,
    GRENADEKILL numeric NOT NULL DEFAULT 0,
    MELEEKILL numeric NOT NULL DEFAULT 0,
    WIN numeric NOT NULL DEFAULT 0,
    LOSS numeric NOT NULL DEFAULT 0,
    AXIS numeric NOT NULL DEFAULT 0,
    ALLIES numeric NOT NULL DEFAULT 0,
    DMRND numeric NOT NULL DEFAULT 0,
    CTFAXIS numeric NOT NULL DEFAULT 0,
    CTFALLIES numeric NOT NULL DEFAULT 0,
    DEMAXIS numeric NOT NULL DEFAULT 0,
    DEMALLIES numeric NOT NULL DEFAULT 0,
    CAPAXIS numeric NOT NULL DEFAULT 0,
    CAPALLIES numeric NOT NULL DEFAULT 0,
    KOHAXIS numeric NOT NULL DEFAULT 0,
    KOHALLIES numeric NOT NULL DEFAULT 0,
    BLAXIS numeric NOT NULL DEFAULT 0,
    BLALLIES numeric NOT NULL DEFAULT 0,
    TDMAXIS numeric NOT NULL DEFAULT 0,
    TDMALLIES numeric NOT NULL DEFAULT 0,
    MAP1 numeric NOT NULL DEFAULT 0,
    MAP2 numeric NOT NULL DEFAULT 0,
    MAP3 numeric NOT NULL DEFAULT 0,
    MAP4 numeric NOT NULL DEFAULT 0,
    MAP5 numeric NOT NULL DEFAULT 0,
    MAP6 numeric NOT NULL DEFAULT 0,
    MAP7 numeric NOT NULL DEFAULT 0,
    MAP8 numeric NOT NULL DEFAULT 0,
    MAP9 numeric NOT NULL DEFAULT 0,
    MAP10 numeric NOT NULL DEFAULT 0,
    MAP11 numeric NOT NULL DEFAULT 0,
    MAP12 numeric NOT NULL DEFAULT 0,
    MAP13 numeric NOT NULL DEFAULT 0,
    MAP14 numeric NOT NULL DEFAULT 0,
    MAP15 numeric NOT NULL DEFAULT 0,
    MAP16 numeric NOT NULL DEFAULT 0,
    MAP17 numeric NOT NULL DEFAULT 0,
    MAP18 numeric NOT NULL DEFAULT 0,
    MAP19 numeric NOT NULL DEFAULT 0,
    MAP20 numeric NOT NULL DEFAULT 0,
    MAP21 numeric NOT NULL DEFAULT 0,
    MAP22 numeric NOT NULL DEFAULT 0,
    MAP23 numeric NOT NULL DEFAULT 0,
    MAP24 numeric NOT NULL DEFAULT 0,
    MAP25 numeric NOT NULL DEFAULT 0,
    MAP26 numeric NOT NULL DEFAULT 0,
    MAP27 numeric NOT NULL DEFAULT 0,
    MAP28 numeric NOT NULL DEFAULT 0,
    PLAYTIME numeric NOT NULL DEFAULT 0,
    SERVTYPE numeric NOT NULL DEFAULT 0,
    AUTH varchar(255) NULL,
    RNK numeric NOT NULL DEFAULT 0,
    START_TIME timestamp NOT NULL,
    END_TIME timestamp NULL,
    CONSTRAINT FK_GAME_REPORT_GAME_ID FOREIGN KEY (GAME_ID) REFERENCES GAME(ID),
    CONSTRAINT FK_GAME_REPORT_PERSONA_ID FOREIGN KEY (PERSONA_ID) REFERENCES PERSONA(ID)
);

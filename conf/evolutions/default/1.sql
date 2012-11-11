# --- Users schema 
 

# --- !Downs

DROP TABLE if exists Bezitting;
DROP TABLE if exists Kaart;
DROP TABLE if exists Vriend;
DROP TABLE if exists Gebruiker;

# --- !Ups
 
CREATE TABLE Gebruiker (
    facebookId BIGINT NOT NULL,
    aantalVerzamelingen smallint NOT NULL,
    PRIMARY KEY (facebookId)
);

CREATE TABLE Vriend (
    eerste BIGINT NOT NULL references Gebruiker,
    tweede BIGINT NOT NULL references Gebruiker,
    PRIMARY KEY (eerste, tweede)
);

CREATE TABLE Kaart (
    nummer INT NOT NULL,
    PRIMARY KEY(nummer)
);

CREATE TABLE Bezitting (
    gebruikerId BIGINT NOT NULL references Gebruiker,
    kaartid INT NOT NULL, --  references Kaart,
    aantal smallint,
    PRIMARY KEY(gebruikerId, kaartId)
);

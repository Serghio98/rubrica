-- Database della Rubrica, per MySQL 8 o successivo.
-- Da eseguire una volta prima di avviare Rubrica.jar, ad esempio:
--     mysql -u root -p < schema_database.sql
-- oppure aprendo il file in MySQL Workbench ed eseguendolo.

CREATE DATABASE IF NOT EXISTS rubrica
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE rubrica;

-- Utenti che accedono al programma. La password non viene salvata:
-- solo il suo hash PBKDF2-HMAC-SHA256, il salt casuale e il numero di iterazioni.
CREATE TABLE IF NOT EXISTS utenti (
    id          INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)   CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,  -- distingue maiuscole e minuscole
    iterazioni  INT UNSIGNED  NOT NULL,
    salt        VARBINARY(32) NOT NULL,
    hash        VARBINARY(64) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_utenti_username (username)
) ENGINE = InnoDB;

-- Contatti. Ognuno appartiene a un solo utente, l'unico che può vederlo e modificarlo.
CREATE TABLE IF NOT EXISTS persone (
    id         INT UNSIGNED     NOT NULL AUTO_INCREMENT,
    id_utente  INT UNSIGNED     NOT NULL,
    nome       VARCHAR(100)     NOT NULL DEFAULT '',
    cognome    VARCHAR(100)     NOT NULL DEFAULT '',
    indirizzo  VARCHAR(255)     NOT NULL DEFAULT '',
    telefono   VARCHAR(50)      NOT NULL,
    eta        TINYINT UNSIGNED NULL,  -- NULL = età non indicata
    PRIMARY KEY (id),
    KEY ix_persone_utente (id_utente),
    CONSTRAINT fk_persone_utente FOREIGN KEY (id_utente) REFERENCES utenti (id) ON DELETE CASCADE
) ENGINE = InnoDB;

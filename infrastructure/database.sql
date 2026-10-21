SET NAMES utf8mb4;

CREATE SCHEMA IF NOT EXISTS onboarding;

CREATE TABLE IF NOT EXISTS onboarding.person_type (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    enumerator    VARCHAR(50)   NOT NULL,
    created_at    TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_person_type PRIMARY KEY (id),
    CONSTRAINT person_type_enumerator_unique UNIQUE (enumerator)
);

INSERT IGNORE INTO onboarding.person_type (enumerator)
VALUES ('NATURAL'), ('LEGAL');

CREATE TABLE IF NOT EXISTS onboarding.person (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    person_key       VARCHAR(36)   NOT NULL,
    person_type_id   BIGINT        NOT NULL,
    document_number  VARCHAR(14)   NOT NULL,
    name             VARCHAR(255)  NOT NULL,
    email            VARCHAR(255)  NOT NULL,
    created_at       TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at       TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_person PRIMARY KEY (id),
    CONSTRAINT person_key_unique UNIQUE (person_key),
    CONSTRAINT person_document_number_unique UNIQUE (document_number),
    CONSTRAINT fk1_person FOREIGN KEY (person_type_id)
        REFERENCES onboarding.person_type (id)
);

CREATE TABLE IF NOT EXISTS onboarding.natural_person (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    person_id   BIGINT        NOT NULL,
    created_at  TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_natural_person PRIMARY KEY (id),
    CONSTRAINT natural_person_person_id_unique UNIQUE (person_id),
    CONSTRAINT fk1_natural_person FOREIGN KEY (person_id)
        REFERENCES onboarding.person (id)
);

INSERT IGNORE INTO onboarding.person (person_key, person_type_id, document_number, name, email)
SELECT '00000000-0000-4000-8000-000000000001', id, '52998224725', 'Administrador PulsePass', 'admin@pulsepass.local'
FROM onboarding.person_type
WHERE enumerator = 'NATURAL';

INSERT IGNORE INTO onboarding.natural_person (person_id)
SELECT id
FROM onboarding.person
WHERE person_key = '00000000-0000-4000-8000-000000000001';

CREATE TABLE IF NOT EXISTS onboarding.legal_person (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    person_id   BIGINT        NOT NULL,
    created_at  TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_legal_person PRIMARY KEY (id),
    CONSTRAINT legal_person_person_id_unique UNIQUE (person_id),
    CONSTRAINT fk1_legal_person FOREIGN KEY (person_id)
        REFERENCES onboarding.person (id)
);

CREATE TABLE IF NOT EXISTS onboarding.person_phone_number (
    id                       BIGINT        NOT NULL AUTO_INCREMENT,
    person_phone_number_key  VARCHAR(36)   NOT NULL,
    person_id                BIGINT        NOT NULL,
    country_code             VARCHAR(3)    NOT NULL,
    area_code                VARCHAR(2)    NOT NULL,
    number                   VARCHAR(9)    NOT NULL,
    created_at               TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at               TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_person_phone_number PRIMARY KEY (id),
    CONSTRAINT person_phone_number_key_unique UNIQUE (person_phone_number_key),
    CONSTRAINT fk1_person_phone_number FOREIGN KEY (person_id)
        REFERENCES onboarding.person (id)
);

CREATE TABLE IF NOT EXISTS onboarding.collaborator_role (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    enumerator    VARCHAR(50)   NOT NULL,
    created_at    TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_collaborator_role PRIMARY KEY (id),
    CONSTRAINT collaborator_role_enumerator_unique UNIQUE (enumerator)
);

INSERT IGNORE INTO onboarding.collaborator_role (enumerator)
VALUES ('OWNER'), ('MEMBER');

CREATE TABLE IF NOT EXISTS onboarding.legal_person_collaborator_status (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    enumerator    VARCHAR(50)   NOT NULL,
    created_at    TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_legal_person_collaborator_status PRIMARY KEY (id),
    CONSTRAINT legal_person_collaborator_status_enumerator_unique UNIQUE (enumerator)
);

INSERT IGNORE INTO onboarding.legal_person_collaborator_status (enumerator)
VALUES ('ACTIVE'), ('DELETED');

CREATE TABLE IF NOT EXISTS onboarding.legal_person_collaborator (
    id                                    BIGINT        NOT NULL AUTO_INCREMENT,
    legal_person_collaborator_key         VARCHAR(36)   NOT NULL,
    legal_person_id                       BIGINT        NOT NULL,
    natural_person_id                     BIGINT        NOT NULL,
    collaborator_role_id                  BIGINT        NOT NULL,
    legal_person_collaborator_status_id   BIGINT        NOT NULL,
    created_at                            TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at                            TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_legal_person_collaborator PRIMARY KEY (id),
    CONSTRAINT legal_person_collaborator_key_unique UNIQUE (legal_person_collaborator_key),
    CONSTRAINT fk1_legal_person_collaborator FOREIGN KEY (legal_person_id)
        REFERENCES onboarding.legal_person (id),
    CONSTRAINT fk2_legal_person_collaborator FOREIGN KEY (natural_person_id)
        REFERENCES onboarding.natural_person (id),
    CONSTRAINT fk3_legal_person_collaborator FOREIGN KEY (collaborator_role_id)
        REFERENCES onboarding.collaborator_role (id),
    CONSTRAINT fk4_legal_person_collaborator FOREIGN KEY (legal_person_collaborator_status_id)
        REFERENCES onboarding.legal_person_collaborator_status (id)
);

CREATE TABLE IF NOT EXISTS onboarding.legal_person_collaborator_history (
    id                                      BIGINT        NOT NULL AUTO_INCREMENT,
    legal_person_collaborator_history_key   VARCHAR(36)   NOT NULL,
    legal_person_collaborator_id            BIGINT        NOT NULL,
    old_status_id                           BIGINT        NULL,
    new_status_id                           BIGINT        NOT NULL,
    history_data                            JSON          NOT NULL,
    created_at                              TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_legal_person_collaborator_history PRIMARY KEY (id),
    CONSTRAINT legal_person_collaborator_history_key_unique UNIQUE (legal_person_collaborator_history_key),
    CONSTRAINT fk1_legal_person_collaborator_history FOREIGN KEY (legal_person_collaborator_id)
        REFERENCES onboarding.legal_person_collaborator (id),
    CONSTRAINT fk2_legal_person_collaborator_history FOREIGN KEY (old_status_id)
        REFERENCES onboarding.legal_person_collaborator_status (id),
    CONSTRAINT fk3_legal_person_collaborator_history FOREIGN KEY (new_status_id)
        REFERENCES onboarding.legal_person_collaborator_status (id)
);

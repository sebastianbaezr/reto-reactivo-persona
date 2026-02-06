CREATE TABLE IF NOT EXISTS persons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_persons_email ON persons(email);

CREATE TABLE IF NOT EXISTS person_bootcamps (
    person_id BIGINT NOT NULL,
    bootcamp_id BIGINT NOT NULL,
    PRIMARY KEY (person_id, bootcamp_id),
    FOREIGN KEY (person_id) REFERENCES persons(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_person_bootcamps_person ON person_bootcamps(person_id);
CREATE INDEX IF NOT EXISTS idx_person_bootcamps_bootcamp ON person_bootcamps(bootcamp_id);

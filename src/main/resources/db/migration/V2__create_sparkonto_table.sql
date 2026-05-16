CREATE TABLE sparkonto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    standardbetrag REAL NOT NULL,
    benutzer_id INTEGER NOT NULL,
    erstellt_am TEXT NOT NULL,
    CONSTRAINT fk_sparkonto_benutzer FOREIGN KEY (benutzer_id) REFERENCES benutzer(id),
    CONSTRAINT uq_sparkonto_name_benutzer UNIQUE (name, benutzer_id)
);

CREATE TABLE jahresausgabe (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    jahresbetrag REAL NOT NULL,
    benutzer_id INTEGER NOT NULL,
    erstellt_am TEXT NOT NULL,
    CONSTRAINT fk_jahresausgabe_benutzer FOREIGN KEY (benutzer_id) REFERENCES benutzer(id)
);

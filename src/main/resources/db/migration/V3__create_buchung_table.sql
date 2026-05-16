CREATE TABLE buchung (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sparkonto_id INTEGER NOT NULL,
    datum TEXT NOT NULL,
    betrag REAL NOT NULL,
    typ TEXT NOT NULL,
    beschreibung TEXT,
    benutzer_id INTEGER NOT NULL,
    erstellt_am TEXT NOT NULL,
    gegenbuchung_id INTEGER,
    CONSTRAINT fk_buchung_sparkonto FOREIGN KEY (sparkonto_id) REFERENCES sparkonto(id),
    CONSTRAINT fk_buchung_benutzer FOREIGN KEY (benutzer_id) REFERENCES benutzer(id)
);

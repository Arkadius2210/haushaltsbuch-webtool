CREATE TABLE benutzer (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    erstellt_am TEXT NOT NULL
);

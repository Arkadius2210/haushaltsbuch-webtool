-- Default-Benutzer für Entwicklung.
-- ACHTUNG: Dieses Passwort ist nur für die Entwicklungsumgebung gedacht
-- und MUSS in Produktion geändert werden!
INSERT INTO benutzer (username, password_hash, erstellt_am)
VALUES ('admin', '$2a$12$LJ3m4sMKfXzSGVbGMXKHcOHn3gY/LbQFYBKQ8sQ0XaUGJMz9HXqXi', '2024-01-01T00:00:00.000');

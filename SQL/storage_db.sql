CREATE TABLE users (
id INTEGER PRIMARY KEY AUTOINCREMENT,
username TEXT NOT NULL UNIQUE,
password TEXT NOT NULL
);

INSERT INTO users (username, password) VALUES
("login1",  "password1"),
("login2",  "password2"),
("login3",  "password3"),
("login4",  "password4");


SELECT * FROM users;
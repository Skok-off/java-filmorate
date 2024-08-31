MERGE INTO ratings (name) KEY (name)
VALUES ('G'),
       ('PG'),
       ('PG-13'),
       ('R'),
       ('NC-17');

MERGE INTO genres (name) KEY (name)
VALUES ('Комедия'),
       ('Драма'),
       ('Мультфильм'),
       ('Триллер'),
       ('Документальный'),
       ('Боевик');

MERGE INTO entity_types (name) KEY (name)
VALUES ('films'),
       ('users'),
       ('reviews');

MERGE INTO operations (name) KEY (name)
VALUES ('REMOVE'),
       ('ADD'),
       ('UPDATE');

MERGE INTO event_types (name) KEY (name)
VALUES ('LIKE'),
       ('REVIEW'),
       ('FRIEND');

-- No explicit ids: H2 identity columns don't advance past manually inserted ids,
-- so the first POST would collide with id 1.
INSERT INTO pets (name, age, type) VALUES ('Alphonse', 4, 'CAT');
INSERT INTO pets (name, age, type) VALUES ('Edward', 4, 'CAT');
INSERT INTO pets (name, age, type) VALUES ('Sloopy', 1, 'DOG');

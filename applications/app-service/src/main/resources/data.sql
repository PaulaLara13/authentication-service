DELETE
FROM user_roles;
DELETE
FROM UserEntity;
DELETE
FROM RoleEntity;

INSERT INTO UserEntity (name, lastname, date, address, phone, email, salary, password)
VALUES ('Admin', 'Demo', DATE '1990-01-01', 'Calle 1', 3000000, 'admin@demo.com', 5000000.00, 'secret'),
       ('Advisor', 'Demo', DATE '1992-05-10', 'Calle 2', 3000001, 'advisor@demo.com', 4000000.00, 'secret'),
       ('Client', 'Demo', DATE '1995-08-20', 'Calle 3', 3000002, 'client@demo.com', 3000000.00, 'secret');

INSERT INTO RoleEntity (name)
VALUES ('ADMIN'),
       ('ADVISOR'),
       ('CLIENT');

-- Link roles using generated IDs
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM UserEntity u JOIN RoleEntity r ON u.email = 'admin@demo.com' AND r.name = 'ADMIN';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM UserEntity u JOIN RoleEntity r ON u.email = 'advisor@demo.com' AND r.name = 'ADVISOR';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM UserEntity u JOIN RoleEntity r ON u.email = 'client@demo.com' AND r.name = 'CLIENT';
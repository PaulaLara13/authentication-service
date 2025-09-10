-- Seed demo users
DELETE
FROM user_roles;
DELETE
FROM UserEntity;
DELETE
FROM RoleEntity;

INSERT INTO UserEntity (id, name, lastname, date, address, phone, email, salary, password)
VALUES (1, 'Admin', 'Demo', DATE '1990-01-01', 'Calle 1', 3000000, 'admin@demo.com', 5000000.00, 'secret'),
       (2, 'Advisor', 'Demo', DATE '1992-05-10', 'Calle 2', 3000001, 'advisor@demo.com', 4000000.00, 'secret'),
       (3, 'Client', 'Demo', DATE '1995-08-20', 'Calle 3', 3000002, 'client@demo.com', 3000000.00, 'secret');

INSERT INTO RoleEntity (id, name)
VALUES (1, 'ADMIN'),
       (2, 'ADVISOR'),
       (3, 'CLIENT');

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1),
       (2, 2),
       (3, 3);

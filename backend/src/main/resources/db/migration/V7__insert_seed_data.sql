-- 初期部署
INSERT INTO departments (id, name, version, created_at, updated_at) VALUES
(1, '開発部', 0, NOW(), NOW()),
(2, '営業部', 0, NOW(), NOW());

-- 初期ユーザー（パスワードはすべて "password" のBCryptハッシュ）
INSERT INTO employees (id, department_id, name, email, password_hash, role, active, version, created_at, updated_at) VALUES
(1, 1, '管理太郎', 'admin@example.com', '$2a$10$UgP05H4zHO3pHyg5vPlSaOMgeTeeS7fB/LR16MMSFYzpwTP9PjsSW', 'ADMIN', true, 0, NOW(), NOW()),
(2, 1, '開発花子', 'dev@example.com', '$2a$10$UgP05H4zHO3pHyg5vPlSaOMgeTeeS7fB/LR16MMSFYzpwTP9PjsSW', 'EMPLOYEE', true, 0, NOW(), NOW()),
(3, 2, '営業次郎', 'manager@example.com', '$2a$10$UgP05H4zHO3pHyg5vPlSaOMgeTeeS7fB/LR16MMSFYzpwTP9PjsSW', 'MANAGER', true, 0, NOW(), NOW());

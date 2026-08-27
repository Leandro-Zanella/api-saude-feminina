-- Carga inicial. Idempotente: só insere o que ainda não existe.

INSERT INTO tb_role (name)
SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM tb_role WHERE name = 'ADMIN');

INSERT INTO tb_role (name)
SELECT 'USER' WHERE NOT EXISTS (SELECT 1 FROM tb_role WHERE name = 'USER');

-- Senha: admin123
INSERT INTO tb_user (name, email, password, role_id, created_at)
SELECT 'Leandro Zanella', 'admin@saudefeminina.com',
       '$2a$10$Qg.0bN9nBir2KvMKhzzeO.oMNZRuz2KCpzUlF7jatoaDXRPe1myPu',
       (SELECT id FROM tb_role WHERE name = 'ADMIN'), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_user WHERE email = 'admin@saudefeminina.com');

-- Senha: maria123
INSERT INTO tb_user (name, email, password, role_id, created_at)
SELECT 'Maria Souza', 'maria@saudefeminina.com',
       '$2a$10$bGT421pVKO0vHANE7C8dvu6fb66MeH1qwINLv6VGe20MJKJC7uJoW',
       (SELECT id FROM tb_role WHERE name = 'USER'), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_user WHERE email = 'maria@saudefeminina.com');

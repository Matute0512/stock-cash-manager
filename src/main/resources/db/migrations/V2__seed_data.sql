-- =====================================================================
-- STOCK CASH MANAGER — Datos Iniciales (Seed)
-- Archivo : V2__seed_data.sql
-- Proposito: Insertar datos minimos para que el sistema sea operable.
-- IMPORTANTE: Cambiar la contrasena del admin en el primer login.
-- =====================================================================

USE stock_cash_db;

-- =====================================================================
-- USUARIO ADMINISTRADOR INICIAL
-- password: Admin1234! (hash BCrypt cost=12 — cambiar en primer login)
-- NUNCA dejar esta contrasena en produccion.
-- =====================================================================
INSERT INTO users (username, password_hash, full_name, role, active)
VALUES (
    'admin',
    '$2a$12$placeholderHashWillBeGeneratedByTheApplicationAtFirstRun',
    'Administrador del Sistema',
    'ADMIN',
    TRUE
);

-- =====================================================================
-- CATEGORIAS DE PRODUCTOS POR DEFECTO
-- El negocio puede modificarlas desde la aplicacion.
-- =====================================================================
INSERT INTO categories (name, description) VALUES
    ('General',       'Productos sin categoria especifica'),
    ('Alimentos',     'Productos alimenticios y bebidas'),
    ('Limpieza',      'Articulos de limpieza e higiene'),
    ('Electronica',   'Equipos y accesorios electronicos'),
    ('Indumentaria',  'Ropa y accesorios de vestimenta'),
    ('Papeleria',     'Articulos de oficina y papeleria');

-- =====================================================================
-- NOTA: El hash de la contrasena del admin se debe generar
-- programaticamente en el primer arranque de la aplicacion.
-- Ver: com.stockcash.security.PasswordHasher
-- =====================================================================

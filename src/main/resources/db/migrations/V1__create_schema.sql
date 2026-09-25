-- =====================================================================
-- STOCK CASH MANAGER — Schema DDL
-- Archivo : V1__create_schema.sql
-- Motor   : MySQL 8.x InnoDB
-- Autor   : Matias Torres
-- Fase    : 3 - Diseño
-- =====================================================================
-- CONVENCIONES:
--   - DECIMAL(12,2)  para montos de dinero (venta, caja)
--   - DECIMAL(12,4)  para costos con PPP (4 decimales de precision)
--   - BIGINT         para IDs de tablas de alta cardinalidad (ventas, movimientos)
--   - INT            para tablas de baja cardinalidad (categorias)
--   - BOOLEAN        para flags activo/inactivo (baja logica)
--   - Todas las tablas en snake_case, columnas en snake_case
-- =====================================================================

CREATE DATABASE IF NOT EXISTS stock_cash_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE stock_cash_db;

-- =====================================================================
-- TABLA: users
-- Proposito: Almacena todos los usuarios del sistema con sus roles.
-- Seguridad: password_hash nunca contiene texto plano (siempre BCrypt).
-- failed_attempts y locked_until implementan el bloqueo por intentos (FR-AUTH-03).
-- =====================================================================
CREATE TABLE IF NOT EXISTS users (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    username       VARCHAR(50)   NOT NULL,
    password_hash  VARCHAR(100)  NOT NULL,
    full_name      VARCHAR(100)  NOT NULL,
    role           ENUM('ADMIN','SUPERVISOR','CAJERO') NOT NULL,
    active         BOOLEAN       NOT NULL DEFAULT TRUE,
    failed_attempts TINYINT      NOT NULL DEFAULT 0,
    locked_until   DATETIME      NULL,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_users         PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- TABLA: categories
-- Proposito: Clasificacion de productos. Permite agrupar y filtrar.
-- =====================================================================
CREATE TABLE IF NOT EXISTS categories (
    id          INT           NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(255)  NULL,
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_categories    PRIMARY KEY (id),
    CONSTRAINT uq_category_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- TABLA: products
-- Proposito: Catalogo central de productos con control de stock.
-- version: columna para Optimistic Locking (NFR-REL-04).
-- cost_price: DECIMAL(12,4) para soportar el calculo PPP con precision (BR-09).
-- CHECK constraints garantizan integridad de datos a nivel de BD.
-- =====================================================================
CREATE TABLE IF NOT EXISTS products (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    sku          VARCHAR(50)   NOT NULL,
    name         VARCHAR(150)  NOT NULL,
    description  TEXT          NULL,
    category_id  INT           NOT NULL,
    cost_price   DECIMAL(12,4) NOT NULL,
    sale_price   DECIMAL(12,2) NOT NULL,
    stock        INT           NOT NULL DEFAULT 0,
    min_stock    INT           NOT NULL DEFAULT 0,
    active       BOOLEAN       NOT NULL DEFAULT TRUE,
    version      INT           NOT NULL DEFAULT 0,
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_products          PRIMARY KEY (id),
    CONSTRAINT uq_products_sku      UNIQUE (sku),
    CONSTRAINT fk_product_category  FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT chk_stock_positive   CHECK (stock >= 0),
    CONSTRAINT chk_cost_positive    CHECK (cost_price >= 0),
    CONSTRAINT chk_sale_positive    CHECK (sale_price >= 0),
    CONSTRAINT chk_min_stock_positive CHECK (min_stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- TABLA: suppliers
-- Proposito: Registro de proveedores del negocio.
-- tax_id: CUIT en Argentina u otro identificador fiscal segun pais.
-- =====================================================================
CREATE TABLE IF NOT EXISTS suppliers (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    company_name VARCHAR(150) NOT NULL,
    tax_id       VARCHAR(20)  NOT NULL,
    phone        VARCHAR(30)  NULL,
    email        VARCHAR(100) NULL,
    address      VARCHAR(200) NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_suppliers      PRIMARY KEY (id),
    CONSTRAINT uq_supplier_taxid UNIQUE (tax_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- TABLA: cash_registers
-- Proposito: Representa una sesion de caja (apertura a cierre).
-- status REQUIRES_REVIEW: caja que requiere atencion del admin
--   tras un crash de sesion del usuario (FR-CASH-09 / CU-03 Flujo C).
-- =====================================================================
CREATE TABLE IF NOT EXISTS cash_registers (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    user_id        BIGINT        NOT NULL,
    opening_amount DECIMAL(12,2) NOT NULL,
    closing_amount DECIMAL(12,2) NULL,
    status         ENUM('OPEN','CLOSED','REQUIRES_REVIEW') NOT NULL DEFAULT 'OPEN',
    opened_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at      DATETIME      NULL,
    notes          VARCHAR(255)  NULL,
    CONSTRAINT pk_cash_registers     PRIMARY KEY (id),
    CONSTRAINT fk_cashreg_user       FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- TABLA: sales
-- Proposito: Cabecera de cada venta realizada.
-- payment_method: determina como se registra en cash_movements (FR-POS-07).
-- status CANCELLED: baja logica de ventas anuladas (BR-03).
-- cancelled_by_user_id: auditoria de quien anulo la venta.
-- =====================================================================
CREATE TABLE IF NOT EXISTS sales (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    user_id               BIGINT        NOT NULL,
    cash_register_id      BIGINT        NOT NULL,
    payment_method        ENUM('CASH','CARD','TRANSFER') NOT NULL,
    subtotal              DECIMAL(12,2) NOT NULL,
    discount_percentage   DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    discount_amount       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total                 DECIMAL(12,2) NOT NULL,
    status                ENUM('COMPLETED','CANCELLED') NOT NULL DEFAULT 'COMPLETED',
    cancelled_by_user_id  BIGINT        NULL,
    cancelled_at          DATETIME      NULL,
    cancellation_reason   VARCHAR(255)  NULL,
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_sales              PRIMARY KEY (id),
    CONSTRAINT fk_sale_user          FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_sale_cashregister  FOREIGN KEY (cash_register_id) REFERENCES cash_registers(id),
    CONSTRAINT fk_sale_cancelled_by  FOREIGN KEY (cancelled_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- TABLA: sale_items
-- Proposito: Detalle de productos de cada venta.
-- unit_price: PRECIO HISTORICO al momento de la venta.
--   Si el precio del producto cambia despues, este registro no se modifica.
--   Garantiza integridad de reportes financieros historicos.
-- =====================================================================
CREATE TABLE IF NOT EXISTS sale_items (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    sale_id     BIGINT        NOT NULL,
    product_id  BIGINT        NOT NULL,
    quantity    INT           NOT NULL,
    unit_price  DECIMAL(12,2) NOT NULL,
    subtotal    DECIMAL(12,2) NOT NULL,
    CONSTRAINT pk_sale_items       PRIMARY KEY (id),
    CONSTRAINT fk_saleitem_sale    FOREIGN KEY (sale_id) REFERENCES sales(id),
    CONSTRAINT fk_saleitem_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_saleitem_qty    CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- TABLA: cash_movements
-- Proposito: Registro de TODOS los movimientos de dinero en una caja.
-- PATRON: amount siempre positivo, la direccion la da el tipo (ENUM).
-- sale_id: nullable — solo se llena en movimientos de tipo SALE_* y SALE_REFUND_CASH.
-- Tipos que afectan saldo fisico de efectivo:
--   INGRESO: OPENING, SALE_CASH, MANUAL_INCOME
--   EGRESO:  SALE_REFUND_CASH, MANUAL_EXPENSE
-- Tipos informativos (no afectan saldo fisico):
--   SALE_CARD, SALE_TRANSFER
-- =====================================================================
CREATE TABLE IF NOT EXISTS cash_movements (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    cash_register_id BIGINT        NOT NULL,
    user_id          BIGINT        NOT NULL,
    sale_id          BIGINT        NULL,
    type             ENUM(
                       'OPENING',
                       'SALE_CASH',
                       'SALE_CARD',
                       'SALE_TRANSFER',
                       'SALE_REFUND_CASH',
                       'MANUAL_INCOME',
                       'MANUAL_EXPENSE'
                     ) NOT NULL,
    amount           DECIMAL(12,2) NOT NULL,
    description      VARCHAR(255)  NOT NULL,
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cash_movements      PRIMARY KEY (id),
    CONSTRAINT fk_movement_register   FOREIGN KEY (cash_register_id) REFERENCES cash_registers(id),
    CONSTRAINT fk_movement_user       FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_movement_sale       FOREIGN KEY (sale_id) REFERENCES sales(id),
    CONSTRAINT chk_movement_amount    CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- TABLA: purchase_orders
-- Proposito: Cabecera de cada compra registrada a un proveedor.
-- Dispara la actualizacion de stock y PPP (FR-SUP-04, BR-09).
-- =====================================================================
CREATE TABLE IF NOT EXISTS purchase_orders (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    supplier_id  BIGINT        NOT NULL,
    user_id      BIGINT        NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    notes        VARCHAR(255)  NULL,
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_purchase_orders   PRIMARY KEY (id),
    CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_purchase_user     FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- TABLA: purchase_order_items
-- Proposito: Detalle de productos de cada orden de compra.
-- previous_cost: costo ANTES de aplicar el PPP.
-- new_ppp_cost:  costo calculado con PPP (BR-09).
-- unit_cost:     precio unitario PAGADO en esta compra especifica.
-- Los tres juntos permiten auditar completamente la evolucion del costo.
-- =====================================================================
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    purchase_order_id BIGINT        NOT NULL,
    product_id        BIGINT        NOT NULL,
    quantity          INT           NOT NULL,
    unit_cost         DECIMAL(12,4) NOT NULL,
    previous_cost     DECIMAL(12,4) NOT NULL,
    new_ppp_cost      DECIMAL(12,4) NOT NULL,
    subtotal          DECIMAL(12,2) NOT NULL,
    CONSTRAINT pk_poi         PRIMARY KEY (id),
    CONSTRAINT fk_poi_order   FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_poi_qty    CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- TABLA: audit_log
-- Proposito: Registro inmutable de eventos criticos del sistema.
-- user_id NULL: permite registrar eventos del sistema sin usuario (ej: bloqueo automatico).
-- RESTRICCION: esta tabla NO tiene UPDATE ni DELETE en la aplicacion (FR-AUD-04).
-- =====================================================================
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NULL,
    event_type  VARCHAR(50)  NOT NULL,
    description TEXT         NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_audit_log  PRIMARY KEY (id),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- INDICES DE RENDIMIENTO
-- Ordenados por modulo y caso de uso que los justifica
-- =====================================================================

-- Autenticacion (FR-AUTH-01)
CREATE INDEX idx_users_username        ON users(username);
CREATE INDEX idx_users_active          ON users(active);

-- Busqueda y alertas de productos (FR-PROD-04, FR-PROD-06)
CREATE INDEX idx_products_sku          ON products(sku);
CREATE INDEX idx_products_name         ON products(name);
CREATE INDEX idx_products_active       ON products(active);
CREATE INDEX idx_products_category     ON products(category_id, active);
CREATE INDEX idx_products_stock_alert  ON products(stock, min_stock, active);

-- Reportes de ventas por periodo (FR-REP-01)
CREATE INDEX idx_sales_created         ON sales(created_at);
CREATE INDEX idx_sales_status_date     ON sales(status, created_at);
CREATE INDEX idx_sales_cashregister    ON sales(cash_register_id);
CREATE INDEX idx_sales_user            ON sales(user_id, created_at);

-- Consulta de items de venta (FR-REP-04 - top productos)
CREATE INDEX idx_sale_items_sale       ON sale_items(sale_id);
CREATE INDEX idx_sale_items_product    ON sale_items(product_id);

-- Control de caja (FR-CASH-02, FR-CASH-09)
CREATE INDEX idx_cashregs_status       ON cash_registers(status);
CREATE INDEX idx_cashregs_user_status  ON cash_registers(user_id, status);

-- Movimientos de caja (FR-CASH-06, FR-CASH-03)
CREATE INDEX idx_movements_reg_type    ON cash_movements(cash_register_id, type);
CREATE INDEX idx_movements_created     ON cash_movements(created_at);

-- Historial de compras por proveedor (FR-SUP-06)
CREATE INDEX idx_purchase_supplier     ON purchase_orders(supplier_id, created_at);

-- Auditoria (FR-AUD-03)
CREATE INDEX idx_audit_event_date      ON audit_log(event_type, created_at);
CREATE INDEX idx_audit_user_date       ON audit_log(user_id, created_at);

# 🖥️ Wireframes de Interfaz de Usuario
## Sistema de Control de Inventario y Caja
### `stock-cash-manager` — Fase 3: Diseño (Parte 3/3 — UI)

---

> **Versión:** 1.0.0
> **Fecha:** 2026-09-25
> **Tecnología:** JavaFX 21 + FXML + CSS
> **Autor:** Matias Torres

---

## 1. Convenciones de Diseño

### Paleta de Colores

| Token | Color | Hex | Uso |
|-------|-------|-----|-----|
| `primary` | Azul Navy | `#1E2A3A` | Sidebar, botones primarios |
| `accent` | Azul | `#2563EB` | Botón de acción principal |
| `success` | Verde | `#16A34A` | Confirmar venta, estado OK |
| `warning` | Naranja | `#D97706` | Alertas de stock bajo |
| `danger` | Rojo | `#DC2626` | Errores, anulaciones |
| `background` | Gris claro | `#F3F4F6` | Fondo de pantallas |
| `surface` | Blanco | `#FFFFFF` | Cards, paneles, tablas |
| `text-primary` | Gris oscuro | `#111827` | Texto principal |
| `text-secondary` | Gris medio | `#6B7280` | Labels secundarios |

### Tipografía
- **Fuente:** System default (garantiza consistencia en Windows)
- **Título de pantalla:** 20px Bold
- **Labels de campo:** 13px Regular
- **Datos en tabla:** 13px Regular
- **Total/monto destacado:** 24px Bold

### Componentes JavaFX reutilizables
| Componente | Clase JavaFX | Uso |
|-----------|-------------|-----|
| Barra de búsqueda | `TextField` + `Button` | Búsqueda de productos |
| Tabla de datos | `TableView<T>` | Listados de todas las entidades |
| Selector de opciones | `ComboBox<T>` | Rol, método de pago, categoría |
| Botón primario | `Button .btn-primary` | Acción principal de cada pantalla |
| Botón secundario | `Button .btn-secondary` | Cancelar, volver |
| Badge de estado | `Label .badge-*` | Estado activo/inactivo, stock bajo |
| Diálogo de confirmación | `Alert.CONFIRMATION` | Anulaciones, cierres de caja |

---

## 2. Mapa de Navegación

```
[LOGIN]
    │
    ▼
[DASHBOARD] ──────────────────────────────────────────────┐
    │                                                       │
    ├── [PUNTO DE VENTA] ──→ [RECIBO DE VENTA (modal)]    │
    │                                                       │
    ├── [PRODUCTOS]                                         │
    │       ├── [LISTA DE PRODUCTOS]                       │
    │       └── [FORMULARIO PRODUCTO (crear/editar)]       │
    │                                                       │
    ├── [PROVEEDORES]                                       │
    │       ├── [LISTA DE PROVEEDORES]                     │
    │       └── [REGISTRAR COMPRA A PROVEEDOR]             │
    │                                                       │
    ├── [CAJA]                                              │
    │       ├── [APERTURA DE CAJA]                         │
    │       ├── [VISTA DE CAJA ACTIVA]                     │
    │       └── [CIERRE DE CAJA (modal)]                   │
    │                                                       │
    ├── [REPORTES]                                          │
    │       ├── [VENTAS POR PERÍODO]                       │
    │       ├── [STOCK BAJO]                               │
    │       └── [TOP PRODUCTOS]                            │
    │                                                       │
    └── [USUARIOS] (solo ADMIN)                             │
            ├── [LISTA DE USUARIOS]                         │
            └── [FORMULARIO USUARIO (crear/editar)]         │
                                                            │
    [CERRAR SESIÓN] ◄───────────────────────────────────────┘
```

---

## 3. Pantallas — Mockups Visuales

### Pantalla 1 — Login

![Login Screen](C:\Users\Matia\.gemini\antigravity\brain\14673c75-01c8-4a4d-8acf-980f695305b8\wireframe_login_1790345580640.jpg)

**Descripción técnica:**
- Layout: `BorderPane` → left: panel oscuro con logo, center: `VBox` centrado con card
- El logo "SCM" se implementa como `Label` con estilo CSS o `ImageView`
- Campo Usuario: `TextField` con `promptText="Usuario"` + ícono con CSS `background-image`
- Campo Contraseña: `PasswordField` con `promptText="Contraseña"`
- Botón Ingresar: deshabilitado si ambos campos están vacíos (binding)
- "Conexión segura": `Label` con ícono shield — solo estético, refuerza confianza

**Comportamiento especial:**
- Al llegar a 3 intentos fallidos: label rojo "Intentos restantes: 2"
- Al llegar a 5: label "Cuenta bloqueada por 15 minutos" + contador regresivo
- `Enter` en cualquier campo dispara el login

---

### Pantalla 2 — Dashboard Principal

![Dashboard](C:\Users\Matia\.gemini\antigravity\brain\14673c75-01c8-4a4d-8acf-980f695305b8\wireframe_dashboard_1790345594745.jpg)

**Descripción técnica:**
- Layout: `BorderPane` → left: `VBox` sidebar, center: `BorderPane` con top bar + content
- Sidebar: `ListView` o botones `Button` con estilos CSS para ítem activo/hover
- Cards métricas: `HBox` de 4 `VBox` cards con `DropShadow` CSS
- Tabla ventas recientes: `TableView<Sale>` con 5 columnas

**RBAC en la UI:**
- El ítem "Usuarios" del sidebar solo se muestra si `role == ADMIN`
- Los ítems se ocultan con `menuItem.setVisible(hasPermission)` en `initialize()`

**Datos en tiempo real:**
- Las métricas se cargan en un `Task<>` de JavaFX (operación en background)
- El indicador de carga (NFR-USA-04) se implementa con `ProgressIndicator`

---

### Pantalla 3 — Punto de Venta (POS)

![Punto de Venta](C:\Users\Matia\.gemini\antigravity\brain\14673c75-01c8-4a4d-8acf-980f695305b8\wireframe_pos_1790345628288.jpg)

**Descripción técnica:**
- Layout: `HBox` dividido 60/40 con `SplitPane` o proporciones con `HGrow`
- **Panel izquierdo:** `VBox` con `TextField` de búsqueda + `ListView<Product>`
  - Búsqueda: listener en `textProperty()` que filtra con retraso de 300ms
  - Cada celda: `ListCell` customizada con nombre, precio y badge de stock
- **Panel derecho:** `VBox` con:
  - `TableView<SaleItemRequestDTO>` editable (cantidad modificable inline)
  - `Label` para subtotal, descuento y total (bindeado a la lista)
  - `ComboBox<PaymentMethod>` — obligatorio para habilitar el botón
  - `Button "Confirmar Venta"` — deshabilitado si carrito vacío o sin método de pago

**Barra de estado inferior:**
- `Label` con estado de caja: `"🟢 Caja abierta | Cajero: Matias Torres"`
- Si no hay caja: `"🔴 Sin caja abierta — POS bloqueado"` y botón deshabilitado

**Descuento (solo ADMIN/SUPERVISOR):**
- `TextField` de porcentaje visible solo si `role != CAJERO`
- Binding: `totalLabel = subtotal × (1 - descuento/100)`

---

### Pantalla 4 — Cierre de Caja (Dialog Modal)

![Cierre de Caja](C:\Users\Matia\.gemini\antigravity\brain\14673c75-01c8-4a4d-8acf-980f695305b8\wireframe_cash_close_1790345705202.jpg)

**Descripción técnica:**
- Implementado como `Stage` secundario con `Modality.APPLICATION_MODAL`
- Layout: `VBox` con `GridPane` para la tabla de arqueo
- El `CashClosingSummaryDTO` popula todos los `Label` de la tabla
- Campo "Efectivo contado": `TextField` con validador numérico
- `Label "Diferencia"`: bindeado a `countedCash - expectedCash`
  - Verde si diferencia == 0 o positiva (sobrante)
  - Rojo si diferencia negativa (faltante)
- Botón "Confirmar Cierre": requiere confirmación secundaria `Alert.CONFIRMATION`

---

## 4. Pantallas Adicionales — Descripción Funcional

### Pantalla 5 — Lista de Productos
```
┌─────────────────────────────────────────────────────────┐
│  📦 Productos                    [+ Nuevo Producto]      │
│                                                         │
│  Buscar: [________________] Categoría: [Todas ▼]        │
│                                                         │
│ ┌────┬──────────────┬──────────┬────────┬───────┬────┐  │
│ │SKU │ Nombre       │ Precio   │ Stock  │ Estado│    │  │
│ ├────┼──────────────┼──────────┼────────┼───────┼────┤  │
│ │001 │ Producto A   │ $1,200   │  🔴 2  │ ✅    │ ✏️ │  │
│ │002 │ Producto B   │ $850     │  15    │ ✅    │ ✏️ │  │
│ │003 │ Producto C   │ $3,400   │  0     │ ❌    │ ✏️ │  │
│ └────┴──────────────┴──────────┴────────┴───────┴────┘  │
└─────────────────────────────────────────────────────────┘
```
- Stock bajo (≤ mínimo): badge rojo con número
- Producto inactivo: fila con opacidad reducida + badge ❌
- Botón ✏️ abre formulario en modo edición

### Pantalla 6 — Formulario de Producto (Crear/Editar)
```
Campos:
  [SKU *]           [Nombre *]
  [Categoría * ▼]   [Estado ✅]
  [Precio Costo *]  [Precio Venta *]   ⚠️ Margen: 25%
  [Stock Actual *]  [Stock Mínimo *]
  [Descripción (textarea)]

  [Cancelar]  [Guardar Producto]
```
- `⚠️ Margen: 25%` calculado en tiempo real (BR-05)
- Si margen negativo: badge naranja de advertencia

### Pantalla 7 — Vista de Caja Activa
```
┌─────────────────────────────────────────────────────────┐
│  💰 Caja Activa                                          │
│  Apertura: hoy 09:15  |  Monto inicial: $500.00         │
│                                                         │
│  SALDO ACTUAL EN EFECTIVO:         $4,750.00  ←grande   │
│                                                         │
│  [+ Ingreso Manual]  [- Egreso Manual]  [Cerrar Caja]   │
│                                                         │
│  Movimientos del día:                                   │
│  09:15  APERTURA              +$500.00    Efectivo      │
│  10:23  Venta #001            +$1,500.00  Efectivo      │
│  11:05  Venta #002            (informativo) Tarjeta     │
│  11:47  Egreso: Pago limpieza -$150.00    Manual        │
└─────────────────────────────────────────────────────────┘
```

### Pantalla 8 — Recibo de Venta (Modal post-confirmación)
```
┌────────────────────────────────┐
│     STOCK CASH MANAGER         │
│  ──────────────────────────── │
│  Venta #00142                  │
│  25/09/2026  11:30:45          │
│  Cajero: Matias Torres         │
│  ──────────────────────────── │
│  Producto A    x2   $2,400     │
│  Producto B    x1   $850       │
│  ──────────────────────────── │
│  Subtotal:          $3,250     │
│  Descuento (5%):    -$162.50   │
│  TOTAL:             $3,087.50  │
│  Método: EFECTIVO              │
│  ──────────────────────────── │
│       ¡Gracias por su compra!  │
│                                │
│   [Imprimir]  [Cerrar]         │
└────────────────────────────────┘
```

---

## 5. Inventario Completo de Pantallas

| # | Pantalla | Archivo FXML | Controller | Roles |
|---|---------|-------------|-----------|-------|
| 1 | Login | `login.fxml` | `LoginController` | Todos |
| 2 | Dashboard | `dashboard.fxml` | `DashboardController` | Todos |
| 3 | Punto de Venta | `pos.fxml` | `PosController` | Todos |
| 4 | Recibo (modal) | `receipt.fxml` | `ReceiptController` | Todos |
| 5 | Lista Productos | `product-list.fxml` | `ProductListController` | Todos |
| 6 | Formulario Producto | `product-form.fxml` | `ProductFormController` | ADM, SUP |
| 7 | Lista Proveedores | `supplier-list.fxml` | `SupplierListController` | ADM, SUP |
| 8 | Registrar Compra | `purchase-order.fxml` | `PurchaseOrderController` | ADM, SUP |
| 9 | Vista Caja Activa | `cash-register.fxml` | `CashRegisterController` | Todos |
| 10 | Cierre de Caja (modal) | `cash-close.fxml` | `CashCloseController` | Todos |
| 11 | Lista Usuarios | `user-list.fxml` | `UserListController` | ADM |
| 12 | Formulario Usuario | `user-form.fxml` | `UserFormController` | ADM |
| 13 | Reportes | `report.fxml` | `ReportController` | ADM, SUP |
| 14 | Recuperación Caja (modal) | `cash-recovery.fxml` | `CashRecoveryController` | Todos |

**Total: 14 vistas FXML** — 14 Controllers — 1 SceneManager central

---

*Wireframes v1.0 — aprobados como base para la Fase 4: Implementación.*

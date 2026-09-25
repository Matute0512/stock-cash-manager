<div align="center">

# 📦 Stock Cash Manager
### Sistema de Control de Inventario y Caja

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven)
![Status](https://img.shields.io/badge/Status-En%20Desarrollo-yellow?style=for-the-badge)

*Aplicación de escritorio para gestión integral de inventario y punto de venta*

</div>

---

## 📋 Descripción

**Stock Cash Manager** es un sistema de escritorio desarrollado en Java con JavaFX que permite gestionar el ciclo completo de un comercio: inventario de productos, proveedores, punto de venta, control de caja y reportes gerenciales.

> **Proyecto de portafolio profesional** — desarrollado siguiendo las 7 fases del SDLC, arquitectura en capas (Clean Architecture), principios SOLID y estándares de seguridad de nivel bancario.

---

## ✨ Funcionalidades

| Módulo | Descripción | Estado |
|--------|-------------|:------:|
| 🔐 Autenticación | Login seguro con BCrypt y control de intentos | 🔄 Pendiente |
| 👥 Gestión de Usuarios | CRUD con sistema de roles (ADMIN/SUPERVISOR/CAJERO) | 🔄 Pendiente |
| 📦 Gestión de Productos | CRUD, categorías, alertas de stock mínimo | 🔄 Pendiente |
| 🚚 Proveedores | Gestión de proveedores e historial de compras | 🔄 Pendiente |
| 🛒 Punto de Venta | Carrito, descuentos, emisión de recibos | 🔄 Pendiente |
| 💰 Control de Caja | Apertura/cierre, arqueo, movimientos | 🔄 Pendiente |
| 📊 Reportes | Ventas, stock, movimientos de caja | 🔄 Pendiente |

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────┐
│  PRESENTACIÓN — JavaFX + FXML       │  Controllers
├─────────────────────────────────────┤
│  NEGOCIO — Business Logic           │  Services + Validators
├─────────────────────────────────────┤
│  DATOS — Acceso a Datos             │  Repositories (DAO)
├─────────────────────────────────────┤
│  INFRAESTRUCTURA                    │  MySQL + Logging
└─────────────────────────────────────┘
```

**Patrones aplicados:** Singleton, DAO, Factory Method, Observer, Session Object

---

## 🔒 Seguridad

- **Contraseñas:** Hashing con BCrypt (cost factor 12)
- **SQL Injection:** 100% Prepared Statements
- **Validación:** Regex + sanitización en capa de servicio
- **Sesiones:** Objeto en memoria, sin persistencia en disco
- **RBAC:** Control de acceso granular por rol
- **Auditoría:** Log de eventos críticos con SLF4J + Logback
- **Credenciales:** Externalizadas fuera del repositorio

---

## 🛠️ Stack Tecnológico

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 17 (LTS) | Lenguaje principal |
| JavaFX | 21.0.3 | Interfaz de usuario |
| MySQL | 8.x | Base de datos |
| jBCrypt | 0.4 | Hashing de contraseñas |
| SLF4J + Logback | 2.0 / 1.5 | Logging y auditoría |
| JUnit 5 | 5.10 | Tests unitarios |
| Mockito | 5.11 | Mocking en tests |
| Maven | 3.x | Build y dependencias |

---

## 🚀 Instalación y Configuración

### Prerrequisitos

- JDK 17 o superior
- MySQL 8.x
- Maven 3.8+
- IntelliJ IDEA (recomendado)

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/Matute0512/stock-cash-manager.git
cd stock-cash-manager

# 2. Configurar la base de datos
# Copiar el archivo de ejemplo y completar tus credenciales
cp src/main/resources/config/database.properties.example \
   src/main/resources/config/database.properties
# Editar database.properties con tus datos de MySQL

# 3. Ejecutar los scripts SQL de creacion de BD
# (ver docs/fase3-diseno/ cuando estén disponibles)

# 4. Compilar y ejecutar
mvn javafx:run
```

> ⚠️ **Importante:** El archivo `database.properties` está en `.gitignore` y **nunca** debe subirse al repositorio.

---

## 📁 Estructura del Proyecto

```
stock-cash-manager/
├── src/main/java/com/stockcash/
│   ├── app/          # Punto de entrada (MainApp)
│   ├── controller/   # Controllers JavaFX
│   ├── service/      # Lógica de negocio
│   ├── repository/   # Acceso a datos (DAO)
│   ├── model/        # Entidades de dominio
│   ├── dto/          # Data Transfer Objects
│   ├── security/     # Autenticación y sesión
│   ├── config/       # Configuración de BD
│   └── util/         # Utilidades y validadores
├── src/main/resources/
│   ├── fxml/         # Vistas JavaFX
│   ├── css/          # Estilos
│   ├── config/       # Archivos de configuración
│   └── db/           # Scripts SQL
├── src/test/         # Tests unitarios e integración
└── docs/             # Documentación técnica por fase SDLC
```

---

## 📚 Documentación

La documentación técnica está organizada por fases del SDLC en la carpeta [`docs/`](docs/).

| Fase | Documento |
|------|-----------|
| Fase 1 — Planificación | [Project Charter](docs/fase1-planificacion/) |
| Fase 2 — Requisitos | [SRS Document](docs/fase2-requisitos/) *(próximamente)* |
| Fase 3 — Diseño | [Architecture + DB Design](docs/fase3-diseno/) *(próximamente)* |

---

## 👤 Autor

**Matias Torres**
- GitHub: [@Matute0512](https://github.com/Matute0512)

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

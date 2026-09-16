# ⚡ ECOGO Web — Buscador de Estaciones de Carga

`Java 17` `Spring Boot 4.1` `PostgreSQL` `JPA / Hibernate` `React` `Leaflet`

---

## 🎯 Finalidad de la Aplicación

ECOGO Web es la versión de escritorio y navegador de la plataforma, pensada para localizar, filtrar y consultar estaciones de carga para vehículos eléctricos e híbridos enchufables en Argentina, sin necesidad de instalar una app.

Comparte propósito y modelo de dominio con la versión Android nativa, pero está construida sobre una arquitectura cliente-servidor: un backend propio expone la información de las estaciones vía API REST, y cualquier cliente web puede consumirla.

### 💡 Problemas clave que resuelve

- **Acceso sin instalación:** el usuario entra desde el navegador, sin descargar nada ni crear cuenta para consultar el mapa.
- **Fuente de datos centralizada:** las estaciones se importan y persisten en una base propia, en lugar de depender de una consulta en vivo a un servicio externo en cada uso.
- **Filtros por compatibilidad técnica:** permite acotar las estaciones por potencia (kW) y tipo de conector (CCS Tipo 2, Tipo 2 Mennekes, CHAdeMO, Schuko).
- **Navegación GPS inmediata:** integración de un toque hacia Google Maps ("Cómo llegar") para trazar la ruta hasta la estación elegida.

---

## 🛠️ Tecnologías Utilizadas

### Backend
- **Lenguaje:** Java 17
- **Framework:** Spring Boot 4.1 (Spring Web, Spring Data JPA, Validation)
- **Persistencia:** Hibernate ORM 7.4 sobre PostgreSQL 18
- **Base de datos:** PostgreSQL alojado en Neon (serverless, región `sa-east-1`)
- **Pool de conexiones:** HikariCP
- **Servidor embebido:** Apache Tomcat 11
- **Cliente HTTP:** `RestClient` de Spring, para la ingesta desde Open Charge Map
- **Build:** Maven + Spring Boot Maven Plugin

### Frontend
- **Librería:** React + Vite
- **Mapas:** Leaflet con tiles de OpenStreetMap
- **Consumo de API:** `fetch` contra el backend propio

### Datos
- **Open Charge Map API** — registro abierto de puntos de carga, licenciado bajo CC BY 4.0. Se importa el conjunto correspondiente a Argentina (`countrycode=AR`) y se persiste localmente.

### Gestión de secretos
Las credenciales de base de datos y la API key de Open Charge Map se inyectan desde `application-local.properties`, excluido del control de versiones mediante `.gitignore`. El repositorio no contiene credenciales.

---

## 📁 Estructura del Proyecto

El backend sigue una arquitectura **monolítica modular en tres capas** (MVC), organizada por tipo de componente:

```
com.ecogo.demo/
├── controller/                       # Capa de Presentación (API REST)
│   └── EstacionController.java       # Endpoints de consulta e importación
├── service/                          # Capa de Negocio
│   ├── EstacionService.java          # Lógica de consulta y filtrado
│   └── OcmImportService.java         # Ingesta desde Open Charge Map
├── repository/                       # Capa de Acceso a Datos
│   └── EstacionRepository.java       # Spring Data JPA
├── model/                            # Modelos del Dominio
│   ├── entity/
│   │   └── Estacion.java             # Entidad de la estación de carga
│   └── enums/
│       ├── TipoConector.java         # TIPO2, CCS, CHADEMO, SCHUKO, OTRO
│       └── EstadoEstacion.java       # DISPONIBLE, OCUPADA, FUERA_DE_SERVICIO
├── dto/                              # Objetos de Transferencia
│   └── OcmPoi.java                   # Mapeo del JSON de Open Charge Map
├── exception/                        # Manejo de Errores
│   └── EstacionNoEncontradaException.java
├── config/                           # Configuración transversal
└── DemoApplication.java              # Punto de entrada de la aplicación
```

### Decisiones de arquitectura

**Monolito modular, no microservicios.** Para el alcance del MVP y el tamaño del equipo, un único artefacto desplegable evita la complejidad operativa de la orquestación distribuida sin perder separación de responsabilidades. La organización por capas permite extraer módulos a futuro si el proyecto escalara.

**Base relacional.** El modelo presenta relaciones explícitas entre usuario, favoritos y estación, con integridad referencial. PostgreSQL además habilita, a futuro, consultas geoespaciales mediante la extensión PostGIS.

**Aplanamiento de conexiones.** Open Charge Map modela cada estación con un arreglo de conexiones. Tras analizar el conjunto argentino, se verificó que la mayoría de los registros presenta una única conexión, por lo que se persiste la de mayor potencia en la propia entidad `Estacion`. Es una simplificación consciente del MVP.

---

## 🔌 API REST

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/estaciones` | Lista todas las estaciones |
| `GET` | `/api/estaciones/{id}` | Detalle de una estación |
| `POST` | `/api/estaciones/importar` | Importa el dataset de Argentina desde Open Charge Map |

> ⚠️ El endpoint de importación es de uso administrativo y temporal. Debe protegerse o retirarse antes de cualquier despliegue público.

### Modelo de datos

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | Identificador interno |
| `nombre` | `String` | Nombre de la estación |
| `direccion` | `String` | Dirección |
| `ciudad` | `String` | Ciudad o provincia |
| `latitud` / `longitud` | `Double` | Coordenadas geográficas |
| `potenciaKw` | `Double` | Potencia en kilovatios |
| `tipoConector` | `enum` | Tipo de conector |
| `estado` | `enum` | Estado de disponibilidad |
| `idExterno` | `Integer` | ID en Open Charge Map (único) |

---

## 🚀 Cómo Levantar y Probar el Proyecto

### 📋 Requisitos Previos

- **JDK 17** o posterior
- **Visual Studio Code** con *Extension Pack for Java* y *Spring Boot Extension Pack*, o IntelliJ IDEA
- El archivo `application-local.properties` con las credenciales de la base (solicitar al equipo)
- Conexión a internet — la base está alojada en la nube

### ⚙️ Pasos para ejecutar

**1. Clonar el repositorio**

```bash
git clone https://github.com/lautaro2427/Ecogo.git

```

**2. Configurar las claves locales**

Colocar el archivo `application-local.properties` en `src/main/resources/`, con el siguiente formato:

```properties
spring.datasource.url=jdbc:postgresql://<host>.sa-east-1.aws.neon.tech/neondb?sslmode=require
spring.datasource.username=<usuario>
spring.datasource.password=<contraseña>
ocm.api.key=<api_key_de_open_charge_map>
```

> El archivo está excluido del repositorio. Solicitarlo al equipo por un canal privado.

**3. Compilar el proyecto**

```bash
./mvnw clean install
```

**4. Ejecutar la aplicación**

Desde la terminal:

```bash
./mvnw spring-boot:run
```

O desde el IDE, ejecutando `DemoApplication` (tecla `F5` en VS Code).

**5. Verificar que funciona**

La consola debe mostrar:

```
HikariPool-1 - Start completed.
Tomcat started on port 8080 (http)
Started DemoApplication
```

Abrir en el navegador:

```
http://localhost:8080/api/estaciones
```

Debe responder con el listado de estaciones en formato JSON.

---

## ⚠️ Notas para el equipo

La base de datos es **compartida por todos los integrantes**. Tener en cuenta:

- El endpoint `/api/estaciones/importar` ya fue ejecutado. No es necesario volver a correrlo.
- Cualquier modificación a las entidades altera el esquema para todos, ya que `ddl-auto` está configurado en `update`. Avisar al equipo antes de hacerlo.
- Las credenciales no deben subirse al repositorio bajo ninguna circunstancia.

---

## 📌 Estado Actual

**Implementado:**
- Modelo de dominio y persistencia de estaciones
- API REST de consulta (listado y detalle)
- Importación automatizada desde Open Charge Map

**En desarrollo:**
- Búsqueda por texto y filtros combinados
- Frontend React con mapa interactivo
- Registro, autenticación y favoritos

---

## 👥 Créditos de datos

Los datos de estaciones provienen de [Open Charge Map](https://openchargemap.org), bajo licencia Creative Commons Attribution 4.0 International (CC BY 4.0).

La cartografía base proviene de [OpenStreetMap](https://www.openstreetmap.org), bajo licencia Open Database License (ODbL).

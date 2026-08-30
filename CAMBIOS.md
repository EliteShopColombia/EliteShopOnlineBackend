# Cambios realizados — Plan de Corrección de Deuda Técnica

Fecha: 2026-08-29

---

## Contexto

Se detectó que los usuarios podían registrarse con nombres de departamento y ciudad
arbitrarios (por ejemplo, "Putita", "Testiculo") sin validación contra la base de
datos de ubicaciones reales. Esto afectaba módulos de Auth, Customer, Seller, Order
y Checkout. Se implementó un plan completo de 7 fases para corregir el problema y
resolver 16 hallazgos asociados (H-01 a H-16).

---

## Fase 1 — Migraciones de BD y corrección de DTOs

### Migración Liquibase `023-fix-department-dimensions-and-email-unique.yaml`
- Columnas `trade_department` y `shipping_department`: `VARCHAR(50)` → `VARCHAR(60)`
  (el nombre más largo en la tabla `department` es "Archipiélago de San Andrés,
  Providencia y Santa Catalina" con 56 caracteres).
- Agregada restricción `UNIQUE` en `seller.email`.

### DTOs actualizados
- `SellerContactEntity.java` — `@Column(length = 60)`
- `SellerTradeDepartment.java` — `MAX_LENGTH = 60`, `@Size(min = 2, max = 60)`
- `SellerRequest.java` — `@Size(min = 2, max = 60)` en `tradeDepartment`
- `RegisterRequest.java` — `@Size(max = 60)` en department y city
- `CustomerRequest.java` — `@Size(max = 60)` en department y city
- `UpdateCustomerRequest.java` — `@Size(max = 60)` en department y city

### Archivos nuevos
- `src/main/resources/db/migrations/023-fix-department-dimensions-and-email-unique.yaml`
- `src/main/resources/db/changelog/db.changelog-master.yaml` (referencia a migración 023)

---

## Fase 2 — Entidades JPA y repositorios de ubicación

### Nuevas entidades
- `shared/infrastructure/persistence/DepartmentEntity.java` — Tabla `department` con
  `id` (Integer) y `name` (String, length=100, unique).
- `shared/infrastructure/persistence/CityEntity.java` — Tabla `city` con `id`,
  `name` y `department_id` (FK a department).

### Nuevos repositorios
- `shared/infrastructure/persistence/DepartmentRepository.java` — `findByName(String name)`
- `shared/infrastructure/persistence/CityRepository.java` —
  `findByNameAndDepartmentId(String name, Integer departmentId)`

### Nueva excepción de dominio
- `shared/exception/InvalidLocationException.java` — Lanzada cuando department o city
  no existen en las tablas de referencia.

### Tests
- `LocationRepositoryTest.java` — 14 tests verificando consultas department y city
  contra H2 con datos sembrados.

---

## Fase 3 — Servicio de validación de ubicación

### Nuevo servicio de dominio
- `shared/domain/LocationValidationService.java` — Inyecta `DepartmentRepository` y
  `CityRepository`. Expone:
  - `validateDepartment(String department)` — Verifica que el departamento exista.
  - `validateCity(String city, Integer departmentId)` — Verifica que la ciudad exista
    dentro del departamento.
  - `validateLocation(String department, String city)` — Valida ambos.

### Tests unitarios
- `LocationValidationServiceTest.java` — 15 tests con mocks de repositorios.
  Verifica:
  - Departamento válido/inválido.
  - Ciudad válida/inválida.
  - Combinación department+city correcta o incorrecta.
  - Nombres vacíos o nulos.

---

## Fase 4 — Integración en Use Cases y Controllers

### Auth — `RegisterUseCase.java`
- Se inyecta `LocationValidationService`.
- Si el request incluye `CustomerInfo` (dniType != null), se valida department y city
  antes de crear el customer.

### Seller — `SellerSaveUseCase.java`
- Se inyecta `LocationValidationService`.
- Valida `contact.tradeDepartment` y `contact.tradeCity` antes de persistir.

### Checkout — `CheckoutUseCase.java`
- Se inyecta `LocationValidationService` como campo.
- Se valida ubicación al inicio de `execute()`, antes de procesar el checkout.

### Customer — `CustomerController.update()`
- Se inyecta `LocationValidationService`.
- Si el request incluye department o city, se valida contra la BD.
- Se preservan los valores existentes cuando el request no envía esos campos.

### Order — `OrderManagementController.java`
- Se inyecta `LocationValidationService`.
- Se valida en `save()` (POST) y `update()` (PUT) con `shippingDepartment` y
  `shippingCity`.

### Bean Configurations actualizados
- `AuthBeanConfiguration` — Agregado parámetro `LocationValidationService`.
- `SellerBeanConfiguration` — Agregado parámetro `LocationValidationService`.
- `CheckoutBeanConfiguration` — Agregado parámetro `LocationValidationService`.

### Tests corregidos
- `CheckoutUseCaseTest` — Constructor actualizado con mock de
  `LocationValidationService`.
- `OrderControllerTest` — Constructor actualizado.
- `OrderControllerAuthorizationTest` — Constructor actualizado.
- `OrderOwnershipTest` — Constructor actualizado.
- `AuthIntegrationTest` — Datos de prueba sembrados con `MERGE INTO` (idempotente);
  department corregido a "Bogotá D.C.", city a "Bogotá".
- `OrderIntegrationTest` — Mismo tratamiento con `MERGE INTO`; nombres corregidos.

### Archivos modificados
- `RegisterUseCase.java`
- `SellerSaveUseCase.java`
- `CheckoutUseCase.java`
- `CustomerController.java`
- `OrderManagementController.java`
- `AuthBeanConfiguration.java`
- `SellerBeanConfiguration.java`
- `CheckoutBeanConfiguration.java`

---

## Fase 5 — GlobalExceptionHandler

### Nuevos ExceptionHandlers
Se agregaron 13 handlers al `GlobalExceptionHandler`:

| Excepción | HTTP Status | Code |
|---|---|---|
| `InvalidLocationException` | 400 | `INVALID_LOCATION` |
| `SellerInvalidTradeDepartmentException` | 400 | `SELLER_INVALID_TRADE_DEPARTMENT` |
| `SellerInvalidTradeCityException` | 400 | `SELLER_INVALID_TRADE_CITY` |
| `SellerInvalidTradeAddressException` | 400 | `SELLER_INVALID_TRADE_ADDRESS` |
| `SellerInvalidTradeNameException` | 400 | `SELLER_INVALID_TRADE_NAME` |
| `SellerInvalidFullnameException` | 400 | `SELLER_INVALID_FULLNAME` |
| `SellerInvalidEmailException` | 400 | `SELLER_INVALID_EMAIL` |
| `SellerInvalidPhoneNumberException` | 400 | `SELLER_INVALID_PHONE_NUMBER` |
| `SellerInvalidDniNumberException` | 400 | `SELLER_INVALID_DNI_NUMBER` |
| `SellerInvalidBankNameException` | 400 | `SELLER_INVALID_BANK_NAME` |
| `SellerInvalidNumberAccountException` | 400 | `SELLER_INVALID_NUMBER_ACCOUNT` |

### Corrección de seguridad — `IllegalArgumentException`
- El handler anterior devolvía `ex.getMessage()` directamente en la respuesta,
  filtrando input del usuario (vulnerabilidad CWE-209).
- Ahora devuelve un mensaje genérico: "Argumento invalido".

---

## Fase 6 — Limpieza de código

### Eliminado `AuthRateLimitFilter.java`
- Filtro `@Component` con `@Profile("!test")` que aplicaba rate limiting vía
  Resilience4j en `/api/v1/auth/login` y `/api/v1/auth/register`.
- Eliminado porque el API Gateway ya maneja rate limiting per-IP vía Redis.

### Eliminados métodos muertos en `CustomerMapper.java`
- `toDomainFromRequest(CustomerRequest)` — Nunca invocado desde ningún controller
  o use case.
- `toDomainFromUpdateRequest(UUID, UpdateCustomerRequest)` — Nunca invocado.
- Se eliminaron los imports no utilizados (`CustomerRequest`, `UpdateCustomerRequest`,
  `Timestamp`, `Instant`, `UUID`).

### Corregido update parcial en `CustomerController`
- Antes: Si un customer no tenía `CustomerInfo` (dniType era null) y el request
  enviaba datos de DNI, la info no se creaba.
- Ahora: Si el customer no tiene info pero el request proporciona todos los campos
  requeridos (dniType, dniNumber, address, department, city), se crea la
  `CustomerInfo` correctamente.

---

## Fase 7 — API Gateway

### Eliminado `src/internal/middleware/auth.go`
- Middleware JWT que parseaba el `Authorization` header, validaba el token y
  seteaba `X-User-ID`, `X-User-Email`, `X-User-Role`.
- Nunca fue conectado a la cadena de middleware en `router.go`. Código muerto.
- La autenticación JWT la maneja el backend Spring Security.

### Eliminado campo `Auth` de `Route` y `RouteMatcher`
- `config.go`: Eliminado `Auth bool` del struct `Route`.
- `routes.go`: Eliminado `Auth bool` del struct `RouteMatcher` y de la asignación
  en `MatchRoute()`.
- `configs/config.yaml`: Eliminados todos los campos `auth: true/false` de las rutas.

### Corregido `getClientIP()` en `ratelimit.go`
- **Antes**: Confiaba ciegamente en `X-Forwarded-For` (spoofable por cualquier
  cliente HTTP).
- **Ahora**: Prefiere `X-Real-IP` (establecido por reverse proxies como nginx/ALB),
  luego cae en `RemoteAddr`. Se eliminó el parsing de `X-Forwarded-For`.

### Secretos movidos a variables de entorno
- `gateway_secret`: Ahora usa `${GATEWAY_SECRET}` (antes hardcodeado en texto plano).
- `redis_url` (contiene contraseña): Ahora usa `${REDIS_URL}` (antes hardcodeado con
  contraseña en texto plano).
- `config.go`: Agregado `os.ExpandEnv()` al cargar YAML para resolver `${VAR}`.

---

## Verificación

### Backend Java
```
Tests run: 346, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### API Gateway Go
```
go build ./...  →  exit 0 (sin errores de compilación)
```

---

## Archivos creados (nuevos)

| Archivo | Descripción |
|---|---|
| `db/migrations/023-fix-department-dimensions-and-email-unique.yaml` | Migración Liquibase |
| `shared/infrastructure/persistence/DepartmentEntity.java` | Entidad JPA department |
| `shared/infrastructure/persistence/CityEntity.java` | Entidad JPA city |
| `shared/infrastructure/persistence/DepartmentRepository.java` | Repo department |
| `shared/infrastructure/persistence/CityRepository.java` | Repo city |
| `shared/exception/InvalidLocationException.java` | Excepción de dominio |
| `shared/domain/LocationValidationService.java` | Servicio de validación |
| `shared/domain/LocationValidationServiceTest.java` | Tests unitarios (15) |
| `shared/infrastructure/persistence/LocationRepositoryTest.java` | Tests repos (14) |

## Archivos modificados

| Archivo | Cambio |
|---|---|
| `auth/application/RegisterUseCase.java` | Validación de ubicación |
| `auth/infrastructure/config/AuthBeanConfiguration.java` | +LocationValidationService |
| `seller/application/usecase/SellerSaveUseCase.java` | Validación de ubicación |
| `seller/infrastructure/config/SellerBeanConfiguration.java` | +LocationValidationService |
| `seller/infrastructure/persistence/SellerContactEntity.java` | length=60 |
| `seller/domain/model/SellerTradeDepartment.java` | MAX_LENGTH=60 |
| `seller/infrastructure/controller/dto/SellerRequest.java` | @Size en tradeDepartment |
| `checkout/application/CheckoutUseCase.java` | Validación de ubicación |
| `checkout/infrastructure/config/CheckoutBeanConfiguration.java` | +LocationValidationService |
| `customer/infrastructure/controller/CustomerController.java` | Validación + fix parcial update |
| `customer/infrastructure/mapper/CustomerMapper.java` | Eliminados métodos muertos |
| `order/infrastructure/controller/OrderManagementController.java` | Validación de ubicación |
| `shared/exception/GlobalExceptionHandler.java` | +13 handlers, fix input leak |
| `auth/infrastructure/controller/dto/RegisterRequest.java` | @Size(max=60) |
| `customer/infrastructure/controller/dto/CustomerRequest.java` | @Size(max=60) |
| `customer/infrastructure/controller/dto/UpdateCustomerRequest.java` | @Size(max=60) |
| `db/changelog/db.changelog-master.yaml` | Referencia a migración 023 |

## Archivos eliminados

| Archivo | Razón |
|---|---|
| `shared/config/AuthRateLimitFilter.java` | Redundante con rate limiting del gateway |

## Archivos de test corregidos

| Archivo | Cambio |
|---|---|
| `checkout/application/CheckoutUseCaseTest.java` | Constructor con LocationValidationService |
| `order/infrastructure/controller/OrderControllerTest.java` | Constructor con LocationValidationService |
| `order/infrastructure/controller/OrderControllerAuthorizationTest.java` | Constructor con LocationValidationService |
| `order/infrastructure/controller/OrderOwnershipTest.java` | Constructor con LocationValidationService |
| `auth/infrastructure/controller/AuthIntegrationTest.java` | MERGE INTO + nombres corregidos |
| `order/infrastructure/controller/OrderIntegrationTest.java` | MERGE INTO + nombres corregidos |

## Gateway (repositorio api-gateway-eliteshop)

| Archivo | Cambio |
|---|---|
| `src/internal/middleware/auth.go` | Eliminado (código muerto) |
| `src/internal/config/config.go` | Eliminado campo Auth, +os.ExpandEnv() |
| `src/internal/config/routes.go` | Eliminado campo Auth de RouteMatcher |
| `configs/config.yaml` | Secretos a env vars, eliminados campos auth |
| `src/internal/middleware/ratelimit.go` | getClientIP sin X-Forwarded-For |

---

## Fixes post-QA (2026-08-29)

Correcciones derivadas del Reporte QA de validación de ubicaciones en entorno de prueba.

### BUG-01: HttpMessageNotReadableException retornaba HTTP 500

**Problema:** Cuando un request tenía campos con formato inválido (por ejemplo,
`paymentMethodId: "fake-method-id"` en vez de un UUID válido), Jackson lanzaba
`HttpMessageNotReadableException`. El `GlobalExceptionHandler` no tenía handler
específico, así que el catch-all `Exception.class` lo interceptaba y retornaba 500
en vez de 400.

**Fix:** Agregado `@ExceptionHandler(HttpMessageNotReadableException.class)` al
`GlobalExceptionHandler` que retorna 400 con mensaje descriptivo y código
`INVALID_REQUEST_BODY`. Incluye detección del tipo de error (UUID, enum, fecha)
para mensajes más específicos.

**Archivo:** `shared/exception/GlobalExceptionHandler.java`

### BUG-02: Webhooks /webhooks/epayco retornaban 404 desde el Gateway

**Problema:** El `router.go` del gateway solo registraba un handler para `/api/`.
Las rutas `/webhooks/epayco` (definidas en `config.yaml`) no matcheaban y caían
al handler default de Go que retorna `404 page not found` en plain text.

**Fix:** Extracción del handler de rutas a una función reutilizable `routeHandler`.
Se agregó `mux.HandleFunc("/webhooks/", routeHandler)` para enrutar paths que
empiecen con `/webhooks/`. El handler existente de `/api/` se mantiene igual.

**Archivo:** `src/internal/router/router.go` (repositorio api-gateway-eliteshop)

### HALLAZGO-01: Ciudad "Bogotá" sin "D.C." no existía en la BD

**Problema:** La tabla `city` solo tenía el registro `id=11001, name='Bogotá D.C.'`
para el department 11. Los usuarios que escribían "Bogotá" (sin "D.C.") obtenían
400 INVALID_LOCATION.

**Fix:** Nueva migración Liquibase `024-add-bogota-city-alias.yaml` que agrega
un registro adicional `id=11002, name='Bogotá', department_id=11` como alias.
El registro original "Bogotá D.C." se mantiene para compatibilidad. Ahora ambos
nombres ("Bogotá" y "Bogotá D.C.") son aceptados como ciudad válida dentro del
department "Bogotá D.C.".

**Archivos:**
- `db/migrations/024-add-bogota-city-alias.yaml` (nuevo)
- `db/changelog/db.changelog-master.yaml` (referencia a migración 024)

### Verificación post-fix

```
Tests run: 346, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

go build ./... → exit 0
```

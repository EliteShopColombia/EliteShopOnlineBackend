# Changelog - EliteShop Colombia Backend

Todas las cambios notables de este proyecto estan documentados en este archivo.
El formato se basa en [Keep a Changelog](https://keepachangelog.com/es/1.1.0/),
y este proyecto adherisce al [Semantic Versioning](https://semver.org/lang/es/).

---

## [1.0.0] - 2026-08-30

### Added
- **Admin module**: Dashboard, list customers/sellers, toggle seller active status, list orders. Role `ROLE_ADMIN` seeded via migration 025.
- **springdoc-openapi**: Swagger UI disponible en `/swagger-ui/index.html` y OpenAPI JSON en `/v3/api-docs`.
- **`.env.example`**: Plantilla documentada de todas las variables de entorno requeridas.
- **Unit tests for cart module**: 8 tests covering AddToCart, GetCart, RemoveFromCart, ClearCart, and UpdateCartItem use cases.
- **Unit tests for seller module**: 11 tests covering FindById, Delete, Update, FindAll, and FindByDni use cases.
- **Unit tests for product module**: 7 tests covering FindById, Delete, and Save use cases.
- **Unit tests for review module**: 10 tests covering FindById, Delete, FindByProductId, Save, and FindAll use cases.
- **Liquibase migrations 023-025**: Location validation (departments/cities), verification table indexes, customer role column and admin seed.

### Changed
- Security configuration: explicit `SessionCreationPolicy.STATELESS` for JWT stateless authentication.
- Updated README.md and README_SPANISH.md with current test count (393), admin module, and module inventory.
- Updated `resolveRole()` in `AuthController` to support `ROLE_ADMIN` from `customer_role` column.

### Security
- All JWT tokens are now stateless (no server-side session).
- Admin endpoints gated behind `ROLE_ADMIN` via Spring Security method-level authorization.

---

## [0.9.0] - 2026-08-29

### Added
- **Location validation**: `LocationValidationService` validates department/city combinations against dimension tables before persisting seller or order data.
- **Seller verification**: Document and selfie upload via MinIO, face matching via external adapter, verification status tracking.
- **Payment webhook HMAC verification**: Webhook payloads validated with HMAC-SHA256 before processing.
- **Ownership authorization**: Order, payment, and review endpoints enforce that only the resource owner can access or modify it.

### Changed
- Refactored `GlobalExceptionHandler` to return standard `ErrorResponse` contract for all endpoints.
- Migrated from `@Autowired` field injection to constructor injection across all modules.
- Updated all use cases to use domain-specific exceptions instead of generic `RuntimeException`.

---

## [0.8.0] - 2026-08-27

### Added
- **Product images**: Upload, download, and delete product images via MinIO adapter.
- **Review images**: Upload and manage images attached to product reviews.
- **Seller profile images**: Avatar upload, replace, download, and delete via MinIO.
- **ePayco payment gateway**: Integration for payment processing and saved payment methods.
- **Resilience4j Circuit Breaker**: Configured for external service calls.

### Changed
- Enhanced order module with items, tracking events, and CAS status transitions.
- Added pagination support to customer, seller, product, and order controllers.

---

## [0.7.0] - 2026-08-25

### Added
- **Cart and Checkout modules**: Domain models, use cases, and controllers for shopping cart and checkout flow.
- **Spring Modulith**: Module boundaries enforced with `@ApplicationModuleTest` and module structure validation.
- **Spring Cloud Config Client**: Externalized configuration via Config Server.
- **Liquibase migrations 012-017**: Tables for cart, cart items, order items, payment methods, and tracking events.

### Changed
- Implemented Clean Architecture + Hexagonal (Ports & Adapters) across all modules.

---

## [0.6.0] - 2026-08-22

### Added
- **Auth module**: JWT-based authentication, registration, login, and role-based authorization (ROLE_CUSTOMER, ROLE_SELLER).
- **Order module**: Full order lifecycle with status management.
- **Payment module**: Payment info management and processing.
- **Spring Security**: JWT filter chain, stateless sessions, CORS configuration.

---

## [0.5.0] - 2026-08-20

### Added
- **Customer module**: Registration, profile management, avatar upload via MinIO.
- **Seller module**: Registration, profile management, bank info, contact info.
- **Product module**: CRUD operations with value object validation.
- **Review module**: Product review management with rating and content.

---

## [0.1.0] - 2026-08-15

### Added
- Initial project setup with Spring Boot 4.0.7, Java 21, PostgreSQL 15.
- Maven build configuration with Google Java Format (Spotless plugin).
- Project Lombok for boilerplate reduction.
- Basic module structure following Clean Architecture principles.

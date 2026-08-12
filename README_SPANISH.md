# EliteShop Colombia Backend

API Backend para **EliteShop Colombia**, una plataforma de comercio electronico construida con Spring Boot 4, Java 21 y principios de Arquitectura Limpia.

---

## Tabla de Contenidos

- [Resumen Ejecutivo](#resumen-ejecutivo)
- [Resumen Tecnico](#resumen-tecnico)
- [Arquitectura](#arquitectura)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Modulos](#modulos)
- [Esquema de Base de Datos](#esquema-de-base-de-datos)
- [Endpoints de la API](#endpoints-de-la-api)
- [Primeros Pasos](#primeros-pasos)
- [Configuracion](#configuracion)
- [Directrices de Desarrollo](#directrices-de-desarrollo)

---

## Resumen Ejecutivo

EliteShop Colombia es una plataforma de comercio electronico colombiana que conecta vendedores y clientes. El backend provee APIs REST para gestionar clientes, vendedores, productos, pedidos, pagos, carritos de compras y resenas de productos. Esta disenado como un monolito modular con limites de dominio claros, permitiendo la evolucion independiente de cada modulo de negocio.

**Estado Actual:** En desarrollo temprano. Los modulos de **Cliente** y **Vendedor** han sido implementados completamente (capas de dominio, aplicacion e infraestructura). El esquema de base de datos define 12 tablas que cubren todo el dominio de negocio, pero los modulos restantes (producto, pedido, carrito, pago) aun no estan implementados en codigo.

---

## Resumen Tecnico

| Aspecto | Detalle |
|---|---|
| **Lenguaje** | Java 21 |
| **Framework** | Spring Boot 4.0.7 |
| **Arquitectura** | Arquitectura Limpia + Hexagonal (Puertos y Adaptadores) |
| **Modularidad** | Spring Modulith 2.0.7 |
| **Base de Datos** | PostgreSQL 15 |
| **Migraciones** | Liquibase |
| **Herramienta de Build** | Maven |
| **Estilo de Codigo** | Google Java Format (Spotless) |
| **Seguridad** | Spring Security (permisivo para desarrollo) |
| **Resiliencia** | Resilience4j Circuit Breaker |
| **Configuracion** | Spring Cloud Config Server |
| **Observabilidad** | Spring Modulith Observability + Actuator |
| **Correo** | Spring Mail |

---

## Arquitectura

El proyecto sigue **Arquitectura Limpia** con una separacion estricta de capas dentro de cada modulo. Cada modulo es autocontenido con sus propias capas de dominio, aplicacion e infraestructura.

```mermaid
graph TB
    subgraph Infrastructure["Capa de Infraestructura"]
        Controller["Controller<br/>(API REST)"]
        DTO["DTOs<br/>(Request/Response)"]
        Mapper["Mapper<br/>(Dominio <-> Entidad)"]
        Adapter["Adaptador de Repositorio<br/>(implementa Puerto)"]
        Entity["Entidades JPA<br/>(Persistencia)"]
        Config["Configuracion<br/>(Beans)"]
    end

    subgraph Application["Capa de Aplicacion"]
        UseCase["Casos de Uso<br/>(Logica de Negocio)"]
    end

    subgraph Domain["Capa de Dominio"]
        Model["Objetos de Valor<br/>(Entidades)"]
        Port["Puerto de Repositorio<br/>(Interfaz)"]
        Exception["Excepciones de Dominio"]
    end

    Controller -->|HTTP| UseCase
    DTO --> Controller
    Mapper --> Controller
    UseCase --> Port
    Adapter -->|implementa| Port
    Entity --> Adapter
    Config --> UseCase

    style Domain fill:#e1f5fe,stroke:#01579b
    style Application fill:#f3e5f5,stroke:#4a148c
    style Infrastructure fill:#e8f5e9,stroke:#1b5e20
```

### Responsabilidades de las Capas

| Capa | Proposito | Dependencias |
|---|---|---|
| **Dominio** | Reglas de negocio, objetos de valor, interfaces de repositorio, excepciones. Cero dependencias externas. | Ninguna |
| **Aplicacion** | Casos de uso que orquestan la logica de negocio. Depende solo de interfaces del dominio. | Dominio |
| **Infraestructura** | Controllers HTTP, entidades JPA, mappers, adaptadores. Conecta el dominio con sistemas externos. | Aplicacion, Dominio |

### Principios de Diseno

- **Inversion de Dependencias:** El dominio define las interfaces de repositorio (puertos); la infraestructura las implementa (adaptadores).
- **Objetos de Valor:** Cada campo del modelo de dominio esta envuelto en un objeto de valor tipado (ej: `CustomerEmail`, `CustomerId`), previniendo la obsesion con primitivos y forzando seguridad de tipos.
- **Casos de Uso:** Cada operacion de negocio es una clase de un solo caso de uso (ej: `CustomerSaveUseCase`), garantizando responsabilidad unica.
- **Sin Filtraciones:** Las preocupaciones de infraestructura (anotaciones JPA, HTTP) nunca aparecen en las capas de dominio o aplicacion.

### Flujo de una Peticion HTTP

```mermaid
sequenceDiagram
    participant Client as Cliente HTTP
    participant Ctrl as CustomerController
    participant DTO as CustomerRequest
    participant Map as CustomerMapper
    participant UC as UseCase
    participant Port as CustomerRepository
    participant Adp as CustomerPostgresAdapter
    participant DB as PostgreSQL

    Client->>Ctrl: POST /api/v1/customers
    Ctrl->>DTO: Validar @RequestBody
    DTO-->>Ctrl: CustomerRequest valido
    Ctrl->>Map: toDomainFromRequest(request)
    Map-->>Ctrl: Customer (dominio)
    Ctrl->>UC: execute(customer)
    UC->>Port: findById(id)
    Port->>Adp: findById(id)
    Adp->>DB: SELECT
    DB-->>Adp: CustomerEntity
    Adp-->>Port: Optional<Customer>
    Port-->>UC: Optional<Customer>
    UC->>Port: save(customer)
    Port->>Adp: save(customer)
    Adp->>DB: INSERT
    DB-->>Adp: CustomerEntity guardado
    Adp-->>Port: Customer (dominio)
    Port-->>UC: Customer
    UC-->>Ctrl: void
    Ctrl->>Map: toResponse(customer)
    Map-->>Ctrl: CustomerResponse
    Ctrl-->>Client: 201 Created + JSON
```

### Flujo de Arquitectura por Capas

```mermaid
flowchart TD
    subgraph External["Sistemas Externos"]
        HTTP[Cliente HTTP]
        DB[(PostgreSQL)]
        ConfigServer[Config Server]
    end

    subgraph Infra["Capa de Infraestructura"]
        Controller[CustomerController]
        DTO[CustomerRequest / CustomerResponse]
        Mapper[CustomerMapper]
        Adapter[CustomerPostgresAdapter]
        Entity[CustomerEntity / CustomerInfoEntity]
        JPA[CustomerJpaRepository]
        BeansConfig[CustomerBeanConfiguration]
    end

    subgraph App["Capa de Aplicacion"]
        SaveUC[CustomerSaveUseCase]
        UpdateUC[CustomerUpdateUseCase]
        DeleteUC[CustomerDeleteUseCase]
        FindAllUC[CustomerFindAllUseCase]
        FindByIdUC[CustomerFindByIdUseCase]
    end

    subgraph Domain["Capa de Dominio"]
        Model[Customer / CustomerInfo]
        VOs[Objetos de Valor<br/>CustomerId, CustomerEmail, ...]
        Port[CustomerRepository<br/>Puerto/Interfaz]
        Exceptions[CustomerExistException<br/>CustomerNotExistException]
    end

    HTTP -->|HTTP Request| Controller
    Controller --> DTO
    Controller --> Mapper
    Controller --> SaveUC
    Controller --> UpdateUC
    Controller --> DeleteUC
    Controller --> FindAllUC
    Controller --> FindByIdUC

    SaveUC --> Port
    UpdateUC --> Port
    DeleteUC --> Port
    FindAllUC --> Port
    FindByIdUC --> Port

    Adapter -->|implementa| Port
    Adapter --> Mapper
    Adapter --> JPA
    JPA --> Entity
    Entity --> DB

    BeansConfig --> SaveUC
    BeansConfig --> UpdateUC
    BeansConfig --> DeleteUC
    BeansConfig --> FindAllUC
    BeansConfig --> FindByIdUC

    ConfigServer -->|config| BeansConfig

    Model --> VOs
    Port --> Exceptions

    style Domain fill:#e1f5fe,stroke:#01579b
    style App fill:#f3e5f5,stroke:#4a148c
    style Infra fill:#e8f5e9,stroke:#1b5e20
    style External fill:#fff3e0,stroke:#e65100
```

### Flujo de Persistencia (Save/Update)

```mermaid
flowchart LR
    subgraph Entry["Entrada"]
        Req[CustomerRequest]
    end

    subgraph Transform["Transformacion"]
        MapReq[Mapper.toDomainFromRequest]
        Domain[Customer dominio]
        MapEnt[Mapper.toEntity]
        Ent[CustomerEntity]
    end

    subgraph Persist["Persistencia"]
        JPA[CustomerJpaRepository.save]
        DB[(PostgreSQL)]
        Persisted[CustomerEntity persistido]
    end

    subgraph Response["Salida"]
        MapRes[Mapper.toResponse]
        Resp[CustomerResponse]
    end

    Req --> MapReq
    MapReq --> Domain
    Domain --> MapEnt
    MapEnt --> Ent
    Ent --> JPA
    JPA --> DB
    DB --> Persisted
    Persisted --> MapRes
    MapRes --> Resp

    style Entry fill:#fff3e0,stroke:#e65100
    style Transform fill:#e1f5fe,stroke:#01579b
    style Persist fill:#e8f5e9,stroke:#1b5e20
    style Response fill:#f3e5f5,stroke:#4a148c
```

### Flujo de Paginacion

```mermaid
sequenceDiagram
    participant Client as Cliente HTTP
    participant Ctrl as CustomerController
    participant UC as CustomerFindAllUseCase
    participant Port as CustomerRepository
    participant Adp as CustomerPostgresAdapter
    participant DB as PostgreSQL

    Client->>Ctrl: GET /api/v1/customers?page=0&size=25
    Ctrl->>Ctrl: PageRequest.of(0, 25)
    Ctrl->>UC: execute(Pageable)
    UC->>Port: findAll(Pageable)
    Port->>Adp: findAll(Pageable)
    Adp->>DB: SELECT ... LIMIT 25 OFFSET 0
    DB-->>Adp: Page<CustomerEntity>
    Adp-->>Port: Page<Customer>
    Port-->>UC: Page<Customer>
    UC-->>Ctrl: Page<Customer>
    Ctrl->>Ctrl: Mapear a PageResponse
    Ctrl-->>Client: 200 OK + PageResponse JSON
```

### Flujo de una Peticion HTTP (Seller)

```mermaid
sequenceDiagram
    participant Client as Cliente HTTP
    participant Ctrl as SellerController
    participant DTO as SellerRequest
    participant Map as SellerMapper
    participant UC as UseCase
    participant Port as SellerRepository
    participant Adp as SellerPostgresAdapter
    participant DB as PostgreSQL

    Client->>Ctrl: POST /api/v1/sellers
    Ctrl->>DTO: Validar @RequestBody
    DTO-->>Ctrl: SellerRequest valido
    Ctrl->>Map: toDomainFromRequest(request)
    Map-->>Ctrl: Seller (dominio)
    Ctrl->>UC: execute(seller)
    UC->>Port: findByDniNumber(dniNumber)
    Port->>Adp: findByDniNumber(dniNumber)
    Adp->>DB: SELECT
    DB-->>Adp: SellerEntity
    Adp-->>Port: Optional<Seller>
    Port-->>UC: Optional<Seller>
    UC->>Port: save(seller)
    Port->>Adp: save(seller)
    Adp->>DB: INSERT
    DB-->>Adp: SellerEntity guardado
    Adp-->>Port: Seller (dominio)
    Port-->>UC: Seller
    UC-->>Ctrl: void
    Ctrl->>Map: toResponse(seller)
    Map-->>Ctrl: SellerResponse
    Ctrl-->>Client: 201 Created + JSON
```

### Flujo de Arquitectura por Capas (Seller)

```mermaid
flowchart TD
    subgraph External["Sistemas Externos"]
        HTTP[Cliente HTTP]
        DB[(PostgreSQL)]
        ConfigServer[Config Server]
    end

    subgraph Infra["Capa de Infraestructura"]
        Controller[SellerController]
        DTO[SellerRequest / SellerResponse]
        Mapper[SellerMapper]
        Adapter[SellerPostgresAdapter]
        Entity[SellerEntity / SellerContactEntity / SellerBankInfoEntity]
        JPA[SellerJpaRepository]
        BeansConfig[SellerBeanConfiguration]
    end

    subgraph App["Capa de Aplicacion"]
        SaveUC[SellerSaveUseCase]
        UpdateUC[SellerUpdateUseCase]
        DeleteUC[SellerDeleteUseCase]
        FindAllUC[SellerFindAllUseCase]
        FindByIdUC[SellerFindByIdUseCase]
        FindContactUC[SellerFindContactBySellerIdUseCase]
        FindBankUC[SellerFindBankInfoBySellerIdUseCase]
    end

    subgraph Domain["Capa de Dominio"]
        Model[Seller / SellerContact / SellerBankInfo]
        VOs[Objetos de Valor<br/>SellerId, SellerEmail, ...]
        Port[SellerRepository<br/>Puerto/Interfaz]
        Exceptions[Excepciones de Dominio]
    end

    HTTP -->|HTTP Request| Controller
    Controller --> DTO
    Controller --> Mapper
    Controller --> SaveUC
    Controller --> UpdateUC
    Controller --> DeleteUC
    Controller --> FindAllUC
    Controller --> FindByIdUC
    Controller --> FindContactUC
    Controller --> FindBankUC

    SaveUC --> Port
    UpdateUC --> Port
    DeleteUC --> Port
    FindAllUC --> Port
    FindByIdUC --> Port
    FindContactUC --> FindByIdUC
    FindBankUC --> FindByIdUC

    Adapter -->|implementa| Port
    Adapter --> Mapper
    Adapter --> JPA
    JPA --> Entity
    Entity --> DB

    BeansConfig --> SaveUC
    BeansConfig --> UpdateUC
    BeansConfig --> DeleteUC
    BeansConfig --> FindAllUC
    BeansConfig --> FindByIdUC
    BeansConfig --> FindContactUC
    BeansConfig --> FindBankUC

    ConfigServer -->|config| BeansConfig

    Model --> VOs
    Port --> Exceptions

    style Domain fill:#e1f5fe,stroke:#01579b
    style App fill:#f3e5f5,stroke:#4a148c
    style Infra fill:#e8f5e9,stroke:#1b5e20
    style External fill:#fff3e0,stroke:#e65100
```

### Flujo de Persistencia (Seller - Save/Update)

```mermaid
flowchart LR
    subgraph Entry["Entrada"]
        Req[SellerRequest]
    end

    subgraph Transform["Transformacion"]
        MapReq[Mapper.toDomainFromRequest]
        Domain[Seller dominio]
        MapEnt[Mapper.toEntity]
        Ent[SellerEntity]
    end

    subgraph Persist["Persistencia"]
        JPA[SellerJpaRepository.save]
        DB[(PostgreSQL)]
        Persisted[SellerEntity persistido]
    end

    subgraph Response["Salida"]
        MapRes[Mapper.toResponse]
        Resp[SellerResponse]
    end

    Req --> MapReq
    MapReq --> Domain
    Domain --> MapEnt
    MapEnt --> Ent
    Ent --> JPA
    JPA --> DB
    DB --> Persisted
    Persisted --> MapRes
    MapRes --> Resp

    style Entry fill:#fff3e0,stroke:#e65100
    style Transform fill:#e1f5fe,stroke:#01579b
    style Persist fill:#e8f5e9,stroke:#1b5e20
    style Response fill:#f3e5f5,stroke:#4a148c
```

### Flujo de Consulta de Contacto y Bank Info

```mermaid
sequenceDiagram
    participant Client as Cliente HTTP
    participant Ctrl as SellerController
    participant FindByIdUC as FindByIdUseCase
    participant FindContactUC as FindContactUseCase
    participant FindBankUC as FindBankInfoUseCase
    participant Port as SellerRepository
    participant Adp as SellerPostgresAdapter
    participant DB as PostgreSQL

    Note over Client, DB: Consulta de Contacto
    Client->>Ctrl: GET /api/v1/seller-contact/{sellerId}
    Ctrl->>FindContactUC: execute(sellerId)
    FindContactUC->>FindByIdUC: execute(sellerId)
    FindByIdUC->>Port: findById(sellerId)
    Port->>Adp: findById(sellerId)
    Adp->>DB: SELECT
    DB-->>Adp: SellerEntity + ContactEntity
    Adp-->>Port: Seller (dominio)
    Port-->>FindByIdUC: Optional<Seller>
    FindByIdUC-->>FindContactUC: Optional<Seller>
    FindContactUC->>FindContactUC: filter(contact != null)
    FindContactUC-->>Ctrl: Optional<Seller>
    Ctrl->>Ctrl: toContactResponse(seller)
    Ctrl-->>Client: 200 OK + SellerContactResponse

    Note over Client, DB: Consulta de Bank Info
    Client->>Ctrl: GET /api/v1/seller-bank-info/{sellerId}
    Ctrl->>FindBankUC: execute(sellerId)
    FindBankUC->>FindByIdUC: execute(sellerId)
    FindByIdUC->>Port: findById(sellerId)
    Port->>Adp: findById(sellerId)
    Adp->>DB: SELECT
    DB-->>Adp: SellerEntity + BankInfoEntity
    Adp-->>Port: Seller (dominio)
    Port-->>FindByIdUC: Optional<Seller>
    FindByIdUC-->>FindBankUC: Optional<Seller>
    FindBankUC->>FindBankUC: filter(bankInfo != null)
    FindBankUC-->>Ctrl: Optional<Seller>
    Ctrl->>Ctrl: toBankInfoResponse(seller)
    Ctrl-->>Client: 200 OK + SellerBankInfoResponse
```

### Diagrama de Relacion de Entidades (Seller)

```mermaid
erDiagram
    seller ||--o{ seller_contact : tiene
    seller ||--o| seller_bank_info : tiene

    seller {
        uuid seller_id PK
        varchar seller_type_trade
        varchar seller_type_dni
        varchar seller_dni_number UK
        varchar seller_trade_name
        varchar seller_fullname
        boolean seller_is_active
        timestamp seller_created_at
        timestamp seller_update_at
    }

    seller_contact {
        uuid seller_id PK, FK
        varchar seller_email
        varchar seller_phone_number
        varchar seller_trade_address
        varchar seller_trade_department
        varchar seller_trade_city
    }

    seller_bank_info {
        uuid seller_id PK, FK
        varchar bank_name
        varchar type_account
        varchar number_account
    }
```

---

## Estructura del Proyecto

```
src/main/java/com/eliteshop/colombia/
├── ColombiaApplication.java          # Punto de entrada de la aplicacion
├── SecurityConfig.java               # Configuracion de Spring Security (permisiva)
│
└── customer/                         # Modulo de Cliente
    ├── application/                  # Casos de uso
    │   ├── CustomerSaveUseCase.java
    │   ├── CustomerUpdateUseCase.java
    │   ├── CustomerDeleteUseCase.java
    │   ├── CustomerFindAllUseCase.java
    │   └── CustomerFindByIdUseCase.java
    │
    ├── domain/                       # Modelo de dominio
    │   ├── model/
    │   │   ├── Customer.java                  # Raiz del agregado
    │   │   ├── CustomerInfo.java              # Objeto de valor (DNI + direccion)
    │   │   ├── CustomerId.java                # Envoltorio UUID
    │   │   ├── CustomerFirstName.java         # Envoltorio String
    │   │   ├── CustomerLastName.java          # Envoltorio String
    │   │   ├── CustomerEmail.java             # Envoltorio String
    │   │   ├── CustomerPhoneNumber.java       # Envoltorio String
    │   │   ├── CustomerPassword.java          # Envoltorio String
    │   │   ├── CustomerProfileImage.java      # Envoltorio String
    │   │   ├── CustomerCreatedAt.java         # Envoltorio Timestamp
    │   │   ├── CustomerUpdatedAt.java         # Envoltorio Timestamp
    │   │   ├── CustomerDniType.java           # Envoltorio String
    │   │   ├── CustomerDniNumber.java         # Envoltorio String
    │   │   ├── CustomerAddress.java           # Envoltorio String
    │   │   ├── CustomerDepartment.java        # Envoltorio String
    │   │   ├── CustomerCity.java              # Envoltorio String
    │   │   ├── CustomerDniCreatedAt.java      # Envoltorio Timestamp
    │   │   └── CustomerDniUpdatedAt.java      # Envoltorio Timestamp
    │   ├── repository/
    │   │   └── CustomerRepository.java        # Puerto (interfaz)
    │   └── exception/
    │       ├── CustomerExistException.java    # HTTP 409
    │       └── CustomerNotExistException.java # HTTP 404
    │
    └── infrastructure/               # Adaptadores externos
        ├── config/
        │   └── CustomerBeanConfiguration.java
        ├── controller/
        │   ├── CustomerController.java
        │   └── dto/
        │           ├── CustomerRequest.java
        │           ├── CustomerResponse.java
        │           └── PageResponse.java
        ├── mapper/
        │   └── CustomerMapper.java
        ├── adapter/
        │   └── CustomerPostgresAdapter.java
        └── persistence/
            ├── CustomerEntity.java
            ├── CustomerInfoEntity.java
            └── CustomerJpaRepository.java

└── seller/                           # Modulo de Vendedor
    ├── application/                  # Casos de uso
    │   ├── SellerSaveUseCase.java
    │   ├── SellerUpdateUseCase.java
    │   ├── SellerDeleteUseCase.java
    │   ├── SellerFindAllUseCase.java
    │   ├── SellerFindByIdUseCase.java
    │   ├── SellerFindByDniUseCase.java
    │   ├── SellerFindContactBySellerIdUseCase.java
    │   └── SellerFindBankInfoBySellerIdUseCase.java
    │
    ├── domain/                       # Modelo de dominio
    │   ├── model/
    │   │   ├── Seller.java                    # Raiz del agregado
    │   │   ├── SellerContact.java             # Objeto de valor (compuesto)
    │   │   ├── SellerBankInfo.java            # Objeto de valor (compuesto)
    │   │   ├── SellerId.java                  # Envoltorio UUID
    │   │   ├── SellerTypeTrade.java           # Enum (NATURAL, LEGAL)
    │   │   ├── SellerTypeDni.java             # Enum (CC, CE, PS, NIT)
    │   │   ├── SellerTypeBankAccount.java     # Enum (SAVINGS, CHECKING, WALLET)
    │   │   ├── SellerDniNumber.java           # Envoltorio String
    │   │   ├── SellerTradeName.java           # Envoltorio String
    │   │   ├── SellerFullname.java            # Envoltorio String
    │   │   ├── SellerIsActive.java            # Envoltorio Boolean
    │   │   ├── SellerCreatedAt.java           # Envoltorio Timestamp
    │   │   ├── SellerUpdatedAt.java           # Envoltorio Timestamp
    │   │   ├── SellerEmail.java               # Envoltorio String
    │   │   ├── SellerPhoneNumber.java         # Envoltorio String
    │   │   ├── SellerTradeAddress.java        # Envoltorio String
    │   │   ├── SellerTradeDepartment.java     # Envoltorio String
    │   │   ├── SellerTradeCity.java           # Envoltorio String
    │   │   ├── SellerBankName.java            # Envoltorio String
    │   │   └── SellerNumberAccount.java       # Envoltorio String
    │   ├── repository/
    │   │   └── SellerRepository.java          # Puerto (interfaz)
    │   └── exception/
    │       ├── SellerAlreadyExistsException.java   # HTTP 409
    │       ├── SellerNotFoundException.java        # HTTP 404
    │       ├── SellerInvalidEmailException.java    # HTTP 400
    │       ├── SellerInvalidPhoneNumberException.java
    │       ├── SellerInvalidDniNumberException.java
    │       ├── SellerInvalidTradeNameException.java
    │       ├── SellerInvalidFullnameException.java
    │       ├── SellerInvalidTradeAddressException.java
    │       ├── SellerInvalidTradeDepartmentException.java
    │       ├── SellerInvalidTradeCityException.java
    │       ├── SellerInvalidBankNameException.java
    │       └── SellerInvalidNumberAccountException.java
    │
    └── infrastructure/               # Adaptadores externos
        ├── config/
        │   └── SellerBeanConfiguration.java
        ├── controller/
        │   ├── SellerController.java
        │   └── dto/
        │           ├── SellerRequest.java
        │           ├── SellerResponse.java
        │           ├── SellerContactResponse.java
        │           └── SellerBankInfoResponse.java
        ├── mapper/
        │   └── SellerMapper.java
        ├── adapter/
        │   └── SellerPostgresAdapter.java
        └── persistence/
            ├── SellerEntity.java
            ├── SellerContactEntity.java
            ├── SellerBankInfoEntity.java
            └── SellerJpaRepository.java
```

---

## Modulos

La base de datos define 12 tablas organizadas en 6 dominios de negocio. Solo el modulo de **Cliente** tiene implementacion en codigo.

```mermaid
graph LR
    subgraph Customer["Cliente"]
        C[customer]
        CI[customer_info]
    end

    subgraph Seller["Vendedor"]
        S[seller]
        SC[seller_contact]
        SB[seller_bank_info]
    end

    subgraph Product["Producto"]
        P[product]
        PR[product_review]
    end

    subgraph Order["Pedido"]
        O[orders]
        OI[order_item]
    end

    subgraph Payment["Pago"]
        PI[payment_info]
    end

    subgraph Cart["Carrito"]
        CA[cart]
        CIT[cart_item]
    end

    C --> CI
    S --> SC
    S --> SB
    S --> P
    C --> PR
    P --> PR
    C --> O
    O --> OI
    P --> OI
    S --> OI
    O --> PI
    C --> CA
    CA --> CIT
    P --> CIT

    style Customer fill:#e1f5fe,stroke:#01579b
    style Seller fill:#fff3e0,stroke:#e65100
    style Product fill:#e8f5e9,stroke:#1b5e20
    style Order fill:#fce4ec,stroke:#880e4f
    style Payment fill:#f3e5f5,stroke:#4a148c
    style Cart fill:#fffde7,stroke:#f57f17
```

| Modulo | Tablas | Estado del Codigo |
|---|---|---|
| **Cliente** | `customer`, `customer_info` | Implementado |
| **Vendedor** | `seller`, `seller_contact`, `seller_bank_info` | Implementado |
| **Producto** | `product`, `product_review` | Solo esquema |
| **Pedido** | `orders`, `order_item` | Solo esquema |
| **Pago** | `payment_info` | Solo esquema |
| **Carrito** | `cart`, `cart_item` | Solo esquema |

---

## Esquema de Base de Datos

La base de datos es **PostgreSQL 15**, gestionada por **Liquibase**. La migracion inicial (`001-create-initial-tables.yaml`) define las 12 tablas con restricciones de clave foranea y restricciones unicas.

### Diagrama de Relacion de Entidades

```mermaid
erDiagram
    customer ||--o| customer_info : tiene
    customer ||--o{ product_review : escribe
    customer ||--o{ orders : realiza
    customer ||--o| cart : posee
    seller ||--o{ seller_contact : tiene
    seller ||--o| seller_bank_info : tiene
    seller ||--o{ product : publica
    seller ||--o{ order_item : cumple
    product ||--o{ product_review : recibe
    product ||--o{ order_item : incluye
    orders ||--o{ order_item : contiene
    orders ||--o| payment_info : paga_con
    cart ||--o{ cart_item : contiene
    product ||--o{ cart_item : se_agrega_a

    customer {
        uuid customer_id PK
        varchar customer_first_name
        varchar customer_last_name
        varchar customer_email UK
        varchar customer_phone_number
        varchar customer_password
        text customer_profile_image
        timestamp customer_created_at
        timestamp customer_update_at
    }

    customer_info {
        uuid customer_id PK, FK
        varchar customer_dni_type
        varchar customer_dni_number UK
        varchar customer_address
        varchar customer_department
        varchar customer_city
        timestamp customer_dni_created_at
        timestamp customer_dni_update_at
    }

    seller {
        uuid seller_id PK
        varchar seller_type_trade
        varchar seller_type_dni
        varchar seller_dni_number UK
        varchar seller_trade_name
        varchar seller_fullname
        boolean seller_is_active
        timestamp seller_created_at
        timestamp seller_update_at
    }

    seller_contact {
        uuid seller_id PK, FK
        varchar seller_email
        varchar seller_phone_number
        varchar seller_trade_address
        varchar seller_trade_department
        varchar seller_trade_city
    }

    seller_bank_info {
        uuid seller_id PK, FK
        varchar bank_name
        varchar type_account
        varchar number_account
    }

    product {
        uuid product_id PK
        uuid seller_id FK
        varchar product_name
        decimal product_price
        int product_stock
    }

    product_review {
        uuid review_id PK
        uuid product_id FK
        uuid customer_id FK
        int product_qualify
        varchar product_review_content
        text product_review_image
    }

    orders {
        uuid order_id PK
        uuid customer_id FK
        varchar order_status
        decimal total_amount
        varchar shipping_address
        varchar shipping_department
        varchar shipping_city
        timestamp created_at
        timestamp updated_at
    }

    order_item {
        uuid order_item_id PK
        uuid orders_id FK
        uuid product_id FK
        uuid seller_id FK
        decimal unit_price
        int quantity
        varchar item_status
    }

    payment_info {
        uuid payment_id PK
        uuid order_id FK
        varchar payment_method
        varchar transaction_id
        varchar payment_status
        timestamp paid_at
    }

    cart {
        uuid cart_id PK
        uuid customer_id FK
        timestamp created_at
        timestamp updated_at
    }

    cart_item {
        uuid cart_item_id PK
        uuid cart_id FK
        uuid product_id FK
        int quantity
        timestamp added_at
    }
```

---

## Endpoints de la API

### Modulo de Cliente

| Metodo | Endpoint | Descripcion | Estado |
|---|---|---|---|
| `POST` | `/api/v1/customers` | Crear un nuevo cliente | Implementado |
| `PUT` | `/api/v1/customers/{id}` | Actualizar un cliente existente | Implementado |
| `DELETE` | `/api/v1/customers/{id}` | Eliminar un cliente | Implementado |
| `GET` | `/api/v1/customers?page=0&size=25` | Obtener clientes paginados | Implementado |
| `GET` | `/api/v1/customers/{id}` | Obtener cliente por ID | Implementado |

### Parametros de Paginacion

| Parametro | Tipo | Default | Descripcion |
|---|---|---|---|
| `page` | int | `0` | Numero de pagina (inicia en 0) |
| `size` | int | `25` | Cantidad de registros por pagina |

### Respuesta Paginada

| Campo | Tipo | Descripcion |
|---|---|---|
| `content` | Array | Lista de clientes |
| `page` | int | Pagina actual |
| `size` | int | Registros por pagina |
| `totalElements` | long | Total de registros |
| `totalPages` | int | Total de paginas |
| `first` | boolean | Es primera pagina |
| `last` | boolean | Es ultima pagina |

### Cuerpo de la Peticion (POST/PUT)

| Campo | Tipo | Descripcion |
|---|---|---|
| `firstName` | String | Nombre del cliente |
| `lastName` | String | Apellido del cliente |
| `email` | String | Correo electronico del cliente |
| `phoneNumber` | String | Numero de telefono del cliente |
| `password` | String | Contrasena del cliente |
| `profileImage` | String | URL de imagen de perfil (opcional) |
| `dniType` | String | Tipo de documento (opcional) |
| `dniNumber` | String | Numero de documento (opcional) |
| `address` | String | Direccion (opcional) |
| `department` | String | Departamento (opcional) |
| `city` | String | Ciudad (opcional) |

### Reglas de Validacion

| Campo | Reglas |
|---|---|
| `firstName` | Obligatorio, 1-100 caracteres |
| `lastName` | Obligatorio, 1-100 caracteres |
| `email` | Obligatorio, formato de email valido |
| `phoneNumber` | Obligatorio, 1-20 caracteres |
| `password` | Obligatorio, 8-100 caracteres |
| `dniType` | Opcional, 1-20 caracteres |
| `dniNumber` | Opcional, 1-20 caracteres |
| `address` | Opcional, 1-150 caracteres |
| `department` | Opcional, 1-50 caracteres |
| `city` | Opcional, 1-60 caracteres |

### Respuestas de Error

| Codigo HTTP | Excepcion | Cuando |
|---|---|---|
| `409 CONFLICT` | `CustomerExistException` | Ya existe un cliente con el mismo ID |
| `404 NOT FOUND` | `CustomerNotExistException` | Cliente no encontrado para actualizacion/eliminacion |

---

### Modulo de Vendedor

| Metodo | Endpoint | Descripcion | Estado |
|---|---|---|---|
| `POST` | `/api/v1/sellers` | Crear un nuevo vendedor | Implementado |
| `PUT` | `/api/v1/sellers/{id}` | Actualizar un vendedor existente | Implementado |
| `DELETE` | `/api/v1/sellers/{id}` | Eliminar un vendedor | Implementado |
| `GET` | `/api/v1/sellers` | Obtener todos los vendedores | Implementado |
| `GET` | `/api/v1/sellers/{id}` | Obtener vendedor por ID | Implementado |
| `GET` | `/api/v1/seller-contact/{sellerId}` | Obtener informacion de contacto del vendedor | Implementado |
| `GET` | `/api/v1/seller-bank-info/{sellerId}` | Obtener informacion bancaria del vendedor | Implementado |

#### Cuerpo de la Peticion (POST/PUT)

| Campo | Tipo | Descripcion |
|---|---|---|
| `typeTrade` | String | Tipo de comercio: `NATURAL`, `LEGAL` |
| `typeDni` | String | Tipo de documento: `CC`, `CE`, `PS`, `NIT` |
| `dniNumber` | String | Numero de documento (5-20 caracteres, unico) |
| `tradeName` | String | Nombre comercial (2-100 caracteres) |
| `fullname` | String | Nombre completo (2-100 caracteres) |
| `email` | String | Correo electronico |
| `phoneNumber` | String | Numero de telefono (formato colombiano) |
| `tradeAddress` | String | Direccion comercial (5-150 caracteres) |
| `tradeDepartment` | String | Departamento (colombiano) |
| `tradeCity` | String | Ciudad (2-60 caracteres) |
| `bankName` | String | Nombre del banco (banco colombiano) |
| `typeBankAccount` | String | Tipo de cuenta: `SAVINGS`, `CHECKING`, `WALLET` |
| `numberAccount` | String | Numero de cuenta (10-30 caracteres) |

#### Reglas de Validacion

| Campo | Reglas |
|---|---|
| `typeTrade` | Obligatorio. Valores: `NATURAL`, `LEGAL` |
| `typeDni` | Obligatorio. Valores: `CC`, `CE`, `PS`, `NIT` |
| `dniNumber` | Obligatorio, 5-20 caracteres, unico |
| `tradeName` | Obligatorio, 2-100 caracteres |
| `fullname` | Obligatorio, 2-100 caracteres |
| `email` | Obligatorio, formato de email valido |
| `phoneNumber` | Obligatorio, formato colombiano (10 digitos, empieza con 3) |
| `tradeAddress` | Obligatorio, 5-150 caracteres |
| `tradeDepartment` | Obligatorio, departamento colombiano valido |
| `tradeCity` | Obligatorio, 2-60 caracteres, ciudad valida para el departamento |
| `bankName` | Obligatorio, banco colombiano valido |
| `typeBankAccount` | Obligatorio. Valores: `SAVINGS`/`Ahorros`, `CHECKING`/`Corriente`, `WALLET`/`Digital` |
| `numberAccount` | Obligatorio, 10-30 caracteres |

#### Respuesta del Vendedor (GET)

| Campo | Tipo | Descripcion |
|---|---|---|
| `id` | UUID | Identificador unico del vendedor |
| `typeTrade` | String | Tipo de comercio |
| `typeDni` | String | Tipo de documento |
| `dniNumber` | String | Numero de documento |
| `tradeName` | String | Nombre comercial |
| `fullname` | String | Nombre completo |
| `isActive` | Boolean | Estado activo |
| `createdAt` | Timestamp | Fecha de creacion |
| `updatedAt` | Timestamp | Fecha de ultima actualizacion (nulable) |

#### Respuesta de Contacto del Vendedor (GET /seller-contact/{sellerId})

| Campo | Tipo | Descripcion |
|---|---|---|
| `sellerId` | UUID | Identificador unico del vendedor |
| `tradeName` | String | Nombre comercial |
| `fullname` | String | Nombre completo |
| `email` | String | Correo electronico |
| `phoneNumber` | String | Numero de telefono |
| `tradeAddress` | String | Direccion comercial |
| `tradeDepartment` | String | Departamento |
| `tradeCity` | String | Ciudad |

#### Respuesta de Informacion Bancaria del Vendedor (GET /seller-bank-info/{sellerId})

| Campo | Tipo | Descripcion |
|---|---|---|
| `sellerId` | UUID | Identificador unico del vendedor |
| `tradeName` | String | Nombre comercial |
| `fullname` | String | Nombre completo |
| `bankName` | String | Nombre del banco |
| `typeBankAccount` | String | Tipo de cuenta |
| `numberAccount` | String | Numero de cuenta |

#### Respuestas de Error

| Codigo HTTP | Excepcion | Cuando |
|---|---|---|
| `409 CONFLICT` | `SellerAlreadyExistsException` | Ya existe un vendedor con el mismo DNI |
| `404 NOT FOUND` | `SellerNotFoundException` | Vendedor no encontrado |
| `400 BAD REQUEST` | Various `SellerInvalid*Exception` | Valores de campo invalidos |

---

## Primeros Pasos

### Prerequisitos

- Java 21
- Maven 3.9+
- Docker y Docker Compose (para base de datos local)
- Spring Cloud Config Server ejecutandose en `http://100.123.31.18:8888`

### Ejecutar Localmente

```bash
# Iniciar PostgreSQL via Docker Compose
docker compose up -d

# Ejecutar la aplicacion
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

La aplicacion inicia en el **puerto 8080** por defecto.

### Ejecutar con Maven

```bash
# Compilar
./mvnw compile

# Ejecutar pruebas
./mvnw test

# Formatear codigo
./mvnw spotless:apply

# Empaquetar
./mvnw package
```

---

## Configuracion

### Perfiles

| Perfil | Proposito |
|---|---|
| `local` | Desarrollo local |
| `dev` | Entorno de desarrollo |
| `prod` | Entorno de produccion |

Todos los perfiles importan configuracion desde el Spring Cloud Config Server en `http://100.123.31.18:8888`.

### Propiedades Principales

| Propiedad | Valor | Descripcion |
|---|---|---|
| `spring.application.name` | `colombia` | Nombre de la aplicacion |
| `spring.docker.compose.enabled` | `false` | Docker Compose deshabilitado por defecto |
| `spring.config.import` | `optional:configserver:http://100.123.31.18:8888` | Config Server |

### Seguridad

Spring Security esta configurado en `SecurityConfig.java` con CSRF deshabilitado y todas las peticiones permitidas. Esto es solo para desarrollo. **El despliegue en produccion debe configurar autenticacion y autorizacion adecuadas.**

---

## Directrices de Desarrollo

### Estilo de Codigo

- **Google Java Format** aplicado via el plugin Spotless de Maven
- Ejecutar `./mvnw spotless:apply` antes de commitear

### Agregar un Nuevo Modulo

Seguir el patron existente del modulo de Cliente:

1. **Capa de dominio:** Crear objetos de valor, interfaz de repositorio, excepciones
2. **Capa de aplicacion:** Crear clases de casos de uso (una por operacion)
3. **Capa de infraestructura:** Crear entidades JPA, adaptador de repositorio, mapper, controller, DTOs, configuracion de beans

### Convenciones de Nomenclatura

| Capa | Convencion |
|---|---|
| Objetos de Valor | `{Modulo}{Campo}` (ej: `CustomerEmail`) |
| Entidades | `{Modulo}Entity` (ej: `CustomerEntity`) |
| Casos de Uso | `{Modulo}{Accion}UseCase` (ej: `CustomerSaveUseCase`) |
| Adaptadores | `{Modulo}PostgresAdapter` |
| Controllers | `{Modulo}Controller` |
| DTOs | `{Modulo}Request`, `{Modulo}Response`, `PageResponse` |

### Reglas Importantes

- La capa de dominio debe tener **cero** dependencias de infraestructura
- Los casos de uso no deben referenciar entidades JPA ni conceptos HTTP
- Cada clase de caso de uso maneja exactamente una operacion de negocio
- Los objetos de valor envuelven primitivos para forzar seguridad de tipos

---

## Licencia

Ver [LICENSE](LICENSE) para mas detalles.

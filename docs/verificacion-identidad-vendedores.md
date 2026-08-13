# Plan de Implementación: Verificación de Identidad de Vendedores

## 1. Contexto

EliteShop Colombia necesita verificar la identidad de sus vendedores para:
- Prevenir documentos falsos
- Evitar venta de productos ilegales
- Cumplir con regulaciones colombianas
- Generar confianza en la plataforma

## 2. Marco Legal Colombiano

| Documento | Entidad | Uso |
|-----------|---------|-----|
| Cédula de Ciudadanía | Registraduría Nacional | Identificación personal |
| NIT | DIAN | Identificación tributaria (persona jurídica) |
| RUT | DIAN | Registro Único Tributario |
| Cámara de Comercio | Cámara de Comercio | Registro mercantil |
| RNEC | Registraduría | Registro Nacional de Extranjeros |

## 3. Alternativas de Verificación

### 3.1 Verificación Manual (Nivel 1)

**Descripción:** Un admin revisa los documentos subidos por el vendedor.

**Proceso:**
1. Vendedor sube: Cédula (anverso/sobrescrito), RUT, Cámara de Comercio
2. Admin revisa en el panel de administración
3. Aprueba o rechaza la solicitud

| Aspecto | Detalle |
|---------|---------|
| Costo | $0 (solo tiempo del admin) |
| Tiempo de verificación | 24-48 horas |
| Seguridad | Baja - depende del ojo humano |
| Escalabilidad | Mala - no escala con miles de vendedores |
| Implementación | Simple - solo un CRUD + flujo de aprobación |

**Archivos necesarios:**
- Tabla `seller_verification` en BD
- Endpoint para subir documentos
- Panel de admin para revisión
- Estados: `PENDING`, `APPROVED`, `REJECTED`

---

### 3.2 Verificación con APIs del Estado (Nivel 2)

**Descripción:** Validar documentos contra fuentes oficiales colombianas.

#### APIs Disponibles:

| API | Entidad | Qué valida | Costo |
|-----|---------|------------|-------|
| **Consulta Cédula** | Registraduría Nacional | Si la cédula existe y coincide el nombre | ~$200-500 por consulta |
| **Consulta NIT** | DIAN | Si el NIT está activo y los datos coinciden | Gratuito (con certificado digital) |
| **Consulta RUT** | DIAN | Estado del RUT, actividades económicas | Gratuito |
| **RNEC** | Registraduría | Datos de ciudadanos | ~$300 por consulta |
| **Cámara de Comercio** | Cámara Nacional | Registro mercantil, representante legal | ~$5.000-15.000 por consulta |

#### Ejemplo de flujo:
```
Vendedor ingresa cédula: 1.234.567.890
    │
    ▼
Backend llama API Registraduría
    │
    ├── Si existe → Valida nombre, apellido, estado
    ├── Si coincide → Aprobado
    └── Si no existe o no coincide → Rechazado
```

| Aspecto | Detalle |
|---------|---------|
| Costo | Medio ($200-15.000 por consulta) |
| Tiempo de verificación | Instantáneo (< 5 segundos) |
| Seguridad | Alta - valida contra fuentes oficiales |
| Escalabilidad | Buena - automatizada |
| Implementación | Media - requiere integración con APIs externas |

---

### 3.3 Verificación con Proveedores Terceros (Nivel 3)

**Descripción:** Usar un servicio especializado en verificación de identidad (KYC).

#### Proveedores Disponibles:

| Proveedor | Cobertura Colombia | Precio | Funcionalidades |
|-----------|-------------------|--------|-----------------|
| **Onfido** | Sí | ~$2-5 por verificación | OCR, facial, documentos |
| **Jumio** | Sí | ~$3-8 por verificación | OCR, facial, liveness |
| **Veriff** | Sí | ~$2-6 por verificación | Facial, documentos |
| **Truora** | Sí (Latam) | ~$1-4 por verificación | Cédula, facial, PEP |
| **Sumsub** | Sí | ~$2-5 por verificación | KYC completo |

#### Proceso:
```
Vendedor sube selfie + cédula
    │
    ▼
Proveedor compara selfie con foto de cédula (facial matching)
    │
    ├── OCR extrae datos de la cédula
    ├── Facial matching: selfie vs foto del documento
    ├── Liveness detection: verifica que no sea foto
    └── Validación contra bases de datos
        │
        ├── Aprobado → Vendedor verificado
        └── Rechazado → Solicita重新 verificación
```

| Aspecto | Detalle |
|---------|---------|
| Costo | Alto ($2-8 por verificación) |
| Tiempo de verificación | Instantáneo (< 30 segundos) |
| Seguridad | Muy alta - múltiples capas |
| Escalabilidad | Excelente - API completa |
| Implementación | Media - SDK o API |

---

### 3.4 Verificación por Video (Nivel 4)

**Descripción:** El vendedor hace una videollamada corta mostrando su cédula.

**Proceso:**
1. Vendedor agenda videollamada (5 minutos)
2. Muestra cédula frente a la cámara
3. Dice su nombre y un código generado
4. Admin o AI verifica

| Aspecto | Detalle |
|---------|---------|
| Costo | Medio (si es manual) / Alto (si es AI) |
| Tiempo de verificación | 5-10 minutos por vendedor |
| Seguridad | Muy alta - difícil de falsificar |
| Escalabilidad | Mala si es manual, buena si es AI |
| Implementación | Compleja - requiere video streaming |

---

### 3.5 Verificación Cruzada de Datos (Nivel 2+)

**Descripción:** Cruzar datos del vendedor sin necesidad de APIs externas.

**Fuentes de datos cruzados:**
- Cédula vs Nombre en la cédula (OCR)
- NIT vs RUT (DIAN)
- Dirección vs Ciudad/Departamento
- Teléfono vs Operador (validar formato colombiano)
- Email vs Dominio (verificar que no sea temporal)

| Aspecto | Detalle |
|---------|---------|
| Costo | Bajo |
| Tiempo | Instantáneo |
| Seguridad | Media - detecta inconsistencias |
| Escalabilidad | Buena |
| Implementación | Simple |

---

## 4. Recomendación por Fase

| Fase | Nivel | Alternativa | Costo | Prioridad |
|------|-------|-------------|-------|-----------|
| **1** | Básico | Verificación manual + Validación de formato | $0 | Alta |
| **2** | Medio | APIs del Estado (Cédula + NIT) | ~$500/vendedor | Alta |
| **3** | Avanzado | Proveedor KYC (Truora para Latam) | ~$3/vendedor | Media |
| **4** | Premium | Video verificación + AI | Variable | Baja |

## 5. Modelo de Base de Datos

```sql
CREATE TABLE seller_verification (
    id UUID PRIMARY KEY,
    seller_id UUID NOT NULL REFERENCES seller(seller_id),
    verification_type VARCHAR(50) NOT NULL,        -- CEDULA, NIT, RUT, KYC
    document_type VARCHAR(20) NOT NULL,             -- CC, NIT, CE
    document_number VARCHAR(20) NOT NULL,
    document_front_url TEXT,                        -- URL imagen anverso
    document_back_url TEXT,                         -- URL imagen reverso
    selfie_url TEXT,                                -- URL selfie (para KYC)
    status VARCHAR(20) NOT NULL,                    -- PENDING, APPROVED, REJECTED, EXPIRED
    verified_at TIMESTAMP,
    expires_at TIMESTAMP,                           -- Algunas verificaciones expiran
    rejection_reason TEXT,
    verified_by VARCHAR(50),                        -- SYSTEM, MANUAL, PROVIDER
    provider_reference VARCHAR(255),                -- ID de referencia del proveedor
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

## 6. Estados de Verificación

```
PENDING → APPROVED
PENDING → REJECTED → PENDING (reintento)
APPROVED → EXPIRED (si expira)
APPROVED → SUSPENDED (si se detecta fraude)
```

## 7. Endpoints API

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/sellers/{id}/verification` | Subir documentos |
| GET | `/api/v1/sellers/{id}/verification` | Estado de verificación |
| POST | `/api/v1/sellers/{id}/verification/validate` | Validar contra API |
| GET | `/api/v1/admin/verifications` | Listar pendientes (admin) |
| PUT | `/api/v1/admin/verifications/{id}/approve` | Aprobar (admin) |
| PUT | `/api/v1/admin/verifications/{id}/reject` | Rechazar (admin) |

## 8. Verificación OblIGATORIA Para

| Tipo de vendedor | Documentos requeridos | Nivel mínimo |
|------------------|----------------------|--------------|
| Persona natural | Cédula + RUT | Nivel 1 (manual) |
| Persona jurídica | NIT + Cámara de Comercio + RUT | Nivel 2 (API) |
| Vendedor internacional | Pasaporte + Comprobante de residencia | Nivel 3 (KYC) |

## 9. Consideraciones de Seguridad

| Medida | Descripción |
|--------|-------------|
| Cifrado | Documentos cifrados en reposo (AES-256) |
| Acceso | Solo admin y el propio vendedor ven los documentos |
| Retención | Documentos eliminados después de X meses |
| Auditoría | Log de todos los accesos a documentos |
| Validación OCR | Detectar si el documento es real vs editado |
| Detección de fraude | Alertar si el mismo documento se usa en múltiples cuentas |

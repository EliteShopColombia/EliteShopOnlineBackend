# Plan de Implementación: Facial Matching para Verificación de Vendedores

## 1. Objetivo

Comparar la selfie del vendedor con la foto de su cédula de ciudadanía para verificar que son la misma persona, sin costo alguno.

## 2. Stack

| Componente | Tecnología | Costo |
|------------|-----------|-------|
| Face detection + comparison | Python + face_recognition | $0 (open source) |
| Almacenamiento imágenes | MinIO (ya en servidor SSH) | $0 |
| Spring Boot backend | Java 21 | $0 (ya existe) |
| PostgreSQL | Ya existente | $0 |

## 3. Repositorios

| Repositorio | Ubicación | Contenido |
|-------------|-----------|-----------|
| Backend | `/home/arch-tarok/Escritorio/Dev/EliteShopColombiaBackend` | Spring Boot API |
| Face Matcher | `/home/arch-tarok/Escritorio/Dev/face-matcher` | Microservicio Python |

## 4. Arquitectura

```
Vendedor (OnBoarding)
    │
    │ 1. Sube cédula (anverso)
    ▼
Spring Boot Backend
    │
    ├── Valida formato (JPG/PNG, < 5MB, 640x480)
    ├── Sube imagen a MinIO → bucket: sellers/{sellerId}/document.jpg
    │
    │ 2. Sube selfie
    ├── Valida formato
    ├── Sube imagen a MinIO → bucket: sellers/{sellerId}/selfie.jpg
    │
    │ 3. Ejecuta verificación
    ├── Descarga ambas imágenes de MinIO
    └── POST http://face-matcher:5000/match
        │   {
        │     "selfie_url": "http://minio:9000/sellers/uuid/selfie.jpg",
        │     "document_url": "http://minio:9000/sellers/uuid/document.jpg"
        │   }
        │
        ▼
Python Face Matcher
    │
    ├── Descarga imágenes de MinIO
    ├── Detecta rostros
    ├── Extrae encodings (128 dimensiones)
    ├── Compara con tolerancia (0.6)
    │
    ├── MATCH → { "match": true, "confidence": 0.95 }
    └── NO MATCH → { "match": false, "confidence": 0.32 }
        │
        ▼
Spring Boot actualiza estado de verificación
```

## 5. Flujo de OnBoarding del Vendedor

```
Paso 1: Upload Cédula
─────────────────────
POST /api/v1/sellers/{id}/verification/document
Body: multipart/form-data { "file": cedula.jpg }

    → Valida formato
    → Sube a MinIO: sellers/{sellerId}/document.jpg
    → Crea registro: status = DOCUMENT_UPLOADED
    → Response: { "status": "DOCUMENT_UPLOADED" }

Paso 2: Upload Selfie
─────────────────────
POST /api/v1/sellers/{id}/verification/selfie
Body: multipart/form-data { "file": selfie.jpg }

    → Valida formato
    → Sube a MinIO: sellers/{sellerId}/selfie.jpg
    → Actualiza registro: status = SELFIE_UPLOADED
    → Response: { "status": "SELFIE_UPLOADED" }

Paso 3: Validar
───────────────
POST /api/v1/sellers/{id}/verification/validate

    → Descarga imágenes de MinIO
    → Envía al Face Matcher
    → Recibe resultado
    → Actualiza registro:
        - Si match → status = APPROVED, confidence = 0.92
        - Si no match → status = REJECTED, reason = "Las caras no coinciden"
    → Response: { "status": "APPROVED", "confidence": 0.92 }
```

## 6. Estados de Verificación

```
PENDING → DOCUMENT_UPLOADED → SELFIE_UPLOADED → APPROVED
                                              → REJECTED (reintentar)
```

## 7. MinIO Configuration

```yaml
# MinIO ya está en el servidor SSH
endpoint: http://minio:9000
access-key: minioadmin
secret-key: minioadmin
bucket: eliteshop-sellers
```

### Estructura de buckets:
```
eliteshop-sellers/
└── sellers/
    ├── {seller-id-1}/
    │   ├── document.jpg
    │   └── selfie.jpg
    ├── {seller-id-2}/
    │   ├── document.jpg
    │   └── selfie.jpg
    └── ...
```

## 8. Repositorio Face Matcher

### Estructura:
```
/home/arch-tarok/Escritorio/Dev/face-matcher/
├── Dockerfile
├── docker-compose.yml
├── requirements.txt
├── app.py                      # Flask API
├── face_service.py             # Lógica de comparación
├── minio_service.py            # Descarga imágenes de MinIO
├── config.py                   # Configuración
└── .env
```

### app.py:
```python
from flask import Flask, request, jsonify
from face_service import compare_faces
from minio_service import download_image
import config

app = Flask(__name__)

@app.route('/match', methods=['POST'])
def match():
    data = request.json
    selfie_url = data['selfie_url']
    document_url = data['document_url']

    # Descargar imágenes de MinIO
    selfie_path = download_image(selfie_url, 'selfie.jpg')
    document_path = download_image(document_url, 'document.jpg')

    # Comparar
    result = compare_faces(selfie_path, document_path)

    # Limpiar archivos temporales
    import os
    os.remove(selfie_path)
    os.remove(document_path)

    return jsonify(result)

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "UP"})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
```

### minio_service.py:
```python
import requests
import tempfile
import os

def download_image(url, filename):
    response = requests.get(url)
    response.raise_for_status()

    temp_dir = tempfile.mkdtemp()
    file_path = os.path.join(temp_dir, filename)

    with open(file_path, 'wb') as f:
        f.write(response.content)

    return file_path
```

### face_service.py:
```python
import face_recognition

def compare_faces(selfie_path, document_path, tolerance=0.6):
    try:
        selfie_image = face_recognition.load_image_file(selfie_path)
        document_image = face_recognition.load_image_file(document_path)

        selfie_encodings = face_recognition.face_encodings(selfie_image)
        document_encodings = face_recognition.face_encodings(document_image)

        if not selfie_encodings:
            return {"match": False, "confidence": 0, "message": "No se detectó rostro en la selfie"}
        if not document_encodings:
            return {"match": False, "confidence": 0, "message": "No se detectó rostro en el documento"}

        distance = face_recognition.face_distance([selfie_encodings[0]], document_encodings[0])[0]
        confidence = 1 - distance
        match = distance <= tolerance

        return {
            "match": match,
            "confidence": round(float(confidence), 2),
            "selfie_face_found": True,
            "document_face_found": True,
            "message": "Identidad verificada" if match else "Las caras no coinciden"
        }

    except Exception as e:
        return {"match": False, "confidence": 0, "message": f"Error: {str(e)}"}
```

### config.py:
```python
import os

MINIO_ENDPOINT = os.getenv('MINIO_ENDPOINT', 'http://minio:9000')
MINIO_ACCESS_KEY = os.getenv('MINIO_ACCESS_KEY', 'minioadmin')
MINIO_SECRET_KEY = os.getenv('MINIO_SECRET_KEY', 'minioadmin')
FACE_MATCHER_TOLERANCE = float(os.getenv('FACE_MATCHER_TOLERANCE', '0.6'))
```

### requirements.txt:
```
flask==3.1.1
face-recognition==1.3.0
dlib==19.24.6
Pillow==11.1.0
minio==7.2.15
requests==2.32.3
```

### Dockerfile:
```dockerfile
FROM python:3.11-slim

RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    libopenblas-dev \
    liblapack-dev \
    libx11-dev \
    libgtk-3-dev \
    python3-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 5000
CMD ["python", "app.py"]
```

### docker-compose.yml:
```yaml
services:
  face-matcher:
    build: .
    container_name: eliteshop-face-matcher
    ports:
      - "5000:5000"
    environment:
      MINIO_ENDPOINT: http://minio:9000
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: minioadmin
      FACE_MATCHER_TOLERANCE: "0.6"
    restart: unless-stopped
```

## 9. Backend Spring Boot (Cambios)

### SellerVerification.java (Domain Model)
```java
public record SellerVerification(
    SellerVerificationId id,
    UUID sellerId,
    String verificationType,
    String documentType,
    String documentNumber,
    String documentMinioKey,
    String selfieMinioKey,
    String status,
    Double confidenceScore,
    String rejectionReason,
    Instant createdAt,
    Instant updatedAt
) {
    // Factory methods: create(), withDocumentUploaded(), withSelfieUploaded(), approved(), rejected()
}
```

### MinIO Client (Infrastructure)
```java
@Component
public class MinIOClient {

    private final MinioClient minioClient;

    public String uploadDocument(UUID sellerId, String filename, InputStream stream) {
        String objectKey = "sellers/" + sellerId + "/document.jpg";
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket("eliteshop-sellers")
                .object(objectKey)
                .stream(stream, -1, 10485760)
                .contentType("image/jpeg")
                .build()
        );
        return objectKey;
    }

    public String uploadSelfie(UUID sellerId, String filename, InputStream stream) {
        String objectKey = "sellers/" + sellerId + "/selfie.jpg";
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket("eliteshop-sellers")
                .object(objectKey)
                .stream(stream, -1, 10485760)
                .contentType("image/jpeg")
                .build()
        );
        return objectKey;
    }

    public String getPresignedUrl(String objectKey) {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket("eliteshop-sellers")
                .object(objectKey)
                .method(Method.GET)
                .expiry(60 * 60) // 1 hora
                .build()
        );
    }
}
```

### FaceMatcherClient.java (Infrastructure)
```java
@Component
public class FaceMatcherClient {

    private final WebClient webClient;

    @Value("${face-matcher.url:http://face-matcher:5000}")
    private String faceMatcherUrl;

    public FaceMatchResult match(String selfieUrl, String documentUrl) {
        Map<String, String> request = Map.of(
            "selfie_url", selfieUrl,
            "document_url", documentUrl
        );

        return webClient.post()
            .uri(faceMatcherUrl + "/match")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(FaceMatchResult.class)
            .block();
    }
}
```

### VerifySellerUseCase.java (Application)
```java
@RequiredArgsConstructor
public class VerifySellerUseCase {

    private final SellerVerificationRepository repository;
    private final MinIOClient minIOClient;
    private final FaceMatcherClient faceMatcherClient;

    public SellerVerification uploadDocument(UUID sellerId, String filename, InputStream stream) {
        String objectKey = minIOClient.uploadDocument(sellerId, filename, stream);
        SellerVerification verification = repository.findBySellerId(sellerId)
            .orElse(SellerVerification.create(sellerId));
        verification = verification.withDocumentUploaded(objectKey);
        repository.save(verification);
        return verification;
    }

    public SellerVerification uploadSelfie(UUID sellerId, String filename, InputStream stream) {
        String objectKey = minIOClient.uploadSelfie(sellerId, filename, stream);
        SellerVerification verification = repository.findBySellerId(sellerId)
            .orElseThrow(() -> new RuntimeException("Sube la cédula primero"));
        verification = verification.withSelfieUploaded(objectKey);
        repository.save(verification);
        return verification;
    }

    public SellerVerification validate(UUID sellerId) {
        SellerVerification verification = repository.findBySellerId(sellerId)
            .orElseThrow(() -> new RuntimeException("No hay verificación pendiente"));

        if (!"SELFIE_UPLOADED".equals(verification.status())) {
            throw new RuntimeException("Primero sube la cédula y la selfie");
        }

        String selfieUrl = minIOClient.getPresignedUrl(verification.selfieMinioKey());
        String documentUrl = minIOClient.getPresignedUrl(verification.documentMinioKey());

        FaceMatchResult result = faceMatcherClient.match(selfieUrl, documentUrl);

        if (result.isMatch() && result.getConfidence() >= 0.6) {
            verification = verification.approved(result.getConfidence());
        } else {
            verification = verification.rejected(result.getMessage());
        }

        repository.save(verification);
        return verification;
    }
}
```

## 10. Modelo de BD

```sql
CREATE TABLE seller_verification (
    id UUID PRIMARY KEY,
    seller_id UUID NOT NULL REFERENCES seller(seller_id),
    verification_type VARCHAR(50) NOT NULL DEFAULT 'FACIAL_MATCH',
    document_type VARCHAR(20) NOT NULL DEFAULT 'CC',
    document_number VARCHAR(20),
    document_minio_key TEXT,
    selfie_minio_key TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    confidence_score DECIMAL(3,2),
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

## 11. Endpoints

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| POST | `/api/v1/sellers/{id}/verification/document` | Subir cédula | multipart/form-data |
| POST | `/api/v1/sellers/{id}/verification/selfie` | Subir selfie | multipart/form-data |
| POST | `/api/v1/sellers/{id}/verification/validate` | Ejecutar facial match | - |
| GET | `/api/v1/sellers/{id}/verification` | Consultar estado | - |

## 12. Secuencia de Implementación

### Paso 1: Face Matcher (repo separado)
| # | Archivo | Descripción |
|---|---------|-------------|
| 1 | `config.py` | Configuración de MinIO |
| 2 | `minio_service.py` | Servicio para descargar de MinIO |
| 3 | `face_service.py` | Lógica de comparación facial |
| 4 | `app.py` | API Flask |
| 5 | `requirements.txt` | Dependencias |
| 6 | `Dockerfile` | Dockerizar |
| 7 | `docker-compose.yml` | Compose del face matcher |

### Paso 2: Backend Spring Boot
| # | Archivo | Descripción |
|---|---------|-------------|
| 1 | Liquibase 005 | Tabla `seller_verification` |
| 2 | Domain | `SellerVerification` model |
| 3 | Infrastructure | `MinIOClient` |
| 4 | Infrastructure | `FaceMatcherClient` |
| 5 | Application | `VerifySellerUseCase` |
| 6 | Infrastructure | `SellerVerificationController` |
| 7 | Infrastructure | `SellerVerificationBeanConfiguration` |

## 13. Verificación

1. Face matcher: `curl -X POST http://localhost:5000/match -d '{"selfie_url":"...","document_url":"..."}'`
2. Backend: Subir cédula → Subir selfie → Validar → Verificar estado APPROVED

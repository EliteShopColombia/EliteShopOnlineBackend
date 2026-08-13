# Casos de Uso: Sistema de Eventos y Cola - EliteShop Colombia

## 1. Contexto

EliteShop Colombia es un E-Commerce modular (Spring Modulith + Arquitectura Hexagonal) que maneja customers, sellers, products, orders, payments y cart. Este documento define los casos de uso que requieren un sistema de eventos y cola para gestionar la comunicación entre módulos de forma desacoplada y confiable.

**Stack actual:**
- Spring Boot 4.0.7 / Java 21
- Spring Modulith (starter-core, starter-jpa)
- PostgreSQL + Liquibase
- Arquitectura hexagonal por módulo

**Tablas existentes en BD (aún sin módulos implementados):**
- `orders` / `order_item` / `payment_info`
- `cart` / `cart_item`

---

## 2. Arquitectura de Eventos Propuesta

```
┌─────────────────────────────────────────────────────────┐
│                    DOMAIN EVENTS                         │
│  (publicados al commitear transacciones JPA)             │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  OrderModule          CartModule         PaymentModule   │
│      │                    │                   │         │
│      ▼                    ▼                   ▼         │
│  OrderCreated        CartCheckedOut     PaymentCompleted │
│  OrderCancelled      ItemAdded          PaymentFailed    │
│  OrderStatusChanged  ItemRemoved        RefundProcessed  │
│                                                         │
├─────────────────────────────────────────────────────────┤
│                   EVENT LISTENERS                        │
│  (procesamiento dentro de la misma app)                  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  InventoryModule     NotificationModule   SellerModule   │
│      │                    │                   │         │
│      ▼                    ▼                   ▼         │
│  StockReserved       EmailSent           SellerNotified  │
│  StockReleased       PushSent            OrderForwarded  │
│  StockLow                                   SalesUpdated  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 3. Casos de Uso por Módulo

### 3.1 MODULO: Orders (Gestión de Pedidos)

#### CU-001: Crear Orden desde Carrito
- **Trigger:** Customer confirma compra (checkout del carrito)
- **Evento emitido:** `OrderCreated`
- **Datos del evento:** `orderId`, `customerId`, `sellerId`, `items[]`, `totalAmount`, `shippingAddress`
- **Reacciones en cadena:**
  - `InventoryModule` → Reserva stock para cada item (descuenta disponibilidad)
  - `NotificationModule` → Email al customer: "Tu orden #XXXX fue creada"
  - `SellerModule` → Notifica al seller: "Tienes un nuevo pedido"
- **Validación previa:** Stock suficiente para todos los items del carrito

#### CU-002: Cambiar Estado de Orden
- **Trigger:** Actualización manual o automática del estado
- **Estados posibles:** `PENDING` → `CONFIRMED` → `PROCESSING` → `SHIPPED` → `DELIVERED` → `COMPLETED`
- **Estados alternos:** `CANCELLED`, `REFUNDED`
- **Evento emitido:** `OrderStatusChanged`
- **Datos del evento:** `orderId`, `customerId`, `sellerId`, `oldStatus`, `newStatus`, `timestamp`
- **Reacciones en cadena:**
  - `NotificationModule` → Email al customer según el estado:
    - `CONFIRMED`: "Tu orden fue confirmada"
    - `SHIPPED`: "Tu orden fue despachada - tracking: XXX"
    - `DELIVERED`: "Tu orden fue entregada"
    - `CANCELLED`: "Tu orden fue cancelada"
  - `InventoryModule`:
    - Si `CANCELLED` → Libera stock reservado
    - Si `DELIVERED` → Confirma la reducción de stock (ya no es reserva)
  - `SellerModule` → Notifica al seller del cambio de estado

#### CU-003: Cancelar Orden
- **Trigger:** Customer o admin cancela la orden
- **Evento emitido:** `OrderCancelled`
- **Datos del evento:** `orderId`, `customerId`, `sellerId`, `reason`, `items[]`
- **Reacciones en cadena:**
  - `InventoryModule` → Libera todo el stock reservado para esa orden
  - `NotificationModule` → Email al customer: "Tu orden fue cancelada"
  - `SellerModule` → Notifica al seller: "El pedido #XXXX fue cancelado"
  - `PaymentModule` → Si ya se pagó, inicia proceso de reembolso

---

### 3.2 MODULO: Inventory (Gestión de Inventario)

#### CU-004: Reservar Stock al Crear Orden
- **Trigger:** Evento `OrderCreated`
- **Acción:** Para cada `orderItem`, decrementar `product_stock` del producto
- **Validación:** Si stock insuficiente para algún item → rechazar la orden
- **Evento emitido:** `StockReserved` o `StockInsufficient`
- **Edge case:** Si hay 10 unidades y la orden pide 15 → rechazar, notificar al customer

#### CU-005: Liberar Stock al Cancelar Orden
- **Trigger:** Evento `OrderCancelled`
- **Acción:** Restaurar `product_stock` para cada item de la orden cancelada
- **Evento emitido:** `StockReleased`
- **Reacción:** Si el stock restaurado supera un umbral → `StockLow` se desactiva

#### CU-006: Alerta de Stock Bajo
- **Trigger:** Después de cada reserva de stock, verificar si `product_stock < threshold`
- **Threshold configurable:** Por defecto 5 unidades
- **Evento emitido:** `StockLow`
- **Reacciones:**
  - `NotificationModule` → Email al seller: "Tu producto X tiene stock bajo"
  - `SellerModule` → Marcar producto como "bajo stock" en su dashboard
- **Prioridad:** Esta es una alerta, no bloquea el flujo de compra

#### CU-007: Confirmar Despacho (Stock Definitivo)
- **Trigger:** Evento `OrderStatusChanged` con `newStatus = SHIPPED`
- **Acción:** Convertir reserva de stock en descuento definitivo
- **Por qué:** Mientras la orden está en `PENDING` o `CONFIRMED`, el stock está "reservado" pero no descontado. Al enviar, se confirma
- **Evento emitido:** `StockConfirmed`

---

### 3.3 MODULO: Payment (Pagos)

#### CU-008: Procesar Pago Exitoso
- **Trigger:** Respuesta positiva de la pasarela de pagos (webhook o callback)
- **Evento emitido:** `PaymentCompleted`
- **Datos del evento:** `paymentId`, `orderId`, `customerId`, `amount`, `paymentMethod`, `transactionId`
- **Reacciones:**
  - `OrderModule` → Cambiar estado de orden a `CONFIRMED`
  - `NotificationModule` → Email al customer: "Pago confirmado - tu orden está siendo procesada"
  - `SellerModule` → Notifica al seller: "El pago del pedido #XXXX fue recibido"

#### CU-009: Procesar Pago Fallido
- **Trigger:** Respuesta negativa de la pasarela de pagos
- **Evento emitido:** `PaymentFailed`
- **Datos del evento:** `paymentId`, `orderId`, `customerId`, `reason`
- **Reacciones:**
  - `OrderModule` → Mantener estado `PENDING` o marcar como `PAYMENT_FAILED`
  - `InventoryModule` → Liberar stock reservado (el pago no se concretó)
  - `NotificationModule` → Email al customer: "El pago no pudo ser procesado"

#### CU-010: Procesar Reembolso
- **Trigger:** Admin aprueba reembolso o cancelación post-pago
- **Evento emitido:** `RefundProcessed`
- **Datos del evento:** `paymentId`, `orderId`, `customerId`, `refundAmount`
- **Reacciones:**
  - `OrderModule` → Cambiar estado a `REFUNDED`
  - `InventoryModule` → Liberar stock
  - `NotificationModule` → Email al customer: "Reembolso procesado"

---

### 3.4 MODULO: Cart (Carrito de Compras)

#### CU-011: Agregar Producto al Carrito
- **Trigger:** Customer agrega un producto
- **Evento emitido:** `CartItemAdded`
- **Datos del evento:** `cartId`, `customerId`, `productId`, `sellerId`, `quantity`
- **Reacciones:**
  - `InventoryModule` → Verificar stock disponible (no descontar aún, solo validar)
  - Si stock insuficiente → Notificar al customer: "Solo quedan X unidades"

#### CU-012: Checkout del Carrito
- **Trigger:** Customer presiona "Comprar"
- **Evento emitido:** `CartCheckedOut`
- **Acción:** Crear la orden y los order_items a partir del carrito
- **Reacciones:**
  - `OrderModule` → Crear orden con estado `PENDING`
  - `InventoryModule` → Reservar stock (CU-004)
  - `PaymentModule` → Iniciar proceso de pago

#### CU-013: Vaciar Carrito
- **Trigger:** Después de checkout exitoso o cancelación
- **Evento emitido:** `CartCleared`
- **Acción:** Eliminar todos los items del carrito del customer

---

### 3.5 MODULO: Notifications (Notificaciones)

#### CU-014: Enviar Email de Confirmación de Orden
- **Trigger:** Evento `OrderCreated`
- **Canal:** Email (spring-boot-starter-mail)
- **Template:** Confirmación con resumen de productos, total, dirección de envío
- **Destinatario:** Customer (email del customer_info)

#### CU-015: Enviar Notificación de Cambio de Estado
- **Trigger:** Evento `OrderStatusChanged`
- **Canal:** Email
- **Template:** Según el nuevo estado (confirmación, envío, entrega, cancelación)
- **Destinatario:** Customer

#### CU-016: Notificar al Seller de Nuevo Pedido
- **Trigger:** Evento `OrderCreated`
- **Canal:** Email
- **Template:** Detalle del pedido recibido, productos, cantidades, total
- **Destinatario:** Seller (email del seller_contact)

#### CU-017: Alerta de Stock Bajo al Seller
- **Trigger:** Evento `StockLow`
- **Canal:** Email
- **Template:** "Tu producto X tiene solo Y unidades disponibles"
- **Destinatario:** Seller propietario del producto

#### CU-018: Notificación de Pago
- **Trigger:** Evento `PaymentCompleted` o `PaymentFailed`
- **Canal:** Email
- **Template:** Confirmación o rechazo de pago
- **Destinatario:** Customer

---

### 3.6 MODULO: Seller (Vendedor)

#### CU-019: Recibir Notificación de Nuevo Pedido
- **Trigger:** Evento `OrderCreated`
- **Acción:** Registrar el pedido en la vista del seller, actualizar métricas
- **Evento emitido:** `SellerOrderReceived`

#### CU-020: Actualizar Estado de Envío
- **Trigger:** Seller cambia el estado de la orden (procesando → enviado)
- **Evento emitido:** `OrderStatusChanged`
- **Reacciones:** Mismo flujo que CU-002

#### CU-021: Recibir Alerta de Stock Bajo
- **Trigger:** Evento `StockLow`
- **Acción:** Marcar producto como "necesita reabastecimiento" en dashboard

---

### 3.7 MODULO: Review (Reseñas)

#### CU-022: Publicar Reseña
- **Trigger:** Customer publica una reseña de un producto
- **Evento emitido:** `ReviewCreated`
- **Datos del evento:** `reviewId`, `productId`, `customerId`, `sellerId`, `qualification`, `content`
- **Reacciones:**
  - `NotificationModule` → Email al seller: "Tu producto X recibió una nueva reseña"
  - `SellerModule` → Actualizar calificación promedio del producto

#### CU-023: Eliminar Reseña
- **Trigger:** Customer o admin elimina una reseña
- **Evento emitido:** `ReviewDeleted`
- **Reacciones:**
  - `SellerModule` → Recalcular calificación promedio

---

## 4. Diagrama de Flujo: Compra Completa

```
Customer                    Backend                    Modules
   │                           │                          │
   │  1. POST /cart/checkout   │                          │
   │──────────────────────────►│                          │
   │                           │                          │
   │                     [BEGIN TRANSACTION]               │
   │                           │                          │
   │                     Create Order (PENDING)            │
   │                     Create OrderItems                 │
   │                     Create PaymentInfo                │
   │                           │                          │
   │                     [COMMIT TRANSACTION]              │
   │                           │                          │
   │                           │ ── OrderCreated ────────►│
   │                           │                          │
   │                           │              ┌───────────┤
   │                           │              │ InventoryModule
   │                           │              │  → Reservar stock
   │                           │              │  → Validar disponibilidad
   │                           │              ├───────────┤
   │                           │              │ NotificationModule
   │                           │              │  → Email confirmación
   │                           │              ├───────────┤
   │                           │              │ SellerModule
   │                           │              │  → Notificar seller
   │                           │              └───────────┤
   │                           │                          │
   │  200 OK                   │                          │
   │◄──────────────────────────│                          │
   │                           │                          │
   │  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │                          │
   │                           │                          │
   │  3. Payment Webhook       │                          │
   │  (PaymentCompleted)       │                          │
   │──────────────────────────►│                          │
   │                           │ ── PaymentCompleted ────►│
   │                           │                          │
   │                           │  OrderModule             │
   │                           │  → Status: CONFIRMED     │
   │                           │                          │
   │                           │  NotificationModule      │
   │                           │  → Email: Pago ok        │
   │                           │                          │
   │  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │                          │
   │                           │                          │
   │  5. Seller: "Enviado"     │                          │
   │──────────────────────────►│                          │
   │                           │ ── OrderStatusChanged ──►│
   │                           │  (SHIPPED)               │
   │                           │                          │
   │                           │  InventoryModule         │
   │                           │  → Confirmar stock       │
   │                           │                          │
   │                           │  NotificationModule      │
   │                           │  → Email: "Despachado"   │
```

---

## 5. Implementación Técnica Recomendada

### 5.1 Fase 1: Eventos síncronos (sin cola externa)

Usar el sistema de eventos nativo de Spring Modulith:

```java
// Domain Event
public record OrderCreated(
    UUID orderId,
    UUID customerId,
    UUID sellerId,
    List<OrderItemEvent> items,
    BigDecimal totalAmount,
    String shippingAddress,
    Instant timestamp
) implements Serializable {}

// Publisher (dentro del módulo Order)
@Service
public class OrderSaveUseCase {
    private final ApplicationEventPublisher publisher;
    
    public Order save(Order order) {
        Order saved = orderRepository.save(order);
        publisher.publishEvent(new OrderCreated(...));
        return saved;
    }
}

// Listener (dentro del módulo Inventory)
@Component
public class InventoryEventListener {
    @EventListener
    @Async
    public void onOrderCreated(OrderCreated event) {
        // Reservar stock
    }
}
```

### 5.2 Fase 2: Cola persistente (opcional, si se necesita garantía)

Si se necesita que los eventos sobrevivan reinicios o tengan retry:

- **Opción A:** RabbitMQ (más simple, sufficiente para la mayoría de casos)
- **Opción B:** Kafka (si se espera alto volumen)
- **Opción C:** Tabla `outbox` en PostgreSQL (patrón Transactional Outbox, sin infraestructura adicional)

La transición de Fase 1 a Fase 2 es transparente porque los listeners no cambian, solo la infraestructura de publicación.

---

## 6. Prioridad de Implementación

| Fase | Casos de Uso | Módulos |
|------|-------------|---------|
| **1** | CU-001, CU-004, CU-014, CU-019 | Order + Inventory + Notification + Seller |
| **2** | CU-002, CU-003, CU-005, CU-015 | Status changes + Stock release |
| **3** | CU-008, CU-009, CU-018 | Payment integration |
| **4** | CU-006, CU-017, CU-021 | Stock low alerts |
| **5** | CU-011, CU-012, CU-013 | Cart events |
| **6** | CU-022, CU-023 | Reviews |

---

## 7. Tabla Resumen de Eventos

| Evento | Módulo Origen | Listeners | Canal |
|--------|--------------|-----------|-------|
| `OrderCreated` | Order | Inventory, Notification, Seller | Sync + Email |
| `OrderStatusChanged` | Order | Notification, Inventory, Seller | Sync + Email |
| `OrderCancelled` | Order | Inventory, Notification, Payment, Seller | Sync + Email |
| `PaymentCompleted` | Payment | Order, Notification | Sync + Email |
| `PaymentFailed` | Payment | Order, Inventory, Notification | Sync + Email |
| `RefundProcessed` | Payment | Order, Inventory, Notification | Sync + Email |
| `CartItemAdded` | Cart | Inventory | Sync |
| `CartCheckedOut` | Cart | Order, Inventory, Payment | Sync |
| `StockReserved` | Inventory | Notification | Sync + Email |
| `StockReleased` | Inventory | Notification | Sync + Email |
| `StockLow` | Inventory | Notification, Seller | Email |
| `ReviewCreated` | Review | Notification, Seller | Email |
| `ReviewDeleted` | Review | Seller | Sync |

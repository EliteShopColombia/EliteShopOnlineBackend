# Plan Incremental Q4 — Agregar Javadoc a Clases Públicas

**Objetivo:** Documentar las 418 clases públicas del proyecto con Javadoc.
**Cobertura actual:** 2/420 (0.48%)
**Cobertura objetivo:** 420/420 (100%)

---

## Estrategia

El plan se ejecuta por **batch paralelos**, priorizando por capa de arquitectura:

| Prioridad | Capa | Razón | Clases |
|-----------|------|-------|-------:|
| 1 | **Domain** | Define el lenguaje del negocio — más importante para desarrolladores | ~120 |
| 2 | **Application** | Casos de uso — orquestan la lógica | ~60 |
| 3 | **Infrastructure** | Controladores, DTOs, adaptadores — menos crítico | ~238 |

Cada batch documenta un módulo completo. Se ejecutan en paralelo cuando no hay dependencias.

---

## Batch 1 — Domain Layer (120 clases)

### 1.1 auth/domain (2 clases)
- [ ] `InvalidCredentialsException.java`
- [ ] `InvalidTokenException.java`

### 1.2 cart/domain (12 clases)
- [ ] `Cart.java`
- [ ] `CartCustomerId.java`
- [ ] `CartId.java`
- [ ] `CartItem.java`
- [ ] `CartItemId.java`
- [ ] `CartItemProductId.java`
- [ ] `CartItemQuantity.java`
- [ ] `CartItemUnitPrice.java`
- [ ] `CartRepository.java`
- [ ] `CartItemAlreadyExistsException.java`
- [ ] `CartItemNotFoundException.java`
- [ ] `CartNotFoundException.java`

### 1.3 checkout/domain (6 clases)
- [ ] `CannotBuyOwnProductException.java`
- [ ] `CannotBuyOwnStoreException.java`
- [ ] `CvvRequiredException.java`
- [ ] `EmptyCartException.java`
- [ ] `InsufficientStockException.java`
- [ ] `PaymentFailedException.java`

### 1.4 customer/domain (22 clases)
- [ ] `Customer.java`
- [ ] `CustomerAddress.java`
- [ ] `CustomerCity.java`
- [ ] `CustomerCreatedAt.java`
- [ ] `CustomerDepartment.java`
- [ ] `CustomerDniCreatedAt.java`
- [ ] `CustomerDniNumber.java`
- [ ] `CustomerDniType.java`
- [ ] `CustomerDniUpdatedAt.java`
- [ ] `CustomerEmail.java`
- [ ] `CustomerFirstName.java`
- [ ] `CustomerId.java`
- [ ] `CustomerInfo.java`
- [ ] `CustomerLastName.java`
- [ ] `CustomerPassword.java`
- [ ] `CustomerPhoneNumber.java`
- [ ] `CustomerProfileImage.java`
- [ ] `CustomerUpdatedAt.java`
- [ ] `CustomerRepository.java`
- [ ] `AvatarNotFoundException.java`
- [ ] `CustomerExistException.java`
- [ ] `CustomerNotFoundException.java`

### 1.5 order/domain (31 clases)
- [ ] `Order.java`
- [ ] `OrderCreatedAt.java`
- [ ] `OrderCustomerId.java`
- [ ] `OrderId.java`
- [ ] `OrderItem.java`
- [ ] `OrderItemId.java`
- [ ] `OrderItemOrderId.java`
- [ ] `OrderItemProductId.java`
- [ ] `OrderItemQuantity.java`
- [ ] `OrderItemSellerId.java`
- [ ] `OrderItemUnitPrice.java`
- [ ] `OrderShippingAddress.java`
- [ ] `OrderShippingCity.java`
- [ ] `OrderShippingDepartment.java`
- [ ] `OrderStatus.java`
- [ ] `OrderTotalAmount.java`
- [ ] `OrderUpdatedAt.java`
- [ ] `DisputeReason.java`
- [ ] `TrackingEvent.java`
- [ ] `TrackingEventId.java`
- [ ] `TrackingEventOrderId.java`
- [ ] `TrackingEventStatus.java`
- [ ] `OrderRepository.java`
- [ ] `OrderItemRepository.java`
- [ ] `TrackingEventRepository.java`
- [ ] `OrderCreatedEvent.java`
- [ ] `OrderStatusChangedEvent.java`
- [ ] `InvalidOrderStatusTransitionException.java`
- [ ] `OrderAccessDeniedException.java`
- [ ] `OrderAlreadyExistsException.java`
- [ ] `OrderNotFoundException.java`

### 1.6 payment/domain (23 clases)
- [ ] `Payment.java`
- [ ] `PaymentMethod.java`
- [ ] `PaymentStatus.java`
- [ ] `CheckoutSession.java`
- [ ] `CheckoutSessionRequest.java`
- [ ] `CustomerPaymentMethod.java`
- [ ] `CustomerPaymentMethodBrand.java`
- [ ] `CustomerPaymentMethodCustomerId.java`
- [ ] `CustomerPaymentMethodDocNumber.java`
- [ ] `CustomerPaymentMethodDocType.java`
- [ ] `CustomerPaymentMethodEpaycoCustomerId.java`
- [ ] `CustomerPaymentMethodEpaycoToken.java`
- [ ] `CustomerPaymentMethodExpiryMonth.java`
- [ ] `CustomerPaymentMethodExpiryYear.java`
- [ ] `CustomerPaymentMethodId.java`
- [ ] `CustomerPaymentMethodLast4.java`
- [ ] `TokenizedCard.java`
- [ ] `PaymentGateway.java`
- [ ] `PaymentRepository.java`
- [ ] `CustomerPaymentMethodRepository.java`
- [ ] `PaymentAlreadyProcessedException.java`
- [ ] `PaymentGatewayException.java`
- [ ] `PaymentNotFoundException.java`

### 1.7 product/domain (15 clases)
- [ ] `Product.java`
- [ ] `ProductId.java`
- [ ] `ProductImage.java`
- [ ] `ProductImageId.java`
- [ ] `ProductImageOrder.java`
- [ ] `ProductImageUrl.java`
- [ ] `ProductName.java`
- [ ] `ProductPrice.java`
- [ ] `ProductSellerId.java`
- [ ] `ProductStock.java`
- [ ] `ProductRepository.java`
- [ ] `ProductImageRepository.java`
- [ ] `ProductAlreadyExistsException.java`
- [ ] `ProductNotFoundException.java`
- [ ] `StockInsufficientException.java`

### 1.8 review/domain (14 clases)
- [ ] `Review.java`
- [ ] `ReviewContent.java`
- [ ] `ReviewCustomerId.java`
- [ ] `ReviewId.java`
- [ ] `ReviewImage.java`
- [ ] `ReviewImageId.java`
- [ ] `ReviewImageOrder.java`
- [ ] `ReviewImageUrl.java`
- [ ] `ReviewProductId.java`
- [ ] `ReviewQualify.java`
- [ ] `ReviewRepository.java`
- [ ] `ReviewImageRepository.java`
- [ ] `ReviewNotFoundException.java`
- [ ] `ReviewNotPurchasedException.java`

### 1.9 seller/domain (52 clases)
- [ ] `Seller.java` + 21 modelos de valor
- [ ] `SellerVerification.java` + 13 modelos de verificación
- [ ] `SellerRepository.java`
- [ ] `SellerCreatedEvent.java`, `SellerVerificationEvent.java`
- [ ] 13 excepciones de seller

### 1.10 shared/domain (7 clases)
- [ ] `PageResult.java` ✅ (ya tiene)
- [ ] `ResourceAccessDeniedException.java` ✅ (ya tiene)
- [ ] `AuthorizationService.java` ✅ (ya tiene)
- [ ] `ErrorResponse.java`
- [ ] `GlobalExceptionHandler.java`
- [ ] `StorageException.java`
- [ ] `AsyncConfiguration.java`

---

## Batch 2 — Application Layer (60 clases)

### 2.1 auth/application (3 clases)
- [ ] `LoginUseCase.java`
- [ ] `RefreshUseCase.java`
- [ ] `RegisterUseCase.java`

### 2.2 cart/application (5 clases)
- [ ] `AddToCartUseCase.java`
- [ ] `ClearCartUseCase.java`
- [ ] `GetCartUseCase.java`
- [ ] `RemoveFromCartUseCase.java`
- [ ] `UpdateCartItemUseCase.java`

### 2.3 checkout/application (1 clase)
- [ ] `CheckoutUseCase.java`

### 2.4 customer/application (5 clases)
- [ ] `CustomerAvatarUseCase.java`
- [ ] `CustomerDeleteUseCase.java`
- [ ] `CustomerFindAllUseCase.java`
- [ ] `CustomerFindByIdUseCase.java`
- [ ] `CustomerUpdateUseCase.java`

### 2.5 order/application (21 clases)
- [ ] `AddTrackingEventUseCase.java`
- [ ] `CancelOrderUseCase.java`
- [ ] `CompleteOrderUseCase.java`
- [ ] `ConfirmDeliveryUseCase.java`
- [ ] `DisputeOrderUseCase.java`
- [ ] `FindOrdersByCustomerIdUseCase.java`
- [ ] `FindOrdersBySellerUseCase.java`
- [ ] `GetTrackingEventsUseCase.java`
- [ ] `OrderDeleteUseCase.java`
- [ ] `OrderFindAllUseCase.java`
- [ ] `OrderFindByIdUseCase.java`
- [ ] `OrderSaveUseCase.java`
- [ ] `OrderStatusCountsUseCase.java`
- [ ] `OrderSummaryUseCase.java`
- [ ] `OrderUpdateUseCase.java`
- [ ] `OutForDeliveryUseCase.java`
- [ ] `PrepareOrderUseCase.java`
- [ ] `RefundOrderUseCase.java`
- [ ] `SearchOrdersUseCase.java`
- [ ] `ShipOrderUseCase.java`
- [ ] `UpdateTrackingUseCase.java`

### 2.6 payment/application (8 clases)
- [ ] `ConfirmPaymentUseCase.java`
- [ ] `CreateCheckoutSessionUseCase.java`
- [ ] `DeletePaymentMethodUseCase.java`
- [ ] `GetPaymentMethodsUseCase.java`
- [ ] `RetryPaymentUseCase.java`
- [ ] `RetryWithSavedCardUseCase.java`
- [ ] `SavePaymentMethodUseCase.java`
- [ ] `SetDefaultPaymentMethodUseCase.java`

### 2.7 product/application (5 clases)
- [ ] `ProductDeleteUseCase.java`
- [ ] `ProductFindAllUseCase.java`
- [ ] `ProductFindByIdUseCase.java`
- [ ] `ProductSaveUseCase.java`
- [ ] `ProductUpdateUseCase.java`

### 2.8 review/application (5 clases)
- [ ] `ReviewDeleteUseCase.java`
- [ ] `ReviewFindAllUseCase.java`
- [ ] `ReviewFindByIdUseCase.java`
- [ ] `ReviewFindByProductIdUseCase.java`
- [ ] `ReviewSaveUseCase.java`

### 2.9 seller/application (11 clases)
- [ ] `SellerAvatarUseCase.java`
- [ ] `SellerDeleteUseCase.java`
- [ ] `SellerFindAllUseCase.java`
- [ ] `SellerFindBankInfoBySellerIdUseCase.java`
- [ ] `SellerFindByDniUseCase.java`
- [ ] `SellerFindByIdUseCase.java`
- [ ] `SellerFindContactBySellerIdUseCase.java`
- [ ] `SellerRegistrationUseCase.java`
- [ ] `SellerSaveUseCase.java`
- [ ] `SellerUpdateUseCase.java`
- [ ] `VerifySellerUseCase.java`

### 2.10 shared/notification/application (2 clases)
- [ ] `RetryPendingNotificationsUseCase.java`
- [ ] `SendNotificationUseCase.java`

---

## Batch 3 — Infrastructure Layer (238 clases)

### 3.1 Controllers + DTOs (~50 clases)
Los controladores son la API pública — documentar endpoints, parámetros y respuestas.

### 3.2 Adapters + Persistence (~40 clases)
Adaptadores de infraestructura — documentar implementación de puertos.

### 3.3 Config + Mappers + Listeners + Email (~30 clases)
Configuración de Spring, mappers, listeners de eventos.

### 3.4 Entities JPA (~30 clases)
Entidades de persistencia — documentar mapeo a tablas.

### 3.5 DTOs de ePayco + respuesta (~50 clases)
DTOs de integración con ePayco y respuestas de API.

### 3.6 Notifications infrastructure (~20 clases)
Adaptadores de Slack, programadores, listeners.

---

## Formato de Javadoc

### Para clases de dominio (Value Objects):
```java
/**
 * Representa el identificador único de un cliente en el sistema.
 *
 * <p>Este Value Object garantiza que el ID no sea nulo y sea un UUID válido.
 */
```

### Para casos de uso:
```java
/**
 * Caso de uso para crear una nueva orden de compra.
 *
 * <p>Orquesta la validación de stock, creación de la orden y publicación
 * del evento {@link OrderCreatedEvent}.
 */
```

### Para controladores:
```java
/**
 * Controlador REST para la gestión de órdenes de compra.
 *
 * <p>Endpoints disponibles:
 * <ul>
 *   <li>{@code POST /api/v1/orders} — Crear nueva orden</li>
 *   <li>{@code GET /api/v1/orders/{id}} — Obtener orden por ID</li>
 * </ul>
 */
```

### Para excepciones:
```java
/**
 * Excepción lanzada cuando no se encuentra una orden solicitada.
 *
 * @see com.eliteshop.colombia.order.infrastructure.controller.OrderManagementController
 */
```

---

## Estimación de Esfuerzo

| Batch | Clases | Tiempo estimado | Paralelizable |
|-------|-------:|----------------:|:-------------:|
| Batch 1 (Domain) | 120 | ~2 horas | Sí (10 sub-batches) |
| Batch 2 (Application) | 60 | ~1 hora | Sí (10 sub-batches) |
| Batch 3 (Infrastructure) | 238 | ~3 horas | Sí (6 sub-batches) |
| **Total** | **418** | **~6 horas** | **Sí** |

---

## Criterios de Aceptación

1. Toda clase pública tiene Javadoc de clase
2. Javadoc incluye descripción en español
3. Value Objects documentan restricciones de validación
4. Casos de uso documentan el flujo que orquestan
5. Controladores documentan endpoints disponibles
6. Excepciones documentan cuándo se lanzan
7. `./mvnw spotless:apply` pasa sin errores
8. `./mvnw test` — 317 tests, 0 failures

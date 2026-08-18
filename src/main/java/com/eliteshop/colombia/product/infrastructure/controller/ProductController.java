package com.eliteshop.colombia.product.infrastructure.controller;

import com.eliteshop.colombia.product.application.usecase.*;
import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.model.ProductImage;
import com.eliteshop.colombia.product.domain.model.ProductImageId;
import com.eliteshop.colombia.product.domain.model.ProductImageOrder;
import com.eliteshop.colombia.product.domain.model.ProductImageUrl;
import com.eliteshop.colombia.product.infrastructure.adapter.ProductMinIOAdapter;
import com.eliteshop.colombia.product.infrastructure.controller.dto.ProductRequest;
import com.eliteshop.colombia.product.infrastructure.controller.dto.ProductResponse;
import com.eliteshop.colombia.product.infrastructure.controller.dto.ProductUpdateRequest;
import com.eliteshop.colombia.product.infrastructure.mapper.ProductMapper;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController {

  private final ProductSaveUseCase saveUseCase;
  private final ProductUpdateUseCase updateUseCase;
  private final ProductDeleteUseCase deleteUseCase;
  private final ProductFindAllUseCase findAllUseCase;
  private final ProductFindByIdUseCase findByIdUseCase;
  private final ProductMapper mapper;
  private final ProductMinIOAdapter minIOAdapter;

  @PostMapping("/products")
  public ResponseEntity<ProductResponse> save(
      @Valid @RequestPart("product") ProductRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> images) {
    Product product = mapper.toDomainFromRequest(request);
    saveUseCase.execute(product);

    if (images != null && !images.isEmpty()) {
      List<ProductImage> productImages = uploadProductImages(product.getId(), images);
      product =
          new Product(
              product.getId(),
              product.getSellerId(),
              product.getName(),
              product.getPrice(),
              product.getStock(),
              productImages);
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(product));
  }

  @PutMapping("/products/{id}")
  public ResponseEntity<ProductResponse> update(
      @PathVariable UUID id,
      @Valid @RequestPart("product") ProductUpdateRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> images) {
    Product existingProduct = findByIdUseCase.execute(new ProductId(id)).orElseThrow();
    Product product = mapper.toDomainFromUpdateRequest(request, existingProduct);
    updateUseCase.execute(product);

    if (images != null && !images.isEmpty()) {
      List<ProductImage> productImages = uploadProductImages(new ProductId(id), images);
      product =
          new Product(
              product.getId(),
              product.getSellerId(),
              product.getName(),
              product.getPrice(),
              product.getStock(),
              productImages);
    }

    Product updatedProduct = findByIdUseCase.execute(new ProductId(id)).orElseThrow();
    return ResponseEntity.ok(mapper.toResponse(updatedProduct));
  }

  @DeleteMapping("/products/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    deleteUseCase.execute(new ProductId(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/products")
  public ResponseEntity<Page<ProductResponse>> findAll(
      @PageableDefault(size = 25) Pageable pageable) {
    Page<Product> products = findAllUseCase.execute(pageable);
    Page<ProductResponse> responses = products.map(mapper::toResponse);
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/products/{id}")
  public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
    return findByIdUseCase
        .execute(new ProductId(id))
        .map(product -> ResponseEntity.ok(mapper.toResponse(product)))
        .orElse(ResponseEntity.notFound().build());
  }

  private List<ProductImage> uploadProductImages(ProductId productId, List<MultipartFile> images) {
    List<ProductImage> productImages = new ArrayList<>();
    int order = 1;

    for (MultipartFile file : images) {
      if (order > 7) {
        break;
      }

      try {
        String imageUrl =
            minIOAdapter.uploadImage(
                productId.getValue().toString(), file.getOriginalFilename(), file.getInputStream());
        ProductImage image =
            new ProductImage(
                ProductImageId.generate(),
                productId,
                new ProductImageUrl(imageUrl),
                new ProductImageOrder(order));
        productImages.add(image);
        order++;
      } catch (Exception e) {
        throw new RuntimeException("Error subiendo imagen del producto", e);
      }
    }

    return productImages;
  }
}

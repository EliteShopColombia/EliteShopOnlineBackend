package com.eliteshop.colombia.product.infrastructure.controller;

import com.eliteshop.colombia.product.application.usecase.*;
import com.eliteshop.colombia.product.domain.model.Product;
import com.eliteshop.colombia.product.domain.model.ProductId;
import com.eliteshop.colombia.product.domain.model.ProductImage;
import com.eliteshop.colombia.product.domain.model.ProductImageId;
import com.eliteshop.colombia.product.domain.model.ProductImageOrder;
import com.eliteshop.colombia.product.domain.model.ProductImageUrl;
import com.eliteshop.colombia.product.domain.repository.ProductImageRepository;
import com.eliteshop.colombia.product.infrastructure.adapter.ProductMinIOAdapter;
import com.eliteshop.colombia.product.infrastructure.controller.dto.ProductRequest;
import com.eliteshop.colombia.product.infrastructure.controller.dto.ProductResponse;
import com.eliteshop.colombia.product.infrastructure.controller.dto.ProductUpdateRequest;
import com.eliteshop.colombia.product.infrastructure.mapper.ProductMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
  private final ProductImageRepository productImageRepository;
  private final com.eliteshop.colombia.shared.security.AuthorizationService authorizationService;

  @PostMapping("/products")
  public ResponseEntity<ProductResponse> save(
      @Valid @RequestPart("product") ProductRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> images,
      HttpServletRequest httpRequest,
      Authentication authentication) {
    UUID sellerId = UUID.fromString((String) httpRequest.getAttribute("gateway.sellerId"));
    Product product = mapper.toDomainFromRequest(request, sellerId);
    saveUseCase.execute(product);

    if (images != null && !images.isEmpty()) {
      List<ProductImage> productImages = uploadProductImages(product.getId(), images);
      for (ProductImage image : productImages) {
        productImageRepository.save(image);
      }
      product =
          new Product(
              product.getId(),
              product.getSellerId(),
              product.getName(),
              product.getPrice(),
              product.getStock(),
              productImages);
    }

    String baseUrl = getBaseUrl(httpRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(product, baseUrl));
  }

  @PutMapping("/products/{id}")
  public ResponseEntity<ProductResponse> update(
      @PathVariable UUID id,
      @Valid @RequestPart("product") ProductUpdateRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> images,
      HttpServletRequest httpRequest,
      Authentication authentication) {
    Product existingProduct = findByIdUseCase.execute(new ProductId(id)).orElseThrow();
    authorizationService.requireSeller(
        authentication,
        existingProduct.getSellerId().getValue(),
        (String) httpRequest.getAttribute("gateway.sellerId"));
    Product product = mapper.toDomainFromUpdateRequest(request, existingProduct);
    updateUseCase.execute(product);

    if (images != null && !images.isEmpty()) {
      productImageRepository.deleteByProductId(new ProductId(id));
      List<ProductImage> productImages = uploadProductImages(new ProductId(id), images);
      for (ProductImage image : productImages) {
        productImageRepository.save(image);
      }
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
    String baseUrl = getBaseUrl(httpRequest);
    return ResponseEntity.ok(mapper.toResponse(updatedProduct, baseUrl));
  }

  @DeleteMapping("/products/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID id, HttpServletRequest request, Authentication authentication) {
    Product product = findByIdUseCase.execute(new ProductId(id)).orElseThrow();
    authorizationService.requireSeller(
        authentication,
        product.getSellerId().getValue(),
        (String) request.getAttribute("gateway.sellerId"));
    deleteUseCase.execute(new ProductId(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/products")
  public ResponseEntity<Page<ProductResponse>> findAll(
      @PageableDefault(size = 25) Pageable pageable, HttpServletRequest httpRequest) {
    Page<Product> products = findAllUseCase.execute(pageable);
    String baseUrl = getBaseUrl(httpRequest);
    Page<ProductResponse> responses = products.map(product -> mapper.toResponse(product, baseUrl));
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/products/{id}")
  public ResponseEntity<ProductResponse> findById(
      @PathVariable UUID id, HttpServletRequest httpRequest) {
    String baseUrl = getBaseUrl(httpRequest);
    return findByIdUseCase
        .execute(new ProductId(id))
        .map(product -> ResponseEntity.ok(mapper.toResponse(product, baseUrl)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/products/images")
  public ResponseEntity<InputStreamResource> serveImage(@RequestParam("key") String objectKey) {
    if (objectKey == null || objectKey.contains("..") || !objectKey.startsWith("products/")) {
      return ResponseEntity.badRequest().build();
    }

    try {
      InputStream imageStream = minIOAdapter.downloadImage(objectKey);
      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_JPEG)
          .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
          .body(new InputStreamResource(imageStream));
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  private String getBaseUrl(HttpServletRequest request) {
    String scheme = request.getHeader("X-Forwarded-Proto");
    if (scheme == null) {
      scheme = request.getScheme();
    }
    String serverName = request.getServerName();
    int serverPort = request.getServerPort();
    String contextPath = request.getContextPath();

    StringBuilder url = new StringBuilder();
    url.append(scheme).append("://").append(serverName);

    String forwardedPort = request.getHeader("X-Forwarded-Port");
    if (forwardedPort != null) {
      try {
        int port = Integer.parseInt(forwardedPort);
        if (("http".equals(scheme) && port != 80) || ("https".equals(scheme) && port != 443)) {
          url.append(":").append(port);
        }
      } catch (NumberFormatException ignored) {
      }
    } else if (!scheme.equals(request.getScheme())) {
      // Proxy detected via X-Forwarded-Proto but no X-Forwarded-Port.
      // The local port doesn't match the external scheme, skip appending it.
    } else if (("http".equals(scheme) && serverPort != 80)
        || ("https".equals(scheme) && serverPort != 443)) {
      url.append(":").append(serverPort);
    }
    url.append(contextPath);
    return url.toString();
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
        throw new com.eliteshop.colombia.shared.exception.StorageException(
            "Error subiendo imagen del producto", e);
      }
    }

    return productImages;
  }
}

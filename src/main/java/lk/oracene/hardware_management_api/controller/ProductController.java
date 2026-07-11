package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.ProductRequest;
import lk.oracene.hardware_management_api.dto.request.ProductUpdateRequest;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.dto.response.ProductResponse;
import lk.oracene.hardware_management_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/update/{productId}")
    @Operation(summary = "Update an existing product (excluding stock)")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get an active product by ID")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active products (paginated)")
    public ResponseEntity<PagedResponse<ProductResponse>> getAllActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(productService.getAllActiveProducts(PageRequest.of(page, size))));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all products including inactive ones (paginated)")
    public ResponseEntity<PagedResponse<ProductResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(productService.getAllProducts(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))));
    }

    @DeleteMapping("/delete/{productId}")
    @Operation(summary = "Deactivate a product (sets isActive to false)")
    public ResponseEntity<Void> deactivate(@PathVariable Long productId) {
        productService.deactivateProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/activate")
    @Operation(summary = "Reactivate an inactive product")
    public ResponseEntity<Void> activate(@PathVariable Long productId) {
        productService.activateProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search active products by name (paginated)")
    public ResponseEntity<PagedResponse<ProductResponse>> search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(productService.searchProducts(query, PageRequest.of(page, size))));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get all active products in a category (paginated)")
    public ResponseEntity<PagedResponse<ProductResponse>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(productService.getProductsByCategory(categoryId, PageRequest.of(page, size))));
    }
}

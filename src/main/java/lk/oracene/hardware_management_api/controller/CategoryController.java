package lk.oracene.hardware_management_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.oracene.hardware_management_api.dto.request.CategoryRequest;
import lk.oracene.hardware_management_api.dto.response.CategoryAvailabilityResponse;
import lk.oracene.hardware_management_api.dto.response.CategoryResponse;
import lk.oracene.hardware_management_api.dto.response.PagedResponse;
import lk.oracene.hardware_management_api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @PutMapping("/update/{categoryId}")
    @Operation(summary = "Update an existing category")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, request));
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get a category by ID")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all categories (paginated)")
    public ResponseEntity<PagedResponse<CategoryResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(categoryService.getAllCategories(PageRequest.of(page, size))));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active categories (paginated)")
    public ResponseEntity<PagedResponse<CategoryResponse>> getAllActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PagedResponse.from(categoryService.getAllActiveCategories(PageRequest.of(page, size))));
    }

    @PatchMapping("/{categoryId}/inactive")
    @Operation(summary = "Set category inactive. Only allowed if no active products exist in the category.")
    public ResponseEntity<CategoryAvailabilityResponse> inactive(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.inactiveCategory(categoryId));
    }

    @PatchMapping("/{categoryId}/active")
    @Operation(summary = "Set category active.")
    public ResponseEntity<CategoryAvailabilityResponse> active(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.activeCategory(categoryId));
    }
}

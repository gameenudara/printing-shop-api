package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.CategoryRequest;
import lk.oracene.hardware_management_api.dto.response.CategoryAvailabilityResponse;
import lk.oracene.hardware_management_api.dto.response.CategoryResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Category;
import lk.oracene.hardware_management_api.repository.CategoryRepository;
import lk.oracene.hardware_management_api.repository.ProductRepository;
import lk.oracene.hardware_management_api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Category already exists with name: " + request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Override
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(
                        "Category not found with id: " + categoryId));

        // Only check for duplicate name if name is actually being changed
        if (!category.getName().equalsIgnoreCase(request.getName())
                && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Category already exists with name: " + request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(
                        "Category not found with id: " + categoryId));
        return mapToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllActiveCategories(Pageable pageable) {
        return categoryRepository.findByIsActiveTrue(pageable).map(this::mapToResponse);
    }

    @Override
    public CategoryAvailabilityResponse inactiveCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));

        if (!category.getIsActive()) {
            return CategoryAvailabilityResponse.builder()
                    .categoryId(category.getCategoryId())
                    .categoryName(category.getName())
                    .isActive(false)
                    .hasProducts(false)
                    .message("Category is already inactive.")
                    .build();
        }

        boolean hasProducts = productRepository.existsByCategory_CategoryIdAndIsActiveTrue(categoryId);

        if (hasProducts) {
            return CategoryAvailabilityResponse.builder()
                    .categoryId(category.getCategoryId())
                    .categoryName(category.getName())
                    .isActive(true)
                    .hasProducts(true)
                    .message("Cannot deactivate category. Products are available in this category.")
                    .build();
        }

        category.setIsActive(false);
        categoryRepository.save(category);
        return CategoryAvailabilityResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getName())
                .isActive(false)
                .hasProducts(false)
                .message("No products available in this category. Category has been set to inactive.")
                .build();
    }

    @Override
    public CategoryAvailabilityResponse activeCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));

        if (category.getIsActive()) {
            return CategoryAvailabilityResponse.builder()
                    .categoryId(category.getCategoryId())
                    .categoryName(category.getName())
                    .isActive(true)
                    .hasProducts(productRepository.existsByCategory_CategoryIdAndIsActiveTrue(categoryId))
                    .message("Category is already active.")
                    .build();
        }

        category.setIsActive(true);
        categoryRepository.save(category);
        return CategoryAvailabilityResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getName())
                .isActive(true)
                .hasProducts(productRepository.existsByCategory_CategoryIdAndIsActiveTrue(categoryId))
                .message("Category has been set to active.")
                .build();
    }

    private CategoryResponse mapToResponse(Category category) {
        long productCount = productRepository.countByCategory_CategoryIdAndIsActiveTrue(category.getCategoryId());
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .productCount(productCount)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .build();
    }
}
package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.CategoryRequest;
import lk.oracene.hardware_management_api.dto.response.CategoryAvailabilityResponse;
import lk.oracene.hardware_management_api.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long categoryId, CategoryRequest request);

    CategoryResponse getCategoryById(Long categoryId);

    Page<CategoryResponse> getAllCategories(Pageable pageable);

    Page<CategoryResponse> getAllActiveCategories(Pageable pageable);

    CategoryAvailabilityResponse inactiveCategory(Long categoryId);

    CategoryAvailabilityResponse activeCategory(Long categoryId);
}
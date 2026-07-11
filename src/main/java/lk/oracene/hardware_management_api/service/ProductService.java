package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.ProductRequest;
import lk.oracene.hardware_management_api.dto.request.ProductUpdateRequest;
import lk.oracene.hardware_management_api.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long productId, ProductUpdateRequest request);

    ProductResponse getProductById(Long productId);

    Page<ProductResponse> getAllActiveProducts(Pageable pageable);

    Page<ProductResponse> getAllProducts(Pageable pageable);

    void deactivateProduct(Long productId);

    void activateProduct(Long productId);

    Page<ProductResponse> searchProducts(String query, Pageable pageable);

    Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);
}

package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.ProductRequest;
import lk.oracene.hardware_management_api.dto.request.ProductUpdateRequest;
import lk.oracene.hardware_management_api.dto.response.ProductPurchaseHistoryResponse;
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

    ProductResponse getProductBySku(String sku);

    ProductResponse getProductByBarcode(String barcode);

    Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);

    Page<ProductResponse> getProductsBySupplier(Long supplierId, Pageable pageable);

    Page<ProductResponse> getLowStockProducts(Pageable pageable);

    Page<ProductPurchaseHistoryResponse> getPurchaseHistory(Long productId, Pageable pageable);
}

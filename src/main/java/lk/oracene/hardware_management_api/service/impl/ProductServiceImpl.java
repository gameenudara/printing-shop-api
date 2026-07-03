package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.ProductRequest;
import lk.oracene.hardware_management_api.dto.request.ProductUpdateRequest;
import lk.oracene.hardware_management_api.dto.response.ProductPurchaseHistoryResponse;
import lk.oracene.hardware_management_api.dto.response.ProductResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Category;
import lk.oracene.hardware_management_api.model.Product;
import lk.oracene.hardware_management_api.model.Supplier;
import lk.oracene.hardware_management_api.repository.CategoryRepository;
import lk.oracene.hardware_management_api.repository.ProductRepository;
import lk.oracene.hardware_management_api.repository.SupplierBillItemRepository;
import lk.oracene.hardware_management_api.repository.SupplierRepository;
import lk.oracene.hardware_management_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierBillItemRepository supplierBillItemRepository;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        String brand = blankToNull(request.getBrand());
        String size = blankToNull(request.getSize());
        String colour = blankToNull(request.getColour());

        if (productRepository.existsByNameBrandSizeColour(request.getName(), brand, size, colour)) {
            throw new BadRequestException("Product name already exists: " + request.getName());
        }

        if (request.getSku() != null && !request.getSku().isBlank()
                && productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new BadRequestException("SKU already exists: " + request.getSku());
        }

        if (request.getBarcode() != null && !request.getBarcode().isBlank()
                && productRepository.existsByBarcode(request.getBarcode())) {
            throw new BadRequestException("Barcode already exists: " + request.getBarcode());
        }

        Product product = new Product();
        product.setCategory(category);
        product.setName(request.getName());
        product.setBrand(brand);
        product.setSize(size);
        product.setColour(colour);
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setDescription(request.getDescription());
        product.setReorderLevel(request.getReorderLevel());
        product.setUnit(request.getUnit());
        product.setStockQuantity(BigDecimal.ZERO);
        product.setIsActive(true);
        product.setIsReturn(true);

        Product saved = productRepository.save(product);

        if (saved.getBarcode() == null || saved.getBarcode().isBlank()) {
            saved.setBarcode("pos" + String.format("%06d", saved.getProductId()));
            saved = productRepository.save(saved);
        }

        return mapToResponse(saved);
    }

    @Override
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with id: " + productId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        String brand = blankToNull(request.getBrand());
        String size = blankToNull(request.getSize());
        String colour = blankToNull(request.getColour());

        if (productRepository.existsByNameBrandSizeColourExcludingId(request.getName(), brand, size, colour, productId)) {
            throw new BadRequestException("Product name already exists: " + request.getName());
        }

        // SKU duplicate check (only if changed)
        if (request.getSku() != null && !request.getSku().isBlank()
                && !request.getSku().equalsIgnoreCase(product.getSku())
                && productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new BadRequestException("SKU already exists: " + request.getSku());
        }

        // Barcode duplicate check (only if changed)
        if (request.getBarcode() != null && !request.getBarcode().isBlank()
                && !request.getBarcode().equals(product.getBarcode())
                && productRepository.existsByBarcode(request.getBarcode())) {
            throw new BadRequestException("Barcode already exists: " + request.getBarcode());
        }

        product.setCategory(category);
        product.setName(request.getName());
        product.setBrand(brand);
        product.setSize(size);
        product.setColour(colour);
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setDescription(request.getDescription());
        product.setReorderLevel(request.getReorderLevel());
        product.setUnit(request.getUnit());

        if (request.getCostPrice() != null) product.setCostPrice(request.getCostPrice());
        if (request.getUnitPrice() != null) product.setUnitPrice(request.getUnitPrice());
        if (request.getDiscount() != null) product.setDiscount(request.getDiscount());

        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findByProductIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Active product not found with id: " + productId));
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllActiveProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public void deactivateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with id: " + productId));

        if (Boolean.FALSE.equals(product.getIsActive())) {
            throw new BadRequestException(
                    "Product is already inactive with id: " + productId);
        }

        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    public void activateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with id: " + productId));

        if (Boolean.TRUE.equals(product.getIsActive())) {
            throw new BadRequestException(
                    "Product is already active with id: " + productId);
        }

        product.setIsActive(true);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return Page.empty(pageable);
        }
        return productRepository.searchActive(query.trim(), pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySkuIgnoreCase(sku)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with SKU: " + sku));
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with barcode: " + barcode));
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Category not found with id: " + categoryId);
        }
        return productRepository.findByCategory_CategoryIdAndIsActiveTrue(categoryId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsBySupplier(Long supplierId, Pageable pageable) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new NotFoundException("Supplier not found with id: " + supplierId);
        }
        return productRepository.findBySupplier_SupplierIdAndIsActiveTrue(supplierId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getLowStockProducts(Pageable pageable) {
        return productRepository.findLowStockProducts(pageable).map(product ->
                ProductResponse.builder()
                        .productId(product.getProductId())
                        .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                        .name(product.getName())
                        .stockQuantity(product.getStockQuantity())
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductPurchaseHistoryResponse> getPurchaseHistory(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found with id: " + productId);
        }
        return supplierBillItemRepository.findByProduct_ProductId(productId, pageable)
                .map(item -> ProductPurchaseHistoryResponse.builder()
                        .billNumber(item.getSupplierBill().getBillNumber())
                        .billDate(item.getSupplierBill().getBillDate())
                        .supplierName(item.getSupplierBill().getSupplier().getName())
                        .quantity(item.getQuantity())
                        .unitCostPrice(item.getUnitCostPrice())
                        .build());
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .supplierId(product.getSupplier() != null ? product.getSupplier().getSupplierId() : null)
                .supplierName(product.getSupplier() != null ? product.getSupplier().getName() : null)
                .name(product.getName())
                .brand(product.getBrand())
                .size(product.getSize())
                .colour(product.getColour())
                .sku(product.getSku())
                .barcode(product.getBarcode())
                .description(product.getDescription())
                .costPrice(product.getCostPrice())
                .unitPrice(product.getUnitPrice())
                .discount(product.getDiscount())
                .supplierDiscount(product.getSupplierDiscount())
                .stockQuantity(product.getStockQuantity())
                .reorderLevel(product.getReorderLevel())
                .unit(product.getUnit())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .build();
    }
}
package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.ProductRequest;
import lk.oracene.hardware_management_api.dto.request.ProductUpdateRequest;
import lk.oracene.hardware_management_api.dto.response.ProductResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Category;
import lk.oracene.hardware_management_api.model.Product;
import lk.oracene.hardware_management_api.repository.CategoryRepository;
import lk.oracene.hardware_management_api.repository.ProductRepository;
import lk.oracene.hardware_management_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

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

        Product product = new Product();
        product.setCategory(category);
        product.setName(request.getName());
        product.setBrand(brand);
        product.setSize(size);
        product.setColour(colour);
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setIsActive(true);

        Product saved = productRepository.save(product);
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

        product.setCategory(category);
        product.setName(request.getName());
        product.setBrand(brand);
        product.setSize(size);
        product.setColour(colour);
        product.setDescription(request.getDescription());
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
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Category not found with id: " + categoryId);
        }
        return productRepository.findByCategory_CategoryIdAndIsActiveTrue(categoryId, pageable)
                .map(this::mapToResponse);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .name(product.getName())
                .brand(product.getBrand())
                .size(product.getSize())
                .colour(product.getColour())
                .description(product.getDescription())
                .costPrice(product.getCostPrice())
                .unitPrice(product.getUnitPrice())
                .discount(product.getDiscount())
                .unit(product.getUnit())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .build();
    }
}
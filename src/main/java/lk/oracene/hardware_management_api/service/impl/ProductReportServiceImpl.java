package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.response.MostSellingProductResponse;
import lk.oracene.hardware_management_api.dto.response.OutOfStockReportResponse.OutOfStockItem;
import lk.oracene.hardware_management_api.dto.response.ProductStockReportResponse;
import lk.oracene.hardware_management_api.model.Product;
import lk.oracene.hardware_management_api.model.SalesStatus;
import lk.oracene.hardware_management_api.repository.ProductRepository;
import lk.oracene.hardware_management_api.repository.SalesItemRepository;
import lk.oracene.hardware_management_api.service.ProductReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductReportServiceImpl implements ProductReportService {

    private final ProductRepository productRepository;
    private final SalesItemRepository salesItemRepository;

    @Override
    public ProductStockReportResponse getStockReport() {
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .toList();

        BigDecimal totalStockQty = BigDecimal.ZERO;
        BigDecimal totalSellingPrice = BigDecimal.ZERO;
        BigDecimal totalPurchasingPrice = BigDecimal.ZERO;

        for (Product p : products) {
            BigDecimal qty = p.getStockQuantity() != null ? p.getStockQuantity() : BigDecimal.ZERO;
            BigDecimal unitPrice = p.getUnitPrice() != null ? p.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal costPrice = p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO;
            BigDecimal discount = p.getDiscount() != null ? p.getDiscount() : BigDecimal.ZERO;
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

            totalStockQty = totalStockQty.add(qty);
            totalSellingPrice = totalSellingPrice.add(unitPrice.multiply(discountMultiplier).multiply(qty));
            totalPurchasingPrice = totalPurchasingPrice.add(costPrice.multiply(qty));
        }

        totalSellingPrice = totalSellingPrice.setScale(2, RoundingMode.HALF_UP);
        totalPurchasingPrice = totalPurchasingPrice.setScale(2, RoundingMode.HALF_UP);

        BigDecimal averageMargin = totalSellingPrice.compareTo(BigDecimal.ZERO) > 0
                ? totalSellingPrice.subtract(totalPurchasingPrice)
                        .divide(totalSellingPrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ProductStockReportResponse.builder()
                .totalProducts(products.size())
                .totalStockQuantity(totalStockQty)
                .totalSellingPrice(totalSellingPrice)
                .totalPurchasingPrice(totalPurchasingPrice)
                .averageMargin(averageMargin)
                .build();
    }

    @Override
    public Page<OutOfStockItem> getOutOfStockReport(Pageable pageable) {
        return productRepository.findOutOfStockProductsPaged(pageable)
                .map(p -> OutOfStockItem.builder()
                        .productId(p.getProductId())
                        .name(p.getName())
                        .sku(p.getSku())
                        .brand(p.getBrand())
                        .colour(p.getColour())
                        .size(p.getSize())
                        .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                        .unitPrice(p.getUnitPrice())
                        .build());
    }

    @Override
    public Page<MostSellingProductResponse> getMostSellingProducts(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        List<SalesStatus> countableStatuses = List.of(
                SalesStatus.PENDING, SalesStatus.PARTIAL, SalesStatus.COMPLETED);

        return salesItemRepository.findMostSellingProducts(fromDate, toDate, countableStatuses, pageable)
                .map(row -> MostSellingProductResponse.builder()
                        .productId((Long) row[0])
                        .productName((String) row[1])
                        .totalQuantitySold((BigDecimal) row[2])
                        .totalRevenue(((BigDecimal) row[3]).setScale(2, RoundingMode.HALF_UP))
                        .build());
    }
}

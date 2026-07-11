package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.response.MostSellingProductResponse;
import lk.oracene.hardware_management_api.model.SalesStatus;
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

    private final SalesItemRepository salesItemRepository;

    @Override
    public Page<MostSellingProductResponse> getMostSellingProducts(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        List<SalesStatus> countableStatuses = List.of(
                SalesStatus.UNPAID, SalesStatus.ADVANCE_PAID, SalesStatus.PAID);

        return salesItemRepository.findMostSellingProducts(fromDate, toDate, countableStatuses, pageable)
                .map(row -> MostSellingProductResponse.builder()
                        .productId((Long) row[0])
                        .productName((String) row[1])
                        .totalQuantitySold((BigDecimal) row[2])
                        .totalRevenue(((BigDecimal) row[3]).setScale(2, RoundingMode.HALF_UP))
                        .build());
    }
}

package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.response.MostSellingProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ProductReportService {

    Page<MostSellingProductResponse> getMostSellingProducts(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}

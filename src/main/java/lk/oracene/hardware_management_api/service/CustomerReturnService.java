package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.CustomerReturnRequest;
import lk.oracene.hardware_management_api.dto.response.CustomerReturnResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerReturnService {
    CustomerReturnResponse create(CustomerReturnRequest request);
    CustomerReturnResponse getById(Long returnId);
    Page<CustomerReturnResponse> getAll(Pageable pageable);
    List<CustomerReturnResponse> getBySaleId(Long saleId);
    Page<CustomerReturnResponse> getByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable);
}

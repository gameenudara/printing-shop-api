package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.CashMovementRequest;
import lk.oracene.hardware_management_api.dto.request.CloseDrawerRequest;
import lk.oracene.hardware_management_api.dto.request.OpenDrawerRequest;
import lk.oracene.hardware_management_api.dto.response.CashDrawerSessionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CashDrawerService {

    CashDrawerSessionResponse openDrawer(OpenDrawerRequest request);

    CashDrawerSessionResponse closeDrawer(CloseDrawerRequest request);

    CashDrawerSessionResponse getCurrentSession();

    CashDrawerSessionResponse addManualCashIn(CashMovementRequest request);

    CashDrawerSessionResponse addManualCashOut(CashMovementRequest request);

    Page<CashDrawerSessionResponse> getSessionHistory(Pageable pageable);

    CashDrawerSessionResponse getSessionDetail(Long sessionId);

    void recordSaleCashIn(BigDecimal amount, String reason, Long saleId);

    void recordCustomerPaymentCashIn(BigDecimal amount, String reason, Long paymentId);
}

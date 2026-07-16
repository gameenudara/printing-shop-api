package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.CashTransactionRequest;
import lk.oracene.hardware_management_api.dto.request.OpenDrawerRequest;
import lk.oracene.hardware_management_api.dto.response.CashDrawerSessionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CashDrawerService {

    CashDrawerSessionResponse openDrawer(OpenDrawerRequest request);

    CashDrawerSessionResponse getCurrentSession(Pageable pageable);

    CashDrawerSessionResponse addCashIn(CashTransactionRequest request);

    CashDrawerSessionResponse addCashOut(CashTransactionRequest request);

    Page<CashDrawerSessionResponse> getSessionHistory(Pageable pageable);

    CashDrawerSessionResponse getSessionDetail(Long sessionId);

    void recordSalePayment(BigDecimal amount, String reason, Long saleId);

    void recordCustomerPayment(BigDecimal amount, String reason, Long paymentId);

    void recordDrawerOpenTest();
}

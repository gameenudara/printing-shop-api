package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.CashTransactionRequest;
import lk.oracene.hardware_management_api.dto.request.OpenDrawerRequest;
import lk.oracene.hardware_management_api.dto.response.CashDrawerSessionResponse;
import lk.oracene.hardware_management_api.dto.response.CashTransactionResponse;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.CashDrawerSession;
import lk.oracene.hardware_management_api.model.CashTransaction;
import lk.oracene.hardware_management_api.model.CashTransactionType;
import lk.oracene.hardware_management_api.repository.CashDrawerSessionRepository;
import lk.oracene.hardware_management_api.repository.CashTransactionRepository;
import lk.oracene.hardware_management_api.service.CashDrawerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CashDrawerServiceImpl implements CashDrawerService {

    private static final Set<CashTransactionType> IN_TYPES = Set.of(
            CashTransactionType.OPENING_BALANCE, CashTransactionType.MANUAL_CASH_IN,
            CashTransactionType.SALE_PAYMENT, CashTransactionType.CUSTOMER_PAYMENT);

    private static final Set<CashTransactionType> CASH_IN_TYPES = Set.of(
            CashTransactionType.MANUAL_CASH_IN, CashTransactionType.SALE_PAYMENT,
            CashTransactionType.CUSTOMER_PAYMENT);

    private static final Set<CashTransactionType> CASH_OUT_TYPES = Set.of(
            CashTransactionType.MANUAL_CASH_OUT);

    private final CashDrawerSessionRepository sessionRepository;
    private final CashTransactionRepository transactionRepository;

    @Override
    public CashDrawerSessionResponse openDrawer(OpenDrawerRequest request) {
        CashDrawerSession session = createSession(request.getOpeningBalance(), request.getNotes());
        return buildResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public CashDrawerSessionResponse getCurrentSession() {
        return buildResponse(getOrCreateCurrentSession());
    }

    @Override
    public CashDrawerSessionResponse addCashIn(CashTransactionRequest request) {
        CashDrawerSession session = getOrCreateCurrentSession();
        addTransaction(session, CashTransactionType.MANUAL_CASH_IN, request.getAmount(), request.getReason(), null);
        return buildResponse(session);
    }

    @Override
    public CashDrawerSessionResponse addCashOut(CashTransactionRequest request) {
        CashDrawerSession session = getOrCreateCurrentSession();
        addTransaction(session, CashTransactionType.MANUAL_CASH_OUT, request.getAmount(), request.getReason(), null);
        return buildResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CashDrawerSessionResponse> getSessionHistory(Pageable pageable) {
        return sessionRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::buildResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CashDrawerSessionResponse getSessionDetail(Long sessionId) {
        CashDrawerSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Cash drawer session not found with id: " + sessionId));
        return buildResponse(session);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSalePayment(BigDecimal amount, String reason, Long saleId) {
        CashDrawerSession session = getOrCreateCurrentSession();
        addTransaction(session, CashTransactionType.SALE_PAYMENT, amount, reason, saleId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordCustomerPayment(BigDecimal amount, String reason, Long paymentId) {
        CashDrawerSession session = getOrCreateCurrentSession();
        addTransaction(session, CashTransactionType.CUSTOMER_PAYMENT, amount, reason, paymentId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordDrawerOpenTest() {
        CashDrawerSession session = getOrCreateCurrentSession();
        addTransaction(session, CashTransactionType.DRAWER_OPEN_TEST, BigDecimal.ZERO,
                "Manual drawer open (no sale)", null);
    }

    private CashDrawerSession getOrCreateCurrentSession() {
        return sessionRepository.findFirstByOrderByCreatedAtDesc()
                .orElseGet(() -> createSession(BigDecimal.ZERO, "Auto-opened (first use)"));
    }

    private CashDrawerSession createSession(BigDecimal openingBalance, String notes) {
        CashDrawerSession session = new CashDrawerSession();
        session.setOpeningBalance(openingBalance);
        session.setNotes(notes);
        CashDrawerSession saved = sessionRepository.save(session);

        addTransaction(saved, CashTransactionType.OPENING_BALANCE, openingBalance, "Opening balance", null);
        return saved;
    }

    private void addTransaction(CashDrawerSession session, CashTransactionType type, BigDecimal amount, String reason, Long referenceId) {
        CashTransaction transaction = new CashTransaction();
        transaction.setSession(session);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setReason(reason);
        transaction.setReferenceId(referenceId);
        transactionRepository.save(transaction);
    }

    private BigDecimal computeBalance(List<CashTransaction> transactions) {
        return transactions.stream()
                .map(t -> IN_TYPES.contains(t.getType()) ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumByTypes(List<CashTransaction> transactions, Set<CashTransactionType> types) {
        return transactions.stream()
                .filter(t -> types.contains(t.getType()))
                .map(CashTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private CashDrawerSessionResponse buildResponse(CashDrawerSession session) {
        List<CashTransaction> transactions = transactionRepository.findBySession_SessionIdOrderByCreatedAtAsc(session.getSessionId());
        BigDecimal currentBalance = computeBalance(transactions);

        List<CashTransactionResponse> transactionResponses = transactions.stream()
                .map(t -> CashTransactionResponse.builder()
                        .transactionId(t.getTransactionId())
                        .type(t.getType())
                        .amount(t.getAmount())
                        .reason(t.getReason())
                        .referenceId(t.getReferenceId())
                        .createdAt(t.getCreatedAt())
                        .createdBy(t.getCreatedBy())
                        .build())
                .toList();

        return CashDrawerSessionResponse.builder()
                .sessionId(session.getSessionId())
                .openingBalance(session.getOpeningBalance())
                .currentBalance(currentBalance)
                .totalCashIn(sumByTypes(transactions, CASH_IN_TYPES))
                .totalCashOut(sumByTypes(transactions, CASH_OUT_TYPES))
                .notes(session.getNotes())
                .openedBy(session.getCreatedBy())
                .openedAt(session.getCreatedAt())
                .transactions(transactionResponses)
                .build();
    }
}

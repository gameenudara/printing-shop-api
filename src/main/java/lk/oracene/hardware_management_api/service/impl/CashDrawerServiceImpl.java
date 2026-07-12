package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.CashMovementRequest;
import lk.oracene.hardware_management_api.dto.request.CloseDrawerRequest;
import lk.oracene.hardware_management_api.dto.request.OpenDrawerRequest;
import lk.oracene.hardware_management_api.dto.response.CashDrawerSessionResponse;
import lk.oracene.hardware_management_api.dto.response.CashMovementResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.CashDrawerSession;
import lk.oracene.hardware_management_api.model.CashDrawerStatus;
import lk.oracene.hardware_management_api.model.CashMovement;
import lk.oracene.hardware_management_api.model.CashMovementType;
import lk.oracene.hardware_management_api.repository.CashDrawerSessionRepository;
import lk.oracene.hardware_management_api.repository.CashMovementRepository;
import lk.oracene.hardware_management_api.service.CashDrawerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CashDrawerServiceImpl implements CashDrawerService {

    private static final Set<CashMovementType> IN_TYPES = Set.of(
            CashMovementType.OPENING_FLOAT, CashMovementType.SALE_CASH_IN,
            CashMovementType.CUSTOMER_PAYMENT_CASH_IN, CashMovementType.MANUAL_CASH_IN);

    private final CashDrawerSessionRepository sessionRepository;
    private final CashMovementRepository movementRepository;

    @Override
    public CashDrawerSessionResponse openDrawer(OpenDrawerRequest request) {
        sessionRepository.findFirstByStatusOrderByCreatedAtDesc(CashDrawerStatus.OPEN)
                .ifPresent(s -> {
                    throw new BadRequestException(
                            "A cash drawer session is already open (id: " + s.getSessionId() + ")");
                });

        CashDrawerSession session = openNewSession(request.getOpeningBalance(), request.getNotes());
        return buildResponse(session);
    }

    @Override
    public CashDrawerSessionResponse closeDrawer(CloseDrawerRequest request) {
        CashDrawerSession session = getOpenSessionOrThrow();

        List<CashMovement> movements = movementRepository.findBySession_SessionIdOrderByCreatedAtAsc(session.getSessionId());
        BigDecimal expected = computeBalance(movements);

        session.setExpectedBalance(expected);
        session.setClosingBalanceActual(request.getClosingBalanceActual());
        session.setVariance(request.getClosingBalanceActual().subtract(expected).setScale(2, RoundingMode.HALF_UP));
        session.setStatus(CashDrawerStatus.CLOSED);
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            session.setNotes(request.getNotes());
        }
        CashDrawerSession saved = sessionRepository.save(session);
        return buildResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CashDrawerSessionResponse getCurrentSession() {
        return buildResponse(getOpenSessionOrThrow());
    }

    @Override
    public CashDrawerSessionResponse addManualCashIn(CashMovementRequest request) {
        CashDrawerSession session = getOrOpenCurrentSession();
        addMovement(session, CashMovementType.MANUAL_CASH_IN, request.getAmount(), request.getReason(), null);
        return buildResponse(session);
    }

    @Override
    public CashDrawerSessionResponse addManualCashOut(CashMovementRequest request) {
        CashDrawerSession session = getOrOpenCurrentSession();
        addMovement(session, CashMovementType.MANUAL_CASH_OUT, request.getAmount(), request.getReason(), null);
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
    public void recordSaleCashIn(BigDecimal amount, String reason, Long saleId) {
        CashDrawerSession session = getOrOpenCurrentSession();
        addMovement(session, CashMovementType.SALE_CASH_IN, amount, reason, saleId);
    }

    @Override
    public void recordCustomerPaymentCashIn(BigDecimal amount, String reason, Long paymentId) {
        CashDrawerSession session = getOrOpenCurrentSession();
        addMovement(session, CashMovementType.CUSTOMER_PAYMENT_CASH_IN, amount, reason, paymentId);
    }

    private CashDrawerSession getOpenSessionOrThrow() {
        return sessionRepository.findFirstByStatusOrderByCreatedAtDesc(CashDrawerStatus.OPEN)
                .orElseThrow(() -> new NotFoundException("No cash drawer session is currently open"));
    }

    private CashDrawerSession getOrOpenCurrentSession() {
        return sessionRepository.findFirstByStatusOrderByCreatedAtDesc(CashDrawerStatus.OPEN)
                .orElseGet(() -> openNewSession(BigDecimal.ZERO, "Auto-opened (no prior open session)"));
    }

    private CashDrawerSession openNewSession(BigDecimal openingBalance, String notes) {
        CashDrawerSession session = new CashDrawerSession();
        session.setOpeningBalance(openingBalance);
        session.setStatus(CashDrawerStatus.OPEN);
        session.setNotes(notes);
        CashDrawerSession saved = sessionRepository.save(session);

        addMovement(saved, CashMovementType.OPENING_FLOAT, openingBalance, "Opening float", null);
        return saved;
    }

    private void addMovement(CashDrawerSession session, CashMovementType type, BigDecimal amount, String reason, Long referenceId) {
        CashMovement movement = new CashMovement();
        movement.setSession(session);
        movement.setType(type);
        movement.setAmount(amount);
        movement.setReason(reason);
        movement.setReferenceId(referenceId);
        movementRepository.save(movement);
    }

    private BigDecimal computeBalance(List<CashMovement> movements) {
        return movements.stream()
                .map(m -> IN_TYPES.contains(m.getType()) ? m.getAmount() : m.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private CashDrawerSessionResponse buildResponse(CashDrawerSession session) {
        List<CashMovement> movements = movementRepository.findBySession_SessionIdOrderByCreatedAtAsc(session.getSessionId());
        BigDecimal currentBalance = computeBalance(movements);

        List<CashMovementResponse> movementResponses = movements.stream()
                .map(m -> CashMovementResponse.builder()
                        .movementId(m.getMovementId())
                        .type(m.getType())
                        .amount(m.getAmount())
                        .reason(m.getReason())
                        .referenceId(m.getReferenceId())
                        .createdAt(m.getCreatedAt())
                        .createdBy(m.getCreatedBy())
                        .build())
                .toList();

        boolean closed = session.getStatus() == CashDrawerStatus.CLOSED;

        return CashDrawerSessionResponse.builder()
                .sessionId(session.getSessionId())
                .status(session.getStatus())
                .openingBalance(session.getOpeningBalance())
                .currentBalance(currentBalance)
                .closingBalanceActual(session.getClosingBalanceActual())
                .expectedBalance(session.getExpectedBalance())
                .variance(session.getVariance())
                .notes(session.getNotes())
                .openedBy(session.getCreatedBy())
                .openedAt(session.getCreatedAt())
                .closedBy(closed ? session.getUpdatedBy() : null)
                .closedAt(closed ? session.getUpdatedAt() : null)
                .movements(movementResponses)
                .build();
    }
}

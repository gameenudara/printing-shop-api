package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.SupplierPaymentRequest;
import lk.oracene.hardware_management_api.dto.response.SupplierPaymentResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Cheque;
import lk.oracene.hardware_management_api.model.ChequeStatus;
import lk.oracene.hardware_management_api.model.ChequeType;
import lk.oracene.hardware_management_api.model.SupplierBill;
import lk.oracene.hardware_management_api.model.SupplierBillStatus;
import lk.oracene.hardware_management_api.model.SupplierPayment;
import lk.oracene.hardware_management_api.model.SupplierPaymentMethod;
import lk.oracene.hardware_management_api.model.SupplierPaymentStatus;
import lk.oracene.hardware_management_api.repository.ChequeRepository;
import lk.oracene.hardware_management_api.repository.SupplierBillRepository;
import lk.oracene.hardware_management_api.repository.SupplierPaymentRepository;
import lk.oracene.hardware_management_api.service.SupplierPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierPaymentServiceImpl implements SupplierPaymentService {

    private final SupplierPaymentRepository supplierPaymentRepository;
    private final SupplierBillRepository supplierBillRepository;
    private final ChequeRepository chequeRepository;

    @Override
    public SupplierPaymentResponse recordPayment(SupplierPaymentRequest request) {
        SupplierBill bill = supplierBillRepository.findById(request.getSupplierBillId())
                .orElseThrow(() -> new NotFoundException(
                        "Supplier bill not found with id: " + request.getSupplierBillId()));

        if (bill.getStatus() == SupplierBillStatus.CANCELLED) {
            throw new BadRequestException("Cannot pay a cancelled bill");
        }
        if (bill.getStatus() == SupplierBillStatus.PAID) {
            throw new BadRequestException("Bill is already fully paid");
        }

        BigDecimal remaining = bill.getTotalAmount().subtract(bill.getPaidAmount());
        if (request.getAmount().compareTo(remaining) > 0) {
            throw new BadRequestException(
                    "Payment amount " + request.getAmount() +
                    " exceeds remaining balance " + remaining);
        }

        Cheque cheque = null;
        if (request.getMethod() == SupplierPaymentMethod.CHEQUE) {
            validateChequeFields(request);
            cheque = buildCheque(request, bill);
            cheque = chequeRepository.save(cheque);
        }

        SupplierPayment payment = new SupplierPayment();
        payment.setSupplierBill(bill);
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus(SupplierPaymentStatus.SUCCESS);
        payment.setReferenceNo(request.getReferenceNo());
        payment.setPaidAt(LocalDateTime.now());
        payment.setCheque(cheque);

        SupplierPayment savedPayment = supplierPaymentRepository.save(payment);

        BigDecimal newPaid = bill.getPaidAmount().add(request.getAmount());
        bill.setPaidAmount(newPaid);
        bill.setStatus(newPaid.compareTo(bill.getTotalAmount()) >= 0
                ? SupplierBillStatus.PAID
                : SupplierBillStatus.PARTIALLY_PAID);
        supplierBillRepository.save(bill);

        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierPaymentResponse> getPaymentsByBill(Long supplierBillId, Pageable pageable) {
        if (!supplierBillRepository.existsById(supplierBillId)) {
            throw new NotFoundException("Supplier bill not found with id: " + supplierBillId);
        }

        return supplierPaymentRepository.findBySupplierBill_SupplierBillId(supplierBillId, pageable)
                .map(this::mapToResponse);
    }

    private void validateChequeFields(SupplierPaymentRequest request) {
        if (request.getChequeNumber() == null || request.getChequeNumber().isBlank()) {
            throw new BadRequestException("Cheque number is required for CHEQUE payment");
        }
        if (request.getBankName() == null) {
            throw new BadRequestException("Bank name is required for CHEQUE payment");
        }
        if (request.getChequeIssueDate() == null) {
            throw new BadRequestException("Cheque issue date is required for CHEQUE payment");
        }
        if (chequeRepository.existsByChequeNumber(request.getChequeNumber())) {
            throw new BadRequestException(
                    "Cheque number already exists: " + request.getChequeNumber());
        }
    }

    private Cheque buildCheque(SupplierPaymentRequest request, SupplierBill bill) {
        Cheque cheque = new Cheque();
        cheque.setSupplier(bill.getSupplier());
        cheque.setChequeType(ChequeType.GIVEN_TO_SUPPLIER);
        cheque.setChequeNumber(request.getChequeNumber());
        cheque.setBankName(request.getBankName());
        cheque.setBranchName(request.getBranchName());
        cheque.setAmount(request.getAmount());
        cheque.setIssueDate(request.getChequeIssueDate());
        cheque.setDueDate(request.getChequeDueDate());
        cheque.setChequeStatus(ChequeStatus.PENDING);
        return cheque;
    }

    private SupplierPaymentResponse mapToResponse(SupplierPayment payment) {
        return SupplierPaymentResponse.builder()
                .supplierPaymentId(payment.getSupplierPaymentId())
                .supplierBillId(payment.getSupplierBill().getSupplierBillId())
                .billNumber(payment.getSupplierBill().getBillNumber())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .referenceNo(payment.getReferenceNo())
                .paidAt(payment.getPaidAt())
                .chequeId(payment.getCheque() != null ? payment.getCheque().getChequePaymentId() : null)
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .createdBy(payment.getCreatedBy())
                .updatedBy(payment.getUpdatedBy())
                .build();
    }
}

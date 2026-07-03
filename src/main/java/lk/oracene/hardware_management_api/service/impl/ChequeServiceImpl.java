package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.response.ChequeResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Cheque;
import lk.oracene.hardware_management_api.model.ChequeStatus;
import lk.oracene.hardware_management_api.model.ChequeType;
import lk.oracene.hardware_management_api.repository.ChequeRepository;
import lk.oracene.hardware_management_api.repository.CustomerRepository;
import lk.oracene.hardware_management_api.repository.SupplierRepository;
import lk.oracene.hardware_management_api.service.ChequeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChequeServiceImpl implements ChequeService {

    private final ChequeRepository chequeRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public ChequeResponse getChequeById(Long chequeId) {
        Cheque cheque = chequeRepository.findById(chequeId)
                .orElseThrow(() -> new NotFoundException("Cheque not found with id: " + chequeId));
        return mapToResponse(cheque);
    }

    @Override
    public Page<ChequeResponse> getAllCheques(Pageable pageable) {
        return chequeRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public Page<ChequeResponse> getChequesByType(ChequeType chequeType, Pageable pageable) {
        return chequeRepository.findByChequeType(chequeType, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<ChequeResponse> getChequesByStatus(ChequeStatus chequeStatus, Pageable pageable) {
        return chequeRepository.findByChequeStatus(chequeStatus, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<ChequeResponse> getChequesByCustomer(Long customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer not found with id: " + customerId);
        }
        return chequeRepository.findByCustomer_CustomerId(customerId, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<ChequeResponse> getPendingChequesByCustomer(Long customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer not found with id: " + customerId);
        }
        return chequeRepository.findByCustomer_CustomerIdAndChequeStatus(customerId, ChequeStatus.PENDING, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<ChequeResponse> getChequesBySupplier(Long supplierId, Pageable pageable) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new NotFoundException("Supplier not found with id: " + supplierId);
        }
        return chequeRepository.findBySupplier_SupplierId(supplierId, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<ChequeResponse> getPendingChequesBySupplier(Long supplierId, Pageable pageable) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new NotFoundException("Supplier not found with id: " + supplierId);
        }
        return chequeRepository.findBySupplier_SupplierIdAndChequeStatus(supplierId, ChequeStatus.PENDING, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public ChequeResponse getChequeByNumber(String chequeNumber) {
        Cheque cheque = chequeRepository.findByChequeNumber(chequeNumber)
                .orElseThrow(() -> new NotFoundException("Cheque not found with number: " + chequeNumber));
        return mapToResponse(cheque);
    }

    @Override
    @Transactional
    public ChequeResponse markAsReturned(Long chequeId) {
        Cheque cheque = chequeRepository.findById(chequeId)
                .orElseThrow(() -> new NotFoundException("Cheque not found with id: " + chequeId));
        if (cheque.getChequeStatus() == ChequeStatus.CANCELLED) {
            throw new BadRequestException("Cannot mark a cancelled cheque as returned");
        }
        if (cheque.getChequeStatus() == ChequeStatus.RETURNED) {
            throw new BadRequestException("Cheque is already marked as returned");
        }
        cheque.setChequeStatus(ChequeStatus.RETURNED);
        return mapToResponse(chequeRepository.save(cheque));
    }

    @Override
    @Transactional
    public ChequeResponse markAsCancelled(Long chequeId) {
        Cheque cheque = chequeRepository.findById(chequeId)
                .orElseThrow(() -> new NotFoundException("Cheque not found with id: " + chequeId));
        if (cheque.getChequeStatus() == ChequeStatus.CLEARED) {
            throw new BadRequestException("Cannot cancel a cleared cheque");
        }
        if (cheque.getChequeStatus() == ChequeStatus.CANCELLED) {
            throw new BadRequestException("Cheque is already cancelled");
        }
        cheque.setChequeStatus(ChequeStatus.CANCELLED);
        return mapToResponse(chequeRepository.save(cheque));
    }

    private ChequeResponse mapToResponse(Cheque cheque) {
        return ChequeResponse.builder()
                .chequePaymentId(cheque.getChequePaymentId())
                .chequeType(cheque.getChequeType())
                .chequeStatus(cheque.getChequeStatus())
                .chequeNumber(cheque.getChequeNumber())
                .bankName(cheque.getBankName())
                .branchName(cheque.getBranchName())
                .amount(cheque.getAmount())
                .issueDate(cheque.getIssueDate())
                .dueDate(cheque.getDueDate())
                .paymentId(cheque.getPayment() != null ? cheque.getPayment().getPaymentId() : null)
                .customerId(cheque.getCustomer() != null ? cheque.getCustomer().getCustomerId() : null)
                .customerName(cheque.getCustomer() != null ? cheque.getCustomer().getCustomerName() : null)
                .supplierId(cheque.getSupplier() != null ? cheque.getSupplier().getSupplierId() : null)
                .supplierName(cheque.getSupplier() != null ? cheque.getSupplier().getName() : null)
                .createdAt(cheque.getCreatedAt())
                .updatedAt(cheque.getUpdatedAt())
                .createdBy(cheque.getCreatedBy())
                .updatedBy(cheque.getUpdatedBy())
                .build();
    }
}

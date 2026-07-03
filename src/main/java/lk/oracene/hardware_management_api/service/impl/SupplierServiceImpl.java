package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.SupplierRequest;
import lk.oracene.hardware_management_api.dto.response.SupplierResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Supplier;
import lk.oracene.hardware_management_api.model.SupplierBillStatus;
import lk.oracene.hardware_management_api.repository.SupplierBillRepository;
import lk.oracene.hardware_management_api.repository.SupplierRepository;
import lk.oracene.hardware_management_api.service.SupplierService;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierBillRepository supplierBillRepository;

    @Override
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && supplierRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already exists: " + request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && supplierRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setIsActive(true);

        Supplier saved = supplierRepository.save(supplier);
        return mapToResponse(saved);
    }

    @Override
    public SupplierResponse updateSupplier(Long supplierId, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException(
                        "Supplier not found with id: " + supplierId));

        supplier.setName(request.getName());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());

        Supplier updated = supplierRepository.save(supplier);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long supplierId) {
        Supplier supplier = supplierRepository.findBySupplierIdAndIsActiveTrue(supplierId)
                .orElseThrow(() -> new NotFoundException(
                        "Active supplier not found with id: " + supplierId));
        return mapToResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAllActiveSuppliers(Pageable pageable) {
        return supplierRepository.findByIsActiveTrue(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public void deactivateSupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException(
                        "Supplier not found with id: " + supplierId));

        if (Boolean.FALSE.equals(supplier.getIsActive())) {
            throw new BadRequestException(
                    "Supplier is already inactive with id: " + supplierId);
        }

        if (supplierBillRepository.existsBySupplier_SupplierIdAndStatusIn(supplierId,
                List.of(SupplierBillStatus.UNPAID, SupplierBillStatus.PARTIALLY_PAID))) {
            throw new BadRequestException(
                    "Cannot deactivate supplier with unpaid bills. Please settle all outstanding bills first.");
        }

        supplier.setIsActive(false);
        supplierRepository.save(supplier);
    }

    @Override
    public void activateSupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException(
                        "Supplier not found with id: " + supplierId));

        if (Boolean.TRUE.equals(supplier.getIsActive())) {
            throw new BadRequestException(
                    "Supplier is already active with id: " + supplierId);
        }

        supplier.setIsActive(true);
        supplierRepository.save(supplier);
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .supplierId(supplier.getSupplierId())
                .name(supplier.getName())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .isActive(supplier.getIsActive())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .createdBy(supplier.getCreatedBy())
                .updatedBy(supplier.getUpdatedBy())
                .build();
    }
}
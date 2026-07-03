package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.SupplierBillItemRequest;
import lk.oracene.hardware_management_api.dto.request.SupplierBillItemUpdateRequest;
import lk.oracene.hardware_management_api.dto.request.SupplierBillRequest;
import lk.oracene.hardware_management_api.dto.response.OutstandingResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierBillItemResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierBillResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierBillSummaryResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Product;
import lk.oracene.hardware_management_api.model.Supplier;
import lk.oracene.hardware_management_api.model.SupplierBill;
import lk.oracene.hardware_management_api.model.SupplierBillItem;
import lk.oracene.hardware_management_api.model.SupplierBillStatus;
import lk.oracene.hardware_management_api.repository.ProductRepository;
import lk.oracene.hardware_management_api.repository.SupplierBillItemRepository;
import lk.oracene.hardware_management_api.repository.SupplierBillRepository;
import lk.oracene.hardware_management_api.repository.SupplierRepository;
import lk.oracene.hardware_management_api.service.SupplierBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierBillServiceImpl implements SupplierBillService {

    private final SupplierBillRepository supplierBillRepository;
    private final SupplierBillItemRepository supplierBillItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Override
    public SupplierBillResponse createBill(SupplierBillRequest request) {
        Supplier supplier = supplierRepository.findBySupplierIdAndIsActiveTrue(request.getSupplierId())
                .orElseThrow(() -> new NotFoundException(
                        "Active supplier not found with id: " + request.getSupplierId()));

        if (supplierBillRepository.existsByBillNumber(request.getBillNumber())) {
            throw new BadRequestException(
                    "Bill number already exists: " + request.getBillNumber());
        }

        SupplierBill bill = new SupplierBill();
        bill.setSupplier(supplier);
        bill.setBillNumber(request.getBillNumber());
        bill.setBillDate(request.getBillDate());
        bill.setDueDate(request.getDueDate());
        bill.setNotes(request.getNotes());
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setStatus(SupplierBillStatus.UNPAID);

        BigDecimal total = BigDecimal.ZERO;
        for (SupplierBillItemRequest itemReq : request.getItems()) {
            total = total.add(
                    resolveUnitCostPrice(itemReq).multiply(BigDecimal.valueOf(itemReq.getQuantity()))
            );
        }
        bill.setTotalAmount(total);

        SupplierBill savedBill = supplierBillRepository.save(bill);

        List<SupplierBillItem> savedItems = request.getItems().stream().map(itemReq -> {
            BigDecimal unitCostPrice = resolveUnitCostPrice(itemReq);

            Product product = productRepository.findByProductIdAndIsActiveTrue(itemReq.getProductId())
                    .orElseThrow(() -> new NotFoundException(
                            "Active product not found with id: " + itemReq.getProductId()));

            product.setSupplier(supplier);
            product.setStockQuantity(
                    product.getStockQuantity().add(BigDecimal.valueOf(itemReq.getQuantity()))
            );
            if (itemReq.getIsReturn() != null) product.setIsReturn(itemReq.getIsReturn());
            product.setCostPrice(unitCostPrice);
            if (itemReq.getUnitPrice() != null) product.setUnitPrice(itemReq.getUnitPrice());
            if (itemReq.getDiscount() != null) product.setDiscount(itemReq.getDiscount());
            if (itemReq.getSupplierDiscount() != null) product.setSupplierDiscount(itemReq.getSupplierDiscount());
            productRepository.save(product);

            SupplierBillItem item = new SupplierBillItem();
            item.setSupplierBill(savedBill);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitCostPrice(unitCostPrice);
            return supplierBillItemRepository.save(item);
        }).toList();

        return mapToResponse(savedBill, savedItems);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierBillResponse getBillById(Long supplierBillId) {
        SupplierBill bill = supplierBillRepository.findById(supplierBillId)
                .orElseThrow(() -> new NotFoundException(
                        "Supplier bill not found with id: " + supplierBillId));

        List<SupplierBillItem> items =
                supplierBillItemRepository.findBySupplierBill_SupplierBillId(supplierBillId);

        return mapToResponse(bill, items);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierBillSummaryResponse> getAllBills(Pageable pageable) {
        return supplierBillRepository.findAll(pageable)
                .map(bill -> SupplierBillSummaryResponse.builder()
                        .supplierBillId(bill.getSupplierBillId())
                        .supplierId(bill.getSupplier().getSupplierId())
                        .supplierName(bill.getSupplier().getName())
                        .billNumber(bill.getBillNumber())
                        .billDate(bill.getBillDate())
                        .dueDate(bill.getDueDate())
                        .totalAmount(bill.getTotalAmount())
                        .paidAmount(bill.getPaidAmount())
                        .remainingAmount(bill.getTotalAmount().subtract(bill.getPaidAmount()))
                        .status(bill.getStatus())
                        .notes(bill.getNotes())
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierBillItemResponse> getItemsByBill(Long supplierBillId, Pageable pageable) {
        if (!supplierBillRepository.existsById(supplierBillId)) {
            throw new NotFoundException("Supplier bill not found with id: " + supplierBillId);
        }
        return supplierBillItemRepository.findBySupplierBill_SupplierBillId(supplierBillId, pageable)
                .map(this::mapItemToResponse);
    }

    @Override
    public SupplierBillItemResponse updateItem(Long supplierBillItemId, SupplierBillItemUpdateRequest request) {
        SupplierBillItem item = supplierBillItemRepository.findById(supplierBillItemId)
                .orElseThrow(() -> new NotFoundException(
                        "Supplier bill item not found with id: " + supplierBillItemId));

        SupplierBill bill = item.getSupplierBill();
        if (bill.getStatus() == SupplierBillStatus.CANCELLED) {
            throw new BadRequestException("Cannot update items of a cancelled bill");
        }

        Product product = item.getProduct();
        BigDecimal oldLineTotal = item.getUnitCostPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        if (request.getQuantity() != null) {
            int qtyDiff = request.getQuantity() - item.getQuantity();
            product.setStockQuantity(product.getStockQuantity().add(BigDecimal.valueOf(qtyDiff)));
            item.setQuantity(request.getQuantity());
        }

        boolean hasCostPriceInput = request.getUnitCostPrice() != null
                || (request.getUnitPrice() != null && request.getSupplierDiscount() != null);

        if (hasCostPriceInput) {
            BigDecimal newUnitCostPrice = resolveUnitCostPriceFromUpdate(request);
            product.setCostPrice(newUnitCostPrice);
            item.setUnitCostPrice(newUnitCostPrice);
        }

        if (request.getIsReturn() != null) product.setIsReturn(request.getIsReturn());
        if (request.getUnitPrice() != null) product.setUnitPrice(request.getUnitPrice());
        if (request.getDiscount() != null) product.setDiscount(request.getDiscount());
        if (request.getSupplierDiscount() != null) product.setSupplierDiscount(request.getSupplierDiscount());

        productRepository.save(product);

        BigDecimal newLineTotal = item.getUnitCostPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        bill.setTotalAmount(bill.getTotalAmount().subtract(oldLineTotal).add(newLineTotal));
        supplierBillRepository.save(bill);

        return mapItemToResponse(supplierBillItemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierBillResponse> getBillsBySupplier(Long supplierId, Pageable pageable) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new NotFoundException("Supplier not found with id: " + supplierId);
        }

        return supplierBillRepository.findBySupplier_SupplierId(supplierId, pageable)
                .map(bill -> {
                    List<SupplierBillItem> items =
                            supplierBillItemRepository.findBySupplierBill_SupplierBillId(bill.getSupplierBillId());
                    return mapToResponse(bill, items);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierBillResponse> getUnpaidBillsBySupplier(Long supplierId, Pageable pageable) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new NotFoundException("Supplier not found with id: " + supplierId);
        }

        return supplierBillRepository.findBySupplier_SupplierIdAndStatusIn(
                        supplierId, List.of(SupplierBillStatus.UNPAID, SupplierBillStatus.PARTIALLY_PAID), pageable)
                .map(bill -> {
                    List<SupplierBillItem> items =
                            supplierBillItemRepository.findBySupplierBill_SupplierBillId(bill.getSupplierBillId());
                    return mapToResponse(bill, items);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public OutstandingResponse getOutstandingBySupplier(Long supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new NotFoundException("Supplier not found with id: " + supplierId);
        }
        BigDecimal totalAmount = supplierBillRepository.sumTotalAmountBySupplierId(supplierId);
        BigDecimal paidAmount = supplierBillRepository.sumPaidAmountBySupplierId(supplierId);
        return OutstandingResponse.builder()
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .outstandingAmount(totalAmount.subtract(paidAmount))
                .build();
    }

    @Override
    public void cancelBill(Long supplierBillId) {
        SupplierBill bill = supplierBillRepository.findById(supplierBillId)
                .orElseThrow(() -> new NotFoundException(
                        "Supplier bill not found with id: " + supplierBillId));

        if (bill.getStatus() == SupplierBillStatus.PAID) {
            throw new BadRequestException("Cannot cancel a fully paid bill");
        }
        if (bill.getStatus() == SupplierBillStatus.CANCELLED) {
            throw new BadRequestException("Bill is already cancelled");
        }

        List<SupplierBillItem> items =
                supplierBillItemRepository.findBySupplierBill_SupplierBillId(supplierBillId);

        items.forEach(item -> {
            Product product = item.getProduct();
            product.setStockQuantity(
                    product.getStockQuantity().subtract(BigDecimal.valueOf(item.getQuantity()))
            );
            productRepository.save(product);
        });

        bill.setStatus(SupplierBillStatus.CANCELLED);
        supplierBillRepository.save(bill);
    }

    private SupplierBillResponse mapToResponse(SupplierBill bill, List<SupplierBillItem> items) {
        List<SupplierBillItemResponse> itemResponses = items.stream()
                .map(this::mapItemToResponse)
                .toList();

        BigDecimal remaining = bill.getTotalAmount().subtract(bill.getPaidAmount());
        BigDecimal totalReturnAmount = bill.getTotalReturnAmount() != null ? bill.getTotalReturnAmount() : BigDecimal.ZERO;

        return SupplierBillResponse.builder()
                .supplierBillId(bill.getSupplierBillId())
                .supplierId(bill.getSupplier().getSupplierId())
                .supplierName(bill.getSupplier().getName())
                .billNumber(bill.getBillNumber())
                .billDate(bill.getBillDate())
                .dueDate(bill.getDueDate())
                .totalAmount(bill.getTotalAmount())
                .paidAmount(bill.getPaidAmount())
                .remainingAmount(remaining)
                .totalReturnAmount(totalReturnAmount)
                .status(bill.getStatus())
                .notes(bill.getNotes())
                .items(itemResponses)
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .createdBy(bill.getCreatedBy())
                .updatedBy(bill.getUpdatedBy())
                .build();
    }

    private SupplierBillItemResponse mapItemToResponse(SupplierBillItem item) {
        return SupplierBillItemResponse.builder()
                .supplierBillItemId(item.getSupplierBillItemId())
                .productId(item.getProduct().getProductId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .quantity(item.getQuantity())
                .unitCostPrice(item.getUnitCostPrice())
                .lineTotal(item.getUnitCostPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .createdBy(item.getCreatedBy())
                .updatedBy(item.getUpdatedBy())
                .build();
    }

    private BigDecimal resolveUnitCostPrice(SupplierBillItemRequest itemReq) {
        if (itemReq.getUnitCostPrice() != null) {
            return itemReq.getUnitCostPrice();
        }
        if (itemReq.getUnitPrice() != null && itemReq.getSupplierDiscount() != null) {
            return itemReq.getUnitPrice()
                    .multiply(BigDecimal.ONE.subtract(
                            itemReq.getSupplierDiscount().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP)
                    ))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        throw new BadRequestException(
                "Either unitCostPrice or both unitPrice and supplierDiscount must be provided for product id: "
                + itemReq.getProductId());
    }

    private BigDecimal resolveUnitCostPriceFromUpdate(SupplierBillItemUpdateRequest req) {
        if (req.getUnitCostPrice() != null) {
            return req.getUnitCostPrice();
        }
        return req.getUnitPrice()
                .multiply(BigDecimal.ONE.subtract(
                        req.getSupplierDiscount().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP)
                ))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}

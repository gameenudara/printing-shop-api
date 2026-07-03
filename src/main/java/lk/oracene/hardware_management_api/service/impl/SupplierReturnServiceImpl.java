package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.SupplierReturnItemRequest;
import lk.oracene.hardware_management_api.dto.request.SupplierReturnRequest;
import lk.oracene.hardware_management_api.dto.response.SupplierBillSummaryResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierReturnItemResponse;
import lk.oracene.hardware_management_api.dto.response.SupplierReturnResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Product;
import lk.oracene.hardware_management_api.model.Supplier;
import lk.oracene.hardware_management_api.model.SupplierBill;
import lk.oracene.hardware_management_api.model.SupplierBillStatus;
import lk.oracene.hardware_management_api.model.SupplierReturn;
import lk.oracene.hardware_management_api.model.SupplierReturnItem;
import lk.oracene.hardware_management_api.repository.ProductRepository;
import lk.oracene.hardware_management_api.repository.SupplierBillRepository;
import lk.oracene.hardware_management_api.repository.SupplierRepository;
import lk.oracene.hardware_management_api.repository.SupplierReturnItemRepository;
import lk.oracene.hardware_management_api.repository.SupplierReturnRepository;
import lk.oracene.hardware_management_api.service.SupplierReturnService;
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
public class SupplierReturnServiceImpl implements SupplierReturnService {

    private final SupplierReturnRepository supplierReturnRepository;
    private final SupplierReturnItemRepository supplierReturnItemRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierBillRepository supplierBillRepository;
    private final ProductRepository productRepository;

    @Override
    public SupplierReturnResponse create(SupplierReturnRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + request.getSupplierId()));

        SupplierBill bill = null;
        if (request.getSupplierBillId() != null) {
            bill = supplierBillRepository.findById(request.getSupplierBillId())
                    .orElseThrow(() -> new NotFoundException("Supplier bill not found with id: " + request.getSupplierBillId()));

            if (!bill.getSupplier().getSupplierId().equals(request.getSupplierId())) {
                throw new BadRequestException("Bill " + request.getSupplierBillId() + " does not belong to supplier " + request.getSupplierId());
            }
            if (bill.getStatus() == SupplierBillStatus.CANCELLED) {
                throw new BadRequestException("Cannot create a return against a cancelled bill");
            }
        }

        SupplierReturn supplierReturn = new SupplierReturn();
        supplierReturn.setSupplier(supplier);
        supplierReturn.setSupplierBill(bill);
        supplierReturn.setReturnDate(LocalDateTime.now());
        supplierReturn.setNote(request.getNote());
        SupplierReturn savedReturn = supplierReturnRepository.save(supplierReturn);

        BigDecimal totalReturnAmount = BigDecimal.ZERO;

        for (SupplierReturnItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found with id: " + itemReq.getProductId()));

            BigDecimal unitCostPrice = itemReq.getUnitCostPrice() != null
                    ? itemReq.getUnitCostPrice()
                    : product.getCostPrice();

            SupplierReturnItem item = new SupplierReturnItem();
            item.setSupplierReturn(savedReturn);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitCostPrice(unitCostPrice);
            supplierReturnItemRepository.save(item);

            totalReturnAmount = totalReturnAmount.add(unitCostPrice.multiply(itemReq.getQuantity()));
        }

        savedReturn.setTotalReturnAmount(totalReturnAmount);
        supplierReturnRepository.save(savedReturn);

        if (bill != null) {
            BigDecimal newTotalReturn = (bill.getTotalReturnAmount() != null ? bill.getTotalReturnAmount() : BigDecimal.ZERO)
                    .add(totalReturnAmount);
            bill.setTotalReturnAmount(newTotalReturn);

            if (newTotalReturn.compareTo(bill.getTotalAmount()) >= 0) {
                bill.setStatus(SupplierBillStatus.REFUNDED);
            } else {
                bill.setStatus(SupplierBillStatus.PARTIAL_REFUND);
            }
            supplierBillRepository.save(bill);
        }

        List<SupplierReturnItem> savedItems = supplierReturnItemRepository.findBySupplierReturn_SupplierReturnId(savedReturn.getSupplierReturnId());
        return buildResponse(savedReturn, savedItems, bill);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierReturnResponse getById(Long supplierReturnId) {
        SupplierReturn supplierReturn = supplierReturnRepository.findById(supplierReturnId)
                .orElseThrow(() -> new NotFoundException("Supplier return not found with id: " + supplierReturnId));
        List<SupplierReturnItem> items = supplierReturnItemRepository.findBySupplierReturn_SupplierReturnId(supplierReturnId);
        return buildResponse(supplierReturn, items, supplierReturn.getSupplierBill());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierReturnResponse> getByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return supplierReturnRepository.findByReturnDateBetween(from, to, pageable)
                .map(sr -> {
                    List<SupplierReturnItem> items = supplierReturnItemRepository.findBySupplierReturn_SupplierReturnId(sr.getSupplierReturnId());
                    return buildResponse(sr, items, sr.getSupplierBill());
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierReturnResponse> getAll(Pageable pageable) {
        return supplierReturnRepository.findAll(pageable).map(sr -> {
            List<SupplierReturnItem> items = supplierReturnItemRepository.findBySupplierReturn_SupplierReturnId(sr.getSupplierReturnId());
            return buildResponse(sr, items, sr.getSupplierBill());
        });
    }

    private SupplierReturnResponse buildResponse(SupplierReturn supplierReturn, List<SupplierReturnItem> items, SupplierBill bill) {
        List<SupplierReturnItemResponse> itemResponses = items.stream()
                .map(item -> {
                    BigDecimal lineTotal = item.getUnitCostPrice() != null
                            ? item.getUnitCostPrice().multiply(item.getQuantity())
                            : null;
                    return SupplierReturnItemResponse.builder()
                            .supplierReturnItemId(item.getSupplierReturnItemId())
                            .productId(item.getProduct().getProductId())
                            .productName(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .unitCostPrice(item.getUnitCostPrice())
                            .lineTotal(lineTotal)
                            .build();
                })
                .toList();

        SupplierBillSummaryResponse billSummary = null;
        if (bill != null) {
            BigDecimal totalReturnAmount = bill.getTotalReturnAmount() != null ? bill.getTotalReturnAmount() : BigDecimal.ZERO;
            billSummary = SupplierBillSummaryResponse.builder()
                    .supplierBillId(bill.getSupplierBillId())
                    .supplierId(bill.getSupplier().getSupplierId())
                    .supplierName(bill.getSupplier().getName())
                    .billNumber(bill.getBillNumber())
                    .billDate(bill.getBillDate())
                    .dueDate(bill.getDueDate())
                    .totalAmount(bill.getTotalAmount())
                    .paidAmount(bill.getPaidAmount())
                    .remainingAmount(bill.getTotalAmount().subtract(bill.getPaidAmount()))
                    .totalReturnAmount(totalReturnAmount)
                    .status(bill.getStatus())
                    .notes(bill.getNotes())
                    .build();
        }

        return SupplierReturnResponse.builder()
                .supplierReturnId(supplierReturn.getSupplierReturnId())
                .supplierId(supplierReturn.getSupplier().getSupplierId())
                .supplierName(supplierReturn.getSupplier().getName())
                .returnDate(supplierReturn.getReturnDate())
                .totalReturnAmount(supplierReturn.getTotalReturnAmount())
                .note(supplierReturn.getNote())
                .bill(billSummary)
                .items(itemResponses)
                .createdAt(supplierReturn.getCreatedAt())
                .createdBy(supplierReturn.getCreatedBy())
                .build();
    }
}

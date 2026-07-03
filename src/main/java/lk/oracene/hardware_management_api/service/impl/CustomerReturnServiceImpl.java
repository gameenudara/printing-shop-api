package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.CustomerReturnItemRequest;
import lk.oracene.hardware_management_api.dto.request.CustomerReturnRequest;
import lk.oracene.hardware_management_api.dto.response.CustomerReturnItemResponse;
import lk.oracene.hardware_management_api.dto.response.CustomerReturnResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.CustomerReturn;
import lk.oracene.hardware_management_api.model.CustomerReturnItem;
import lk.oracene.hardware_management_api.model.Product;
import lk.oracene.hardware_management_api.model.ReturnCondition;
import lk.oracene.hardware_management_api.model.Sales;
import lk.oracene.hardware_management_api.model.SalesItem;
import lk.oracene.hardware_management_api.model.SalesStatus;
import lk.oracene.hardware_management_api.repository.CustomerReturnItemRepository;
import lk.oracene.hardware_management_api.repository.CustomerReturnRepository;
import lk.oracene.hardware_management_api.repository.ProductRepository;
import lk.oracene.hardware_management_api.repository.SalesItemRepository;
import lk.oracene.hardware_management_api.repository.SalesRepository;
import lk.oracene.hardware_management_api.service.CustomerReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerReturnServiceImpl implements CustomerReturnService {

    private final CustomerReturnRepository customerReturnRepository;
    private final CustomerReturnItemRepository customerReturnItemRepository;
    private final SalesRepository salesRepository;
    private final SalesItemRepository salesItemRepository;
    private final ProductRepository productRepository;

    @Override
    public CustomerReturnResponse create(CustomerReturnRequest request) {
        Sales sale = salesRepository.findByInvoiceNumber(request.getInvoiceNumber())
                .orElseThrow(() -> new NotFoundException("Sale not found with invoice number: " + request.getInvoiceNumber()));

        if (sale.getStatus() == SalesStatus.CANCELLED) {
            throw new BadRequestException("Cannot process return for a cancelled sale");
        }
        if (sale.getStatus() == SalesStatus.REFUNDED) {
            throw new BadRequestException("Cannot process return for a fully refunded sale");
        }

        CustomerReturn customerReturn = new CustomerReturn();
        customerReturn.setSale(sale);
        customerReturn.setReturnDate(LocalDateTime.now());
        customerReturn.setNote(request.getNote());
        CustomerReturn savedReturn = customerReturnRepository.save(customerReturn);

        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        List<CustomerReturnItem> savedItems = new ArrayList<>();

        for (CustomerReturnItemRequest itemReq : request.getItems()) {
            SalesItem saleItem = salesItemRepository.findById(itemReq.getSaleItemId())
                    .orElseThrow(() -> new NotFoundException("Sale item not found with id: " + itemReq.getSaleItemId()));

            if (!saleItem.getSale().getSalesId().equals(sale.getSalesId())) {
                throw new BadRequestException("Sale item " + itemReq.getSaleItemId() + " does not belong to sale " + sale.getSalesId());
            }

            BigDecimal alreadyReturned = customerReturnItemRepository.sumReturnedQuantityBySaleItemId(itemReq.getSaleItemId());
            if (alreadyReturned.add(itemReq.getQuantity()).compareTo(saleItem.getQuantity()) > 0) {
                throw new BadRequestException("Return quantity exceeds returnable quantity for sale item " + itemReq.getSaleItemId());
            }

            BigDecimal refundAmount = saleItem.getLineTotal()
                    .divide(saleItem.getQuantity(), 4, RoundingMode.HALF_UP)
                    .multiply(itemReq.getQuantity())
                    .setScale(2, RoundingMode.HALF_UP);

            Product product = saleItem.getProduct();

            if (itemReq.getCondition() == ReturnCondition.GOOD) {
                product.setStockQuantity(product.getStockQuantity().add(itemReq.getQuantity()));
                productRepository.save(product);
            }

            CustomerReturnItem returnItem = new CustomerReturnItem();
            returnItem.setCustomerReturn(savedReturn);
            returnItem.setProduct(product);
            returnItem.setSaleItem(saleItem);
            returnItem.setQuantity(itemReq.getQuantity());
            returnItem.setCondition(itemReq.getCondition());
            returnItem.setRefundAmount(refundAmount);
            CustomerReturnItem savedItem = customerReturnItemRepository.save(returnItem);

            savedItems.add(savedItem);
            totalRefundAmount = totalRefundAmount.add(refundAmount);
        }

        updateSaleStatus(sale);
        salesRepository.save(sale);

        return buildResponse(savedReturn, savedItems);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerReturnResponse getById(Long returnId) {
        CustomerReturn customerReturn = customerReturnRepository.findById(returnId)
                .orElseThrow(() -> new NotFoundException("Customer return not found with id: " + returnId));
        List<CustomerReturnItem> items = customerReturnItemRepository.findByCustomerReturn_ReturnId(returnId);
        return buildResponse(customerReturn, items);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerReturnResponse> getAll(Pageable pageable) {
        return customerReturnRepository.findAll(pageable).map(cr -> {
            List<CustomerReturnItem> items = customerReturnItemRepository.findByCustomerReturn_ReturnId(cr.getReturnId());
            return buildResponse(cr, items);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerReturnResponse> getByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return customerReturnRepository.findByReturnDateBetween(from, to, pageable)
                .map(cr -> {
                    List<CustomerReturnItem> items = customerReturnItemRepository.findByCustomerReturn_ReturnId(cr.getReturnId());
                    return buildResponse(cr, items);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerReturnResponse> getBySaleId(Long saleId) {
        if (!salesRepository.existsById(saleId)) {
            throw new NotFoundException("Sale not found with id: " + saleId);
        }
        return customerReturnRepository.findBySale_SalesId(saleId).stream()
                .map(cr -> {
                    List<CustomerReturnItem> items = customerReturnItemRepository.findByCustomerReturn_ReturnId(cr.getReturnId());
                    return buildResponse(cr, items);
                })
                .toList();
    }

    private void updateSaleStatus(Sales sale) {
        BigDecimal totalOriginalQty = salesItemRepository.findBySale_SalesId(sale.getSalesId())
                .stream()
                .map(SalesItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReturnedQty = customerReturnRepository.findBySale_SalesId(sale.getSalesId())
                .stream()
                .flatMap(cr -> customerReturnItemRepository.findByCustomerReturn_ReturnId(cr.getReturnId()).stream())
                .map(CustomerReturnItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalReturnedQty.compareTo(totalOriginalQty) >= 0) {
            sale.setStatus(SalesStatus.REFUNDED);
        } else {
            sale.setStatus(SalesStatus.PARTIAL_REFUND);
        }
    }

    private CustomerReturnResponse buildResponse(CustomerReturn customerReturn, List<CustomerReturnItem> items) {
        List<CustomerReturnItemResponse> itemResponses = items.stream()
                .map(item -> CustomerReturnItemResponse.builder()
                        .returnItemId(item.getReturnItemId())
                        .productId(item.getProduct().getProductId())
                        .productName(item.getProduct().getName())
                        .saleItemId(item.getSaleItem().getSaleItemId())
                        .quantity(item.getQuantity())
                        .condition(item.getCondition())
                        .refundAmount(item.getRefundAmount())
                        .build())
                .toList();

        BigDecimal totalRefund = items.stream()
                .map(CustomerReturnItem::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CustomerReturnResponse.builder()
                .returnId(customerReturn.getReturnId())
                .saleId(customerReturn.getSale().getSalesId())
                .invoiceNumber(customerReturn.getSale().getInvoiceNumber())
                .returnDate(customerReturn.getReturnDate())
                .note(customerReturn.getNote())
                .totalRefundAmount(totalRefund)
                .items(itemResponses)
                .createdAt(customerReturn.getCreatedAt())
                .createdBy(customerReturn.getCreatedBy())
                .build();
    }
}

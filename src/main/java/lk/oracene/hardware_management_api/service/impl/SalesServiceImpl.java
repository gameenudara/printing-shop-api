package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.DraftConfirmRequest;
import lk.oracene.hardware_management_api.dto.request.SalesItemRequest;
import lk.oracene.hardware_management_api.dto.request.SalesRequest;
import lk.oracene.hardware_management_api.dto.response.PaymentResponse;
import lk.oracene.hardware_management_api.dto.response.SalesItemResponse;
import lk.oracene.hardware_management_api.dto.response.SalesResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Customer;
import lk.oracene.hardware_management_api.model.Payment;
import lk.oracene.hardware_management_api.model.User;
import lk.oracene.hardware_management_api.model.PaymentStatus;
import lk.oracene.hardware_management_api.model.Product;
import lk.oracene.hardware_management_api.model.Sales;
import lk.oracene.hardware_management_api.model.SalesItem;
import lk.oracene.hardware_management_api.model.SalesStatus;
import lk.oracene.hardware_management_api.repository.CustomerRepository;
import lk.oracene.hardware_management_api.repository.PaymentRepository;
import lk.oracene.hardware_management_api.repository.ProductRepository;
import lk.oracene.hardware_management_api.repository.SalesItemRepository;
import lk.oracene.hardware_management_api.repository.SalesRepository;
import lk.oracene.hardware_management_api.repository.UserRepository;
import lk.oracene.hardware_management_api.service.PrintService;
import lk.oracene.hardware_management_api.service.SalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SalesServiceImpl implements SalesService {

    private final SalesRepository salesRepository;
    private final SalesItemRepository salesItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PrintService printService;

    @Override
    public SalesResponse createSale(SalesRequest request) {
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                    .orElseThrow(() -> new NotFoundException(
                            "Active customer not found with id: " + request.getCustomerId()));
        }

        Sales sale = new Sales();
        sale.setCustomer(customer);
        sale.setSaleDate(LocalDateTime.now());
        sale.setDiscountAmount(request.getDiscountAmount());
        sale.setStatus(SalesStatus.UNPAID);
        sale.setCashier(getLoggedInCashierName());

        Sales savedSale = salesRepository.save(sale);
        String invoiceNumber = "INV" + String.format("%06d", savedSale.getSalesId());
        savedSale.setInvoiceNumber(invoiceNumber);
        salesRepository.save(savedSale);

        BigDecimal subTotal = processItems(request.getItems(), savedSale);
        BigDecimal totalAmount = subTotal
                .subtract(request.getDiscountAmount())
                .setScale(2, RoundingMode.HALF_UP);

        savedSale.setSubTotal(subTotal);
        savedSale.setTotalAmount(totalAmount);
        salesRepository.save(savedSale);

        if (request.getReceivedAmount() != null && request.getPaymentMethod() != null) {
            BigDecimal received = request.getReceivedAmount();
            BigDecimal paidAmount = received.min(totalAmount);

            Payment payment = new Payment();
            payment.setSale(savedSale);
            payment.setPaidAmount(paidAmount);
            payment.setReceivedAmount(received);
            payment.setPaidAt(LocalDateTime.now());
            payment.setMethod(request.getPaymentMethod());
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setReferenceNo(request.getPaymentReferenceNo());
            paymentRepository.save(payment);

            if (paidAmount.compareTo(totalAmount) >= 0) {
                savedSale.setStatus(SalesStatus.PAID);
            } else {
                savedSale.setStatus(SalesStatus.ADVANCE_PAID);
            }
            salesRepository.save(savedSale);
        }

        SalesResponse response = buildResponse(savedSale);
        printReceiptSilently(response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesResponse getSaleById(Long saleId) {
        Sales sale = salesRepository.findById(saleId)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + saleId));
        return buildResponse(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesResponse getSaleByInvoiceNumber(String invoiceNumber) {
        Sales sale = salesRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new NotFoundException("Sale not found with invoice number: " + invoiceNumber));
        return buildResponse(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalesResponse> getAllSales(Pageable pageable) {
        return buildPageResponse(salesRepository.findByStatusNot(SalesStatus.DRAFT, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalesResponse> searchSalesByInvoiceNumber(String invoiceNumber, Pageable pageable) {
        return buildPageResponse(salesRepository.findByInvoiceNumberContainingIgnoreCase(invoiceNumber, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalesResponse> getSalesByCustomer(Long customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer not found with id: " + customerId);
        }
        return buildPageResponse(salesRepository.findByCustomer_CustomerId(customerId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalesResponse> getPendingSalesByCustomer(Long customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer not found with id: " + customerId);
        }
        return salesRepository.findByCustomer_CustomerIdAndStatusIn(
                customerId, List.of(SalesStatus.UNPAID, SalesStatus.ADVANCE_PAID), pageable)
                .map(this::buildSummaryResponse);
    }

    @Override
    public void cancelSale(Long saleId) {
        Sales sale = salesRepository.findById(saleId)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + saleId));

        if (sale.getStatus() == SalesStatus.CANCELLED) {
            throw new BadRequestException("Sale is already cancelled");
        }

        sale.setStatus(SalesStatus.CANCELLED);
        salesRepository.save(sale);
    }

    @Override
    public SalesResponse createDraft(SalesRequest request) {
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                    .orElseThrow(() -> new NotFoundException(
                            "Active customer not found with id: " + request.getCustomerId()));
        }

        Sales sale = new Sales();
        sale.setCustomer(customer);
        sale.setSaleDate(LocalDateTime.now());
        sale.setDiscountAmount(request.getDiscountAmount());
        sale.setStatus(SalesStatus.DRAFT);
        sale.setCashier(getLoggedInCashierName());

        Sales savedSale = salesRepository.save(sale);
        savedSale.setInvoiceNumber("DFT" + String.format("%06d", savedSale.getSalesId()));
        salesRepository.save(savedSale);

        BigDecimal subTotal = processItems(request.getItems(), savedSale);

        BigDecimal totalAmount = subTotal
                .subtract(request.getDiscountAmount())
                .setScale(2, RoundingMode.HALF_UP);

        savedSale.setSubTotal(subTotal);
        savedSale.setTotalAmount(totalAmount);
        salesRepository.save(savedSale);

        return buildResponse(savedSale);
    }

    @Override
    public SalesResponse updateDraft(Long saleId, SalesRequest request) {
        Sales sale = salesRepository.findById(saleId)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + saleId));

        if (sale.getStatus() != SalesStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT sales can be updated");
        }

        salesItemRepository.deleteBySale_SalesId(saleId);

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                    .orElseThrow(() -> new NotFoundException(
                            "Active customer not found with id: " + request.getCustomerId()));
        }

        sale.setCustomer(customer);
        sale.setDiscountAmount(request.getDiscountAmount());

        BigDecimal subTotal = processItems(request.getItems(), sale);

        BigDecimal totalAmount = subTotal
                .subtract(request.getDiscountAmount())
                .setScale(2, RoundingMode.HALF_UP);

        sale.setSubTotal(subTotal);
        sale.setTotalAmount(totalAmount);
        salesRepository.save(sale);

        return buildResponse(sale);
    }

    @Override
    public SalesResponse confirmDraft(Long saleId, DraftConfirmRequest request) {
        Sales sale = salesRepository.findById(saleId)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + saleId));

        if (sale.getStatus() != SalesStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT sales can be confirmed");
        }

        String invoiceNumber = "INV" + String.format("%06d", sale.getSalesId());
        sale.setInvoiceNumber(invoiceNumber);
        sale.setStatus(SalesStatus.UNPAID);

        if (request.getReceivedAmount() != null && request.getPaymentMethod() != null) {
            BigDecimal received = request.getReceivedAmount();
            BigDecimal paidAmount = received.min(sale.getTotalAmount());

            Payment payment = new Payment();
            payment.setSale(sale);
            payment.setPaidAmount(paidAmount);
            payment.setReceivedAmount(received);
            payment.setPaidAt(LocalDateTime.now());
            payment.setMethod(request.getPaymentMethod());
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setReferenceNo(request.getPaymentReferenceNo());
            paymentRepository.save(payment);

            if (paidAmount.compareTo(sale.getTotalAmount()) >= 0) {
                sale.setStatus(SalesStatus.PAID);
            } else {
                sale.setStatus(SalesStatus.ADVANCE_PAID);
            }
        }

        salesRepository.save(sale);
        SalesResponse response = buildResponse(sale);
        printReceiptSilently(response);
        return response;
    }

    @Override
    public void deleteDraft(Long saleId) {
        Sales sale = salesRepository.findById(saleId)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + saleId));

        if (sale.getStatus() != SalesStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT sales can be deleted");
        }

        salesItemRepository.deleteBySale_SalesId(saleId);
        salesRepository.delete(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalesResponse> getDrafts(Pageable pageable) {
        return buildPageResponse(salesRepository.findByStatus(SalesStatus.DRAFT, pageable));
    }

    private BigDecimal processItems(List<SalesItemRequest> itemRequests, Sales sale) {
        BigDecimal subTotal = BigDecimal.ZERO;
        for (SalesItemRequest itemReq : itemRequests) {
            Product product = productRepository.findByProductIdAndIsActiveTrue(itemReq.getProductId())
                    .orElseThrow(() -> new NotFoundException(
                            "Active product not found with id: " + itemReq.getProductId()));

            BigDecimal unitPrice = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : product.getUnitPrice();
            BigDecimal discountPct = itemReq.getDiscountPct() != null ? itemReq.getDiscountPct()
                    : (product.getDiscount() != null ? product.getDiscount() : BigDecimal.ZERO);
            BigDecimal flatDiscount = itemReq.getFlatDiscount() != null ? itemReq.getFlatDiscount() : BigDecimal.ZERO;

            BigDecimal priceAfterPct = unitPrice
                    .multiply(BigDecimal.ONE.subtract(discountPct.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
            BigDecimal priceAfterFlat = priceAfterPct.subtract(flatDiscount);
            if (priceAfterFlat.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException(
                        "Flat discount exceeds unit price for product '" + product.getName() + "'");
            }
            BigDecimal lineTotal = priceAfterFlat.multiply(itemReq.getQuantity()).setScale(2, RoundingMode.HALF_UP);

            SalesItem item = new SalesItem();
            item.setSale(sale);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setDiscountPct(discountPct);
            item.setFlatDiscount(flatDiscount);
            item.setLineTotal(lineTotal);
            salesItemRepository.save(item);

            subTotal = subTotal.add(lineTotal);
        }
        return subTotal.setScale(2, RoundingMode.HALF_UP);
    }

    private Page<SalesResponse> buildPageResponse(Page<Sales> salesPage) {
        List<Long> ids = salesPage.getContent().stream().map(Sales::getSalesId).toList();
        if (ids.isEmpty()) return salesPage.map(this::buildResponse);

        Map<Long, List<SalesItem>> itemsById = salesItemRepository.findBySale_SalesIdIn(ids)
                .stream().collect(Collectors.groupingBy(si -> si.getSale().getSalesId()));

        Map<Long, List<Payment>> paymentsById = paymentRepository.findBySale_SalesIdIn(ids)
                .stream().collect(Collectors.groupingBy(p -> p.getSale().getSalesId()));

        return salesPage.map(sale -> buildResponse(
                sale,
                itemsById.getOrDefault(sale.getSalesId(), List.of()),
                paymentsById.getOrDefault(sale.getSalesId(), List.of())));
    }

    private SalesResponse buildResponse(Sales sale, List<SalesItem> items, List<Payment> payments) {
        List<SalesItemResponse> itemResponses = items.stream()
                .map(item -> SalesItemResponse.builder()
                        .saleItemId(item.getSaleItemId())
                        .productId(item.getProduct().getProductId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discountPct(item.getDiscountPct())
                        .flatDiscount(item.getFlatDiscount())
                        .lineTotal(item.getLineTotal())
                        .createdAt(item.getCreatedAt())
                        .updatedAt(item.getUpdatedAt())
                        .createdBy(item.getCreatedBy())
                        .updatedBy(item.getUpdatedBy())
                        .build())
                .toList();

        List<PaymentResponse> paymentResponses = payments.stream()
                .map(this::mapToPaymentResponse)
                .toList();

        BigDecimal totalPaid = paymentResponses.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(PaymentResponse::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceived = paymentResponses.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(p -> p.getReceivedAmount() != null ? p.getReceivedAmount() : p.getPaidAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = sale.getTotalAmount() != null
                ? sale.getTotalAmount().subtract(totalPaid).max(BigDecimal.ZERO)
                : BigDecimal.ZERO;

        BigDecimal change = totalReceived.subtract(
                sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO).max(BigDecimal.ZERO);

        return SalesResponse.builder()
                .salesId(sale.getSalesId())
                .invoiceNumber(sale.getInvoiceNumber())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getCustomerId() : null)
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getCustomerName() : null)
                .saleDate(sale.getSaleDate())
                .subTotal(sale.getSubTotal())
                .discountAmount(sale.getDiscountAmount())
                .totalAmount(sale.getTotalAmount())
                .paidAmount(totalPaid)
                .receivedAmount(totalReceived)
                .changeAmount(change)
                .remainingAmount(remaining)
                .status(sale.getStatus())
                .cashier(sale.getCashier())
                .items(itemResponses)
                .payments(paymentResponses)
                .createdAt(sale.getCreatedAt())
                .updatedAt(sale.getUpdatedAt())
                .createdBy(sale.getCreatedBy())
                .updatedBy(sale.getUpdatedBy())
                .build();
    }

    private SalesResponse buildResponse(Sales sale) {
        List<SalesItemResponse> itemResponses = salesItemRepository.findBySale_SalesId(sale.getSalesId())
                .stream()
                .map(item -> SalesItemResponse.builder()
                        .saleItemId(item.getSaleItemId())
                        .productId(item.getProduct().getProductId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discountPct(item.getDiscountPct())
                        .flatDiscount(item.getFlatDiscount())
                        .lineTotal(item.getLineTotal())
                        .createdAt(item.getCreatedAt())
                        .updatedAt(item.getUpdatedAt())
                        .createdBy(item.getCreatedBy())
                        .updatedBy(item.getUpdatedBy())
                        .build())
                .toList();

        List<PaymentResponse> paymentResponses = paymentRepository.findBySale_SalesId(sale.getSalesId())
                .stream()
                .map(this::mapToPaymentResponse)
                .toList();

        BigDecimal totalPaid = paymentResponses.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(PaymentResponse::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceived = paymentResponses.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(p -> p.getReceivedAmount() != null ? p.getReceivedAmount() : p.getPaidAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = sale.getTotalAmount() != null
                ? sale.getTotalAmount().subtract(totalPaid).max(BigDecimal.ZERO)
                : BigDecimal.ZERO;

        BigDecimal change = totalReceived.subtract(
                sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO).max(BigDecimal.ZERO);

        return SalesResponse.builder()
                .salesId(sale.getSalesId())
                .invoiceNumber(sale.getInvoiceNumber())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getCustomerId() : null)
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getCustomerName() : null)
                .saleDate(sale.getSaleDate())
                .subTotal(sale.getSubTotal())
                .discountAmount(sale.getDiscountAmount())
                .totalAmount(sale.getTotalAmount())
                .paidAmount(totalPaid)
                .receivedAmount(totalReceived)
                .changeAmount(change)
                .remainingAmount(remaining)
                .status(sale.getStatus())
                .cashier(sale.getCashier())
                .items(itemResponses)
                .payments(paymentResponses)
                .createdAt(sale.getCreatedAt())
                .updatedAt(sale.getUpdatedAt())
                .createdBy(sale.getCreatedBy())
                .updatedBy(sale.getUpdatedBy())
                .build();
    }

    private String getLoggedInCashierName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByEmail(auth.getName())
                .map(User::getFirstName)
                .orElse(null);
    }

    private void printReceiptSilently(SalesResponse response) {
        if (response.getStatus() == SalesStatus.DRAFT) return;
        try {
            printService.printReceipt(response);
        } catch (Exception e) {
            log.warn("Receipt printing failed for invoice {}: {}", response.getInvoiceNumber(), e.getMessage());
        }
    }

    private SalesResponse buildSummaryResponse(Sales sale) {
        BigDecimal totalPaid = paymentRepository.findBySale_SalesId(sale.getSalesId())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceived = paymentRepository.findBySale_SalesId(sale.getSalesId())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(p -> p.getReceivedAmount() != null ? p.getReceivedAmount() : p.getPaidAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = sale.getTotalAmount() != null
                ? sale.getTotalAmount().subtract(totalPaid).max(BigDecimal.ZERO)
                : BigDecimal.ZERO;

        BigDecimal change = totalReceived.subtract(
                sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO).max(BigDecimal.ZERO);

        return SalesResponse.builder()
                .salesId(sale.getSalesId())
                .invoiceNumber(sale.getInvoiceNumber())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getCustomerId() : null)
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getCustomerName() : null)
                .saleDate(sale.getSaleDate())
                .subTotal(sale.getSubTotal())
                .discountAmount(sale.getDiscountAmount())
                .totalAmount(sale.getTotalAmount())
                .paidAmount(totalPaid)
                .receivedAmount(totalReceived)
                .changeAmount(change)
                .remainingAmount(remaining)
                .status(sale.getStatus())
                .cashier(sale.getCashier())
                .createdAt(sale.getCreatedAt())
                .updatedAt(sale.getUpdatedAt())
                .createdBy(sale.getCreatedBy())
                .updatedBy(sale.getUpdatedBy())
                .build();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .saleId(payment.getSale().getSalesId())
                .paidAmount(payment.getPaidAmount())
                .receivedAmount(payment.getReceivedAmount())
                .paidAt(payment.getPaidAt())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .referenceNo(payment.getReferenceNo())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .createdBy(payment.getCreatedBy())
                .updatedBy(payment.getUpdatedBy())
                .build();
    }
}

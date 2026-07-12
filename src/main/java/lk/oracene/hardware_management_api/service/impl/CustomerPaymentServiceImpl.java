package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.CustomerPaymentRequest;
import lk.oracene.hardware_management_api.dto.response.CustomerPaymentResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Customer;
import lk.oracene.hardware_management_api.model.Payment;
import lk.oracene.hardware_management_api.model.PaymentMethod;
import lk.oracene.hardware_management_api.model.PaymentStatus;
import lk.oracene.hardware_management_api.model.Sales;
import lk.oracene.hardware_management_api.model.SalesStatus;
import lk.oracene.hardware_management_api.repository.CustomerRepository;
import lk.oracene.hardware_management_api.repository.PaymentRepository;
import lk.oracene.hardware_management_api.repository.SalesRepository;
import lk.oracene.hardware_management_api.service.CashDrawerService;
import lk.oracene.hardware_management_api.service.CustomerPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerPaymentServiceImpl implements CustomerPaymentService {

    private final CustomerRepository customerRepository;
    private final SalesRepository salesRepository;
    private final PaymentRepository paymentRepository;
    private final CashDrawerService cashDrawerService;

    @Override
    public CustomerPaymentResponse addPayment(CustomerPaymentRequest request) {
        Sales sale = salesRepository.findById(request.getSaleId())
                .orElseThrow(() -> new NotFoundException(
                        "Sale not found with id: " + request.getSaleId()));

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findByCustomerIdAndIsActiveTrue(request.getCustomerId())
                    .orElseThrow(() -> new NotFoundException(
                            "Active customer not found with id: " + request.getCustomerId()));

            if (sale.getCustomer() == null || !sale.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                throw new BadRequestException(
                        "Sale " + request.getSaleId() + " does not belong to customer " + request.getCustomerId());
            }
        }

        if (sale.getStatus() == SalesStatus.CANCELLED) {
            throw new BadRequestException("Cannot add payment to a cancelled sale");
        }

        BigDecimal totalPaid = paymentRepository.findBySale_SalesId(sale.getSalesId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = sale.getTotalAmount().subtract(totalPaid);
        if (request.getPaidAmount().compareTo(remaining) > 0) {
            throw new BadRequestException(
                    "Payment amount " + request.getPaidAmount() +
                    " exceeds remaining balance " + remaining);
        }

        Payment payment = new Payment();
        payment.setSale(sale);
        payment.setPaidAmount(request.getPaidAmount());
        payment.setPaidAt(LocalDateTime.now());
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setReferenceNo(request.getReferenceNo());
        Payment saved = paymentRepository.save(payment);

        if (request.getMethod() == PaymentMethod.CASH) {
            recordCashInSilently(request.getPaidAmount(), "Customer payment for sale " + sale.getInvoiceNumber(), saved.getPaymentId());
        }

        BigDecimal newTotalPaid = totalPaid.add(request.getPaidAmount());
        if (newTotalPaid.compareTo(sale.getTotalAmount()) >= 0) {
            sale.setStatus(SalesStatus.PAID);
        } else {
            sale.setStatus(SalesStatus.ADVANCE_PAID);
        }
        salesRepository.save(sale);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerPaymentResponse> getPaymentsByCustomer(Long customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer not found with id: " + customerId);
        }
        return paymentRepository.findBySale_Customer_CustomerId(customerId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerPaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + paymentId));
        return mapToResponse(payment);
    }

    private void recordCashInSilently(BigDecimal amount, String reason, Long paymentId) {
        try {
            cashDrawerService.recordCustomerPaymentCashIn(amount, reason, paymentId);
        } catch (Exception e) {
            log.warn("Cash drawer movement failed for payment {}: {}", paymentId, e.getMessage());
        }
    }

    private CustomerPaymentResponse mapToResponse(Payment payment) {
        Customer customer = payment.getSale().getCustomer();
        return CustomerPaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .customerId(customer != null ? customer.getCustomerId() : null)
                .customerName(customer != null ? customer.getCustomerName() : null)
                .saleId(payment.getSale().getSalesId())
                .paidAmount(payment.getPaidAmount())
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

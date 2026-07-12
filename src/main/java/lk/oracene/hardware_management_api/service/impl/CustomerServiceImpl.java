package lk.oracene.hardware_management_api.service.impl;

import lk.oracene.hardware_management_api.dto.request.CustomerRequest;
import lk.oracene.hardware_management_api.dto.response.CustomerResponse;
import lk.oracene.hardware_management_api.dto.response.OutstandingResponse;
import lk.oracene.hardware_management_api.exception.BadRequestException;
import lk.oracene.hardware_management_api.exception.NotFoundException;
import lk.oracene.hardware_management_api.model.Customer;
import lk.oracene.hardware_management_api.model.SalesStatus;
import lk.oracene.hardware_management_api.repository.CustomerRepository;
import lk.oracene.hardware_management_api.repository.PaymentRepository;
import lk.oracene.hardware_management_api.repository.SalesRepository;
import lk.oracene.hardware_management_api.service.CustomerService;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final SalesRepository salesRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (request.getPhone() != null && customerRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already exists: " + request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        Customer customer = new Customer();
        customer.setCustomerName(request.getCustomerName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setEmail(request.getEmail());
        customer.setIsActive(true);

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    @Override
    public CustomerResponse updateCustomer(Long customerId, CustomerRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException(
                        "Customer not found with id: " + customerId));

        customer.setCustomerName(request.getCustomerName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setEmail(request.getEmail());

        Customer updated = customerRepository.save(customer);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {
        Customer customer = customerRepository.findByCustomerIdAndIsActiveTrue(customerId)
                .orElseThrow(() -> new NotFoundException(
                        "Active customer not found with id: " + customerId));
        return mapToResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllActiveCustomers(Pageable pageable) {
        return customerRepository.findByIsActiveTrue(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return Page.empty(pageable);
        }
        return customerRepository.searchActive(query.trim(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OutstandingResponse getOutstandingByCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer not found with id: " + customerId);
        }
        BigDecimal totalAmount = salesRepository.sumTotalAmountByCustomerId(customerId);
        BigDecimal paidAmount = paymentRepository.sumPaidAmountByCustomerId(customerId);
        return OutstandingResponse.builder()
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .outstandingAmount(totalAmount.subtract(paidAmount))
                .build();
    }

    @Override
    public void deactivateCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException(
                        "Customer not found with id: " + customerId));

        if (salesRepository.existsByCustomer_CustomerIdAndStatus(customerId, SalesStatus.UNPAID)) {
            throw new BadRequestException(
                    "Cannot deactivate customer with pending bills. Please settle all outstanding bills first.");
        }

        customer.setIsActive(false);
        customerRepository.save(customer);
    }

    @Override
    public void activateCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException(
                        "Customer not found with id: " + customerId));

        if (Boolean.TRUE.equals(customer.getIsActive())) {
            throw new BadRequestException(
                    "Customer is already active with id: " + customerId);
        }

        customer.setIsActive(true);
        customerRepository.save(customer);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .customerName(customer.getCustomerName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .email(customer.getEmail())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .createdBy(customer.getCreatedBy())
                .updatedBy(customer.getUpdatedBy())
                .build();
    }
}
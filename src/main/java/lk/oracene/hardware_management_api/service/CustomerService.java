package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.CustomerRequest;
import lk.oracene.hardware_management_api.dto.response.CustomerResponse;
import lk.oracene.hardware_management_api.dto.response.OutstandingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse updateCustomer(Long customerId, CustomerRequest request);

    CustomerResponse getCustomerById(Long customerId);

    Page<CustomerResponse> getAllActiveCustomers(Pageable pageable);

    Page<CustomerResponse> getAllCustomers(Pageable pageable);

    Page<CustomerResponse> searchCustomersByName(String name, Pageable pageable);

    OutstandingResponse getOutstandingByCustomer(Long customerId);

    void deactivateCustomer(Long customerId);

    void activateCustomer(Long customerId);
}

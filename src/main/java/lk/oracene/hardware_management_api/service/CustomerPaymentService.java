package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.request.CustomerPaymentRequest;
import lk.oracene.hardware_management_api.dto.response.CustomerPaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerPaymentService {

    CustomerPaymentResponse addPayment(CustomerPaymentRequest request);

    Page<CustomerPaymentResponse> getPaymentsByCustomer(Long customerId, Pageable pageable);

    CustomerPaymentResponse getPaymentById(Long paymentId);
}

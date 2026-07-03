package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByIsActiveTrue(Pageable pageable);

    Optional<Customer> findByCustomerIdAndIsActiveTrue(Long customerId);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    Page<Customer> findByCustomerNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);
}

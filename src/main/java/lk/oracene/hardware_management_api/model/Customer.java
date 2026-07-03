package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_phone", columnList = "phone"),
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_is_active", columnList = "is_active")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    private String customerName;
    private String phone;
    private String address;
    private String email;
    private Boolean isActive = true;
}

package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "suppliers", indexes = {
        @Index(name = "idx_supplier_phone", columnList = "phone"),
        @Index(name = "idx_supplier_email", columnList = "email")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class Supplier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierId;

    private String name;
    private String phone;
    private String email;
    private String address;
    private Boolean isActive = true;
}

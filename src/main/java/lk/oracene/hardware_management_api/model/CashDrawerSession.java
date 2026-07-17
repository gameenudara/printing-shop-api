package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "cash_drawer_sessions")
@Data
@EqualsAndHashCode(callSuper = false)
public class CashDrawerSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal openingBalance;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CashDrawerSessionStatus status = CashDrawerSessionStatus.ONGOING;
}

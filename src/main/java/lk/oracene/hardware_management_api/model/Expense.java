package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_expense_date", columnList = "expense_date")
})
@Data
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expenseId;

    private String title;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    private LocalDateTime expenseDate;

    private String note;
}

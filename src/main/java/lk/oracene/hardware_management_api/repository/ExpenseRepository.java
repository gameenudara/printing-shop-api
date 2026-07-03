package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findAllByOrderByExpenseDateDesc(Pageable pageable);

    List<Expense> findByExpenseDateBetweenOrderByExpenseDateAsc(LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal sumAllAmounts();

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate BETWEEN :from AND :to")
    BigDecimal sumAmountsByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

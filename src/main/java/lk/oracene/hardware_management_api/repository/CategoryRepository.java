package lk.oracene.hardware_management_api.repository;

import lk.oracene.hardware_management_api.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    Page<Category> findByIsActiveTrue(Pageable pageable);
}

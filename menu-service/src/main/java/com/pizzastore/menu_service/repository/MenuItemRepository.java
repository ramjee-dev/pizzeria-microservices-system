package com.pizzastore.menu_service.repository;

import com.pizzastore.menu_service.entity.Category;
import com.pizzastore.menu_service.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByCategory(Category category);

    List<MenuItem> findByCategoryAndAvailableTrue(Category category);

    List<MenuItem> findByAvailableTrue();

    @Query("SELECT m FROM MenuItem m WHERE m.available = true AND " +
            "(LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<MenuItem> searchAvailableItems(@Param("keyword") String keyword);

    Long countByCategory(Category category);

    Long countByAvailableTrue();
}

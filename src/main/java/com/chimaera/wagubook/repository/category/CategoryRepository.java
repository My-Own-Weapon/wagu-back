package com.chimaera.wagubook.repository.category;

import com.chimaera.wagubook.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

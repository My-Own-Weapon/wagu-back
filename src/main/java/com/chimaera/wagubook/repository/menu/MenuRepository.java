package com.chimaera.wagubook.repository.menu;

import com.chimaera.wagubook.entity.Menu;
import com.chimaera.wagubook.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByMenuName(String menuName);
    Optional<Menu> findByIdAndPost(Long menuId, Post post);
}

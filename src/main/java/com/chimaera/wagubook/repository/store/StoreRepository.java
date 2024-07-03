package com.chimaera.wagubook.repository.store;

import com.chimaera.wagubook.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {
}

package com.chimaera.wagubook.repository.store;

import com.chimaera.wagubook.entity.Location;
import com.chimaera.wagubook.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {
    Optional<Store> findByStoreLocation(Location StoreLocation);
    Page<Store> searchStores(String keyword, Pageable pageable);
    Optional<Store> findById(Long storeId);
    Optional<Store> findByStoreLocationAndStoreName(Location storeLocation, String storeName);
}

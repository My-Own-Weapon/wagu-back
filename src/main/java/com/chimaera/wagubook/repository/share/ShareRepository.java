package com.chimaera.wagubook.repository.share;
import com.chimaera.wagubook.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ShareRepository extends JpaRepository<Share, Long>{
    Optional<Share> findByUrl(String url);
}

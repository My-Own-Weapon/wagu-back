package com.chimaera.wagubook.repository;


import com.chimaera.wagubook.entity.Store;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StoreQueryRepository {
    private final EntityManager em;


    public List<Store> findAllByScreen(String left, String right, String up, String down) {
        double dLeft = Double.parseDouble(left);
        double dRight = Double.parseDouble(right);
        double dDown = Double.parseDouble(down);
        double dUp = Double.parseDouble(up);


        String str = String.format(" where l.posx >%f and l.posx <%f and l.posy >%f and l.posy< %f", dLeft,dRight,dUp,dDown);
        return em.createQuery(
                "select s from Store s"+
                        " join fetch s.storeLocation l"+
                        str
                        , Store.class
        ).getResultList();
    }


    public Store findByStoreId(Long storeId) {
        String str = String.format(" where s.storeId=%d", storeId);
        List<Store> resultList = em.createQuery(
                "select s from Store s"+
                        " join fetch s.storeLocation l"+
                        str
                , Store.class
        ).getResultList();
        if(resultList.isEmpty())
            return null;
        return resultList.get(0);
    }
}

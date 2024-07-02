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
        float l,r,u,d;

        l = Float.parseFloat(left);
        r = Float.parseFloat(right);
        u = Float.parseFloat(up);
        d = Float.parseFloat(down);

        String str = String.format(" where l.posx >%f and l.posx <%f and l.posy >%f and l.posy< %f", l,r,u,d);
        return em.createQuery(
                "select s from Store s"+
                        " join fetch s.storeLocation l"+
                        str
                        , Store.class
        ).getResultList();
    }
}

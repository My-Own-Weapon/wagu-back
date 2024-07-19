package com.chimaera.wagubook.repository.member;


import com.chimaera.wagubook.entity.LiveRoom;
import com.chimaera.wagubook.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom  {
    Optional<Member> findByUsername(String username);

    void saveLiveRoom(LiveRoom liveRoom);
    void deleteLiveRoom(String sessionId);

}

package com.chimaera.wagubook.repository.member;

import com.chimaera.wagubook.entity.LiveRoom;
import com.chimaera.wagubook.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    Page<Member> searchMembers(String username, Pageable pageable); // 사용자 검색
    void saveLiveRoom(LiveRoom liveRoom); // 라이브 룸 저장
    void deleteLiveRoom(String sessionId); // 라이브 룸 삭제
}

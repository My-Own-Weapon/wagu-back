package com.chimaera.wagubook.service;


import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final MemberRepository memberRepository;

    public List<Member> searchMembers(String username) {
        return memberRepository.findByUsernameContaining(username);
    }
}

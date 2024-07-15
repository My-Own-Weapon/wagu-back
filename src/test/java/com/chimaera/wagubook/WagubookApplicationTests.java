package com.chimaera.wagubook;

import com.chimaera.wagubook.controller.MemberController;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WagubookApplicationTests {

    @Autowired
    private MemberController memberController;
    private MemberRepository memberRepository;
    @Test
    void contextLoads() {
    }

//    @Test
//    void testJoin(){
//        Member member1 = Member.newBuilder()
//                .username("id1")
//                .name("name1")
//                .onLive(false)
//                .password("pw1")
//                .phoneNumber("010")
//                .build();
//
//        memberController.join();
//
//    }
}

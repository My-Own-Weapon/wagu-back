package com.chimaera.wagubook.service;

import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.Share;
import com.chimaera.wagubook.repository.ShareRepository;
import com.chimaera.wagubook.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class ShareService {
    private final ShareRepository shareRepository;
    private final MemberRepository memberRepository;

    public String createUrl(Long memberId) {

        //랜덤 10자리 숫자 생성
        boolean loop = true;
        String randomCode = "";
        while(loop){
            randomCode = generateRandomCode('0','z',10);
            // 중복검사
            Optional<Share> os = shareRepository.findByUrl(randomCode);
            if(os.isEmpty()){
                loop = false;
                System.out.println("randomCode : " + randomCode);
            }
        }

        Member findMember = memberRepository.findById(memberId).get();

        //share entity 생성
        Share share = Share.newBuilder()
                .url(randomCode)
                .localDateTime(LocalDateTime.now())
                .memberList(new ArrayList<>())
                .build();

        share.getMemberList().add(findMember);
//        shareRepository.save(share);

        return randomCode;
    }

    private static final Random RANDOM = new Random();
    public static String generateRandomCode(final char leftLimit, final char rightLimit, final int limit) {
        return RANDOM.ints(leftLimit, rightLimit + 1)
                .filter(i -> Character.isAlphabetic(i) || Character.isDigit(i))
                .limit(limit)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public String findShareId(String url, Long memberId) {
        Optional<Share> os = shareRepository.findByUrl(url);
        if(os.isPresent()){
            Share findShare = os.get();
            //유효기간(30분) 만료 전일 때만 반환
            Duration duration = Duration.between(findShare.getLocalDateTime(),LocalDateTime.now());
            System.out.println("localDateTime : " + LocalDateTime.now());
            System.out.println("ShareDAteTime : " + findShare.getLocalDateTime());
            System.out.println("duration : "+duration.getSeconds());
            if(duration.getSeconds() < 1800){

                // share 에 member 등록
                addMember(findShare, memberId);

                return "" + findShare.getId();
            }
        }
        return "해당 url이 존재하지 않습니다.";
    }

    private void addMember(Share share, Long memberId) {
        Member findMember = memberRepository.findById(memberId).get();
        //이미 포함된 사람인지 확인
        if(share.getMemberList().contains(findMember))
            return;
        share.getMemberList().add(findMember);
    }

}

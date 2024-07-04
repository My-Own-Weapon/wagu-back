package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.ShareResponse;
import com.chimaera.wagubook.entity.Share;
import com.chimaera.wagubook.repository.ShareRepository;
import com.chimaera.wagubook.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class ShareService {
    private final ShareRepository shareRepository;

    public ShareResponse createUrl() {

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

        //share entity 생성
        Share share = Share.newBuilder()
                .url(randomCode)
                .localDateTime(LocalDateTime.now())
                .isValid(true)
                .build();
        shareRepository.save(share);

        return new ShareResponse(share);
    }

    private static final Random RANDOM = new Random();
    public static String generateRandomCode(final char leftLimit, final char rightLimit, final int limit) {
        return RANDOM.ints(leftLimit, rightLimit + 1)
                .filter(i -> Character.isAlphabetic(i) || Character.isDigit(i))
                .limit(limit)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public String findShareId(String url) {
        Optional<Share> os = shareRepository.findByUrl(url);
        if(os.isPresent()){
            Share findShare = os.get();
            //유효기간(30분) 만료 전일 때만 반환
            Duration duration = Duration.between(findShare.getLocalDateTime(),LocalDateTime.now());
            System.out.println("localDateTime : " + LocalDateTime.now());
            System.out.println("ShareDAteTime : " + findShare.getLocalDateTime());
            System.out.println("duration : "+duration.getSeconds());
            if(duration.getSeconds() < 1800){
                return "" + os.get().getId();
            }
        }
        return "해당 url이 존재하지 않습니다.";
    }
}

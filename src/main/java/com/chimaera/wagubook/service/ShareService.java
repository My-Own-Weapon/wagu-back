package com.chimaera.wagubook.service;

import ch.qos.logback.core.testUtil.RandomUtil;
import com.chimaera.wagubook.dto.ShareResponse;
import com.chimaera.wagubook.entity.Share;
import com.chimaera.wagubook.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class ShareService {
    private final RedisUtil redisUtil;
    private static final String INVITE_LINK_PREFIX = "memberId=%d";

    /**
     *
     *
     * */
    public ShareResponse createUrl(Long memberId) {
        final Optional<String> link = redisUtil.getData(INVITE_LINK_PREFIX.formatted(memberId), String.class);
        if(link.isEmpty()){
            final String randomCode = generateRandomCode('0','z',10);
            redisUtil.setDataExpire(INVITE_LINK_PREFIX.formatted(memberId), randomCode, RedisUtil.toTomorrow());
            Share share = new Share();
            return new ShareResponse();
        }
    }

    private static final Random RANDOM = new Random();
    public static String generateRandomCode(final char leftLimit, final char rightLimit, final int limit) {
        return RANDOM.ints(leftLimit, rightLimit + 1)
                .filter(i -> Character.isAlphabetic(i) || Character.isDigit(i))
                .limit(limit)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}

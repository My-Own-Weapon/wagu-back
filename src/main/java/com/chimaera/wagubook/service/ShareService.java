package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.response.StoreResponse;
import com.chimaera.wagubook.dto.response.StoreSearchResponse;
import com.chimaera.wagubook.entity.Share;
import com.chimaera.wagubook.entity.Store;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.repository.share.ShareRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ShareService {
    private final ShareRepository shareRepository;
    private final StoreRepository storeRepository;
    private final RedisService redisService;

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

        //share entity 생성
        Share share = Share.newBuilder()
                .url(randomCode)
//                .localDateTime(LocalDateTime.now())
                .voteStoreList(new HashMap<>())
                .build();

        shareRepository.save(share);

        //Redis에 저장
        redisService.setValuesObject(randomCode, share, Duration.ofMinutes(5));

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
//        Optional<Share> os = shareRepository.findByUrl(url);
////        if(os.isPresent()){
////            Share findShare = os.get();
////            //유효기간(30분) 만료 전일 때만 반환
////            Duration duration = Duration.between(findShare.getLocalDateTime(),LocalDateTime.now());
////            System.out.println("localDateTime : " + LocalDateTime.now());
////            System.out.println("ShareDAteTime : " + findShare.getLocalDateTime());
////            System.out.println("duration : "+duration.getSeconds());
////            if(duration.getSeconds() < 1800){
////
////                return "" + findShare.getId();
////            }
////        }
        Share value = (Share)redisService.getObject(url);
        if(value == null)
            throw new CustomException(ErrorCode.NOT_FOUND_URL);
        else
            return ""+value.getId();

//        return "해당 url이 존재하지 않습니다.";
    }

    @Transactional
    public String addVoteStore(String url, String storeId) {
        //가게 찾기
        Optional<Store> os =storeRepository.findById(Long.parseLong(storeId));
        if(os.isEmpty()){
            throw new CustomException(ErrorCode.NOT_FOUND_STORE);
        }
        Store findStore = storeRepository.findById(Long.parseLong(storeId)).get();


        //공유방에서 리스트 찾기
        Share share = (Share)redisService.getObject(url);
        if(share == null)
            throw new CustomException(ErrorCode.NOT_FOUND_URL);
//        Optional<Share> osh = shareRepository.findById(Long.parseLong(shareId));
//        if(osh.isEmpty()){
//            throw new CustomException(ErrorCode.NOT_FOUND_SHARE);
//        }
//        Share share = osh.get();
        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();

        System.out.println("[before add]");
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            System.out.println("key : " + entry.getKey() + " value : " + entry.getValue());
        }

        //이미 포함하고 있으면
        System.out.println(findStore.getId());
        if(voteStoreList.containsKey(findStore.getId()))
            throw new CustomException(ErrorCode.ALREADY_ADD);

        //최대 개수를 넘은 경우
        if(voteStoreList.size() == 10)
            throw new CustomException(ErrorCode.OVER_MAX);

        voteStoreList.put(findStore.getId(), 0);
        //변경사항 저장
//        shareRepository.save(share);
        redisService.setValuesObject(url, share, Duration.ofMinutes(5));

        System.out.println("[after add]");
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            System.out.println("key : " + entry.getKey() + " value : " + entry.getValue());
        }
        return "투표에 추가되었습니다.";
    }

    @Transactional
    public String deleteVoteStore(String url, String storeId) {
        //가게 찾기
        Store findStore = storeRepository.findById(Long.parseLong(storeId)).get();
        //공유 엔티티에서 제거
        Share share = (Share) redisService.getObject(url);
        if(share == null)
            throw new CustomException(ErrorCode.NOT_FOUND_URL);
//        Share share = shareRepository.findById(Long.parseLong(shareId)).get();

        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();

        //투표 리스트에 있으면 제거
        if(voteStoreList.remove(findStore.getId())!=null){
            System.out.println("[after delete]");
            for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
                System.out.println("key : " + entry.getKey() + " value : " + entry.getValue());
            }
//            shareRepository.save(share);
            redisService.setValuesObject(url, share, Duration.ofMinutes(5));
            return "투표에서 삭제되었습니다.";
        }
        return "투표 리스트에 존재하지 않는 가게입니다.";
    }

    public String like(String url, String storeId) {
        //공유 데이터 찾기
        Share share = (Share) redisService.getObject(url);
        if(share == null)
            throw new CustomException(ErrorCode.NOT_FOUND_URL);

//        Share share = shareRepository.findById(Long.parseLong(shareId)).get();

        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();
        Long key = Long.parseLong(storeId);
        voteStoreList.replace(key, voteStoreList.get(key)+1);
        System.out.println("value : " + voteStoreList.get(key));

        System.out.println("[after like]");
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            System.out.println("key : " + entry.getKey() + " value : " + entry.getValue());
        }
//        shareRepository.save(share);
        redisService.setValuesObject(url, share, Duration.ofMinutes(5));
        return "투표 성공";
    }

    public String likeCancel(String url, String storeId) {
        //공유 데이터 찾기
        Share share = (Share) redisService.getObject(url);
        if(share == null)
            throw new CustomException(ErrorCode.NOT_FOUND_URL);
//        Share share = shareRepository.findById(Long.parseLong(shareId)).get();

        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();
        Long key = Long.parseLong(storeId);
        voteStoreList.replace(key, voteStoreList.get(key)-1);
//        shareRepository.save(share);
        redisService.setValuesObject(url, share, Duration.ofMinutes(5));

        System.out.println("[after cancel]");
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            System.out.println("key : " + entry.getKey() + " value : " + entry.getValue());
        }
        return "투표 취소";
    }

    public List<StoreResponse> showResult(String url) {
        //공유 데이터 찾기
        Share share = (Share) redisService.getObject(url);
        if(share == null)
            throw new CustomException(ErrorCode.NOT_FOUND_URL);
//        Share share = shareRepository.findById(Long.parseLong(shareId)).get();

        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();
        int max = 0;
        for (Integer value : voteStoreList.values()) {
            max = Math.max(max, value);
        }
        List<StoreResponse> ret = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            if(entry.getValue() == max){
                ret.add(new StoreResponse(storeRepository.findById(entry.getKey()).get()));
            }
        }
        return ret;
    }


    public List<StoreSearchResponse> showVoteList(String url) {
        Share share = (Share) redisService.getObject(url);
        if(share == null)
            throw new CustomException(ErrorCode.NOT_FOUND_URL);

        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();
        List<StoreSearchResponse> ret = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            ret.add(new StoreSearchResponse(storeRepository.findById(entry.getKey()).get()));
        }
        return ret;
    }
}

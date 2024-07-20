package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.response.StoreResponse;
import com.chimaera.wagubook.dto.response.StoreSearchResponse;
import com.chimaera.wagubook.entity.Share;
import com.chimaera.wagubook.entity.Store;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.repository.redis.RedisLockRepository;
import com.chimaera.wagubook.repository.redis.RedisRepository;
import com.chimaera.wagubook.repository.share.ShareRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ShareService {
    private final ShareRepository shareRepository;
    private final StoreRepository storeRepository;
    private final RedisRepository redisRepository;
    private final RedisLockRepository redisLockRepository;
    private Integer TIME_LIMIT_MINUTE = 5;
    private Integer SLEEP_TIME_MILLI = 10;

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
                .voteStoreList(new HashMap<>())
                .build();

        shareRepository.save(share);

        //Redis에 저장
        redisRepository.setValuesObject(randomCode, share, Duration.ofMinutes(TIME_LIMIT_MINUTE));

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
        Share value = (Share) redisRepository.getObject(url);
        if(value == null)
            throw new CustomException(ErrorCode.NOT_FOUND_URL);
        return ""+value.getId();
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
        Share share = (Share) redisRepository.getObject(url);
        if(share == null){
            throw new CustomException(ErrorCode.NOT_FOUND_URL);
        }
        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();

        //TODO: 디버그 코드 추후 삭제
        System.out.println("[before add]");
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            System.out.println("key : " + entry.getKey() + " value : " + entry.getValue());
        }
        System.out.println(findStore.getId());

        //이미 포함하고 있으면 ALREADY_ADD
        if(voteStoreList.containsKey(findStore.getId())){
            throw new CustomException(ErrorCode.ALREADY_ADD);
        }

        //최대 개수를 넘은 경우 OVER_MAX
        if(voteStoreList.size() == 10){
            throw new CustomException(ErrorCode.OVER_MAX);
        }

        //변경 후 저장
        voteStoreList.put(findStore.getId(), 0);
        redisRepository.setValuesObject(url, share, Duration.ofMinutes(TIME_LIMIT_MINUTE));

        //TODO: 디버그 코드 추후 삭제
        System.out.println("[after add]");
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            System.out.println("key : " + entry.getKey() + " value : " + entry.getValue());
        }
        return "투표에 추가되었습니다.";
    }

    @Transactional
    public String deleteVoteStore(String url, String storeId) {

        //공유 엔티티 조회
        Share share = (Share) redisRepository.getObject(url);
        if(share == null){
            throw new CustomException(ErrorCode.NOT_FOUND_URL);
        }
        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();


        //투표 리스트에 있으면 제거
        if(voteStoreList.remove(storeId)!=null){
            //TODO: 디버그 코드 추후 삭제
            System.out.println("[after delete]");
            for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
                System.out.println("key : " + entry.getKey() + " value : " + entry.getValue());
            }

            //redis에 저장
            redisRepository.setValuesObject(url, share, Duration.ofMinutes(TIME_LIMIT_MINUTE));
            return "투표에서 삭제되었습니다.";
        }
        return "투표 리스트에 존재하지 않는 가게입니다.";
    }

    public String like(String url, String storeId) throws InterruptedException{

        //redis Lock
        while (Boolean.FALSE.equals(redisLockRepository.lock("like"+url+storeId))){
            Thread.sleep(SLEEP_TIME_MILLI);
        }

        //redis에서 공유 데이터 조회
        Share share = (Share) redisRepository.getObject(url);
        if(share == null){
            redisLockRepository.unlock("like"+url+storeId);
            throw new CustomException(ErrorCode.NOT_FOUND_URL);
        }

        //voteStoreList 데이터 변경
        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();
        Long key = Long.parseLong(storeId);
        voteStoreList.replace(key, voteStoreList.get(key)+1);

        //TODO: 디버그 코드 추후 삭제
        System.out.println("value : " + voteStoreList.get(key));
        System.out.println("[after like]");
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            System.out.println("key : " + entry.getKey() + " value : " + entry.getValue());
        }

        //redis에 저장
        redisRepository.setValuesObject(url, share, Duration.ofMinutes(TIME_LIMIT_MINUTE));

        //redis unLock
        redisLockRepository.unlock("like"+url+storeId);
        return "투표 성공";
    }

    public String likeCancel(String url, String storeId) throws InterruptedException{

        //redis Lock
        while (Boolean.FALSE.equals(redisLockRepository.lock("likeCancel"+url+storeId))){
            Thread.sleep(SLEEP_TIME_MILLI);
        }

        //공유 데이터 찾기
        Share share = (Share) redisRepository.getObject(url);
        if(share == null){
            redisLockRepository.unlock("likeCancel"+url+storeId);
            throw new CustomException(ErrorCode.NOT_FOUND_URL);
        }

        //voteStoreList 데이터 변경
        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();
        Long key = Long.parseLong(storeId);
        voteStoreList.replace(key, voteStoreList.get(key)-1);

        //redis에 저장
        redisRepository.setValuesObject(url, share, Duration.ofMinutes(TIME_LIMIT_MINUTE));

        //TODO: 디버그 코드 추후 삭제
        System.out.println("[after cancel]");
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            System.out.println("key : " + entry.getKey() + " value : " + entry.getValue());
        }

        //redis unLock
        redisLockRepository.unlock("likeCancel"+url+storeId);
        return "투표 취소";
    }

    public List<StoreSearchResponse> showResult(String url) {
        //공유 데이터 찾기
        Share share = (Share) redisRepository.getObject(url);
        if(share == null)
            throw new CustomException(ErrorCode.NOT_FOUND_URL);

        //최댓값 찾기
        int max = 0;
        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();
        for (Integer value : voteStoreList.values()) {
            max = Math.max(max, value);
        }
      
        //최댓값에 해당하는 데이터 찾기
        List<StoreSearchResponse> ret = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            if(entry.getValue() == max){
                ret.add(new StoreSearchResponse(storeRepository.findById(entry.getKey()).get()));
            }
        }
        return ret;
    }


    public List<StoreSearchResponse> showVoteList(String url) {
        //share entity 조회
        Share share = (Share) redisRepository.getObject(url);
        if(share == null)
            throw new CustomException(ErrorCode.NOT_FOUND_URL);

        //voteStoreList 를 StoreSearchResponse 로 변환 후 반환
        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();
        List<StoreSearchResponse> ret = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            ret.add(new StoreSearchResponse(storeRepository.findById(entry.getKey()).get()));
        }
        return ret;
    }
}

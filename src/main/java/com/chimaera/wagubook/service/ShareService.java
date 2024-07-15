package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.StoreResponse;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.Share;
import com.chimaera.wagubook.entity.Store;
import com.chimaera.wagubook.repository.share.ShareRepository;
import com.chimaera.wagubook.repository.member.MemberRepository;
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
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;

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
//                .memberList(new ArrayList<>())
                .voteStoreList(new HashMap<>())
//                .storeList(new ArrayList<>())
                .build();

//        share.getMemberList().add(findMember);
        shareRepository.save(share);

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
//                addMember(findShare, memberId);

                return "" + findShare.getId();
            }
        }
        return "해당 url이 존재하지 않습니다.";
    }

//    private void addMember(Share share, Long memberId) {
//        Member findMember = memberRepository.findById(memberId).get();
//        //이미 포함된 사람인지 확인
//        if(share.getMemberIdList().contains(findMember.getId()))
//            return;
//        share.getMemberIdList().add(findMember.getId());
//    }

    @Transactional
    public String addVoteStore(String shareId, String storeId) {
        //가게 찾기
        Store findStore = storeRepository.findById(Long.parseLong(storeId)).get();
        //가게 추가
        Share share = shareRepository.findById(Long.parseLong(shareId)).get();
        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();


//        List<VoteStore> storeList = share.getStoreList();

        //이미 포함하고 있으면
        if(voteStoreList.containsKey(findStore.getId()))
            return "이미 추가된 가게입니다.";
//        for (VoteStore voteStore : storeList) {
//            if(voteStore.getStore_id()== findStore.getId())
//                return "이미 추가된 가게입니다.";
//        }

        //최대 개수를 넘은 경우
        if(voteStoreList.size() == 10){
            return "최대 10개까지만 투표에 추가할 수 있습니다.";
        }

        voteStoreList.put(findStore.getId(), 0);
        //변경사항 저장
        shareRepository.save(share);
        return "투표에 추가되었습니다.";
    }

    @Transactional
    public String deleteVoteStore(String shareId, String storeId) {
        //가게 찾기
        Store findStore = storeRepository.findById(Long.parseLong(storeId)).get();
        //공유 엔티티에서 제거
        Share share = shareRepository.findById(Long.parseLong(shareId)).get();

        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();

        //투표 리스트에 있으면 제거
        if(voteStoreList.containsKey(findStore.getId())){
            voteStoreList.remove(findStore.getId());

            shareRepository.save(share);
            return "투표에서 삭제되었습니다.";
        }
        return "투표 리스트에 존재하지 않는 가게입니다.";
    }

    public String like(String shareId, String storeId) {
        //가게 찾기
        Share share = shareRepository.findById(Long.parseLong(shareId)).get();

        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();
        Long key = Long.parseLong(storeId);
        voteStoreList.replace(key, voteStoreList.get(key)+1);

//        List<VoteStore> storeList = share.getStoreList();
//        for (VoteStore voteStore : storeList) {
//            if(voteStore.getStore_id() == Long.parseLong(storeId)){
//                voteStore.setLike_cnt(voteStore.getLike_cnt()+1);
//            }
//        }
        shareRepository.save(share);
        return "투표 성공";
    }

    public String likeCancel(String shareId, String storeId) {
        //가게 찾기
        Share share = shareRepository.findById(Long.parseLong(shareId)).get();

        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();
        Long key = Long.parseLong(storeId);
        voteStoreList.replace(key, voteStoreList.get(key)-1);
        shareRepository.save(share);
        return "투표 취소";
    }

    public List<StoreResponse> showResult(String shareId) {
        Share share = shareRepository.findById(Long.parseLong(shareId)).get();

        HashMap<Long, Integer> voteStoreList = share.getVoteStoreList();
        int max = 0;
        for (Integer value : voteStoreList.values()) {
            Math.max(max, value);
        }

        List<StoreResponse> ret = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : voteStoreList.entrySet()) {
            if(entry.getValue() == max){
                ret.add(new StoreResponse(storeRepository.findById(entry.getKey()).get()));
            }
        }
        return ret;
    }
}

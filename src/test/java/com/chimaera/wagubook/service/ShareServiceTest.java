package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.response.StoreSearchResponse;
import com.chimaera.wagubook.entity.Share;
import com.chimaera.wagubook.entity.Store;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.repository.share.ShareRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShareServiceTest {
    @Autowired
    ShareService shareService;
    @Autowired
    ShareRepository shareRepository;
    @Autowired
    StoreRepository storeRepository;

    @BeforeAll
    void setUp() {
        System.out.println("========Before All========");
        for(int i=0; i<11; i++){
            Store store = Store.newBuilder()
                    .storeName("testStore" + i)
                    .build();
            storeRepository.save(store);
        }
    }

    @AfterEach
    void tearDown() {
        System.out.println("========After Each========");
    }

    @Test
    @DisplayName("createUrl() : 랜덤 url 생성")
    void createUrl() {
        System.out.println("[+] 랜덤 url 생성");
        //given
        Long memberId = (long)2;

        //when
        String resultUrl = shareService.createUrl(memberId);

        //then
        assertNotEquals(resultUrl, null);
        System.out.println("[+] not null test 성공");
        assertEquals(resultUrl.length(), 10);
        System.out.println("[+] 10자리 생성 성공");
        Optional<Share> share = shareRepository.findByUrl(resultUrl);
        assertTrue(share.isPresent());
        System.out.println("[+] DB에 저장 성공");
    }

    @DisplayName("addVoteStore() : 투표에 추가 성공")
    @Test
    @Order(2)
    void addVoteStore() {
        //given
        String storeId = "1";
        long memberId = (long)1;
        String url = shareService.createUrl(memberId);

        //when
        String message = shareService.addVoteStore(url, storeId);

        //then
        assertEquals(message, "투표에 추가되었습니다.");
    }

    @DisplayName("addVoteStore() : 존재하지 않는 스토어일때 예외 발생")
    @Test
    void addVoteStore_NOTFOUND() {
        //given
        long memberId = (long)1;
        String storeId = "12";
        String url = shareService.createUrl(memberId);

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> shareService.addVoteStore(url, storeId)
        );

        //then
        assertEquals(exception.getMessage(), "해당 스토어를 찾을 수 없습니다.");
    }

    @DisplayName("addVoteStore() : 투표에 추가할 때, url이 잘못되었거나 만료되었을 때 예외 발생")
    @Test
    void addVoteStore_NOT_FOUND_URL() {
        //given
        long memberId = (long)1;
        String storeId = "1";
        String url = "testcode22";

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> shareService.addVoteStore(url, storeId)
        );

        //then
        assertEquals(exception.getMessage(), "해당 url이 존재하지 않습니다.");
    }

    @DisplayName("addVoteStore() : 이미 투표에 포함되어 있을때 투표에 추가하면 예외 발생")
    @Test
    @Order(3)
    void addVoteStore_ALREADY_ADD() {
        //given
        long memberId = (long)1;
        String storeId = "1";
        String url = shareService.createUrl(memberId);

        //when
        shareService.addVoteStore(url, storeId);
        CustomException exception = assertThrows(CustomException.class,
                () -> shareService.addVoteStore(url, storeId)
        );

        //then
        assertEquals(exception.getMessage(), "이미 추가된 가게입니다.");
    }

    @DisplayName("addVoteStore() : 투표에 포함된 가게 수가 10개일 때 새로운 가게를 추가하면 예외 발생")
    @Test
    void addVoteStore_OVER_MAX() {
        //given
        long memberId = (long)1;
        String url = shareService.createUrl(memberId);
        for(int i=1; i<11; i++){
            shareService.addVoteStore(url, ""+i);
        }

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> shareService.addVoteStore(url, "11")
        );

        //then
        assertEquals(exception.getMessage(), "최대 10개까지만 투표에 추가할 수 있습니다.");
    }

    @Test
    @DisplayName("deleteVoteStore() : 투표 리스트에서 삭제 성공")
    void deleteVoteStore() {
        //given
        long memberId = (long)1;
        String storeId = "1";
        String url = shareService.createUrl(memberId);
        shareService.addVoteStore(url, storeId);

        //when
        String message = shareService.deleteVoteStore(url, storeId);

        //then
        assertEquals(message, "투표에서 삭제되었습니다.");
    }

    @Test
    @DisplayName("deleteVoteStore() : 투표 리스트에 존재하지 않는 storeId가 들어온 경우 실패 메시지 전달")
    void deleteVoteStore_() {
        //given
        long memberId = (long)1;
        String storeId = "1";
        String url = shareService.createUrl(memberId);
        shareService.addVoteStore(url, storeId);

        //when
        String message = shareService.deleteVoteStore(url, "2");

        //then
        assertEquals(message, "투표 리스트에 존재하지 않는 가게입니다.");
    }

    @Test
    @DisplayName("deleteVoteStore() : 투표에서 삭제할 때, url이 잘못되었거나 만료되었을 때 예외 발생")
    void deleteVoteStore_NOF_FOUND_URL() {
        //given
        long memberId = (long)1;
        String storeId = "1";
        String url = "testcode22";

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> shareService.deleteVoteStore(url, storeId)
        );

        //then
        assertEquals(exception.getMessage(), "해당 url이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("like() : 투표 성공")
    void like() throws InterruptedException{
        //given
        long memberId = (long)1;
        String storeId = "1";
        String url = shareService.createUrl(memberId);
        shareService.addVoteStore(url, storeId);

        //when
        String message = shareService.like(url, storeId);

        //then
        assertEquals(message, "투표 성공");
    }

    @Test
    @DisplayName("likeCancel() : 투표 취소 성공")
    void likeCancel() throws InterruptedException{
        //given
        long memberId = (long)1;
        String storeId = "1";
        String url = shareService.createUrl(memberId);
        shareService.addVoteStore(url, storeId);
        shareService.like(url, storeId);

        //when
        String message = shareService.likeCancel(url, storeId);

        //then
        assertEquals(message, "투표 취소");
    }

    @Test
    @DisplayName("showResult() : 가장 많이 투표한 가게 반환 성공")
    void showResult() throws InterruptedException{
        //given
        //url 생성
        long memberId = (long)1;
        String storeId1 = "1";
        String storeId2 = "2";
        String url = shareService.createUrl(memberId);
        //투표에 추가
        shareService.addVoteStore(url, storeId1);
        shareService.addVoteStore(url, storeId2);
        //좋아요
        shareService.like(url, storeId1);
//        shareService.like(url, storeId1);
//        shareService.like(url, storeId2);

        //when
        List<StoreSearchResponse> responses = shareService.showResult(url);

        //then
        assertEquals(responses.size(), 1);
        assertEquals(responses.get(0).getStoreId(), (long)1);
    }
}
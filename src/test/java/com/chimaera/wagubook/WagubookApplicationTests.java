package com.chimaera.wagubook;

import com.chimaera.wagubook.controller.MemberController;
import com.chimaera.wagubook.dto.request.MemberRequest;
import com.chimaera.wagubook.entity.*;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.repository.menu.MenuImageRepository;
import com.chimaera.wagubook.repository.menu.MenuRepository;
import com.chimaera.wagubook.repository.post.PostRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import com.chimaera.wagubook.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.*;

@SpringBootTest
class WagubookApplicationTests {

    @Autowired
    private MemberController memberController;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;

    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MenuImageRepository menuImageRepository;
    @Test
    void contextLoads() {
    }

    @Test
    void createMemberData(){
        for(int i=1; i<=100; i++){
            MemberRequest memberRequest = new MemberRequest();
            memberRequest.setPassword("pw" + i);
            memberRequest.setUsername("id" + i);
            memberRequest.setName("name" + i);
            memberRequest.setPasswordConfirm("pw" + i);
            memberRequest.setPhoneNumber("010-0000-0000");
            memberService.join(memberRequest);
        }
    }

    void create(Long id, String address, double x, double y, String menuName, Category c,String storeName , String url){
        Member member = memberService.findById(id);

        Location location = new Location();
        location.setPosy(y);
        location.setPosx(x);
        location.setAddress(address);


        Store store = Store.newBuilder()
                .storeName(storeName)
                .storeLocation(location)
                .build();

        storeRepository.save(store);

        List<Menu> menus = new ArrayList<>();


        Menu menu = Menu.newBuilder()
                .menuName(menuName)
                .menuPrice(10000)
                .menuContent("맛있어요 " + Math.random())
                .store(store)
                .build();
        menus.add(menu);

        Post post = Post.newBuilder()
                .member(member)
                .store(store)
                .menus(menus)
                .postMainMenu(menuName)
                .category(c)
                .permission(Permission.PUBLIC)
                .isAuto(false)
                .createDate(LocalDateTime.now())
                .isFinished(true)
                .build();
        postRepository.save(post);

        menus.get(0).setPost(post);

        MenuImage menuImage = MenuImage.newBuilder()
                .url(url)
                .menu(menu)
                .build();
        menuImageRepository.save(menuImage);

        menu.setMenuImage(menuImage);
        menuRepository.save(menu);
    }
    @Test
    void createPostAndStoreData(){
        create((long) (Math.random() * 99),"경기 수원시 영통구 대학1로64번길 55", 37.298994, 127.0438405, "팟타이새우", Category.KOREAN,"방콕스토리", "https://wagu-book-gitget-deploy-bucket.s3.ap-northeast-2.amazonaws.com/%EB%8B%A4%EC%9A%B4%EB%A1%9C%EB%93%9C.jfif");
        create((long) (Math.random() * 99),"경기 수원시 영통구 대학로 14 1층 소소곱창", 37.2978364111217, 127.042315709229, "모듬구이", Category.KOREAN,"소소곱창","https://wagu-book-gitget-deploy-bucket.s3.ap-northeast-2.amazonaws.com/%EB%8B%A4%EC%9A%B4%EB%A1%9C%EB%93%9C+(1).jfif");
        create((long) (Math.random() * 99),"경기 수원시 영통구 대학로 34", 37.2992617297301, 127.043643237333, "돌솥부대찌개", Category.KOREAN,"석기정돌솥부대찌개 경기대점", "https://wagu-book-gitget-deploy-bucket.s3.ap-northeast-2.amazonaws.com/%EB%8B%A4%EC%9A%B4%EB%A1%9C%EB%93%9C+(2).jfif");
        create((long) (Math.random() * 99),"경기 수원시 영통구 대학1로8번길 5 101호", 37.2973222846293, 127.042552496297, "프리미엄 부리또", Category.WESTERN,"용부리또 본점", "https://wagu-book-gitget-deploy-bucket.s3.ap-northeast-2.amazonaws.com/r_380+(1).jpg");
        create((long) (Math.random() * 99),"경기 수원시 영통구 창룡대로 370-1", 37.294848851988775, 127.04840312421157, "빅맥세트", Category.WESTERN,"맥도날드", "https://wagu-book-gitget-deploy-bucket.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20240712_232704958.jpg");
        create((long) (Math.random() * 99),"경기 수원시 장안구 창훈로66번길 16-14", 37.298062707582034, 127.02973054495024, "잔치국수", Category.KOREAN,"국수집", "https://wagu-book-gitget-deploy-bucket.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20240712_232704958_04.jpg");
        create((long) (Math.random() * 99),"경기 수원시 장안구 창훈로66번길 17", 37.29835574299786, 127.0289298724135, "칼국수", Category.KOREAN,"홍두깨손칼국수", "https://wagu-book-gitget-deploy-bucket.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20240712_232704958_03.jpg");
        create((long) (Math.random() * 99),"경기 수원시 장안구 창훈로60번길 33", 37.2987767905689, 127.02968571094746, "육개장순대국", Category.KOREAN,"큰맘할매순대국", "https://wagu-book-gitget-deploy-bucket.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20240712_232704958_02.jpg");
        create((long) (Math.random() * 99),"경기 수원시 장안구 광교산로 138", 37.29967995072292, 127.03019643613152, "육전", Category.KOREAN,"상전", "https://wagu-book-gitget-deploy-bucket.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20240712_232704958_01.jpg");
    }
}

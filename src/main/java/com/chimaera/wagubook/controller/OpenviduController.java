package com.chimaera.wagubook.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.chimaera.wagubook.entity.*;
import com.chimaera.wagubook.service.LiveRoomService;
import com.chimaera.wagubook.service.MemberService;
import com.chimaera.wagubook.service.StoreService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.openvidu.java.client.Connection;
import io.openvidu.java.client.ConnectionProperties;
import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.openvidu.java.client.Session;
import io.openvidu.java.client.SessionProperties;

@RestController
@RequiredArgsConstructor
public class OpenviduController {

    private final MemberService memberService;
    private final StoreService storeService;
    private final LiveRoomService liveRoomService;

    @Value("${OPENVIDU_URL}")
    private String OPENVIDU_URL;

    @Value("${OPENVIDU_SECRET}")
    private String OPENVIDU_SECRET;

    private OpenVidu openvidu;
    private final Map<String, Long> connectionMemberMap = new ConcurrentHashMap<>(); // <token, memberId> 를 매핑
    private final Map<String, Long> sessionCreatorMap = new ConcurrentHashMap<>(); // <sessionId, creatorMemberId> 를 매핑

    @PostConstruct
    public void init() {
        this.openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    /**
     * Address, storeName을 받아서 store를 가져오는 api
     */
    @GetMapping("/api/stores")
    public ResponseEntity<Map<String, Object>> getStore(@ RequestParam Location location, @ RequestParam String storeName) {
        Map<String, Object> response = new HashMap<>();
        response.put("store", storeService.findByStoreLocationAndStoreName(location, storeName));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * @param params The Session properties
     * @return The Session ID and member ID
     * TODO: liveRoom을 생성. store와 liveRoom을 1:N. store에서 live를 하고 있는 사람들을 찾아내야 함.
     */
    @PostMapping("/api/sessions")
    public ResponseEntity<Map<String, Object>> initializeSession(@RequestBody(required = false) Map<String, Object> params, HttpSession httpSession)
            throws OpenViduJavaClientException, OpenViduHttpException {

        // 멤버 확인
        Long memberId = (Long) httpSession.getAttribute("memberId");

        // 라이브를 켬
        memberService.turnLive(memberId);

        // 세션 생성
        SessionProperties properties = SessionProperties.fromJson(params).build();
        Session session = openvidu.createSession(properties);
        System.out.println("=====================session 연결 : " + session.getSessionId());

        // storeLocation과 storeName 추출
        Map<String, Object> storeLocationMap = (Map<String, Object>) params.get("storeLocation");
        String storeName = (String) params.get("storeName");

        // storeLocation 객체 생성
        String address = (String) storeLocationMap.get("address");
        double posx = ((Number) storeLocationMap.get("posx")).doubleValue();
        double posy = ((Number) storeLocationMap.get("posy")).doubleValue();
        Location storeLocation = new Location();
        storeLocation.setAddress(address);
        storeLocation.setPosx(posx);
        storeLocation.setPosy(posy);

        // Store 객체 찾기
        Store store = storeService.findByStoreLocationAndStoreName(storeLocation, storeName);
        // store가 없으면 새로 생성
        if (store == null) {
            store = Store.newBuilder()
                    .storeName(storeName)
                    .storeLocation(storeLocation)
                    .build();
            storeService.save(store); // 새 Store 객체 저장
        }
        // 기존 LiveRoom 확인
        LiveRoom existingLiveRoom = memberService.findLiveRoomByMemberId(memberId);


        System.out.println("=====================liveRoom 연결 : " + existingLiveRoom);
        // 기존 LiveRoom이 있으면 삭제
        if(existingLiveRoom != null){
            liveRoomService.deleteLiveRoom(existingLiveRoom.getSessionId());
        }

        // 기존 LiveRoom이 없으면 새로 생성
        LiveRoom liveRoom = LiveRoom.newBuilder()
                .member(memberService.findById(memberId))
                .sessionId(session.getSessionId())
                .store(store)
                .build();

        memberService.saveLiveRoom(liveRoom);

        //이미 LiveRoomParticipant 일 경우 추가하지 않음
        LiveRoomParticipant existingLiveRoomParticipant = liveRoomService.getLiveRoomParticipantByMemberId(memberId);
        if(existingLiveRoomParticipant != null){
            liveRoomService.deleteLiveRoomParticipant(existingLiveRoomParticipant.getMember().getId());
        }

        //LiveRoomParticipant 추가
        LiveRoomParticipant liveRoomParticipant = LiveRoomParticipant.newBuilder()
                .member(memberService.findById(memberId))
                .sessionId(session.getSessionId())
                .liveRoom(liveRoom)
                .build();


        liveRoomService.saveLiveRoomParticipant(liveRoomParticipant);

        // 세션 생성자 정보 저장
        sessionCreatorMap.put(session.getSessionId(), memberId);

        // 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getSessionId());
        response.put("memberId", memberId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * @param sessionId The Session in which to create the Connection
     * @param params    The Connection properties
     * @return The Token associated to the Connection and member ID
     */
    @PostMapping("/api/sessions/{sessionId}/connections")
    public ResponseEntity<Map<String, Object>> createConnection(@PathVariable("sessionId") String sessionId,
                                                                @RequestBody(required = false) Map<String, Object> params, HttpSession httpSession)
            throws OpenViduJavaClientException, OpenViduHttpException {

        // 멤버 확인.
        Long memberId = (Long) httpSession.getAttribute("memberId");

        // 세션 연결
        Session session = openvidu.getActiveSession(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);


        // 연결한 사용자 ID 저장
        connectionMemberMap.put(connection.getToken(), memberId);

        //이미 LiveRoomParticipant 일 경우 추가하지 않음
        LiveRoomParticipant existingLiveRoomParticipant = liveRoomService.getLiveRoomParticipantByMemberId(memberId);
        if(existingLiveRoomParticipant != null){
            liveRoomService.deleteLiveRoomParticipant(existingLiveRoomParticipant.getMember().getId());
        }

        // LiveRoomParticipant 추가
        Member member = memberService.findById(memberId);
        LiveRoom liveRoom = liveRoomService.getLiveRoomBySessionId(sessionId); // LiveRoom 찾기
        if(liveRoom != null){
            LiveRoomParticipant liveRoomParticipant = LiveRoomParticipant.newBuilder()
                    .member(member)
                    .sessionId(sessionId)
                    .liveRoom(liveRoom)
                    .build();
            liveRoomService.saveLiveRoomParticipant(liveRoomParticipant);
        }

        // 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("token", connection.getToken());
        response.put("memberId", memberId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 방 만든 사람의 memberId를 반환.
     * @param sessionId The Session ID
     * @return The member ID of the session creator
     */
    @GetMapping("/api/sessions/{sessionId}/creator")
    public ResponseEntity<Map<String, Object>> getSessionCreator(@PathVariable("sessionId") String sessionId, HttpSession httpSession) {
        Long creatorMemberId = sessionCreatorMap.get(sessionId);
        if (creatorMemberId == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Boolean isCreator = (creatorMemberId.equals((Long) httpSession.getAttribute("memberId")));

        Map<String, Object> response = new HashMap<>();
        response.put("creatorMemberId", creatorMemberId);
        response.put("isCreator", isCreator);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * TODO: 방에 참여한 사람이 나갔을 때 Live_Room_Participant에서 삭제.
     */
    @DeleteMapping("/api/sessions/{sessionId}/member/{memberId}")
    public ResponseEntity<String> closeConnection(@PathVariable("sessionId") String sessionId, @PathVariable("memberId") Long memberId) {
        LiveRoomParticipant liveRoomParticipant = liveRoomService.getLiveRoomParticipantByMemberId(memberId);
        if (liveRoomParticipant == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        liveRoomService.deleteLiveRoomParticipant(memberId);
        return new ResponseEntity<>("라이브스트리밍 참여자가 나갔습니다.", HttpStatus.OK);
    }

    /**
     * 라이브를 종료하고 세션을 삭제.
     * TODO: 종료 시 DB에서 LiveRoom에서도 제거. store에 있는 liveRoom list에서 제거.
     *
     */
    @DeleteMapping("/api/sessions/{sessionId}")
    public ResponseEntity<String> closeSession(@PathVariable("sessionId") String sessionId, HttpSession httpSession) {
        Long memberId = (Long) httpSession.getAttribute("memberId");
        memberService.turnLive(memberId);

        Session session = openvidu.getActiveSession(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            LiveRoom liveRoom = liveRoomService.getLiveRoomBySessionId(sessionId);
            if (liveRoom != null) {
                liveRoomService.deleteLiveRoom(liveRoom.getSessionId());
            }
            session.close();
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            throw new RuntimeException(e);
        }
        sessionCreatorMap.remove(sessionId);

        return new ResponseEntity<>("라이브스트리밍이 종료되었습니다.", HttpStatus.OK);
    }


    @PostMapping("/api/sessions/voice")
    public ResponseEntity<Map<String, Object>> initializeSessionVoice(@RequestBody(required = false) Map<String, Object> params, HttpSession httpSession)
            throws OpenViduJavaClientException, OpenViduHttpException {

        // 멤버 확인
        Long memberId = (Long) httpSession.getAttribute("memberId");

        // 세션 생성
        SessionProperties properties = SessionProperties.fromJson(params).build();
        Session session = openvidu.createSession(properties);
        System.out.println("=====================session voice 연결 : " + session.getSessionId());

        // 세션 생성자 정보 저장
        sessionCreatorMap.put(session.getSessionId(), memberId);

        // 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getSessionId());
        response.put("memberId", memberId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * @param sessionId The Session in which to create the Connection
     * @param params    The Connection properties
     * @return The Token associated to the Connection and member ID
     */
    @PostMapping("/api/sessions/{sessionId}/connections/voice")
    public ResponseEntity<Map<String, Object>> createConnectionVoice(@PathVariable("sessionId") String sessionId,
                                                                @RequestBody(required = false) Map<String, Object> params, HttpSession httpSession)
            throws OpenViduJavaClientException, OpenViduHttpException {

        // 멤버 확인.
        Long memberId = (Long) httpSession.getAttribute("memberId");

        // 세션 연결
        Session session = openvidu.getActiveSession(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);

        System.out.println("=====================connection voice 연결 : " + connection.getToken());
        System.out.println("=====================connection voice 연결 : " + memberId);

        // 연결한 사용자 ID 저장
        connectionMemberMap.put(connection.getToken(), memberId);

        // LiveRoomParticipant 추가
        LiveRoom liveRoom = liveRoomService.getLiveRoomBySessionId(sessionId);
        if (liveRoom != null) {
            LiveRoomParticipant liveRoomParticipant = LiveRoomParticipant.newBuilder()
                    .member(memberService.findById(memberId))
                    .sessionId(session.getSessionId())
                    .liveRoom(liveRoom)
                    .build();
            liveRoomService.saveLiveRoomParticipant(liveRoomParticipant);
        }

        // 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("token", connection.getToken());
        response.put("memberId", memberId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 현재 session 방에 몇 명이 있는지 return 하는 api
     * Integer 로 return.
     */
    @GetMapping("/api/sessions/{sessionId}/connections")
    public ResponseEntity<Integer> getConnections(@PathVariable("sessionId") String sessionId) {
        List<LiveRoomParticipant> liveRoomParticipant = liveRoomService.getLiveRoomParticipantBySessionId(sessionId);
        for (LiveRoomParticipant roomParticipant : liveRoomParticipant) {
            System.out.println(roomParticipant.getMember().getId());
        }
        return new ResponseEntity<>(liveRoomParticipant.size(), HttpStatus.OK);
    }
    /**
     * 현재 얘가 session 방에 있는 앤지 check 하는 api
     * boolean으로 return.
     */
    @GetMapping("/api/sessions/find/{sessionId}")
    public ResponseEntity<Boolean> findSession(@PathVariable("sessionId") String sessionId, HttpSession httpSession) {
        Long memberId = (Long) httpSession.getAttribute("memberId");
        List<LiveRoomParticipant> liveRoomParticipant = liveRoomService.getLiveRoomParticipantBySessionId(sessionId);
        for (LiveRoomParticipant roomParticipant : liveRoomParticipant) {
            if (roomParticipant.getMember().getId().equals(memberId)) {
                return new ResponseEntity<>(true, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(false, HttpStatus.OK);
    }
}

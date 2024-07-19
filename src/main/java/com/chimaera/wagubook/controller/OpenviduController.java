package com.chimaera.wagubook.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.chimaera.wagubook.service.MemberService;
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

        System.out.println("=====================connection 연결 : " + connection.getToken());
        System.out.println("=====================connection 연결 : " + memberId);

        // 연결한 사용자 ID 저장
        connectionMemberMap.put(connection.getToken(), memberId);

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
        Boolean isCreator = (creatorMemberId == (Long) httpSession.getAttribute("memberId"));

        Map<String, Object> response = new HashMap<>();
        response.put("creatorMemberId", creatorMemberId);
        response.put("isCreator", isCreator);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 라이브를 종료하고 세션을 삭제.
     * TODO: 종료 시 DB에서 LiveRoom에서도 제거. store에 있는 liveRoom list에서 제거.
     */
    @DeleteMapping("/api/sessions/{sessionId}")
    public ResponseEntity<String> closeSession(@PathVariable("sessionId") String sessionId, HttpSession httpSession) {
        // 멤버 확인
        Long memberId = (Long) httpSession.getAttribute("memberId");

        // 라이브를 끔
        memberService.turnLive(memberId);

        // 세션 삭제
        Session session = openvidu.getActiveSession(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            session.close();
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            throw new RuntimeException(e);
        }
        sessionCreatorMap.remove(sessionId);

        return new ResponseEntity<>("라이브스트리밍이 종료되었습니다.", HttpStatus.OK);
    }
}

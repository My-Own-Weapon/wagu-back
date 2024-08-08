package com.chimaera.wagubook.controller;
import com.chimaera.wagubook.dto.request.WebSocketRequest;
import com.chimaera.wagubook.dto.response.WebSocketResponse;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.repository.webRTC.SessionRepository;
import com.chimaera.wagubook.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chimaera.wagubook.exception.ErrorCode.SESSION_ROOM_NOT_FOUND;

public class ChatSignalHandler extends TextWebSocketHandler {
    @Autowired
    private MemberService memberService;
    private final SessionRepository sessionRepository = SessionRepository.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<WebSocketSession> sessionList = new ArrayList<>();

    // 데이터 타입
    private static final String MSG_TYPE_JOIN_ROOM = "join_room";
    private static final String MSG_TYPE_CHAT = "chat";

    /**
     * 웹 소켓이 연결되면 실행되는 메서드
     * 어떤 session인지 출력
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session){
        System.out.println("웹 소켓 연결 성공 session : " + session);
        Long memberId = (Long)session.getAttributes().get("memberId");
        System.out.println("member Id : " + memberId);
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
//        sessionList.add(session);
    }

    @Override
    protected void handleTextMessage(final WebSocketSession session, final TextMessage message) throws Exception {
        Long memberId = (Long)(session.getAttributes().get("memberId"));
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        // 수신된 메시지를 JSON 객체로 파싱
        String payload = message.getPayload();
        WebSocketRequest data = new ObjectMapper().readValue(payload, WebSocketRequest.class);
        String type = data.getType();
        String roomURL = data.getRoomURL();

        // 타입에 따라 다른 로직을 적용할 수 있다 (P2P연결을 위한 기능)
        //SDP 오퍼(SDP Offer): 피어 A가 연결을 시작하기 위해 자신의 미디어 세션 설정을 피어 B에게 보내는 메시지
        //SDP 응답(SDP Answer): 피어 B가 SDP 오퍼를 수신하고 자신의 설정을 포함하여 응답하는 메시지
        //ICE 후보자(ICE Candidates): P2P 연결을 설정하기 위해 피어들이 교환하는 네트워크 경로 정보
        //React에서 P2P연결을 하기 위해 필요하다 특정 누군가에 대한게 아니라 브로드케스드


        switch (type){
            /**
             * MSG_TYPE_JOIN_ROOM : 처음 방에 입장했을 때
             * 방을 시작하는 경우 (스트리머) - 새로운 방을 생성하고 Client(방장)의 세션 정보를 저장
             * 방에 참여하는 경우 (시청자) - sessionList에 Client 세션 정보를 저장
             */
            case MSG_TYPE_JOIN_ROOM:
                //방이 존재할 때
                if(sessionRepository.isExistRoom(roomURL)){
                    sessionRepository.addClient(roomURL, session);
                }
                //방을 새로 만들 때
                else{
                    sessionRepository.addClientInNewRoom(roomURL, session);
                }
                //이 세션이 어느 방에 들어가 있는지 저장
                sessionRepository.saveRoomIdHashMapBySession(session, roomURL);

                //방 안에 유저 이름 저장
                sessionRepository.addUsernameInRoom(session.getId(), data.getSenderName());

                Map<String, WebSocketSession> joinClientList = sessionRepository.getClientList(roomURL);

                //방 안 참가자에게 접속한 사람 정보 전달
                List<String> participantSessionList = new ArrayList<>();
                for (Map.Entry<String, WebSocketSession> entry : joinClientList.entrySet()) {
                    if(entry.getValue() != session){
                        participantSessionList.add(entry.getKey());
                        sendMessage(entry.getValue(),new WebSocketResponse().builder()
                                .type("join")
                                .senderId(data.getSenderId())
                                .senderName(data.getSenderName())
                                .data(data.getData())
                                .build());
                    }
                }

                //방에 참여하고 있던 참가자 리스트(username, sessionId)
                List<String> participantNameList = new ArrayList<>();
                for (Map.Entry<String, WebSocketSession> entry : joinClientList.entrySet()) {
                    if(entry.getValue() != session)
                        participantNameList.add(sessionRepository.getUsernameInRoom(entry.getKey()));
                }

                //접속한 사람에게 방 안 참가자들 정보를 반환
                sendMessage(session, new WebSocketResponse().builder()
                        .type("all_users")
                        .senderId(data.getSenderId())
                        .senderName(data.getSenderName())
                        .data(data.getData())
                        .allUsersNickNames(participantNameList)
                        .build());
                break;

            case MSG_TYPE_CHAT:

                if (sessionRepository.isExistRoom(roomURL)) {
                    Map<String, WebSocketSession> ClientList = sessionRepository.getClientList(roomURL);

                    //방에 존재하는 모든 사람들에게 메시지를 보냄
                    for (WebSocketSession ws : ClientList.values()) {
                        sendMessage(ws,
                                new WebSocketResponse().builder()
                                        .type(data.getType())
                                        .senderId(data.getSenderId())
                                        .senderName(data.getSenderName())
                                        .data(data.getData())
                                        .build());
                    }
                } else {
                    throw new CustomException(SESSION_ROOM_NOT_FOUND);
                }
                break;


            default:
        }
    }

    private void sendMessage(WebSocketSession session, WebSocketResponse message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            System.out.println("============== 발생한 에러 메세지: {}" + e.getMessage());
        }
    }

    // 웹소켓 연결이 끊어지면 실행되는 메소드
    @Override
    @Transactional
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        Long memberId = (Long)session.getAttributes().get("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
        Member findMember = memberService.findById(memberId);

        // 끊어진 세션이 어느방에 있었는지 조회
        String roomUrl = sessionRepository.getRoomUrlToSession(session);

        // 2) 방 참가자들 세션 정보들 사이에서 삭제
        sessionRepository.deleteClient(roomUrl, session);

        // 3) 별도 해당 참가자 세션 정보도 삭제
        sessionRepository.deleteRoomIdToSession(session);

        // 4) 별도 해당 닉네임 리스트에서도 삭제
        sessionRepository.deleteUsernameInRoom(session.getId());
        // 본인 제외 모두에게 전달
        for(Map.Entry<String, WebSocketSession> client : sessionRepository.getClientList(roomUrl).entrySet()){
            sendMessage(client.getValue(),
                    new WebSocketResponse().builder()
                            .type("leave")
                            .senderId(findMember.getUsername())
                            .senderName(findMember.getName())
                            .build());
        }
    }
}
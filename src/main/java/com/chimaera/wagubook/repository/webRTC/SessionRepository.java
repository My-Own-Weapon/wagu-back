package com.chimaera.wagubook.repository.webRTC;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 기능 : 웹소켓에 필요한 세션 정보를 저장, 관리 (싱글톤)
@Slf4j
@Component
@NoArgsConstructor
public class SessionRepository {
    private static SessionRepository sessionRepository;
    /**
     * 방 번호를 사용해서 세션 리스트를 찾을 수 있다.
     * <roomId, <sessionId, session>>
     */
    private final Map<String, Map<String, WebSocketSession>> sessionListInRoom = new ConcurrentHashMap<>();


    /**
     * 세션 아이디를 사용해서 방 번호를 찾을 수 있다.
     * <session, roomId>
     */
    private final HashMap<WebSocketSession, String> roomIdHashMapBySessionId = new HashMap<>();


    /**
     * 방에 있는 사용자 이름 정보를 저장
     * <sessionId, username>
     */
    private final Map<String, String> usernamesBySessionId = new HashMap<>();


    /**
     * Session 데이터를 공통으로 사용하기 위해 싱글톤으로 구현
     */
    public static SessionRepository getInstance(){
        if(sessionRepository == null){
            synchronized (SessionRepository.class){
                sessionRepository = new SessionRepository();
            }
        }
        return sessionRepository;
    }

    /**
     * 방이 존재하는지 확인하는 로직
     */
    public boolean isExistRoom(String roomURL) {
        return sessionListInRoom.containsKey(roomURL);
    }

    /**
     * 방에 클라이언트가 참여했을 때, 세션 리스트에 추가하는 로직
     */
    public void addClient(String roomURL, WebSocketSession session) {
        sessionListInRoom.get(roomURL).put(session.getId(), session);
    }

    /**
     * 방이 처음 생성될 때, 생성 후 세션 리스트에 추가
     */
    public void addClientInNewRoom(String roomURL, WebSocketSession session) {
        Map<String, WebSocketSession> newClient = new HashMap<>();
        newClient.put(session.getId(), session);
        sessionListInRoom.put(roomURL, newClient);
    }

    /**
     * 방에 클라이언트가 참여하면 session으로 방을 찾을 수 있도록 session과 room을 매핑한다.
     */
    public void saveRoomIdHashMapBySession(WebSocketSession session, String roomURL) {
        roomIdHashMapBySessionId.put(session, roomURL);
    }

    public Map<String, WebSocketSession> getClientList(String roomURL) {
        return sessionListInRoom.get(roomURL);
    }

    public void addUsernameInRoom(String sessionId, String username) {
        this.usernamesBySessionId.put(sessionId, username);
    }

    /**
     * sessionId로 사용자 이름을 조회
     */
    public String getUsernameInRoom(String sessionId) {
        return this.usernamesBySessionId.get(sessionId);
    }

    public String getRoomUrlToSession(WebSocketSession session) {
        return roomIdHashMapBySessionId.get(session);
    }

    public void deleteClient(String roomUrl, WebSocketSession session) {
        Map<String, WebSocketSession> clientList = sessionListInRoom.get(roomUrl);
        String removeSessionId = "";
        for (Map.Entry<String, WebSocketSession> client : clientList.entrySet()) {
            if(client.getKey().equals(session.getId())){
                removeSessionId = client.getKey();
                break;
            }
        }
        clientList.remove(removeSessionId);
        sessionListInRoom.remove(removeSessionId);

    }

    public void deleteRoomIdToSession(WebSocketSession session) {
        roomIdHashMapBySessionId.remove(session);
    }


    public void deleteUsernameInRoom(String sessionId) {
        usernamesBySessionId.remove(sessionId);
    }
}

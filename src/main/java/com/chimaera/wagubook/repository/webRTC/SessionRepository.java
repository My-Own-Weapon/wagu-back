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
     * 세션 아이디와 사용자 이름(ID) 정보를 매핑
     * <sessionId, <username, name>>
     */
    private final Map<String, Map<String, String>> membersBySessionId = new HashMap<>();


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
        System.out.println("addClientInRoom : " + sessionListInRoom.get(roomURL));
    }

    /**
     * 방이 처음 생성될 때, 생성 후 세션 리스트에 추가
     */
    public void addClientInNewRoom(String roomURL, WebSocketSession session) {
        Map<String, WebSocketSession> newClient = new HashMap<>();
        newClient.put(session.getId(), session);
        sessionListInRoom.put(roomURL, newClient);
        System.out.println("addClientInNewRoom : " + sessionListInRoom.get(roomURL));
    }

    /**
     * 방에 클라이언트가 참여하면 session으로 방을 찾을 수 있도록 session과 room을 매핑한다.
     */
    public void saveRoomIdHashMapBySession(WebSocketSession session, String roomURL) {
        roomIdHashMapBySessionId.put(session, roomURL);
    }

    public Map<String, WebSocketSession> getClientList(String roomURL) {
        if(sessionListInRoom.isEmpty())
            return null;
        return sessionListInRoom.get(roomURL);
    }

    public void saveMemberToSessionId(String sessionId, String username, String name) {
        Map<String, String> newClient = new HashMap<>();
        newClient.put("username", username);
        newClient.put("name", name);
        this.membersBySessionId.put(sessionId, newClient);
    }

    /**
     * sessionId로 사용자 이름을 조회
     */
    public Map<String, String> getMemberToSessionId(String sessionId) {
        return this.membersBySessionId.get(sessionId);
    }

    public String getRoomUrlToSession(WebSocketSession session) {
        if(roomIdHashMapBySessionId.isEmpty())
            return null;
        return roomIdHashMapBySessionId.get(session);
    }

    public void deleteClient(String roomUrl, WebSocketSession session) {
        System.out.println(roomUrl);
        Map<String, WebSocketSession> clientList = sessionListInRoom.get(roomUrl);
        System.out.println("[deleteClient] clientList : " + clientList);
        clientList.remove(session.getId());
        if(clientList.isEmpty())
            sessionListInRoom.remove(roomUrl);

    }

    public void deleteRoomIdToSession(WebSocketSession session) {
        roomIdHashMapBySessionId.remove(session);
    }


    public void deleteUsernameInRoom(String sessionId) {
        membersBySessionId.remove(sessionId);
    }
}

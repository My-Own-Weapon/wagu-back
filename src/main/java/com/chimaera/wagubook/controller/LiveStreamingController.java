package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.CreateLiveRoomRequest;
import com.chimaera.wagubook.dto.SendMessageRequest;
import com.chimaera.wagubook.entity.ChatMessage;
import com.chimaera.wagubook.entity.LiveRoom;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.Store;
import com.chimaera.wagubook.service.LiveRoomService;
import com.chimaera.wagubook.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class LiveStreamingController {

    private final LiveRoomService liveRoomService;
    private final MemberService memberService;


    @PostMapping
    public LiveRoom createLiveRoom(@RequestBody CreateLiveRoomRequest request, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        Member member = memberService.findById(memberId);

        Store store = request.getStoreName() == null ? null : new Store(request.getStoreName());
        return liveRoomService.createLiveRoom(member, store, request.getTitle());
    }

    @GetMapping("/followers")
    public List<LiveRoom> getFollowedLiveRooms(HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        Member member = new Member(memberId);

        return liveRoomService.getFollowedLiveRooms(member);
    }

    @GetMapping("/map")
    public List<LiveRoom> getStoreLiveRooms(@RequestParam String storeName) {
        return liveRoomService.getStoreLiveRooms(storeName);
    }

    @GetMapping("/{room_id}")
    public LiveRoom getLiveRoomDetails(@PathVariable Long room_id) {
        return liveRoomService.getLiveRoomDetails(room_id);
    }

    @PostMapping("/{room_id}")
    public void enterLiveRoom(@PathVariable Long room_id, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        Member member = new Member(memberId);

        liveRoomService.enterLiveRoom(room_id, member);
    }

    @PostMapping("/{room_id}/exit")
    public void exitLiveRoom(@PathVariable Long room_id, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        Member member = new Member(memberId);

        liveRoomService.exitLiveRoom(room_id, member);
    }

    @PostMapping("/{room_id}/capture")
    public void captureLiveRoom(@PathVariable Long room_id, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        Member member = new Member(memberId);

        liveRoomService.captureLiveRoom(room_id, member);
    }

    @DeleteMapping("/{room_id}/exit")
    public void endLiveRoom(@PathVariable Long room_id, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        Member member = new Member(memberId);

        liveRoomService.endLiveRoom(room_id, member);
    }

    @PostMapping("/{room_id}/messages")
    public void sendMessage(@PathVariable Long room_id, @RequestBody SendMessageRequest request, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        Member member = new Member(memberId);

        liveRoomService.sendMessage(room_id, member, request.getMessage());
    }

    @GetMapping("/{room_id}/messages")
    public List<ChatMessage> getMessages(@PathVariable Long room_id) {
        return liveRoomService.getMessages(room_id);
    }
}

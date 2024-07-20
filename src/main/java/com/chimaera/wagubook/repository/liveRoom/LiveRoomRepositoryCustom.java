package com.chimaera.wagubook.repository.liveRoom;

import com.chimaera.wagubook.entity.LiveRoom;

import java.util.List;

public interface LiveRoomRepositoryCustom {
    public List<LiveRoom> findAllFollowedRooms(Long memberId);
}

package com.chimaera.wagubook.repository.liveRoom;

import com.chimaera.wagubook.entity.LiveRoom;
import com.chimaera.wagubook.entity.QFollow;
import com.chimaera.wagubook.entity.QLiveRoom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class LiveRoomRepositoryImpl implements LiveRoomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<LiveRoom> findAllFollowedRooms(Long memberId){
        QLiveRoom liveRoom = QLiveRoom.liveRoom;
        QFollow follow = QFollow.follow;

        return queryFactory.selectFrom(liveRoom)
                .where(liveRoom.member.id
                        .in(queryFactory.select(follow.fromMember.id)
                                .from(follow)
                                .where(follow.toMember.id.eq(memberId))))
                .fetch();
    }
}

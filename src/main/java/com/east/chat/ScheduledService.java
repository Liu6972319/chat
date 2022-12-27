package com.east.chat;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.db.nosql.redis.RedisDS;
import cn.hutool.json.JSONUtil;
import com.east.chat.room.RoomEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

@Slf4j
@Component
public class ScheduledService {

    /**
     * 定时检查房间时间
     */
    @Scheduled(fixedRate = 1000)
    public void scheduled(){
        Set<String> room = RedisDS.create().getJedis().zrangeByLex("rooms", "-", "+");
        for (String roomStr : room) {
            RoomEntity roomEntity = JSONUtil.toBean(roomStr, RoomEntity.class);
            long between = DateUtil.between(roomEntity.getDate(), new Date(), DateUnit.HOUR);
            int hour = roomEntity.getHour();
            if (hour <= between){
                // 删除
                RedisDS.create().getJedis().zrem("rooms",roomStr);
            }
        }
    }
}

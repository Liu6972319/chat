package com.east.chat.room;

import cn.hutool.db.nosql.redis.RedisDS;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.east.chat.AjaxResult;
import com.east.chat.common.RabbitCommon;
import com.east.chat.websocket.entity.SocketEntity;
import com.east.chat.websocket.util.WsSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("room")
public class RoomController {

    @Autowired
    RedissonClient redissonClient;

    private static String roomKey = "rooms:";

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    /**
     * 加入房间
     *
     * @param roomId
     * @param userId
     * @return
     */
    @PostMapping("join")
    @ResponseBody
    public AjaxResult join(String roomId, String userId) {
        redissonClient.getLock("joinLockKey:" + roomId).lock(10, TimeUnit.SECONDS);

        List<String> lrange = redisTemplate.opsForList().range(roomKey + roomId, 0, -1);
        if (lrange != null && !lrange.contains(userId)) {

            // 告知其他用户有用户上线 让其更新 userList
            for (String item : lrange) {
                JSONObject data = new JSONObject();
                data.set("users", JSONUtil.parseArray(Collections.singletonList(userId)));
                SocketEntity join = new SocketEntity("join", data);
                WsSessionManager.send(item, join);
                log.info("告知 {} 加入 {}", item, userId);
            }

            // 放在前端创建 offer
            // 添加用户到房间
            redisTemplate.opsForList().leftPush(roomKey + roomId, userId);
        }

        redissonClient.getLock("joinLockKey:" + roomId).unlock();

        return AjaxResult.success(lrange);
    }


    @PostMapping("joinSuccess")
    @ResponseBody
    public AjaxResult joinSuccess(String roomId, String userId) {
        redissonClient.getLock("joinLockKey:" + roomId).unlock();
//        JedisLock.releaseLock("joinLockKey:" + roomId, userId);
        return AjaxResult.success();
    }

    /**
     * 退出房间
     *
     * @param roomId
     * @param userId
     * @return
     */
    @PostMapping("exit")
    @ResponseBody
    public AjaxResult exit(String roomId, String userId) {
        synchronized (this) {
            Jedis jedis = RedisDS.create().getJedis();
            jedis.lrem(roomKey + roomId, 0, userId);
            // 通知其他用户 `我` 下线了
            List<String> lrange = jedis.lrange(roomKey + roomId, 0, -1);
            if (lrange != null && lrange.size() > 0) {
                List<String> collect = lrange.stream().distinct().collect(Collectors.toList());

                collect.forEach(item -> {
                    JSONObject object = new JSONObject();
                    object.putOnce("userId", userId);
                    WsSessionManager.send(item, new SocketEntity("exit", object));
                });
            }
            return AjaxResult.success();
        }
    }
}

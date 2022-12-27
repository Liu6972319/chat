package com.east.chat.room;

import cn.hutool.db.nosql.redis.RedisDS;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.east.chat.AjaxResult;
import com.east.chat.common.RabbitCommon;
import com.east.chat.util.JedisLock;
import com.east.chat.websocket.entity.SocketEntity;
import com.east.chat.websocket.util.WsSessionManager;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("room")
public class RoomController {

    @Autowired
    AmqpTemplate amqpTemplate;

    private static String roomKey = "rooms:";

    /**
     * 将房间用户信息存储进入redis中
     * <roomId,<List<userId>>
     *
     * @param roomId
     * @param userId
     * @return
     */
//    @PostMapping("push")
//    @ResponseBody
//    public AjaxResult push(String roomId, String userId) {
//        RedisDS.create().getJedis().lpush(roomKey + roomId, userId);
//        return AjaxResult.success();
//    }

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
        synchronized (this) {
            List<String> lrange = redisTemplate.opsForList().range(roomKey + roomId, 0, -1);
            if (lrange != null && !lrange.contains(userId)) {
                // 添加用户到房间
                redisTemplate.opsForList().leftPush(roomKey + roomId, userId);
                // 告知其他用户有用户上线 让其更新 userList
                for (String item : lrange) {
                    WebSocketSession webSocketSession = WsSessionManager.get(item);
                    if (webSocketSession != null) {
                        try {
                            JSONObject data = new JSONObject();
                            data.set("users", JSONUtil.parseArray(Collections.singletonList(userId)));
                            SocketEntity join = new SocketEntity("join", data);
                            webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(join)));
                            log.info("告知 {} 加入 {}", item, userId);
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                            throw new RuntimeException(e.getMessage());
                        }
                    }
                }
                // 设置用户待连接
                Map<String, String> map = new HashMap<>();
                map.put("userId", userId);
                map.put("roomId", roomId);
                amqpTemplate.convertAndSend(RabbitCommon.JOIN_QUEUE, map);
            }

            return AjaxResult.success(lrange);
        }
        // 获取房间用户
//            Jedis instance = RedisDS.create().getJedis();
//        if (JedisLock.tryLock("joinLockKey:" + roomId, userId, 30000)) {
//
//
//        }
//        return AjaxResult.error();
    }


    @PostMapping("joinSuccess")
    @ResponseBody
    public AjaxResult joinSuccess(String roomId, String userId) {
        JedisLock.releaseLock("joinLockKey:" + roomId, userId);
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
                    try {
                        JSONObject object = new JSONObject();
                        object.putOnce("userId", userId);
                        WebSocketSession webSocketSession = WsSessionManager.get(item);
                        if (webSocketSession != null) {
                            webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(new SocketEntity("exit", object))));
                            log.info("告知 {} ，用户 {} 退出房间 {} ",item,userId,roomId);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            return AjaxResult.success();
        }
    }

    /**
     * 接收 userId 告知该 userId 创建 offer
     *
     * @param message
     * @throws IOException
     */
    @RabbitListener(queues = RabbitCommon.JOIN_QUEUE)
    @RabbitHandler
    public void joinQueueHandler(Map<String, String> map, Message message, Channel channel) throws IOException {
        String userId = map.get("userId");
        String roomId = map.get("roomId");
        try {
            if (JedisLock.tryLock("joinLockKey:" + roomId, userId, 3000)) {
                WebSocketSession webSocketSession = WsSessionManager.get(userId);
                if (webSocketSession != null) {
                    // 告知加入的用户创建 offer
                    SocketEntity join = new SocketEntity("createOffer", new JSONObject());
                    webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(join)));
                    log.info("告知 {} 创建 offer" , userId);
                }
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
                //log.info("消费消息确认 {}，接收到了回调方法",message.getMessageProperties().getConsumerQueue());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            JedisLock.releaseLock("joinLockKey:" + roomId, userId);
            //重新回到队列
            //requeue = true 重回队列，false 丢弃
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
        }
    }


}

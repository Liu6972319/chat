package com.east.chat.websocket;

import cn.hutool.db.nosql.redis.RedisDS;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.east.chat.websocket.entity.SocketEntity;
import com.east.chat.websocket.util.WsSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class WebSocketController implements WebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Object id = session.getAttributes().get("id");
        if (id != null){
            WsSessionManager.add(id.toString(), session);
        }
//        WsSessionManager.add(session.getId(),session);
        log.info("打开一个 WebSocket: {}。用户id:{},当前连接数: {}", session.getId(), id, WsSessionManager.onLineSize());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object id = session.getAttributes().get("id");
        if (id != null ){
            WsSessionManager.remove(id.toString());
//        WsSessionManager.remove(session.getId());
            //清理下线用户 避免造成redis垃圾数据
            clearCloseUser(id.toString());
        }

        log.info("关闭一个 WebSocket: {}。用户id:{},当前连接数: {}", session.getId(), id, WsSessionManager.onLineSize());
    }

    @Override
    public void handleMessage(WebSocketSession wsSession, WebSocketMessage<?> message) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        SocketEntity socketEntity = JSONUtil.toBean((String) message.getPayload(), SocketEntity.class);
        // 反射找到类
        Class<?> clz = Class.forName("com.east.chat.websocket.handle.SocketMessageHandle");
        // 获取方法
        Method method = clz.getMethod(socketEntity.getMethod(), WebSocketSession.class, SocketEntity.class);
        if (socketEntity.getData() != null) {
            method.invoke(clz.newInstance(), wsSession, socketEntity);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("webSocket 连接出现异常。断开……");
        log.error(exception.getMessage());
        if (session.isOpen()) {
            session.close();
        }
        Object id = session.getAttributes().get("id");
        if (id !=null){
            WsSessionManager.remove(id.toString());
//        WsSessionManager.remove(session.getId());
            clearCloseUser(id.toString());
        }
    }

    /*
     * 是否支持消息拆分发送：如果接收的数据量比较大，最好打开(true), 否则可能会导致接收失败。
     * 如果出现WebSocket连接接收一次数据后就自动断开，应检查是否是这里的问题。
     */
    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    /**
     * 清理下线用户
     *
     * @param id
     */
    public void clearCloseUser(String id) {
        // 清除 redis 无效数据
        Jedis jedis = RedisDS.create().getJedis();
        Set<String> keys = jedis.keys("rooms:*");
        keys.forEach(key -> {
            List<String> lrange = jedis.lrange(key, 0, -1);
            if (lrange.contains(id)) {
                lrange.remove(id);
                jedis.lrem(key, 0, id);
                for (String userId : lrange) {
                    WebSocketSession webSocketSession = WsSessionManager.get(userId);
                    if (webSocketSession != null) {
                        synchronized (webSocketSession) {
                            try {
                                JSONObject object = new JSONObject();
                                object.putOnce("userId", id);
                                webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(new SocketEntity("exit", object))));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        });
    }
}

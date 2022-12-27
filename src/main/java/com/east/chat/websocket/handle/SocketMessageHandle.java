package com.east.chat.websocket.handle;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.east.chat.util.JedisLock;
import com.east.chat.websocket.entity.SocketEntity;
import com.east.chat.websocket.util.WsSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * websocket 消息处理类
 */
@Slf4j
public class SocketMessageHandle {
    /**
     * 心跳检测方法
     */
    public void heartbeat(WebSocketSession session, SocketEntity object) {
        if ((int) object.getData().get("heartbeat") != 1) {
            WsSessionManager.removeAndClose(session.getAttributes().get("id").toString());
        }
    }

    /**
     * 通知其他人 `我` 上线
     *
     * @param session
     * @param object
     */
    public void offer(WebSocketSession session, SocketEntity object) throws IOException {
//        log.info("offer:::::userId:{} -> targetId:{}", data.get("userId"), targetId);
        rtcHandle(session, object);
    }

    private void rtcHandle(WebSocketSession session, SocketEntity object) throws IOException {
        JSONObject data = object.getData();
        String targetId = data.get("targetId").toString();
        WebSocketSession webSocketSession = WsSessionManager.get(targetId);
        if (webSocketSession != null) {
            synchronized (webSocketSession) {
                webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(object)));
            }
        }
    }

    public void candidate(WebSocketSession session, SocketEntity object) throws IOException {
//        log.info("candidate:::::userId:{} -> targetId:{}", data.get("userId"), targetId);
        rtcHandle(session, object);
    }

    public void answer(WebSocketSession session, SocketEntity object) throws IOException {
//        log.info("answer:::::userId:{} -> targetId:{}", data.get("userId"), targetId);
        rtcHandle(session, object);
    }

    public void joinSuccess(WebSocketSession session, SocketEntity object){
        String userId = object.getData().get("userId").toString();
        JedisLock.releaseLock("joinLockKey",userId);
    }

}

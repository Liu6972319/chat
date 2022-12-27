package com.east.chat.websocket;

import cn.hutool.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;

@ServerEndpoint("/ws")
@Component
public class WsServerEndpoint {
    /**
     * 连接成功
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) {
//        WsSessionManager.add("",session.getContainer());
        System.out.println("连接成功");
    }

    /**
     * 连接关闭
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {
        System.out.println("连接关闭");
    }

    /**
     * 接收到消息
     *
     */
    @OnMessage
    public String onMsg(String obj) throws IOException {
        return obj;
    }
}

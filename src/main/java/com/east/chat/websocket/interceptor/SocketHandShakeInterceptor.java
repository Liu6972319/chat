package com.east.chat.websocket.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class SocketHandShakeInterceptor implements HandshakeInterceptor {
    /*
     * 在WebSocket连接建立之前的操作，以鉴权为例
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

//        log.info("在 webSocket 连接之前处理。");

        // 获取url传递的参数，通过attributes在Interceptor处理结束后传递给WebSocketHandler
        // WebSocketHandler可以通过WebSocketSesgetsion的getAttributes()方法获取参数
        ServletServerHttpRequest serverRequest = (ServletServerHttpRequest) request;
        String id = serverRequest.getServletRequest().getParameter("id");
//        String name = serverRequest.getServletRequest().getParameter("name");
//        if (tokenValidation.validateSign()) {
//            log.info("验证通过。 WebSocket 连接.... ");
        if (id != null){
            attributes.put("id", id);
        }
//            attributes.put("name", name);
//            return super.beforeHandshake(request, response, wsHandler, attributes);
//        } else {
//            log.error("验证失败。 WebSocket 不会连接。 ");
//            return false;
//        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
        // 省略
    }
}
